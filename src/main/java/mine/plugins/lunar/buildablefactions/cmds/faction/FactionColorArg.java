package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;

public class FactionColorArg extends FactionOnlyArg {

    public FactionColorArg() {
        super("recolor", new String[] {"Missing color"});
    }

    @Override
    protected void execute(Faction faction, FactionPlayer factionPlayer,
                           FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {

        var selectedColorName = args.getFirst();

        ChatColor selectedColor;
        try {
            selectedColor = ChatColor.valueOf(selectedColorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            onlinePlayer.player.sendMessage("Color '"+selectedColorName+"' doesn't exist");
            return;
        }

        faction.recolor(onlinePlayer, selectedColor);
    }

    @Override
    public String info() {
        return "Changes the faction color";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Faction.validColors.stream().map(Enum::name).toList();
    }
}
