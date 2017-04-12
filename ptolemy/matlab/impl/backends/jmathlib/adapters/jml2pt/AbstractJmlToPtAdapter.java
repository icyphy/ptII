package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import ptolemy.data.Token;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class AbstractJmlToPtAdapter {

	protected ToPtObjectAdapter<Token> fallbackDelegate;
	
	public AbstractJmlToPtAdapter(final ToPtObjectAdapter<Token> fallbackDelegate) {
		super();
		this.fallbackDelegate = fallbackDelegate;
	}
	
}