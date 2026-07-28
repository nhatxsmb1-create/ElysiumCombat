package dev.elysium.combat.skill;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class SkillManager {

    private final ElysiumCombat plugin;
    private final Map<PlayerClass, Map<Integer, Skill>> skills = new EnumMap<>(PlayerClass.class);

    public static final int CMD_SKILL_1 = 2001;
    public static final int CMD_SKILL_2 = 2002;
    public static final int CMD_SKILL_3 = 2003;

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
                ConfigurationSection cs = root.getConfigurationSection(className);
                Map<Integer, Skill> slotMap = new HashMap<>();
                for (int i = 1; i <= 3; i++) {
                    ConfigurationSection s = cs.getConfigurationSection("skill" + i);
                    if (s == null) continue;
                    try {
                        SkillEffect fx = SkillEffect.valueOf(s.getString("effect","BUFF_POTION").toUpperCase());
                        slotMap.put(i, new Skill(
                            s.getString("name","Skill "+i),
                            s.getString("description",""),
                            s.getString("icon",""),
                            s.getInt("mana-cost", 0),
                            s.getInt("cooldown",10),
                            fx, new HashMap<>(s.getValues(false))
                        ));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Loi skill"+i+" cua "+className+": "+e.getMessage());
                    }
                }
                skills.put(pc, slotMap);
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Skills loaded: " + skills.size() + " classes.");
    }

    public Skill getSkill(PlayerClass pc, int slot) {
        Map<Integer,Skill> m = skills.get(pc);
        return m != null ? m.get(slot) : null;
    }

    // ── Skill item ────────────────────────────────────────────────────────────

    public ItemStack buildSkillItem(Player player, PlayerClass pc, int slot) {
        Skill skill = getSkill(pc, slot);
        Material mat = Material.valueOf(plugin.getCombatConfig().getSkillItemMaterial());
        if (skill == null) return new ItemStack(mat);
        ItemStack item = new ItemStack(mat);
        refreshSkillItemMeta(player, item, pc, slot, skill);
        return item;
    }

    public void refreshSkillItemMeta(Player player, ItemStack item, PlayerClass pc, int slot, Skill skill) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String cdKey = pc + "_skill" + slot;
        long rem  = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
        boolean onCd = rem > 0;

        meta.setDisplayName(ColorUtil.color(skill.getIcon() + " &f" + skill.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7" + skill.getDescription()));
        lore.add("");
        lore.add(ColorUtil.color("&7Cooldown: &f" + skill.getCooldownSeconds() + "s"));
        lore.add("");
        lore.add(onCd
            ? ColorUtil.color("&c⏳ Hoi chieu: &e" + rem + "s")
            : ColorUtil.color("&a✔ San sang"));
        lore.add(ColorUtil.color("&8Chuot phai de dung"));

        meta.setLore(lore);
        meta.setCustomModelData(2000 + slot);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(meta);
    }

    public void refreshHotbarSkills(Player player, PlayerClass pc) {
        if (pc == PlayerClass.NONE) return;
        int[] slots = {
            plugin.getCombatConfig().getSkillSlot(1),
            plugin.getCombatConfig().getSkillSlot(2),
            plugin.getCombatConfig().getSkillSlot(3)
        };
        for (int i = 1; i <= 3; i++) {
            ItemStack item = player.getInventory().getItem(slots[i-1]);
            Skill skill = getSkill(pc, i);
            if (item == null || skill == null) continue;
            if (!item.hasItemMeta() || item.getItemMeta().getCustomModelData() != 2000+i) continue;
            refreshSkillItemMeta(player, item, pc, i, skill);
        }
    }

    // ── Activate — chi check cooldown, khong ton mana ─────────────────────────

    public void activate(Player player, int slot) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if (pc == PlayerClass.NONE) {
            player.sendMessage(ColorUtil.color("&cHay chon class truoc: /combat class"));
            return;
        }
        Skill skill = getSkill(pc, slot);
        if (skill == null) return;

        String cdKey = pc + "_skill" + slot;

        // Chi kiem tra cooldown
        if (CoreAPI.getCore().getCooldownManager().has(player.getUniqueId(), cdKey)) {
            long rem = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
            player.sendActionBar(ColorUtil.component("&c" + skill.getName() + " &7hoi chieu! &e" + rem + "s"));
            return;
        }

        // Set cooldown va execute
        CoreAPI.getCore().getCooldownManager().set(
            player.getUniqueId(), cdKey, skill.getCooldownSeconds() * 1000L);

        executeEffect(player, skill);

        player.sendActionBar(ColorUtil.component(skill.getIcon() + " &e" + skill.getName() + " &7kich hoat!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0,1,0), 10);

        refreshHotbarSkills(player, pc);
    }

    // ── Kiem tra skill item (PUBLIC de dung o nhieu noi) ─────────────────────

    public boolean isSkillItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        int cmd = meta.getCustomModelData();
        return cmd >= CMD_SKILL_1 && cmd <= CMD_SKILL_3;
    }

    // ── Effects ───────────────────────────────────────────────────────────────

    private void executeEffect(Player player, Skill skill) {
        switch (skill.getEffect()) {
            case BUFF_POTION  -> buffPotion(player, skill);
            case RESTORE_MANA -> restoreMana(player, skill);
            case HEAL         -> heal(player, skill);
            case DASH         -> dash(player, skill);
        }
    }

    private void buffPotion(Player player, Skill skill) {
        applyPotion(player, skill.get("potion-type","SPEED"),
            skill.get("potion-amplifier",0), skill.get("duration",100));
        String extra = skill.get("extra-potion-type","");
        if (!extra.isBlank()) applyPotion(player, extra,
            skill.get("extra-potion-amplifier",0), skill.get("extra-duration",100));
    }

    private void applyPotion(Player player, String name, int amp, int dur) {
        PotionEffectType type = PotionEffectType.getByName(name);
        if (type == null) return;
        player.addPotionEffect(new PotionEffect(type, dur, amp, false, true, true));
    }

    private void restoreMana(Player player, Skill skill) {
        int amount = skill.get("mana-restore",50);
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep != null) {
            ep.addMana(amount);
            player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0,1,0), 20, 0.5,0.5,0.5, 0.2);
        }
    }

    private void heal(Player player, Skill skill) {
        double amt   = skill.get("heal-amount",6.0);
        int regenDur = skill.get("regen-duration",60);
        int regenAmp = skill.get("regen-amplifier",0);
        player.setHealth(Math.min(player.getHealth()+amt, player.getMaxHealth()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDur, regenAmp));
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 8, 0.5,0.5,0.5,0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.8f);
    }

    private void dash(Player player, Skill skill) {
        double dist = skill.get("dash-distance",6.0);
        player.setVelocity(player.getLocation().getDirection().normalize().multiply(dist).setY(0.3));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15,0.3,0.2,0.3,0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }
}
