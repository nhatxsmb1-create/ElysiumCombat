package dev.elysium.combat.hud;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarHUDManager {

    private final ElysiumCombat plugin;
    private final Map<UUID, String> tempMessages = new HashMap<>();
    private final Map<UUID, Long> tempExpirations = new HashMap<>();

    public ActionBarHUDManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        startHUDTask();
    }

    /**
     * Gửi tin nhắn tạm thời đè lên HUD (VD: Cooldown skill, Cảnh báo)
     */
    public void sendTemporaryMessage(Player player, String message, int ticks) {
        UUID uuid = player.getUniqueId();
        tempMessages.put(uuid, message);
        tempExpirations.put(uuid, System.currentTimeMillis() + (ticks * 50L));
        player.sendActionBar(ColorUtil.component(message));
    }

    private void startHUDTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = p.getUniqueId();
                    
                    // Nếu đang có tin nhắn tạm thời thì giữ hiển thị tin đó
                    if (tempExpirations.containsKey(uuid)) {
                        if (now < tempExpirations.get(uuid)) {
                            p.sendActionBar(ColorUtil.component(tempMessages.get(uuid)));
                            continue;
                        } else {
                            tempExpirations.remove(uuid);
                            tempMessages.remove(uuid);
                        }
                    }

                    ElysiumPlayer ep = CoreAPI.getPlayer(p);
                    if (ep == null) continue;

                    int hp = (int) p.getHealth();
                    int maxHp = (int) p.getMaxHealth();
                    int mana = ep.getMana();
                    int maxMana = ep.getMaxMana();
                    int level = ep.getLevel();
                    
                    String hud = "&8[&eLv." + level + "&8] " + 
                                 "&c❤ " + hp + "/" + maxHp + " &8| " + 
                                 "&b💧 " + mana + "/" + maxMana;

                    int combo = plugin.getComboManager().getCombo(uuid);
                    if (combo >= 2) {
                        hud += " &8| " + plugin.getComboManager().buildComboBar(p, combo);
                    }

                    p.sendActionBar(ColorUtil.component(hud));
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Cập nhật mỗi 0.25s để mượt hơn
    }
}
