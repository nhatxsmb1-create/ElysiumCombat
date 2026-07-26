package dev.elysium.combat;

import dev.elysium.combat.api.CombatAPI;
import dev.elysium.combat.clazz.ClassManager;
import dev.elysium.combat.config.CombatConfig;
import dev.elysium.combat.listener.CombatListener;
import dev.elysium.combat.listener.SkillListener;
import dev.elysium.combat.mana.ManaManager;
import dev.elysium.combat.skill.SkillManager;
import dev.elysium.combat.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumCombat extends JavaPlugin {

    private static ElysiumCombat instance;

    private CombatConfig  combatConfig;
    private ClassManager  classManager;
    private SkillManager  skillManager;
    private StatsManager  statsManager;
    private ManaManager   manaManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("classes.yml", false);
        saveResource("skills.yml", false);

        combatConfig = new CombatConfig(this);
        classManager = new ClassManager(this);
        skillManager = new SkillManager(this);
        statsManager = new StatsManager(this);
        manaManager  = new ManaManager(this);

        CombatAPI.init(this);

        getCommand("combat").setExecutor(new CombatCommand(this));
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);

        // Apply stats cho cac player da online (reload case)
        getServer().getOnlinePlayers().forEach(p -> {
            classManager.loadPlayerClass(p);
            statsManager.apply(p);
        });

        getLogger().info("=== ElysiumCombat v" + getDescription().getVersion() + " enabled! ===");
        getLogger().info("Classes: " + classManager.getClassCount() + " | Skills loaded.");
    }

    @Override
    public void onDisable() {
        if (manaManager != null) manaManager.stop();
        // Remove tat ca attribute modifier khi disable
        getServer().getOnlinePlayers().forEach(p -> statsManager.remove(p));
        getLogger().info("ElysiumCombat disabled.");
    }

    public static ElysiumCombat getInstance() { return instance; }
    public CombatConfig getCombatConfig()      { return combatConfig; }
    public ClassManager getClassManager()      { return classManager; }
    public SkillManager getSkillManager()      { return skillManager; }
    public StatsManager getStatsManager()      { return statsManager; }
    public ManaManager  getManaManager()       { return manaManager; }
}
