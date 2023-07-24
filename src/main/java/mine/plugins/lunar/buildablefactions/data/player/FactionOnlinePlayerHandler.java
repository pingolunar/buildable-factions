package mine.plugins.lunar.buildablefactions.data.player;

import mine.plugins.lunar.plugin_framework.player.OnlinePlayerDataHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionOnlinePlayerHandler extends OnlinePlayerDataHandler<FactionOnlinePlayer> {

    public FactionOnlinePlayerHandler(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected FactionOnlinePlayer getDefaultPlayerData(Player player) {
        return new FactionOnlinePlayer(plugin, player);
    }
}
