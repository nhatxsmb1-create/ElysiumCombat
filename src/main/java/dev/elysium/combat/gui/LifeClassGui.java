package dev.elysium.combat.gui;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.LifeClass;
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
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class LifeClassGui implements Listener {
    private final ElysiumCombat plugin;
    public static final String TITLE = ColorUtil.color("&8&lCHỌN NGHỀ NGHIỆP");

    public LifeClassGui(ElysiumCombat plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", "");
        for(int i = 0; i < 27; i++) inv.setItem(i, bg);

        // Trang trí màu sắc cho từng Cột Nghề
        ItemStack bluePane = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", "");
        ItemStack greenPane = createItem(Material.LIME_STAINED_GLASS_PANE, " ", "");
        ItemStack redPane = createItem(Material.RED_STAINED_GLASS_PANE, " ", "");

        inv.setItem(2, bluePane); inv.setItem(20, bluePane);
        inv.setItem(4, greenPane); inv.setItem(22, greenPane);
        inv.setItem(6, redPane); inv.setItem(24, redPane);

        LifeClass current = plugin.getLifeClassManager().getLifeClass(p);

        inv.setItem(11, createClassItem(Material.DIAMOND_PICKAXE, LifeClass.FORGER, current));
        inv.setItem(13, createClassItem(Material.COMPASS, LifeClass.FREELANCER, current));
        inv.setItem(15, createClassItem(Material.DIAMOND_SWORD, LifeClass.STRIKER, current));

        p.openInventory(inv);
    }

    private ItemStack createClassItem(Material mat, LifeClass clazz, LifeClass currentClass) {
        boolean isCurrent = (clazz == currentClass);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        String name = "";
        if (clazz == LifeClass.FORGER) name = "&b&lKHAI PHÁ GIẢ";
        else if (clazz == LifeClass.STRIKER) name = "&c&lCHIẾN THẦN";
        else if (clazz == LifeClass.FREELANCER) name = "&a&lLÃNG KHÁCH";
        
        meta.setDisplayName(ColorUtil.color(name));
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        if (clazz == LifeClass.FORGER) {
            lore.add(ColorUtil.color("&7* Nghề chuyên dụng cho người cày mỏ."));
            lore.add(ColorUtil.color("&a+ 200% &7Tốc độ đào mỏ (Haste 3)"));
            lore.add(ColorUtil.color("&a+ Mở khóa 100% &7Tiềm năng Cúp Lắp Ráp"));
            lore.add(ColorUtil.color("&c- 50% &7Sát thương Lõi Hệ Phái"));
        } else if (clazz == LifeClass.STRIKER) {
            lore.add(ColorUtil.color("&7* Nghề chuyên dụng đánh Boss đi ải."));
            lore.add(ColorUtil.color("&a+ 150% &7Sát thương Lõi Hệ Phái"));
            lore.add(ColorUtil.color("&c- Bị giảm cực mạnh &7tốc độ đào mỏ"));
        } else if (clazz == LifeClass.FREELANCER) {
            lore.add(ColorUtil.color("&7* Dành cho người chơi hệ Cày Đơn Độc (Solo)."));
            lore.add(ColorUtil.color("&e+ 100% &7Sát thương chiến đấu (Bình thường)"));
            lore.add(ColorUtil.color("&e+ 100% &7Tốc độ đào mỏ (Bình thường)"));
        }
        
        lore.add("");
        if (isCurrent) {
            lore.add(ColorUtil.color("&a&l[ ĐANG CHỌN ]"));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.RESPIRATION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(ColorUtil.color("&eNhấp để chuyển sang nghề này."));
        }
        
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.color(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        
        Player p = (Player) e.getWhoClicked();
        LifeClass current = plugin.getLifeClassManager().getLifeClass(p);
        LifeClass target = null;
        
        if (e.getRawSlot() == 11) target = LifeClass.FORGER;
        else if (e.getRawSlot() == 13) target = LifeClass.FREELANCER;
        else if (e.getRawSlot() == 15) target = LifeClass.STRIKER;
        
        if (target != null && target != current) {
            plugin.getLifeClassManager().setLifeClass(p, target);
            p.sendMessage(ColorUtil.color("&aBạn đã chuyển đổi Nghề Nghiệp thành công!"));
            p.closeInventory();
            open(p);
        }
    }
}