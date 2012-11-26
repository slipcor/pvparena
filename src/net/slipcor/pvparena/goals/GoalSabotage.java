package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "Sabotage"</pre>
 * 
 * The first advanced Arena Goal. Sneak into an other team's base and ignite
 * their TNT.
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class GoalSabotage extends ArenaGoal implements Listener {

	public GoalSabotage(Arena arena) {
		super(arena, "Sabotage");
		db = new Debug(103);
	}
	
	private String flagName = "";
	private HashMap<String, String> paTeamFlags = new HashMap<String, String>();
	private HashMap<ArenaTeam, TNTPrimed> tnts = new HashMap<ArenaTeam, TNTPrimed>();
	
	@Override
	public String version() {
		return "v0.9.8.15";
	}

	int priority = 7;
	int killpriority = 1;
	
	@Override
	public GoalSabotage clone() {
		return new GoalSabotage(arena);
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (string.contains(sTeam + "tnt")) {
				res.setPriority(this, priority);
			}
		}
		
		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (!list.contains(sTeam + "tnt")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("tnt")) {
						found = true;
						break;
					}
				}
				if (!found)
					return team.getName() + "tnt not set";
			}
		}
		return null;
	}

	/**
	 * hook into an interacting player
	 * @param res 
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return 
	 */
	@Override
	public PACheck checkInteract(PACheck res, Player player, Block block) {
		if (block == null || res.getPriority() > priority) {
			return res;
		}
		db.i("checking interact");

		if (!block.getType().equals(Material.TNT)) {
			db.i("block, but not flag");
			return res;
		}
		db.i("flag click!");
		
		if (player.getItemInHand() == null || !player.getItemInHand().getType().equals(Material.FLINT_AND_STEEL)) {
			db.i("block, but no sabotage items");
			return res;
		}

		Vector vLoc;
		Vector vFlag = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		
		ArenaTeam pTeam = ap.getArenaTeam();
		if (pTeam == null) {
			return res;
		}
		for (ArenaTeam team : arena.getTeams()) {
			String aTeam = team.getName();

			if (aTeam.equals(pTeam.getName()))
				continue;
			if (team.getTeamMembers().size() < 1)
				continue; // dont check for inactive teams
			
			db.i("checking for tnt of team " + aTeam);
			vLoc = block.getLocation().toVector();
			db.i("block: " + vLoc.toString());
			if (SpawnManager.getBlocks(arena, aTeam + "tnt").size() > 0) {
				vFlag = SpawnManager.getBlockNearest(
						SpawnManager.getBlocks(arena, aTeam + "tnt"),
						new PABlockLocation(player.getLocation())).toLocation().toVector();
			}
			
			if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
				db.i("flag found!");
				db.i("vFlag: " + vFlag.toString());
				arena.broadcast(Language.parse(MSG.GOAL_SABOTAGE_IGNITED,
						pTeam.colorizePlayer(player) + ChatColor.YELLOW,
						team.getColoredName() + ChatColor.YELLOW));
				
				takeFlag(team.getName(), true, new PALocation(block.getLocation()));
				return res; 
			}
		}
		
		return res;
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
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		if (res.getPriority() <= killpriority) {
			res.setPriority(this, killpriority);
		}
		return res;
	}
	
	@Override
	public PACheck checkSetFlag(PACheck res, Player player, Block block) {

		if (res.getPriority() > priority || !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(this, priority); // success :)
		
		return res;
	}

	private void commit(Arena arena, String sTeam, boolean win) {
		db.i("[SABOTAGE] committing end: " + sTeam);
		db.i("win: " + String.valueOf(win));

		String winteam = sTeam;

		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals(sTeam) == win) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {

				ap.addStatistic(arena.getName(), type.LOSSES, 1);
				arena.tpPlayerToCoordName(ap.get(), "spectator");
				ap.setTelePass(false);
			}
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				winteam = team.getName();
				break;
			}
		}

		if (arena.getTeam(winteam) != null) {
			
			PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team "
							+ winteam + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team "
							+ winteam + ChatColor.YELLOW));
		}
		
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (args[0].contains("tnt")) {
			for (ArenaTeam team : arena.getTeams()) {
				String sTeam = team.getName();
				if (args[0].contains(sTeam + "tnt")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);

					arena.msg(sender, Language.parse(MSG.GOAL_SABOTAGE_TOSETTNT, flagName));
				}
			}
		}
	}

	@Override
	public void commitEnd(boolean force) {
		db.i("[SABOTAGE]");

		ArenaTeam aTeam = null;
		
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null && !force) {
			PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player respawnPlayer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		
		
		ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(respawnPlayer.getName()).getArenaTeam();
		
		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller())));
	
		new InventoryRefillRunnable(arena, respawnPlayer, event.getDrops());
		
		if (arena.isCustomClassAlive()
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			InventoryManager.drop(respawnPlayer);
			event.getDrops().clear();
		}

		SpawnManager.distribute(arena,  ArenaPlayer.parsePlayer(respawnPlayer.getName()));
		
		arena.unKillPlayer(respawnPlayer, event.getEntity()
				.getLastDamageCause().getCause(), respawnPlayer.getKiller());
	}
	
	@Override
	public boolean commitSetFlag(Player player, Block block) {
		if (block == null || !block.getType().equals(Material.TNT)) {
			return false;
		}
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		db.i("trying to set a tnt");

		// command : /pa redtnt1
		// location: red1tnt:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()), flagName);

		arena.msg(player, Language.parse(MSG.GOAL_SABOTAGE_SETTNT, flagName));

		PAA_Region.activeSelections.remove(player.getName());
		this.flagName = "";
		return false;
	}
	
	@Override
	public void configParse(YamlConfiguration cfg) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}
	
	@Override
	public void disconnect(ArenaPlayer ap) {
		if (paTeamFlags == null) {
			return;
		}
		
		String flag = this.getHeldFlagTeam(arena, ap.getName());
		if (flag != null) {
			ArenaTeam flagTeam = arena.getTeam(flag);
			paTeamFlags.remove(flag);
			distributeFlag(ap, flagTeam);
		}
	}

	private void distributeFlag(ArenaPlayer player, ArenaTeam team) {
		HashSet<ArenaPlayer> players = team.getTeamMembers();
		
		int i = (new Random()).nextInt(players.size());
		
		for (ArenaPlayer ap : players) {
			db.i("distributing sabotage: " + ap.getName());
			if (ap.equals(player)) {
				continue;
			}
			if (--i <= 1) {
				paTeamFlags.put(team.getName(), ap.getName());
				ap.get().getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
				arena.msg(ap.get(), Language.parse(MSG.GOAL_SABOTAGE_YOUTNT));
				return;
			}
		}
	}
	
	private String getHeldFlagTeam(Arena arena, String player) {
		if (paTeamFlags.size() < 1) {
			return null;
		}
		
		db.i("getting held TNT of player " + player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + "'s sabotage is carried by " + paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}
	
	@Override
	public String guessSpawn(String place) {
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns: " + place);
		
		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
			if (name.endsWith("tnt")) {
				for (ArenaTeam team : arena.getTeams()) {
					String sTeam = team.getName();
					if (name.startsWith(sTeam) && place.startsWith(sTeam)) {
						locs.put(i++, name);
						db.i("found match: " + name);
					}
				}
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	@Override
	public boolean hasSpawn(String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().equals(teamName.toLowerCase()+"tnt")) {
				return true;
			}
			if (string.toLowerCase().startsWith(teamName.toLowerCase()+"spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(Player player) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();
		takeFlag(team.getName(), false,
				SpawnManager.getCoords(arena, team.getName() + "tnt"));
		if (!paTeamFlags.containsKey(team.getName())) {
			db.i("adding team " + team.getName());
			distributeFlag(null, team);
		}
	}
	
	@Override
	public void parsePlayerDeath(Player p, EntityDamageEvent event) {
		String s = getHeldFlagTeam(arena, p.getName());
		ArenaTeam t = arena.getTeam(s);
		if (s != null && t != null) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
			paTeamFlags.remove(s);
			distributeFlag(ap, t);
		}
	}

	@Override
	public void parseStart() {
		db.i("initiating arena");
		paTeamFlags.clear();
		for (ArenaTeam team : arena.getTeams()) {
			takeFlag(team.getName(), false,
					SpawnManager.getCoords(arena, team.getName() + "tnt"));
			if (!paTeamFlags.containsKey(team.getName())) {
				db.i("adding team " + team.getName());
				distributeFlag(null, team);
			}
		}
	}
	
	@Override
	public void reset(boolean force) {
		paTeamFlags.clear();
		for (TNTPrimed t : tnts.values()) {
			t.remove();
		}
		tnts.clear();
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
	public void takeFlag(String teamName, boolean take, PALocation lBlock) {
		lBlock.toLocation().getBlock().setType(take?Material.AIR:Material.TNT);
		if (take) {
			TNTPrimed tnt = (TNTPrimed) Bukkit.getWorld(SpawnManager.getRegionCenter(arena).getWorldName()).spawnEntity(lBlock.toLocation(), EntityType.PRIMED_TNT);
			tnts.put(arena.getTeam(teamName), tnt);
		}
	}
	
	@Override
	public void unload(Player player) {
		disconnect(ArenaPlayer.parsePlayer(player.getName()));
	}
	
	@EventHandler
	public void onTNTExplode(EntityExplodeEvent event) {
		if (!event.getEntityType().equals(EntityType.PRIMED_TNT)) {
			return;
		}
		
		TNTPrimed t = (TNTPrimed) event.getEntity();
		
		for (ArenaTeam team : tnts.keySet()) {
			if (t.getUniqueId().equals(tnts.get(team).getUniqueId())) {
				event.setCancelled(true);
				t.remove();
				commit(arena, team.getName(), false);
			}
		}
	}
	
	@EventHandler
	public void onTNTIgnite(ExplosionPrimeEvent event) {
		if (!event.getEntityType().equals(EntityType.PRIMED_TNT)) {
			return;
		}
		
		//TNTPrimed t = (TNTPrimed) event.getEntity();
		PABlockLocation tLoc = new PABlockLocation(event.getEntity().getLocation());
		
		HashSet<PABlockLocation> locs = SpawnManager.getBlocks(arena, "tnt");
		
		PABlockLocation nearest = SpawnManager.getBlockNearest(locs, tLoc);
		
		if (nearest.getDistance(tLoc) < 2) {
			event.setCancelled(true);
		}
	}
}
