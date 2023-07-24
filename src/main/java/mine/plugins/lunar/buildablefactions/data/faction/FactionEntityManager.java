package mine.plugins.lunar.buildablefactions.data.faction;

import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class FactionEntityManager {

    private static NamespacedKey chunkKey;

    public static void enable(JavaPlugin plugin) {
        chunkKey = new NamespacedKey(plugin, "chunkPlayerID");
    }

    public static boolean isEntityIDSet(Entity entity) {
        return entity.getPersistentDataContainer().get(chunkKey, PersistentDataType.STRING) != null;
    }

    public static @NonNull ClaimChunk getEntityChunkID(Entity entity) {
        var loadedID = entity.getPersistentDataContainer().get(chunkKey, PersistentDataType.STRING);
        var emptyClaimChunk = new ClaimChunk(null);
        if (loadedID == null) return emptyClaimChunk;

        try {
            return new ClaimChunk(UUID.fromString(loadedID));
        } catch (IllegalArgumentException ignored) {
            return emptyClaimChunk;
        }
    }

    public static void setEntityChunkID(Entity entity) {
        var claimChunk = ClaimChunk.getClaimChunkHandler().getLoaded(entity.getLocation().getChunk());
        if (claimChunk == null) return;

        var playerID = claimChunk.getPlayerID();
        if (playerID == null) return;

        entity.getPersistentDataContainer().set(chunkKey, PersistentDataType.STRING, playerID.toString());
    }
}
