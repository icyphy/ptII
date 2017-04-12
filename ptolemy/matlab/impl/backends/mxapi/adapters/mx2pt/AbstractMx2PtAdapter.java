package ptolemy.matlab.impl.backends.mxapi.adapters.mx2pt;

import ptolemy.matlab.impl.backends.mxapi.PtMatlab;

/**
 * An abstract class for all the adapters of Mx object into PtolemyII objects. It provides them with a read {@link PtMatlab} field to 
 * perform the adaption routines upon.
 * 
 * @author david
 *
 */
public class AbstractMx2PtAdapter {
	
	final PtMatlab ptMatlab;

	public AbstractMx2PtAdapter(PtMatlab ptMatlab) {
		super();
		this.ptMatlab = ptMatlab;
	}

}
