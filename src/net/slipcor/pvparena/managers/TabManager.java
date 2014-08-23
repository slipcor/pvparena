package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.api.IArenaCommandHandler;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.AbstractGlobalCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class TabManager {
    private TabManager() {}

    public static List<String> getMatches(final CommandSender sender, final List<AbstractArenaCommand> arenaCommands, final List<AbstractGlobalCommand> globalCommands, String[] args) {
        final Set<String> matches = new LinkedHashSet<String>();
        Arena arena = null;
        if (sender instanceof Player) {
            arena = ArenaPlayer.parsePlayer(sender.getName()).getArena();
        }

        if (args.length < 1) {
            for (final Arena a : ArenaManager.getArenas()) {
                matches.add(a.getName());
            }
            return new ArrayList<String>(matches);
        }

        if (arena == null) {
            // no proper arena yet
            arena = ArenaManager.getArenaByName(args[0]);
            if (arena == null && ArenaManager.getArenas().size() == 1) {
                // still no arena, get the only arena!
                arena = ArenaManager.getFirst();
                // continue with one arg less
                args = Arrays.copyOfRange(args, 1, args.length);
                if (args.length < 1) {
                    // empty -> turn to catchall
                    args = new String[]{""};
                }
            } else if (arena != null) {
                // arena has been found
                if (args.length < 2) {
                    // return the exact arena name
                    matches.add(arena.getName());
                    return new ArrayList<String>(matches);
                } else {
                    // we have more args!
                    args = Arrays.copyOfRange(args, 1, args.length);
                    if (args.length < 1) {
                        // empty -> turn to catchall
                        args = new String[]{""};
                    }
                }
            } else {
                // arena has still not been found
                if (args.length > 1) {
                    // the sender has already entered the next thing without a valid arena
                    sender.sendMessage(Language.parse(Language.MSG.ERROR_ARENA_NOTFOUND, args[0]));
                    return new ArrayList<String>(matches);
                }
                for (final Arena a : ArenaManager.getArenas()) {
                    matches.add(a.getName());
                }
                return new ArrayList<String>(matches);
            }
        }

        if (args.length == 1) {
            addCommandsStartingWithPrefix(matches, sender, arena, arenaCommands, args[0]);
            addCommandsStartingWithPrefix(matches, sender, arena, globalCommands, args[0]);

            if (arena == null) {
                addCommandsStartingWithPrefix(matches, sender, null, PVPArena.instance.getAgm().getAllGoals(), args[0]);
                addCommandsStartingWithPrefix(matches, sender, null, PVPArena.instance.getAmm().getAllMods(), args[0]);
            } else {
                addCommandsStartingWithPrefix(matches, sender, arena, new ArrayList<ArenaGoal>(arena.getGoals()), args[0]);
                addCommandsStartingWithPrefix(matches, sender, arena, new ArrayList<ArenaModule>(arena.getMods()), args[0]);
            }
            return new ArrayList<String>(matches);
        }

        final List<CommandTree<String>> commands = new ArrayList<CommandTree<String>>();
        addTreesMatchingValueInHandlerList(commands, arenaCommands, arena, args[0]);
        addTreesMatchingValueInHandlerList(commands, globalCommands, arena, args[0]);
        if (arena == null) {
            addTreesMatchingValueInHandlerList(commands, PVPArena.instance.getAgm().getAllGoals(), null, args[0]);
            addTreesMatchingValueInHandlerList(commands, PVPArena.instance.getAmm().getAllMods(), null, args[0]);
        } else {
            addTreesMatchingValueInHandlerList(commands, new ArrayList<ArenaGoal>(arena.getGoals()), arena, args[0]);
            addTreesMatchingValueInHandlerList(commands, new ArrayList<ArenaModule>(arena.getMods()), arena, args[0]);
        }

        for (final CommandTree<String> tree : commands) {
            addMatchesFromCommandTree(matches, Arrays.copyOfRange(args, 1, args.length), tree);
        }
        return new ArrayList<String>(matches);
    }

    /**
     * Find a Set of main sub commands and main shortcuts matching a prefix in a List of ArenaCommandHandler
     *
     * @param matches the Set to add to
     * @param list    the ArenaCommandHandler list to search
     * @param prefix  the prefix to look for
     */
    private static void addCommandsStartingWithPrefix(final Set<String> matches, final CommandSender sender, final Arena arena, final List<? extends IArenaCommandHandler> list, final String prefix) {
        for (final IArenaCommandHandler ach : list) {
            if (ach.hasPerms(sender, arena)) {
                for (final String value : ach.getMain()) {
                    if (value.startsWith(prefix)) {
                        matches.add(value);
                    }
                }
                for (final String value : ach.getShort()) {
                    if (value.startsWith(prefix)) {
                        matches.add(value);
                    }
                }
            }
        }
    }

    /**
     * Add matching entries of an Enum to a List of String
     *
     * @param result the List to add to
     * @param key    the key to match
     * @param list   the Enum list to search
     */
    private static void addEnumMatchesToList(final List<String> result, final String key, final List<? extends Enum> list) {
        for (final Enum e : list) {
            if (e.name().startsWith(key)) {
                result.add(e.name());
            }
        }
    }

    /**
     * Search a CommandTree (recursively), add found matches to the matches Set
     *
     * @param matches the set to add to
     * @param args    the arguments to search for
     * @param sub     the current CommandTree
     */
    private static void addMatchesFromCommandTree(final Set<String> matches, final String[] args, final CommandTree<String> sub) {
        if (args.length < 1) {
            return;
        }
        String override = args[0];
        if (args.length == 1) {
            // we have the last argument
            for (final String key : sub.getContent()) {
                matches.addAll(getKeyMatchesInsideDefinition(override, key));
            }
            return;
        }
        if (override.isEmpty()) {
            for (String key : sub.getContent()) {
                matches.addAll(getKeyMatchesInsideDefinition(override, key));
            }
        } else {
            // should have a subvalue
            for (String key : sub.getContent()) {
                String newOverride = getOverrideFromDefinition(override, key);
                if (getKeyMatchesInsideDefinition(newOverride, key).size() > 0) {
                    override = newOverride;
                    break;
                }
            }
        }
        if (sub.contains(override)) {
            addMatchesFromCommandTree(matches, Arrays.copyOfRange(args, 1, args.length), sub.get(override));
        }
    }

    /**
     * Read a list of ArenaCommandHandler, add exact matches to a list of CommandTree
     *
     * @param treeList    the list of CommandTree to add to
     * @param handlerList the list of ArenaCommandHandler to search
     * @param arena       the arena instance to apply for subvalues (can be null)
     * @param value       the value to search for
     */
    private static void addTreesMatchingValueInHandlerList(final List<CommandTree<String>> treeList, final List<? extends IArenaCommandHandler> handlerList, final Arena arena, final String value) {

        outer:
        for (final IArenaCommandHandler aac : handlerList) {
            for (final String entry : aac.getMain()) {
                if (entry.equals(value)) {
                    treeList.add(aac.getSubs(arena));
                    continue outer;
                }
            }
            for (final String entry : aac.getShort()) {
                if (entry.equals(value)) {
                    treeList.add(aac.getSubs(arena));
                    continue outer;
                }
            }
        }
    }

    /**
     * Return matches of a key inside a special node definition
     *
     * @param key        the key to match
     * @param definition the node definition ("{Enum}")
     * @return a set of matching nodes
     */
    private static List<String> getKeyMatchesInsideDefinition(final String key, final String definition) {
        final List<String> result = new ArrayList<String>();
        if (key != null && !key.isEmpty() && definition.startsWith(key) || key != null && key.isEmpty() && !definition.startsWith("{")) {
            result.add(definition);
        }
        if (definition.startsWith("{")) {
            if ("{Material}".equals(definition)) {
                final Material[] mats = Material.values();
                addEnumMatchesToList(result, key, Arrays.asList(mats));
            } else if ("{Player}".equals(definition)) {
                final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                if (key != null && key.isEmpty()) {
                    for (final Player val : players) {
                        result.add(val.getName());
                    }
                } else {
                    for (final Player val : players) {
                        if (val.getName().startsWith(key)) {
                            result.add(val.getName());
                        }
                    }
                }
            } else if ("{RegionProtection}".equals(definition)) {
                final ArenaRegion.RegionProtection[] protections = ArenaRegion.RegionProtection.values();
                addEnumMatchesToList(result, key, Arrays.asList(protections));
            } else if ("{RegionFlag}".equals(definition)) {
                final ArenaRegion.RegionFlag[] flags = ArenaRegion.RegionFlag.values();
                addEnumMatchesToList(result, key, Arrays.asList(flags));
            } else if ("{RegionType}".equals(definition)) {
                final ArenaRegion.RegionType[] types = ArenaRegion.RegionType.values();
                addEnumMatchesToList(result, key, Arrays.asList(types));
            } else if ("{Boolean}".equals(definition)) {
                final List<String> values = new ArrayList<String>();
                values.addAll(StringParser.negative);
                values.addAll(StringParser.positive);
                if (key != null && key.isEmpty()) {
                    result.addAll(values);
                } else {
                    for (final String val : values) {
                        if (val.startsWith(key)) {
                            result.add(val);
                        }
                    }
                }
            } else if ("{PotionEffectType}".equals(definition)) {
                final PotionEffectType[] pet = PotionEffectType.values();
                if (key != null && key.isEmpty()) {
                    for (final PotionEffectType val : pet) {
                        result.add(val.getName());
                    }
                } else {
                    for (final PotionEffectType val : pet) {
                        if (val.getName().startsWith(key)) {
                            result.add(val.getName());
                        }
                    }
                }
            } else if ("{EntityType}".equals(definition)) {
                final EntityType[] entityTypes = EntityType.values();
                addEnumMatchesToList(result, key, Arrays.asList(entityTypes));
            }
        }
        return result;
    }

    /**
     * Find an override key inside a special node definition
     *
     * @param key        the key to find
     * @param definition the node definition ("{Enum}")
     * @return the definition if found, the key, if not
     */
    private static String getOverrideFromDefinition(final String key, final String definition) {
        if (definition.startsWith("{")) {
            if ("{Material}".equals(definition)) {
                final Material[] mats = Material.values();
                return getOverrideKey(key, definition, Arrays.asList(mats));
            } else if ("{String}".equals(definition)) {
                return definition;
            } else if ("{Player}".equals(definition)) {
                final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                for (final Player val : players) {
                    if (val.getName().equals(key)) {
                        return definition;
                    }
                }
                return key;
            } else if ("{RegionProtection}".equals(definition)) {
                final ArenaRegion.RegionProtection[] protections = ArenaRegion.RegionProtection.values();
                return getOverrideKey(key, definition, Arrays.asList(protections));
            } else if ("{RegionFlag}".equals(definition)) {
                final ArenaRegion.RegionFlag[] flags = ArenaRegion.RegionFlag.values();
                return getOverrideKey(key, definition, Arrays.asList(flags));
            } else if ("{RegionType}".equals(definition)) {
                final ArenaRegion.RegionType[] types = ArenaRegion.RegionType.values();
                return getOverrideKey(key, definition, Arrays.asList(types));
            } else if ("{Boolean}".equals(definition)) {
                final List<String> values = new ArrayList<String>();
                values.addAll(StringParser.negative);
                values.addAll(StringParser.positive);
                for (final String val : values) {
                    if (val.equals(key)) {
                        return definition;
                    }
                }
                return key;
            } else if ("{int}".equals(definition)) {
                try {
                    final int i = Integer.parseInt(key);
                    return definition;
                } catch (final NumberFormatException e) {
                    return key;
                }
            } else if ("{PotionEffectType}".equals(definition)) {
                final PotionEffectType[] pet = PotionEffectType.values();

                for (final PotionEffectType val : pet) {
                    if (val.getName().equals(key)) {
                        return definition;
                    }
                }
                return key;
            } else if ("{EntityType}".equals(definition)) {
                final EntityType[] entityTypes = EntityType.values();
                return getOverrideKey(key, definition, Arrays.asList(entityTypes));
            }
        }
        return key;
    }

    /**
     * Find a key inside a List of Enum
     *
     * @param key        the key to find
     * @param definition the node definition ("{Enum}")
     * @param list       the Enum list to search
     * @return the definition if found, the key, if not
     */
    private static String getOverrideKey(final String key, final String definition, final List<? extends Enum> list) {
        for (final Enum e : list) {
            if (e.name().equals(key)) {
                return definition;
            }
        }
        return key;
    }
}