/* Chop an input sequence and construct from it a new output sequence.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Chop
/**
This actor reads a sequence of input tokens of any type, and writes a
sequence of tokens constructed from the input sequence (possibly
supplemented with zeros).  The number of input tokens consumed
is given by <i>numberToRead</i>, and the number of output tokens
produced is given by <i>numberToWrite</i>.
The <i>offset</i> parameter (default 0) specifies where in the output
block the first (oldest) input that is read should go.
If <i>offset</i> is positive and <i>usePastInputs</i> is true,
then the first few outputs will come from values read in previous iterations.
<p>
A simple use of this actor is to pad a block of inputs with zeros.
Set <i>offset</i> to zero and use <i>numberToWrite &gt; numberToRead</i>.
<a name="zero padding"></a>
<a name="padding"></a>
<p>
Another simple use is to obtain overlapping windows from
an input stream.
Set <i>usePastInputs</i> to true, use <i>numberToWrite &gt; numberToRead</i>,
and set <i>offset</i> equal to <i>numberToWrite - numberToRead</i>.
<a name="overlapping windows"></a>
<a name="windowing"></a>
<p>
The general operation is illustrated with the following examples.
If <i>offset</i> is positive,
there two possible scenarios, illustrated by the following examples:
<p>
<pre>
<pre>
     iiiiii                  numberToRead = 6
      \    \                 offset = 2
     ppiiiiii00              numberToWrite = 10
<p>
     iiiiii                  numberToRead = 6
      \ \  \                 offset = 2
     ppiii                   numberToWrite = 5
</pre>
</pre>
<p>
The symbol "i" refers to any input token. The leftmost symbol
refers to the oldest input token of the ones consumed in a given
firing. The symbol "p" refers to a token that is either zero
(if <i>usePastInputs</i> is false) or is equal to a previously
consumed input token (if <i>usePastInputs</i> is true).
The symbol "0" refers to a zero-valued token.
In the first of the above examples, the entire input block is
copied to the output, and then filled out with zeros.
In the second example, only a portion of the input block fits.
The remaining input tokens are discarded, although they might
be used in subsequent firings if <i>usePastInputs</i> is true.
<p>
When the <i>offset</i> is negative, this indicates that the
first <i>offset</i> input tokens that are read should be
discarded.  The corresponding scenarios are shown below:
<p>
<pre>
<pre>
     iiiiii                  numberToRead = 6
    / /  /                   offset = -2
     iiii000000              numberToWrite = 10
<p>
     iiiiii                  numberToRead = 6
    / / //                   offset = -2
     iii                     numberToWrite = 3
</pre>
</pre>
<p>
In the first of these examples, the first two input tokens are
discarded.  In the second example, the first two and the last input
token are discarded.
<p>
The zero-valued tokens are constructed using the zero() method of
the first input token that is read in the firing.  This returns
a zero-valued token with the same type as the input.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class Chop extends SDFTransformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Chop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        numberToRead = new Parameter(this, "numberToRead");
        numberToRead.setExpression("128");
        numberToRead.setTypeEquals(BaseType.INT);
        
        numberToWrite = new Parameter(this, "numberToWrite");
        numberToWrite.setExpression("64");
        numberToWrite.setTypeEquals(BaseType.INT);
        
        offset = new Parameter(this, "offset");
        offset.setExpression("0");
        offset.setTypeEquals(BaseType.INT);

        usePastInputs = new Parameter(this, "usePastInputs");
        usePastInputs.setExpression("true");
        usePastInputs.setTypeEquals(BaseType.BOOLEAN);
        
        input_tokenConsumptionRate.setExpression("numberToRead");
        output_tokenProductionRate.setExpression("numberToWrite");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of input tokens to read.
     *  This is an integer, with default 128.
     */
    public Parameter numberToRead;

    /** The number of tokens to write to the output.
     *  This is an integer, with default 64.
     */
    public Parameter numberToWrite;

    /** Start of output block relative to start of input block.
     *  This is an integer, with default 0.
     */
    public Parameter offset;

    /**  If offset is greater than 0, specify whether to use previously
     *   read inputs (otherwise use zeros).
     *  This is a boolean, with default true.
     */
    public Parameter usePastInputs;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the validity of parameter values, set production and
     *  consumption rates on the ports, and, if necessary, invalidate
     *  the current schedule.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Note: it is important that none of these sections depend on
        // eachother.
        if (attribute == numberToRead) {
            _numberToRead = ((IntToken)numberToRead.getToken()).intValue();
            if (_numberToRead <= 0) {
                throw new IllegalActionException(this,
                        "Invalid numberToRead: " + _numberToRead);
            }
        } else if (attribute == numberToWrite) {
            _numberToWrite = ((IntToken)numberToWrite.getToken()).intValue();
            if (_numberToWrite <= 0) {
                throw new IllegalActionException(this,
                        "Invalid numberToWrite: " + _numberToRead);
            }
            _buffer = new Token[_numberToWrite];
        } else if (attribute == offset) {
            _offsetValue = ((IntToken)offset.getToken()).intValue();
        } else if (attribute == usePastInputs) {
            _usePast = ((BooleanToken)usePastInputs.getToken()).booleanValue();
        }

        if (attribute == offset || attribute == usePastInputs) {
            if (_offsetValue > 0) {
                _pastBuffer = new Token[_offsetValue];
                _pastNeedsInitializing = true;
            }
        }

        if (attribute == numberToRead ||
                attribute == numberToWrite ||
                attribute == offset ||
                attribute == usePastInputs) {
            // NOTE: The following computation gets repeated when each of
            // these gets set, but it's a simple calculation, so we live
            // with it.
            // The variables _highLimit and _lowLimit indicate the range of
            // output indexes that come directly from the input block
            // that is read.
            _highLimit = _offsetValue + _numberToRead - 1;
            if (_highLimit >= _numberToWrite) _highLimit = _numberToWrite - 1;

            if (_offsetValue >= 0) {
                _lowLimit = _offsetValue;
                _inputIndex = 0;
            } else {
                _lowLimit = 0;
                _inputIndex = -_offsetValue;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the specified number of input tokens, and produce
     *  the specified number of output tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int inputIndex = _inputIndex;
        int pastBufferIndex = 0;
        Token[] inBuffer = input.get(0, _numberToRead);
        Token zero = inBuffer[0].zero();
        for (int i = 0; i < _numberToWrite; i++) {
            if (i > _highLimit) {
                _buffer[i] = zero;
            } else if (i < _lowLimit) {
                if (_usePast) {
                    if (_pastNeedsInitializing) {
                        // Fill past buffer with zeros.
                        for (int j = 0; j < _pastBuffer.length; j++) {
                            _pastBuffer[j] = zero;
                        }
                        _pastNeedsInitializing = false;
                    }
                    _buffer[i] = _pastBuffer[pastBufferIndex++];
                } else {
                    _buffer[i] = zero;
                }
            } else {
                // FIXME: This will access past samples...
                _buffer[i] = inBuffer[inputIndex];
                inputIndex++;
            }
        }
        if (_usePast && _offsetValue > 0) {
            // Copy input buffer into past buffer.  Have to be careful
            // here because the buffer might be longer than the
            // input window.
            int startCopy = _numberToRead - _offsetValue;
            int length = _pastBuffer.length;
            int destination = 0;
            if (startCopy < 0) {
                // Shift older data.
                destination = _pastBuffer.length - _numberToRead;
                System.arraycopy(_pastBuffer, _numberToRead,
                        _pastBuffer, 0, destination);
                startCopy = 0;
                length = _numberToRead;
            }
            System.arraycopy(inBuffer, startCopy, _pastBuffer,
                    destination, length);
        }
        output.send(0, _buffer, _numberToWrite);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private int _highLimit, _inputIndex, _lowLimit;
    private int _numberToRead, _numberToWrite, _offsetValue;
    private Token[] _buffer, _pastBuffer;
    private boolean _usePast, _pastNeedsInitializing;
}
