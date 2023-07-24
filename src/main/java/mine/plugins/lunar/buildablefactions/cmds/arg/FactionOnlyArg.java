package mine.plugins.lunar.buildablefactions.cmds.arg;

import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;

import javax.annotation.Nullable;
import java.util.LinkedList;

public abstract class FactionOnlyArg extends FactionOnlinePlayerArg {

    public FactionOnlyArg(String name) {
        this(name, null, null);
    }

    public FactionOnlyArg(String name, @Nullable String[] argsInfo) {
        this(name, argsInfo, null);
    }

    public FactionOnlyArg(String name, @Nullable String[] argsInfo, @Nullable String permission) {
        super(name, argsInfo, permission);
    }

    protected abstract void execute(Faction faction, FactionPlayer factionPlayer,
                                    FactionOnlinePlayer onlinePlayer, LinkedList<String> args);

    @Override
    protected void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args) {
        onlinePlayer.setDatabaseTask(() -> {
            var factionPlayer = onlinePlayer.getFactionPlayer();

            var factionData = factionPlayer.getFactionData();
            if (factionData == null) {
                onlinePlayer.player.sendMessage("You need to be in a faction to use this command");
                return;
            }

            var faction = Faction.getFactionHandler().get(factionData.factionID);
            execute(faction, factionPlayer, onlinePlayer, args);
        });
    }
}
