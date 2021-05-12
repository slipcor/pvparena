package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.slipcor.pvparena.core.Utils.getSerializableItemStacks;

/**
 * <pre>PVP Arena SET Command class</pre>
 * <p/>
 * A command to set config values
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Set extends AbstractArenaCommand {

    public PAA_Set() {
        super(new String[]{"pvparena.cmds.set"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2})) {
            return;
        }

        // args[0]
        // usage: /pa {arenaname} set [page]

        if (args.length < 2) {
            try {
                int page = Integer.parseInt(args[0]);

                page = page < 1 ? 1 : page;

                final Map<String, String> keys = new HashMap<>();

                int position = 0;

                for (final String node : arena.getArenaConfig().getYamlConfiguration()
                        .getKeys(true)) {
                    if (CFG.getByNode(node) == null) {
                        continue;
                    }
                    if (position++ >= (page - 1) * 10) {
                        final String[] split = node.split("\\.");
                        keys.put(node, split[split.length - 1]);
                    }
                    if (keys.size() >= 10) {
                        break;
                    }
                }
                arena.msg(sender, ChatColor.COLOR_CHAR + "6------ config list [" + page + "] ------");
                for (final Map.Entry<String, String> stringStringEntry : keys.entrySet()) {
                    arena.msg(sender,
                            stringStringEntry.getValue() + " => " + CFG.getByNode(stringStringEntry.getKey()).getType());
                }

            } catch (final Exception e) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, args[0]));
            }
            return;
        }

        // args[0]
        // usage: /pa {arenaname} set [node] [value]
        set(sender, arena, args[0], args[1]);
    }

    private void set(final CommandSender player, final Arena arena, final String node, final String value) {

        for (final String s : arena.getArenaConfig().getYamlConfiguration().getKeys(true)) {
            if (s.toLowerCase().endsWith('.' + node.toLowerCase())) {
                set(player, arena, s, value);
                return;
            }
        }

        final String type = CFG.getByNode(node) == null ? "" : CFG.getByNode(node).getType();


        if ("boolean".equals(type)) {
            if ("true".equalsIgnoreCase(value)) {
                arena.getArenaConfig().setManually(node, Boolean.TRUE);
                arena.msg(
                        player,
                        Language.parse(arena, MSG.SET_DONE, node,
                                String.valueOf("true".equalsIgnoreCase(value))));
            } else if ("false".equalsIgnoreCase(value)) {
                arena.getArenaConfig().setManually(node, Boolean.FALSE);
                arena.msg(
                        player,
                        Language.parse(arena, MSG.SET_DONE, node,
                                String.valueOf("true".equalsIgnoreCase(value))));
            } else {
                arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
                        "boolean (true|false)"));
                return;
            }
        } else if ("string".equals(type)) {
            arena.getArenaConfig().setManually(node, String.valueOf(value));
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_DONE, node,
                            String.valueOf(value)));
        } else if ("int".equals(type)) {
            final int iValue;

            try {
                iValue = Integer.parseInt(value);
            } catch (final Exception e) {
                arena.msg(player, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, value));
                return;
            }
            arena.getArenaConfig().setManually(node, iValue);
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_DONE, node,
                            String.valueOf(iValue)));
        } else if ("double".equals(type)) {
            final double dValue;

            try {
                dValue = Double.parseDouble(value);
            } catch (final Exception e) {
                arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
                        "double (e.g. 12.00)"));
                return;
            }
            arena.getArenaConfig().setManually(node, dValue);
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_DONE, node,
                            String.valueOf(dValue)));
        } else if ("tp".equals(type)) {
            if (!"exit".equals(value) && !"old".equals(value) && !"spectator".equals(value)) {
                arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
                        "tp (exit|old|spectator|...)"));
                return;
            }
            arena.getArenaConfig().setManually(node, value);
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_DONE, node, value));
        } else if ("material".equals(type)) {
            if ("hand".equals(value)) {
                if (player instanceof Player) {

                    String itemDefinition = ((Player) player).getEquipment().getItemInMainHand().getType().name();
                    arena.getArenaConfig().setManually(node, itemDefinition);
                    arena.msg(
                            player,
                            Language.parse(arena, MSG.SET_DONE, node,
                                    itemDefinition));
                } else {
                    arena.msg(player, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
                }
                return;
            }

            try {
                final Material mat = Material.valueOf(value.toUpperCase());
                if (mat != Material.AIR) {
                    arena.getArenaConfig().setManually(node, mat.name());
                    arena.msg(player, Language.parse(arena, MSG.SET_DONE, node, mat.name()));
                }

                arena.getArenaConfig().save();
                return;
            } catch (final Exception e) {
                arena.msg(player, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, value,
                        "valid ENUM or item ID"));
            }
            return;
        } else if ("items".equals(type)) {
            if ("hand".equals(value)) {
                if (player instanceof Player) {

                    ItemStack item = ((Player) player).getInventory().getItemInMainHand();
                    arena.getArenaConfig().setManually(node, getSerializableItemStacks(item));
                    arena.msg(
                            player,
                            Language.parse(arena, MSG.SET_DONE, node, item.getType().name()));
                    arena.getArenaConfig().save();
                } else {
                    arena.msg(player, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
                }
                return;
            }
            if ("inventory".equals(value)) {
                if (player instanceof Player) {

                    final ItemStack[] items = ((Player) player).getInventory().getContents();
                    arena.getArenaConfig().setManually(node, getSerializableItemStacks(items));
                    arena.msg(
                            player,
                            Language.parse(arena, MSG.SET_DONE, node, "inventory"));
                    arena.getArenaConfig().save();
                } else {
                    arena.msg(player, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
                }
                return;
            }
            arena.msg(player, Language.parse(arena, MSG.SET_ITEMS_NOT));
        } else {
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_UNKNOWN, node,
                            String.valueOf(value)));
            arena.msg(
                    player,
                    Language.parse(arena, MSG.SET_HELP, node,
                            String.valueOf(value)));
            return;
        }
        arena.getArenaConfig().save();
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.SET));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("set");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!s");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return CFG.getTabTree();
    }
}
