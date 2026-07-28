package dev.elysium.combat.gui;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.skill.Skill;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * GUI xem tat ca skill cua class hien tai.
 * Mo bang: /combat menu
 * Click vao skill de kich hoat.
 */
public class SkillMenuGui extends ElysiumGui {

    private final ElysiumCombat combat;
    private final Player        target;

    public SkillMenuGui(Player target, ElysiumCombat combat) {
        super("&c✦ Skills", 27);
        this.target = target;
        this.combat = combat;
    }

    @Override
    public void build(Player viewer) {
        fill(ItemBuilder.filler());

        PlayerClass pc = combat.getClassManager().getPlayerClass(target.getUniqueId());
        ClassData   cd = combat.getClassManager().getClassData(pc);

        // Chua chon class
        if (pc == PlayerClass.NONE || cd == null) {
            setButton(13, new GuiButton(
                new ItemBuilder(Material.BARRIER)
                    .name("&cChua chon class!")
                    .lore("&7Dung: &e/combat class")
                    .build()
            ));
            return;
        }

        // ── Header: Class info ────────────────────────────────────────────────
        fill(4, new ItemBuilder(Material.NETHER_STAR)
            .name(cd.getDisplayName())
            .lore(
                "&7" + cd.getDescription(),
                "",
                "&7Defense: &a" + cd.getDefense() + "%",
                "&7Mana Regen: &b+" + cd.getManaRegen() + "/2s",
                "",
                "&8Click skill de kich hoat"
            ).glow().build());

        // ── 3 Skill slots ─────────────────────────────────────────────────────
        int[] guiSlots = {10, 13, 16};

        for (int i = 1; i <= 3; i++) {
            Skill skill = combat.getSkillManager().getSkill(pc, i);
            if (skill == null) continue;

            ItemStack skillItem = combat.getSkillManager().buildSkillItem(target, pc, i);
            final int skillIndex = i;

            setButton(guiSlots[i-1], new GuiButton(skillItem, e -> {
                Player clicker = (Player) e.getWhoClicked();
                // Dong GUI truoc roi kich hoat
                combat.getServer().getScheduler().runTaskLater(combat, () -> {
                    combat.getSkillManager().activate(clicker, skillIndex);
                }, 1L);
            }));

            // Label so slot
            fill(guiSlots[i-1] + 9, new ItemBuilder(Material.GRAY_DYE)
                .name("&7Skill " + i)
                .lore("&8Slot hotbar " + (combat.getCombatConfig().getSkillSlot(i) + 1))
                .build());
        }

        // ── Footer: Huong dan ─────────────────────────────────────────────────
        fill(22, new ItemBuilder(Material.BOOK)
            .name("&7Huong dan")
            .lore(
                "&7• Click vao skill tren GUI de dung",
                "&7• Hoac chuot phai vao item tren hotbar",
                "&7• Skill item o slot 7-8-9",
                "&7• Skill item khong the bo, khong mat khi chet"
            ).build());
    }
}
