package dev.elysium.combat.clazz;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ClassManager {

    private final ElysiumCombat plugin;
    private final Map<PlayerClass, ClassData> classes     = new LinkedHashMap<>();
    private final Map<UUID, PlayerClass>      playerClass = new HashMap<>();

    public ClassManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        loadClasses();
    }

    private void loadClasses() {
        File file = new File(plugin.getDataFolder(), "classes.yml");
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file)
            .getConfigurationSection("classes");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            try {
                PlayerClass pc = PlayerClass.valueOf(key.toUpperCase());
                ConfigurationSection s  = root.getConfigurationSection(key);
                ConfigurationSection st = s.getConfigurationSection("stats");
                classes.put(pc, new ClassData(key,
                    ColorUtil.color(s.getString("display-name", key)),
                    s.getString("description", ""),
                    s.getString("item", "STONE_SWORD"),
                    st != null ? st.getDouble("bonus-hp",0)       : 0,
                    st != null ? st.getDouble("bonus-damage",0)    : 0,
                    st != null ? st.getInt("defense",0)            : 0,
                    st != null ? st.getDouble("speed-modifier",0)  : 0,
                    st != null ? st.getInt("mana-regen",2)         : 2,
                    st != null ? st.getInt("max-mana-bonus",0)     : 0
                ));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Class khong hop le: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + classes.size() + " classes.");
    }

    public void loadPlayerClass(Player player) {
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep == null || ep.getPlayerClass().isBlank() || ep.getPlayerClass().equals("NONE")) {
            playerClass.put(player.getUniqueId(), PlayerClass.NONE); return;
        }
        try { playerClass.put(player.getUniqueId(), PlayerClass.valueOf(ep.getPlayerClass())); }
        catch (IllegalArgumentException e) { playerClass.put(player.getUniqueId(), PlayerClass.NONE); }
    }

    /**
     * Cap skill item vao hotbar slot 6,7,8.
     * Goi khi: chon class, join server, reload.
     */
    public void giveSkillItems(Player player) {
        PlayerClass pc = getPlayerClass(player.getUniqueId());
        int[] slots = {
            plugin.getCombatConfig().getSkillSlot(1),
            plugin.getCombatConfig().getSkillSlot(2),
            plugin.getCombatConfig().getSkillSlot(3)
        };
        if (pc == PlayerClass.NONE) {
            for (int slot : slots) player.getInventory().setItem(slot, null);
            return;
        }
        for (int i = 1; i <= 3; i++) {
            ItemStack item = plugin.getSkillManager().buildSkillItem(player, pc, i);
            player.getInventory().setItem(slots[i - 1], item);
        }
    }

    public void setPlayerClass(UUID uuid, PlayerClass pc) { playerClass.put(uuid, pc); }
    public PlayerClass             getPlayerClass(UUID uuid) { return playerClass.getOrDefault(uuid, PlayerClass.NONE); }
    public ClassData               getClassData(PlayerClass pc) { return classes.get(pc); }
    public Collection<ClassData>   getAllClasses()           { return classes.values(); }
    public int                     getClassCount()          { return classes.size(); }
}
