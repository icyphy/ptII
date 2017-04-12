package ptolemy.matlab.impl.utils;

/**
 * Represents a logging context, i.e a little logging domain with associated categories, names or conventions.
 * 
 * @author David Guardado Barcia
 *
 */
public interface LoggingContext {
	
	static final String ROOT_LOGGING_CATEGORY = "ptolemy.matlab";

	/**
	 * Returns a string representing the heading chunk of a log entry.
	 * @param key a identifier being supposedly meaningful in a log entry
	 */
	String logHeader(final int key);
	
	/**
	 * Returns a string representing the heading chunk of a log entry.
	 * @param key a identifier being supposedly meaningful in a log entry
	 */
	String logHeader(final long key);

	/**
	 * Returns a string representing the heading chunk of a log entry.
	 * @param key a identifier being supposedly meaningful in a log entry
	 */
	String logHeader(final String key);

	/**
	 * Returns a string representing the heading chunk of a log entry.
	 */
	String logHeader();
	
}
