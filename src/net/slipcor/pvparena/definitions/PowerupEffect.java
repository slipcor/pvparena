package net.slipcor.pvparena.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Ends;
import net.slipcor.pvparena.managers.Players;

/**
 * powerup effect class
 * 
 * -
 * 
 * contains definitions and values of a powerup effect
 * 
 * @author slipcor
 * 
 * @version v0.6.35
 * 
 */

public class PowerupEffect {
	protected boolean active = false;
	protected int uses = -1;
	protected int duration = -1;
	protected classes type = null;
	protected String mobtype = null;
	private double factor = 1.0;
	private double chance = 1.0;
	private int diff = 0;
	private List<String> items = new ArrayList<String>();
	private Debug db = new Debug(17);
	private PotionEffect potEff = null;

	/**
	 * PowerupEffect classes
	 */
	public static enum classes {
		DMG_CAUSE, DMG_RECEIVE, DMG_REFLECT, FREEZE, HEAL, HEALTH, IGNITE, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT, JUMP, POTEFF;
	}

	/**
	 * PowerupEffect instant classes (effects that activate when collecting)
	 */
	public static enum instants {
		FREEZE, HEALTH, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT, POTEFF;
	}

	/**
	 * create a powerup effect class
	 * 
	 * @param eClass
	 *            the effect class to create
	 * @param puEffectVals
	 *            the map of effect values to set/add
	 */
	public PowerupEffect(String eClass, HashMap<String, Object> puEffectVals,
			PotionEffect effect) {
		db.i("adding effect " + eClass);
		this.type = parseClass(eClass);
		this.potEff = effect;

		db.i("effect class is " + type.toString());
		for (Object evName : puEffectVals.keySet()) {
			if (evName.equals("uses")) {
				this.uses = (Integer) puEffectVals.get(evName);
				db.i("uses :" + String.valueOf(uses));
			} else if (evName.equals("duration")) {
				this.duration = (Integer) puEffectVals.get(evName);
				db.i("duration: " + String.valueOf(duration));
			} else if (evName.equals("factor")) {
				this.factor = (Double) puEffectVals.get(evName);
				db.i("factor: " + String.valueOf(factor));
			} else if (evName.equals("chance")) {
				this.chance = (Double) puEffectVals.get(evName);
				db.i("chance: " + String.valueOf(chance));
			} else if (evName.equals("diff")) {
				this.diff = (Integer) puEffectVals.get(evName);
				db.i("diff: " + String.valueOf(diff));
			} else if (evName.equals("items")) {
				this.items.add((String) puEffectVals.get(evName));
				db.i("items: " + items.toString());
			} else if (evName.equals("type")) {
				// mob type
				this.mobtype = (String) puEffectVals.get(evName);
				db.i("type: " + type.name());
			} else {
				db.w("undefined effect class value: " + evName);
			}
		}
	}

	/**
	 * get the PowerupEffect class from name
	 * 
	 * @param s
	 *            the class name
	 * @return a powerup effect
	 */
	public static classes parseClass(String s) {
		for (classes c : classes.values()) {
			if (c.name().equalsIgnoreCase(s))
				return c;
			if (s.toUpperCase().startsWith("POTION.")) {
				return classes.POTEFF;
			}
		}
		return null;
	}

	/**
	 * initiate PowerupEffect
	 * 
	 * @param player
	 *            the player to commit the effect on
	 */
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
		if (potEff != null) {
			player.addPotionEffect(potEff);
		}
	}

	/**
	 * remove PowerupEffect Potion Effect from player
	 * 
	 * @param player
	 *            the player to clear
	 */
	public void removeEffect(Player player) {
		if (potEff != null) {
			player.removePotionEffect(potEff.getType());
		}
	}

	/**
	 * commit PowerupEffect in combat
	 * 
	 * @param attacker
	 *            the attacking player to access
	 * @param defender
	 *            the defending player to access
	 * @param event
	 *            the triggering event
	 */
	public void commit(Player attacker, Player defender,
			EntityDamageByEntityEvent event) {
		db.i("committing entitydamagebyentityevent: " + this.type.name());
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
			if (attacker == null) {
				return;
			}
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				EntityDamageByEntityEvent reflectEvent = new EntityDamageByEntityEvent(
						defender, attacker, event.getCause(),
						(int) Math.round(event.getDamage() * factor));
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

	/**
	 * commit PowerupEffect on player
	 * 
	 * @param player
	 *            the player to commit the effect on
	 * @return true if the commit succeeded, false otherwise
	 */
	public boolean commit(Player player) {

		db.i("committing " + this.type.name());
		Random r = new Random();
		if (r.nextFloat() <= chance) {
			if (this.type == classes.HEALTH) {
				if (diff > 0) {
					player.setHealth(player.getHealth() + diff);
				} else {
					player.setHealth((int) Math.round(player.getHealth()
							* factor));
				}
				return true;
			} else if (this.type == classes.LIVES) {
				int lives = Arenas.getArenaByPlayer(player).paLives.get(player
						.getName());
				if (lives > 0) {
					Arenas.getArenaByPlayer(player).paLives.put(
							player.getName(), lives + diff);
				} else {
					Arena arena = Arenas.getArenaByPlayer(player);

					// pasted from onEntityDeath;

					String sTeam = Players.getTeam(player);

					Announcement.announce(arena, Announcement.type.LOSER,
							Language.parse("killedby", player.getName(),
									Players.parseDeathCause(arena, player,
											DamageCause.MAGIC, player)));
					Players.tellEveryone(arena, Language.parse("killedby",
							arena.colorizePlayerByTeam(player, sTeam) + ChatColor.YELLOW,
							Players.parseDeathCause(arena, player,
									DamageCause.MAGIC, player)));
					Players.parsePlayer(player).losses++;
					// needed so player does not get found when dead
					arena.removePlayer(player, "lose");
					Players.setTeam(player, "");

					Ends.checkAndCommit(arena);
				}

				return true;
			} else if (this.type == classes.PORTAL) {
				// player.set
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

	/**
	 * commit PowerupEffect on health gain
	 * 
	 * @param event
	 *            the triggering event
	 */
	public void commit(EntityRegainHealthEvent event) {
		db.i("committing entityregainhealthevent " + this.type.name());
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setAmount((int) Math.round(event.getAmount() * factor));
				((Player) event.getEntity()).setSaturation(20);
				((Player) event.getEntity()).setFoodLevel(20);
			} // else: chance fail :D
		} else {
			db.w("unexpected fight heal effect: " + this.type.name());
		}
	}

	/**
	 * commit PowerupEffect on velocity event
	 * 
	 * @param event
	 *            the triggering event
	 */
	public void commit(PlayerVelocityEvent event) {
		db.i("committing velocityevent " + this.type.name());
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setVelocity(event.getVelocity().multiply(factor));
			} // else: chance fail :D
		} else {
			db.w("unexpected jump effect: " + this.type.name());
		}
	}

	/**
	 * Get the PotionEffect of a PotionEffect class string
	 * 
	 * @param eClass
	 *            the class string to parse
	 * @return the PotionEffect or null
	 */
	public static PotionEffect parsePotionEffect(String eClass) {
		eClass = eClass.replace("POTION.", "");

		// POTION.BLA:1 <--- duration
		// POTION.BLA:1:1 <--- amplifyer

		int duration = 1;
		int amplifyer = 1;

		if (eClass.contains(":")) {
			String[] s = eClass.split(":");

			eClass = s[0];
			try {
				duration = Integer.parseInt(s[1]);
			} catch (Exception e) {
				Language.log_warning("warn",
						"invalid duration for PotionEffect " + eClass);
			}

			if (s.length > 2) {

				try {
					amplifyer = Integer.parseInt(s[2]);
				} catch (Exception e) {
					Language.log_warning("warn",
							"invalid duration for PotionEffect " + eClass);
				}
			}
		}

		for (PotionEffectType pet : PotionEffectType.values()) {
			if (pet.getName().equals(eClass)) {
				return new PotionEffect(pet, duration, amplifyer);
			}
		}
		return null;
	}
}
