package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlyArg;
import mine.plugins.lunar.buildablefactions.cmds.arg.NoFactionOnlyArg;
import mine.plugins.lunar.buildablefactions.cmds.performance.DatabasePerformanceArg;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.plugin_framework.cmds.args.Arg;
import mine.plugins.lunar.plugin_framework.cmds.args.LinkArg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class FactionLinkArg extends LinkArg {

    public FactionLinkArg(JavaPlugin plugin) {
        super("faction",
            List.of(new DatabasePerformanceArg(),
                    new FactionCreateArg(), new FactionInviteArg(),
                    new FactionRenameArg(), new FactionRetagArg(), new FactionColorArg(),
                    new FactionInfoArg(), new FactionMapArg(),
                    new FactionClaimArg(), new FactionUnclaimArg(),
                    new FactionJoinArg(), new FactionLeaveArg()));
    }

    @Override
    public String info() {
        return "Base faction command";
    }

    @Override
    protected List<Arg> tabComplete(CommandSender sender, Collection<Arg> args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        var factionPlayer = FactionPlayer.getFactionPlayerHandler().get(player.getUniqueId());
        var faction = factionPlayer.getFaction();

        Predicate<Arg> factionFilter = faction != null ? arg -> !(arg instanceof NoFactionOnlyArg) :
        arg -> !(arg instanceof FactionOnlyArg);

        return args.stream().filter(factionFilter).toList();
    }
}
