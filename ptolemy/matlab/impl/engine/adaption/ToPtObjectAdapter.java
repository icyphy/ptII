package ptolemy.matlab.impl.engine.adaption;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * An adapter to convert Matlab objects into Ptolemy II objects.
 * 
 * @author David Guardado Barcia
 *
 */
public interface ToPtObjectAdapter <T extends Token> {
	/**
	 * Converts a Matlab object into a Ptolemy II object.
	 * @param matlabObject Matlab object
	 * @param matlabObjectInfo infoset about the object (type, dimensions, etc)
	 * @return an instance of {@link Token}
	 * @throws IllegalActionException
	 */
	T adapt(final MatlabObject matlabObject, final MatlabObjectInfo matlabObjectInfo) throws IllegalActionException;
}
