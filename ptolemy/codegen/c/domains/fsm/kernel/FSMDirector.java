/* Code generator helper class associated with the FSMDirector class.

 Copyright (c) 2005-2010 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.fsm.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor.TransitionRetriever;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// FSMDirector

/**
 Code generator helper class associated with the FSMDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class FSMDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  FSMDirector.
     *  @param director The associated ptolemy.domains.fsm.kernel.FSMDirector
     */
    public FSMDirector(ptolemy.domains.fsm.kernel.FSMDirector director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the code for the firing of actors controlled by this
     *  director.  It generates code for making preemptive transition,
     *  checking if a transition is taken, firing refinements and
     *  making non-preemptive transition.
     *
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {
        ptolemy.domains.fsm.kernel.FSMActor controller = ((ptolemy.domains.fsm.kernel.FSMDirector) getComponent())
                .getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);

        StringBuffer code = new StringBuffer();

        // generate code for preemptive transition
        code.append(_eol + "/* Preemptive Transition */" + _eol + _eol);
        controllerHelper.generateTransitionCode(code,
                new TransitionRetriever() {
                    public Iterator retrieveTransitions(State state) {
                        return state.preemptiveTransitionList().iterator();
                    }
                });

        code.append(_eol);

        // check to see if a preemptive transition is taken
        code.append("if ("
                + controllerHelper.processCode("$actorSymbol(transitionFlag)")
                + " == 0) {" + _eol);

        // generate code for refinements
        _generateRefinementCode(code);

        // generate code for non-preemptive transition
        code.append(_eol + "/* Nonpreemptive Transition */" + _eol + _eol);
        controllerHelper.generateTransitionCode(code,
                new TransitionRetriever() {
                    public Iterator retrieveTransitions(State state) {
                        return state.nonpreemptiveTransitionList().iterator();
                    }
                });

        code.append("}" + _eol);

        return code.toString();
    }

    /** Generate code for the firing of refinements.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    protected void _generateRefinementCode(StringBuffer code)
            throws IllegalActionException {

        ptolemy.domains.fsm.kernel.FSMDirector director = (ptolemy.domains.fsm.kernel.FSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director
                .getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);

        int depth = 1;
        code.append(_getIndentPrefix(depth));
        code.append("switch ("
                + controllerHelper.processCode("$actorSymbol(currentState)")
                + ") {" + _eol);

        Iterator states = controller.entityList().iterator();
        int stateCount = 0;
        depth++;

        while (states.hasNext()) {
            code.append(_getIndentPrefix(depth));
            code.append("case " + stateCount + ":" + _eol);
            stateCount++;

            depth++;

            State state = (State) states.next();
            Actor[] actors = state.getRefinement();

            if (actors != null) {
                for (int i = 0; i < actors.length; i++) {
                    CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actors[i]);

                    // fire the actor
                    code.append(actorHelper.generateFireCode());

                    // update buffer offset after firing each actor once
                    int[][] rates = actorHelper.getRates();
                    Iterator ports = ((Entity) actors[i]).portList().iterator();
                    int portNumber = 0;
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        if (rates != null) {
                            code.append("switch ("
                                    + actorHelper.processCode("$actorSymbol("
                                            + "currentConfiguration)") + ") {"
                                    + _eol);
                            for (int k = 0; k < rates.length; k++) {
                                code.append("case " + k + ":" + _eol);
                                if (rates[k] != null) {
                                    int rate = rates[k][portNumber];
                                    if (port.isInput()) {
                                        _updatePortOffset(port, code, rate);
                                    } else {
                                        _updateConnectedPortsOffset(port, code,
                                                rate);
                                    }
                                    code.append("break;" + _eol);
                                }
                            }
                            code.append("}" + _eol);
                        } else {
                            int rate = DFUtilities.getRate(port);
                            if (port.isInput()) {
                                _updatePortOffset(port, code, rate);
                            } else {
                                _updateConnectedPortsOffset(port, code, rate);
                            }
                        }
                        portNumber++;
                    }

                }
            }

            code.append(_getIndentPrefix(depth));
            code.append("break;" + _eol); //end of case statement
            depth--;
        }

        depth--;
        code.append(_getIndentPrefix(depth));
        code.append("}" + _eol); //end of switch statement
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            // modal controller is not used as a stand-alone actor.
            if (((ptolemy.domains.fsm.kernel.FSMDirector) _director)
                    .getController() == actor) {
                continue;
            }
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(actorHelper.generateFireFunctionCode());
        }
        return code.toString();
    }

    // FIXME: Having this code here breaks the test cases under
    // $PTII/codegen/c/domains/fsm/test/. This code is probably
    // specific to a target (e.g. OpenRTOS or PRET), so it should
    // be moved into an subclass which overrides this method. The
    // subclass should be put under the target-specific packages.
    //    public String _generateActorCode() throws IllegalActionException{
    //        StringBuffer code = new StringBuffer();
    //        ptolemy.domains.fsm.kernel.FSMDirector director = (ptolemy.domains.fsm.kernel.FSMDirector) getComponent();
    //        ptolemy.domains.fsm.kernel.FSMActor controller = director
    //        .getController();
    //        //FSMActor controllerHelper = (FSMActor) _getHelper(controller);
    //
    //        //boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
    //        //        .booleanValue();
    //
    //        int depth = 1;
    //
    //        Iterator states = controller.entityList().iterator();
    //        int stateCount = 0;
    //        depth++;
    //
    //        while (states.hasNext()) {
    //            // code.append(_getIndentPrefix(depth));
    //            //code.append("case " + stateCount + ":" + _eol);
    //            stateCount++;
    //
    //            depth++;
    //
    //            State state = (State) states.next();
    //            Actor[] actors = state.getRefinement();
    //
    //            if (actors != null) {
    //                for (int i = 0; i < actors.length; i++) {
    //                    CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actors[i]);
    //
    //                    code.append("void "+_getActorName(actors[i])+"(){");
    //
    //                    code.append(actorHelper.generateFireCode());
    //                    code.append(actorHelper.generateTypeConvertFireCode());
    //                    code.append(_eol+"}"+_eol);
    //                }}
    //        }
    //        return code.toString();
    //    }
    //
    //
    //    private String _getActorName(Actor actor) {
    //        String actorFullName = actor.getFullName();
    //        actorFullName = actorFullName.substring(1,actorFullName.length());
    //        actorFullName = actorFullName.replace('.', '_');
    //        actorFullName = actorFullName.replace(' ', '_');
    //        return actorFullName;
    //    }
    //
    //
    //
    //    public String generatePreinitializeCode()throws IllegalActionException{
    //        StringBuffer code = new StringBuffer();
    //        code.append(super.generatePreinitializeCode());
    //
    //        code.append(_generateActorCode());
    //
    //        return code.toString();
    //    }
    //

}
