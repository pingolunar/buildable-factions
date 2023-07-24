package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;

public class FactionInviteArg extends FactionOnlyArg {

    public FactionInviteArg() {
        super("invite", new String[] {"Missing player name"});
    }

    @Override
    protected void execute(Faction faction, FactionPlayer factionPlayer,
                           FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {

        var selectedPlayerName = args.getFirst();

        var selectedPlayer = Bukkit.getPlayer(selectedPlayerName);
        if (selectedPlayer == null) {
            onlinePlayer.player.sendMessage(selectedPlayerName+" isn't online");
            return;
        }

        var selectedOnlinePlayer = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(selectedPlayer.getUniqueId());
        if (selectedOnlinePlayer == null) {
            onlinePlayer.player.sendMessage(selectedPlayerName+" isn't available");
            return;
        }

        selectedOnlinePlayer.invite(faction);
        faction.sendFactionMsg(selectedOnlinePlayer.player.getDisplayName()+" was invited to the faction");
    }

    @Override
    public String info() {
        return "Invites a player to your faction";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).toList();
    }
}
