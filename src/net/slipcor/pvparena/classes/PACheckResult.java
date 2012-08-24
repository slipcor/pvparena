package net.slipcor.pvparena.classes;

/**
 * 
 * This class represents a result for a looped check.
 * 
 * It is repeatedly handed while iterating several checks, returning
 * itself or the recent used PACheckResult, based on priority.
 * 
 * @author slipcor
 *
 */

public class PACheckResult {
	private int priority = 0;
	private String error = null;
	private String modName = null;
	
	/**
	 * 
	 * @return true if there was an error
	 */
	public boolean hasError() {
		return error != null;
	}
	
	/**
	 * 
	 * @return the error message
	 */
	public String getError() {
		return error;
	}

	/**
	 * 
	 * @return the module name triggering the current result
	 */
	public String getModName() {
		return modName;
	}
	
	/**
	 * 
	 * @return the PACR priority
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * set the error message
	 * @param error the error message
	 */
	public void setError(String error) {
		this.error = error;
		this.priority += 1000;
	}

	
	/**
	 * set the module name
	 * @param error the module name
	 */
	public void setModName(String modName) {
		this.modName = modName;
	}
	
	/**
	 * set the priority
	 * @param priority the priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
