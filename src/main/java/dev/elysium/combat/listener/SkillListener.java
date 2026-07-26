package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SkillListener implements Listener {

    private final ElysiumCombat plugin;

    public SkillListener(ElysiumCombat plugin) { this.plugin = plugin; }

    /**
     * Nhan phim F (swap offhand) -> Kich hoat Skill 1
     * Skill 2: /combat skill 2
     * Skill 3: /combat skill 3
     */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if (pc == PlayerClass.NONE) return;

        // Cancel su kien de tranh swap item that su
        e.setCancelled(true);

        // Kich hoat skill 1
        plugin.getSkillManager().activate(player, 1);
    }
}
