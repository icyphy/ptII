package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.CharToken;
import jmathlib.core.tokens.DataToken;
import jmathlib.core.tokens.MatrixToken;
import jmathlib.core.tokens.OperandToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Jml2PtStringAdapter implements ToPtObjectAdapter<StringToken> {

	@Override
	public StringToken adapt(MatlabObject matlabObject, MatlabObjectInfo matlabType)
			throws IllegalActionException {
		final DataToken jmlToken = ((JMLObject) matlabObject).getJmlToken();
		if (jmlToken instanceof CharToken) {
			return adaptFromCharToken((CharToken) jmlToken);
		} else if (jmlToken instanceof MatrixToken) {
			return adaptFromMatrixToken((MatrixToken) jmlToken);
		}
		throw new IllegalActionException("cannot adapt from JML type = " + jmlToken.getClass().getSimpleName());
	}

	private static StringToken adaptFromMatrixToken(MatrixToken matrixToken) throws IllegalActionException {
		if (matrixToken.getSizeX() > 1 || matrixToken.getSizeY() > 1) {
			throw new IllegalActionException("cannot adapt into a single string from an MxN array or matrix of strings with M or N greater than one");
		}
		final OperandToken matrixElement = matrixToken.elementAt(0, 0);
		if (!(matrixElement instanceof CharToken)) {
			throw new IllegalActionException("cannot adapt into a single string from an array or matrix of " + matrixElement.getClass().getSimpleName() + "'s");			
		}
		return new StringToken(((CharToken) matrixElement).getValue());
	}

	private static StringToken adaptFromCharToken(final CharToken charToken) throws IllegalActionException {
		if (charToken.getSizeY() > 1) {
			throw new IllegalActionException("cannot adapt into a single string from an MxN array or matrix of strings with M or N greater than one");
		}
		return new StringToken(charToken.getValue());
	}

}
