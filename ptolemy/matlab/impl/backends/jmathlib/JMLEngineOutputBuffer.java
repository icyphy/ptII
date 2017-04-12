package ptolemy.matlab.impl.backends.jmathlib;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import jmathlib.core.interfaces.JMathLibOutput;

/**
 * @author david
 *
 */
public class JMLEngineOutputBuffer implements JMathLibOutput, Iterable<String> {
	
	private final static int DEFAULT_LINE_SIZE_IN_BYTES = 80;
	
	private final BlockingQueue<String> outputLines;
	
	public JMLEngineOutputBuffer(final int maxSizeInBytes) {
		 this(maxSizeInBytes,DEFAULT_LINE_SIZE_IN_BYTES);
	}
	
	public JMLEngineOutputBuffer(final int maxSizeInBytes, final int lineSizeInBytes) {
		 outputLines = new ArrayBlockingQueue<String>(maxSizeInBytes/lineSizeInBytes);
	}

	/**
	 * @see jmathlib.core.interfaces.JMathLibOutput#displayText(java.lang.String)
	 */
	@Override
	public void displayText(String text) {
		if (outputLines.remainingCapacity() < 1) {
			outputLines.poll();
		}
		outputLines.add(text);
	}

	/**
	 * @see jmathlib.core.interfaces.JMathLibOutput#setStatusText(java.lang.String)
	 */
	@Override
	public void setStatusText(String text) {
		displayText(text);
	}

	@Override
	public Iterator<String> iterator() {
		return this.outputLines.iterator();
	}
	
	
}
