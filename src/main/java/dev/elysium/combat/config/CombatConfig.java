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
}
