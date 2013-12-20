/* A store for key-value pairs. */
package ptolemy.actor.lib;

import java.util.HashMap;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 A store for key-value pairs.
 This actor stores key-value pairs and provides an interface for retrieving
 them one at a time or in groups.
 
 @author Shuhei Emoto, Edward A. Lee, Kentaro Mizouchi
 @version $Id: TypedAtomicActor.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 10.1
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Dictionary extends TypedAtomicActor {

	public Dictionary(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		value = new TypedIOPort(this, "value", true, false);
		new SingletonParameter(value, "_showName").setExpression("true");
		
		writeKey = new TypedIOPort(this, "writeKey", true, false);
		new SingletonParameter(writeKey, "_showName").setExpression("true");
		
		readKey = new TypedIOPort(this, "readKey", true, false);
		new SingletonParameter(readKey, "_showName").setExpression("true");
		
		readKeyArray = new TypedIOPort(this, "readKeyArray", true, false);
		new SingletonParameter(readKeyArray, "_showName").setExpression("true");
		readKeyArray.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

		result = new TypedIOPort(this, "result", false, true);
		new SingletonParameter(result, "_showName").setExpression("true");
		result.setTypeSameAs(value);
		
		resultArray = new TypedIOPort(this, "resultArray", false, true);
		new SingletonParameter(resultArray, "_showName").setExpression("true");
		// FIXME: The length of the output array should match the length of the readKeyArray.
		// How to do that?
		resultArray.setTypeAtLeast(ArrayType.arrayOf(value));
		
		triggerKeys = new TypedIOPort(this, "triggerKeys", true, false);
		new SingletonParameter(triggerKeys, "_showName").setExpression("true");

		keys = new TypedIOPort(this, "keys", false, true);
		new SingletonParameter(keys, "_showName").setExpression("true");
		keys.setTypeAtLeast(ArrayType.arrayOf(writeKey));

		_store = new HashMap<Token, Token>();
	}
	
	/** Input port for providing a value to store in the dictionary.
	 *  The value will be stored only if a writeKey input arrives
	 *  at the same time. Otherwise, it will be discarded.
	 */
	public TypedIOPort value;
	
	/** An input that provides a key for a key-value pair to be stored
	 *  in the dictionary. If a key arrives on this port, but there is no
	 *  value on the value port or the value is nil, then the dictionary
	 *  entry with the specified key will be removed. Otherwise, the
	 *  value provided on the value port will be stored indexed by this key.
	 */
	public TypedIOPort writeKey;
	
	/** An input that provides a key for a value to be read from the dictionary.
	 *  If the dictionary does not contain any value corresponding to this
	 *  key, then the output will be a nil token.
	 */
	public TypedIOPort readKey;
	
	/** An input that provides an array of keys to be read simultaneously
	 *  from the dictionary. The output will be an array with the same length
	 *  as this input where each entry in the output array is the value corresponding
	 *  to the corresponding key in the input array. For any key that has no
	 *  entry in the dictionary, a nil token will be inserted in the output array.
	 */
	public TypedIOPort readKeyArray;
	
	/** An output providing the result of a single reading of the dictionary
	 *  via the readKey input port.
	 */
	public TypedIOPort result;

	/** An output providing the result of a multiple reading of the dictionary
	 *  via the readKeyArray input port.
	 */
	public TypedIOPort resultArray;
	
	/** Upon receiving any token at this port, this actor will produce
	 *  on the keys output an array containing all the keys of entries
	 *  in the dictionary. The order is arbitrary.
	 */
	public TypedIOPort triggerKeys;
	
	/** Upon receiving any token at the triggerKeys port, this actor will produce
	 *  on this output an array containing all the keys of entries
	 *  in the dictionary. The order is arbitrary.
	 */
	public TypedIOPort keys;

	/** If there is a writeKey input, then update the dictionary;
	 *  specifically, if there is also a value input, then insert
	 *  into the dictionary the key-value pair given by these two inputs.
	 *  Otherwise, or if the value input is a nil token, then delete
	 *  the dictionary entry corresponding to the key.
	 *  If there is a readKey input, then read the dictionary and
	 *  produce on the result output the entry corresponding to the key,
	 *  or a nil token if there is no such entry.
	 *  If there is a readKeyArray input, then read the dictionary and
	 *  produce on the resultArray output the entries corresponding to the keys,
	 *  with nil tokens inserted for any missing entry.
	 *  If there is a triggerKeys input, then produce on the keys output
	 *  an array containing all the keys in the dictionary, in arbitrary order.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		if (writeKey.getWidth() > 0 && writeKey.hasToken(0)) {
			Token theValue = value.get(0);
			Token theKey = writeKey.get(0);
			if (theValue == null || theValue.isNil()) {
				// Remove the entry.
				_store.remove(theKey);
			} else {
				_store.put(theKey, theValue);
			}
		} else if (value.getWidth() > 0 && value.hasToken(0)) {
			// Read and discard the input token so that DE doesn't refire me.
			value.get(0);
		}
		if (readKey.getWidth() > 0 && readKey.hasToken(0)) {
			Token theKey = readKey.get(0);
			Token theResult = _store.get(theKey);
			// NOTE: We choose to output a nil token if the result is not in the store.
			if (theResult != null) {
				result.send(0, theResult);
			} else {
				result.send(0, Token.NIL);
			}
		}
		if (readKeyArray.getWidth() > 0 && readKeyArray.hasToken(0)) {
			ArrayToken theKeys = (ArrayToken)readKeyArray.get(0);
			Token[] theResult = new Token[theKeys.length()];
			int i = 0;
			for (Token theKey : theKeys.arrayValue()) {
				theResult[i] = _store.get(theKey);
				if (theResult[i] == null) {
					theResult[i] = Token.NIL;
				}
				i++;
			}
			resultArray.send(0, new ArrayToken(value.getType(), theResult));
		}
		if (triggerKeys.getWidth() > 0 && triggerKeys.hasToken(0)) {
			// Must consume the trigger, or DE will fire me again.
			triggerKeys.get(0);
			Token[] result = new Token[_store.size()];
			int i = 0;
			for (Token token : _store.keySet()) {
				result[i] = token;
				i++;
			}
			keys.send(0,  new ArrayToken(result));
		}
	}
	
	/** Clear the dictionary.
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		_store.clear();
	}
	
	private HashMap<Token, Token> _store;
}
