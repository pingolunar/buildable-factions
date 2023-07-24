package mine.plugins.lunar.buildablefactions;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public record EconomyManager(JavaPlugin plugin) {

    public @Nullable Economy getEconomyProvider() {
        var server = plugin.getServer();
        if (server.getPluginManager().getPlugin("Vault") == null)
            return null;

        var rsp = server.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return null;

        return rsp.getProvider();
    }

}
