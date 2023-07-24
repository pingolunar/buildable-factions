package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class FactionInfoArg extends FactionOnlyArg {

    public FactionInfoArg() {
        super("info", new String[] {});
    }

    @Override
    protected void execute(Faction faction, FactionPlayer factionPlayer,
                           FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {

        onlinePlayer.player.sendMessage("--Faction Information--", "Name: "+faction.getName(),
            "Tag: "+faction.getTag(), "Players: ");

        var playersMsg = new StringBuilder();
        for (var player : faction.getOfflinePlayers().toList())
            playersMsg.append("- ").append(player.getName()).append(" | ").append(player.isOnline() ? "Online" : "Offline");

        onlinePlayer.player.sendMessage(playersMsg.toString());
    }

    @Override
    public String info() {
        return "Shows your faction information";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Collections.emptyList();
    }
}
