package dev.elysium.combat.clazz;

import dev.elysium.combat.ElysiumCombat;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeClassManager {
    private final ElysiumCombat plugin;
    private final NamespacedKey key;
    private final Map<UUID, LifeClass> cache = new HashMap<>();

    public LifeClassManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "life_class");
    }

    public LifeClass getLifeClass(Player player) {
        if (cache.containsKey(player.getUniqueId())) return cache.get(player.getUniqueId());
        if (player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            try {
                LifeClass lc = LifeClass.valueOf(player.getPersistentDataContainer().get(key, PersistentDataType.STRING));
                cache.put(player.getUniqueId(), lc);
                return lc;
            } catch (Exception ignored) {}
        }
        return LifeClass.NONE;
    }

    public void setLifeClass(Player player, LifeClass lc) {
        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, lc.name());
        cache.put(player.getUniqueId(), lc);
    }

    public void clearCache(UUID uuid) {
        cache.remove(uuid);
    }
}