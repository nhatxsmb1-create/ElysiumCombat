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
    public static final String TITLE = ColorUtil.color("&8&lCHĂ¡Â»Å’N NGHĂ¡Â»â‚¬ NGHIĂ¡Â»â€ P");

    public LifeClassGui(ElysiumCombat plugin) {
        this.plugin = plugin;
        
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        
        ItemStack bg = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", "");
        for(int i = 0; i < 27; i++) inv.setItem(i, bg);

        // Trang trÄ‚Â­ mÄ‚Â u sĂ¡ÂºÂ¯c cho tĂ¡Â»Â«ng CĂ¡Â»â„¢t NghĂ¡Â»Â
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
        if (clazz == LifeClass.FORGER) name = "&b&lKHAI PHÄ‚Â GIĂ¡ÂºÂ¢";
        else if (clazz == LifeClass.STRIKER) name = "&c&lCHIĂ¡ÂºÂ¾N THĂ¡ÂºÂ¦N";
        else if (clazz == LifeClass.FREELANCER) name = "&a&lLÄ‚Æ’NG KHÄ‚ÂCH";
        
        meta.setDisplayName(ColorUtil.color(name));
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        if (clazz == LifeClass.FORGER) {
            lore.add(ColorUtil.color("&7* NghĂ¡Â»Â chuyÄ‚Âªn dĂ¡Â»Â¥ng cho ngĂ†Â°Ă¡Â»Âi cÄ‚Â y mĂ¡Â»Â."));
            lore.add(ColorUtil.color("&a&l[+] &f200% &7TĂ¡Â»â€˜c Ă„â€˜Ă¡Â»â„¢ Ă„â€˜Ä‚Â o mĂ¡Â»Â (Haste 3)"));
            lore.add(ColorUtil.color("&a+ MĂ¡Â»Å¸ khÄ‚Â³a 100% &7TiĂ¡Â»Âm nĂ„Æ’ng CÄ‚Âºp LĂ¡ÂºÂ¯p RÄ‚Â¡p"));
            lore.add(ColorUtil.color("&c&l[-] &f50% &7SÄ‚Â¡t thĂ†Â°Ă†Â¡ng LÄ‚Âµi HĂ¡Â»â€¡ PhÄ‚Â¡i"));
        } else if (clazz == LifeClass.STRIKER) {
            lore.add(ColorUtil.color("&7* NghĂ¡Â»Â chuyÄ‚Âªn dĂ¡Â»Â¥ng Ă„â€˜Ä‚Â¡nh Boss Ă„â€˜i Ă¡ÂºÂ£i."));
            lore.add(ColorUtil.color("&a&l[+] &f150% &7SÄ‚Â¡t thĂ†Â°Ă†Â¡ng LÄ‚Âµi HĂ¡Â»â€¡ PhÄ‚Â¡i"));
            lore.add(ColorUtil.color("&c- BĂ¡Â»â€¹ giĂ¡ÂºÂ£m cĂ¡Â»Â±c mĂ¡ÂºÂ¡nh &7tĂ¡Â»â€˜c Ă„â€˜Ă¡Â»â„¢ Ă„â€˜Ä‚Â o mĂ¡Â»Â"));
        } else if (clazz == LifeClass.FREELANCER) {
            lore.add(ColorUtil.color("&7* DÄ‚Â nh cho ngĂ†Â°Ă¡Â»Âi chĂ†Â¡i hĂ¡Â»â€¡ CÄ‚Â y Ă„ÂĂ†Â¡n Ă„ÂĂ¡Â»â„¢c (Solo)."));
            lore.add(ColorUtil.color("&e&l[+] &f100% &7SÄ‚Â¡t thĂ†Â°Ă†Â¡ng chiĂ¡ÂºÂ¿n Ă„â€˜Ă¡ÂºÂ¥u (BÄ‚Â¬nh thĂ†Â°Ă¡Â»Âng)"));
            lore.add(ColorUtil.color("&e&l[+] &f100% &7TĂ¡Â»â€˜c Ă„â€˜Ă¡Â»â„¢ Ă„â€˜Ä‚Â o mĂ¡Â»Â (BÄ‚Â¬nh thĂ†Â°Ă¡Â»Âng)"));
        }
        
        lore.add("");
        if (isCurrent) {
            lore.add(ColorUtil.color("&a&l[ Ă„ÂANG CHĂ¡Â»Å’N ]"));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.RESPIRATION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(ColorUtil.color("&eNhĂ¡ÂºÂ¥p Ă„â€˜Ă¡Â»Æ’ chuyĂ¡Â»Æ’n sang nghĂ¡Â»Â nÄ‚Â y."));
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
            p.sendMessage(ColorUtil.color("&aBĂ¡ÂºÂ¡n Ă„â€˜Ä‚Â£ chuyĂ¡Â»Æ’n Ă„â€˜Ă¡Â»â€¢i NghĂ¡Â»Â NghiĂ¡Â»â€¡p thÄ‚Â nh cÄ‚Â´ng!"));
            p.closeInventory();
            open(p);
        }
    }
}