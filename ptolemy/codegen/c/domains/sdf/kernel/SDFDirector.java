/* Code generator helper class associated with the SDFDirector class.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.c.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.ComponentCodeGenerator;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
//// SDFDirector

/**
   Code generator helper associated with the SDFDirector class. This class
   is also associated with a code generator.
   FIXME: Should associated with a static scheduling code generator.

   @author Ye Zhou, Edward A. Lee
   @version $Id: SDFDirector.java
   @since Ptolemy II 5.0
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (eal)
*/

public class SDFDirector extends Director {

    /** Construct the code generator helper associated with the given SDFDirector.
     *  @param component The associated componenet.
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    /** Generatre the code for the firing of actors according to the SDF
     *  schedule.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be 
     *   fired cannot find its associated helper.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        Attribute iterations = getComponent().getAttribute("iterations");
        if (iterations != null) {
            int iterationCount = ((IntToken)((Variable)iterations).getToken()).intValue();
            if (iterationCount <= 0) {
                code.append("while (true) {\n");
            } else {
                code.append("for (int iteration = 0; iteration < "
                        + iterationCount + "; iteration ++) {\n");
            }
            // generate FireCode here;
            Schedule schedule = ((StaticSchedulingDirector) getComponent())
                    .getScheduler().getSchedule();
            
            Iterator actorsToFire = schedule.iterator();
            while (actorsToFire.hasNext()) {
                Firing firing = (Firing) actorsToFire.next();
                Actor actor = firing.getActor();
                // FIXME: Before looking for a helper class, we should check
                // to see whether the actor contains a code generator attribute.
                // If it does, we should use that as the helper.
                CodeGeneratorHelper helperObject
                        = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                Variable firings = (Variable) ((NamedObj) actor)
                        .getAttribute("firingsPerIteration");
                int firingsPerIteration
                        = ((IntToken) firings.getToken()).intValue();
                for (int i = 0; i < firing.getIterationCount(); i ++) {
                    helperObject.generateFireCode(code);
                    // FIXME: Each time fire an actor, increase the offset of
                    // each of its port by the port rate.
                    Iterator inputPorts = actor.inputPortList().iterator();
                    while (inputPorts.hasNext()) {
                        IOPort inputPort = (IOPort) inputPorts.next();
                        int offset = ((CodeGeneratorHelper) helperObject)
                                .getOffset(inputPort);
                        // FIXME: temporarily we don't consider cyclic buffer.
                        //offset = offset + DFUtilities.getRate(inputPort);
                        offset = (offset + DFUtilities.getRate(inputPort))
                                % ((CodeGeneratorHelper) helperObject).getBufferSize(inputPort);
                        ((CodeGeneratorHelper) helperObject)
                                .setOffset(inputPort, offset);
                    }
                    // FIXME: Some day I am going to add input ports and output ports together.
                    Iterator outputPorts = actor.outputPortList().iterator();
                    while (outputPorts.hasNext()) {
                        IOPort outputPort = (IOPort) outputPorts.next();
                        int offset = ((CodeGeneratorHelper) helperObject)
                                .getOffset(outputPort);
                        //offset = offset + DFUtilities.getRate(outputPort);
                        offset = (offset + DFUtilities.getRate(outputPort))
                                % ((CodeGeneratorHelper) helperObject).getBufferSize(outputPort);
                        ((CodeGeneratorHelper) helperObject)
                                .setOffset(outputPort, offset);
                    }
                }
            }
            code.append("}\n");
        } else {
            throw new IllegalActionException(getComponent(),
                    "The SDF Director does not have an attribute"
                    + "iterations");
        }
    }

    
    
    /** Generate the initialize code for this director. First set the buffer
     *  sizes of each port of the actors under the associated director. 
     *  The initialize code for the director is generated by appending the
     *  intialize code for each actor.
     *  @return The generated initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* The initialization of the director. */\n");
        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj)actor);
            // Set the buffer size for each port of this actor.
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                int bufferSize = getBufferSize(inputPort);
                ((CodeGeneratorHelper) helperObject)
                        .setBufferSize(inputPort, bufferSize);
            }
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                int bufferSize = getBufferSize(outputPort);
                ((CodeGeneratorHelper) helperObject)
                        .setBufferSize(outputPort, bufferSize);
            }
            // Set the offset of each port of this actor to be 0.
            ((CodeGeneratorHelper)helperObject).resetOffsets();
            code.append(helperObject.generateInitializeCode());
        }
        return code.toString();
    }

    /** Return the buffer size of a given port.
     *  @param port The given port.
     *  @return The buffer size of the given port.
     *  @exception IllegalActionException if te firingsPerIteration variable
     *   does not contain a token.
     */
    public int getBufferSize(IOPort port) 
            throws IllegalActionException {
        int bufferSize = 1;
        Variable firings =
            (Variable)port.getContainer().getAttribute("firingsPerIteration");
        int firingsPerIteration = 1;
        if (firings != null) {
            firingsPerIteration = ((IntToken)firings.getToken()).intValue();
        }
        if (port.isInput() || port.getRemoteReceivers().length > 0) {
            bufferSize = firingsPerIteration
                    * DFUtilities.getRate(port);
        }
        return bufferSize;
    }
}
