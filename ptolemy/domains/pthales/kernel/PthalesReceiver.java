/* A receiver for multidimensional dataflow.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.domains.pthales.lib.PthalesAtomicActor;
import ptolemy.domains.pthales.lib.PthalesIOPort;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A receiver for the Pthales model of computation.
 *
 * @author R&eacute;mi Barr&egrave;re
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesReceiver extends SDFReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check whether the array is correct or not. FIXME: What does it
     *  mean to be correct?
     *  @param baseSpec The origin.
     *  @param patternSpec Fitting of the array.
     *  @param tilingSpec Paving of the array.
     *  @param dimensions Dimensions contained in the array.
     *  @exception IllegalActionException FIXME: If what?
     */
    public void checkArray(LinkedHashMap<String, Integer[]> baseSpec,
            LinkedHashMap<String, Integer[]> patternSpec,
            LinkedHashMap<String, Integer[]> tilingSpec, List<String> dimensions)
                    throws IllegalActionException {

        /* FIXME: Checks for validity of array needed here.
         */
    }

    /** Do nothing.
     */
    @Override
    public void clear() {
        // Ignore
    }

    /** Return a list with tokens that are currently in the receiver
     *  available for get() or getArray(). The oldest token (the one
     *  that was put first) should be listed first in any implementation
     *  of this method.
     *  @return A list of instances of Token.
     */
    @Override
    public List<Token> elementList() {
        // FIXME: implement this.
        return new LinkedList();
    }

    /** Update parameters of this receiver according to the actor and port.
     * @param actor  The actor.
     * @param port The port.
     */
    public void fillParameters(Actor actor, IOPort port) {
        int[] repetitions = null;

        PthalesIOPort.setDataType(port);

        repetitions = PthalesAtomicActor.getIterations((ComponentEntity) actor);

        LinkedHashMap<String, Integer[]> base = PthalesIOPort.getBase(port);

        // Number of tokens per data
        _nbTokens = PthalesIOPort.getNbTokenPerData(port);

        // FIXME: sizeRepetition is not used?
        // total repetition size
        //int sizeRepetition = 1;
        //for (Integer size : repetitions) {
        //    sizeRepetition *= size;
        //}

        // origin construction once per port (order is not important)
        int origin = 0;
        for (String _dimension : _dimensions) {
            // a base can be null so origin does not increase
            if (base.get(_dimension) != null) {
                origin += base.get(_dimension)[0] * _jumpAddr.get(_dimension)
                        * _nbTokens;
            }
        }

        if (port.isInput()) {
            // Specific to input ports
            _originIn = origin;
            _repetitionsIn = repetitions;
            _patternSizeIn = PthalesIOPort.getPatternNbAddress(port);
            _patternIn = PthalesIOPort.getInternalPattern(port);
            _tilingIn = PthalesIOPort.getExternalTiling(port,
                    _repetitionsIn.length);

        } else {
            // Specific to output ports
            _originOut = origin;
            _repetitionsOut = repetitions;
            _patternSizeOut = PthalesIOPort.getPatternNbAddress(port);
            _patternOut = PthalesIOPort.getInternalPattern(port);
            _tilingOut = PthalesIOPort.getExternalTiling(port,
                    _repetitionsOut.length);
        }
    }

    /** Get a token from this receiver.
     *  @return A token read from the receiver.
     *  @exception NoTokenException If there is no token.
     */
    @Override
    public Token get() throws NoTokenException {
        if (_buffer != null) {
            Token result = null;
            if (_dynamic) {
                if (_currentCtrlPositionOut < _headerSize) {
                    result = _header.get(_currentCtrlPositionOut);
                    _currentCtrlPositionOut++;
                    if (_currentCtrlPositionOut >= _headerSize) {
                        _header.clear();
                    }
                } else {
                    result = _buffer[_getAddress(_positionIn++, true)];
                }

            } else {
                result = _buffer[_getAddress(_positionIn++, true)];
            }
            return result;
        } else {
            throw new NoTokenException(getContainer(),
                    "Empty buffer in PthalesReceiver !");
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
    @Override
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        Token[] result = new Token[numberOfTokens];

        for (int i = 0; i < numberOfTokens; i++) {
            result[i] = get();
        }

        return result;
    }

    /** Return true if the buffer can contain one more token.
     *  @return true or false.
     */
    @Override
    public boolean hasRoom() {
        return _getAddress(_positionOut, false) < _buffer.length;
    }

    /** Return true if the buffer can contain n more token.
     *  @param numberOfTokens the number of needed places to put tokens
     *  @return true or false.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return _getAddress(_positionOut + numberOfTokens - 1, false) < _buffer.length;
    }

    /** Return if the buffer contains 1 more token to be read.
     *  @return True.
     */
    @Override
    public boolean hasToken() {
        return _getAddress(_positionIn, true) < _buffer.length;
    }

    /** Return if the buffer contains n more token to be read.
     *  @param numberOfTokens the number of needed tokens
     *  @return True.
     */
    @Override
    public boolean hasToken(int numberOfTokens) {
        return _getAddress(_positionIn + numberOfTokens - 1, true) < _buffer.length;
    }

    /** Return true if the receiver is dynamic.
     *  A dynamic receiver's get() method returns data
     *  from the current control position, which is is adjusted
     *  each time get() is called.
     *  A non-dynamic receiver's get() method merely
     *  increments the positionIn pointer each time get() is called.
     *  @return true if this receiver is dynamic.
     *  @see #setDynamic(boolean)
     */
    public boolean isDynamic() {
        return _dynamic;
    }

    /** Return true.
     *  @return True.
     */
    @Override
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into the next appropriate array position,
     *  given the FIXME.
     *  If the specified token is null, this method
     *  inserts a null into the array.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    @Override
    public void put(Token token) {
        if (_buffer != null) {
            if (!_dynamic) {
                _buffer[_getAddress(_positionOut++, false)] = token;
            } else {
                if (_header.size() == 0) {
                    //the first token of the header
                    _headerSize = ((IntToken) token).intValue() * 2 + 2;
                    _header.add(token);
                    _currentCtrlPositionOut = 0;
                } else if (_header.size() < _headerSize) {
                    _header.add(token);
                    if (_header.size() == _headerSize) {
                        int finalSize = 1;
                        int nDims = ((IntToken) _header.get(0)).intValue();

                        //Computed for output ports only
                        _sizes = new LinkedHashMap<String, Integer>();

                        _dimensions = new String[nDims];

                        LinkedHashMap<String, Integer[]> pattern = new LinkedHashMap<String, Integer[]>();

                        for (int i = 0; i < nDims; i++) {
                            int val = ((IntToken) _header.get(3 + 2 * i))
                                    .intValue();

                            String dim = ((StringToken) _header.get(2 + 2 * i))
                                    .stringValue();

                            finalSize *= val;

                            _sizes.put(dim, val);
                            _dimensions[i] = dim;
                            Integer[] values = new Integer[2];
                            values[0] = val;
                            values[1] = 1;
                            pattern.put(dim, values);
                        }
                        if (_buffer == null || _buffer.length < finalSize) {
                            _buffer = new Token[finalSize
                                                * ((IntToken) _header.get(1)).intValue()];
                        }

                        // Address jump for each dimension, determined by output port only
                        int previousSize;
                        for (int nDim = 0; nDim < _dimensions.length; nDim++) {
                            previousSize = 1;
                            for (int prev = 0; prev < nDim; prev++) {
                                if (_sizes.get(_dimensions[prev]) != null) {
                                    previousSize *= _sizes
                                            .get(_dimensions[prev]);
                                }
                            }
                            _jumpAddr.put(_dimensions[nDim], previousSize);
                        }

                        _patternOut = pattern;
                        _patternSizeOut = finalSize;

                        //parameters for reading data out
                        IOPort port = getContainer();

                        LinkedHashMap<String, Integer[]> base = PthalesIOPort
                                .getBase(port);

                        //                      Number of tokens per data
                        _nbTokens = PthalesIOPort.getNbTokenPerData(port);

                        int origin = 0;
                        for (String _dimension : _dimensions) {
                            // a base can be null so origin does not increase
                            if (base.get(_dimension) != null) {
                                origin += base.get(_dimension)[0]
                                        * _jumpAddr.get(_dimension) * _nbTokens;
                            }
                        }

                        //Specific to input ports
                        _originIn = origin;
                    }
                } else {
                    _buffer[_getAddress(_positionOut++, false)] = token;
                }
            }
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
    @Override
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        for (int i = 0; i < numberOfTokens; i++) {
            put(tokenArray[i]);
        }
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
    @Override
    public void putArrayToAll(Token[] tokens, int numberOfTokens,
            Receiver[] receivers) throws NoRoomException,
            IllegalActionException {
        for (Receiver receiver : receivers) {

            for (int i = 0; i < numberOfTokens; i++) {
                receiver.put(tokens[i]);
            }
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
    @Override
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
    @Override
    public void reset() throws IllegalActionException {
        _positionIn = 0;
        _positionOut = 0;
    }

    /** Set whether this receiver is dynamic.
     *  @param dynamic True if this receiver is dynamic.
     *  @see #isDynamic()
     */
    public void setDynamic(boolean dynamic) {
        _dynamic = dynamic;
    }

    /** Specify a buffer to use for storing the array.
     *  This method is used by the PthalesDirector to provide a buffer
     *  for an external port of a composite actor containing said PthalesDirector.
     *  @param actor The composite actor containing the PthalesDirector.
     *  @param port The external port.
     *  @param buffer The external buffer.
     */
    public void setExternalBuffer(Actor actor, IOPort port, Token[] buffer) {
        if (_buffer == null) {
            _buffer = buffer;
            _positionOut = _buffer.length;
            try {
                setOutputArray(port, actor);
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
        }
    }

    /** Specifies the input array that will read the buffer allocated
     * as output.  Here we only check that everything is correct, and
     * compute addresses in output buffer.
     * @param port the associated port
     * @param actor the associated actor
     * @exception IllegalActionException
     */
    public void setInputArray(IOPort port, Actor actor)
            throws IllegalActionException {

        // Common to all ports
        if (!_dynamic && _buffer != null) {
            fillParameters(actor, port);
        }
    }

    /** Specifies the output array that will be read by the receiver.
     * It is the output array that determines the available size and
     * dimensions for the receivers.  This function allocates a buffer
     * that is used as a memory would be (linear)
     * @param port the associated port
     * @param actor the associated actor
     * @exception IllegalActionException
     */
    public void setOutputArray(IOPort port, Actor actor)
            throws IllegalActionException {

        // Output determines needed array size , as input cannot read which has not been written

        // Total size of the array in "memory"
        int finalSize = PthalesIOPort.getArraySize(port);
        if (_buffer == null || _buffer.length < finalSize) {
            _buffer = new Token[finalSize
                                * PthalesIOPort.getNbTokenPerData(port)];
        }

        // Computed for output ports only
        _sizes = PthalesIOPort.getArraySizes(port);

        String[] objs = PthalesIOPort.getDimensions(port);
        _dimensions = new String[objs.length];

        for (int i = 0; i < objs.length; i++) {
            _dimensions[i] = objs[i];
        }

        // Address jump for each dimension, determined by output port only
        int previousSize;
        for (int nDim = 0; nDim < _dimensions.length; nDim++) {
            previousSize = 1;
            for (int prev = 0; prev < nDim; prev++) {
                if (_sizes.get(_dimensions[prev]) != null) {
                    previousSize *= _sizes.get(_dimensions[prev]);
                }
            }
            _jumpAddr.put(_dimensions[nDim], previousSize);
        }

        // Common to all ports
        fillParameters(actor, port);
    }

    /** Set repetitions of the actor containing the port that contains
     *  the receiver.
     *  @param repetitions An array of Integer repetitions.
     */
    public void setReadParameters(Integer[] repetitions) {
        if (repetitions != null) {
            _repetitionsIn = new int[repetitions.length];

            for (int i = 0; i < repetitions.length; i++) {
                _repetitionsIn[i] = repetitions[i].intValue();
            }

            _patternSizeIn = PthalesIOPort.getPatternNbAddress(getContainer());
            _patternIn = PthalesIOPort.getInternalPattern(getContainer());

            _tilingIn = PthalesIOPort.getExternalTiling(getContainer(),
                    _repetitionsIn.length);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////               package friendly variables                  ////

    int _positionIn = 0;

    int _positionOut = 0;

    int _currentCtrlPositionOut = 0;

    int _headerSize = 0;

    boolean _dynamic = false;

    List<Token> _header = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    //                    Variables for input ports                ////

    int _originIn = 0;

    int _patternSizeIn;

    int[] _repetitionsIn;

    LinkedHashMap<String, Integer[]> _patternIn;

    LinkedHashMap<String, Integer[]> _tilingIn;

    // Variables for input ports
    int _originOut = 0;

    int _patternSizeOut;

    int[] _repetitionsOut;

    LinkedHashMap<String, Integer[]> _patternOut;

    LinkedHashMap<String, Integer[]> _tilingOut;

    // Variables for all cases
    LinkedHashMap<String, Integer> _jumpAddr = new LinkedHashMap<String, Integer>();

    int _nbTokens;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Buffer memory. */
    protected Token[] _buffer = null;

    // This variable is set by output ports only
    /** array size by dimension. */
    protected LinkedHashMap<String, Integer> _sizes = null;

    // This variable is set by output ports only
    /** Dimensions */
    String[] _dimensions = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the index for the buffer storing the array that corresponds
     *  to the specified position. If the second argument is true, then
     *  we want the index for reading the next array value after the
     *  previously read array value. If the second argument is false, then
     *  we want the index for the next place to write a new token into the
     *  array.
     *
     *  Here, "position" means the following. After this receiver is reset(),
     *  there will be a sequence of calls to get() to obtain tokens and/or
     *  to put() to insert tokens into the array. Position <i>n</i> means
     *  the <i>n</i>th read or the <i>n</i>th write. Whether it is a read
     *  or a write is specified by the second argument, which is true if it
     *  is a read and false if it is a write.
     */
    private int _getAddress(int position, boolean input) {

        int origin = 0;

        int patternSize = 0;
        LinkedHashMap<String, Integer[]> pattern = null;
        LinkedHashMap<String, Integer[]> tiling = null;
        int[] repetitions = null;

        if (input || _patternOut == null) {
            patternSize = _patternSizeIn;
            pattern = _patternIn;
            tiling = _tilingIn;
            origin = _originIn;
            repetitions = _repetitionsIn;
        }
        if (!input || _patternIn == null) {
            patternSize = _patternSizeOut;
            pattern = _patternOut;
            tiling = _tilingOut;
            origin = _originOut;
            repetitions = _repetitionsOut;
        }

        // Pattern order
        String[] patternOrder = null;
        patternOrder = new String[pattern.size()];
        pattern.keySet().toArray(patternOrder);
        // tiling order
        String[] tilingOrder = new String[tiling.size()];
        tiling.keySet().toArray(tilingOrder);

        // Position computation
        int rep = (int) Math.floor(position
                / (double) (patternSize * _nbTokens));
        int dim = (int) Math.floor(position % (patternSize * _nbTokens))
                / _nbTokens;
        int numToken = (int) Math.floor(position % _nbTokens);

        // address indexes
        int dims[] = new int[pattern.size() + 1];
        // address indexes
        int reps[] = new int[repetitions.length + 1];

        if (repetitions.length > 0) {
            int repeats = repetitions[0];
            reps[0] = rep % repeats;
            for (int nRep = 1; nRep < repetitions.length; nRep++) {
                reps[nRep] = rep / repeats;
                repeats *= repetitions[nRep];
                if (nRep < repetitions.length - 1) {
                    reps[nRep] = reps[nRep] % repetitions[nRep];
                }
            }
        }
        int dimensions = pattern.get(patternOrder[0])[0];
        dims[0] = dim % dimensions;
        for (int nDim = 1; nDim < pattern.size(); nDim++) {
            dims[nDim] = dim / dimensions;
            dimensions *= pattern.get(patternOrder[nDim])[0];
            if (nDim < pattern.size() - 1) {
                dims[nDim] = dims[nDim] % pattern.get(patternOrder[nDim])[0];
            }
        }

        // addresses creation
        int jumpDim;
        int jumpRep;

        jumpRep = 0;
        for (int nRep = 0; nRep < tilingOrder.length; nRep++) {
            if (tiling.get(tilingOrder[nRep]) != null
                    && !tilingOrder[nRep].startsWith("empty")) {
                jumpRep += tiling.get(tilingOrder[nRep])[0] * reps[nRep]
                        * _jumpAddr.get(tilingOrder[nRep]) * _nbTokens;
            }
        }

        // Pattern is written/read for each iteration
        jumpDim = 0;

        for (int nDim = 0; nDim < pattern.size(); nDim++) {
            if (!patternOrder[nDim].startsWith("empty")) {
                jumpDim += pattern.get(patternOrder[nDim])[1] * dims[nDim]
                        * _jumpAddr.get(patternOrder[nDim]) * _nbTokens;
            }
        }

        return origin + jumpDim + jumpRep + numToken;
    }
}
