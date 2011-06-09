/* A Formal Method Verification (FMV) State supporting graphical specification generation for safety or reachability analysis.

 Copyright (c) 2008-2009 The Regents of the University of California.
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

package ptolemy.domains.modal.kernel.fmv;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FmvAutomaton

/**
 * A Formal Method Verification (FMV) State. It supports graphical specification
 * generation for safety or reachability analysis.
 *
 * @author Chihhong Patrick Cheng
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
public class FmvState extends State {

    /**
     * Create an FmvState in the specified container with the specified
     * name. The name must be unique within the container or an exception is
     * thrown. The container argument must not be null, or a
     * NullPointerException will be thrown.
     *
     * @param container The container.
     * @param name The name of this automaton within the container.
     * @exception IllegalActionException If the entity cannot be
     * contained by the proposed container.
     * @exception NameDuplicationException If the name coincides with
     * an entity already in the container.
     */
    public FmvState(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isRiskAnalysisState = new Parameter(this, "isRiskAnalysisState");
        isRiskAnalysisState.setTypeEquals(BaseType.BOOLEAN);
        isRiskAnalysisState.setExpression("false");
        isReachabilityAnalysisState = new Parameter(this,
                "isReachabilityAnalysisState");
        isReachabilityAnalysisState.setTypeEquals(BaseType.BOOLEAN);
        isReachabilityAnalysisState.setExpression("false");

    }

    /** A boolean parameter indicating that this state is a risk state.
     *  The default value is false.
     */
    public Parameter isRiskAnalysisState;

    /** A boolean parameter indicating whether this state is a
     *  reachability analysis state.   The default value is false.
     */
    public Parameter isReachabilityAnalysisState;

}
