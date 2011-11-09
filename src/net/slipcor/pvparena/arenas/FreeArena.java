package net.slipcor.pvparena.arenas;

/*
 * Free Fight Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.6 - CTF Arena
 * 
 * history:
 *
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class FreeArena extends Arena{
	public FreeArena(String sName, PVPArenaPlugin plugin) {
		super();

		this.name = sName;
		this.configFile = new File("plugins/pvparena/config.free_" + name + ".yml");
		
		new File("plugins/pvparena").mkdir();
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArenaPlugin.lang.log_error("filecreateerror","config.free_" + name);
			}

		parseConfig("free");
		
		this.teamkilling = true;
		this.manuallyselectteams = false;
		this.woolhead = false;
		this.forceeven = false;
		this.randomSpawn = true;
		fightTeams.clear();
		fightTeams.put("free", ChatColor.WHITE.name());
	}

	@Override
	public String isSetup() {
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("coords") == null) {
			return "coords";
		}

		List<String> list = config.getKeys("coords");
		if (!list.contains("spectator"))
			return "spectator";
		if (!list.contains("lounge"))
			return "lounge";
		if (!list.contains("exit"))
			return "exit";
		Iterator<String> iter = list.iterator();
		int spawns = 0;
		while (iter.hasNext()) {
			String s = iter.next();
			if (s.startsWith("spawn"))
				spawns++;
		}
		if (spawns > 3) {
			return null;
		}

		return "spawns";
	}
	
	@Override
	public boolean checkEnd() {
		if (fightUsersTeam.size() > 1) {
			return false;
		}
		
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			StatsManager.addWinStat(z, "free", this);
			loadPlayer(z, sTPwin);
			giveRewards(z); // if we are the winning team, give reward!
			fightUsersClass.remove(z.getName());
		}
		reset();
		return true;
	}
	
	@Override
	public void chooseColor(Player player) {
		if (!(fightUsersTeam.containsKey(player.getName()))) {
			goToWaypoint(player, "lounge");
			fightUsersTeam.put(player.getName(), "free");
			Arena.tellPlayer(player, PVPArenaPlugin.lang.parse("youjoinedfree"));
			tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoinedfree", player.getName()));

		} else {
			Arena.tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
		}
	}

	@Override
	public boolean checkSpawnCommand(String[] args, Player player) {
		if (args[0].equalsIgnoreCase("lounge")) {
			setCoords(player, "lounge");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setlounge"));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean checkLoungeCommand(String[] args, Player player) {
		if (args[0].equalsIgnoreCase("lounge")) {
			setCoords(player, "lounge");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setlounge"));
			return true;
		}
		return false;
	}
}
