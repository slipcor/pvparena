package net.slipcor.pvparena.arenas.teams;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaGoal;

/**
 * team arena type class
 * 
 * contains >general< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.7.10
 * 
 */

public class TeamArena extends ArenaGoal {
	public TeamArena() {
		super("teams");
	}
	private final HashMap<String, Integer> lives = new HashMap<String, Integer>(); // flags

	@Override
	public String version() {
		return "v0.8.4.6";
	}
	
	@Override
	public void addDefaultTeams(Arena arena, YamlConfiguration config) {
		config.addDefault("game.woolHead", Boolean.valueOf(false));
		if (arena.getArenaConfig().get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
	}

	public void parseRespawn(Arena arena, Player respawnPlayer, ArenaTeam respawnTeam,
			int lives, DamageCause cause, Entity damager) {

		arena.broadcast(Language.parse("killedbylives",
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, cause, damager),
				String.valueOf(lives)));
		this.lives.put(respawnPlayer.getName(), lives);
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
	}


	public int getLives(Arena arena, Player defender) {
		return this.lives.get(defender.getName());
	}

	/**
	 * hook into a player losing lives
	 * 
	 * @param player
	 *            the player losing lives
	 * @param lives
	 *            the remaining lives
	 * @return the remaining lives
	 */
	public int reduceLives(Arena arena, Player player, int lives) {
		lives = this.lives.get(player.getName());
		db.i("lives before death: " + lives);
		return lives;
	}

	/**
	 * initiate an arena
	 * @param a 
	 */
	@Override
	public void teleportAllToSpawn(Arena arena) {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt("game.lives", 3));
			}
		}
	}
	
}
