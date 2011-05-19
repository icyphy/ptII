/* Code generator helper class associated with the FSMDirector class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.pret.domains.fsm.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////////
//// FSMDirector

/**
 Code generator helper associated with the PRET FSMDirector class. This class
 is also associated with a code generator.

 @author  Shanna-Shaye Forbes,Ben Lickly
 @version $Id$
 @since Ptolemy II 7.2
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class FSMDirector extends
        ptolemy.codegen.c.domains.fsm.kernel.FSMDirector {

    /**
     * Construct the code generator helper associated with the given
     * FSMDirector.
     * @param fsmDirector The associated ptolemy.domains.fsm.kernel.FSMDirector
     */
    public FSMDirector(ptolemy.domains.fsm.kernel.FSMDirector fsmDirector) {
        super(fsmDirector);
    }

    /**
     * Generate the preInitialization code for the director.
     * @return string containing the preinitializaton code
     * @exception IllegalActionException If thrown by the superclass or
     * by while generating the actor code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append(_eol + "//before call to generateActorCode" + _eol);
        code.append(_generateActorCode());
        code.append(_eol + "//after call to generateActorCode" + _eol);

        return code.toString();
    }

    /*
     * Generates the code to transfer outputs from a port to its receiver.
     * @param outputPort - the port generating output
     * @param code - StringBuffer the generated code should appended to.
     * @exception IllegalActionAction If thrown by the super class
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code
                .append(_eol
                        + "//generate transferOutputsCode inside pret FSM  director called."
                        + _eol);
        super.generateTransferOutputsCode(outputPort, code);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Generate code for all the actors associated with the given FSMDirector
     * @return String containing the actor code.
     */
    private String _generateActorCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.domains.fsm.kernel.FSMDirector director = (ptolemy.domains.fsm.kernel.FSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director
                .getController();
        //FSMActor controllerHelper = (FSMActor) _getHelper(controller);

        //boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
        //        .booleanValue();

        int depth = 1;

        //Iterator states = controller.entityList().iterator();
        Iterator states = controller.deepEntityList().iterator();
        int stateCount = 0;
        depth++;

        while (states.hasNext()) {
            // code.append(_getIndentPrefix(depth));
            //code.append("case " + stateCount + ":" + _eol);
            stateCount++;

            depth++;

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
                    //code.append("void "+_getActorName(actors2)+"(){");
                    //code.append(actorHelper.generateFireCode());
                    code.append(actorHelper.generateFireFunctionCode());
                    code.append(actorHelper.generateTypeConvertFireCode());
                    //code.append(_eol+"}"+_eol);
                }
            }
        }

        return code.toString();
    }
}
