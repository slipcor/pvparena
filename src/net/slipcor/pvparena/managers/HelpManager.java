/*
 * help manager class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.4.0 - mayor rewrite, improved help
 */

package net.slipcor.pvparena.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpManager {

	public static boolean parseCommand(Player player, String[] args) {
		ArenaManager.tellPlayer(player, ChatColor.AQUA
				+ "---  PVP Arena Help  ---");

		if (args.length == 1) {

			helpList(player);

		} else if (args.length == 2) {

			if (args[1].equalsIgnoreCase("general")) {

				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "--- General Commands ---");
				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general join" + ChatColor.WHITE
						+ " | for help with joining");
				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general watch" + ChatColor.WHITE
						+ " | for help with watching");
				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general bet" + ChatColor.WHITE
						+ " | for help with bets");
				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "/pa help general leave" + ChatColor.WHITE
						+ " | for help with leaving");

			} else if (args[1].equalsIgnoreCase("info")) {

				ArenaManager.tellPlayer(player, ChatColor.BLUE
						+ "---  Info  Commands  ---");
				ArenaManager.tellPlayer(player, ChatColor.BLUE
						+ "/pa help info list" + ChatColor.WHITE
						+ " | for information commands");
				ArenaManager.tellPlayer(player, ChatColor.BLUE
						+ "/pa help info stats" + ChatColor.WHITE
						+ " | for information commands");

			} else if (args[1].equalsIgnoreCase("setup")) {

				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "---  Setup Commands  ---");
				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "/pa help info setup create" + ChatColor.WHITE
						+ " | for help with spawns");
				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "/pa help info setup spawn" + ChatColor.WHITE
						+ " | for help with spawns");
				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "/pa help info setup regionset" + ChatColor.WHITE
						+ " | for help with setting a region");
				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "/pa help info setup regionsave" + ChatColor.WHITE
						+ " | for help with saving a region");
				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "/pa help info setup regionremove" + ChatColor.WHITE
						+ " | for help with removing a region");

			} else if (args[1].equalsIgnoreCase("admin")) {

				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "---  Admin Commands  ---");
				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "/pa help info admin reload" + ChatColor.WHITE
						+ " | for help with reloading");
				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "/pa help info admin remove" + ChatColor.WHITE
						+ " | for help with removing");
				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "/pa help info admin disable" + ChatColor.WHITE
						+ " | for help with disabling");
				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "/pa help info admin forcestop" + ChatColor.WHITE
						+ " | for help with force-stopping");

			} else {

				helpList(player);

			}

		} else if (args.length == 3) {

			if (args[1].equalsIgnoreCase("general")) {

				ArenaManager.tellPlayer(player, ChatColor.YELLOW
						+ "--- General Commands ---");

				if (args[2].equalsIgnoreCase("join")) {

					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname]" + ChatColor.WHITE
							+ " | Join the arena, random chooses team");
					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname] [teamname]" + ChatColor.WHITE
							+ " | Join the arena team");

				} else if (args[2].equalsIgnoreCase("watch")) {

					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa [arenaname] watch" + ChatColor.WHITE
							+ " | Watch a fight");

				} else if (args[2].equalsIgnoreCase("bet")) {

					ArenaManager
							.tellPlayer(
									player,
									ChatColor.YELLOW
											+ "/pa [arenaname] bet [(player/team)name] [amount]"
											+ ChatColor.WHITE
											+ " | Bet on a player/team");
					ArenaManager
							.tellPlayer(player,
									"Note: You get 4x money for betting on a the right player, ");
					ArenaManager.tellPlayer(player,
							"      2x the money for betting on the right team");

				} else if (args[2].equalsIgnoreCase("leave")) {

					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa leave" + ChatColor.WHITE
							+ " | Leave the arena you are in");

				} else {

					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general join" + ChatColor.WHITE
							+ " | for help with joining");
					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general watch" + ChatColor.WHITE
							+ " | for help with watching");
					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general bet" + ChatColor.WHITE
							+ " | for help with bets");
					ArenaManager.tellPlayer(player, ChatColor.YELLOW
							+ "/pa help general leave" + ChatColor.WHITE
							+ " | for help with leaving");

				}

			} else if (args[1].equalsIgnoreCase("info")) {

				ArenaManager.tellPlayer(player, ChatColor.BLUE
						+ "---  Info  Commands  ---");

				if (args[2].equalsIgnoreCase("list")) {

					ArenaManager.tellPlayer(player, ChatColor.BLUE + "/pa list"
							+ ChatColor.WHITE + " | List all arenas");
					ArenaManager.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] list" + ChatColor.WHITE
							+ " | List all players in the arena");

				} else if (args[2].equalsIgnoreCase("stats")) {

					ArenaManager.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] teams" + ChatColor.WHITE
							+ " | Get teams statistics");
					ArenaManager.tellPlayer(player, ChatColor.BLUE
							+ "/pa [arenaname] users" + ChatColor.WHITE
							+ " | Get users statistics");

				} else {

					ArenaManager.tellPlayer(player, ChatColor.BLUE
							+ "/pa help info list" + ChatColor.WHITE
							+ " | for information commands");
					ArenaManager.tellPlayer(player, ChatColor.BLUE
							+ "/pa help info stats" + ChatColor.WHITE
							+ " | for information commands");

				}

			} else if (args[1].equalsIgnoreCase("setup")) {

				ArenaManager.tellPlayer(player, ChatColor.GREEN
						+ "---  Setup Commands  ---");

				if (args[2].equalsIgnoreCase("create")) {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] create [type]" + ChatColor.WHITE
							+ " | Create an arena");
					ArenaManager
							.tellPlayer(player,
									"Note: valid types are: team, ctf, free ; all other types fall back to team");

				} else if (args[2].equalsIgnoreCase("spawn")) {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] [spawn]" + ChatColor.WHITE
							+ " | Set the spawn");
					ArenaManager.tellPlayer(player, "Valid spawns are:");
					ArenaManager.tellPlayer(player,
							"All Arena types: spectator, exit");
					ArenaManager.tellPlayer(player,
							"CTF: [teamname]lounge, [teamname]spawn");
					ArenaManager
							.tellPlayer(player,
									"Team: [teamname]lounge, [teamname]spawn (if randomSpawn is not set)");
					ArenaManager
							.tellPlayer(player,
									"Free: lounge, spawn[x] (or for Team if randomSpawn is set)");
					ArenaManager.tellPlayer(player,
							"Note: [x] can be any letter or digit.");

				} else if (args[2].equalsIgnoreCase("regionset")) {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region" + ChatColor.WHITE
							+ " | Start setting a region");

				} else if (args[2].equalsIgnoreCase("regionsave")) {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region [regionname]"
							+ ChatColor.WHITE + " | Save selection to region");
					ArenaManager.tellPlayer(player, "Valid region names are:");
					ArenaManager.tellPlayer(player,
							"All Arena types: spectator, exit");
					ArenaManager.tellPlayer(player,
							"CTF, Team: [teamname]lounge");
					ArenaManager.tellPlayer(player, "Free: freelounge");

				} else if (args[2].equalsIgnoreCase("regionremove")) {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa [arenaname] region remove [regionname]"
							+ ChatColor.WHITE + " | Remove a region");
					ArenaManager.tellPlayer(player, "Valid region names are:");
					ArenaManager.tellPlayer(player,
							"All Arena types: spectator, exit");
					ArenaManager.tellPlayer(player,
							"CTF, Team: [teamname]lounge");
					ArenaManager.tellPlayer(player, "Free: freelounge");

				} else {

					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa help info setup create" + ChatColor.WHITE
							+ " | for help with spawns");
					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa help info setup spawn" + ChatColor.WHITE
							+ " | for help with spawns");
					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa help info setup regionset" + ChatColor.WHITE
							+ " | for help with setting a region");
					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa help info setup regionsave"
							+ ChatColor.WHITE
							+ " | for help with saving a region");
					ArenaManager.tellPlayer(player, ChatColor.GREEN
							+ "/pa help info setup regionremove"
							+ ChatColor.WHITE
							+ " | for help with removing a region");

				}

			} else if (args[1].equalsIgnoreCase("admin")) {

				ArenaManager.tellPlayer(player, ChatColor.RED
						+ "---  Admin Commands  ---");

				if (args[2].equalsIgnoreCase("reload")) {

					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa reload" + ChatColor.WHITE
							+ " | Reload all arenas");
					ArenaManager
							.tellPlayer(player,
									"Note: use this to reload PVP Arena. The /reload command is buggy!");

				} else if (args[2].equalsIgnoreCase("remove")) {

					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] remove" + ChatColor.WHITE
							+ " | Remove an arena");

				} else if (args[2].equalsIgnoreCase("disable")) {

					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] disable" + ChatColor.WHITE
							+ " | Disable usage of an arena");
					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] enable" + ChatColor.WHITE
							+ " | Enable usage of an arena");
					ArenaManager
							.tellPlayer(player,
									"Note: OPs still can access the arena for testing purposes.");

				} else if (args[2].equalsIgnoreCase("forcestop")) {

					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa [arenaname] forcestop" + ChatColor.WHITE
							+ " | Force an arena to stop");

				} else {

					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa help info admin reload" + ChatColor.WHITE
							+ " | for help with reloading");
					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa help info admin remove" + ChatColor.WHITE
							+ " | for help with removing");
					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa help info admin disable" + ChatColor.WHITE
							+ " | for help with disabling");
					ArenaManager.tellPlayer(player, ChatColor.RED
							+ "/pa help info admin forcestop" + ChatColor.WHITE
							+ " | for help with force-stopping");

				}

			} else {

				helpList(player);

			}
		}
		return false;
	}

	private static void helpList(Player player) {

		ArenaManager.tellPlayer(player, ChatColor.YELLOW + "/pa help general"
				+ ChatColor.WHITE + " | for general commands");
		ArenaManager.tellPlayer(player, ChatColor.BLUE + "/pa help info"
				+ ChatColor.WHITE + " | for information commands");
		ArenaManager.tellPlayer(player, ChatColor.GREEN + "/pa help setup"
				+ ChatColor.WHITE + " | for setup commands");
		ArenaManager.tellPlayer(player, ChatColor.RED + "/pa help admin"
				+ ChatColor.WHITE + " | for administration commands");

	}
}
