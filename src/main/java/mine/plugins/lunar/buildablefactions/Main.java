package mine.plugins.lunar.buildablefactions;

import mine.plugins.lunar.buildablefactions.cmds.faction.FactionLinkArg;
import mine.plugins.lunar.buildablefactions.data.faction.FactionEntityManager;
import mine.plugins.lunar.buildablefactions.data.faction.Faction;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.buildablefactions.events.FactionBarrierListener;
import mine.plugins.lunar.buildablefactions.events.FactionLoaderListener;
import mine.plugins.lunar.buildablefactions.events.FactionWelcomeListener;
import mine.plugins.lunar.plugin_framework.cmds.BaseCmd;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Debugger.isDebugActive = false;
        var pluginManager = getServer().getPluginManager();

        FactionEntityManager.enable(this);

        Faction.setFactionHandler(this);

        FactionPlayer.setFactionPlayerHandler(this);

        FactionOnlinePlayer.setFactionOnlinePlayerHandler(this);
        FactionOnlinePlayer.setSpawnChunks(this);

        ClaimChunk.setClaimChunkHandler(this);

        getLogger().log(Level.INFO, "Enabling Vault economy API...");

        var econ = new EconomyManager(this).getEconomyProvider();
        if (econ == null) {
            getLogger().log(Level.SEVERE, "Failed to enable Vault economy API");
            pluginManager.disablePlugin(this);
            return;
        }

        FactionOnlinePlayer.setEcon(econ);

        pluginManager.registerEvents(new FactionLoaderListener(this), this);
        pluginManager.registerEvents(new FactionBarrierListener(this), this);
        pluginManager.registerEvents(new FactionWelcomeListener(this), this);

        new BaseCmd(new FactionLinkArg(this), Collections.singletonList("f"));
    }

    @Override
    public void onDisable() {
        FactionLoaderListener.disable();
        FactionOnlinePlayer.disable();
        Faction.disableGlowHandler();
    }
}
