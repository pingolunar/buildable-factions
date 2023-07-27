package mine.plugins.lunar.buildablefactions.events;

import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.faction.FactionEntityManager;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import mine.plugins.lunar.plugin_framework.event.BarrierProtection;
import mine.plugins.lunar.plugin_framework.event.listener.BarrierProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;

public class FactionBarrierListener extends BarrierProtectionListener {

    public FactionBarrierListener(JavaPlugin plugin) {
        super(plugin);
    }

    //region Utils

    /**
     * Teleports the player to the location if the chunk information is not loaded at the moment
     */
    private @Nullable ClaimChunk teleportOnLoad(Player player, Location to, BarrierProtection barrierProtection) {

        var toChunk = to.getChunk();
        var toClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(toChunk);

        var isBarrierProtectionValid = barrierProtection == BarrierProtection.PORTAL ||
                                       barrierProtection == BarrierProtection.PLAYER_TELEPORT;

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);

        if (toClaimChunk != null || !isBarrierProtectionValid) {
            if (onlinePlayer.isRandomTeleporting()) onlinePlayer.setRandomTeleported();
            return toClaimChunk;
        }

        var toWorld = to.getWorld();
        if (toWorld != null) player.sendMessage("Attempting teleportation to: "+toWorld.getName()+
                ", "+to.toVector().toBlockVector());

        onlinePlayer.setDatabaseTask(() -> {

            var loadedClaimChunk = ClaimChunk.getClaimChunkHandler().register(toChunk);
            if (loadedClaimChunk.isClaimed() && !loadedClaimChunk.allows(onlinePlayer)) {

                if (!onlinePlayer.isRandomTeleporting()) {
                    player.sendMessage("Can't teleport because chunk isn't owned");
                    return;
                }

                if (Debugger.isDebugActive) plugin.getLogger().log(Level.INFO,
                        "Re-trying to random teleporting: "+player.getDisplayName());
                onlinePlayer.randomTeleport();
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(to);
                player.sendMessage("Teleported");
                if (onlinePlayer.isRandomTeleporting()) onlinePlayer.setRandomTeleported();
            });
        }, false, true);

        return null;
    }

    private boolean haveChunksSamePermission(@NonNull Chunk blockChunk, @NonNull ClaimChunk sourceClaimChunk) {

        var blockClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(blockChunk);
        if (blockClaimChunk == null) return false;

        return blockClaimChunk.hasSamePermission(sourceClaimChunk);
    }

    private void removeDisallowedBlocks(List<Block> blocks, @NonNull ClaimChunk sourceClaimChunk) {

        var blocksIterator = blocks.iterator();
        while (blocksIterator.hasNext()) {
            var block = blocksIterator.next();

            if (haveChunksSamePermission(block.getChunk(), sourceClaimChunk))
                continue;

            blocksIterator.remove();
        }
    }

    private @NonNull ClaimChunk getEntityClaimChunk(Entity entity) {
        return FactionEntityManager.getEntityChunkID(entity);
    }
    //endregion

    //region Extension
    @Override
    protected boolean isVictimHitAllowed(Entity victim, Entity attacker) {
        if (attacker instanceof Player player)
            return isPlayerInteractionAllowed(player, victim.getLocation(), BarrierProtection.PLAYER_INTERACT);

        var victimClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(victim.getLocation().getChunk());
        if (victimClaimChunk == null) return false;

        if (!victimClaimChunk.isClaimed())
            return true;

        var victimSourceClaimChunk = getEntityClaimChunk(victim);
        var attackerSourceClaimChunk = getEntityClaimChunk(attacker);

        return victimSourceClaimChunk.hasSamePermission(attackerSourceClaimChunk);
    }

    @Override
    protected boolean isPlayerInteractionAllowed(@NonNull Player player, Location location, BarrierProtection barrierProtection) {

        if (barrierProtection == BarrierProtection.PLAYER_MOVEMENT)
            return true;

        var sourceClaimChunk = teleportOnLoad(player, location, barrierProtection);
        if (sourceClaimChunk == null) return false;

        if (!sourceClaimChunk.isClaimed())
            return true;

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        return sourceClaimChunk.allows(onlinePlayer);
    }

    @Override
    protected boolean isEntityInteractionAllowed(Entity entity, Location location, BarrierProtection barrierProtection) {
        var blockClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(location.getChunk());
        if (blockClaimChunk == null) return false;

        if (!blockClaimChunk.isClaimed())
            return true;

        var sourceClaimChunk = FactionEntityManager.getEntityChunkID(entity);
        return haveChunksSamePermission(location.getChunk(), sourceClaimChunk);
    }

    @Override
    protected boolean isEntitySpawnAllowed(Entity entity) {
        FactionEntityManager.setEntityChunkID(entity);
        return true;
    }

    @Override
    protected boolean areLocationsBetweenBorder(Location location, @Nullable Location otherlocation) {
        if (otherlocation == null) return true;

        var sourceClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(otherlocation.getChunk());
        if (sourceClaimChunk == null) return true;

        return !haveChunksSamePermission(location.getChunk(), sourceClaimChunk);
    }

    @Override
    protected boolean areBlocksBetweenBorder(List<BlockState> blocks, Location sourceLocation) {

        var sourceClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(sourceLocation.getChunk());
        if (sourceClaimChunk == null) return true;

        for (var block : blocks) {

            var blockClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(block.getChunk());
            if (blockClaimChunk == null) return true;

            if (!blockClaimChunk.isClaimed())
                continue;

            if (!haveChunksSamePermission(block.getChunk(), sourceClaimChunk))
                return true;
        }

        return false;
    }

    @Override
    protected void removeDisallowedBlocks(List<Block> blocks, Location sourceLocation) {
        var sourceClaimChunk = ClaimChunk.getClaimChunkHandler().get(sourceLocation.getChunk());
        removeDisallowedBlocks(blocks, sourceClaimChunk);
    }

    @Override
    protected void removeDisallowedBlocks(List<Block> blocks, Entity entity) {
        var sourceClaimChunk = FactionEntityManager.getEntityChunkID(entity);
        removeDisallowedBlocks(blocks, sourceClaimChunk);
    }
    //endregion
}
