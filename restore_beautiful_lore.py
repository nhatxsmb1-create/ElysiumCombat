import re

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'r', encoding='utf-8') as f:
    code = f.read()

new_meta = '''    public void refreshSkillItemMeta(Player player, ItemStack item, PlayerClass pc, int slot, Skill skill) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String cdKey = pc + "_skill" + slot;
        long rem  = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
        boolean onCd = rem > 0;

        meta.setDisplayName(ColorUtil.color(skill.getIcon() + " " + skill.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&8&m                     "));
        
        lore.add(ColorUtil.color("&7" + skill.getDescription()));
        
        lore.add("");
        lore.add(ColorUtil.color("&f&lTHÔNG TIN:"));
        lore.add(ColorUtil.color("  &8▪ &7Hồi chiêu: &e" + skill.getCooldownSeconds() + "s"));
        lore.add(ColorUtil.color("  &8▪ &7Năng lượng: &b" + (skill.getManaCost() > 0 ? skill.getManaCost() : "Không tốn")));
        lore.add(ColorUtil.color("&8&m                     "));
        
        if (onCd) {
            lore.add(ColorUtil.color("&c&l⏳ ĐANG HỒI CHIÊU &8(&e" + rem + "s&8)"));
        } else {
            lore.add(ColorUtil.color("&a&l✔ SẴN SÀNG"));
            lore.add(ColorUtil.color("&eClick Chuột Phải &7để kích hoạt!"));
        }

        meta.setLore(lore);
        meta.setCustomModelData(2000 + slot);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "elysium_skill");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            
        item.setItemMeta(meta);
    }'''

old_meta = re.search(r'    public void refreshSkillItemMeta.*?^\s*\}$', code, re.MULTILINE | re.DOTALL)
if old_meta:
    code = code[:old_meta.start()] + new_meta + code[old_meta.end():]

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'w', encoding='utf-8') as f:
    f.write(code)

print("Done")