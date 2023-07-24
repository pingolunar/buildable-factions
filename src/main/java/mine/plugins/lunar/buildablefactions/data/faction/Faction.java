package mine.plugins.lunar.buildablefactions.data.faction;

import fr.skytasul.glowingentities.GlowingEntities;
import lombok.Getter;
import lombok.NonNull;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.player.FactionPlayer;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunk;
import mine.plugins.lunar.buildablefactions.data.world.ClaimChunkHandler;
import mine.plugins.lunar.plugin_framework.data.Debugger;
import mine.plugins.lunar.plugin_framework.database.loader.type.YmlDatabase;
import mine.plugins.lunar.plugin_framework.utils.Counter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Faction implements Serializable, YmlDatabase {

    //region Constructors
    Faction() {
        this("", "");
    }

    Faction(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }
    //endregion

    //region Handler
    private static JavaPlugin plugin;

    private static GlowingEntities glowHandler;
    @Getter private static FactionHandler factionHandler;

    public static void setFactionHandler(JavaPlugin plugin) {
        Faction.plugin = plugin;

        factionHandler = new FactionHandler(plugin);
        glowHandler = new GlowingEntities(plugin);
    }

    public static void disableGlowHandler() {
        glowHandler.disable();
    }
    //endregion

    //region Map
    private final static int mapSize = 4;

    private final static ChatColor wildernessColor = ChatColor.GRAY, factionColor = ChatColor.GREEN,
            otherFactionColor = ChatColor.RED, playerColor = ChatColor.BLUE;

    private final static String mapIcon = "□";

    private static ChatColor getChunkColor(ClaimChunk claimChunk, FactionOnlinePlayer onlinePlayer) {
        if (!claimChunk.isClaimed()) return wildernessColor;
        if (claimChunk.owns(onlinePlayer)) return playerColor;
        if (claimChunk.allows(onlinePlayer)) return factionColor;
        return otherFactionColor;
    }

    private static String getMapIcon(ClaimChunk claimChunk, FactionOnlinePlayer onlinePlayer) {
        return getChunkColor(claimChunk, onlinePlayer) + mapIcon;
    }

    public static void map(FactionOnlinePlayer onlinePlayer) {

        onlinePlayer.player.sendMessage("Generating map...");
        onlinePlayer.setDatabaseTask(() -> {

            var playerChunk = onlinePlayer.player.getLocation().getChunk();
            var xChunk = playerChunk.getX(); var zChunk = playerChunk.getZ();

            var mapLineSize = mapSize * 2 + 1;
            var chunksID = new String[(int) Math.pow(mapLineSize, 2)];

            for (int i = 0, z = zChunk - mapSize; z <= zChunk + mapSize; z++)
                for (int x = xChunk - mapSize; x <= xChunk + mapSize; x++, i++)
                    chunksID[i] = ClaimChunkHandler.IDFromChunk(onlinePlayer.player.getWorld(), x, z);

            var claimChunks = ClaimChunk.getClaimChunkHandler().get(List.of(chunksID)).toList();
            var msg = new StringBuilder("--Chunk Map--\n");

            int i = 0;
            for (var claimChunk : claimChunks) {
                msg.append(" ").append(getMapIcon(claimChunk, onlinePlayer));
                if (++i >= mapLineSize) { msg.append("\n"); i = 0; }
            }

            onlinePlayer.player.sendMessage(msg.toString());
        });
    }
    //endregion

    //region Join
    /**
     * Attempts to add the player to the faction
     */
    public boolean join(FactionOnlinePlayer onlinePlayer) {

        if (!isPublic && !onlinePlayer.isInvited(this)) {
            onlinePlayer.player.sendMessage("You can't join a private faction without an invite");
            return false;
        }

        if (!onlinePlayer.joinFaction(this)) {
            onlinePlayer.player.sendMessage("You're already in a faction");
            return false;
        }

        sendFactionMsg(onlinePlayer.player.getDisplayName()+" has joined the faction");
        return true;
    }
    //endregion

    //region Leave
    public void leave(FactionOnlinePlayer onlinePlayer) {

        if (!onlinePlayer.leaveFaction(this)) {
            onlinePlayer.player.sendMessage("You're not in a faction");
            return;
        }

        onlinePlayer.player.sendMessage("You have left the faction: "+getFullName());
        sendFactionMsg(onlinePlayer.player.getDisplayName()+" left the faction");
        if (!isDisabled()) return;

        isPublic = true;
        factionHandler.unregister(id);
    }
    //endregion

    //region Create
    public final static int maxFactions = 20;

    private static void create(Faction playerFaction, FactionOnlinePlayer onlinePlayer) {

        if (!isNameLengthCorrect(onlinePlayer.player, playerFaction.name)
        || !isTagLengthCorrect(onlinePlayer.player, playerFaction.tag))
            return;

        onlinePlayer.player.sendMessage("Verifying faction...");
        onlinePlayer.setDatabaseTask(() -> {
            var factions = factionHandler.get().toList();

            if (factions.size() >= maxFactions) {
                onlinePlayer.player.sendMessage("There's already a maximum of "+maxFactions+" factions",
                        "Consider joining one instead");
                return;
            }

            for (var faction : factions)
                if (isNameUsed(onlinePlayer.player, playerFaction.name, faction)
                    || isTagUsed(onlinePlayer.player, playerFaction.tag, faction)) {

                    if (Debugger.isDebugActive) onlinePlayer.player.sendMessage("Faction is invalid");
                    return;
                }

            if (!playerFaction.join(onlinePlayer))
                return;

            playerFaction.isPublic = false;
            factionHandler.register(playerFaction);
            onlinePlayer.player.sendMessage("Faction '"+playerFaction.name+"' created");
        });
    }

    public static void create(FactionOnlinePlayer onlinePlayer, String name, String tag) {
        create(new Faction(name, tag), onlinePlayer);
    }
    //endregion

    //region Name
    private static final int maxNameLength = 20;

    @Getter private String name;

    private static boolean isNameLengthCorrect(Player player, String name) {
        if (name.length() <= tagLength) {
            player.sendMessage("Faction name must have at least "+(tagLength+1)+" letters");
            return false;
        }

        if (name.length() > maxNameLength) {
            player.sendMessage("Faction name can't have more than "+maxNameLength+" letters");
            return false;
        }

        return true;
    }

    private static boolean isNameUsed(Player player, String name, Faction faction) {
        if (faction.isDisabled()) return false;

        var isNameUsed = faction.name.equalsIgnoreCase(name);
        if (isNameUsed) player.sendMessage("Faction name '"+name+"' is already in use");
        return isNameUsed;
    }

    private static boolean isNameValid(Player player, String name, Collection<Faction> factions) {
        if (!isNameLengthCorrect(player, name))
            return false;

        for (var faction : factions)
            if (isNameUsed(player, name, faction))
                return false;

        return true;
    }

    public void rename(FactionOnlinePlayer onlinePlayer, String name) {
        var factions = factionHandler.get().toList();

        if (!isNameValid(onlinePlayer.player, name, factions)) {
            if (Debugger.isDebugActive) onlinePlayer.player.sendMessage("Invalid name");
            return;
        }

        this.name = name;
        sendFactionMsg("Your faction was renamed to: "+name);
    }

    /**
     * @return The colored name and tag
     */
    public String getFullName() {
        return color+name+ChatColor.RESET+" ["+color+tag.toUpperCase()+ChatColor.RESET+"]";
    }
    //endregion

    //region Tag
    private static final int tagLength = 3;

    @Getter private String tag;

    private static boolean isTagLengthCorrect(Player player, String tag) {
        var isTagLengthCorrect = tag.length() == tagLength;
        if (!isTagLengthCorrect) player.sendMessage("Faction tag must have "+tagLength+" letters");
        return isTagLengthCorrect;
    }

    private static boolean isTagUsed(Player player, String tag, Faction faction) {
        if (faction.isDisabled()) return false;

        var isTagUsed = faction.tag.equalsIgnoreCase(tag);
        if (isTagUsed) player.sendMessage("Tag '"+tag+"' is already in use");
        return isTagUsed;
    }

    private static boolean isTagValid(Player player, String tag, Collection<Faction> factions) {
        if (!isTagLengthCorrect(player, tag))
            return false;

        for (var faction : factions)
            if (isTagUsed(player, tag, faction))
                return false;

        return true;
    }

    public void reTag(FactionOnlinePlayer onlinePlayer, String tag) {
        var factions = factionHandler.get().toList();

        if (!isTagValid(onlinePlayer.player, tag, factions)) {
            if (Debugger.isDebugActive) onlinePlayer.player.sendMessage("Invalid tag");
            return;
        }

        this.tag = tag;
        sendFactionMsg("Your faction tag was changed to: "+tag);
    }
    //endregion

    //region Color
    public static final Collection<ChatColor> validColors = List.of(
            ChatColor.BLACK, ChatColor.DARK_GRAY, ChatColor.GRAY, ChatColor.WHITE, ChatColor.GOLD, ChatColor.YELLOW,
            ChatColor.DARK_BLUE, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.DARK_GREEN,ChatColor.GREEN,
            ChatColor.DARK_RED, ChatColor.RED, ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE);

    private @NonNull ChatColor color = ChatColor.WHITE;

    public void recolor(FactionOnlinePlayer onlinePlayer, @NonNull ChatColor color) {

        if (!validColors.contains(color)) {
            onlinePlayer.player.sendMessage("Color '"+color+" isn't supported");
            return;
        }

        this.color = color;
        sendFactionMsg("Faction color changed to: "+color.name());
    }
    //endregion

    //region Data
    public final UUID id = UUID.randomUUID();
    @Getter private transient Counter chunkCounter;

    private static final String bannerID = "banner";

    @Override
    public void enable(YamlConfiguration config) {
        factionBanner = config.getItemStack(bannerID, new ItemStack(Material.WHITE_BANNER));
        chunkCounter = new Counter();
    }

    @Override
    public YamlConfiguration disable() {
        var config = new YamlConfiguration();
        config.set(bannerID, factionBanner);
        return config;
    }
    //endregion

    //region Banner
    @Getter private transient ItemStack factionBanner;

    public void setBanner(FactionOnlinePlayer onlinePlayer, ItemStack bannerItem) {

        if (!(bannerItem instanceof Banner)) {
            onlinePlayer.player.sendMessage("You must be holding a banner to change the faction banner");
            return;
        }

        factionBanner = bannerItem;
        sendFactionMsg("The faction banner was updated");
    }
    //endregion

    //region State
    @Getter private boolean isPublic = true;

    public void toggleState() {
        isPublic = !isPublic;
        sendFactionMsg("Your faction is now "+(isPublic ? "public" : "private"));
    }

    public boolean isDisabled() {
        return playersIDs.isEmpty() && !isPublic;
    }
    //endregion

    //region Mail
    private static final Duration mailMsgCooldown = Duration.ofSeconds(60);
    private long mailMsgTimestamp = System.currentTimeMillis();

    public boolean isMailOnCooldown() {
        return Duration.ofMillis(System.currentTimeMillis())
                .minus(Duration.ofMillis(mailMsgTimestamp)).compareTo(mailMsgCooldown) < 0;
    }

    public void sendMail() {
        if (isMailOnCooldown()) return;
        mailMsgTimestamp = System.currentTimeMillis();


    }
    //endregion

    //region Roles
    private final HashMap<UUID, FactionRole> roles = new HashMap<>();
    public final FactionRole rookieRole = new FactionRole("Rookie", ChatColor.GREEN);

    public @Nullable FactionRole getRole(UUID id) {
        var role = roles.get(id);
        if (role != null) return role;
        return rookieRole.id.equals(id) ? rookieRole : null;
    }

    public void addRole(Player player, FactionRole role) {
        roles.put(role.id, role);
        player.sendMessage("Role '"+role.getName()+" created");
    }

    public void removeRole(FactionOnlinePlayer onlinePlayer, FactionRole role) {
        var factionPlayers = FactionPlayer.getFactionPlayerHandler().getList(List.copyOf(playersIDs)).toList();

        for (var factionPlayer : factionPlayers)
            if (role.id.equals(factionPlayer.getFactionRoleID())) {
                onlinePlayer.player.sendMessage("Can't remove a role with players assigned to it");
                return;
            }

        var roleRemoved = roles.remove(role.id);
        onlinePlayer.player.sendMessage("Role '"+(roleRemoved != null ? role.getName() : "Unknown")+" removed");
    }
    //endregion

    //region Players
    private final HashSet<UUID> playersIDs = new HashSet<>();

    public Stream<OfflinePlayer> getOfflinePlayers() {
        return playersIDs.stream().map(Bukkit::getOfflinePlayer);
    }

    public boolean addPlayer(Player player) {
        for (var onlineFactionPlayer : getPlayersOnline())
            setGlow(onlineFactionPlayer, player);

        return playersIDs.add(player.getUniqueId());
    }

    public boolean removePlayer(Player player) {
        return playersIDs.remove(player.getUniqueId());
    }

    public long getPlayersOnlineAmount() {
        return playersIDs.stream().filter(playerID -> factionHandler.isLoaded(playerID.toString())).count();
    }

    public Collection<Player> getPlayersOnline() {
        return playersIDs.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    /**
     * Sends a chat message to all online faction players
     */
    public void sendFactionMsg(String msg) {
        for (var onlineFactionPlayer : getPlayersOnline())
            onlineFactionPlayer.sendMessage(msg);
    }

    public void setGlow(Player player, Player otherPlayer)  {
        try {
            glowHandler.setGlowing(player, otherPlayer, rookieRole.getColor());
            glowHandler.setGlowing(otherPlayer, player, rookieRole.getColor());

        } catch (ReflectiveOperationException ignored) {
            if (!Debugger.isDebugActive) return;
            Faction.plugin.getLogger().log(Level.WARNING, "Failed to set glow between '"+
                    player.getDisplayName()+"' and '"+otherPlayer.getDisplayName()+"'");
        }
    }

    public static void unsetGlow(PlayerQuitEvent e) {
        glowHandler.onQuit(e);
    }
    //endregion
}
