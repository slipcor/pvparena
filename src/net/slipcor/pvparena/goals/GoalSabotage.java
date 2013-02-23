package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Map;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
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
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "Sabotage"
 * </pre>
 * 
 * The first advanced Arena Goal. Sneak into an other team's base and ignite
 * their TNT.
 * 
 * @author slipcor
 */

public class GoalSabotage extends ArenaGoal implements Listener {

	public GoalSabotage() {
		super("Sabotage");
		debug = new Debug(103);
	}

	private String flagName = "";
	private Map<String, String> teamFlags = null;
	private Map<ArenaTeam, TNTPrimed> teamTNTs = null;

	@Override
	public String version() {
		return "v1.0.1.59";
	}
	
	private static final int PRIORITY = 7;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(final PACheck res, final String string) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (string.contains(sTeam + "tnt")) {
				res.setPriority(this, PRIORITY);
			}
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		String team = this.checkForMissingTeamSpawn(list);
		if (team != null) {
			return team;
		}
		return this.checkForMissingTeamCustom(list,"tnt");
	}

	/**
	 * hook into an interacting player
	 * 
	 * @param res
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return
	 */
	@Override
	public PACheck checkInteract(final PACheck res, final Player player, final Block block) {
		if (block == null || res.getPriority() > PRIORITY) {
			return res;
		}
		debug.i("checking interact", player);

		if (!block.getType().equals(Material.TNT)) {
			debug.i("block, but not flag", player);
			return res;
		}
		debug.i("flag click!", player);

		if (player.getItemInHand() == null
				|| !player.getItemInHand().getType()
						.equals(Material.FLINT_AND_STEEL)) {
			debug.i("block, but no sabotage items", player);
			return res;
		}

		Vector vLoc;
		Vector vFlag = null;
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		final ArenaTeam pTeam = aPlayer.getArenaTeam();
		if (pTeam == null) {
			return res;
		}
		for (ArenaTeam team : arena.getTeams()) {
			final String aTeam = team.getName();

			if (aTeam.equals(pTeam.getName())) {
				continue;
			}
			if (team.getTeamMembers().size() < 1) {
				continue; // dont check for inactive teams
			}
			debug.i("checking for tnt of team " + aTeam, player);
			vLoc = block.getLocation().toVector();
			debug.i("block: " + vLoc.toString(), player);
			if (SpawnManager.getBlocks(arena, aTeam + "tnt").size() > 0) {
				vFlag = SpawnManager
						.getBlockNearest(
								SpawnManager.getBlocks(arena, aTeam + "tnt"),
								new PABlockLocation(player.getLocation()))
						.toLocation().toVector();
			}

			if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
				debug.i("flag found!", player);
				debug.i("vFlag: " + vFlag.toString(), player);
				arena.broadcast(Language.parse(MSG.GOAL_SABOTAGE_IGNITED,
						pTeam.colorizePlayer(player) + ChatColor.YELLOW,
						team.getColoredName() + ChatColor.YELLOW));

				takeFlag(team.getName(), true,
						new PALocation(block.getLocation()));
				res.setPriority(this, PRIORITY);
				return res;
			}
		}

		return res;
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
		if (res.getPriority() >= PRIORITY) {
			return res;
		}

		final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		final int maxTeamPlayers = arena.getArenaConfig().getInt(
				CFG.READY_MAXTEAMPLAYERS);

		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			final ArenaTeam team = arena.getTeam(args[0]);

			if (team != null && maxTeamPlayers > 0
						&& team.getTeamMembers().size() >= maxTeamPlayers) {
				res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
				return res;
			}
		}

		res.setPriority(this, PRIORITY);
		return res;
	}

	@Override
	public PACheck checkSetBlock(final PACheck res, Player player, final Block block) {

		if (res.getPriority() > PRIORITY
				|| !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(this, PRIORITY); // success :)

		return res;
	}

	private void commit(final Arena arena, final String sTeam, final boolean win) {
		debug.i("[SABOTAGE] committing end: " + sTeam);
		debug.i("win: " + win);

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

			ArenaModuleManager
					.announce(
							arena,
							Language.parse(MSG.TEAM_HAS_WON,
									arena.getTeam(winteam).getColor() + "Team "
											+ winteam + ChatColor.YELLOW),
							"WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team " + winteam
							+ ChatColor.YELLOW));
		}

		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(final CommandSender sender, final String[] args) {
		if (args[0].contains("tnt")) {
			for (ArenaTeam team : arena.getTeams()) {
				final String sTeam = team.getName();
				if (args[0].contains(sTeam + "tnt")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);

					arena.msg(sender, Language.parse(
							MSG.GOAL_SABOTAGE_TOSETTNT, flagName));
				}
			}
		}
	}

	@Override
	public void commitEnd(final boolean force) {
		debug.i("[SABOTAGE]");

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

			ArenaModuleManager.announce(
					arena,
					Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName() + ChatColor.YELLOW));
		}

		if (ArenaModuleManager.commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitInteract(final Player player, final Block clickedBlock) {
	}

	@Override
	public boolean commitSetFlag(final Player player, final Block block) {
		if (block == null || !block.getType().equals(Material.TNT)) {
			return false;
		}

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		debug.i("trying to set a tnt", player);

		// command : /pa redtnt1
		// location: red1tnt:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()),
				flagName);

		arena.msg(player, Language.parse(MSG.GOAL_SABOTAGE_SETTNT, flagName));

		PAA_Region.activeSelections.remove(player.getName());
		this.flagName = "";
		return false;
	}

	@Override
	public void configParse(final YamlConfiguration cfg) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}

	@Override
	public void disconnect(final ArenaPlayer aPlayer) {

		final String flag = this.getHeldFlagTeam(aPlayer.getName());
		if (flag != null) {
			final ArenaTeam flagTeam = arena.getTeam(flag);
			getFlagMap().remove(flag);
			distributeFlag(aPlayer, flagTeam);
		}
	}

	private void distributeFlag(final ArenaPlayer player, final ArenaTeam team) {
		final Set<ArenaPlayer> players = team.getTeamMembers();

		int pos = new Random().nextInt(players.size());

		for (ArenaPlayer ap : players) {
			debug.i("distributing sabotage: " + ap.getName(), ap.getName());
			if (ap.equals(player)) {
				continue;
			}
			if (--pos <= 1) {
				getFlagMap().put(team.getName(), ap.getName());
				ap.get().getInventory()
						.addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
				arena.msg(ap.get(), Language.parse(MSG.GOAL_SABOTAGE_YOUTNT));
				return;
			}
		}
	}

	private String getHeldFlagTeam(final String player) {
		if (getFlagMap().size() < 1) {
			return null;
		}

		debug.i("getting held TNT of player " + player, player);
		for (String sTeam : getFlagMap().keySet()) {
			debug.i("team " + sTeam + "'s sabotage is carried by "
					+ getFlagMap().get(sTeam) + "s hands", player);
			if (player.equals(getFlagMap().get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}
	
	private Map<String, String> getFlagMap() {
		if (teamFlags == null) {
			teamFlags = new HashMap<String, String>();
		}
		return teamFlags;
	}
	
	private Map<ArenaTeam, TNTPrimed> getTNTmap() {
		if (teamTNTs == null) {
			teamTNTs = new HashMap<ArenaTeam, TNTPrimed>();
		}
		return teamTNTs;
	}

	@Override
	public String guessSpawn(final String place) {
		// no exact match: assume we have multiple spawnpoints
		final Map<Integer, String> locs = new HashMap<Integer, String>();
		int pos = 0;

		debug.i("searching for team spawns: " + place);

		final Map<String, Object> coords = (HashMap<String, Object>) arena
				.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(pos++, name);
				debug.i("found match: " + name);
			}
			if (name.endsWith("tnt")) {
				for (ArenaTeam team : arena.getTeams()) {
					final String sTeam = team.getName();
					if (name.startsWith(sTeam) && place.startsWith(sTeam)) {
						locs.put(pos++, name);
						debug.i("found match: " + name);
					}
				}
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		final Random random = new Random();
		
		return locs.get(random.nextInt(locs.size()));
	}

	@Override
	public boolean hasSpawn(final String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().equals(teamName.toLowerCase() + "tnt")) {
				return true;
			}
			if (string.toLowerCase().startsWith(
					teamName.toLowerCase() + "spawn")) {
				return true;
			}
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
				for (ArenaClass aClass : arena.getClasses()) {
					if (string.toLowerCase().startsWith(teamName.toLowerCase() + 
							aClass.getName() + "spawn")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void initate(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();
		takeFlag(team.getName(), false,
				SpawnManager.getCoords(arena, team.getName() + "tnt"));
		if (!getFlagMap().containsKey(team.getName())) {
			debug.i("adding team " + team.getName(), player);
			distributeFlag(null, team);
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parsePlayerDeath(final Player player, final EntityDamageEvent event) {
		final String teamName = getHeldFlagTeam(player.getName());
		final ArenaTeam team = arena.getTeam(teamName);
		if (teamName != null && team != null) {
			final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
			getFlagMap().remove(teamName);
			distributeFlag(aPlayer, team);
		}
	}

	@Override
	public void parseStart() {
		debug.i("initiating arena");
		getFlagMap().clear();
		for (ArenaTeam team : arena.getTeams()) {
			takeFlag(team.getName(), false,
					SpawnManager.getCoords(arena, team.getName() + "tnt"));
			if (!getFlagMap().containsKey(team.getName())) {
				debug.i("adding team " + team.getName());
				distributeFlag(null, team);
			}
		}
	}

	@Override
	public void reset(final boolean force) {
		getFlagMap().clear();
		for (TNTPrimed t : getTNTmap().values()) {
			t.remove();
		}
		getTNTmap().clear();
	}

	@Override
	public void setDefaults(final YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		if (config.get("teams.free") != null) {
			config.set("teams", null);
		}
		if (config.get("teams") == null) {
			debug.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red", ChatColor.RED.name());
			config.addDefault("teams.blue", ChatColor.BLUE.name());
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
	public void takeFlag(final String teamName, final boolean take, final PALocation lBlock) {
		lBlock.toLocation().getBlock()
				.setType(take ? Material.AIR : Material.TNT);
		if (take) {
			TNTPrimed tnt = (TNTPrimed) Bukkit.getWorld(
					SpawnManager.getRegionCenter(arena).getWorldName())
					.spawnEntity(lBlock.toLocation(), EntityType.PRIMED_TNT);
			getTNTmap().put(arena.getTeam(teamName), tnt);
		}
	}

	@Override
	public void unload(final Player player) {
		disconnect(ArenaPlayer.parsePlayer(player.getName()));
	}

	@EventHandler
	public void onTNTExplode(final EntityExplodeEvent event) {
		if (!event.getEntityType().equals(EntityType.PRIMED_TNT)) {
			return;
		}

		final TNTPrimed tnt = (TNTPrimed) event.getEntity();

		for (ArenaTeam team : getTNTmap().keySet()) {
			if (tnt.getUniqueId().equals(getTNTmap().get(team).getUniqueId())) {
				event.setCancelled(true);
				tnt.remove();
				commit(arena, team.getName(), false);
			}
		}

		final PABlockLocation tLoc = new PABlockLocation(event.getEntity()
				.getLocation());

		final Set<PABlockLocation> locs = SpawnManager.getBlocks(arena, "tnt");

		final PABlockLocation nearest = SpawnManager.getBlockNearest(locs, tLoc);

		if (nearest.getDistance(tLoc) < 2) {
			event.setCancelled(true);
		}
	}
}
