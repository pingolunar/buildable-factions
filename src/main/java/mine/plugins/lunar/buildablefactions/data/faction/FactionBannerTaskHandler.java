package mine.plugins.lunar.buildablefactions.data.faction;

import lombok.NonNull;
import lombok.val;
import mine.plugins.lunar.buildablefactions.data.player.FactionOnlinePlayer;
import mine.plugins.lunar.buildablefactions.data.world.BannerState;
import mine.plugins.lunar.plugin_framework.border.BorderManager;
import mine.plugins.lunar.plugin_framework.task.LinearParticleTask;
import mine.plugins.lunar.plugin_framework.task.TaskHandler;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.joml.Vector2d;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

public class FactionBannerTaskHandler extends TaskHandler {

    public static final Duration bannerTaskDuration = Duration.ofSeconds(20);
    private static final List<Vector2d> circleBorder = BorderManager.getCircleBorder(0.7, 6);
    private static final float bannerHeight = 1.8f;

    private final LinearParticleTask bannerParticleTask;
    private final TaskHandler bannerSoundTask;

    private @Nullable Entity bannerNameHologram;

    public FactionBannerTaskHandler(JavaPlugin plugin) {
        super(plugin, 5);
        bannerParticleTask = new LinearParticleTask(plugin, 16, 10);
        bannerSoundTask = new TaskHandler(plugin, 120);
    }

    public void start(Banner banner, BannerState bannerState,
                      FactionOnlinePlayer onlinePlayer, Runnable databaseTask) {

        bannerNameHologram = null;

        var bannerLoc = banner.getLocation();
        var world = onlinePlayer.player.getWorld();

        val bannerTimestamp = System.currentTimeMillis();
        super.start(() -> {

            if (world.getBlockAt(bannerLoc).getType() != bannerState.type) {

                onlinePlayer.player.sendMessage(ChatColor.GRAY+"Your "+bannerState.name.toLowerCase()+
                        ChatColor.GRAY+" banner was destroyed");
                return false;
            }

            if (Duration.ofMillis(System.currentTimeMillis()-bannerTimestamp).compareTo(bannerTaskDuration) < 0)
                return true;

            databaseTask.run();

            for (var player : Bukkit.getOnlinePlayers())
                FactionOnlinePlayer.getFactionOnlinePlayerHandler().get(player).updateFactionBorder();
            return false;
        });

        var offsetBannerLoc = bannerLoc.clone();
        offsetBannerLoc.setY(bannerLoc.getY()+bannerHeight);
        bannerNameHologram = createNameHologram(offsetBannerLoc,
                onlinePlayer.getFactionName()+" "+bannerState.name);

        bannerParticleTask.clearLocations();
        bannerParticleTask.addLocations(circleBorder
            .stream().map(pos -> new Location(world, pos.x, 0, pos.y).add(bannerLoc)
            .add(0.5, 0, 0.5)).toList());

        bannerParticleTask.clearVelocities();
        bannerParticleTask.setVelocity("up", new Vector(0, bannerHeight / bannerTaskDuration.toSeconds(), 0));
        bannerParticleTask.start(onlinePlayer.player, new Particle.DustOptions(bannerState.color, 0.8f));

        bannerSoundTask.start(() -> {
            world.playSound(bannerLoc, Sound.BLOCK_BEACON_AMBIENT, 1, 1);
            return true;
        });
    }

    public void stop() {
        bannerParticleTask.stop();
        bannerSoundTask.stop();

        if (bannerNameHologram != null)
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (bannerNameHologram != null) bannerNameHologram.remove();
            });

        super.stop();
    }

    private @Nullable Entity createNameHologram(@NonNull Location loc, @NonNull String name) {
        var world = loc.getWorld();
        if (world == null) return null;

        var offsetLoc = loc.clone();
        offsetLoc.add(0.5, -2, 0.5);

        var hologram = (ArmorStand) world.spawnEntity(offsetLoc, EntityType.ARMOR_STAND);
        hologram.setAI(false);
        hologram.setCollidable(false);
        hologram.setInvisible(true);
        hologram.setInvulnerable(true);
        hologram.setGravity(false);
        hologram.setSilent(true);

        hologram.setCustomNameVisible(true);
        hologram.setCustomName(name);
        return hologram;
    }
}
