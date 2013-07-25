package us.pixelmon.installer;

public class Utils {
	public static boolean isNix() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("nix") ||
			os.contains("nux")  ||
			os.contains("aix")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return true;
		}
		else {
			return false;
		}
	}
}
