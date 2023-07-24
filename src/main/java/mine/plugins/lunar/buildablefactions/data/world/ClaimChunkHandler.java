package mine.plugins.lunar.buildablefactions.data.world;

import lombok.NonNull;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandler;
import mine.plugins.lunar.plugin_framework.database.loader.DatabaseTxtLoader;
import mine.plugins.lunar.plugin_framework.database.loader.main.DefaultMainDatabaseLoader;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ClaimChunkHandler extends DatabaseHandler<ClaimChunk> {

    public ClaimChunkHandler(JavaPlugin plugin) {
        super(plugin, 2000, ClaimChunk.class,
                new DefaultMainDatabaseLoader<>(), new DatabaseTxtLoader<>(1));
    }

    @Override
    protected ClaimChunk getDefaultData() {
        return new ClaimChunk();
    }

    public static boolean areChunksEquals(@NonNull Location location, @NonNull Location otherLocation) {
        return areChunksEquals(location.getChunk(), otherLocation.getChunk());
    }

    public static boolean areChunksEquals(@NonNull Chunk chunk, @NonNull Chunk otherChunk) {
        return IDFromChunk(chunk).equals(IDFromChunk(otherChunk));
    }

    public static boolean areChunksEquals(@NonNull String chunkID, @NonNull String otherChunkID) {
        return chunkID.equals(otherChunkID);
    }

    public static String IDFromChunk(@NonNull Chunk chunk) {
        return IDFromChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static String IDFromChunk(@NonNull World world, int xChunk, int zChunk) {
        return Paths.get(world.getUID().toString(), String.valueOf(xChunk), String.valueOf(zChunk)).toString();
    }

    public static Chunk ChunkFromID(String chunkID) throws IllegalArgumentException, IllegalStateException {

        var chunkInfo = chunkID.split(Pattern.quote(File.separator));
        if (chunkInfo.length != 3) throw new IllegalArgumentException();

        var world = Bukkit.getWorld(UUID.fromString(chunkInfo[0]));
        if (world == null) throw new IllegalStateException();

        var chunkX = Integer.parseInt(chunkInfo[1]);
        var chunkZ = Integer.parseInt(chunkInfo[2]);

        return world.getChunkAt(chunkX, chunkZ);
    }

    public ClaimChunk register(Chunk chunk) {
        var claimChunk = super.register(IDFromChunk(chunk));
        claimChunk.registerData();
        return claimChunk;
    }

    public void unregister(Chunk chunk) {
        var claimChunk = super.unregister(IDFromChunk(chunk));
        if (claimChunk == null) return;
        claimChunk.unregisterData();
    }

    public ClaimChunk get(Chunk chunk) {
        return super.get(IDFromChunk(chunk));
    }

    public Stream<ClaimChunk> get(Collection<String> IDs) {
        return super.get(IDs);
    }

    public @Nullable ClaimChunk getLoaded(Chunk chunk) {
        return super.getLoaded(IDFromChunk(chunk));
    }

    public boolean isLoaded(Chunk chunk) {
        return super.isLoaded(IDFromChunk(chunk));
    }
}
