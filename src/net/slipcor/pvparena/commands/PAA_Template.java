package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>PVP Arena TEMPLATE Command class</pre>
 * <p/>
 * A command to save config values as a template
 *
 * @author slipcor
 */

public class PAA_Template extends AbstractArenaCommand {

    public PAA_Template() {
        super(new String[]{});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!this.hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{2})) {
            return;
        }

        // pa {arena} template save|load [filename]

        File directory = new File(PVPArena.instance.getDataFolder(), "templates");
        File output = new File(directory, args[1] + ".temp");
        if (args[0].equalsIgnoreCase("save")) {
            try {
                output.createNewFile();
                arena.getArenaConfig().getYamlConfiguration().save(output);

                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(output);

                cfg.set("spawns", null);
                cfg.set("arenaregion", null);

                cfg.save(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
            arena.msg(sender, Language.parse(MSG.TEMPLATE_SAVE_DONE, args[1]));
        } else if (args[0].equalsIgnoreCase("load")) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(output);

            for (String key : cfg.getKeys(false)) {
                arena.getArenaConfig().getYamlConfiguration().set(key, cfg.get(key));
            }
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.TEMPLATE_LOAD_DONE, args[1]));
        } else {
            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[0], "load | save"));
        }
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.TEMPLATE));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("template");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!tmp");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"load"});
        result.define(new String[]{"save"});
        return result;
    }
}
