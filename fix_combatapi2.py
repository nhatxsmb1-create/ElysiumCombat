import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

combat_api = '''package dev.elysium.combat.api;

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

    // Gọi từ hệ thống /trangbi của ElysiumItem khi LẮP LÕI
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

    // Gọi từ hệ thống /trangbi của ElysiumItem khi THÁO LÕI
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
    
    // Hàm tạo ra vật phẩm Lõi Kỹ Năng vật lý
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
            lore.add(ColorUtil.color("&a&l[ CHỈ SỐ GIA TĂNG ]"));
            lore.add(ColorUtil.color("&7* Máu: &c+" + cd.getBonusHp()));
            lore.add(ColorUtil.color("&7* Sát Thương: &c+" + cd.getBonusDamage()));
            lore.add(ColorUtil.color("&7* Giáp: &a+" + cd.getDefense()));
            lore.add("");
            lore.add(ColorUtil.color("&e&l[ HƯỚNG DẪN KÍCH HOẠT ]"));
            lore.add(ColorUtil.color("&7Dùng lệnh &f/trangbi &7và trang bị lõi"));
            lore.add(ColorUtil.color("&7này để &aMở khóa Kỹ Năng &7của Hệ Phái."));
            
            meta.setLore(lore);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            
            NamespacedKey key = new NamespacedKey(ElysiumCombat.getInstance(), "combat_core");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, pc.name());
            
            item.setItemMeta(meta);
            return item;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
'''
write_file('src/main/java/dev/elysium/combat/api/CombatAPI.java', combat_api)
print("Fixed CombatAPI.java lore")