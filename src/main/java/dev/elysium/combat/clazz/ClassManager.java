package dev.elysium.combat.clazz;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ClassManager {

    private final ElysiumCombat plugin;
    private final Map<PlayerClass, ClassData> classes    = new LinkedHashMap<>();
    private final Map<UUID, PlayerClass>      playerClass = new HashMap<>();

    public ClassManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        loadClasses();
    }

    private void loadClasses() {
        File file = new File(plugin.getDataFolder(), "classes.yml");
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file)
            .getConfigurationSection("classes");
        if (root == null) { plugin.getLogger().warning("classes.yml trong!"); return; }

        for (String key : root.getKeys(false)) {
            try {
                PlayerClass pc = PlayerClass.valueOf(key.toUpperCase());
                ConfigurationSection s = root.getConfigurationSection(key);
                if (s == null) continue;
                ConfigurationSection st = s.getConfigurationSection("stats");

                classes.put(pc, new ClassData(
                    key,
                    ColorUtil.color(s.getString("display-name", key)),
                    s.getString("description", ""),
                    s.getString("item", "STONE_SWORD"),
                    st != null ? st.getDouble("bonus-hp", 0)        : 0,
                    st != null ? st.getDouble("bonus-damage", 0)     : 0,
                    st != null ? st.getInt("defense", 0)             : 0,
                    st != null ? st.getDouble("speed-modifier", 0)   : 0,
                    st != null ? st.getInt("mana-regen", 2)          : 2,
                    st != null ? st.getInt("max-mana-bonus", 0)      : 0
                ));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Class khong hop le: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + classes.size() + " classes.");
    }

    public void loadPlayerClass(Player player) {
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep == null || ep.getPlayerClass().equals("NONE") || ep.getPlayerClass().isEmpty()) {
            playerClass.put(player.getUniqueId(), PlayerClass.NONE);
            return;
        }
        try {
            PlayerClass pc = PlayerClass.valueOf(ep.getPlayerClass());
            playerClass.put(player.getUniqueId(), pc);
        } catch (IllegalArgumentException e) {
            playerClass.put(player.getUniqueId(), PlayerClass.NONE);
        }
    }

    public void setPlayerClass(UUID uuid, PlayerClass pc) {
        playerClass.put(uuid, pc);
    }

    public PlayerClass getPlayerClass(UUID uuid) {
        return playerClass.getOrDefault(uuid, PlayerClass.NONE);
    }

    public ClassData           getClassData(PlayerClass pc) { return classes.get(pc); }
    public Collection<ClassData> getAllClasses()             { return classes.values(); }
    public int                   getClassCount()            { return classes.size(); }
}
