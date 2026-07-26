package dev.elysium.combat.skill;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class SkillManager {

    private final ElysiumCombat plugin;
    // classId -> slotIndex -> Skill
    private final Map<PlayerClass, Map<Integer, Skill>> skills = new EnumMap<>(PlayerClass.class);

    public SkillManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        loadSkills();
    }

    private void loadSkills() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file)
            .getConfigurationSection("skills");
        if (root == null) { plugin.getLogger().warning("skills.yml trong!"); return; }

        for (String className : root.getKeys(false)) {
            try {
                PlayerClass pc = PlayerClass.valueOf(className.toUpperCase());
                ConfigurationSection classSec = root.getConfigurationSection(className);
                Map<Integer, Skill> slotMap = new HashMap<>();

                for (int i = 1; i <= 3; i++) {
                    ConfigurationSection s = classSec.getConfigurationSection("skill" + i);
                    if (s == null) continue;
                    try {
                        SkillEffect effect = SkillEffect.valueOf(s.getString("effect","DAMAGE_AOE").toUpperCase());
                        Map<String, Object> params = new HashMap<>(s.getValues(false));
                        slotMap.put(i, new Skill(
                            s.getString("name","Skill " + i),
                            s.getString("description",""),
                            s.getInt("mana-cost", 20),
                            s.getInt("cooldown", 10),
                            effect, params
                        ));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Loi load skill" + i + " cua " + className);
                    }
                }
                skills.put(pc, slotMap);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public Skill getSkill(PlayerClass pc, int slot) {
        Map<Integer, Skill> slotMap = skills.get(pc);
        return slotMap != null ? slotMap.get(slot) : null;
    }

    // ── Activate ─────────────────────────────────────────────────────────────

    public void activate(Player player, int slot) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if (pc == PlayerClass.NONE) {
            player.sendMessage(ColorUtil.color("&cHay chon class truoc: /combat class"));
            return;
        }

        Skill skill = getSkill(pc, slot);
        if (skill == null) {
            player.sendMessage(ColorUtil.color("&cClass cua ban khong co skill " + slot + "!"));
            return;
        }

        String cdKey = pc + "_skill" + slot;
        if (CoreAPI.getCore().getCooldownManager().has(player.getUniqueId(), cdKey)) {
            long remaining = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
            player.sendMessage(ColorUtil.color("&c" + skill.getName() + " &7dang hoi phuc! &e" + remaining + "s"));
            return;
        }

        if (!CoreAPI.useMana(player, skill.getManaCost())) {
            player.sendMessage(ColorUtil.color("&cKhong du mana! Can: &b" + skill.getManaCost()));
            return;
        }

        // Set cooldown
        CoreAPI.getCore().getCooldownManager().set(
            player.getUniqueId(), cdKey, skill.getCooldownSeconds() * 1000L);

        // Execute
        executeEffect(player, skill);

        // Feedback
        player.sendActionBar(ColorUtil.component("&e" + skill.getName() + " &7kich hoat!"));
    }

    // ── Effects ───────────────────────────────────────────────────────────────

    private void executeEffect(Player player, Skill skill) {
        switch (skill.getEffect()) {
            case DAMAGE_AOE    -> damageAoe(player, skill);
            case DAMAGE_SINGLE -> damageSingle(player, skill);
            case KNOCKBACK     -> knockback(player, skill);
            case BUFF_SELF     -> buffSelf(player, skill);
            case DEBUFF_AOE    -> debuffAoe(player, skill);
            case PROJECTILE    -> projectile(player, skill);
            case DASH          -> dash(player, skill);
            case FIRE_ARROW    -> fireArrow(player, skill);
            case ARROW_RAIN    -> arrowRain(player, skill);
        }
    }

    private void damageAoe(Player player, Skill skill) {
        double radius = skill.get("radius", 4.0);
        double damage = skill.get("damage", 8.0);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0,1,0), 5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
        player.getNearbyEntities(radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(player))
            .forEach(e -> ((LivingEntity)e).damage(damage, player));
    }

    private void damageSingle(Player player, Skill skill) {
        double damage = skill.get("damage", 15.0);
        LivingEntity target = getNearestEnemy(player, 8);
        if (target == null) { player.sendMessage(ColorUtil.color("&cKhong co muc tieu!")); return; }
        player.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0,1,0), 10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.8f);
        target.damage(damage, player);
    }

    private void knockback(Player player, Skill skill) {
        double kb = skill.get("knockback", 3.0);
        int slowDur = skill.get("slow-duration", 60);
        int slowAmp = skill.get("slow-amplifier", 1);
        LivingEntity target = getNearestEnemy(player, 10);
        if (target == null) { player.sendMessage(ColorUtil.color("&cKhong co muc tieu!")); return; }
        Vector dir = target.getLocation().subtract(player.getLocation()).toVector().normalize().setY(0.4);
        target.setVelocity(dir.multiply(kb));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDur, slowAmp));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1f, 1f);
    }

    private void buffSelf(Player player, Skill skill) {
        String potionName = skill.get("potion-type", "SPEED");
        int amplifier = skill.get("potion-amplifier", 1);
        int duration  = skill.get("duration", 100);
        PotionEffectType type = PotionEffectType.getByName(potionName);
        if (type == null) { plugin.getLogger().warning("PotionEffectType khong hop le: " + potionName); return; }
        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0,1,0), 15);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void debuffAoe(Player player, Skill skill) {
        double radius   = skill.get("radius", 5.0);
        String potName  = skill.get("potion-type", "SLOWNESS");
        int amplifier   = skill.get("potion-amplifier", 1);
        int duration    = skill.get("duration", 60);
        PotionEffectType type = PotionEffectType.getByName(potName);
        if (type == null) return;
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0,1,0), 30, radius/2, 1, radius/2, 0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_PLACE, 1f, 0.5f);
        player.getNearbyEntities(radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(player))
            .forEach(e -> ((LivingEntity)e).addPotionEffect(new PotionEffect(type, duration, amplifier)));
    }

    private void projectile(Player player, Skill skill) {
        Fireball fb = player.launchProjectile(Fireball.class);
        fb.setYield(1.5f);
        fb.setIsIncendiary(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.8f);
    }

    private void dash(Player player, Skill skill) {
        double distance = skill.get("dash-distance", 5.0);
        Vector dir = player.getLocation().getDirection().normalize().multiply(distance);
        Location target = player.getLocation().add(dir);
        target.setY(player.getLocation().getY());
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
        player.teleport(target);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }

    private void fireArrow(Player player, Skill skill) {
        double damage = skill.get("damage", 10.0);
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setFireTicks(100);
        // Luu damage vao metadata de xu ly trong CombatListener
        arrow.setMetadata("elysium_skill_damage",
            new org.bukkit.metadata.FixedMetadataValue(plugin, damage));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1.2f);
    }

    private void arrowRain(Player player, Skill skill) {
        double radius = skill.get("radius", 6.0);
        int count  = skill.get("arrow-count", 8);
        double dmg = skill.get("damage", 5.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.7f);
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double x = player.getLocation().getX() + radius * Math.cos(angle);
            double z = player.getLocation().getZ() + radius * Math.sin(angle);
            Location loc = new Location(player.getWorld(), x, player.getLocation().getY() + 10, z);
            Arrow arrow = player.getWorld().spawn(loc, Arrow.class);
            arrow.setVelocity(new Vector(0, -1.5, 0));
            arrow.setMetadata("elysium_skill_damage",
                new org.bukkit.metadata.FixedMetadataValue(plugin, dmg));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private LivingEntity getNearestEnemy(Player player, double radius) {
        return player.getNearbyEntities(radius, radius, radius).stream()
            .filter(e -> e instanceof LivingEntity && !e.equals(player))
            .map(e -> (LivingEntity) e)
            .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
    }
          }
