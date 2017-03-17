package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlock;
import net.slipcor.pvparena.core.Config;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

public class CircleParticleRunnable implements Runnable {
    final Arena arena;
    double radius = 3;
    int i = 0;

    public CircleParticleRunnable(Arena arena, Config.CFG config) {
        this.arena = arena;
        radius = arena.getArenaConfig().getInt(config, 3);
    }

    @Override
    public void run() {

        for (PABlock spawn : arena.getBlocks()) {
            if (spawn.getName().startsWith("flag")) {
                final double x = spawn.getLocation().getX() + radius * Math.cos(Math.toRadians(i));
                final double y = spawn.getLocation().getY();
                final double z = spawn.getLocation().getZ() + radius * Math.sin(Math.toRadians(i));

                Bukkit.getWorld(arena.getWorld()).spawnParticle(
                        Particle.REDSTONE,
                        x, y, z,
                        0, // count
                        1, 1, 1, // offsets (colors)
                        1 // extra (lighting)
                );
            }
        }

        i += 10;

        if (i >= 360) {
            i = 0;
        }
    }
}
