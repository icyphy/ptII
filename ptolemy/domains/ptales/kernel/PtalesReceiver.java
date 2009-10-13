package ptolemy.domains.ptales.kernel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

// FIXME: For now, support arrays up to four dimensions only.

public class PtalesReceiver extends SDFReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do nothing.
     *  @exception IllegalActionException If clear() is not supported by
     *   the domain.
     */
    public void clear() {
        // Ignore
    }

    /** Return a list with tokens that are currently in the receiver
     *  available for get() or getArray(). The oldest token (the one
     *  that was put first) should be listed first in any implementation
     *  of this method.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException If the operation is not supported.
     */
    public List<Token> elementList() {
        // FIXME: implement this.
        return new LinkedList();
    }

    /** Get a token from this receiver.
     *  @return A token read from the receiver.
     *  @exception NoTokenException If there is no token.
     */
    public Token get() throws NoTokenException {
        Token result = _buffer[_readPointer[0]][_readPointer[1]][_readPointer[2]][_readPointer[3]];
        _incrementReadPointer(1);
        return result;
    }

    /** Get an array of tokens from this receiver. The <i>numberOfTokens</i>
     *  argument specifies the number of tokens to get. In an implementation,
     *  the length of the returned array must be equal to
     *  <i>numberOfTokens</i>.
     *  @param numberOfTokens The number of tokens to get in the
     *   returned array.
     *  @return An array of tokens read from the receiver.
     *  @exception NoTokenException If there are not <i>numberOfTokens</i>
     *   tokens.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        // FIXME: implement
        return null;
    }
    
    /** Return true.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasToken() {
        return true;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasToken(int numberOfTokens) {
        return true;
    }

    /** Return true.
     *  @return True.
     */
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void put(Token token) {
        _buffer[_writePointer[0]][_writePointer[1]][_writePointer[2]][_writePointer[3]] = token;
        _incrementWritePointer(1);
    }

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver.  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        // FIXME: Implement.
    }

    /** Put a sequence of tokens to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param tokens The sequence of token to put.
     *  @param numberOfTokens The number of tokens to put (the array might
     *   be longer).
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putArrayToAll(Token[] tokens, int numberOfTokens,
            Receiver[] receivers) throws NoRoomException,
            IllegalActionException {
        // FIXME: Implement.
    }

    /** Put a single token to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param token The token to put.
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        for (Receiver receiver : receivers) {
            receiver.put(token);
        }
    }

    /** Reset this receiver to its initial state, which is typically
     *  either empty (same as calling clear()) or unknown.
     *  @exception IllegalActionException If reset() is not supported by
     *   the domain.
     */
    public void reset() throws IllegalActionException {
        for (int i = 0; i < _NUMBER_OF_DIMENSIONS; i++) {
            _readPointer[i] = 0;
            _writePointer[i] = 0;
        }
    }

    /** Specify the pattern in which data is read from the receiver.
     *  A side effect of this method is to set the capacity of the receiver.
     *  @param readSpec Number of tokens read per firing by dimension.
     *  @param firingCounts Firing counts by dimension.
     *  @param dimensions List of all the dimensions in the system.
     *  @throws IllegalActionException If setting the capacity fails.
     */
    public void setReadPattern(
            LinkedHashMap<String,Integer> readSpec, 
            Map<String,Integer> firingCounts,
            LinkedHashSet<String> dimensions) 
            throws IllegalActionException {
        _dimensions = dimensions;
        if (dimensions.size() > _NUMBER_OF_DIMENSIONS) {
            throw new IllegalActionException(getContainer(),
                    "FIXME: Number of dimensions must be "
                    + _NUMBER_OF_DIMENSIONS
                    + " or fewer, for now.");
        }
        int i = 0;
        for(String dimension : dimensions) {
            Integer size = readSpec.get(dimension);
            if (size == null) {
                size = _ONE;
            }
            if (size.intValue() <= 0) {
                throw new IllegalActionException(getContainer(),
                        "Dimension size is required to be strictly greater than zero.");                
            }
            _sizes[i] = size.intValue() * firingCounts.get(dimension).intValue();
            i++;
        }
        // In case there are fewer dimensions than supported,
        // fill in the remaining dimensions with ones.
        while (i < _NUMBER_OF_DIMENSIONS) {
            _sizes[i] = 1;
            i++;
        }
        if (_buffer == null 
                || _buffer.length < _sizes[0]
                || _buffer[0].length < _sizes[1]
                || _buffer[0][0].length < _sizes[2]
                || _buffer[0][0][0].length < _sizes[3]) {
            _buffer = new Token[_sizes[0]][_sizes[1]][_sizes[2]][_sizes[3]];
        }
    }
    
    /** Specify the pattern in which data is written to the receiver.
     *  @param writeSpec Number of tokens written per firing by dimension.
     */
    public void setWritePattern(LinkedHashMap<String,Integer> writeSpec) {
        // FIXME: Ignored for now.
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    
    protected void _incrementReadPointer(int amount) {
        _incrementPointer(_readPointer, amount);
    }
    
    protected void _incrementWritePointer(int amount) {
        _incrementPointer(_writePointer, amount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** Buffer memory. */
    protected Token[][][][] _buffer;
    
    /** Ordered list of the dimensions in the system. */
    protected LinkedHashSet _dimensions;
    
    /** Read pattern. */
    protected int[] _readPattern = new int[_NUMBER_OF_DIMENSIONS];

    /** Current read pointer into the buffer. */
    protected int[] _readPointer = new int[_NUMBER_OF_DIMENSIONS];
    
    /** Current size of the buffer. */
    protected int[] _sizes = new int[_NUMBER_OF_DIMENSIONS];
    
    /** Write pattern. */
    protected int[] _writePattern = new int[_NUMBER_OF_DIMENSIONS];

    /** Current write pointer into the buffer. */
    protected int[] _writePointer = new int[_NUMBER_OF_DIMENSIONS];
    
    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /**
     * @param amount
     */
    private void _incrementPointer(int[] pointer, int amount) {
        for (int i = 0; i < amount; i++) {
            // FIXME: For now, ignore read pattern.
            pointer[0] += 1;
            if (pointer[0] >= _sizes[0]) {
                pointer[0] = 0;
                pointer[1] += 1;
                if (pointer[1] >= _sizes[1]) {
                    pointer[1] = 0;
                    pointer[2] += 1;
                    if (pointer[2] >= _sizes[2]) {
                        pointer[2] = 0;
                        pointer[3] += 1;
                        if (pointer[3] >= _sizes[3]) {
                            pointer[3] = 0;
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    
    /** Number of dimensions supported. */
    private static int _NUMBER_OF_DIMENSIONS = 4;

    /** Value one. */
    private static Integer _ONE = new Integer(1);
}
