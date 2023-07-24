package mine.plugins.lunar.buildablefactions.data.player;

import lombok.Getter;
import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;

import java.io.Serializable;
import java.util.UUID;

public class PlayerFactionData implements Serializable {
    public final UUID factionID;
    @Getter @NonNull UUID roleID;

    PlayerFactionData(Faction faction) {
        factionID = faction.id;
        roleID = faction.rookieRole.id;
    }
}
