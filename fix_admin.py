import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

acg = '''package dev.elysium.combat.gui;
import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.api.CombatAPI;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
public class AdminCoreGui implements Listener {
    private final ElysiumCombat plugin;
    public static final String TITLE = ColorUtil.color("&4&lKHO LÕI HỆ PHÁI (ADMIN)");
    public AdminCoreGui(ElysiumCombat plugin) { this.plugin = plugin; }
    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bgMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);
        PlayerClass[] classes = {PlayerClass.WARRIOR, PlayerClass.MAGE, PlayerClass.ASSASSIN, PlayerClass.RANGER, PlayerClass.SUPPORT};
        int[] slots = {11, 12, 13, 14, 15};
        for (int i = 0; i < classes.length; i++) {
            ItemStack coreItem = CombatAPI.getCoreItem(classes[i].name());
            if (coreItem != null) {
                ItemMeta meta = coreItem.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add(ColorUtil.color("&a&l[!] &aNhấp để lấy Lõi này"));
                meta.setLore(lore);
                coreItem.setItemMeta(meta);
                inv.setItem(slots[i], coreItem);
            }
        }
        p.openInventory(inv);
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        PlayerClass target = null;
        if (slot == 11) target = PlayerClass.WARRIOR;
        else if (slot == 12) target = PlayerClass.MAGE;
        else if (slot == 13) target = PlayerClass.ASSASSIN;
        else if (slot == 14) target = PlayerClass.RANGER;
        else if (slot == 15) target = PlayerClass.SUPPORT;
        if (target != null) {
            ItemStack core = CombatAPI.getCoreItem(target.name());
            if (core != null) {
                p.getInventory().addItem(core);
                p.sendMessage(ColorUtil.color("&aĐã nhận &e" + target.name() + " CORE"));
            }
        }
    }
}
'''
write_file('src/main/java/dev/elysium/combat/gui/AdminCoreGui.java', acg)
print("Fixed AdminCoreGui.java")