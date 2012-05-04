package net.slipcor.pvparena.arenas.teams;

import org.bukkit.ChatColor;
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
		return "v0.7.10.6";
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
