package dev.elysium.combat.config;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;

public class CombatConfig {

    private final ElysiumCombat plugin;
    private FileConfiguration cfg;

    public CombatConfig(ElysiumCombat plugin) {
        this.plugin = plugin;
        this.cfg    = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
    }

    public String getPrefix()             { return ColorUtil.color(cfg.getString("prefix","&c[Combat] &r")); }
    public int    getManaDisplayInterval(){ return cfg.getInt("mana.display-interval", 20); }
    public int    getManaRegenInterval()  { return cfg.getInt("mana.regen-interval", 40); }
    public int    getBaseMana()           { return 100; } // Base mana truoc khi cong/tru class bonus
    public boolean isClassChangeEnabled() { return cfg.getBoolean("class-change.enabled", true); }
    public double  getClassChangeCost()   { return cfg.getDouble("class-change.cost", 0); }
}
