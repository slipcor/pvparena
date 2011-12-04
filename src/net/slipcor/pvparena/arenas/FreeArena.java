package net.slipcor.pvparena.arenas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/*
 * Free Fight Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.14 - timed arena modes
 * 
 * history:
 *
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
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
	public FreeArena(String sName, PVPArena p) {
		super(p);

		this.name = sName;
		this.plugin = p;
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
		this.randomlySelectTeams = true;
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
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		if (config.get("coords") == null) {
			return "no coords set";
		}

		Set<String> list = config.getConfigurationSection("coords").getValues(false).keySet();
		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("lounge"))
			return "lounge not set";
		if (!list.contains("exit"))
			return "exit not set";
		Iterator<?> iter = list.iterator();
		int spawns = 0;
		while (iter.hasNext()) {
			String s = (String) iter.next();
			if (s.startsWith("spawn"))
				spawns++;
		}
		if (spawns > 3) {
			return null;
		}

		return "not enough spawns (" + spawns + ")";
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
