package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "Flags"
 * </pre>
 * 
 * Well, should be clear. Capture flags, bring them home, get points, win.
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalFlags extends ArenaGoal implements Listener {

	public GoalFlags() {
		super("Flags");
		debug = new Debug(100);
	}

	private final Map<String, Integer> paTeamLives = new HashMap<String, Integer>();
	private final Map<String, String> paTeamFlags = new HashMap<String, String>();
	private final Map<String, ItemStack> paHeadGears = new HashMap<String, ItemStack>();
	private static Set<Material> headFlags = new HashSet<Material>();

	private String flagName = "";

	static {
		headFlags.add(Material.PUMPKIN);
		headFlags.add(Material.WOOL);
		headFlags.add(Material.JACK_O_LANTERN);
		headFlags.add(Material.SKULL_ITEM);
	}

	@Override
	public String version() {
		return "v0.10.2.28";
	}

	private static final int PRIORITY = 6;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(final PACheck res, final String string) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		if (string.equalsIgnoreCase("flagtype")
				|| string.equalsIgnoreCase("flageffect")
				|| string.equalsIgnoreCase("touchdown")) {
			res.setPriority(this, PRIORITY);
		}

		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (string.contains(sTeam + "flag")) {
				res.setPriority(this, PRIORITY);
			}
		}

		return res;
	}

	@Override
	public PACheck checkEnd(final PACheck res) {

		if (res.getPriority() > PRIORITY) {
			return res;
		}

		final int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(this, PRIORITY); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError(this, "No teams playing!");
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (!list.contains(sTeam + "flag")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("flag")) {
						found = true;
						break;
					}
				}
				if (!found) {
					return team.getName() + "flag not set";
				}
			}
		}
		return null;
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

		if (!block
				.getType()
				.name()
				.equals(arena.getArenaConfig().getString(
						CFG.GOAL_FLAGS_FLAGTYPE))) {
			debug.i("block, but not flag", player);
			return res;
		}
		debug.i("flag click!", player);

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		if (paTeamFlags.containsValue(player.getName())) {
			debug.i("player " + player.getName() + " has got a flag", player);
			vLoc = block.getLocation().toVector();
			sTeam = aPlayer.getArenaTeam().getName();
			debug.i("block: " + vLoc.toString(), player);
			if (SpawnManager.getBlocks(arena, sTeam + "flag").size() > 0) {
				vFlag = SpawnManager
						.getBlockNearest(
								SpawnManager.getBlocks(arena, sTeam + "flag"),
								new PABlockLocation(player.getLocation()))
						.toLocation().toVector();
			} else {
				debug.i(sTeam + "flag = null", player);
			}

			debug.i("player is in the team " + sTeam, player);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				debug.i("player is at his flag", player);

				if (paTeamFlags.containsKey(sTeam)
						|| paTeamFlags.containsKey("touchdown")) {
					debug.i("the flag of the own team is taken!", player);

					if (arena.getArenaConfig().getBoolean(
							CFG.GOAL_FLAGS_MUSTBESAFE)
							&& !paTeamFlags.containsKey("touchdown")) {
						debug.i("cancelling", player);

						arena.msg(player,
								Language.parse(MSG.GOAL_FLAGS_NOTSAFE));
						return res;
					}
				}

				String flagTeam = getHeldFlagTeam(player.getName());

				debug.i("the flag belongs to team " + flagTeam, player);

				try {
					if (flagTeam.equals("touchdown")) {
						arena.broadcast(Language.parse(
								MSG.GOAL_FLAGS_TOUCHHOME, arena.getTeam(sTeam)
										.colorizePlayer(player)
										+ ChatColor.YELLOW, String
										.valueOf(paTeamLives.get(aPlayer
												.getArenaTeam().getName()) - 1)));
					} else {
						arena.broadcast(Language.parse(
								MSG.GOAL_FLAGS_BROUGHTHOME, arena
										.getTeam(sTeam).colorizePlayer(player)
										+ ChatColor.YELLOW,
								arena.getTeam(flagTeam).getColoredName()
										+ ChatColor.YELLOW, String
										.valueOf(paTeamLives.get(flagTeam) - 1)));
					}
					paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + flagTeam);
					e.printStackTrace();
				}
				if (flagTeam.equals("touchdown")) {
					takeFlag(ChatColor.BLACK.name(), false,
							SpawnManager.getCoords(arena, "touchdownflag"));
				} else {
					takeFlag(arena.getTeam(flagTeam).getColor().name(), false,
							SpawnManager.getCoords(arena, flagTeam + "flag"));
				}
				removeEffects(player);
				if (arena.getArenaConfig().getBoolean(
						CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
					if (paHeadGears.get(player.getName()) == null) {
						player.getInventory().setHelmet(
								new ItemStack(Material.AIR, 1));
					} else {
						player.getInventory().setHelmet(
								paHeadGears.get(player.getName()).clone());
						paHeadGears.remove(player.getName());
					}
				}

				flagTeam = flagTeam.equals("touchdown") ? (flagTeam + ":" + aPlayer
						.getArenaTeam().getName()) : flagTeam;

				reduceLivesCheckEndAndCommit(arena, flagTeam); // TODO move to
																// "commit" ?
			}
		} else {
			final ArenaTeam pTeam = aPlayer.getArenaTeam();
			if (pTeam == null) {
				return res;
			}
			final Set<ArenaTeam> setTeam = new HashSet<ArenaTeam>();

			for (ArenaTeam team : arena.getTeams()) {
				setTeam.add(team);
			}
			setTeam.add(new ArenaTeam("touchdown", "BLACK"));
			for (ArenaTeam team : setTeam) {
				final String aTeam = team.getName();

				if (aTeam.equals(pTeam.getName())) {
					debug.i("equals!OUT! ", player);
					continue;
				}
				if (team.getTeamMembers().size() < 1
						&& !team.getName().equals("touchdown")) {
					debug.i("size!OUT! ", player);
					continue; // dont check for inactive teams
				}
				if (paTeamFlags != null && paTeamFlags.containsKey(aTeam)) {
					debug.i("taken!OUT! ", player);
					continue; // already taken
				}
				debug.i("checking for flag of team " + aTeam, player);
				vLoc = block.getLocation().toVector();
				debug.i("block: " + vLoc.toString(), player);
				if (SpawnManager.getBlocks(arena, aTeam + "flag").size() > 0) {
					vFlag = SpawnManager
							.getBlockNearest(
									SpawnManager.getBlocks(arena, aTeam
											+ "flag"),
									new PABlockLocation(player.getLocation()))
							.toLocation().toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					debug.i("flag found!", player);
					debug.i("vFlag: " + vFlag.toString(), player);

					if (team.getName().equals("touchdown")) {

						arena.broadcast(Language.parse(
								MSG.GOAL_FLAGS_GRABBEDTOUCH,
								pTeam.colorizePlayer(player) + ChatColor.YELLOW));
					} else {

						arena.broadcast(Language
								.parse(MSG.GOAL_FLAGS_GRABBED,
										pTeam.colorizePlayer(player)
												+ ChatColor.YELLOW,
										team.getColoredName()
												+ ChatColor.YELLOW));
					}
					try {
						paHeadGears.put(player.getName(), player.getInventory()
								.getHelmet().clone());
					} catch (Exception e) {
					}
					final ItemStack itemStack = block.getState().getData().toItemStack()
							.clone();
					if (arena.getArenaConfig().getBoolean(
							CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
						itemStack.setDurability(getFlagOverrideTeamShort(arena, aTeam));
					}
					player.getInventory().setHelmet(itemStack);
					applyEffects(player);

					takeFlag(team.getColor().name(), true,
							new PALocation(block.getLocation()));
					paTeamFlags.put(aTeam, player.getName()); // TODO move to
																// "commit" ?

					return res;
				}
			}
		}

		return res;
	}

	private void applyEffects(final Player player) {
		final String value = arena.getArenaConfig().getString(
				CFG.GOAL_FLAGS_FLAGEFFECT);

		if (value.equalsIgnoreCase("none")) {
			return;
		}

		PotionEffectType pet = null;

		final String[] split = value.split("x");

		int amp = 1;

		if (split.length > 1) {
			try {
				amp = Integer.parseInt(split[1]);
			} catch (Exception e) {
			}
		}

		for (PotionEffectType x : PotionEffectType.values()) {
			if (x == null) {
				continue;
			}
			if (x.getName().equalsIgnoreCase(split[0])) {
				pet = x;
				break;
			}
		}

		if (pet == null) {
			PVPArena.instance.getLogger().warning(
					"Invalid Potion Effect Definition: " + value);
			return;
		}

		player.addPotionEffect(new PotionEffect(pet, amp, 2147000));
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
	public PACheck checkSetBlock(final PACheck res, final Player player, final Block block) {

		if (res.getPriority() > PRIORITY
				|| !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(this, PRIORITY); // success :)

		return res;
	}

	private void commit(final Arena arena, final String sTeam, final boolean win) {
		debug.i("[CTF] committing end: " + sTeam);
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

		paTeamLives.clear();
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(final CommandSender sender, final String[] args) {
		if (args[0].equalsIgnoreCase("flagtype")) {
			if (args.length < 2) {
				arena.msg(
						sender,
						Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT,
								String.valueOf(args.length), "2"));
				return;
			}

			try {
				final int amount = Integer.parseInt(args[1]);
				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGTYPE,
						Material.getMaterial(amount).name());
			} catch (Exception e) {
				final Material mat = Material.getMaterial(args[1].toUpperCase());

				if (mat == null) {
					arena.msg(sender,
							Language.parse(MSG.ERROR_MAT_NOT_FOUND, args[1]));
					return;
				}

				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGTYPE, mat.name());
			}
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TYPESET,
					CFG.GOAL_FLAGS_FLAGTYPE.toString()));

		} else if (args[0].equalsIgnoreCase("flageffect")) {

			// /pa [arena] flageffect SLOW 2
			if (args.length < 2) {
				arena.msg(
						sender,
						Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT,
								String.valueOf(args.length), "2"));
				return;
			}

			if (args[1].equalsIgnoreCase("none")) {
				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, args[1]);

				arena.getArenaConfig().save();
				arena.msg(
						sender,
						Language.parse(MSG.SET_DONE,
								CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), args[1]));
				return;
			}

			PotionEffectType pet = null;

			for (PotionEffectType x : PotionEffectType.values()) {
				if (x == null) {
					continue;
				}
				if (x.getName().equalsIgnoreCase(args[1])) {
					pet = x;
					break;
				}
			}

			if (pet == null) {
				arena.msg(sender, Language.parse(
						MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[1]));
				return;
			}

			int amp = 1;

			if (args.length == 5) {
				try {
					amp = Integer.parseInt(args[2]);
				} catch (Exception e) {
					arena.msg(sender,
							Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
					return;
				}
			}
			final String value = args[1] + "x" + amp;
			arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, value);

			arena.getArenaConfig().save();
			arena.msg(
					sender,
					Language.parse(MSG.SET_DONE,
							CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), value));

		} else if (args[0].contains("flag")) {
			for (ArenaTeam team : arena.getTeams()) {
				final String sTeam = team.getName();
				if (args[0].contains(sTeam + "flag")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);

					arena.msg(sender,
							Language.parse(MSG.GOAL_FLAGS_TOSET, flagName));
				}
			}
		} else if (args[0].equalsIgnoreCase("touchdown")) {
			flagName = args[0] + "flag";
			PAA_Region.activeSelections.put(sender.getName(), arena);

			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, flagName));
		}
	}

	@Override
	public void commitEnd(final boolean force) {
		debug.i("[FLAGS]");

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
	public boolean commitSetFlag(final Player player, final Block block) {
		if (block == null
				|| !block
						.getType()
						.name()
						.equals(arena.getArenaConfig().getString(
								CFG.GOAL_FLAGS_FLAGTYPE))) {
			return false;
		}

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		debug.i("trying to set a flag", player);

		// command : /pa redflag1
		// location: red1flag:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()),
				flagName);

		arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, flagName));

		PAA_Region.activeSelections.remove(player.getName());
		flagName = "";

		return false;
	}

	@Override
	public void commitStart() {
		// empty to kill the error ;)
	}

	@Override
	public void configParse(final YamlConfiguration config) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}

	@Override
	public void disconnect(final ArenaPlayer aPlayer) {
		if (paTeamFlags == null) {
			return;
		}
		final String sTeam = getHeldFlagTeam(aPlayer.getName());
		final ArenaTeam flagTeam = arena.getTeam(sTeam);
		
		if (flagTeam == null) {
			if (sTeam == null) {
				return;
			} else {
				arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPEDTOUCH, aPlayer
						.getArenaTeam().getColorCodeString()
						+ aPlayer.getName()
						+ ChatColor.YELLOW));

				paTeamFlags.remove("touchdown");
				if (paHeadGears != null && paHeadGears.get(aPlayer.getName()) != null) {
					if (aPlayer.get() != null) {
						aPlayer.get().getInventory()
								.setHelmet(paHeadGears.get(aPlayer.getName()).clone());
					}
					paHeadGears.remove(aPlayer.getName());
				}

				takeFlag(ChatColor.BLACK.name(), false,
						SpawnManager.getCoords(arena, "touchdownflag"));

			}
		} else {
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED, aPlayer
					.getArenaTeam().getColorCodeString()
					+ aPlayer.getName()
					+ ChatColor.YELLOW, flagTeam.getName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null && paHeadGears.get(aPlayer.getName()) != null) {
				if (aPlayer.get() != null) {
					aPlayer.get().getInventory()
							.setHelmet(paHeadGears.get(aPlayer.getName()).clone());
				}
				paHeadGears.remove(aPlayer.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));
		}
	}

	private short getFlagOverrideTeamShort(final Arena arena, final String team) {
		if (arena.getArenaConfig().getUnsafe("flagColors." + team) == null) {
			if (team.equals("touchdown")) {
				return StringParser
						.getColorDataFromENUM(ChatColor.BLACK.name());
			}
			return StringParser.getColorDataFromENUM(arena.getTeam(team)
					.getColor().name());
		}
		return StringParser.getColorDataFromENUM((String) arena
				.getArenaConfig().getUnsafe("flagColors." + team));
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (!res.hasError() && res.getPriority() <= PRIORITY) {
			res.setError(
					this,
					String.valueOf(paTeamLives.containsKey(aPlayer.getArenaTeam()
									.getName()) ? paTeamLives.get(aPlayer
									.getArenaTeam().getName()) : 0));
		}
		return res;
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	private String getHeldFlagTeam(final String player) {
		if (paTeamFlags.size() < 1) {
			return null;
		}

		debug.i("getting held FLAG of player " + player, player);
		for (String sTeam : paTeamFlags.keySet()) {
			debug.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands", player);
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
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
			if (name.endsWith("flag")) {
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
		final Random r = new Random();

		return locs.get(r.nextInt(locs.size()));
	}

	@Override
	public boolean hasSpawn(final String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.equalsIgnoreCase(teamName.toLowerCase() + "flag")) {
				return true;
			}
			if (string.toLowerCase().startsWith(
					teamName.toLowerCase() + "spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (!paTeamLives.containsKey(team.getName())) {
			paTeamLives.put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
					.getInt(CFG.GOAL_FLAGS_LIVES));

			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
			takeFlag(ChatColor.BLACK.name(), false,
					SpawnManager.getCoords(arena, "touchdownflag"));
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parsePlayerDeath(final Player player,
			final EntityDamageEvent lastDamageCause) {

		if (paTeamFlags == null) {
			debug.i("no flags set!!", player);
			return;
		}
		final String sTeam = getHeldFlagTeam(player.getName());
		final ArenaTeam flagTeam = arena.getTeam(sTeam);
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		
		if (flagTeam == null) {
			if (sTeam == null) {
				return;
			} else {
				arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPEDTOUCH, aPlayer
						.getArenaTeam().getColorCodeString()
						+ aPlayer.getName()
						+ ChatColor.YELLOW));

				paTeamFlags.remove("touchdown");
				if (paHeadGears != null && paHeadGears.get(aPlayer.getName()) != null) {
					if (aPlayer.get() != null) {
						aPlayer.get().getInventory()
								.setHelmet(paHeadGears.get(aPlayer.getName()).clone());
					}
					paHeadGears.remove(aPlayer.getName());
				}

				takeFlag(ChatColor.BLACK.name(), false,
						SpawnManager.getCoords(arena, "touchdownflag"));
			}
		} else {
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED, aPlayer
					.getArenaTeam().colorizePlayer(player) + ChatColor.YELLOW,
					flagTeam.getColoredName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null
					&& paHeadGears.get(player.getName()) != null) {
				player.getInventory().setHelmet(
						paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));
		}
	}

	@Override
	public void parseStart() {
		paTeamLives.clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				debug.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_FLAGS_LIVES, 3));
			}
			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
		}
		takeFlag(ChatColor.BLACK.name(), false,
				SpawnManager.getCoords(arena, "touchdownflag"));
	}

	private boolean reduceLivesCheckEndAndCommit(final Arena arena, final String team) {

		debug.i("reducing lives of team " + team);
		
		if (paTeamLives.get(team) == null) {
			if (team.contains(":")) {
				final String realTeam = team.split(":")[1];
				final int pos = paTeamLives.get(realTeam) - 1;
				if (pos > 0) {
					paTeamLives.put(realTeam, pos);
				} else {
					paTeamLives.remove(realTeam);
					commit(arena, realTeam, true);
					return true;
				}
			}
		} else {
			final int pos = paTeamLives.get(team) - 1;
			if (pos > 0) {
				paTeamLives.put(team, pos);
			} else {
				paTeamLives.remove(team);
				commit(arena, team, false);
				return true;
			}
		}
		return false;
	}

	private void removeEffects(final Player player) {
		final String value = arena.getArenaConfig().getString(
				CFG.GOAL_FLAGS_FLAGEFFECT);

		if (value.equalsIgnoreCase("none")) {
			return;
		}

		PotionEffectType pet = null;

		final String[] split = value.split("x");

		for (PotionEffectType x : PotionEffectType.values()) {
			if (x == null) {
				continue;
			}
			if (x.getName().equalsIgnoreCase(split[0])) {
				pet = x;
				break;
			}
		}

		if (pet == null) {
			PVPArena.instance.getLogger().warning(
					"Invalid Potion Effect Definition: " + value);
			return;
		}

		player.removePotionEffect(pet);
		player.addPotionEffect(new PotionEffect(pet, 0, 1));
	}

	@Override
	public void reset(final boolean force) {
		paTeamFlags.clear();
		paHeadGears.clear();
		paTeamLives.clear();
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
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
				&& (config.get("flagColors") == null)) {
			debug.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
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
	public void takeFlag(final String flagColor, final boolean take, final PALocation lBlock) {
		if (lBlock == null) {
			return;
		}
		if (!arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE)
				.equals("WOOL")) {
			lBlock.toLocation()
					.getBlock()
					.setType(
							take ? Material.BEDROCK : Material.valueOf(arena
									.getArenaConfig().getString(
											CFG.GOAL_FLAGS_FLAGTYPE)));
			return;
		}
		if (take) {
			lBlock.toLocation().getBlock()
					.setData(StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.toLocation().getBlock()
					.setData(StringParser.getColorDataFromENUM(flagColor));
		}
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaTeam team : arena.getTeams()) {
			score = (paTeamLives.containsKey(team.getName()) ? paTeamLives
					.get(team.getName()) : 0);
			if (scores.containsKey(team)) {
				scores.put(team.getName(), scores.get(team.getName()) + score);
			} else {
				scores.put(team.getName(), score);
			}
		}

		return scores;
	}

	@Override
	public void unload(final Player player) {
		disconnect(ArenaPlayer.parsePlayer(player.getName()));
		if (allowsJoinInBattle()) {
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
		}
	}

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();

		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();

		if (arena == null || !arena.getName().equals(this.arena.getName())) {
			return;
		}

		if (event.isCancelled()
				|| getHeldFlagTeam(player.getName()) == null) {
			return;
		}

		if (event.getInventory().getType().equals(InventoryType.CRAFTING)
				&& event.getRawSlot() != 5) {
			return;
		}

		event.setCancelled(true);
	}
}
