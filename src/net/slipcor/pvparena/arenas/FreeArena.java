package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/*
 * Free Fight Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.9 - Permissions, rewrite
 * 
 * history:
 *
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.7 - Bugfixes
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public class FreeArena extends Arena{
	
	/*
	 * freefight constructor
	 * 
	 * - open or create a new configuration file
	 * - parse the arena config
	 */
	public FreeArena(String sName, PVPArena plugin) {
		super();

		this.name = sName;
		this.configFile = new File("plugins/pvparena/config.free_" + name + ".yml");
		
		new File("plugins/pvparena").mkdir();
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","config.free_" + name);
			}

		configParse("free");
		
		this.teamKilling = true;
		this.manuallySelectTeams = false;
		this.forceWoolHead = false;
		this.forceEven = false;
		this.randomSpawn = true;
		paTeams.clear();
		paTeams.put("free", ChatColor.WHITE.name());
	}

	/*
	 * setup check
	 * 
	 * returns null if setup correct
	 * returns string if not
	 * 
	 * FREEFIGHT: check for "lounge" and >3 spawns
	 */
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
	
	/*
	 * return "only one player/team alive"
	 * 
	 * - if only one player/team is alive:
	 *   - announce winning player
	 *   - teleport everyone out
	 *   - give rewards
	 *   - check for bets won
	 */
	@Override
	public boolean checkEndAndCommit() {
		if (paPlayersTeam.size() > 1) {
			return false;
		}
		
		Set<String> set = paPlayersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			
			tellEveryone(PVPArena.lang.parse("playerhaswon",ChatColor.WHITE + o.toString()));
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			StatsManager.addWinStat(z, "free", this);
			resetPlayer(z, sTPwin);
			giveRewards(z); // if we are the winning team, give reward!
			paPlayersClass.remove(z.getName());
		}
		reset();
		return true;
	}
	
	/*
	 * stick a player into the standard team
	 */
	@Override
	public void chooseColor(Player player) {
		if (!(paPlayersTeam.containsKey(player.getName()))) {
			tpPlayerToCoordName(player, "lounge");
			paPlayersTeam.put(player.getName(), "free");
			Arena.tellPlayer(player, PVPArena.lang.parse("youjoinedfree"));
			tellEveryoneExcept(player, PVPArena.lang.parse("playerjoinedfree", player.getName()));

		} else {
			Arena.tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
		}
	}

	/*
	 * returns "is spawn-set command"
	 */
	@Override
	public boolean isSpawnCommand(String[] args, Player player) {
		if (args[0].equalsIgnoreCase("lounge")) {
			setCoords(player, "lounge");
			tellPlayer(player, PVPArena.lang.parse("setlounge"));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isLoungeCommand(String[] args, Player player) {
		if (args[0].equalsIgnoreCase("lounge")) {
			setCoords(player, "lounge");
			tellPlayer(player, PVPArena.lang.parse("setlounge"));
			return true;
		}
		return false;
	}
}
