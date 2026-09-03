package dev.elysium.combat.api;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CombatAPI {
    public static void init(ElysiumCombat plugin) {}
    // GÄ‚Â¡Ă‚Â»Ă‚Âi tÄ‚Â¡Ă‚Â»Ă‚Â« hÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¡ thÄ‚Â¡Ă‚Â»Ă¢â‚¬Ëœng /trangbi cÄ‚Â¡Ă‚Â»Ă‚Â§a ElysiumItem khi LÄ‚Â¡Ă‚ÂºĂ‚Â®P LĂ„â€Ă¢â‚¬Â¢I
    public static void equipCore(Player player, String coreName) {
        try {
            PlayerClass pc = PlayerClass.valueOf(coreName.toUpperCase());
            ElysiumCombat.getInstance().getClassManager().setPlayerClass(player.getUniqueId(), pc);
            ElysiumCombat.getInstance().getStatsManager().apply(player);
            ElysiumCombat.getInstance().getClassManager().giveSkillItems(player);
        } catch (IllegalArgumentException e) {
            ElysiumCombat.getInstance().getLogger().warning("Invalid core equipped: " + coreName);
        }
    }

    // GÄ‚Â¡Ă‚Â»Ă‚Âi tÄ‚Â¡Ă‚Â»Ă‚Â« hÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¡ thÄ‚Â¡Ă‚Â»Ă¢â‚¬Ëœng /trangbi cÄ‚Â¡Ă‚Â»Ă‚Â§a ElysiumItem khi THĂ„â€Ă‚ÂO LĂ„â€Ă¢â‚¬Â¢I
    public static void unequipCore(Player player) {
        ElysiumCombat.getInstance().getClassManager().setPlayerClass(player.getUniqueId(), PlayerClass.NONE);
        ElysiumCombat.getInstance().getStatsManager().apply(player);
        // Remove skill items
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (ElysiumCombat.getInstance().getSkillManager().isSkillItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }
    
    // HĂ„â€Ă‚Â m tÄ‚Â¡Ă‚ÂºĂ‚Â¡o ra vÄ‚Â¡Ă‚ÂºĂ‚Â­t phÄ‚Â¡Ă‚ÂºĂ‚Â©m LĂ„â€Ă‚Âµi KÄ‚Â¡Ă‚Â»Ă‚Â¹ NÄ‚â€Ă†â€™ng vÄ‚Â¡Ă‚ÂºĂ‚Â­t lĂ„â€Ă‚Â½
    public static ItemStack getCoreItem(String coreName) {
        try {
            PlayerClass pc = PlayerClass.valueOf(coreName.toUpperCase());
            ClassData cd = ElysiumCombat.getInstance().getClassManager().getClassData(pc);
            if (cd == null) return null;
            
            Material mat = Material.matchMaterial(cd.getItem());
            if (mat == null) mat = Material.NETHER_STAR;
            
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorUtil.color(cd.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.color("&7" + cd.getDescription()));
            lore.add("");
            lore.add(ColorUtil.color("&a&l[ CHÄ‚Â¡Ă‚Â»Ă‹â€  SÄ‚Â¡Ă‚Â»Ă‚Â GIA TÄ‚â€Ă¢â‚¬ÂNG ]"));
            lore.add(ColorUtil.color("&7* MĂ„â€Ă‚Â¡u: &c+" + cd.getBonusHp()));
            lore.add(ColorUtil.color("&7* SĂ„â€Ă‚Â¡t ThÄ‚â€ Ă‚Â°Ä‚â€ Ă‚Â¡ng: &c+" + cd.getBonusDamage()));
            lore.add(ColorUtil.color("&7* GiĂ„â€Ă‚Â¡p: &a+" + cd.getDefense()));
            lore.add("");
            lore.add(ColorUtil.color("&e&l[ HÄ‚â€ Ă‚Â¯Ä‚Â¡Ă‚Â»Ă‚ÂNG DÄ‚Â¡Ă‚ÂºĂ‚ÂªN KĂ„â€Ă‚ÂCH HOÄ‚Â¡Ă‚ÂºĂ‚Â T ]"));
            lore.add(ColorUtil.color("&7DĂ„â€Ă‚Â¹ng lÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¡nh &f/trangbi &7vĂ„â€Ă‚Â  Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚ÂºĂ‚Â·t lĂ„â€Ă‚Âµi nĂ„â€Ă‚Â y"));
            lore.add(ColorUtil.color("&7vĂ„â€Ă‚Â o Ă„â€Ă‚Â´ HÄ‚Â¡Ă‚Â»Ă¢â‚¬Â¡ PhĂ„â€Ă‚Â¡i Ä‚â€Ă¢â‚¬ËœÄ‚Â¡Ă‚Â»Ă†â€™ nhÄ‚Â¡Ă‚ÂºĂ‚Â­n KÄ‚Â¡Ă‚Â»Ă‚Â¹ NÄ‚â€Ă†â€™ng."));
            
            meta.setLore(lore);
            
            NamespacedKey key = new NamespacedKey(ElysiumCombat.getInstance(), "combat_core");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, pc.name());
            
            item.setItemMeta(meta);
            return item;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}