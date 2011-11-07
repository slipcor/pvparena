package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.powerups.Powerup;

import org.bukkit.entity.Player;

/*
 * custom runnable class
 * 
 * author: slipcor
 * 
 * version: v0.3.4 - Powerups!!
 * 
 * history:
 * 
 *     v0.3.4 - Powerups!!
 */

public class PowerupManager {
	public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
	public List<Powerup> puTotal = new ArrayList<Powerup>(); 
	@SuppressWarnings("unchecked")
	public PowerupManager(HashMap<String, Object> powerUps) {
		// receives the config map, adds all plugins
		PVPArenaPlugin.instance.log.info("initialising powerupmanager");
		Powerup p;
		for (String pName : powerUps.keySet()) {
			PVPArenaPlugin.instance.log.info("reading powerUps");
			p = new Powerup(pName, (HashMap<String, Object>) powerUps.get(pName));
			puTotal.add(p);
		}
	}
	public void tick() {
		for(Powerup p : puActive.values()) {
			if (p.canBeTriggered()) {
				p.tick();
			}
		}
	}
}
/*
 * example config layout
 * 
    Shield:
        item: OBSIDIAN
        dmg_receive:
            factor: 0.6
    Minions:
        item: BONE
        spawn_mob:
            type: skeleton
            health: 2.0
        spawn_mob:
            type: skeleton
            duration: 10s
    Sprint:
        item: FEATHER
        sprint:
            duration: 10s
    QuadDamage:
        item: IRON_INGOT
        dmg_cause:
            factor: 4
            duration: 10s
    Dodge:
        item: IRON_DOOR
        dmg_receive:
            chance: 0.2
            factor: 0
            duration: 5s
    Reflect:
        item: WOOD_DOOR
        dmg_reflect:
            chance: 0.5
            factor: 0.3
            uses: 5
    Ignite:
        item: FLINT_AND_STEEL
        ignite:
            chance: 0.66
            duration: 10s
    IceBlock:
        item: ICE
        freeze:
            factor: 0
            duration: 8s
        dmg_receive:
            factor: 0
            duration: 8s
    Invulnerability:
        item: EGG
        dmg_receive:
            factor:0
            duration 5s
    OneUp:
        item: BROWN_MUSHROOM
        lives:
            diff: 1
    Death:
        item: RED_MUSHROOM
        lives:
            diff: -1
    Slippery:
        item: WATER_BUCKET
        slip:
            duration: 10s
    Dizzyness:
        item: COMPASS
        portal:
            duration: 10s
    Rage:
        item: ROTTEN_FLESH
        dmg_cause:
            factor: 1.5
            chance: 0.8
            duration: 5s
        dmg_cause:
            factor: 0
            chance: 0.2
            duration: 5s
    Berserk:
        item: CACTUS
        dmg_cause:
            factor: 1.5
            duration: 5s
        dmg_receive:
            dactor: 1.5
            duration: 5s
    Healing:
        item: APPLE
        heal:
            factor: 1.5
            duration: 10s
    Heal:
        item: BREAD
        health:
            diff: 3
    Repair:
        item: WORKBENCH
        repair:
            items: helmet,chestplate,leggins,boots
            factor: 0.2
 *  
 */