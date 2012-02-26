package net.slipcor.pvparena.core;

import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * help manager class
 * 
 * -
 * 
 * provides command parsing to help players get along with PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class Help {
	private Debug db = new Debug(3);

	/**
	 * display detailed help
	 * 
	 * @param player
	 *            the player committing the command
	 * @param args
	 *            the command arguments
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseCommand(Player player, String[] args) {
		Arenas.tellPlayer(player, ChatColor.AQUA + "---  PVP Arena Help  ---");

		if (args.length == 1) {

			helpList(player);

		} else if (args.length == 2) {

			if (args[1].equalsIgnoreCase("general")) {

				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "--- General Commands ---");
				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general join" + ChatColor.WHITE
						+ " | for help with joining");
				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general watch" + ChatColor.WHITE
						+ " | for help with watching");
				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general bet" + ChatColor.WHITE
						+ " | for help with bets");
				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general leave" + ChatColor.WHITE
						+ " | for help with leaving");

			} else if (args[1].equalsIgnoreCase("info")) {

				Arenas.tellPlayer(player, ChatColor.BLUE
						+ "---  Info  Commands  ---");
				Arenas.tellPlayer(player, ChatColor.BLUE + "/pa help info list"
						+ ChatColor.WHITE + " | for information commands");
				Arenas.tellPlayer(player, ChatColor.BLUE
						+ "/pa help info stats" + ChatColor.WHITE
						+ " | for information commands");

			} else if (args[1].equalsIgnoreCase("setup")) {

				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "---  Setup Commands  ---");
				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "/pa help setup create" + ChatColor.WHITE
						+ " | for help with spawns");
				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "/pa help setup spawn" + ChatColor.WHITE
						+ " | for help with spawns");
				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "/pa help setup regionset" + ChatColor.WHITE
						+ " | for help with setting a region");
				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "/pa help setup regionsave" + ChatColor.WHITE
						+ " | for help with saving a region");
				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "/pa help setup regionremove" + ChatColor.WHITE
						+ " | for help with removing a region");

			} else if (args[1].equalsIgnoreCase("admin")) {

				Arenas.tellPlayer(player, ChatColor.RED
						+ "---  Admin Commands  ---");
				Arenas.tellPlayer(player, ChatColor.RED
						+ "/pa help admin reload" + ChatColor.WHITE
						+ " | reloading");
				Arenas.tellPlayer(player, ChatColor.RED
						+ "/pa help admin remove" + ChatColor.WHITE
						+ " | removing");
				Arenas.tellPlayer(player, ChatColor.RED
						+ "/pa help admin disable" + ChatColor.WHITE
						+ " | disabling");
				Arenas.tellPlayer(player, ChatColor.RED
						+ "/pa help admin forcestop" + ChatColor.WHITE
						+ " | force-stopping");

			} else {
				helpList(player);
			}

		} else if (args.length == 3) {

			if (args[1].equalsIgnoreCase("general")) {

				Arenas.tellPlayer(player, ChatColor.YELLOW
						+ "--- General Commands ---");

				if (args[2].equalsIgnoreCase("join")) {

					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname]" + ChatColor.WHITE
							+ " | Join the arena, random chooses team");
					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname] [teamname]" + ChatColor.WHITE
							+ " | Join the arena team");

				} else if (args[2].equalsIgnoreCase("watch")) {

					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname] watch" + ChatColor.WHITE
							+ " | Watch a fight");

				} else if (args[2].equalsIgnoreCase("bet")) {

					Arenas.tellPlayer(
							player,
							ChatColor.YELLOW
									+ "/pa [arenaname] bet [(player/team)name] [amount]"
									+ ChatColor.WHITE
									+ " | Bet on a player/team");
					Arenas.tellPlayer(player,
							"Note: You get 4x money for betting on a the right player, ");
					Arenas.tellPlayer(player,
							"      2x the money for betting on the right team");

				} else if (args[2].equalsIgnoreCase("leave")) {

					Arenas.tellPlayer(player, ChatColor.YELLOW + "/pa leave"
							+ ChatColor.WHITE + " | Leave the arena you are in");

				} else {

					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general join" + ChatColor.WHITE
							+ " | for help with joining");
					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general watch" + ChatColor.WHITE
							+ " | for help with watching");
					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general bet" + ChatColor.WHITE
							+ " | for help with bets");
					Arenas.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general leave" + ChatColor.WHITE
							+ " | for help with leaving");

				}

			} else if (args[1].equalsIgnoreCase("info")) {

				Arenas.tellPlayer(player, ChatColor.BLUE
						+ "---  Info  Commands  ---");

				if (args[2].equalsIgnoreCase("list")) {

					Arenas.tellPlayer(player, ChatColor.BLUE + "/pa list"
							+ ChatColor.WHITE + " | List all arenas");
					Arenas.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] list" + ChatColor.WHITE
							+ " | List all players in the arena");

				} else if (args[2].equalsIgnoreCase("stats")) {

					Arenas.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] teams" + ChatColor.WHITE
							+ " | Get teams statistics");
					Arenas.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] users" + ChatColor.WHITE
							+ " | Get users statistics");

				} else {

					Arenas.tellPlayer(player, ChatColor.BLUE
							+ "/pa help info list" + ChatColor.WHITE
							+ " | for information commands");
					Arenas.tellPlayer(player, ChatColor.BLUE
							+ "/pa help info stats" + ChatColor.WHITE
							+ " | for information commands");

				}

			} else if (args[1].equalsIgnoreCase("setup")) {

				Arenas.tellPlayer(player, ChatColor.GREEN
						+ "---  Setup Commands  ---");

				if (args[2].equalsIgnoreCase("create")) {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] create [type]" + ChatColor.WHITE
							+ " | Create an arena");
					Arenas.tellPlayer(player,
							"Note: valid types are: team, ctf, free ; all other types fall back to team");

				} else if (args[2].equalsIgnoreCase("spawn")) {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] [spawn]" + ChatColor.WHITE
							+ " | Set the spawn");
					Arenas.tellPlayer(player, "Valid spawns are:");
					Arenas.tellPlayer(player,
							"All Arena types: spectator, exit");
					Arenas.tellPlayer(player,
							"CTF: [teamname]lounge, [teamname]spawn");
					Arenas.tellPlayer(player,
							"Team: [teamname]lounge, [teamname]spawn (if randomSpawn is not set)");
					Arenas.tellPlayer(player,
							"Free: lounge, spawn[x] (or for Team if randomSpawn is set)");
					Arenas.tellPlayer(player,
							"Note: [x] can be any letter or digit.");

				} else if (args[2].equalsIgnoreCase("regionset")) {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region" + ChatColor.WHITE
							+ " | Start setting a region");

				} else if (args[2].equalsIgnoreCase("regionsave")) {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region [regionname]"
							+ ChatColor.WHITE + " | Save selection to region");
					Arenas.tellPlayer(player, "Valid region names are:");
					Arenas.tellPlayer(player,
							"All Arena types: spectator, exit");
					Arenas.tellPlayer(player, "CTF, Team: [teamname]lounge");
					Arenas.tellPlayer(player, "Free: freelounge");

				} else if (args[2].equalsIgnoreCase("regionremove")) {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region remove [regionname]"
							+ ChatColor.WHITE + " | Remove a region");
					Arenas.tellPlayer(player, "Valid region names are:");
					Arenas.tellPlayer(player,
							"All Arena types: spectator, exit");
					Arenas.tellPlayer(player, "CTF, Team: [teamname]lounge");
					Arenas.tellPlayer(player, "Free: freelounge");

				} else {

					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa help setup create" + ChatColor.WHITE
							+ " | for help with spawns");
					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa help setup spawn" + ChatColor.WHITE
							+ " | for help with spawns");
					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa help setup regionset" + ChatColor.WHITE
							+ " | for help with setting a region");
					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa help setup regionsave" + ChatColor.WHITE
							+ " | for help with saving a region");
					Arenas.tellPlayer(player, ChatColor.GREEN
							+ "/pa help setup regionremove" + ChatColor.WHITE
							+ " | for help with removing a region");

				}

			} else if (args[1].equalsIgnoreCase("admin")) {

				Arenas.tellPlayer(player, ChatColor.RED
						+ "---  Admin Commands  ---");

				if (args[2].equalsIgnoreCase("reload")) {

					Arenas.tellPlayer(player, ChatColor.RED + "/pa reload"
							+ ChatColor.WHITE + " | Reload all arenas");
					Arenas.tellPlayer(player,
							"Note: use this to reload PVP Arena. The /reload command is buggy!");

				} else if (args[2].equalsIgnoreCase("remove")) {

					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] remove" + ChatColor.WHITE
							+ " | Remove an arena");

				} else if (args[2].equalsIgnoreCase("disable")) {

					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] disable" + ChatColor.WHITE
							+ " | Disable usage of an arena");
					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] enable" + ChatColor.WHITE
							+ " | Enable usage of an arena");
					Arenas.tellPlayer(player,
							"Note: OPs still can access the arena for testing purposes.");

				} else if (args[2].equalsIgnoreCase("forcestop")) {

					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] forcestop" + ChatColor.WHITE
							+ " | Force an arena to stop");

				} else {

					Arenas.tellPlayer(player, ChatColor.RED
							+ "---  Admin Commands  ---");
					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa help admin reload" + ChatColor.WHITE
							+ " | reloading");
					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa help admin remove" + ChatColor.WHITE
							+ " | removing");
					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa help admin disable" + ChatColor.WHITE
							+ " | disabling");
					Arenas.tellPlayer(player, ChatColor.RED
							+ "/pa help admin forcestop" + ChatColor.WHITE
							+ " | force-stopping");

				}

			} else {
				helpList(player);
			}
		}
		return true;
	}

	/**
	 * display the help menu to a player
	 * 
	 * @param player
	 *            the player committing the command
	 */
	private static void helpList(Player player) {

		Arenas.tellPlayer(player, ChatColor.YELLOW + "/pa help general"
				+ ChatColor.WHITE + " | for general commands");
		Arenas.tellPlayer(player, ChatColor.BLUE + "/pa help info"
				+ ChatColor.WHITE + " | for information commands");
		Arenas.tellPlayer(player, ChatColor.GREEN + "/pa help setup"
				+ ChatColor.WHITE + " | for setup commands");
		Arenas.tellPlayer(player, ChatColor.RED + "/pa help admin"
				+ ChatColor.WHITE + " | for administration commands");

	}
}
