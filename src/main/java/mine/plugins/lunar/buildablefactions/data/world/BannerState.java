package mine.plugins.lunar.buildablefactions.data.world;

import mine.plugins.lunar.plugin_framework.item.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public enum BannerState {
    CLAIM("Claim", Color.LIME, Material.LIME_BANNER, ChatColor.GREEN),
    UNCLAIM("Unclaim", Color.RED, Material.RED_BANNER, ChatColor.RED);

    private static final String nameSuffix = " Banner";

    public final String description;
    public final Color color;
    public final Material type;

    public final String name;

    BannerState(String description, Color color, Material type, ChatColor chatColor) {
        this.description = description;
        this.color = color;
        this.type = type;

        name = chatColor+description+nameSuffix;
    }

    public ItemStack getItem() {
        var itemBuilder = new ItemBuilder(type);
        itemBuilder.setName(name);
        itemBuilder.setLore(
                ChatColor.WHITE+"Place this banner in the floor of the chunk you want to "+ description.toLowerCase(),
                ChatColor.WHITE+"This item can be obtained by typing '/f "+description.toLowerCase()+"'");
        return itemBuilder.get();
    }

    public static @Nullable BannerState fromName(String name) {
        try {
            return BannerState.valueOf(ChatColor.stripColor(name.replaceFirst(nameSuffix, "").toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
