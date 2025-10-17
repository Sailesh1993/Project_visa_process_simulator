package MVC.simu.framework;

/**
 * Utility class for managing trace-level logging within the simulation framework.
 * <p>
 * Provides simple console output for debugging and progress tracking,
 * controlled by a {@link Level trace level}.
 * </p>
 */
public class Trace {

    /**
     * Defines available levels of tracing detail.
     */
	public enum Level { INFO, WAR, ERR }

	private static Level traceLevel;

    /**
     * Sets the current trace level. Messages below this level will be suppressed.
     *
     * @param lvl the minimum {@link Level} to be printed
     */
	public static void setTraceLevel(Level lvl){
		traceLevel = lvl;
	}

    /**
     * Prints a trace message to the console if its level meets or exceeds
     * the current {@link #traceLevel}.
     *
     * @param lvl the level of this message
     * @param txt the message to output
     */
    public static void out(Level lvl, String txt){
		if (lvl.ordinal() >= traceLevel.ordinal()){
			System.out.println(txt);
		}
	}

    /**
     * Formats simulation time values to two decimal places for consistent output.
     *
     * @param t the time value to format
     * @return a formatted string representing the time
     */
    public static String formatTime(double t) {
        return String.format("%.2f", t);
    }
}
