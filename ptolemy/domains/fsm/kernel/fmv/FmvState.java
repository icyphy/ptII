/* An FSM supporting verification using formal methods.

 Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel.fmv;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FmvAutomaton

/**
 * A Formal Method Verification (FMV) State.
 * 
 * @author Chihhong Patrick Cheng
 * @version $Id$
 * @since Ptolemy II 7.1
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

    /**
     * React to a change in an attribute. If the changed attribute is
     * the <i>refinementName</i> attribute, record the change but do
     * not check whether there is a TypedActor with the specified name
     * and having the same container as the FSMActor containing this
     * state.
     * 
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If thrown by the superclass
     * attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == isRiskAnalysisState) {
            NamedObj container = getContainer();
            // Container might not be an FSMActor if, for example,
            // the state is in a library.
            if (container instanceof FmvAutomaton) {
                if (((BooleanToken) isRiskAnalysisState.getToken())
                        .booleanValue()) {
                    if (((BooleanToken) isReachabilityAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"25\" style=\"fill:green\"/>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    }
                }  else{
                    if (((BooleanToken) isReachabilityAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:green\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                                        + "</svg>\n");
                    }
                }
            }
        } else if (attribute == isReachabilityAnalysisState) {
            NamedObj container = getContainer();
            // Container might not be an FSMActor if, for example,
            // the state is in a library.
            if (container instanceof FmvAutomaton) {
                if (((BooleanToken) isReachabilityAnalysisState.getToken())
                        .booleanValue()) {
                    if (((BooleanToken) isRiskAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"25\" style=\"fill:green\"/>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:green\"/>\n"
                                        + "</svg>\n");
                    }

                } else{
                    if (((BooleanToken) isRiskAnalysisState.getToken())
                            .booleanValue()) {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:red\"/>\n"
                                        + "</svg>\n");
                    } else {
                        _attachText(
                                "_iconDescription",
                                "<svg>\n"
                                        + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                                        + "</svg>\n");
                    }
                }
            }
        }
    }

}
