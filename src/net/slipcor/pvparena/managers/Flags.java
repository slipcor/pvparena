package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
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
 * @version v0.6.36
 * 
 */

public class Flags {

	// protected static: Debug manager (same for all child Arenas)
	public static final Debug db = new Debug(29);

	/**
	 * [FLAG] take away one life of a team
	 * 
	 * @param team
	 *            the team name to take away
	 * @return
	 */
	public static boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {
		db.i("reducing lives of team " + team);
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
		db.i("checking interact");

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			db.i("pumpkin & not pumpkin");
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			db.i("flag & not flag");
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
			sTeam = Players.getTeam(player);
			db.i("block: " + vLoc.toString());
			if (Spawns.getSpawns(arena, sTeam + type).size() > 0) {
				vFlag = Spawns.getNearest(Spawns.getSpawns(arena, sTeam + type), player.getLocation()).toVector();
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
								Language.parse(type + "notsafe"));
						return;
					}
				}

				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the " + type + " belongs to team " + flagTeam);

				try {

					Players.tellEveryone(arena, Language.parse(type
							+ "homeleft",
							arena.colorizePlayerByTeam(player, sTeam)
									+ ChatColor.YELLOW,
							arena.colorizeTeam(flagTeam) + ChatColor.YELLOW,
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
				String playerTeam = Players.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!Players.getPlayerTeamMap(arena).containsValue(team))
					continue; // dont check for inactive teams
				if (arena.paTeamFlags.containsKey(team)) {
					continue; // already taken
				}
				db.i("checking for " + type + " of team " + team);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString());
				if (Spawns.getSpawns(arena, team + type).size() > 0) {
					vFlag = Spawns.getNearest(Spawns.getSpawns(arena, team + type), player.getLocation()).toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i(type + " found!");
					db.i("vFlag: " + vFlag.toString());
					Players.tellEveryone(arena, Language.parse(type + "grab",
							arena.colorizePlayerByTeam(player, playerTeam)
									+ ChatColor.YELLOW,
							arena.colorizeTeam(team) + ChatColor.YELLOW));

					if (arena.cfg.getBoolean("game.woolFlagHead")) {
						try {
						arena.paHeadGears.put(player.getName(), player
								.getInventory().getHelmet().clone());
						} catch (Exception e) {
							
						}
						ItemStack is = block.getState().getData().toItemStack()
								.clone();
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
	 * 
	 * @param arena
	 *            the arena to check
	 * @param team
	 *            the team to read
	 * @return the wool color short of the override
	 */
	private static short getFlagOverrideTeamShort(Arena arena, String team) {
		if (arena.cfg.get("flagColors." + team) == null) {

			return StringParser.getColorDataFromENUM(arena.paTeams.get(team));
		}
		return StringParser.getColorDataFromENUM(arena.cfg
				.getString("flagColors." + team));
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
		db.i("trying to set a " + type);

		String sName = Arena.regionmodify.replace(arena.name + ":", "");

		// command : /pa redflag1
		// location: red1flag:
		
		Spawns.setCoords(arena, block.getLocation(), sName + type);

		Arenas.tellPlayer(player, Language.parse("set" + type, sName));

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
		db.i("checking death in a " + type + " arena");

		String flagTeam = getHeldFlagTeam(arena, player.getName());
		if (flagTeam != null) {
			Players.tellEveryone(
					arena,
					Language.parse(
							type + "save",
							arena.colorizePlayerByTeam(player,
									Players.getTeam(player))
									+ ChatColor.YELLOW,
							arena.colorizeTeam(flagTeam) + ChatColor.YELLOW));
			arena.paTeamFlags.remove(flagTeam);
			if (arena.paHeadGears != null
					&& arena.paHeadGears.get(player.getName()) != null) {
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
		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");
		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}
		for (String sTeam : arena.paTeams.keySet()) {
			if (Players.getPlayerTeamMap(arena).containsValue(sTeam)) {
				// team is active
				arena.paLives.put(sTeam, arena.cfg.getInt("game.lives", 3));
			}
			if (arena.cfg.getBoolean("arenatype.flags")) {
			takeFlag(arena.paTeams.get(sTeam), false, pumpkin,
					Spawns.getCoords(arena, sTeam + type));
			}
		}
		if (arena.cfg.getBoolean("arenatype.domination")) {
			arena.paFlags = new HashMap<Location, String>();
		}
	}

	/**
	 * check if a flag is being set
	 * 
	 * @param block
	 *            the block being clicked
	 * @param player
	 *            the player interacting
	 * @return true if a flag is being set, false otherwise
	 */
	public static boolean checkSetFlag(Block block, Player player) {

		if (Arena.regionmodify.contains(":")) {
			String[] s = Arena.regionmodify.split(":");
			Arena arena = Arenas.getArenaByName(s[0]);
			if (arena == null) {
				return false;
			}
			db.i("onInteract: flag/pumpkin");
			if (arena.cfg.getBoolean("arenatype.flags")) {
				Flags.setFlag(arena, player, block);
				if (Arena.regionmodify.equals("")) {
					return true; // success :)
				}
			}
		} else if (block != null && block.getType().equals(Material.WOOL)) {
			Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());
			if (arena != null) {
				if ((PVPArena.hasAdminPerms(player) || (PVPArena
						.hasCreatePerms(player, arena)))
						&& (player.getItemInHand() != null)
						&& (player.getItemInHand().getTypeId() == arena.cfg
								.getInt("setup.wand", 280))) {
					HashSet<Location> flags = Spawns.getSpawns(arena, "flags");
					if (flags.contains(block.getLocation())) {
						return false;
					}
					Spawns.setCoords(arena, block.getLocation(),
							"flag" + flags.size());
					Arenas.tellPlayer(
							player,
							Language.parse("setflag",
									String.valueOf(flags.size())));
					return true;
				}
			}
		}
		return false;
	}
}
