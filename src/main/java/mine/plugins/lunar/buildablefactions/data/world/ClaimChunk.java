package mine.plugins.lunar.buildablefactions.data.world;

import lombok.Getter;
import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.buildablefactions.data.player.PlayerFactionData;
import mine.plugins.lunar.plugin_framework.database.loader.type.TxtDatabase;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class ClaimChunk implements TxtDatabase {

    //region Constructors
    ClaimChunk() {
        playerID = null;
    }

    ClaimChunk(FactionOnlinePlayer onlinePlayer) {
        this.playerID = onlinePlayer.player.getUniqueId();
    }

    public ClaimChunk(@Nullable UUID playerID) { this.playerID = playerID; }
    //endregion

    //region Handler
    @Getter private static ClaimChunkHandler claimChunkHandler;

    public static void setClaimChunkHandler(JavaPlugin plugin) {
        claimChunkHandler = new ClaimChunkHandler(plugin);
    }
    //endregion

    //region Data
    @Getter private @Nullable UUID playerID;

    @Override
    public void enable(@NonNull String[] lines) {
        if (lines.length == 0) return;
        var playerID = lines[0];
        try { this.playerID = UUID.fromString(playerID); }
        catch (IllegalArgumentException ignored) { }
    }

    @Override
    public String[] disable() {
        if (playerID == null) return new String[0];
        return new String[] { playerID.toString() };
    }

    public void registerData() {
        if (playerID == null) return;

        var factionPlayer = FactionPlayer.getFactionPlayerHandler().register(playerID);
        factionPlayer.getChunkCounter().increment();

        var factionData = factionPlayer.getFactionData();
        if (factionData == null) return;

        var faction = Faction.getFactionHandler().register(factionData.factionID);
        faction.getChunkCounter().increment();
    }

    public void unregisterData() {
        if (playerID == null) return;

        var factionPlayer = FactionPlayer.getFactionPlayerHandler().unregister(playerID);
        if (factionPlayer == null) return;
        factionPlayer.getChunkCounter().decrement();

        var factionData = factionPlayer.getFactionData();
        if (factionData == null) return;

        var faction = Faction.getFactionHandler().unregister(factionData.factionID);
        if (faction == null) return;
        faction.getChunkCounter().decrement();
    }
    //endregion

    /**
     * @return If this chunk belongs to the same player or the same faction
     */
    public boolean hasSamePermission(ClaimChunk claimChunk) {
        if (Objects.equals(playerID, claimChunk.playerID)) return true;
        if (playerID == null || claimChunk.playerID == null) return false;

        var playerFactionID = getPlayerFactionID();
        var otherPlayerFactionID = claimChunk.getPlayerFactionID();
        if (playerFactionID == null || otherPlayerFactionID == null) return false;

        return playerFactionID.equals(otherPlayerFactionID);
    }

    public boolean isClaimed() {
        return playerID != null;
    }

    public boolean claim(Player player) {
        return claim(player.getUniqueId());
    }

    public boolean claim(UUID playerID) {
        if (this.playerID != null) return false;
        this.playerID = playerID;
        registerData();
        return true;
    }

    public boolean unclaim() {
        if (playerID == null) return false;
        unregisterData();
        playerID = null;
        return true;
    }

    public @Nullable FactionPlayer getPlayer() {
        if (playerID == null) return null;
        return FactionPlayer.getFactionPlayerHandler().getLoaded(playerID);
    }

    public @Nullable PlayerFactionData getPlayerFactionData() {
        var factionPlayer = getPlayer();
        if (factionPlayer == null) return null;
        return factionPlayer.getFactionData();
    }

    public @Nullable UUID getPlayerFactionID() {
        var playerFactionData = getPlayerFactionData();
        if (playerFactionData == null) return null;
        return playerFactionData.factionID;
    }

    public @Nullable Faction getFaction() {
        var playerFactionID = getPlayerFactionID();
        if (playerFactionID == null) return null;
        return Faction.getFactionHandler().getLoaded(playerFactionID);
    }

    public boolean owns(FactionOnlinePlayer onlinePlayer) {
        return onlinePlayer.player.getUniqueId().equals(playerID);
    }

    /**
     * @return If the player has claimed this chunk or if it belongs to this player's faction.
     */
    public boolean allows(FactionOnlinePlayer onlinePlayer) {
        if (owns(onlinePlayer)) return true;

        var playerFaction = onlinePlayer.getFaction();
        if (playerFaction == null) return false;

        var chunkFaction = getFaction();
        if (chunkFaction == null) return false;

        return playerFaction.id.equals(chunkFaction.id);
    }

}
