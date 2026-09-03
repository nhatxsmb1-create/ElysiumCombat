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
    // GĂ¡Â»Âi tĂ¡Â»Â« hĂ¡Â»â€¡ thĂ¡Â»â€˜ng /trangbi cĂ¡Â»Â§a ElysiumItem khi LĂ¡ÂºÂ®P LÄ‚â€¢I
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

    // GĂ¡Â»Âi tĂ¡Â»Â« hĂ¡Â»â€¡ thĂ¡Â»â€˜ng /trangbi cĂ¡Â»Â§a ElysiumItem khi THÄ‚ÂO LÄ‚â€¢I
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
    
    // HÄ‚Â m tĂ¡ÂºÂ¡o ra vĂ¡ÂºÂ­t phĂ¡ÂºÂ©m LÄ‚Âµi KĂ¡Â»Â¹ NĂ„Æ’ng vĂ¡ÂºÂ­t lÄ‚Â½
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
            lore.add(ColorUtil.color("&a&l[ CHĂ¡Â»Ë† SĂ¡Â»Â GIA TĂ„â€NG ]"));
            lore.add(ColorUtil.color("&7* MÄ‚Â¡u: &c+" + cd.getBonusHp()));
            lore.add(ColorUtil.color("&7* SÄ‚Â¡t ThĂ†Â°Ă†Â¡ng: &c+" + cd.getBonusDamage()));
            lore.add(ColorUtil.color("&7* GiÄ‚Â¡p: &a+" + cd.getDefense()));
            lore.add("");
            lore.add(ColorUtil.color("&e&l[ HĂ†Â¯Ă¡Â»ÂNG DĂ¡ÂºÂªN KÄ‚ÂCH HOĂ¡ÂºÂ T ]"));
            lore.add(ColorUtil.color("&7DÄ‚Â¹ng lĂ¡Â»â€¡nh &f/trangbi &7vÄ‚Â  Ă„â€˜Ă¡ÂºÂ·t lÄ‚Âµi nÄ‚Â y"));
            lore.add(ColorUtil.color("&7vÄ‚Â o Ä‚Â´ HĂ¡Â»â€¡ PhÄ‚Â¡i Ă„â€˜Ă¡Â»Æ’ nhĂ¡ÂºÂ­n KĂ¡Â»Â¹ NĂ„Æ’ng."));
            
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