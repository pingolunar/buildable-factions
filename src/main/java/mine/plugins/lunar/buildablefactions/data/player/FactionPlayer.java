package mine.plugins.lunar.buildablefactions.data.player;

import lombok.Getter;
import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.faction.FactionRole;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunkHandler;
import mine.plugins.lunar.plugin_framework.database.loader.type.DefaultDatabase;
import mine.plugins.lunar.plugin_framework.utils.Counter;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Stream;

public class FactionPlayer implements Serializable, DefaultDatabase {

    //region Faction Player Handler
    @Getter private static FactionPlayerHandler factionPlayerHandler;

    public static void setFactionPlayerHandler(JavaPlugin plugin) {
        factionPlayerHandler = new FactionPlayerHandler(plugin);
    }
    //endregion

    FactionPlayer(@Nullable PlayerFactionData factionData) {
        this.factionData = factionData;
    }

    //region Data
    private boolean isNew = true;
    public boolean isNew() {
        var isNewCopy = isNew;
        isNew = false;
        return isNewCopy;
    }

    @Getter private transient Counter chunkCounter;

    @Override
    public void enable() {
        chunkCounter = new Counter();
    }

    @Override
    public void disable() {

    }
    //endregion

    //region Faction
    private @Getter @Nullable PlayerFactionData factionData;

    public @Nullable Faction getFaction() {
        if (factionData == null) return null;
        return Faction.getFactionHandler().get(factionData.factionID);
    }

    /**
     * Attempts to register the faction data
     * @return If it was successful
     */
    boolean joinFaction(@NonNull Faction faction) {
        if (factionData != null) return false;
        factionData = new PlayerFactionData(faction);
        return true;
    }

    /**
     * Attempts to clear the faction data
     */
    boolean leaveFaction(@NonNull Faction faction) {
        if (factionData == null) return false;
        factionData = null;
        return true;
    }
    //endregion

    //region Role
    public void setFactionRole(FactionRole role) {
        if (factionData == null) return;
        factionData.roleID = role.id;
    }

    public @Nullable UUID getFactionRoleID() {
        if (factionData == null) return null;
        return factionData.roleID;
    }
    //endregion

    //region Chunk Claims
    private final HashSet<String> claimedChunks = new HashSet<>();

    public Stream<String> getClaimedChunksIDs() {
        return claimedChunks.stream();
    }

    public void addClaimedChunk(Chunk chunk) {
        claimedChunks.add(ClaimChunkHandler.IDFromChunk(chunk));
    }

    public void removeClaimedChunk(Chunk chunk) {
        claimedChunks.remove(ClaimChunkHandler.IDFromChunk(chunk));
    }

    public @Nullable String getRandomClaimChunkID() {
        var randomClaimChunkID = claimedChunks.stream().findAny();
        return randomClaimChunkID.orElse(null);
    }

    public int getClaimedChunks() {
        return claimedChunks.size();
    }
    //endregion
}
