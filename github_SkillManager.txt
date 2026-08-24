package dev.elysium.combat.skill;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
        java.io.File file = new java.io.File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        org.bukkit.configuration.file.FileConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        org.bukkit.configuration.ConfigurationSection root = cfg.getConfigurationSection("skills");
        if (root == null) return;
        
        for (String className : root.getKeys(false)) {
            PlayerClass pc;
            try { pc = PlayerClass.valueOf(className.toUpperCase()); }
            catch (Exception e) { continue; }
            
            org.bukkit.configuration.ConfigurationSection classSec = root.getConfigurationSection(className);
            if (classSec == null) continue;
            
            for (int i = 1; i <= 3; i++) {
                org.bukkit.configuration.ConfigurationSection s = classSec.getConfigurationSection("skill" + i);
                if (s == null) continue;
                
                String name = s.getString("name", "Unknown");
                String desc = s.getString("description", "No description");
                String icon = s.getString("icon", "⚔");
                int mana = s.getInt("mana-cost", 0);
                int cd = s.getInt("cooldown", 10);
                
                SkillEffect eff = SkillEffect.BUFF_POTION;
                try { eff = SkillEffect.valueOf(s.getString("effect", "BUFF_POTION").toUpperCase()); }
                catch(Exception ignored) {}
                
                Map<String, Object> params = new java.util.HashMap<>(s.getValues(false));
                
                Skill skill = new Skill(name, desc, icon, mana, cd, eff, params);
                registerSkill(pc, i, skill);
            }
        }
        plugin.getLogger().info("Loaded skills for " + skills.size() + " classes.");
    }

    public void registerSkill(PlayerClass pc, int slot, Skill skill) {
        skills.computeIfAbsent(pc, k -> new java.util.HashMap<>()).put(slot, skill);
    }

    public Skill getSkill(PlayerClass pc, int slot) {
        return skills.getOrDefault(pc, new java.util.HashMap<>()).get(slot);
    }

    public void clearSkills() {
        skills.clear();
    }

    // ── Kiem tra skill item (PUBLIC de dung o nhieu noi) ─────────────────────

    public boolean isSkillItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        
        Material expectedMat;
        try {
            expectedMat = Material.valueOf(plugin.getCombatConfig().getSkillItemMaterial());
        } catch (Exception e) {
            expectedMat = Material.NETHER_STAR;
        }
        
        if (item.getType() != expectedMat) return false;
        
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "elysium_skill");
        if (meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BYTE)) {
            return true;
        }

        if (!meta.hasCustomModelData()) return false;
        int cmd = meta.getCustomModelData();
        return cmd >= CMD_SKILL_1 && cmd <= CMD_SKILL_3;
    }

    // ── Effects ──────────────────────────────────────────────────────────────

    public void playSkillEffects(Player player, Skill skill) {
        String particle = skill.get("particle", "NONE");
        if (!particle.equalsIgnoreCase("NONE")) {
            try {
                org.bukkit.Particle p = org.bukkit.Particle.valueOf(particle);
                player.getWorld().spawnParticle(p, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}
        }
        String sound = skill.get("sound", "NONE");
        if (!sound.equalsIgnoreCase("NONE")) {
            try {
                org.bukkit.Sound s = org.bukkit.Sound.valueOf(sound);
                player.getWorld().playSound(player.getLocation(), s, 1f, 1f);
            } catch (Exception ignored) {}
        }
    }

    // ── Kich hoat Skill ──────────────────────────────────────────────────────
    
    public void activate(Player player, int slot) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if (pc == null || pc == PlayerClass.NONE) return;

        Skill skill = getSkill(pc, slot);
        if (skill == null) return;

        String cdKey = pc + "_skill" + slot;
        long rem = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
        if (rem > 0) {
            player.sendMessage(ColorUtil.color("&cChiêu thức chưa sẵn sàng! Thử lại sau " + rem + "s."));
            return;
        }

        dev.elysium.core.player.ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep == null) return;
        if (ep.getMana() < skill.getManaCost()) {
            player.sendMessage(ColorUtil.color("&cKhông đủ Mana!"));
            return;
        }

        ep.setMana(ep.getMana() - skill.getManaCost());
        CoreAPI.getCore().getCooldownManager().set(player.getUniqueId(), cdKey, skill.getCooldownSeconds() * 1000L);

        playSkillEffects(player, skill);

        if (skill.getEffect() != null) {
            switch (skill.getEffect()) {
                case BUFF_POTION -> {
                    String type = skill.get("potion_type", "SPEED");
                    if (type == null) type = skill.get("potion-type", "SPEED");
                    int dur = skill.get("duration", 100);
                    int amp = skill.get("amplifier", 0);
                    if (skill.get("potion-amplifier", null) != null) {
                        amp = skill.get("potion-amplifier", 0);
                    }
                    try {
                        org.bukkit.potion.PotionEffectType pet = org.bukkit.potion.PotionEffectType.getByName(type);
                        if (pet != null) {
                            player.addPotionEffect(new org.bukkit.potion.PotionEffect(pet, dur, amp));
                        }
                    } catch (Exception ignored) {}
                }
                case RESTORE_MANA -> {
                    int amount = skill.get("amount", 20);
                    if (skill.get("mana-restore", null) != null) amount = skill.get("mana-restore", 20);
                    ep.setMana(Math.min(ep.getMaxMana(), ep.getMana() + amount));
                }
                case HEAL -> {
                    double amount = skill.get("amount", 4.0);
                    if (skill.get("heal-amount", null) != null) amount = skill.get("heal-amount", 4.0);
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + amount));
                }
                case DASH -> {
                    double str = skill.get("strength", 1.5);
                    if (skill.get("dash-distance", null) != null) str = skill.get("dash-distance", 1.5);
                    org.bukkit.util.Vector v = player.getLocation().getDirection().normalize().multiply(str).setY(0.2);
                    player.setVelocity(v);
                }
            }
        }

        refreshHotbarSkills(player, pc);
    }

    // ── Skill item ───────────────────────────────────────────────────────────

    public ItemStack buildSkillItem(Player player, PlayerClass pc, int slot) {
        Skill skill = getSkill(pc, slot);
        Material mat;
        try { mat = Material.valueOf(plugin.getCombatConfig().getSkillItemMaterial()); }
        catch (Exception e) { mat = Material.NETHER_STAR; }
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

        meta.setDisplayName(ColorUtil.color(skill.getIcon() + " " + skill.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8&m                     "));
        
        lore.add(ColorUtil.color("&7" + skill.getDescription()));
        
        lore.add("");
        lore.add(ColorUtil.color("&f&lTHÔNG TIN:"));
        lore.add(ColorUtil.color("  &8▪ &7Hồi chiêu: &e" + skill.getCooldownSeconds() + "s"));
        lore.add(ColorUtil.color("  &8▪ &7Năng lượng: &b" + (skill.getManaCost() > 0 ? skill.getManaCost() : "Không tốn")));
        lore.add(ColorUtil.color("&8&m                     "));
        
        if (onCd) {
            lore.add(ColorUtil.color("&c&l⏳ ĐANG HỒI CHIÊU &8(&e" + rem + "s&8)"));
        } else {
            lore.add(ColorUtil.color("&a&l✔ SẴN SÀNG"));
            lore.add(ColorUtil.color("&eClick Chuột Phải &7để kích hoạt!"));
        }

        meta.setLore(lore);
        meta.setCustomModelData(2000 + slot);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "elysium_skill");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            
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
            ItemStack item = buildSkillItem(player, pc, i);
            player.getInventory().setItem(slots[i-1], item);
        }
    }
}