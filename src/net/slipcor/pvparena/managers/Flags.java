package net.slipcor.pvparena.managers;

import java.util.HashMap;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * flag manager class
 * 
 * -
 * 
 * provides commands to deal with flags/pumpkins
 * 
 * @author slipcor
 * 
 * @version v0.6.3
 * 
 */

public class Flags {

	// protected static: Debug manager (same for all child Arenas)
	public static final Debug db = new Debug();

	/**
	 * [FLAG] take away one life of a team
	 * 
	 * @param team
	 *            the team name to take away
	 * @return
	 */
	public static boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {
		if (arena.paLives.get(team) != null) {
			int i = arena.paLives.get(team) - 1;
			if (i > 0) {
				arena.paLives.put(team, i);
			} else {
				arena.paLives.remove(team);
				Ends.commit(arena, team, false);
				return true;
			}
		}
		return false;
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	protected static String getHeldFlagTeam(Arena arena, String player) {
		db.i("getting held FLAG of player " + player);
		for (String sTeam : arena.paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + arena.paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(arena.paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	/**
	 * parse player interaction
	 * 
	 * @param player
	 *            the player to parse
	 * @param block
	 *            the clicked block
	 */
	public static void checkInteract(Arena arena, Player player, Block block) {

		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		if (block == null) {
			return;
		}

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}
		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}
		db.i(type + " click!");

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;

		if (arena.paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a " + type);
			vLoc = block.getLocation().toVector();
			sTeam = arena.pm.getTeam(player);
			db.i("block: " + vLoc.toString());
			if (Spawns.getCoords(arena, sTeam + type) != null) {
				vFlag = Spawns.getCoords(arena, sTeam + type).toVector();
			} else {
				db.i(sTeam + type + " = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his " + type);

				if (arena.paTeamFlags.containsKey(sTeam)) {
					db.i("the " + type + " of the own team is taken!");

					if (arena.cfg.getBoolean("game.mustbesafe")) {
						db.i("cancelling");

						Arenas.tellPlayer(player,
								PVPArena.lang.parse(type + "notsafe"));
						return;
					}
				}

				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the " + type + " belongs to team " + flagTeam);

				String scFlagTeam = ChatColor.valueOf(arena.paTeams
						.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(arena.paTeams.get(sTeam))
						+ player.getName() + ChatColor.YELLOW;

				try {

					arena.pm.tellEveryone(PVPArena.lang.parse(
							type + "homeleft", scPlayer, scFlagTeam,
							String.valueOf(arena.paLives.get(flagTeam) - 1)));
					arena.paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + flagTeam);
				}
				takeFlag(arena.paTeams.get(flagTeam), false, pumpkin,
						Spawns.getCoords(arena, flagTeam + type));
				if (arena.cfg.getBoolean("game.woolFlagHead")) {
					player.getInventory().setHelmet(
							arena.paHeadGears.get(player.getName()).clone());
					arena.paHeadGears.remove(player.getName());
				}

				reduceLivesCheckEndAndCommit(arena, flagTeam);
			}
		} else {
			for (String team : arena.paTeams.keySet()) {
				String playerTeam = arena.pm.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!arena.pm.getPlayerTeamMap().containsValue(team))
					continue; // dont check for inactive teams
				if (arena.paTeamFlags.containsKey(team)) {
					continue; // already taken
				}
				db.i("checking for " + type + " of team " + team);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString());
				if (Spawns.getCoords(arena, team + type) != null) {
					vFlag = Spawns.getCoords(arena, team + type).toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i(type + " found!");
					db.i("vFlag: " + vFlag.toString());
					String scTeam = ChatColor.valueOf(arena.paTeams.get(team))
							+ team + ChatColor.YELLOW;
					String scPlayer = ChatColor.valueOf(arena.paTeams
							.get(playerTeam))
							+ player.getName()
							+ ChatColor.YELLOW;
					arena.pm.tellEveryone(PVPArena.lang.parse(type + "grab",
							scPlayer, scTeam));
					
					if (arena.cfg.getBoolean("game.woolFlagHead")) {
					
						arena.paHeadGears.put(player.getName(), player
								.getInventory().getHelmet().clone());
						ItemStack is = block.getState().getData().toItemStack().clone();
						is.setDurability(getFlagOverrideTeamShort(arena, team));
						player.getInventory().setHelmet(is);
					
					}
					
					takeFlag(arena.paTeams.get(team), true, pumpkin,
							block.getLocation());

					arena.paTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}
	
	/**
	 * get the durability short from a team name
	 * @param arena the arena to check
 	 * @param team the team to read
	 * @return the wool color short of the override
	 */
	private static short getFlagOverrideTeamShort(Arena arena, String team) {
		if (arena.cfg.get("flagColors."+team) == null) {
			
			return StringParser.getColorDataFromENUM(arena.paTeams.get(team));
		}
		return StringParser.getColorDataFromENUM(arena.cfg.getString("flagColors."+team));
	}

	/**
	 * take/reset an arena flag
	 * 
	 * @param flagColor
	 *            the teamcolor to reset
	 * @param take
	 *            true if take, else reset
	 * @param pumpkin
	 *            true if pumpkin, false otherwise
	 * @param lBlock
	 *            the location to take/reset
	 */
	private static void takeFlag(String flagColor, boolean take,
			boolean pumpkin, Location lBlock) {
		if (pumpkin) {
			return;
		}

		if (take) {
			lBlock.getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.getBlock().setData(
					StringParser.getColorDataFromENUM(flagColor));
		}
	}

	/**
	 * set the flag/pumpkin to the selected block
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player doing the selection
	 * @param block
	 *            the block being selected
	 */
	public static void setFlag(Arena arena, Player player, Block block) {
		if (block == null) {
			return;
		}
		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String sName = Arena.regionmodify.replace(arena.name + ":", "");

		Spawns.setCoords(arena, block.getLocation(), sName + type);

		Arenas.tellPlayer(player, PVPArena.lang.parse("set" + type, sName));

		Arena.regionmodify = "";
	}

	/**
	 * check a dying player if he held a flag, drop it, if so
	 * 
	 * @param player
	 *            the player to check
	 */
	public static void checkEntityDeath(Arena arena, Player player) {
		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String flagTeam = getHeldFlagTeam(arena, player.getName());
		if (flagTeam != null) {
			String scFlagTeam = ChatColor.valueOf(arena.paTeams.get(flagTeam))
					+ flagTeam + ChatColor.YELLOW;
			String scPlayer = ChatColor.valueOf(arena.paTeams.get(arena.pm
					.getTeam(player))) + player.getName() + ChatColor.YELLOW;
			arena.pm.tellEveryone(PVPArena.lang.parse(type + "save", scPlayer,
					scFlagTeam));
			arena.paTeamFlags.remove(flagTeam);
			if (arena.paHeadGears != null) {
				player.getInventory().setHelmet(
						arena.paHeadGears.get(player.getName()).clone());
				arena.paHeadGears.remove(player.getName());
			}

			takeFlag(arena.paTeams.get(flagTeam), false, pumpkin,
					Spawns.getCoords(arena, flagTeam + type));

		}
	}

	/**
	 * method for CTF arena to override
	 */
	public static void init_arena(Arena arena) {
		for (String sTeam : arena.paTeams.keySet()) {
			if (arena.pm.getPlayerTeamMap().containsValue(sTeam)) {
				// team is active
				arena.paLives.put(sTeam, arena.cfg.getInt("game.lives", 3));
			}
		}
		if (arena.cfg.getBoolean("arenatype.domination")) {
			arena.paFlags = new HashMap<Location, String>();
		}
	}
}
