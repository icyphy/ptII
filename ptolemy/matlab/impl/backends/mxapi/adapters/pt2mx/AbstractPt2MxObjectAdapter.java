package ptolemy.matlab.impl.backends.mxapi.adapters.pt2mx;

import ptolemy.data.Token;
import ptolemy.matlab.impl.backends.mxapi.PtMatlab;
import ptolemy.matlab.impl.engine.adaption.ToMatlabObjectAdapter;

/**
 * @author david
 *
 */
public abstract class AbstractPt2MxObjectAdapter<T extends Token>
	implements ToMatlabObjectAdapter<T> {

	protected ToMatlabObjectAdapter<Token> delegate;
	protected final PtMatlab ptMatlab;

	public AbstractPt2MxObjectAdapter(final ToMatlabObjectAdapter<Token> delegate,final PtMatlab ptMatlab) {
		this.delegate = delegate;
		this.ptMatlab = ptMatlab;
	}
	
}
