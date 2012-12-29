package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class "PlayerKillreward"</pre>
 * 
 * This will feature several ways of altering player rewards
 * 
 * The following modes exist:
 * * GEAR_UP - get better gear until you reached the final step and then win
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalPlayerKillReward extends ArenaGoal {
	public GoalPlayerKillReward() {
		super("PlayerKillReward");
		db = new Debug(102);
	}
	
	//private GameMode gm;
	/*
	private static enum GameMode {
		GEAR_UP;
	}*/
	
	private HashMap<Integer, ItemStack[]> items = new HashMap<Integer, ItemStack[]>();
	
	EndRunnable er = null;

	HashMap<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.10.2.6";
	}

	int priority = 6;
	
	@Override
	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() < priority && string.equalsIgnoreCase("killrewards") || string.equalsIgnoreCase("!kr")) {
			res.setPriority(this, priority);
		}
		return res;
	}
	
	@Override
	public PACheck checkEnd(PACheck res) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (!arena.isFreeForAll()) {
			int count = TeamManager.countActiveTeams(arena);

			if (count <= 1) {
				res.setPriority(this, priority); // yep. only one team left. go!
			}
			return res;
		}
		
		int count = lives.size();

		if (count == 1) {
			res.setPriority(this, priority); // yep. only one player left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		if (!arena.isFreeForAll()) {

			for (ArenaTeam team : arena.getTeams()) {
				String sTeam = team.getName();
				if (!list.contains(team + "spawn")) {
					boolean found = false;
					for (String s : list) {
						if (s.startsWith(sTeam) && s.endsWith("spawn")) {
							found = true;
							break;
						}
					}
					if (!found)
						return team.getName() + "spawn not set";
				}
			}
			return null;
		}
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! ("+count+"/4)";
	}
	
	@Override
	public PACheck checkJoin(CommandSender sender, PACheck res, String[] args) {
		if (res.getPriority() >= priority) {
			return res;
		}

		int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		int maxTeamPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS);
		
		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			ArenaTeam team = arena.getTeam(args[0]);
			
			if (team != null) {
			
				if (maxTeamPlayers > 0 && team.getTeamMembers().size() >= maxTeamPlayers) {
					res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
					return res;
				}
			}
		}
		
		res.setPriority(this, priority);
		return res;
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (!PAA__Command.argCountValid(sender, arena, args, new Integer[]{2,3})) {
			return;
		}
		
		// /pa [arena] !kr [number] {remove}
		
		int value = 0;
		
		try {
			value = Integer.parseInt(args[1]);
		} catch (Exception e) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[1]));
			return;
		}
		if (args.length > 2) {
			items.remove(value);
			arena.msg(sender,  Language.parse(MSG.GOAL_KILLREWARD_REMOVED, args[1]));
		} else {
			if (!(sender instanceof Player)) {
				Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
				return;
			}
			Player p = (Player) sender;
			String contents = StringParser.getStringFromItemStacks(p.getInventory().getArmorContents())+","+
			StringParser.getStringFromItemStacks(p.getInventory().getContents());
			
			items.put(value, StringParser.getItemStacksFromString(contents));
			arena.msg(sender,  Language.parse(MSG.GOAL_KILLREWARD_ADDED, args[1], contents));
			
		}
		
		saveItems();
	}

	@Override
	public void commitEnd(boolean force) {
		if (er != null) {
			return;
		}
		
		
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT))
					continue;

				if (arena.isFreeForAll()) {
					
					ArenaModuleManager.announce(arena, Language.parse(MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");
	
					arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
				} else {
					
					ArenaModuleManager.announce(arena, Language.parse(MSG.TEAM_HAS_WON, team.getColoredName()), "WINNER");
	
					arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, team.getColoredName()));
					break;
				}
			}
			
			if (ArenaModuleManager.commitEnd(arena, team)) {
				return;
			}
		}
		er = new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void parsePlayerDeath(Player player, EntityDamageEvent event) {
		if (!lives.containsKey(player.getName())) {
			return;
		}
		lives.put(player.getName(), getMaxInt());
		
		class ResetRunnable implements Runnable {
			private Player p;
			@Override
			public void run() {
				reset(p);
			}
			
			ResetRunnable(Player p) {
				this.p = p;
			}

			private void reset(Player p) {
				if (!lives.containsKey(p.getName())) {
					return;
				}
				
				int i = lives.get(p.getName());
				if (!ArenaPlayer.parsePlayer(p.getName()).getStatus().equals(Status.FIGHT)) {
					return;
				}
				InventoryManager.clearInventory(p);
				if (!items.containsKey(i)) {
					ArenaPlayer.parsePlayer(p.getName()).getArenaClass().equip(p);
				} else {
					ArenaClass.equip(p, items.get(i));
				}
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new ResetRunnable(player), 4L);
		Player killer = player.getKiller();
		
		if (killer == null) {
			return;
		}
		
		int i = lives.get(killer.getName());
		db.i("kills to go: " + i, killer);
		if (i <= 1) {
			// player has won!
			HashSet<ArenaPlayer> plrs = new HashSet<ArenaPlayer>();
			for (ArenaPlayer ap : arena.getFighters()) {
				if (ap.getName().equals(killer.getName())) {
					continue;
				}
				plrs.add(ap);
			}
			for (ArenaPlayer ap : plrs) {
				lives.remove(ap.getName());
				db.i("faking player death", ap.get());
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true, false);
				
				ap.setStatus(Status.LOST);
				ap.addLosses();
				
				PlayerState.fullReset(arena, ap.get());
			}

			if (ArenaManager.checkAndCommit(arena, false))
				return;
			
			PACheck.handleEnd(arena, false);
		} else {
			i--;
			lives.put(killer.getName(), i);
			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new ResetRunnable(killer), 4L);
		}
	}

	@Override
	public void displayInfo(CommandSender sender) {
		//sender.sendMessage("killrewards: " + arena.getArenaConfig().getInt(CFG.GOAL_PLIVES_LIVES));
	}

	@Override
	public boolean hasSpawn(String string) {
		if (arena.isFreeForAll()) {
			return (string.toLowerCase().startsWith("spawn"));
		}
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().startsWith(teamName.toLowerCase()+"spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(Player player) {
		lives.put(player.getName(), getMaxInt());
	}
	
	private int getMaxInt() {
		int max = 0;
		for (int i : items.keySet()) {
			max = Math.max(max, i);
		}
		return max;
	}

	@Override
	public boolean isInternal() {
		return true;
	}
	
	@Override
	public void parseLeave(Player player) {
		if (player == null) {
			PVPArena.instance.getLogger().warning(this.getName() + ": player NULL");
			return;
		}
		if (lives.containsKey(player.getName())) {
			lives.remove(player.getName());
		}
	}

	@Override
	public void parseStart() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), getMaxInt());
			}
		}
	}
	
	@Override
	public void reset(boolean force) {
		er = null;
		lives.clear();
		items.clear();
	}
	
	@Override
	public void setDefaults(YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		
		if (config.get("teams.free") != null) {
			config.set("teams",null);
		}
		if (config.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red",
					ChatColor.RED.name());
			config.addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
				&& (config.get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
		
		ConfigurationSection cs = (ConfigurationSection) config.get("goal.playerkillrewards");
		
		if (cs != null) {
			for (String line : cs.getKeys(false)) {
				try {
					items.put(Integer.parseInt(line.substring(2)), StringParser.getItemStacksFromString(cs.getString(line)));
				} catch (Exception e) {
					
				}
			}
		}
		
		if (items.size() < 1) {
		
			items.put(5,StringParser.getItemStacksFromString("298,299,300,301,268")); // leather
			items.put(4,StringParser.getItemStacksFromString("302,303,304,305,272")); // chain
			items.put(3,StringParser.getItemStacksFromString("314,315,316,317,267")); // gold
			items.put(2,StringParser.getItemStacksFromString("306,307,308,309,276")); // iron
			items.put(1,StringParser.getItemStacksFromString("310,311,312,313,276")); // diamond
			
			saveItems();
		}
	}
	
	private void saveItems() {
		for (int i : items.keySet())
			arena.getArenaConfig().setManually("goal.playerkillrewards.kr"+i, StringParser.getStringFromItemStacks(items.get(i)));
		
		arena.getArenaConfig().save();
	}
	
	@Override
	public void unload(Player player) {
		lives.remove(player.getName());
	}
}
