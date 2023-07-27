package mine.plugins.lunar.buildablefactions.data.player;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.faction.FactionBannerTaskHandler;
import mine.plugins.lunar.buildablefactions.data.world.BannerState;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.plugin_framework.border.BorderManager;
import mine.plugins.lunar.plugin_framework.border.RectangleBorder;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandler;
import mine.plugins.lunar.plugin_framework.player.OnlinePlayerData;
import mine.plugins.lunar.plugin_framework.player.OnlinePlayerDataListener;
import mine.plugins.lunar.plugin_framework.task.LinearParticleTask;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2d;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class FactionOnlinePlayer extends OnlinePlayerData {

    //region Faction Online Player Handler
    @Getter private static FactionOnlinePlayerHandler factionOnlinePlayerHandler;
    private static OnlinePlayerDataListener onlinePlayerDataListener;

    public static void setFactionOnlinePlayerHandler(JavaPlugin plugin) {
        var pluginManager = plugin.getServer().getPluginManager();
        factionOnlinePlayerHandler = new FactionOnlinePlayerHandler(plugin);

        onlinePlayerDataListener = new OnlinePlayerDataListener(factionOnlinePlayerHandler);
        pluginManager.registerEvents(onlinePlayerDataListener, plugin);
    }

    public static void disable() {
        onlinePlayerDataListener.disable();
    }
    //endregion

    public FactionOnlinePlayer(JavaPlugin plugin, @NonNull Player player) {
        super(plugin, player);

        bannerTask = new FactionBannerTaskHandler(plugin);
        borderParticleTask = new LinearParticleTask(plugin, 16, 20, false);
    }

    //region Database Cooldown
    private @Nullable Future<?> databaseTask = null;

    /**
     * Sends a warning message to the player if the request couldn't be executed
     */
    public void setDatabaseTask(Runnable databaseTask) {
        setDatabaseTask(databaseTask, true);
    }

    public void setDatabaseTask(Runnable databaseTask, boolean warnPlayer) {
        setDatabaseTask(databaseTask, warnPlayer, false);
    }

    public void setDatabaseTask(Runnable databaseTask, boolean warnPlayer, boolean ignoreQueue) {
        if (!ignoreQueue && isDatabaseTaskActive()) {
            if (warnPlayer) player.sendMessage(ChatColor.GRAY+"Your last action is still being processed\n" +
                    "Try again later");
            return;
        }

        if (Debugger.isDebugActive && warnPlayer) player.sendMessage("Executing request...");
        this.databaseTask = DatabaseHandler.execute(databaseTask);

        if (this.databaseTask != null) return;

        player.sendMessage(ChatColor.RED + "Request failed, try again");
    }

    private boolean isDatabaseTaskActive() {
        return databaseTask != null && !databaseTask.isDone();
    }
    //endregion

    //region Teleport
    private static final HashMap<UUID, Chunk[]> spawnChunks = new HashMap<>();

    public static void setSpawnChunks(JavaPlugin plugin) {
        for (var world : plugin.getServer().getWorlds())
            spawnChunks.put(world.getUID(), world.getLoadedChunks());
    }

    @Getter private boolean isRandomTeleporting = false;

    public void setRandomTeleporting() {
        isRandomTeleporting = true;
    }

    public void randomTeleport() {
        try {
            player.teleport(getRandomTeleport());
            setRandomTeleporting();
            player.sendMessage("Random teleport complete");

        } catch (IllegalStateException ignored) {
            player.sendMessage(ChatColor.RED+"Random teleport failed");
        }
    }

    public Location getRandomTeleport() throws IllegalStateException {
        return getRandomTeleport(player.getWorld());
    }

    public Location getRandomTeleport(World world) throws IllegalStateException {

        var loadedChunks = spawnChunks.get(world.getUID());
        if (loadedChunks == null || loadedChunks.length == 0) throw new IllegalStateException();

        var rngLocal = getRandomLocalChunkCoordinates();

        for (int i = 0; i < 99; i++) {
            var randomLocInfo = getRandomLocation(loadedChunks, rngLocal);
            if (!randomLocInfo.isSafe()) continue;
            return randomLocInfo.loc();
        }

        return getRandomLocation(loadedChunks, rngLocal).loc();
    }

    private Vector2d getRandomLocalChunkCoordinates() {
        return new Vector2d(ThreadLocalRandom.current().nextDouble(0, BorderManager.chunkSize),
                            ThreadLocalRandom.current().nextDouble(0, BorderManager.chunkSize));
    }

    public @NonNull LocationInfo getRandomLocation(@NonNull Chunk chunk) throws IllegalStateException {
        var rngLocal = getRandomLocalChunkCoordinates();
        return getRandomLocation(chunk, rngLocal);
    }

    public @NonNull LocationInfo getRandomLocation(@NonNull Chunk chunk,
                                                   Vector2d rngLocal) throws IllegalStateException {
        var randomChunkWorld = chunk.getWorld();

        var randomChunkVec = new Vector2d(chunk.getX()*BorderManager.chunkSize+rngLocal.x,
                chunk.getZ()*BorderManager.chunkSize+rngLocal.y);

        var highestBlock = randomChunkWorld.getHighestBlockAt((int)randomChunkVec.x, (int)randomChunkVec.y);
        var randomLoc = new Location(randomChunkWorld, randomChunkVec.x, highestBlock.getY()+1, randomChunkVec.y);

        return new LocationInfo(randomLoc, !highestBlock.isPassable());
    }

    public @NonNull LocationInfo getRandomLocation(@NonNull Chunk[] chunks) throws IllegalStateException {
        var rngLocal = getRandomLocalChunkCoordinates();
        return getRandomLocation(chunks, rngLocal);
    }

    public record LocationInfo(Location loc, boolean isSafe) {}

    public @NonNull LocationInfo getRandomLocation(@NonNull Chunk[] chunks,
                                                   Vector2d rngLocal) throws IllegalStateException {
        if (chunks.length == 0)
            throw new IllegalStateException();

        var randomChunk = chunks[ThreadLocalRandom.current().nextInt(0, chunks.length)];
        return getRandomLocation(randomChunk, rngLocal);
    }

    public void setRandomTeleported() {
        isRandomTeleporting = false;
    }

    //endregion

    //region Faction
    public FactionPlayer getFactionPlayer() {
        return FactionPlayer.getFactionPlayerHandler().get(player.getUniqueId());
    }

    public @Nullable Faction getFaction() {
        var factionPlayer = getFactionPlayer();
        return factionPlayer.getFaction();
    }

    /**
     * Attempts to add the player to the faction
     * <p>
     * Ignores faction requirements
     */
    public boolean joinFaction(Faction faction) {
        return faction.addPlayer(player) && getFactionPlayer().joinFaction(faction);
    }

    /**
     * Attempts to remove the player from the faction
     * <p>
     * Ignores faction requirements
     */
    public boolean leaveFaction(Faction faction) {
        return faction.removePlayer(player) && getFactionPlayer().leaveFaction(faction);
    }

    public void enableGlow() {
        var faction = getFaction();
        if (faction == null) return;

        for (var factionPlayer : faction.getPlayersOnline()) {
            faction.setGlow(player, factionPlayer);
        }
    }
    //endregion

    //region Territory
    public static final String territorySuffix = "'s territory";

    /**
     * @return The player display name if it isn't in a faction
     */
    public String getFactionName() {
        var playerFaction = getFaction();
        if (playerFaction == null) return player.getDisplayName();

        return playerFaction.getFullName();
    }
    //endregion

    //region Faction Invite
    private final HashSet<UUID> factionInvites = new HashSet<>();

    public void invite(Faction faction) {
        factionInvites.add(faction.id);
        player.sendMessage("You were invited to join: "+faction.getFullName());
    }

    public boolean isInvited(Faction faction) {
        return factionInvites.contains(faction.id);
    }

    public Stream<UUID> getInvites() {
        return factionInvites.stream();
    }
    //endregion

    //region Faction Banner
    private static final double baseChunkPrice = 150;

    private double getChunkPrice() {
        var factionPlayer = getFactionPlayer();
        return baseChunkPrice + baseChunkPrice * factionPlayer.getClaimedChunks();
    }

    private final FactionBannerTaskHandler bannerTask;

    private void startBannerTask(Banner banner, BannerState bannerState, Runnable databaseTask) {

        if (!bannerTask.isStopped()) {
            player.sendMessage("You're already using a faction banner");
            return;
        }

        player.sendMessage(bannerState.description+"ing the chunk in "+
                FactionBannerTaskHandler.bannerTaskDuration.toSeconds()+" seconds");

        bannerTask.start(banner, bannerState, this, databaseTask);
    }

    private boolean canPayClaim(double claimPrice) {

        if (!canWithdraw(claimPrice)) {
            player.sendMessage("You need "+claimPrice+"$ to claim this chunk",
                    "You have "+econ.getBalance(player)+"$");
            return false;
        }

        return true;
    }

    public void claim(Banner banner) {
        val bannerChunk = banner.getLocation().getChunk();

        if (!canPayClaim(getChunkPrice()))
            return;

        startBannerTask(banner, BannerState.CLAIM, () -> {

            var chunkPrice = getChunkPrice();
            if (!canPayClaim(chunkPrice))
                return;

            var claimChunk = ClaimChunk.getClaimChunkHandler().get(bannerChunk);
            if (!claimChunk.claim(player)) {
                player.sendMessage("This chunk is already claimed");
                return;
            }

            withdraw(chunkPrice);

            var factionPlayer = getFactionPlayer();
            factionPlayer.addClaimedChunk(bannerChunk);

            player.sendMessage("Chunk claimed");
            banner.getWorld().playSound(banner.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        });
    }

    public void unclaim(Banner banner) {
        val bannerChunk = banner.getLocation().getChunk();

        startBannerTask(banner, BannerState.UNCLAIM, () -> {
            var claimChunk = ClaimChunk.getClaimChunkHandler().get(bannerChunk);

            if (!claimChunk.owns(this)) {
                player.sendMessage("You don't have permission to unclaim this chunk");
                return;
            }

            var claimChunkFactionPlayer = claimChunk.getPlayer();
            if (claimChunkFactionPlayer == null || !claimChunk.unclaim()) {
                player.sendMessage("This chunk isn't claimed");
                return;
            }

            claimChunkFactionPlayer.removeClaimedChunk(bannerChunk);
            deposit(getChunkPrice());

            player.sendMessage("Chunk unclaimed");
            banner.getWorld().playSound(banner.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
        });
    }
    //endregion

    //region Faction Border
    private final LinearParticleTask borderParticleTask;

    public void updateFactionBorderY(Location loc) {
        for (var location : borderParticleTask.getLocations())
            location.setY(loc.getBlockY()+1);
    }

    private Vector2d getWorldCoordinates(Chunk chunk) {
        return new Vector2d(chunk.getX() * BorderManager.chunkSize, chunk.getZ() * BorderManager.chunkSize);
    }

    public void updateFactionBorder() {
        updateFactionBorder(player.getLocation().getChunk());
    }

    public void updateFactionBorder(Chunk chunk) {
        updateFactionBorder(chunk, ClaimChunk.getClaimChunkHandler().getLoaded(chunk));
    }

    public void updateFactionBorder(Chunk chunk, @Nullable ClaimChunk claimChunk) {
        borderParticleTask.clearLocations();
        if (claimChunk == null) return;

        for (var border : RectangleBorder.values())
            addFactionBorder(chunk, claimChunk, border, false);

        var chunkWorld = chunk.getWorld();

        addNearFactionBorder(chunkWorld, chunk,
                new Vector2d[] {new Vector2d(-1, -1), new Vector2d(1, -1)}, true);

        addNearFactionBorder(chunkWorld, chunk,
                new Vector2d[] {new Vector2d(-1, 1), new Vector2d(1, 1)}, false);

        borderParticleTask.restart(player, new Particle.DustOptions(Color.LIME, 0.7f));
    }

    private void addNearFactionBorder(World chunkWorld, Chunk chunk,
                                      Vector2d[] otherChunksRelativePos, boolean reverseDir) {

        for (var otherChunkRelativePos : otherChunksRelativePos) {
            var otherChunk = chunkWorld.getChunkAt(chunk.getX() + (int) otherChunkRelativePos.x,
                    chunk.getZ() + (int) otherChunkRelativePos.y);

            var otherClaimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(otherChunk);
            if (otherClaimChunk == null) continue;

            for (var border : RectangleBorder.values())
                addFactionBorder(otherChunk, otherClaimChunk, border, reverseDir);
        }
    }

    /**
     * Chunks should be different
     */
    private void addFactionBorder(Chunk fromChunk, @NonNull ClaimChunk fromChunkClaim,
                                  RectangleBorder rectangleBorder, boolean reverseDir) {

        var toChunkCoordinates = new Vector2d(fromChunk.getX(), fromChunk.getZ());
        toChunkCoordinates.add(rectangleBorder.chunkRelativePos);

        var toChunk = fromChunk.getWorld().getChunkAt((int)toChunkCoordinates.x, (int)toChunkCoordinates.y);

        var toChunkClaim = ClaimChunk.getClaimChunkHandler().getLoaded(toChunk);
        if (toChunkClaim == null) return;

        if (fromChunkClaim.allows(this) == toChunkClaim.allows(this))
            return;

        var fromChunkWorld = fromChunk.getWorld();
        var fromWorldCoordinates = getWorldCoordinates(fromChunk);

        var playerYLoc = player.getLocation().getY();

        if (reverseDir) {
            fromWorldCoordinates.add(new Vector2d(rectangleBorder.chunkRelativePos).mul(BorderManager.chunkSize));
            rectangleBorder = rectangleBorder.getOpposite();
        }

        var particleLocations = rectangleBorder.localLineBorder.stream().map(borderLinePos -> {
            var particlePos = new Vector2d(fromWorldCoordinates).add(borderLinePos);
            return new Location(fromChunkWorld, particlePos.x, playerYLoc+1, particlePos.y);
        }).toList();

        borderParticleTask.addLocations(particleLocations);
    }
    //endregion

    //region Economy
    @Setter private static @NonNull Economy econ;

    private void debugTransaction(EconomyResponse transactionResponse) {
        if (!Debugger.isDebugActive) return;
        player.sendMessage("Transaction success: "+transactionResponse.transactionSuccess(),
                "Error msg: "+transactionResponse.errorMessage);
    }

    public void deposit(double amount) {
        var transactionResponse = econ.depositPlayer(player, amount);
        debugTransaction(transactionResponse);
        if (transactionResponse.transactionSuccess()) player.sendMessage("Received "+amount+"$");
    }

    public boolean canWithdraw(double amount) {
        return econ.has(player, amount);
    }

    public void withdraw(double amount) {
        var transactionResponse = econ.withdrawPlayer(player, amount);
        debugTransaction(transactionResponse);
        if (transactionResponse.transactionSuccess()) player.sendMessage("Paid "+amount+"$");
    }
    //endregion
}
