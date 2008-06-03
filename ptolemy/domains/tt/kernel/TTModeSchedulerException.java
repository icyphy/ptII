package ptolemy.domains.tt.kernel;

/**
 * Scheduler Exception that occurs in the Time Triggered Domain.
 * 
 * @author Patricia Derler
 * 
 */
public class TTModeSchedulerException extends Exception {

	/**
	 * create a new mode scheduler exception.
	 * 
	 * @param string Message describing the cause of the exception.
	 */
	public TTModeSchedulerException(String string) {
		super(string);
	}

}
