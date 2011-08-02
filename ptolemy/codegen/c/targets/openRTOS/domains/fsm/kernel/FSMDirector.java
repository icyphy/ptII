/* Code generator helper class associated with the FSMDirector class.

 Copyright (c) 2009-2011 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.openRTOS.domains.fsm.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// FSMDirector

/**
 Code generator helper associated with the OpenRTOS FSMDirector class. This class
 is also associated with a code generator.

 @author  Shanna-Shaye Forbes,Ben Lickly
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class FSMDirector extends
        ptolemy.codegen.c.domains.fsm.kernel.FSMDirector {

    /** Construct the code generator helper associated with the given
     *  FSMDirector.
     *  @param fsmDirector The associated
     *  ptolemy.domains.fsm.kernel.FSMDirector
     */
    public FSMDirector(ptolemy.domains.fsm.kernel.FSMDirector fsmDirector) {
        super(fsmDirector);
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
            //CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            //code.append(actorHelper.generateFireFunctionCode());
            //code.append(_getActorName(actor)+"();");
            code.append("//generateFireFunctionCode method called ");
        }
        return code.toString();
    }

    /**
     * Generate the preinitialization code for the director.
     * @return string containing the preinitializaton code
     * @exception IllegalActionException If thrown by the superclass or thrown
     * while generating code for the director.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append(_eol
                + "//before call to generateActorCode in FSM Constructor"
                + _eol);
        code.append(_generateActorCode());
        code.append(_eol
                + "//after call to generateActorCode in FSM Constructor" + _eol);
        return code.toString();
    }

    /**
     * Generate the code to transfer outputs from a port to its receiver.
     * @param outputPort - the port generating output
     * @param code - StringBuffer the generated code should appended to.
     * @exception IllegalActionException If thrown by the superclass.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(_eol
                + "//generate transferOutputsCode inside OPENRTOS FSM  director called."
                + _eol);
        super.generateTransferOutputsCode(outputPort, code);

    }

    /**
     * Return the worst case execution time (WCET) seen by this
     * director.
     * @return The Worst Case Execution Time (WCET).
     * @exception IllegalActionException If there is a problem determining
     * the WCET or a problem accessing the model.
     */
    public double getWCET() throws IllegalActionException {
        ptolemy.domains.fsm.kernel.FSMDirector director = (ptolemy.domains.fsm.kernel.FSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director
                .getController();
        //int depth = 1;

        Iterator states = controller.deepEntityList().iterator();
        //int stateCount = 0;
        //depth++;
        double largestWCET = 0.0;

        while (states.hasNext()) {

            //stateCount++;

            //depth++;

            State state = (State) states.next();
            Actor[] actors = state.getRefinement();
            Set<Actor> actorsSet = new HashSet();
            ;
            if (actors != null) {
                for (int i = 0; i < actors.length; i++) {
                    actorsSet.add(actors[i]);
                }
            }
            for (Actor actor : actorsSet) {
                ptolemy.codegen.actor.Director df = new ptolemy.codegen.actor.Director(
                        actor.getDirector());
                double localWCET = df.getWCET();
                if (localWCET > largestWCET) {
                    largestWCET = localWCET;
                }

            }
        }
        if (_debugging) {
            _debug("fsm director has wcet of " + largestWCET);
        }
        return largestWCET;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
                    //code.append(actorHelper.generateFireCode());
                    code.append(_getActorName(actors[i]) + "();" + _eol);

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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Generate code for all the actors associated with the given FSMDirector.
     * @return String containing the actor code.
     * @exception IllegalActionException If throw while accessing the model.
     */
    private String _generateActorCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.domains.fsm.kernel.FSMDirector director = (ptolemy.domains.fsm.kernel.FSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director
                .getController();
        //int depth = 1;

        //Iterator states = controller.entityList().iterator();
        Iterator states = controller.deepEntityList().iterator();
        //int stateCount = 0;
        //depth++;

        while (states.hasNext()) {
            // code.append(_getIndentPrefix(depth));
            //code.append("case " + stateCount + ":" + _eol);
            //stateCount++;

            //depth++;

            State state = (State) states.next();
            Actor[] actors = state.getRefinement();
            Set<Actor> actorsSet = new HashSet();
            ;
            if (actors != null) {
                for (int i = 0; i < actors.length; i++) {
                    actorsSet.add(actors[i]);
                }
            }

            if (actors != null) {
                //for (int i = 0; i < actors.length; i++) {
                Iterator actorIterator = actorsSet.iterator();
                Actor actors2;
                while (actorIterator.hasNext()) {
                    actors2 = (Actor) actorIterator.next();
                    CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actors2);
                    if (actors2.getDirector().getFullName().contains("Giotto") == false) {
                        //code.append("void "+_getActorName(actors2)+"(){");
                        code.append(actorHelper.generateFireFunctionCode()); // this was there initially and it works with SDF
                        code.append(actorHelper.generateTypeConvertFireCode());
                        //code.append(_eol+"}"+_eol);
                    } else {
                        code.append(_eol
                                + "//modal model contains giotto director"
                                + _eol);

                    }
                }
            }
        }

        return code.toString();
    }

    /** Generates the name of an actor
     * @param actor - The actor whose name is to be determined
     * @return string with the actors full name
     * */
    private String _getActorName(Actor actor) {
        String actorFullName = actor.getFullName();
        actorFullName = actorFullName.substring(1, actorFullName.length());
        actorFullName = actorFullName.replace('.', '_');
        actorFullName = actorFullName.replace(' ', '_');
        return actorFullName;
    }
}
