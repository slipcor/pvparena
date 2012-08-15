package net.slipcor.pvparena.classes;

public class PACheckResult {
	private int priority = 0;
	private String error = null;
	private String modName = null;
	
	public boolean hasError() {
		return error != null;
	}
	
	public String getError() {
		return error;
	}

	public String getModName() {
		return modName;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setError(String error) {
		this.error = error;
		this.priority += 1000;
	}

	public void setModName(String modName) {
		this.modName = modName;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
