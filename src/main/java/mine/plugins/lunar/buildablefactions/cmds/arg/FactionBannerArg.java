package mine.plugins.lunar.buildablefactions.cmds.arg;

import mine.plugins.lunar.buildablefactions.data.world.BannerState;
import mine.plugins.lunar.plugin_framework.cmds.args.PlayerArg;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class FactionBannerArg extends PlayerArg {

    private final BannerState bannerState;

    protected FactionBannerArg(BannerState bannerState) {
        super(bannerState.description.toLowerCase(), new String[] {});
        this.bannerState = bannerState;
    }

    @Override
    public String info() {
        return "Gives the "+bannerState.name+ ChatColor.RESET+" item";
    }

    @Override
    protected void execute(Player player, LinkedList<String> args) {

        if (!giveBanner(player, bannerState)) {
            player.sendMessage("You already have the "+bannerState.name.toLowerCase());
            return;
        }

        player.sendMessage(bannerState.name+ChatColor.WHITE+" obtained");
    }

    @Override
    protected Collection<String> tabComplete(Player player, LinkedList<String> args) {
        return Collections.emptyList();
    }

    public static boolean giveBanner(Player player, BannerState bannerState) {
        var playerInv = player.getInventory();
        var bannerItem = bannerState.getItem();

        if (playerInv.contains(bannerItem)) return false;
        playerInv.addItem(bannerItem);
        return true;
    }
}
