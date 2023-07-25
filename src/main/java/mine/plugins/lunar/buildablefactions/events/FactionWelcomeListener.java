package mine.plugins.lunar.buildablefactions.events;

import lombok.AllArgsConstructor;
import mine.plugins.lunar.buildablefactions.cmds.arg.FactionBannerArg;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.world.BannerState;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunkHandler;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@AllArgsConstructor
public class FactionWelcomeListener implements Listener {

    private final JavaPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void welcomePlayer(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);

        if (!onlinePlayer.getFactionPlayer().isNew())
            return;

        giveWelcomeKit(player);
        onlinePlayer.randomTeleport();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void respawnPlayer(PlayerRespawnEvent e) {
        if (e.isAnchorSpawn() || e.isBedSpawn()) return;

        var player = e.getPlayer();
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        var factionPlayer = onlinePlayer.getFactionPlayer();

        var randomClaimChunkID = factionPlayer.getRandomClaimChunkID();
        if (randomClaimChunkID != null) {

            var randomChunk = ClaimChunkHandler.chunkFromID(randomClaimChunkID);
            for (int i = 0; i < 99; i++) {
                var randomLocInfo = onlinePlayer.getRandomLocation(randomChunk);
                if (!randomLocInfo.isSafe()) continue;

                e.setRespawnLocation(randomLocInfo.loc());
                return;
            }

            e.setRespawnLocation(onlinePlayer.getRandomLocation(randomChunk).loc());
            return;
        }

        giveWelcomeKit(player);

        if (Debugger.isDebugActive) plugin.getLogger().log(Level.INFO,
                "Random teleporting on respawn: "+player.getDisplayName());

        var respawnWorld = e.getRespawnLocation().getWorld();
        if (respawnWorld == null) respawnWorld = player.getWorld();

        var randomLoc = onlinePlayer.getRandomTeleport(respawnWorld);
        e.setRespawnLocation(randomLoc);
    }

    private void giveWelcomeKit(Player player) {
        FactionBannerArg.giveBanner(player, BannerState.CLAIM);
        FactionBannerArg.giveBanner(player, BannerState.UNCLAIM);
    }
}
