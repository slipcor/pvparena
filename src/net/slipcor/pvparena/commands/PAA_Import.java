package net.slipcor.pvparena.commands;

import java.io.File;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.importer.Importer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * <pre>PVP Arena INSTALL Command class</pre>
 * 
 * A command to install modules
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PAA_Import extends AbstractGlobalCommand {
	private static Debug debug = new Debug(111);

	public PAA_Import() {
		super(new String[0]);
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}

		if (!argCountValid(sender, args,
				new Integer[]{0,1})) {
			return;
		}

		// pa import
		// pa import [arenaname]

		

		if (args.length == 1) {
			final YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(PVPArena.instance.getDataFolder().getPath() + "/config_"+args[0]+".yml");
				Importer.commitImport(args[0], config);
				Arena.pmsg(sender, Language.parse(MSG.IMPORT_DONE, args[0]));
			} catch (Exception e) {
				Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_NOTFOUND, args[0]));
				return;
			}
			
			return;
		}

		debug.i("importing arenas...", sender);
		try {
			final File path = PVPArena.instance.getDataFolder();
			final File[] fileArray = path.listFiles();
			int position;
			for (position = 0; position < fileArray.length; position++) {
				if (!fileArray[position].isDirectory() && fileArray[position].getName().contains("config_")) {
					String sName = fileArray[position].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					
					final YamlConfiguration config = new YamlConfiguration();
					try {
						config.load(fileArray[position]);
						Importer.commitImport(sName, config);
						Arena.pmsg(sender, Language.parse(MSG.IMPORT_DONE, sName));
					} catch (Exception e) {
						Arena.pmsg(sender, Language.parse(MSG.ERROR_ERROR, sName));
						e.printStackTrace();
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.IMPORT));
	}
}
