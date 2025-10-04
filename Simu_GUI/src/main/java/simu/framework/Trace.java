package simu.framework;

public class Trace {
	public enum Level { INFO, WAR, ERR }
	private static Level traceLevel;
	
	public static void setTraceLevel(Level lvl){
		traceLevel = lvl;
	}

    public static void out(Level lvl, String txt){
		if (lvl.ordinal() >= traceLevel.ordinal()){
			System.out.println(txt);
		}
	}

    public static String formatTime(double t) {
        return String.format("%.2f", t);
    }
}
