package net.slipcor.pvparena.powerups;

/*
 * powerup effect class
 * 
 * author: slipcor
 * 
 * version: v0.3.6 - CTF Arena
 * 
 * history:
 * 
 *     v0.3.5 - Powerups!!
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.StatsManager;

public class PowerupEffect {
	boolean active = false;
	int uses = -1;
	int duration = -1;
	double factor = 1.0;
	double chance = 1.0;
	classes type = null;
	String mobtype = null;
	String effectClass = null;
	int diff = 0;
	List<String> items = new ArrayList<String>();
	DebugManager db = new DebugManager();
	
	public static enum classes {
		DMG_CAUSE, DMG_RECEIVE, DMG_REFLECT, FREEZE, HEAL, HEALTH,
		IGNITE, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT, JUMP;
	}
	
	public static enum instants {
		FREEZE, HEALTH, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT;
	}
	
	public static classes parseClass(String s) {
		for (classes c : classes.values()) {
			if (c.name().equalsIgnoreCase(s))
				return c;
		}
		return null;
	}
	
	public PowerupEffect(String eClass, HashMap<String, Object> puEffectVals) {
		db.i("adding effect "+eClass);
		this.type = parseClass(eClass);

		db.i("effect class is "+type.toString());
		for (Object evName : puEffectVals.keySet()) {
			if (evName.equals("uses")) {
				this.uses = (Integer) puEffectVals.get(evName);
				db.i("uses :" +String.valueOf(uses));
			} else if (evName.equals("duration")) {
				this.duration = (Integer) puEffectVals.get(evName);
				db.i("duration: "+String.valueOf(duration));
			} else if (evName.equals("factor")) {
				this.factor = (Double) puEffectVals.get(evName);
				db.i("factor: "+String.valueOf(factor));
			} else if (evName.equals("chance")) {
				this.chance = (Double) puEffectVals.get(evName);
				db.i("chance: "+String.valueOf(chance));
			} else if (evName.equals("diff")) {
				this.diff = (Integer) puEffectVals.get(evName);
				db.i("diff: "+String.valueOf(diff));
			} else if (evName.equals("items")) {
				this.items.add((String) puEffectVals.get(evName));
				db.i("items: "+items.toString());
			} else if (evName.equals("type")) {
				// mob type
				this.mobtype = (String) puEffectVals.get(evName);
				db.i("type: "+type.name());
			} else {
				db.w("undefined effect class value: " + evName);
			}
		}
	}

	public void init(Player player) {
		if (uses == 0)
			return;
		else if (uses > 0) {
			active = true;
			uses--;
		} else {
			active = true;
		}
		
		db.i("initiating - " + type.name());
		
		if (duration == 0) {
			active = false;
		}
		for (instants i : instants.values()) {
			if (this.type.toString().equals(i.toString())) {
				// type is instant. commit!
				commit(player);
			}
		}
	}
	
	public void commit(Player attacker, Player defender, EntityDamageByEntityEvent event) {
		if (this.type == classes.DMG_RECEIVE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setDamage((int) Math.round(event.getDamage() * factor));
			} // else: chance fail :D
		} else if (this.type == classes.DMG_CAUSE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setDamage((int) Math.round(event.getDamage() * factor));
			} // else: chance fail :D
		} else if (this.type == classes.DMG_REFLECT) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				EntityDamageByEntityEvent reflectEvent = new EntityDamageByEntityEvent(defender, attacker, event.getCause(), (int) Math.round(event.getDamage() * factor));
				PVPArena.entityListener.onEntityDamageByEntity(reflectEvent);
			} // else: chance fail :D
		} else if (this.type == classes.IGNITE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				defender.setFireTicks(20);
			} // else: chance fail :D
		} else {
			db.w("unexpected fight powerup effect: " + this.type.name());
		}
	}
	
	public boolean commit(Player player) {

		Random r = new Random();
		if (r.nextFloat() <= chance) {
			if (this.type == classes.HEALTH) {
					if (diff > 0) {
						player.setHealth(player.getHealth()+diff);
					} else {
						player.setHealth((int) Math.round(player.getHealth()*factor));
					}
				return true;
			} else if (this.type == classes.LIVES) {
				byte lives = ArenaManager.getArenaByPlayer(player).fightUsersLives.get(player.getName());
				if (lives > 0)
					ArenaManager.getArenaByPlayer(player).fightUsersLives.put(player.getName(), (byte) (lives + diff));
				else {
					Arena arena = ArenaManager.getArenaByPlayer(player);
					
					// pasted from onEntityDeath;
					
					String sTeam = arena.fightUsersTeam.get(player.getName());
					String color = arena.fightTeams.get(sTeam);
					if (!color.equals("free")) {
						arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.valueOf(color) + player.getName() + ChatColor.YELLOW));
					} else {
						arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
					}
					StatsManager.addLoseStat(player, sTeam, arena);
					arena.fightUsersTeam.remove(player.getName()); // needed so player does not get found when dead
					arena.fightUsersRespawn.put(player.getName(), arena.fightUsersClass.get(player.getName()));
					
					arena.checkEnd();
				}
				
				return true;
			} else if (this.type == classes.PORTAL) {
				//player.set
				return true;
			} else if (this.type == classes.REPAIR) {
				for (String i : items) {
					ItemStack is = null;
					if (i.contains("HELM")) {
						is = player.getInventory().getHelmet();
					} else if ((i.contains("CHEST")) || (i.contains("PLATE"))) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("LEGGINS")) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("BOOTS")) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("SWORD")) {
						is = player.getItemInHand();
					}
					if (is == null)
						continue;
					
					if (diff > 0) {
						if (is.getDurability() + diff > Byte.MAX_VALUE)
							is.setDurability(Byte.MAX_VALUE);
						else
							is.setDurability((short) (is.getDurability() + diff));
					}
				}
				return true;
			} else if (this.type == classes.SPAWN_MOB) {
				return true;
			} else if (this.type == classes.SPRINT) {
				player.setSprinting(true);
				return true;
			}
		}
		db.w("unexpected " + this.type.name());
		return false;
	}

	public void commit(EntityRegainHealthEvent event) {
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setAmount((int) Math.round(event.getAmount() * factor));
				((Player)event.getEntity()).setSaturation(20);
				((Player)event.getEntity()).setFoodLevel(20);
			} // else: chance fail :D
		} else {
			db.w("unexpected fight heal effect: " + this.type.name());
		}
	}

	public void commit(PlayerVelocityEvent event) {
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setVelocity(event.getVelocity().multiply(factor));
			} // else: chance fail :D
		} else {
			db.w("unexpected jump effect: " + this.type.name());
		}
	}
}
