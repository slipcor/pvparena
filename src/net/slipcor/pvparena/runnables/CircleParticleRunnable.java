package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlock;
import net.slipcor.pvparena.core.ColorUtils;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Utils;
import org.bukkit.*;

import java.util.Map;

public class CircleParticleRunnable implements Runnable {
    private Map<Location, String> flagMap;
    private final Arena arena;
    private double radius;
    private int i = 0;

    public CircleParticleRunnable(Arena arena, Config.CFG config, Map<Location, String> flagMap) {
        this.arena = arena;
        this.flagMap = flagMap;
        this.radius = arena.getArenaConfig().getInt(config, 3);
    }

    private Color getDustColor(Location flagLocation) {
        if(this.flagMap.containsKey(flagLocation)) {
            ChatColor teamColor = this.arena.getTeam(this.flagMap.get(flagLocation)).getColor();
            return ColorUtils.getDyeColorFromChatColor(teamColor).getColor();
        }
        return Color.WHITE;
    }

    @Override
    public void run() {

        for (PABlock spawn : this.arena.getBlocks()) {
            if (spawn.getName().startsWith("flag") || spawn.getName().startsWith("beacon")) {
                final Location flagCenter = Utils.getCenteredLocation(spawn.getLocation().toLocation());
                final double x = flagCenter.getX() + this.radius * Math.cos(Math.toRadians(this.i));
                final double y = flagCenter.getY();
                final double z = flagCenter.getZ() + this.radius * Math.sin(Math.toRadians(this.i));

                final Color dustColor = this.getDustColor(spawn.getLocation().toLocation());

                Bukkit.getWorld(this.arena.getWorld()).spawnParticle(
                        Particle.REDSTONE,
                        x, y, z,
                        0, // count
                        1, 1, 1, // offsets (colors)
                        1, // extra (lighting)
                        new Particle.DustOptions(dustColor, 1)
                );
            }
        }

        this.i += 10;

        if (this.i >= 360) {
            this.i = 0;
        }
    }
}
