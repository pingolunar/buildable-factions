package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlinePlayerArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class FactionMapArg extends FactionOnlinePlayerArg {

    public FactionMapArg() {
        super("map", new String[] {}, "bf.debug");
    }

    @Override
    protected void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        Faction.map(onlinePlayer);
    }

    @Override
    public String info() {
        return "Shows a map of the nearby chunks";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Collections.emptyList();
    }

}
