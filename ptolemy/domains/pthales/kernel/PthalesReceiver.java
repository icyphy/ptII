/* A receiver for multidimensional dataflow.

 Copyright (c) 1998-2009 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.domains.pthales.kernel;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

// FIXME: For now, support arrays up to four dimensions only.

public class PthalesReceiver extends SDFReceiver {

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
            _readBase[i] = 0;
            _readPointer[i] = 0;
            _writeBase[i] = 0;
            _writePointer[i] = 0;
        }
    }

    /** Specify the pattern in which data is read from the receiver.
     *  A side effect of this method is to set the capacity of the receiver.
     *  This must be called after setWritePattern().
     *  @param readSpec Number of tokens read per firing by dimension.
     *  @throws IllegalActionException If setting the capacity fails.
     */
    public void setReadPattern(
            LinkedHashMap<String,Integer[]> readSpec)
            throws IllegalActionException {
        // FIXME: Ignoring stride for now.
        
        // If the _dimensions have not been set, then this receiver
        // is not connected to a sender, and there is nothing to do.
        if (_dimensions == null) {
            return;
        }
        
        // First set defaults for any unspecified dimensions.
        int i = 0;
        for(String dimension : _dimensions) {
            Integer[] size = readSpec.get(dimension);
            if (size == null) {
                size = new Integer[2];
                size[0] = _ONE;
                size[1] = _ONE;
            }
            if (size[0].intValue() <= 0) {
                throw new IllegalActionException(getContainer(),
                        "Dimension size is required to be strictly greater than zero.");                
            }
            i++;
        }

        // Fill in the read pattern. This is an array of pairs,
        // where each pair gives first the dimension number to read and
        // then the number of tokens to read in that dimension.
        i = 0;
        Set<Integer> dimensionsCovered = new HashSet<Integer>();
        for (Entry<String,Integer[]> entry : readSpec.entrySet()) {
            int dimensionNumber = _indexOf(entry.getKey(), _dimensions);
            dimensionsCovered.add(new Integer(dimensionNumber));
            _readPattern[i][0] = dimensionNumber;
            _readPattern[i][1] = entry.getValue()[0].intValue();
            i++;
        }
        // The readSpec may not have covered all the dimensions,
        // so we have to cover them here.
        for (int dimensionNumber = 0; dimensionNumber < _NUMBER_OF_DIMENSIONS; dimensionNumber++) {
            if (!dimensionsCovered.contains(dimensionNumber)) {
                _readPattern[i][0] = dimensionNumber;
                _readPattern[i][1] = 1;
                i++;
            }
        }
    }
    
    /** Specify the pattern in which data is written to the receiver.
     *  This must be called before setReadPattern().
     *  @param writeSpec Number of tokens written per firing by dimension.
     *  @throws IllegalActionException If a non-positive dimension value is given.
     */
    public void setWritePattern(
            LinkedHashMap<String,Integer[]> writeSpec,
            LinkedHashMap<String,Integer[]> sizeSpec,
            List<String> dimensions) 
            throws IllegalActionException {
        int i = 0;
        // FIXME: Ignoring stride for now.
        
        _dimensions = dimensions;
        
        // First set defaults for any unspecified dimensions.
        for(String dimension : dimensions) {
            Integer[] size = writeSpec.get(dimension);
            if (size == null) {
                size = new Integer[2];
                size[0] = _ONE;
                size[1] = _ONE;
            }
            if (size[0].intValue() <= 0) {
                throw new IllegalActionException(getContainer(),
                        "Dimension size is required to be strictly greater than zero.");                
            }
            
            // Also set the size spec.
            _sizes[i] = sizeSpec.get(dimension)[0].intValue();

            i++;
        }
        // In case there are fewer dimensions than supported,
        // fill in the remaining dimensions with ones.
        while (i < _NUMBER_OF_DIMENSIONS) {
            _sizes[i] = 1;
            i++;
        }
        // Allocate the buffer, unless we already have a suitably sized
        // buffer.
        if (_buffer == null 
                || _buffer.length < _sizes[0]
                || _buffer[0].length < _sizes[1]
                || _buffer[0][0].length < _sizes[2]
                || _buffer[0][0][0].length < _sizes[3]) {
            _buffer = new Token[_sizes[0]][_sizes[1]][_sizes[2]][_sizes[3]];
        }

        // Next, fill in the write pattern. This is an array of pairs,
        // where each pair gives first the dimension number to read and
        // then the number of tokens to read in that dimension.
        i = 0;
        Set<Integer> dimensionsCovered = new HashSet<Integer>();
        for (Entry<String,Integer[]> entry : writeSpec.entrySet()) {
            int dimensionNumber = _indexOf(entry.getKey(), dimensions);
            dimensionsCovered.add(new Integer(dimensionNumber));
            _writePattern[i][0] = dimensionNumber;
            _writePattern[i][1] = entry.getValue()[0].intValue();
            i++;
        }
        // The writeSpec may not have covered all the dimensions,
        // so we have to cover them here.
        for (int dimensionNumber = 0; dimensionNumber < _NUMBER_OF_DIMENSIONS; dimensionNumber++) {
            if (!dimensionsCovered.contains(dimensionNumber)) {
                _writePattern[i][0] = dimensionNumber;
                _writePattern[i][1] = 1;
                i++;
            }
        }

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    
    protected void _incrementReadPointer(int amount) {
        _incrementPointer(_readPointer, amount, _readPattern, _readBase);
    }
    
    protected void _incrementWritePointer(int amount) {
        _incrementPointer(_writePointer, amount, _writePattern, _writeBase);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** Buffer memory. */
    protected Token[][][][] _buffer;
    
    /** Base of the current block being read. */
    protected int[] _readBase = new int[_NUMBER_OF_DIMENSIONS];

    /** Read pattern, as an array of pairs, where each pair gives
     *  the dimension number first, then the number of tokens to
     *  read in that dimension.
     */
    protected int[][] _readPattern = new int[_NUMBER_OF_DIMENSIONS][2];

    /** Current read pointer into the buffer. */
    protected int[] _readPointer = new int[_NUMBER_OF_DIMENSIONS];
    
    /** Current size of the buffer. */
    protected int[] _sizes = new int[_NUMBER_OF_DIMENSIONS];
    
    /** Base of the current block being read. */
    protected int[] _writeBase = new int[_NUMBER_OF_DIMENSIONS];

    /** Read pattern, as an array of pairs, where each pair gives
     *  the dimension number first, then the number of tokens to
     *  read in that dimension.
     */
    protected int[][] _writePattern = new int[_NUMBER_OF_DIMENSIONS][2];

    /** Current write pointer into the buffer. */
    protected int[] _writePointer = new int[_NUMBER_OF_DIMENSIONS];
    
    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    private void _check(int[] pointer, int d) {
        if (pointer[d] >= _sizes[d]) {
            throw new InternalErrorException("Incremented pointer past the size in dimension " + d);
        }
    }
    /** Increment the given pointer into the array.
     *  @param pointer The pointer to increment.
     *  @param amount The amount to increment the pointer by.
     *  @param pattern The pattern for the write.
     *  @param base The base of the current block being read or written.
     */
    private void _incrementPointer(int[] pointer, int amount, int[][] pattern, int[] base) {
        // FIXME: This is a silly programming style.
        // Should be structured and parameterized by _NUMBER_OF_DIMENSIONS.
        for (int i = 0; i < amount; i++) {
            int d = pattern[0][0];
            pointer[d]++;
            if (pointer[d] - base[d] >= pattern[0][1]) {
                pointer[d] = base[d];
                d = pattern[1][0];
                pointer[d]++;
                if (pointer[d] - base[d] >= pattern[1][1]) {
                    pointer[d] = base[d];
                    d = pattern[2][0];
                    pointer[d]++;
                    if (pointer[d] - base[d] >= pattern[2][1]) {
                        pointer[d] = base[d];
                        d = pattern[3][0];
                        pointer[d]++;
                        if (pointer[d] - base[d] >= pattern[3][1]) {
                            // Need to move the base.
                            for (int j = 0; j < _NUMBER_OF_DIMENSIONS; j++) {
                                // Find the increment for dimension j given in pattern.
                                int increment = 1;
                                for (int k = 0; k < _NUMBER_OF_DIMENSIONS; k++) {
                                    if (pattern[k][0] == j) {
                                        increment = pattern[k][1];
                                        break;
                                    }
                                }
                                base[j] += increment;
                                if (base[j] >= _sizes[j]) {
                                    base[j] = 0;
                                } else {
                                    // Base increment didn't overflow, so we are done.
                                    break;
                                }
                            }
                            // Copy the base into the pointer.
                            for (int j = 0; j < _NUMBER_OF_DIMENSIONS; j++) {
                                pointer[j] = base[j];
                                _check(pointer, j);
                            }
                        }
                    }
                }
            }
        }
        for (int j = 0; j < _NUMBER_OF_DIMENSIONS; j++) {
            _check(pointer, j);
        }
    }
    
    /** Return the index of the entry in the specified set.
     *  @param entry The entry.
     *  @param set The set.
     * @throws IllegalActionException If the entry is not in the set.
     */
    private int _indexOf(String entry, List<String> set) throws IllegalActionException {
        int result = 0;
        for (String setMember : set) {
            if (entry.equals(setMember)) {
                return result;
            }
            result++;
        }
        throw new IllegalActionException(getContainer(),
                "The dimension "
                + entry
                + " is not in the set of dimensions "
                + set);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    
    /** The dimensions relevant to this receiver. */
    private List<String> _dimensions;
    
    /** Number of dimensions supported. */
    private static int _NUMBER_OF_DIMENSIONS = 4;

    /** Value one. */
    private static Integer _ONE = new Integer(1);
}
