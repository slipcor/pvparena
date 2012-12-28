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

public class PAA_Import extends PA__Command {
	private static Debug db = new Debug(111);

	public PAA_Import() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
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
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(PVPArena.instance.getDataFolder().getPath() + "/config_"+args[0]+".yml");
				Importer.commitImport(args[0], config);
				Arena.pmsg(sender, Language.parse(MSG.IMPORT_DONE, args[0]));
			} catch (Exception e) {
				Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_NOTFOUND, args[0]));
				e.printStackTrace();
				return;
			}
			
			return;
		}

		db.i("importing arenas...", sender);
		try {
			File path = PVPArena.instance.getDataFolder();
			File[] f = path.listFiles();
			int i;
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory() && f[i].getName().contains("config_")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					
					YamlConfiguration config = new YamlConfiguration();
					try {
						config.load(f[i]);
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
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.IMPORT));
	}
}
