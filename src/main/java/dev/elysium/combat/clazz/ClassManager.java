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
                    ColorUtil.color(s.getString("display-name",key)),
                    s.getString("description",""),
                    s.getString("item","STONE_SWORD"),
                    st != null ? st.getDouble("bonus-hp",0)      : 0,
                    st != null ? st.getDouble("bonus-damage",0)   : 0,
                    st != null ? st.getInt("defense",0)           : 0,
                    st != null ? st.getDouble("speed-modifier",0) : 0,
                    st != null ? st.getInt("mana-regen",2)        : 2,
                    st != null ? st.getInt("max-mana-bonus",0)    : 0
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
     * Cap skill item vao hotbar.
     * 1. Quet toan bo inventory, xoa het skill item cu.
     * 2. Cuu item thuong khoi slot skill (tra lai inventory / drop neu day).
     * 3. Dat skill item moi vao slot 6, 7, 8.
     */
    public void giveSkillItems(Player player) {
        int[] slots = {
            plugin.getCombatConfig().getSkillSlot(1),
            plugin.getCombatConfig().getSkillSlot(2),
            plugin.getCombatConfig().getSkillSlot(3)
        };

        // Buoc 1: Xoa tat ca skill item cu trong toan bo inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (plugin.getSkillManager().isSkillItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        // Buoc 2: Cuu item thuong khoi cac slot skill
        for (int slot : slots) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing != null && !existing.getType().isAir()) {
                // Thu them vao inventory, neu day thi drop
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(existing);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                player.getInventory().setItem(slot, null);
            }
        }

        // Buoc 3: Dat skill item moi
        PlayerClass pc = getPlayerClass(player.getUniqueId());
        if (pc == PlayerClass.NONE) return;

        for (int i = 1; i <= 3; i++) {
            ItemStack item = plugin.getSkillManager().buildSkillItem(player, pc, i);
            player.getInventory().setItem(slots[i-1], item);
        }
    }

    public void setPlayerClass(UUID uuid, PlayerClass pc) { playerClass.put(uuid, pc); }
    public PlayerClass             getPlayerClass(UUID uuid) { return playerClass.getOrDefault(uuid, PlayerClass.NONE); }
    public ClassData               getClassData(PlayerClass pc) { return classes.get(pc); }
    public Collection<ClassData>   getAllClasses()          { return classes.values(); }
    public int                     getClassCount()         { return classes.size(); }
}
