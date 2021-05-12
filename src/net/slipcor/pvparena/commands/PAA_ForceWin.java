package net.slipcor.pvparena.commands;

import com.google.common.collect.ImmutableMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class PAA_ForceWin extends AbstractArenaCommand {

    public PAA_ForceWin() {
        super(new String[]{"pvparena.cmds.forcewin"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1})) {
            return;
        }


        // /pa {arenaname} forcewin [playername]
        // /pa {arenaname} forcewin [teamname]

        if (Bukkit.getPlayer(args[0]) == null && arena.isFreeForAll()) {
            arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[0]));
        } else if (Bukkit.getPlayer(args[0]) == null) {
            ArenaTeam aTeam = arena.getTeam(args[0]);
            if (aTeam == null) {
                arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[0]));
                arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[0]));
                return;
            }
            // existing team
            for (final ArenaTeam team : arena.getTeams()) {
                if (team.getName().equalsIgnoreCase(aTeam.getName())) {
                    // skip winner
                    continue;
                }
                for (final ArenaPlayer ap : team.getTeamMembers()) {
                    if (ap.getStatus() == ArenaPlayer.Status.FIGHT) {
                        ap.get().getWorld().strikeLightningEffect(ap.get().getLocation());
                        final EntityDamageEvent e = new EntityDamageEvent(ap.get(), EntityDamageEvent.DamageCause.LIGHTNING,
                                10.0);
                        PlayerListener.finallyKillPlayer(arena, ap.get(), e);
                    }
                }
            }
        } else {
            // existing player name
            ArenaPlayer aplayer = ArenaPlayer.parsePlayer(Bukkit.getPlayer(args[0]).getName());
            if (!arena.equals(aplayer.getArena())) {
                arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[0]));
                return;
            }
            if (arena.isFreeForAll()) {
                for (ArenaPlayer ap : arena.getFighters()) {
                    if (ap.equals(aplayer)) {
                        continue;
                    }
                    if (ap.getStatus() == ArenaPlayer.Status.FIGHT) {
                        ap.get().getWorld().strikeLightningEffect(ap.get().getLocation());
                        final EntityDamageEvent e = new EntityDamageEvent(ap.get(), EntityDamageEvent.DamageCause.LIGHTNING,
                                10.0);
                        PlayerListener.finallyKillPlayer(arena, ap.get(), e);
                    }
                }
            } else {
                for (final ArenaTeam team : arena.getTeams()) {
                    if (team.getName().equalsIgnoreCase(aplayer.getArenaTeam().getName())) {
                        // skip winner
                        continue;
                    }
                    for (final ArenaPlayer ap : team.getTeamMembers()) {
                        if (ap.getStatus() == ArenaPlayer.Status.FIGHT) {
                            ap.get().getWorld().strikeLightningEffect(ap.get().getLocation());
                            final EntityDamageEvent e = new EntityDamageEvent(ap.get(), EntityDamageEvent.DamageCause.LIGHTNING,
                                    10.0);
                            PlayerListener.finallyKillPlayer(arena, ap.get(), e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.FORCEWIN));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("forcewin");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!fw");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        for (ArenaTeam team : arena.getTeams()) {
            result.define(new String[]{team.getName()});
        }
        result.define(new String[]{"{Player}"});
        return result;
    }
}
