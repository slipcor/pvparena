package net.slipcor.pvparena.arenas.teams;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaType;

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

public class TeamArena extends ArenaType {
	public TeamArena() {
		super("teams");
	}

	@Override
	public String version() {
		return "v0.8.4.6";
	}
	
	@Override
	public void addDefaultTeams(YamlConfiguration config) {
		config.addDefault("game.woolHead", Boolean.valueOf(false));
		
		if (arena.cfg.get("teams") == null || arena.cfg.get("teams.free") != null) {
			arena.cfg.set("teams.free", null);
			db.i("no teams defined, adding custom red and blue!");
			arena.cfg.getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.cfg.getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
	}

	public void parseRespawn(Player respawnPlayer, ArenaTeam respawnTeam,
			int lives, DamageCause cause, Entity damager) {

		arena.tellEveryone(Language.parse("killedbylives",
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, cause, damager),
				String.valueOf(lives)));
		arena.lives.put(respawnPlayer.getName(), lives);
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
	}
}
