/* An actor that outputs a quantized version of the input.

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
package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Quantizer

/**
 <p>Produce an output token on each firing with a value that is
 a quantized version of the input.  The input and output types
 are both double.</p>
 <p>
 The <i>levels</i> parameter contains an array of doubles
 specifying the quantization levels. The elements must be in
 an increasing order, or an exception will be thrown.
 The default value of <i>levels</i> is {-1.0, 1.0}.</p>
 <p>
 Suppose <i>u</i> is the input, and
 <i>levels</i> = {<i>a</i>, <i>b</i>, <i>c</i>}, where
 <i>a</i> &lt; <i>b</i> &lt; <i>c</i>, then the output of the actor will be:
 </p>
 <p>
 <i>y</i> = <i>a</i>, for <i>u</i> &lt;= (<i>b</i>+<i>a</i>)/2;
 <br><i>y</i> = <i>b</i>, for (<i>b</i>+<i>a</i>)/2 &lt;</br>
 <br><i>u</i> &lt;= (<i>c</i>+<i>b</i>)/2;</br>
 <br><i>y</i> = <i>c</i>, for <i>u</i> &gt; (<i>c</i>+<i>b</i>)/2;</br>
 </p><p>
 Thus, for the default <i>levels</i>, the output is (almost)
 the signum function of the input, or +1.0 if the input is positive,
 and -1.0 otherwise.  This is almost the signum function because it
 outputs -1.0 if the input is zero.
 </p><p>
 This actor does not require that the quantization intervals be equal,
 i.e. we allow that (<i>c</i>-<i>b</i>) != (<i>b</i>-<i>a</i>).</p>

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class Quantizer extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Quantizer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        levels = new Parameter(this, "levels");
        levels.setExpression("{-1.0, 1.0}");
        levels.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(levels);

        // Set the type constraints.
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The quantization levels.
     *  This parameter contains an array of doubles with default value
     *  {-1.0, 1.0}.
     */
    public Parameter levels;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Quantizer newObject = (Quantizer) super.clone(workspace);

        newObject._thresholds = new double[_thresholds.length];
        System.arraycopy(_thresholds, 0, newObject._thresholds, 0,
                _thresholds.length);

        return newObject;
    }

    /** If the argument is the levels parameter, check that the array
     *  is increasing and has the right dimension.  Recompute the
     *  quantization thresholds.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the levels array is not
     *   increasing.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == levels) {
            ArrayToken levelsValue = (ArrayToken) levels.getToken();
            double[] _levels = new double[levelsValue.length()];
            double previous = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < levelsValue.length(); i++) {
                _levels[i] = ((DoubleToken) levelsValue.getElement(i))
                        .doubleValue();

                // Check nondecreasing property.
                if (_levels[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of levels is not nondecreasing.");
                }

                previous = _levels[i];
            }

            // Compute the quantization thresholds.
            _thresholds = new double[_levels.length - 1];

            for (int j = 0; j < _levels.length - 1; j++) {
                _thresholds[j] = (_levels[j + 1] + _levels[j]) / 2.0;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the quantization of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            double in = ((DoubleToken) input.get(0)).doubleValue();
            int index = _getQuantizationIndex(in);
            output.send(0, ((ArrayToken) levels.getToken()).getElement(index));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Compute the quantization index for the given input value.
     * @parameter in The input value
     * @return The quantization index.
     */
    private int _getQuantizationIndex(double in) {
        int index = _thresholds.length;

        for (int i = 0; i < _thresholds.length; i++) {
            if (in <= _thresholds[i]) {
                index = i;
                break;
            }
        }

        return index;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The thresholds for quantization.
    private double[] _thresholds;
}
