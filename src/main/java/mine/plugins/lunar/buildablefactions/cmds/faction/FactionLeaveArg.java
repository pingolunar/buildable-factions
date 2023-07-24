package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class FactionLeaveArg extends FactionOnlyArg {

    public FactionLeaveArg() {
        super("leave", new String[] {});
    }

    @Override
    protected void execute(Faction faction, FactionPlayer factionPlayer,
                           FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        faction.leave(onlinePlayer);
    }

    @Override
    public String info() {
        return "Leave the faction you're currently in";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Collections.emptyList();
    }
}
