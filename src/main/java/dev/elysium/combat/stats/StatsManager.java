package dev.elysium.combat.stats;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final ElysiumCombat plugin;
    private final Map<UUID, CombatStats> statsCache = new HashMap<>();

    // NamespacedKey cho tung loai modifier
    private final NamespacedKey KEY_HP;
    private final NamespacedKey KEY_DAMAGE;
    private final NamespacedKey KEY_SPEED;
    private final NamespacedKey KEY_ARMOR;

    public StatsManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        KEY_HP     = new NamespacedKey(plugin, "elysium_class_hp");
        KEY_DAMAGE = new NamespacedKey(plugin, "elysium_class_damage");
        KEY_SPEED  = new NamespacedKey(plugin, "elysium_class_speed");
        KEY_ARMOR  = new NamespacedKey(plugin, "elysium_class_armor");
    }

    /** Ap dung stats cua class hien tai len player */
    public void apply(Player player) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        ClassData cd = plugin.getClassManager().getClassData(pc);

        // Xoa modifier cu truoc
        removeModifier(player, Attribute.GENERIC_MAX_HEALTH, KEY_HP);
        removeModifier(player, Attribute.GENERIC_ATTACK_DAMAGE, KEY_DAMAGE);
        removeModifier(player, Attribute.GENERIC_MOVEMENT_SPEED, KEY_SPEED);
        removeModifier(player, Attribute.GENERIC_ARMOR, KEY_ARMOR);

        if (cd == null || pc == PlayerClass.NONE) {
            statsCache.put(player.getUniqueId(), new CombatStats(0, 2, 0));
            return;
        }

        // Ap dung modifier moi
        addModifier(player, Attribute.GENERIC_MAX_HEALTH, KEY_HP, cd.getBonusHp());
        addModifier(player, Attribute.GENERIC_ATTACK_DAMAGE, KEY_DAMAGE, cd.getBonusDamage());
        addModifier(player, Attribute.GENERIC_MOVEMENT_SPEED, KEY_SPEED, cd.getSpeedModifier());

        // Cache combat stats
        statsCache.put(player.getUniqueId(), new CombatStats(
            cd.getDefense(), cd.getManaRegen(), cd.getMaxManaBonus()));

        // Cap nhat max mana trong ElysiumCore
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep != null) {
            int baseMana = plugin.getServer().getOnlinePlayers().isEmpty() ? 100
                : plugin.getCombatConfig().getBaseMana();
            ep.setMaxMana(baseMana + cd.getMaxManaBonus());
            if (ep.getMana() > ep.getMaxMana()) ep.setMana(ep.getMaxMana());
        }

        // Heal neu HP vuot qua max moi
        if (player.getHealth() > player.getMaxHealth())
            player.setHealth(player.getMaxHealth());
    }

    /** Xoa tat ca modifier cua ElysiumCombat */
    public void remove(Player player) {
        removeModifier(player, Attribute.GENERIC_MAX_HEALTH, KEY_HP);
        removeModifier(player, Attribute.GENERIC_ATTACK_DAMAGE, KEY_DAMAGE);
        removeModifier(player, Attribute.GENERIC_MOVEMENT_SPEED, KEY_SPEED);
        removeModifier(player, Attribute.GENERIC_ARMOR, KEY_ARMOR);
        statsCache.remove(player.getUniqueId());
    }

    public CombatStats getStats(Player player) {
        return statsCache.getOrDefault(player.getUniqueId(), new CombatStats(0, 2, 0));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addModifier(Player player, Attribute attribute, NamespacedKey key, double value) {
        if (value == 0) return;
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        AttributeModifier mod = new AttributeModifier(key, value,
            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY);
        inst.addModifier(mod);
    }

    private void removeModifier(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        inst.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .forEach(inst::removeModifier);
    }
                    }
