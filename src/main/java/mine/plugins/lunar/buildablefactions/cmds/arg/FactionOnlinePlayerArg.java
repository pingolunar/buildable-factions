package mine.plugins.lunar.buildablefactions.cmds.arg;

import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.plugin_framework.cmds.args.PlayerArg;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.LinkedList;

public abstract class FactionOnlinePlayerArg extends PlayerArg {

    public FactionOnlinePlayerArg(String name) {
        this(name, null, null);
    }

    public FactionOnlinePlayerArg(String name, @Nullable String[] argsInfo) {
        this(name, argsInfo, null);
    }

    public FactionOnlinePlayerArg(String name, @Nullable String[] argsInfo, @Nullable String permission) {
        super(name, argsInfo, permission);
    }

    protected abstract void execute(FactionOnlinePlayer onlinePlayer, LinkedList<String> args);

    @Override
    protected void execute(Player player, LinkedList<String> args) {
        var onlinePlayerData = FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player);
        execute(onlinePlayerData, args);
    }
}
