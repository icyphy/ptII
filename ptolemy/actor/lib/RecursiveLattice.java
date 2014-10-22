/* A recursive (all pole) lattice filter.

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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// RecursiveLattice

/**
 A recursive (all-pole) filter with a lattice structure.
 The coefficients of such a filter are called "reflection coefficients."
 Recursive lattice filters are typically used as synthesis filters for
 random processes because it is easy to ensure that they are stable.
 A recursive lattice filter is stable if its reflection
 coefficients are all less than unity in magnitude.  To get the
 reflection coefficients for a linear predictor for a particular
 random process, you can use the LevinsonDurbin actor.
 The inputs and outputs are of type double.
 <p>
 The default reflection coefficients correspond to the following
 transfer function:
 <pre>
                           1
 H(z) =  --------------------------------------
        1 - 2z<sup>-1</sup> + 1.91z<sup>-2</sup> - 0.91z<sup>-3</sup> + 0.205z<sup>-4</sup>
 </pre>
 <p>
 The structure of the filter is as follows:
 <pre>
      y[0]          y[1]                 y[n-1]           y[n]
 X(n) ---(+)-&gt;--o--&gt;----(+)-&gt;--o---&gt;-- ... -&gt;--(+)-&gt;--o---&gt;---o---&gt;  Y(n)
           \   /          \   /                  \   /        |
          +Kn /        +Kn-1 /                  +K1 /         |
             X              X                      X          |
          -Kn \        -Kn-1 \                  -K1 \         V
           /   \          /   \                  /   \        |
         (+)-&lt;--o--[z]--(+)-&lt;--o--[z]- ... -&lt;--(+)-&lt;--o--[z]--/
                w[1]           w[2]                   w[n]
 </pre>
 where the [z] are unit delays and the (+) are adders
 and "y" and "w" are variables representing the state of the filter.
 <p>
 The reflection (or partial-correlation (PARCOR))
 coefficients should be specified
 right to left, K1 to Kn as above.
 Using exactly the same coefficients in the
 Lattice actor will result in precisely the inverse transfer function.
 <p>
 Note that the definition of reflection coefficients is not quite universal
 in the literature. The reflection coefficients in reference [2]
 are the negative of the ones used by this actor, which
 correspond to the definition in most other texts,
 and to the definition of partial-correlation (PARCOR)
 coefficients in the statistics literature.
 The signs of the coefficients used in this actor are appropriate for values
 given by the LevinsonDurbin actor.
 <p>
 <b>References</b>
 <p>[1]
 J. Makhoul, "Linear Prediction: A Tutorial Review",
 <i>Proc. IEEE</i>, Vol. 63, pp. 561-580, Apr. 1975.
 <p>[2]
 S. M. Kay, <i>Modern Spectral Estimation: Theory & Application</i>,
 Prentice-Hall, Englewood Cliffs, NJ, 1988.

 @see ptolemy.actor.lib.IIR
 @see ptolemy.actor.lib.LevinsonDurbin
 @see ptolemy.actor.lib.Lattice
 @see ptolemy.domains.sdf.lib.VariableRecursiveLattice
 @author Edward A. Lee, Christopher Hylands, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class RecursiveLattice extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecursiveLattice(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        reflectionCoefficients = new Parameter(this, "reflectionCoefficients");

        // Note that setExpression() will call attributeChanged().
        reflectionCoefficients
                .setExpression("{0.804534, -0.820577, 0.521934, -0.205}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The reflection coefficients.  This is an array of doubles with
     *  default value {0.804534, -0.820577, 0.521934, -0.205}. These
     *  are the reflection coefficients for the linear predictor of a
     *  particular random process.
     */
    public Parameter reflectionCoefficients;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>reflectionCoefficients</i> parameter,
     *  then reallocate the arrays to use.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == reflectionCoefficients) {
            ArrayToken value = (ArrayToken) reflectionCoefficients.getToken();
            int valueLength = value.length();

            if (_backward == null || valueLength != _backward.length - 1) {
                // Need to allocate or reallocate the arrays.
                _backward = new double[valueLength + 1];
                _backwardCache = new double[valueLength + 1];
                _forward = new double[valueLength + 1];
                _forwardCache = new double[valueLength + 1];
                _reflectionCoefficients = new double[valueLength];
            }

            for (int i = 0; i < valueLength; i++) {
                _reflectionCoefficients[i] = ((DoubleToken) value.getElement(i))
                        .doubleValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RecursiveLattice newObject = (RecursiveLattice) super.clone(workspace);

        int forwardLength = 0;
        if (_forward == null) {
            try {
                ArrayToken value = (ArrayToken) reflectionCoefficients
                        .getToken();
                forwardLength = value.length() + 1;
            } catch (IllegalActionException ex) {
                throw new CloneNotSupportedException("Failed to clone: " + ex);
            }
        } else {
            forwardLength = _forward.length;
        }

        int backwardLength = 0;
        if (_backward == null) {
            try {
                ArrayToken value = (ArrayToken) reflectionCoefficients
                        .getToken();
                backwardLength = value.length() + 1;
            } catch (IllegalActionException ex) {
                throw new CloneNotSupportedException("Failed to clone: " + ex);
            }
        } else {
            backwardLength = _backward.length;
        }
        newObject._backward = new double[backwardLength];
        newObject._backwardCache = new double[backwardLength];
        newObject._forward = new double[forwardLength];
        newObject._forwardCache = new double[forwardLength];
        newObject._reflectionCoefficients = new double[forwardLength - 1];

        if (_backward != null) {
            System.arraycopy(_backward, 0, newObject._backward, 0,
                    backwardLength);
        }
        if (_backwardCache != null) {
            System.arraycopy(_backwardCache, 0, newObject._backwardCache, 0,
                    _backwardCache.length);
        }
        if (_forward != null) {
            System.arraycopy(_forward, 0, newObject._forward, 0,
                    _forward.length);
        }
        if (_forwardCache != null) {
            System.arraycopy(_forwardCache, 0, newObject._forwardCache, 0,
                    _forwardCache.length);
        }
        if (_reflectionCoefficients != null) {
            System.arraycopy(_reflectionCoefficients, 0,
                    newObject._reflectionCoefficients, 0,
                    _reflectionCoefficients.length);
        }

        try {
            ArrayToken value = (ArrayToken) reflectionCoefficients.getToken();
            for (int i = 0; i < value.length(); i++) {
                _reflectionCoefficients[i] = ((DoubleToken) value.getElement(i))
                        .doubleValue();
            }
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
        return newObject;
    }

    /** Consume one input token, if there is one, and produce one output
     *  token.  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            DoubleToken inputValue = (DoubleToken) input.get(0);

            // NOTE: The following code is ported from Ptolemy Classic.
            double k;
            int M = _backward.length - 1;

            // Forward prediction error
            _forwardCache[0] = inputValue.doubleValue(); // _forward(0) = x(n)

            for (int i = 1; i <= M; i++) {
                k = _reflectionCoefficients[M - i];
                _forwardCache[i] = k * _backwardCache[i] + _forwardCache[i - 1];
            }

            output.broadcast(new DoubleToken(_forwardCache[M]));

            // Backward:  Compute the w's for the next round
            for (int i = 1; i < M; i++) {
                k = -_reflectionCoefficients[M - 1 - i];
                _backwardCache[i] = _backwardCache[i + 1] + k
                        * _forwardCache[i + 1];
            }

            _backwardCache[M] = _forwardCache[M];
        }
    }

    /** Initialize the state of the filter.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Invoke any initializable methods.
        super.initialize();
        for (int i = 0; i < _forward.length; i++) {
            _forward[i] = 0.0;
            _forwardCache[i] = 0.0;
            _backward[i] = 0.0;
            _backwardCache[i] = 0.0;
        }
    }

    /** Update the backward and forward prediction errors that
     *  were generated in fire() method.
     *  @return False if the number of iterations matches the number requested.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        System.arraycopy(_backwardCache, 0, _backward, 0, _backwardCache.length);
        System.arraycopy(_forwardCache, 0, _forward, 0, _forwardCache.length);
        return super.postfire();
    }

    /** Check to see if this actor is ready to fire.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // Get a copy of the current filter state that we can modify.
        System.arraycopy(_backward, 0, _backwardCache, 0, _backwardCache.length);
        System.arraycopy(_forward, 0, _forwardCache, 0, _forwardCache.length);
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // We set these to null and then the constructor calls setExpression()
    // which in turn calls attributeChanged() which then allocates
    // these arrays.
    // Backward prediction errors, represented by "w" in the class
    // comment.
    private double[] _backward = null;

    // Cache of backward prediction errors, represented by "w" in the class
    // comment.  The fire() method updates _forwardCache and postfire()
    // copies _forwardCache to _forward so this actor will work in domains
    // like SR.
    private double[] _backwardCache = null;

    // Forward prediction errors, represented by "y" in the class
    // comment.
    private double[] _forward = null;

    // Cache of forward prediction errors, represented by "y" in the class
    // comment.  The fire() method updates _forwardCache and postfire()
    // copies _forwardCache to _forward so this actor will work in domains
    // like SR.
    private double[] _forwardCache = null;

    // Cache of reflection coefficients.
    private double[] _reflectionCoefficients = null;
}
