package net.slipcor.pvparena.arenas.teams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.neworder.ArenaType;

public class TeamArena extends ArenaType {
	/**
	 * TeamName => PlayerName
	 */
	public HashMap<String, ItemStack> paHeadGears = null;
	
	public TeamArena() {
		super("teams");
	}

	@Override
	public Object clone() {
		ArenaType at = (ArenaType)super.clone();
		at.setArena(this.arena);
		return at;
	}
	
	@Override
	public String checkSpawns(Set<String> list) {
		if (allowsRandomSpawns()) {

			// now we need 1 spawn and a lounge for every team

			db.i("parsing random");

			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				db.i("parsing '" + s + "'");
				if (s.equals("lounge"))
					continue; // skip 
				if (s.startsWith("spawn"))
					spawns++;
				if (s.endsWith("lounge"))
					lounges++;
			}
			if (spawns > 3 && lounges >= arena.getTeams().size()) {
				return null;
			}

			return spawns + "/" + 4 + "x spawn ; " + lounges + "/"
					+ arena.getTeams().size() + "x lounge";
		} else {
			// not random! we need teams * 2 (lounge + spawn) + exit + spectator
			db.i("parsing not random");
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			HashSet<String> setTeams = new HashSet<String>();
			while (iter.hasNext()) {
				String s = iter.next();
				db.i("parsing '" + s + "'");
				db.i("spawns: " + spawns + "; lounges: " + lounges);
				if (s.endsWith("spawn") && (!s.equals("spawn"))) {
					spawns++;
				} else if (s.endsWith("lounge") && (!s.equals("lounge"))) {
					lounges++;
				} else if (s.contains("spawn") && (!s.equals("spawn"))) {
					String[] temp = s.split("spawn");
					if (arena.getTeam(temp[0]) != null) {
						if (setTeams.contains(temp[0])) {
							db.i("team already set");
							continue;
						}
						db.i("adding team");
						setTeams.add(temp[0]);
						spawns++;
					}
				}
			}
			if (spawns == arena.getTeams().size()
					&& lounges == arena.getTeams().size()) {
				return null;
			}

			return spawns + "/" + arena.getTeams().size() + "x spawn ; "
					+ lounges + "/" + arena.getTeams().size() + "x lounge";
		}
	}
	
	public void parseRespawn(Player respawnPlayer,
			ArenaTeam respawnTeam, int lives, DamageCause cause, Entity damager) {

		Players.tellEveryone(arena, Language.parse("killedbylives",
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				Players.parseDeathCause(arena, respawnPlayer, cause, damager),
				String.valueOf(lives)));
		arena.paLives.put(respawnPlayer.getName(), lives);
	}
}
