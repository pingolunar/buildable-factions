package mine.plugins.lunar.buildablefactions.events;

import mine.plugins.lunar.buildablefactions.data.faction.FactionEntityManager;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.buildablefactions.data.world.BannerState;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunkHandler;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandler;
import mine.plugins.lunar.plugin_framework.player.ActionBarMsg;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class FactionLoaderListener implements Listener {

    //region Reload
    public FactionLoaderListener(JavaPlugin plugin) {

        plugin.getLogger().log(Level.INFO, "Registering players...");
        for (var player : Bukkit.getServer().getOnlinePlayers())
            registerPlayer(player);

        for (var world : Bukkit.getWorlds()) {
            plugin.getLogger().log(Level.INFO, "Registering world '"+world.getName()+"'...");

            for (var entity : world.getEntities())
                if (!(entity instanceof Player) && !FactionEntityManager.isEntityIDSet(entity))
                    FactionEntityManager.setEntityChunkID(entity);

            for (var chunk : world.getLoadedChunks())
                registerChunk(chunk);
        }

        plugin.getLogger().log(Level.INFO, "Updating players...");
        for (var player : Bukkit.getServer().getOnlinePlayers())
            FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player).updateFactionBorder();
    }

    public static void disable() {
        FactionPlayer.getFactionPlayerHandler().unregisterLoaded();
        ClaimChunk.getClaimChunkHandler().unregisterLoaded();
    }
    //endregion

    //region Database
    private static void registerPlayer(Player player) {
        var factionPlayer = FactionPlayer.getFactionPlayerHandler().register(player);
        Faction.getFactionHandler().register(factionPlayer.getFactionData());
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        onlinePlayer.updateFactionBorder();
    }

    private static void unregisterPlayer(Player player) {
        var factionPlayer = FactionPlayer.getFactionPlayerHandler().unregister(player.getUniqueId());
        if (factionPlayer == null) return;
        Faction.getFactionHandler().unregister(factionPlayer.getFactionData());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void registerPlayer(PlayerJoinEvent e) {
        DatabaseHandler.execute(() -> registerPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void unregisterPlayer(PlayerQuitEvent e) {
        DatabaseHandler.execute(() -> {
            var player = e.getPlayer();
            unregisterPlayer(player);
            Faction.unsetGlow(e);
        });
    }

    private static void registerChunk(Chunk chunk) {
        ClaimChunk.getClaimChunkHandler().register(chunk);
    }

    private static void unregisterChunk(Chunk chunk) {
        ClaimChunk.getClaimChunkHandler().unregister(chunk);
    }

    @EventHandler
    private void registerChunk(ChunkLoadEvent e) {
        DatabaseHandler.execute(() -> registerChunk(e.getChunk()));
    }

    @EventHandler
    private void unregisterChunk(ChunkUnloadEvent e) {
        DatabaseHandler.execute(() -> unregisterChunk(e.getChunk()));
    }
    //endregion

    //region Faction Banner
    @EventHandler
    private void manageBanner(BlockPlaceEvent e) {
        var blockPlaced = e.getBlockPlaced();
        if (!(blockPlaced.getState() instanceof Banner banner))
            return;

        var bannerItemMeta = e.getItemInHand().getItemMeta();
        var player = e.getPlayer();

        if (bannerItemMeta == null || BannerState.fromName(bannerItemMeta.getDisplayName()) == null) {
            if (bannerItemMeta != null) Bukkit.getLogger().info("banner display name: "+
                    bannerItemMeta.getDisplayName());

            if (Debugger.isDebugActive) player.sendMessage("Invalid faction banner detected");
            return;
        }

        if (Debugger.isDebugActive)
            player.sendMessage("Attempting to use a chunk banner");

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        switch (banner.getType()) {
            case LIME_BANNER -> onlinePlayer.claim(banner);
            case RED_BANNER -> onlinePlayer.unclaim(banner);
        }
    }
    //endregion

    //region Faction Border
    @EventHandler(priority = EventPriority.HIGHEST)
    private void updateFactionBorder(PlayerRespawnEvent e) {
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(e.getPlayer());
        onlinePlayer.updateFactionBorder(e.getRespawnLocation().getChunk());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void showChunkOwner(PlayerMoveEvent e) {
        var player = e.getPlayer();

        var to = e.getTo();
        if (to == null) return;
        var from = e.getFrom();

        var toChunk = to.getChunk();
        var fromChunk = from.getChunk();

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);

        if (ClaimChunkHandler.areChunksEquals(toChunk, fromChunk)) {

            if (from.getBlockX() == to.getBlockX() &&
                    from.getBlockY() == to.getBlockY() &&
                    from.getBlockZ() == to.getBlockZ())
                return;

            //region PlayerBlockMoveEvent
            if (from.getBlockY() == to.getBlockY())
                return;

            onlinePlayer.updateFactionBorderY(to);
            //endregion
            return;
        }

        //region PlayerChunkMoveEvent
        onlinePlayer.setDatabaseTask(() -> {

            var toChunkClaim = ClaimChunk.getClaimChunkHandler().getLoaded(toChunk);
            var fromChunkClaim = ClaimChunk.getClaimChunkHandler().getLoaded(fromChunk);

            onlinePlayer.updateFactionBorder(toChunk, toChunkClaim);
            if (toChunkClaim == null || fromChunkClaim == null)
                return;

            if (toChunkClaim.allows(onlinePlayer) == fromChunkClaim.allows(onlinePlayer))
                return;

            if (!toChunkClaim.isClaimed()) {
                onlinePlayer.sendMsg(new ActionBarMsg()
                    .appendMsg("Leaving ", ChatColor.GRAY)
                    .appendMsg(getTerritoryOwner(fromChunkClaim), ChatColor.WHITE)
                    .appendMsg(FactionOnlinePlayer.territorySuffix, ChatColor.GRAY));
                return;
            }

            onlinePlayer.sendMsg(new ActionBarMsg()
                .appendMsg("Entering ", ChatColor.GRAY)
                .appendMsg(getTerritoryOwner(toChunkClaim), ChatColor.WHITE)
                .appendMsg(FactionOnlinePlayer.territorySuffix, ChatColor.GRAY));
        }, false);
        //endregion
    }

    private String getTerritoryOwner(ClaimChunk claimChunk) {
        var chunkFaction = claimChunk.getFaction();
        if (chunkFaction != null) return chunkFaction.getFullName();

        var claimPlayerID = claimChunk.getPlayerID();
        if (claimPlayerID != null)
            return Bukkit.getOfflinePlayer(claimPlayerID).getName();

        return "Wilderness";
    }
    //endregion

    //region Advancements
    private static final double advancementReward = 50;

    @EventHandler
    private void rewardPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        var player = e.getPlayer();
        var advancement = e.getAdvancement();

        var advancementDisplay = advancement.getDisplay();
        if (advancementDisplay == null) return;

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        var advancementType = advancementDisplay.getType();

        onlinePlayer.deposit(advancementReward * (advancementType.ordinal()+1));
    }
    //endregion

}
