package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.commands.PAA_Setup;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <pre>
 * Configuration Manager class
 * </pre>
 * <p/>
 * Provides static methods to manage Configurations
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class ConfigurationManager {
    //private final static Debug DEBUG = new Debug(25);

    private ConfigurationManager() {
    }

    /**
     * create a config manager instance
     *
     * @param arena the arena to load
     * @param cfg   the configuration
     */
    public static boolean configParse(final Arena arena, final Config cfg) {
        if (!cfg.load()) {
            return false;
        }
        final YamlConfiguration config = cfg.getYamlConfiguration();

        final List<String> goals = cfg.getStringList("goals", new ArrayList<String>());
        final List<String> modules = cfg.getStringList("mods", new ArrayList<String>());

        if (cfg.getString(CFG.GENERAL_TYPE, "null") == null
                || "null".equals(cfg.getString(CFG.GENERAL_TYPE, "null"))) {
            cfg.createDefaults(goals, modules);
        } else {
            // opening existing arena
            arena.setFree("free".equals(cfg.getString(CFG.GENERAL_TYPE)));


            values:
            for (final CFG c : CFG.getValues()) {
                if (c.hasModule()) {
                    for (final String goal : goals) {
                        if (goal.equals(c.getModule())) {
                            if (cfg.getUnsafe(c.getNode()) == null) {
                                cfg.createDefaults(goals, modules);
                                break values;
                            }
                        }
                    }

                    for (final String mod : modules) {
                        if (mod.equals(c.getModule())) {
                            if (cfg.getUnsafe(c.getNode()) == null) {
                                cfg.createDefaults(goals, modules);
                                break values;
                            }
                        }
                    }
                    continue; // node unused, don't check for existence!
                }
                if (cfg.getUnsafe(c.getNode()) == null) {
                    cfg.createDefaults(goals, modules);
                    break;
                }
            }

            List<String> list = cfg.getStringList(CFG.LISTS_GOALS.getNode(),
                    new ArrayList<String>());
            for (final String goal : list) {
                ArenaGoal aGoal = PVPArena.instance.getAgm()
                        .getGoalByName(goal);
                if (aGoal == null) {
                    PVPArena.instance.getLogger().warning(
                            "Goal referenced in arena '" +
                                    arena.getName() + "' not found (uninstalled?): " + goal);
                    continue;
                }
                aGoal = (ArenaGoal) aGoal.clone();
                aGoal.setArena(arena);
                arena.goalAdd(aGoal);
            }

            list = cfg.getStringList(CFG.LISTS_MODS.getNode(),
                    new ArrayList<String>());
            for (final String mod : list) {
                ArenaModule aMod = PVPArena.instance.getAmm().getModByName(mod);
                if (aMod == null) {
                    PVPArena.instance.getLogger().warning(
                            "Module referenced in arena '" +
                                    arena.getName() + "' not found (uninstalled?): " + mod);
                    continue;
                }
                aMod = (ArenaModule) aMod.clone();
                aMod.setArena(arena);
                aMod.toggleEnabled(arena);
            }

        }

        if (config.get("classitems") == null) {
            if (PVPArena.instance.getConfig().get("classitems") == null) {
                config.addDefault("classitems.Ranger",
                        "BOW,ARROW:64,LEATHER_HELMET,LEATHER_CHESTPLATE,LEATHER_LEGGINGS,LEATHER_BOOTS");
                config.addDefault("classitems.Swordsman",
                        "DIAMOND_SWORD,IRON_HELMET,IRON_CHESTPLATE,IRON_LEGGINGS,IRON_BOOTS");
                config.addDefault("classitems.Tank",
                        "STONE_SWORD,DIAMOND_HELMET,DIAMOND_CHESTPLATE,DIAMOND_LEGGINGS,DIAMOND_BOOTS");
                config.addDefault("classitems.Pyro",
                        "FLINT_AND_STEEL,TNT:3,LEATHER_HELMET,LEATHER_CHESTPLATE,LEATHER_LEGGINGS,LEATHER_BOOTS");
            } else {
                for (final String key : PVPArena.instance.getConfig().getKeys(false)) {
                    config.addDefault("classitems." + key, PVPArena.instance
                            .getConfig().get("classitems." + key));
                }
            }
        }

        if (config.get("time_intervals") == null) {
            String prefix = "time_intervals.";
            config.addDefault(prefix + "1", "1..");
            config.addDefault(prefix + "2", "2..");
            config.addDefault(prefix + "3", "3..");
            config.addDefault(prefix + "4", "4..");
            config.addDefault(prefix + "5", "5..");
            config.addDefault(prefix + "10", "10 %s");
            config.addDefault(prefix + "20", "20 %s");
            config.addDefault(prefix + "30", "30 %s");
            config.addDefault(prefix + "60", "60 %s");
            config.addDefault(prefix + "120", "2 %m");
            config.addDefault(prefix + "180", "3 %m");
            config.addDefault(prefix + "240", "4 %m");
            config.addDefault(prefix + "300", "5 %m");
            config.addDefault(prefix + "600", "10 %m");
            config.addDefault(prefix + "1200", "20 %m");
            config.addDefault(prefix + "1800", "30 %m");
            config.addDefault(prefix + "2400", "40 %m");
            config.addDefault(prefix + "3000", "50 %m");
            config.addDefault(prefix + "3600", "60 %m");
        }

        PVPArena.instance.getAgm().setDefaults(arena, config);

        config.options().copyDefaults(true);

        cfg.set(CFG.Z, "1.3.3.217");
        cfg.save();
        cfg.load();

        final Map<String, Object> classes = config.getConfigurationSection(
                "classitems").getValues(false);
        arena.getClasses().clear();
        arena.getDebugger().i("reading class items");
        ArenaClass.addGlobalClasses(arena);
        for (final Map.Entry<String, Object> stringObjectEntry1 : classes.entrySet()) {
            final String sItemList;

            try {
                sItemList = (String) stringObjectEntry1.getValue();
            } catch (final Exception e) {
                Bukkit.getLogger().severe(
                        "[PVP Arena] Error while parsing class, skipping: "
                                + stringObjectEntry1.getKey());
                continue;
            }
            try {

                String classChest = (String) config.getConfigurationSection("classchests").get(stringObjectEntry1.getKey());
                PABlockLocation loc = new PABlockLocation(classChest);
                Chest c = (Chest) loc.toLocation().getBlock().getState();
                ItemStack[] contents = c.getInventory().getContents();
                final ItemStack[] items = Arrays.copyOfRange(contents, 0, contents.length - 5);
                final ItemStack offHand = contents[contents.length - 5];
                final ItemStack[] armors = Arrays.copyOfRange(contents, contents.length - 4, contents.length);
                arena.addClass(stringObjectEntry1.getKey(), items, offHand, armors);
                arena.getDebugger().i("adding class chest items to class " + stringObjectEntry1.getKey());

            }   catch (Exception e) {
                final String[] sItems = sItemList.split(",");
                final ItemStack[] items = new ItemStack[sItems.length];
                final ItemStack[] offHand = new ItemStack[1];
                final ItemStack[] armors = new ItemStack[4];

                for (int i = 0; i < sItems.length; i++) {

                    if (sItems[i].contains(">>!<<")) {
                        final String[] split = sItems[i].split(">>!<<");

                        final int id = Integer.parseInt(split[0]);
                        armors[id] = StringParser.getItemStackFromString(split[1]);

                        if (armors[id] == null) {
                            PVPArena.instance.getLogger().warning(
                                    "unrecognized armor item: " + split[1]);
                        }

                        sItems[i] = "AIR";
                    } else if (sItems[i].contains(">>O<<")) {
                        final String[] split = sItems[i].split(">>O<<");

                        final int id = Integer.parseInt(split[0]);
                        offHand[id] = StringParser.getItemStackFromString(split[1]);

                        if (offHand[id] == null) {
                            PVPArena.instance.getLogger().warning(
                                    "unrecognized offhand item: " + split[1]);
                        }
                        sItems[i] = "AIR";
                    }

                    items[i] = StringParser.getItemStackFromString(sItems[i]);
                    if (items[i] == null) {
                        PVPArena.instance.getLogger().warning(
                                "unrecognized item: " + items[i]);
                    }
                }
                arena.addClass(stringObjectEntry1.getKey(), items, offHand[0], armors);
                arena.getDebugger().i("adding class items to class " + stringObjectEntry1.getKey());
            }
        }
        arena.addClass("custom", StringParser.getItemStacksFromString("AIR"), StringParser.getItemStackFromString("AIR"), StringParser.getItemStacksFromString("AIR"));
        arena.setOwner(cfg.getString(CFG.GENERAL_OWNER));
        arena.setLocked(!cfg.getBoolean(CFG.GENERAL_ENABLED));
        arena.setFree("free".equals(cfg.getString(CFG.GENERAL_TYPE)));
        if (config.getConfigurationSection("arenaregion") == null) {
            arena.getDebugger().i("arenaregion null");
        } else {
            arena.getDebugger().i("arenaregion not null");
            final Map<String, Object> regs = config.getConfigurationSection(
                    "arenaregion").getValues(false);
            for (final String rName : regs.keySet()) {
                arena.getDebugger().i("arenaregion '" + rName + '\'');
                final ArenaRegion region = Config.parseRegion(arena, config,
                        rName);

                if (region == null) {
                    PVPArena.instance.getLogger().severe(
                            "Error while loading arena, region null: " + rName);
                } else if (region.getWorld() == null) {
                    PVPArena.instance.getLogger().severe(
                            "Error while loading arena, world null: " + rName);
                } else {
                    arena.addRegion(region);
                }
            }
        }
        arena.setRoundMap(config.getStringList("rounds"));

        cfg.save();

        PVPArena.instance.getAgm().configParse(arena, config);

        if (cfg.getYamlConfiguration().getConfigurationSection("teams") == null) {
            if (arena.isFreeForAll()) {
                config.set("teams.free", "WHITE");
            } else {
                config.set("teams.red", "RED");
                config.set("teams.blue", "BLUE");
            }
        }

        cfg.reloadMaps();

        final Map<String, Object> tempMap = cfg
                .getYamlConfiguration().getConfigurationSection("teams")
                .getValues(true);

        if (arena.isFreeForAll()) {
            if (!arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL)) {
                PVPArena.instance.getLogger().warning("Arena " + arena.getName() + " is running in NO-PVP mode! Make sure people can die! Ignore this if you're running infect!");
            }
        } else {
            for (final Map.Entry<String, Object> stringObjectEntry : tempMap.entrySet()) {
                final ArenaTeam team = new ArenaTeam(stringObjectEntry.getKey(),
                        (String) stringObjectEntry.getValue());
                arena.getTeams().add(team);
                arena.getDebugger().i("added team " + team.getName() + " => "
                        + team.getColorCodeString());
            }
        }

        ArenaModuleManager.configParse(arena, config);
        cfg.save();
        cfg.reloadMaps();

        arena.setPrefix(cfg.getString(CFG.GENERAL_PREFIX));
        return true;
    }

    /**
     * check if an arena is configured completely
     *
     * @param arena the arena to check
     * @return an error string if there is something missing, null otherwise
     */
    public static String isSetup(final Arena arena) {
        //arena.getArenaConfig().load();

        if (arena.getArenaConfig().getUnsafe("spawns") == null) {
            return Language.parse(arena, MSG.ERROR_NO_SPAWNS);
        }

        for (final String editor : PAA_Edit.activeEdits.keySet()) {
            if (PAA_Edit.activeEdits.get(editor).getName().equals(
                    arena.getName())) {
                return Language.parse(arena, MSG.ERROR_EDIT_MODE);
            }
        }

        for (final String setter : PAA_Setup.activeSetups.keySet()) {
            if (PAA_Setup.activeSetups.get(setter).getName().equals(
                    arena.getName())) {
                return Language.parse(arena, MSG.ERROR_SETUP_MODE);
            }
        }

        final Set<String> list = arena.getArenaConfig().getYamlConfiguration()
                .getConfigurationSection("spawns").getValues(false).keySet();

        final String sExit = arena.getArenaConfig().getString(CFG.TP_EXIT);
        if (!"old".equals(sExit) && !list.contains(sExit)) {
            return "Exit Spawn ('" + sExit + "') not set!";
        }
        final String sWin = arena.getArenaConfig().getString(CFG.TP_WIN);
        if (!"old".equals(sWin) && !list.contains(sWin)) {
            return "Win Spawn ('" + sWin + "') not set!";
        }
        final String sLose = arena.getArenaConfig().getString(CFG.TP_LOSE);
        if (!"old".equals(sLose) && !list.contains(sLose)) {
            return "Lose Spawn ('" + sLose + "') not set!";
        }
        final String sDeath = arena.getArenaConfig().getString(CFG.TP_DEATH);
        if (!"old".equals(sDeath) && !list.contains(sDeath)) {
            return "Death Spawn ('" + sDeath + "') not set!";
        }

        String error = ArenaModuleManager.checkForMissingSpawns(arena, list);
        if (error != null) {
            return Language.parse(arena, MSG.ERROR_MISSING_SPAWN, error);
        }
        error = PVPArena.instance.getAgm().checkForMissingSpawns(arena, list);
        if (error != null) {
            return Language.parse(arena, MSG.ERROR_MISSING_SPAWN, error);
        }
        return null;
    }
}
