package mine.plugins.lunar.buildablefactions.cmds.performance;

import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.plugin_framework.cmds.args.Arg;
import mine.plugins.lunar.plugin_framework.database.DatabaseHandler;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.IntStream;

public class DatabasePerformanceArg extends Arg {

    private static final int totalRequestsLimit = 500;

    public DatabasePerformanceArg() {
        super("perf", new String[] {"Missing database name", "Missing requests amount"},
                "bf.debug");
    }

    private static final String databaseFactionName = "faction", databasePlayerName = "player",
            databaseChunkName = "chunk";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void execute(CommandSender sender, String base, LinkedList<String> args) {
        var databaseName = args.getFirst().toLowerCase();

        var database = switch (databaseName) {
            case databaseFactionName -> Faction.getFactionHandler();
            case databasePlayerName -> FactionPlayer.getFactionPlayerHandler();
            case databaseChunkName -> ClaimChunk.getClaimChunkHandler();
            default -> null;
        };

        if (database == null) {
            sender.sendMessage("YmlDatabase '"+databaseName+"' doesn't exist");
            return;
        }

        int totalRequests;
        try {
            totalRequests = Math.min(totalRequestsLimit, Integer.parseInt(args.getLast()));
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid total requests");
            return;
        }

        if (totalRequests <= 0) {
            sender.sendMessage("Total requests must be higher than 0");
            return;
        }

        sender.sendMessage("Preparing test on database '"+databaseName+"'...",
                "Detected "+database.getLoadedSize()+" loaded assets");

        DatabaseHandler.execute(() -> {
            var filesID = database.getFilesID().toList();
            sender.sendMessage("Executing "+totalRequests+" requests with "+filesID.size()+" file IDs...");

            var start = System.currentTimeMillis();
            IntStream.range(0, totalRequests).forEach($ -> database.get(filesID).toList());

            sender.sendMessage("Data request complete within: " + (System.currentTimeMillis() - start) + "ms");
        });
    }

    @Override
    protected Collection<String> tabComplete(CommandSender sender, LinkedList<String> args) {
        return switch(args.size()) {
            case 0, 1 -> List.of(databaseFactionName, databasePlayerName, databaseChunkName);
            case 2 -> List.of("<Request Nº>");
            default -> Collections.emptyList();
        };
    }

    @Override
    public String info() {
        return "Executes a performance test with the selected amount of requests on the selected database, " +
                "showing the time it took to complete and the amount of currently loaded assets";
    }
}
