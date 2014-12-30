/* Provide an interface for representing derivative functions.

Copyright (c) 2014 The Regents of the University of California.
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


package org.ptolemy.qss.util;


import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// DerivativeFcn


/** Provide an interface for representing derivative functions.
 *
 * <p>The interface encapsulates the derivatives of one or more state variables,
 * <i>x</i>, as:</p>
 * <p><i>xdot = f{t, x, u}</i></p>
 *
 * <p>where</p>
 * <ul>
 * <li><i>t</i>, simulation time.</li>
 * <li><i>x</i>, vector of state variables, <i>x{t}</i>.</li>
 * <li><i>u</i>, vector of input variables, <i>u{t}</i>.</li>
 * <li><i>xdot</i>, vector of time rates of change of the state variables.
 * That is, <i>xdot = dx/dt</i>.</li>
 * <li><i>f</i>, vector-valued derivative function.</li>
 * <li>The notation <i>g{y}</i> means that <i>g</i> is a function
 * of <i>y</i>.</li>
 * </ul>
 *
 *
 * <h2>Expected implementation conventions</h2>
 *
 * <p>An implementing class should enforce:</p>
 * <ul>
 * <li>The count of state variables <i>Nx > 0</i>.</li>
 * <li>The count of input variables <i>Nu >= 0</i>.</li>
 * </ul>
 *
 * @author David M. Lorenzetti
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public interface DerivativeFcn {

    // FIXME: Rename this to DerivativeFunction because in Ptolemy,
    // class names should consist of complete words.

    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods


    /** Return the count of state variables.
     *
     * <p>Expect <code>0 &lt; this.getStateCt()</code>.</p>
     *
     * @return Count of state variables.
     */
    public int getStateCt();


    /** Return the count of input variables.
     *
     * <p>Expect <code>0 &le; this.getInputVarCt()</code>.</p>
     *
     * @return Count of input variables.
     */
    public int getInputVarCt();


	/**
	 * Indicate existence of directional derivatives.
	 *
	 * <p>
	 * Prescribed by interface <code>DerivativeFcn</code>.
	 * </p>
	 *
	 * @return True if directional derivatives are provided.
	 */
   public boolean getProvidesDirectionalDerivatives();


    /** Evaluate the derivative function.
     *
     * <p>Expected implementation conventions:</p>
     * <ul>
     * <li>The state variable vector has at least one element.</li>
     * <li>The method does not change any entry in <code>uu</code>.</li>
     * <li>If the <code>DerivativeFcn</code> does not take any input variables,
     * then <code>uu == null</code>.</li>
     * </ul>
     *
     * @param time Simulation time.
     * @param xx Vector of state variables at <code>time</code>.
     * @param uu Vector of input variables at <code>time</code>.
     * @param xdot (output) Vector of time rates of change of the state variables at <code>time</code>.
     * @return Success (0 for success, else user-defined error code).
     */
    public int evalDerivs(final Time time, double[] xx, double[] uu,
        final double[] xdot)
        throws Exception;

    /**
     * Evaluate directional derivative function.
     *
     * <p>
     * Prescribed by interface <code>DerivativeFcn</code>.
     * </p>
     *
     * @param idx The continuous state index
     * @param xx_dot The vector of first state derivatives with respect to time
     * @param uu_dot The vector of first input derivatives with respect to time
     * @return xdot_dot The second derivative with respect to time.
     * @throws IllegalActionException
     */
   public double evalDirectionalDerivs(int idx, double[] xx_dot, double[] uu_dot)
       throws Exception;


}  // End interface DerivativeFcn.
