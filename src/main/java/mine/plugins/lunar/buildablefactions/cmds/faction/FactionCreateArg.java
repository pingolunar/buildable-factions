package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.NoFactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionCreateArg extends NoFactionOnlyArg {

    protected FactionCreateArg() {
        super("create", new String[] {"Missing name", "Missing tag"});
    }

    @Override
    public String info() {
        return "Creates a faction and sets you as its owner";
    }

    @Override
    protected void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        Faction.create(onlinePlayer, args.getFirst(), args.getLast());
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return switch (args.size()) {
            case 0, 1 -> List.of("<Name>");
            case 2 -> List.of("<Tag>");
            default -> Collections.emptyList();
        };
    }
}
