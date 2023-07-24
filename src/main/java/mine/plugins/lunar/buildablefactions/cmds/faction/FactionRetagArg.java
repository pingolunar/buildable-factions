package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FactionRetagArg extends FactionOnlyArg {

    public FactionRetagArg() {
        super("retag", new String[] {"Missing new tag"});
    }

    @Override
    protected void execute(Faction faction, FactionPlayer factionPlayer,
                           FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        faction.reTag(onlinePlayer, args.getFirst());
    }

    @Override
    public String info() {
        return "Replaces the tag of the faction";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return List.of("<New tag>");
    }
}
