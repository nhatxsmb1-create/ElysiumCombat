import re

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'r', encoding='utf-8') as f:
    code = f.read()

new_refresh = '''    public void refreshHotbarSkills(Player player, PlayerClass pc) {
        if (pc == PlayerClass.NONE) return;
        int[] slots = {
            plugin.getCombatConfig().getSkillSlot(1),
            plugin.getCombatConfig().getSkillSlot(2),
            plugin.getCombatConfig().getSkillSlot(3)
        };
        for (int i = 1; i <= 3; i++) {
            ItemStack item = player.getInventory().getItem(slots[i - 1]);
            Skill skill = getSkill(pc, i);
            if (item == null || skill == null) continue;
            if (!isSkillItem(item)) continue;
            refreshSkillItemMeta(player, item, pc, i, skill);
        }
    }'''
# Find existing refreshHotbarSkills
old_refresh = re.search(r'    public void refreshHotbarSkills.*?^\s*\}$', code, re.MULTILINE | re.DOTALL)
if old_refresh:
    code = code[:old_refresh.start()] + new_refresh + code[old_refresh.end():]

new_meta = '''    public void refreshSkillItemMeta(Player player, ItemStack item, PlayerClass pc, int slot, Skill skill) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String cdKey = pc + "_skill" + slot;
        long rem  = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
        boolean onCd = rem > 0;

        meta.setDisplayName(ColorUtil.color(skill.getIcon() + " &f" + skill.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7" + skill.getDescription()));
        lore.add("");
        lore.add(ColorUtil.color("&7Cooldown: &f" + skill.getCooldownSeconds() + "s"));
        lore.add(ColorUtil.color("&7Mana Cost: &b" + (skill.getManaCost() > 0 ? skill.getManaCost() : "0")));
        lore.add("");
        lore.add(onCd
            ? ColorUtil.color("&c⏳ Hoi chieu: &e" + rem + "s")
            : ColorUtil.color("&a✔ San sang"));
        lore.add(ColorUtil.color("&8Chuot phai de dung"));

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