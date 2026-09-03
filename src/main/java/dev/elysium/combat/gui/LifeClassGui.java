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

import java.util.ArrayList;
import java.util.List;

public class LifeClassGui implements Listener {
    private final ElysiumCombat plugin;
    public static final String TITLE = ColorUtil.color("&8&lCHá»ŒN NGHá»€ NGHIá»†P");

    public LifeClassGui(ElysiumCombat plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", "");
        for(int i=0; i<27; i++) inv.setItem(i, bg);

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
        if (clazz == LifeClass.FORGER) name = "&b&lKHAI PHĂ GIáº¢";
        else if (clazz == LifeClass.STRIKER) name = "&c&lCHIáº¾N THáº¦N";
        else if (clazz == LifeClass.FREELANCER) name = "&a&lLĂƒNG KHĂCH";
        
        meta.setDisplayName(ColorUtil.color(name));
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        if (clazz == LifeClass.FORGER) {
            lore.add(ColorUtil.color("&7* Nghá» chuyĂªn dá»¥ng cho ngÆ°á»i cĂ y má»."));
            lore.add(ColorUtil.color("&a+ 200% &7Tá»‘c Ä‘á»™ Ä‘Ă o má»"));
            lore.add(ColorUtil.color("&a+ Má»Ÿ khĂ³a 100% &7Tiá»m nÄƒng CĂºp Láº¯p RĂ¡p"));
            lore.add(ColorUtil.color("&c- 50% &7SĂ¡t thÆ°Æ¡ng LĂµi Há»‡ PhĂ¡i"));
        } else if (clazz == LifeClass.STRIKER) {
            lore.add(ColorUtil.color("&7* Nghá» chuyĂªn dá»¥ng Ä‘Ă¡nh Boss Ä‘i áº£i."));
            lore.add(ColorUtil.color("&a+ 150% &7SĂ¡t thÆ°Æ¡ng LĂµi Há»‡ PhĂ¡i"));
            lore.add(ColorUtil.color("&c- Bá»‹ giáº£m cá»±c máº¡nh &7tá»‘c Ä‘á»™ Ä‘Ă o má»"));
        } else if (clazz == LifeClass.FREELANCER) {
            lore.add(ColorUtil.color("&7* DĂ nh cho ngÆ°á»i chÆ¡i há»‡ CĂ y ÄÆ¡n Äá»™c (Solo)."));
            lore.add(ColorUtil.color("&e+ 100% &7SĂ¡t thÆ°Æ¡ng chiáº¿n Ä‘áº¥u (BĂ¬nh thÆ°á»ng)"));
            lore.add(ColorUtil.color("&e+ 100% &7Tá»‘c Ä‘á»™ Ä‘Ă o má» (BĂ¬nh thÆ°á»ng)"));
        }
        
        lore.add("");
        if (isCurrent) {
            lore.add(ColorUtil.color("&a&l[ ÄANG CHá»ŒN ]"));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.RESPIRATION, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS, org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        } else {
            lore.add(ColorUtil.color("&eNháº¥p Ä‘á»ƒ chuyá»ƒn sang nghá» nĂ y."));
        }
        
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.color(name));
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
            p.sendMessage(ColorUtil.color("&aBáº¡n Ä‘Ă£ chuyá»ƒn Ä‘á»•i Nghá» Nghiá»‡p thĂ nh cĂ´ng!"));
            p.closeInventory();
            open(p);
        }
    }
}