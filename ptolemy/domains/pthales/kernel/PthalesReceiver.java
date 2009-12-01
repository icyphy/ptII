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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

public class PthalesReceiver extends SDFReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check whether the array is correct or not. FIXME: What does it
     *  mean to be correct?
     *  @param baseSpec The origin.
     *  @param patternSpec Fitting of the array.
     *  @param tilingSpec Paving of the array.
     *  @param dimensions Dimensions contained in the array.
     *  @throws IllegalActionException FIXME: If what?
     */
    public void checkArray(LinkedHashMap<String, Integer[]> baseSpec,
            LinkedHashMap<String, Integer[]> patternSpec,
            LinkedHashMap<String, Integer[]> tilingSpec, 
            List<String> dimensions)
            throws IllegalActionException {

        /* FIXME: Checks for validity of array needed here.
        // First set defaults for any unspecified dimensions.
        for (String dimension : dimensions) {
            // Do some error checking. 
            Integer size = patternSpec.get(dimension)[0];
            if (size != null && size.intValue() <= 0) {
                throw new IllegalActionException(getContainer(),
                        "Dimension size is required to be strictly greater than zero.");
            }
            Integer tiling = tilingSpec.get(dimension)[0];
            if (tiling != null && tiling.intValue() <= 0) {
                throw new IllegalActionException(getContainer(),
                        "Tiling is required to be strictly greater than zero.");
            }

            // Also set the overall buffer size spec.
            if (tilingSpec.get(dimension) == null) {
                throw new IllegalActionException(
                        getContainer(),
                        "Size specification does not include "
                                + dimension
                                + ", which is included in the repetitions parameter of the sending actor.");
            }
        }
        */
    }

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
        if (_buffer != null) {
            Token result =  _buffer[_addressesIn[_posIn++]];
            return result;
        } else {
            throw new NoTokenException("Empty buffer in PthalesReceiver !");
        }
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
        Token[] result = new Token[numberOfTokens];
        for (int i = 0; i < numberOfTokens; i++)
            result[i] = get();
            
        return result;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasRoom() {
        return (_posOut + 1 <= _buffer.length );
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasRoom(int numberOfTokens) {
        return (_posOut + numberOfTokens <= _buffer.length );
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasToken() {
        return (_posOut >=_posIn + 1 );
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasToken(int numberOfTokens) {
        return (_posOut >=_posIn + numberOfTokens );
    }

    /** Return true.
     *  @return True.
     */
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into this receiver.
     *  If the specified token is null, this method
     *  inserts a null into the array.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void put(Token token) {
        if (_buffer != null) {
            _buffer[_addressesOut[_posOut++]] = token;
        }
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
        for (int i = 0; i < numberOfTokens; i++)
            put(tokenArray[i]);
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
        for (Receiver receiver : receivers) {
        for (int i = 0; i < numberOfTokens; i++)
                receiver.put(tokens[i]);
            }
      }

    /** Put a single token to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  If the specified token is null, this method inserts a
     *  null into the arrays.
     *  @param token The token to put, or null to put no token.
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
    }

    /** Specifies the input array that will read the buffer allocated as output.
     * Here we only check that everything is correct, and computes addresses in output buffer.
     * @param baseSpec : origins 
     * @param patternSpec : fitting of the array
     * @param tilingSpec : paving of the array
     * @param repetitionsSpec
     * @param dimensions : dimensions contained in the array
     * @throws IllegalActionException
     */
    public void setInputArray(LinkedHashMap<String, Integer[]> baseSpec,
            LinkedHashMap<String, Integer[]> patternSpec,
            LinkedHashMap<String, Integer[]> tilingSpec,
            Integer[] repetitionsSpec, List<String> dimensions)
            throws IllegalActionException {

        _base = baseSpec;
        _pattern = patternSpec;
        _tiling = tilingSpec;
        _repetitions = repetitionsSpec;

        checkArray(baseSpec, patternSpec, tilingSpec, dimensions);

        if (_buffer != null)
            computeAddresses(true);
    }

    /** Specifies the output array that will be read by the receiver
     * It is the output array that determines the available size and dimensions
     * for the receivers.
     * This function allocates a buffer that is used as a memory would be (linear)
     * @param baseSpec : origins 
     * @param patternSpec : fitting of the array
     * @param tilingSpec : paving of the array
     * @param repetitionsSpec
     * @param dimensions : dimensions contained in the array
     * @throws IllegalActionException
     */
    public void setOutputArray(LinkedHashMap<String, Integer[]> baseSpec,
            LinkedHashMap<String, Integer[]> patternSpec,
            LinkedHashMap<String, Integer[]> tilingSpec,
            Integer[] repetitionsSpec, List<String> dimensions)
            throws IllegalActionException {

        _dimensions = dimensions;
        _base = baseSpec;
        _pattern = patternSpec;
        _tiling = tilingSpec;
        _repetitions = repetitionsSpec;

        checkArray(baseSpec, patternSpec, tilingSpec, dimensions);

        // Output determines array size needed as input cannot read which has not been written

        // Sizes
        // FIXME: works for "compacted sizes", maybe needs some modifications
        int value;
        int valuePattern;
        int valueTiling;
        for (String dimension : dimensions) {
            valuePattern = 0;
            if (patternSpec.get(dimension) != null)
                valuePattern += patternSpec.get(dimension)[0].intValue()
                        * patternSpec.get(dimension)[1].intValue();
            Object repList[] = tilingSpec.keySet().toArray();
            valueTiling = 0;
            if (tilingSpec.get(dimension) != null) {
                for (int i = 0; i < repList.length; i++) {
                    if (repList[i].equals(dimension))
                        valueTiling = tilingSpec.get(dimension)[0].intValue()
                                * repetitionsSpec[i];
                }
            }
            if (valuePattern == 0)
                value = valueTiling;
            else if (valueTiling == 0)
                value = valuePattern;
            else
                value = valuePattern * valueTiling;

            _sizes.put(dimension, value);
        }

        // Total size of the array in "memory"
        int finalSize = getDataSize();
        if (_buffer == null || _buffer.length < getDataSize())
            _buffer = new Token[finalSize];

        //
        computeAddresses(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////               package friendly methods                    ////

    /** Returns the size of the array once computed.
     *  @return The array size.
     */
    int getDataSize() {
        int blockSize = 1;
        for (String dimension : _dimensions) {
            blockSize *= _sizes.get(dimension);
        }
        return blockSize;
    }
    
    /** Returns the number of addresses needed to access all of the
     * datas for all iterations
     *  @return the number of adresses.
     */
    int getAddressNumber() {
        int valuePattern = 1;
        for (String dimension : _dimensions) {
            if (_pattern.get(dimension) != null)
                valuePattern *= _pattern.get(dimension)[0].intValue()
                        * _pattern.get(dimension)[1].intValue();
        }
        
        int sizeRepetition = 1;
        // size for tiling dimensions 
        for (Integer repetition : _repetitions) {
            // Dimension added to pattern list
            sizeRepetition *= repetition;
        }

        return valuePattern*sizeRepetition;
    }

    /** Returns the size of 
     * the array for each dimension
     */
    String getArraySize() {
        String blockSize = "";
        for (String dimension : _dimensions) {
            blockSize += dimension + "=" + _sizes.get(dimension) + ",";
        }
        return blockSize.substring(0, blockSize.length() - 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////               package friendly variables                  ////

    int _posIn = 0;
    int _posOut = 0;

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** Buffer memory. */
    protected Token[] _buffer = null;

    /** addresses for input or output */
    protected int[] _addressesOut = null;

    protected int[] _addressesIn = null;

    /** Current sizes of the buffer. */
    protected LinkedHashMap<String, Integer> _sizes = new LinkedHashMap<String, Integer>();

    /** parameters */
    protected LinkedHashMap<String, Integer[]> _base = null;

    protected LinkedHashMap<String, Integer[]> _pattern = null;

    protected LinkedHashMap<String, Integer[]> _tiling = null;

    protected Integer[] _repetitions = null;

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    void computeAddresses(boolean input) {
        // same number of addresses than buffer size (worst case)
        int jumpPattern[] = new int[getAddressNumber()];

        // Position in buffer 
        int pos = 0;

        // Pattern size, used for each repetition
        int sizePattern = 1;
        // repetition size
        int sizeRepetition = 1;

        // size for pattern dimensions 
        Iterator patternIterator = _pattern.keySet().iterator();
        while (patternIterator.hasNext()) {
            String dimPattern = (String) patternIterator.next();
            sizePattern *= _pattern.get(dimPattern)[0];
        }

        // size for tiling dimensions 
        for (Integer repetition : _repetitions) {
            // Dimension added to pattern list
            sizeRepetition *= repetition;
        }

        // address indexes
        int dims[] = new int[_pattern.size() + 1];
        // address indexes
        int reps[] = new int[_repetitions.length + 1];

        // addresses creation
        int jumpDim;
        int jumpRep;
        int previousSize;

        // Pattern order
        String[] patternOrder = new String[_dimensions.size()];
        _pattern.keySet().toArray(patternOrder);
        // tiling order 
        String[] tilingOrder = new String[_dimensions.size()];
        _tiling.keySet().toArray(tilingOrder);

        // Address jump for each dimension
        LinkedHashMap<String, Integer> jumpAddr = new LinkedHashMap<String, Integer>();
        for (int nDim = 0; nDim < _dimensions.size(); nDim++) {
            previousSize = 1;
            for (int prev = 0; prev < nDim; prev++) {
                previousSize *= _sizes.get(_dimensions.get(prev));
            }
            jumpAddr.put(_dimensions.get(nDim), previousSize);
        }

        // origin construction (order is not important)
        Integer origin = 0;
        for (int nDim = 0; nDim < _dimensions.size(); nDim++) {
            origin += _base.get(_dimensions.get(nDim))[0]
                    * jumpAddr.get(_dimensions.get(nDim));
        }

        // Address construction  (order is important)
        for (int rep = 0; rep < sizeRepetition; rep++) {
            jumpRep = 0;
            for (int nRep = 0; nRep < tilingOrder.length; nRep++) {
                if (_tiling.get(tilingOrder[nRep]) != null)
                {
                    jumpRep += _tiling.get(tilingOrder[nRep])[0] * reps[nRep]
                        * jumpAddr.get(tilingOrder[nRep]);
                }
            }

            for (int dim = 0; dim < sizePattern; dim++) {
                jumpDim = 0;
                for (int nDim = 0; nDim < _pattern.size(); nDim++) {
                    jumpDim += _pattern.get(patternOrder[nDim])[1] * dims[nDim]
                            * jumpAddr.get(patternOrder[nDim]);
                }

                jumpPattern[pos] = origin + jumpDim + jumpRep;
                pos++;
                
                //pos = pos%_buffer.length;

                // pattern indexes update
                dims[0]++;
                for (int nDim = 0; nDim < _pattern.size(); nDim++) {
                    if (dims[nDim] == _pattern.get(patternOrder[nDim])[0]) {
                        dims[nDim] = 0;
                        dims[nDim + 1]++;
                    }
                }
            }
            // repetition indexes update
            reps[0]++;
            for (int nRep = 0; nRep < _repetitions.length; nRep++) {
                if (reps[nRep] == _repetitions[nRep]) {
                    reps[nRep] = 0;
                    reps[nRep + 1]++;
                }
            }
        }

        if (input)
            _addressesIn = jumpPattern;
        else
            _addressesOut = jumpPattern;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The dimensions relevant to this receiver. */
    private List<String> _dimensions;
}
