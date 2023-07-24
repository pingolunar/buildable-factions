package mine.plugins.lunar.buildablefactions.data.player;

import lombok.NonNull;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandler;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandlerUUID;
import mine.plugins.lunar.plugin_framework.database.loader.DefaultDatabaseLoader;
import mine.plugins.lunar.plugin_framework.database.loader.main.MainDatabaseSerLoader;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.UUID;

public class FactionPlayerHandler extends DatabaseHandlerUUID<FactionPlayer> {

    /**
     * @see DatabaseHandler
     */
    public FactionPlayerHandler(JavaPlugin plugin) {
        super(plugin, 10, FactionPlayer.class,
                new MainDatabaseSerLoader<>(), new DefaultDatabaseLoader<>());
    }

    @Override
    protected FactionPlayer getDefaultData() {
        return new FactionPlayer(null);
    }

    public FactionPlayer register(@NonNull Player player) {
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        onlinePlayer.enableGlow();
        return register(player.getUniqueId());
    }

    public FactionPlayer register(@NonNull UUID playerID) {
        return super.register(playerID);
    }

    public @Nullable FactionPlayer unregister(@NonNull UUID playerID) {
        var factionPlayer = getLoaded(playerID);

        if (factionPlayer == null) return null;
        if (factionPlayer.getChunkCounter().getCount() > 1) return factionPlayer;

        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(playerID);
        if (onlinePlayer != null) return factionPlayer;

        return super.unregister(playerID);
    }

    public FactionPlayer getLoaded(UUID playerID) {
        return super.getLoaded(playerID);
    }
}
