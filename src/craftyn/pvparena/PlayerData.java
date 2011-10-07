package craftyn.pvparena;
/*
 * PlayerData class
 * 
 * author: craftyn
 * editor: slipcor
 * 
 * version: v0.0.0 - copypaste
 * 
 * history:
 * 		v0.0.0 - copypaste
 */

public class PlayerData {
	private String fightClass = "none";
	private String team = "none";

	public String getFightClass() {
		return this.fightClass;
	}

	public void setFightClass(String t) {
		this.fightClass = t;
	}

	public String getTeam() {
		return this.team;
	}

	public void setTeam(String t) {
		this.team = t;
	}
}
