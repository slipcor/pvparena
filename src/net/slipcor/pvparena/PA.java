package net.slipcor.pvparena;

import java.util.Arrays;
import java.util.HashSet;

public class PA {
	public static HashSet<String> positive = new HashSet<String>(Arrays.asList("yes", "on", "true", "1"));
	public static HashSet<String> negative = new HashSet<String>(Arrays.asList("no", "off", "false", "0"));

	public static void resetStaticVariables() {
		positive = null;
		negative = null;
	}
}
