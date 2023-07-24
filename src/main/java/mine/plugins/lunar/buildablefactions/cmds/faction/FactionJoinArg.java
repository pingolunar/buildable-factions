package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.NoFactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionJoinArg extends NoFactionOnlyArg {

    public FactionJoinArg() {
        super("join", new String[] {"Missing faction name"});
    }

    @Override
    protected void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        onlinePlayer.setDatabaseTask(() -> {
            var selectedFactionName = args.getFirst();

            var selectedFactions = onlinePlayer.getInvites().map(inviteID -> Faction.getFactionHandler().get(inviteID))
                .filter(faction -> faction.getName().equalsIgnoreCase(selectedFactionName)).toList();

            if (selectedFactions.isEmpty()) {
                onlinePlayer.player.sendMessage("Faction name '"+selectedFactionName+"' doesn't exist");
                return;
            }

            var selectedFaction = selectedFactions.get(0);
            selectedFaction.join(onlinePlayer);
        });
    }

    @Override
    public String info() {
        return "Join the selected faction";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        var onlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);

        return onlinePlayer.getInvites().map(inviteID -> {
            var faction = Faction.getFactionHandler().getLoaded(inviteID);
            if (faction == null) return null;
            return faction.getName();

        }).filter(Objects::nonNull).toList();
    }
}
