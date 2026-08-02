package dev.elysium.combat;

import dev.elysium.combat.api.CombatAPI;
import dev.elysium.combat.clazz.ClassManager;
import dev.elysium.combat.combo.ComboManager;
import dev.elysium.combat.config.CombatConfig;
import dev.elysium.combat.kill.KillNotifyManager;
import dev.elysium.combat.listener.CombatListener;
import dev.elysium.combat.listener.SkillListener;
import dev.elysium.combat.mana.ManaManager;
import dev.elysium.combat.particle.HitParticleManager;
import dev.elysium.combat.skill.SkillManager;
import dev.elysium.combat.stats.StatsManager;
import dev.elysium.combat.tag.CombatTagManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ElysiumCombat extends JavaPlugin {

    private static ElysiumCombat instance;
    private CombatConfig       combatConfig;
    private ClassManager       classManager;
    private SkillManager       skillManager;
    private StatsManager       statsManager;
    private ManaManager        manaManager;
    private ComboManager       comboManager;
    private CombatTagManager   combatTagManager;
    private HitParticleManager hitParticleManager;
    private KillNotifyManager  killNotifyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("classes.yml", false);
        saveResource("skills.yml", false);

        combatConfig       = new CombatConfig(this);
        classManager       = new ClassManager(this);
        skillManager       = new SkillManager(this);
        statsManager       = new StatsManager(this);
        manaManager        = new ManaManager(this);
        comboManager       = new ComboManager(this);
        combatTagManager   = new CombatTagManager(this);
        hitParticleManager = new HitParticleManager(this);
        killNotifyManager  = new KillNotifyManager(this);

        CombatAPI.init(this);

        getCommand("combat").setExecutor(new CombatCommand(this));
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);

        getServer().getOnlinePlayers().forEach(p -> {
            classManager.loadPlayerClass(p);
            statsManager.apply(p);
            classManager.giveSkillItems(p);
        });

        getLogger().info("=== ElysiumCombat v" + getDescription().getVersion() + " enabled! ===");
    }

    @Override
    public void onDisable() {
        if (manaManager != null) manaManager.stop();
        getServer().getOnlinePlayers().forEach(p -> statsManager.remove(p));
        getLogger().info("ElysiumCombat disabled.");
    }

    // ── Reload ───────────────────────────────────────────────────────────────

    public void reload() {
        combatConfig.reload();
        // Reload lai class + skill tu file
        classManager  = new ClassManager(this);
        skillManager  = new SkillManager(this);
        statsManager  = new StatsManager(this);
        // Re-apply cho tat ca player dang online
        getServer().getOnlinePlayers().forEach(p -> {
            classManager.loadPlayerClass(p);
            statsManager.apply(p);
            classManager.giveSkillItems(p);
        });
        getLogger().info("[ElysiumCombat] Reloaded.");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public static ElysiumCombat getInstance()        { return instance; }
    public CombatConfig       getCombatConfig()      { return combatConfig; }
    public ClassManager       getClassManager()      { return classManager; }
    public SkillManager       getSkillManager()      { return skillManager; }
    public StatsManager       getStatsManager()      { return statsManager; }
    public ManaManager        getManaManager()       { return manaManager; }
    public ComboManager       getComboManager()      { return comboManager; }
    public CombatTagManager   getCombatTagManager()  { return combatTagManager; }
    public HitParticleManager getHitParticleManager(){ return hitParticleManager; }
    public KillNotifyManager  getKillNotifyManager() { return killNotifyManager; }
}
