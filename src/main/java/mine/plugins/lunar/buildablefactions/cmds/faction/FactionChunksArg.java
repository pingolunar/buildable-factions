package mine.plugins.lunar.buildablefactions.cmds.faction;

import mine.plugins.lunar.buildablefactions.cmds.arg.FactionOnlinePlayerArg;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunkHandler;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class FactionChunksArg extends FactionOnlinePlayerArg {

    public FactionChunksArg() {
        super("chunks", new String[] {});
    }

    @Override
    protected void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        var chunksInfo = new StringBuilder();
        var factionPlayer = onlinePlayer.getFactionPlayer();

        if (factionPlayer.getClaimedChunks() == 0) {
            onlinePlayer.player.sendMessage("You don't have any claimed chunks");
            return;
        }

        factionPlayer.getClaimedChunksIDs().forEach(chunkID -> {
            var chunk = ClaimChunkHandler.chunkFromID(chunkID);
            chunksInfo.append(" -").append(chunk.getWorld().getName())
                .append(" | ").append(chunk.getX()).append(", ").append(chunk.getZ()).append("\n");
        });

        onlinePlayer.player.sendMessage("Claimed chunks locations: ",
                chunksInfo.toString());
    }

    @Override
    public String info() {
        return "Shows all the locations of the chunks you own";
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Collections.emptyList();
    }
}
