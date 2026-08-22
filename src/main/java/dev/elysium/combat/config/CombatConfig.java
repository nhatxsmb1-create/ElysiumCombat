package dev.elysium.combat.config;

import dev.elysium.combat.ElysiumCombat;
import org.bukkit.configuration.file.FileConfiguration;

public class CombatConfig {

    private final ElysiumCombat plugin;
    private FileConfiguration cfg;

    public CombatConfig(ElysiumCombat plugin) {
        this.plugin = plugin; this.cfg = plugin.getConfig();
    }

    public void reload()                           { plugin.reloadConfig(); cfg = plugin.getConfig(); }
    public String getSkillItemMaterial()           { return cfg.getString("skill-item.material","NETHER_STAR"); }
    public int    getSkillSlot(int n)              { return cfg.getInt("skill-item.slot-"+n, 5+n); }
    public int    getManaDisplayInterval()         { return cfg.getInt("mana.display-interval",20); }
    public int    getManaRegenInterval()           { return cfg.getInt("mana.regen-interval",40); }
    public int    getCooldownUpdateInterval()      { return cfg.getInt("cooldown-update-interval",20); }
    public int    getBaseMana()                    { return 100; }

    public org.bukkit.inventory.ItemStack getChangeClassItem() {
        if (!cfg.getBoolean("class-change.enabled", true)) return null;
        String matStr = cfg.getString("class-change.item.material", "NETHER_STAR");
        int amount = cfg.getInt("class-change.item.amount", 1);
        org.bukkit.Material mat = org.bukkit.Material.matchMaterial(matStr);
        if (mat == null) mat = org.bukkit.Material.NETHER_STAR;

        dev.elysium.core.gui.ItemBuilder builder = new dev.elysium.core.gui.ItemBuilder(mat);
        builder.amount(amount);
        
        String name = cfg.getString("class-change.item.name");
        if (name != null && !name.isEmpty()) builder.name(name);
        
        java.util.List<String> lore = cfg.getStringList("class-change.item.lore");
        if (lore != null && !lore.isEmpty()) {
            String[] loreArr = new String[lore.size()];
            for (int i = 0; i < lore.size(); i++) loreArr[i] = dev.elysium.core.util.ColorUtil.color(lore.get(i));
            builder.lore(loreArr);
        }
        
        // Add glow for VIP look
        builder.glow();
        
        return builder.build();
    }
}
