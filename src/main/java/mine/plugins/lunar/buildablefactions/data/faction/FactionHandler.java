package mine.plugins.lunar.buildablefactions.data.faction;

import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.player.PlayerFactionData;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandlerUUID;
import mine.plugins.lunar.plugin_framework.database.loader.DatabaseYmlLoader;
import mine.plugins.lunar.plugin_framework.database.loader.main.MainDatabaseSerLoader;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.UUID;

public class FactionHandler extends DatabaseHandlerUUID<Faction> {

    public FactionHandler(JavaPlugin plugin) {
        super(plugin, 10, Faction.class,
            new MainDatabaseSerLoader<>(), new DatabaseYmlLoader<>());
    }

    @Override
    protected Faction getDefaultData() {
        return new Faction();
    }

    public void unregister(@Nullable PlayerFactionData factionData) {
        if (factionData == null) return;
        unregister(factionData.factionID);
    }

    public @Nullable Faction unregister(@NonNull UUID factionID) {
        var faction = getLoaded(factionID);

        if (faction == null) return null;
        if (faction.getPlayersOnlineAmount() > 0 || faction.getChunkCounter().getCount() > 1) return faction;

        return super.unregister(factionID);
    }

    public @Nullable Faction register(@Nullable PlayerFactionData factionData) {
        if (factionData == null) return null;
        return register(factionData.factionID);
    }

    public Faction register(Faction faction) {
        saveData(faction.id, faction);
        return register(faction.id);
    }

    public Faction getLoaded(UUID factionID) {
        return super.getLoaded(factionID);
    }

}
