package ptolemy.matlab.impl.backends.jmathlib.adapters.jml2pt;

import jmathlib.core.tokens.CharToken;
import jmathlib.core.tokens.DataToken;
import jmathlib.core.tokens.MatrixToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.MatlabObjectInfo;
import ptolemy.matlab.impl.engine.adaption.ToPtObjectAdapter;

/**
 * @author david
 *
 */
public class Jml2PtStringMatrixAdapter implements ToPtObjectAdapter<ArrayToken> {

	@Override
	public ArrayToken adapt(MatlabObject matlabObject,final MatlabObjectInfo matlabType)
			throws IllegalActionException {
		final JMLObject jmlObject = (JMLObject) matlabObject;
		return adaptFromJmlToken(jmlObject.getJmlToken());
	}

	private static ArrayToken adaptFromJmlToken(DataToken jmlToken) throws IllegalActionException {
		final int nRows = jmlToken.getSizeY();
		final int nCols = jmlToken.getSizeX();
		int n = 0;
		final Token[] ptStringTokenArray = new Token[nRows * nCols]; // expected 1xN or Nx1 anyway.
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				CharToken jmElement = getElementAt(jmlToken, i, j);
				ptStringTokenArray[n++] = new StringToken(jmElement.getValue());
			}
		}
		return new ArrayToken(ptStringTokenArray);
	}

	private static CharToken getElementAt(DataToken jmlToken, int i, int j) throws IllegalActionException {
		CharToken jmlArrayElement;
		if (jmlToken instanceof CharToken) {
			jmlArrayElement = getElementFrom2DimCharToken((CharToken) jmlToken, i, j);
		} else if (jmlToken instanceof MatrixToken) {
			jmlArrayElement = getElementFrom2DimMatrixToken((MatrixToken) jmlToken, i, j);					
		} else {
			throw new IllegalActionException("cannot adapt into Ptolemy ArrayToken from JML type = " + jmlToken.getClass().getSimpleName());
		}
		return jmlArrayElement;
	}

	private static CharToken getElementFrom2DimCharToken(CharToken jmlToken, int i, int j) {
		CharToken jmlArrayElement = (CharToken) jmlToken.getElement(i, j);
		return jmlArrayElement;
	}

	private static CharToken getElementFrom2DimMatrixToken(final MatrixToken jmlToken, int i, int j) {
		final CharToken jmlArrayElement = (CharToken) jmlToken.elementAt(i, j);
		return jmlArrayElement;
	}

}
