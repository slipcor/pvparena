package net.slipcor.pvparena.commands;

import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena INFO Command class</pre>
 * 
 * A command to display the active modules of an arena and settings
 * 
 * @author slipcor
 */

public class PAI_Info extends AbstractArenaCommand {

	public PAI_Info() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		String displayMode = null;
		
		if (args.length > 0) {
			displayMode = args[0];
		}
		
		arena.msg(sender, Language.parse(arena, MSG.INFO_HEAD_HEADLINE, arena.getName(), arena.getPrefix()));
		
		arena.msg(sender, Language.parse(arena, MSG.INFO_HEAD_TEAMS, 
				StringParser.joinSet(arena.getTeamNamesColored(), ChatColor.COLOR_CHAR + "r, ")));
		
		arena.msg(sender, StringParser.colorVar("fighting", arena.isFightInProgress()) + " | " +
				StringParser.colorVar("custom", arena.isCustomClassAlive()) + " | " +
				StringParser.colorVar("enabled", !arena.isLocked()));
		
		final Set<String> classes = new HashSet<String>();
		for (ArenaClass ac : arena.getClasses()) {
			if (!ac.getName().equalsIgnoreCase("custom")) {
				classes.add(ac.getName());
			}
		}
		
		arena.msg(sender,  Language.parse(arena, MSG.INFO_CLASSES, StringParser.joinSet(classes, ", ")));
		arena.msg(sender,  Language.parse(arena, MSG.INFO_OWNER, (arena.getOwner()==null?"server":arena.getOwner())));

		if (displayMode == null || displayMode.equals("chat")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "chat"));
			arena.msg(sender, StringParser.colorVar("colorNick",
					arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK))+" | " +
				StringParser.colorVar("defaultTeam", 
						arena.getArenaConfig().getBoolean(CFG.CHAT_DEFAULTTEAM))+" | " +
				StringParser.colorVar("enabled", 
						arena.getArenaConfig().getBoolean(CFG.CHAT_ENABLED))+" | " +
				StringParser.colorVar("onlyPrivate", 
						arena.getArenaConfig().getBoolean(CFG.CHAT_ONLYPRIVATE)));
		}
		
		if (displayMode == null || displayMode.equals("command")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "command"));
			arena.msg(sender, StringParser.colorVar("defaultjoin", 
						arena.getArenaConfig().getBoolean(CFG.CMDS_DEFAULTJOIN)));
		}

		if (displayMode == null || displayMode.equals("damage")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "damage"));
			arena.msg(sender, StringParser.colorVar("armor",
					arena.getArenaConfig().getBoolean(CFG.DAMAGE_ARMOR))+" | " +
					"spawnCamp: "+ arena.getArenaConfig().getInt(CFG.DAMAGE_SPAWNCAMP)+" | " +
				StringParser.colorVar("weapons", 
						arena.getArenaConfig().getBoolean(CFG.DAMAGE_WEAPONS)));
		}

		if (displayMode == null || displayMode.equals("general")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "general"));
			arena.msg(sender, StringParser.colorVar("classspawn",
					arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN))+" | " +
				StringParser.colorVar("leavedeath", 
						arena.getArenaConfig().getBoolean(CFG.GENERAL_LEAVEDEATH))+" | " +
				StringParser.colorVar("quickspawn", 
						arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN))+" | " +
				StringParser.colorVar("smartspawn", 
						arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)));
			
			arena.msg(sender,
					"gameMode: "+ arena.getArenaConfig().getInt(CFG.GENERAL_GAMEMODE)+" | " +
					"time: "+ arena.getArenaConfig().getInt(CFG.GENERAL_TIME)+" | " +
					"wand: "+ arena.getArenaConfig().getInt(CFG.GENERAL_WAND));

		}
		
		if (displayMode == null || displayMode.equals("command")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "goal"));
			arena.msg(sender, StringParser.colorVar("addLivesPerPlayer", 
						arena.getArenaConfig().getBoolean(CFG.GOAL_ADDLIVESPERPLAYER)));
		}

		if (displayMode == null || displayMode.equals("item")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "item"));
			arena.msg(sender, "minplayers: " +
					arena.getArenaConfig().getInt(CFG.ITEMS_MINPLAYERS)+" | " +
					"rewards: " +
						arena.getArenaConfig().getString(CFG.ITEMS_REWARDS)+" | " +
				StringParser.colorVar("random", 
						arena.getArenaConfig().getBoolean(CFG.ITEMS_RANDOM)));
			
		}

		if (displayMode == null || displayMode.equals("join")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "join"));
			arena.msg(sender, "range: " +
					arena.getArenaConfig().getInt(CFG.JOIN_RANGE)+" | " +
				StringParser.colorVar("forceregionjoin", 
						arena.getArenaConfig().getBoolean(CFG.JOIN_FORCE)));
			
		}

		if (displayMode == null || displayMode.equals("perms")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "perms"));
			arena.msg(sender, StringParser.colorVar("explicitarena",
					arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA))+" | " +
				StringParser.colorVar("explicitclass", 
						arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITCLASS))+" | " +
				StringParser.colorVar("joininbattle", 
						arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE))+" | " +
				StringParser.colorVar("teamkill", 
						arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL)));
			
		}

		if (displayMode == null || displayMode.equals("player")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "player"));
			arena.msg(sender, StringParser.colorVar("autoIgniteTNT",
					arena.getArenaConfig().getBoolean(CFG.PLAYER_AUTOIGNITE))+" | " +
				StringParser.colorVar("dropsInventory", 
						arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY))+" | " +
				StringParser.colorVar("preventDeath", 
						arena.getArenaConfig().getBoolean(CFG.PLAYER_PREVENTDEATH))+" | " +
				StringParser.colorVar("refillInventory", 
						arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)));

			arena.msg(sender,
					"exhaustion: "+ arena.getArenaConfig().getDouble(CFG.PLAYER_EXHAUSTION)+" | " +
					"foodLevel: "+ arena.getArenaConfig().getInt(CFG.PLAYER_FOODLEVEL)+" | " +
					"health: "+ arena.getArenaConfig().getInt(CFG.PLAYER_HEALTH)+" | " +
					"saturation: "+ arena.getArenaConfig().getInt(CFG.PLAYER_SATURATION));
		}

		if (displayMode == null || displayMode.equals("protect")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "protect"));
			arena.msg(sender, StringParser.colorVar("enabled",
					arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED))+" | " +
				StringParser.colorVar("punish", 
						arena.getArenaConfig().getBoolean(CFG.PROTECT_PUNISH))+" | " +
				"spawn: " + arena.getArenaConfig().getInt(CFG.PROTECT_SPAWN));

		}

		if (displayMode == null || displayMode.equals("ready")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "ready"));
			arena.msg(sender, StringParser.colorVar("checkEachPlayer",
					arena.getArenaConfig().getBoolean(CFG.READY_CHECKEACHPLAYER))+" | " +
				StringParser.colorVar("checkEachTeam", 
						arena.getArenaConfig().getBoolean(CFG.READY_CHECKEACHTEAM))+" | " +
				"autoClass: " + arena.getArenaConfig().getString(CFG.READY_AUTOCLASS));


			arena.msg(sender,
					"block: "+ arena.getArenaConfig().getInt(CFG.READY_BLOCK)+" | " +
					"minPlayers: "+ arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS)+" | " +
					"maxPlayers: "+ arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS)+" | " +
					"maxTeam: "+ arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS)+" | " +
					"neededRatio: "+ arena.getArenaConfig().getDouble(CFG.READY_NEEDEDRATIO));
		}
		
		if (displayMode == null || displayMode.equals("time")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "time"));
			arena.msg(sender,
					"endCountDown: "+ arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN)+" | " +
					"startCountDown: "+ arena.getArenaConfig().getInt(CFG.TIME_STARTCOUNTDOWN)+" | " +
					"regionTimer: "+ arena.getArenaConfig().getInt(CFG.TIME_REGIONTIMER));
			arena.msg(sender,
					"teleportProtect: "+ arena.getArenaConfig().getInt(CFG.TIME_TELEPORTPROTECT)+" | " +
					"warmupCountDown: "+ arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN)+" | " +
					"pvp: "+ arena.getArenaConfig().getInt(CFG.TIME_PVP));
			
		}

		if (displayMode == null || displayMode.equals("tp")) {
			arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "tp"));
			arena.msg(sender,
					"death: "+ arena.getArenaConfig().getString(CFG.TP_DEATH)+" | " +
					"exit: "+ arena.getArenaConfig().getString(CFG.TP_EXIT)+" | " +
					"lose: "+ arena.getArenaConfig().getString(CFG.TP_LOSE)+" | " +
					"win: "+ arena.getArenaConfig().getString(CFG.TP_WIN));
				
		}

		if (displayMode == null || displayMode.equals("")) {
			if (displayMode == null || displayMode.equals("chat")) {
				arena.msg(sender, Language.parse(arena, MSG.INFO_SECTION, "chat"));
				arena.msg(sender, StringParser.colorVar("classSignsDisplay",
						arena.getArenaConfig().getBoolean(CFG.USES_CLASSSIGNSDISPLAY))+" | " +
					StringParser.colorVar("deathMessages", 
							arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES))+" | " +
					StringParser.colorVar("evenTeams", 
							arena.getArenaConfig().getBoolean(CFG.USES_EVENTEAMS)));

				arena.msg(sender, StringParser.colorVar("ingameClassSwitch",
						arena.getArenaConfig().getBoolean(CFG.USES_INGAMECLASSSWITCH))+" | " +
					StringParser.colorVar("overlapCheck", 
							arena.getArenaConfig().getBoolean(CFG.USES_OVERLAPCHECK))+" | " +
					StringParser.colorVar("woolHead", 
							arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)));
			}
		}
		
		if (displayMode == null || displayMode.equalsIgnoreCase("region")) {
		
			if (arena.getRegions() != null) {
				final Set<String> regions = new HashSet<String>();
				for (ArenaRegionShape ar : arena.getRegions()) {
					regions.add(ar.getRegionName());
				}
				
				arena.msg(sender,  Language.parse(arena, MSG.INFO_REGIONS, StringParser.joinSet(regions, ", ")));
			}
		}
		

		if (displayMode == null || displayMode.equalsIgnoreCase("goal")) {
			for (ArenaGoal goal : arena.getGoals()) {
				arena.msg(sender, Language.parse(arena, MSG.INFO_GOAL_ACTIVE, goal.getName()));
				goal.displayInfo(sender);
			}
		}
		
		if (displayMode == null || displayMode.equalsIgnoreCase("mod")) {
			for (ArenaModule mod : arena.getMods()) {
				arena.msg(sender, Language.parse(arena, MSG.INFO_MOD_ACTIVE, mod.getName()));
				mod.displayInfo(sender);
			}
		}

		if (displayMode == null || displayMode.equalsIgnoreCase("region")) {
			for (ArenaRegionShape reg : arena.getRegions()) {
				reg.displayInfo(sender);
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.INFO));
	}
}
