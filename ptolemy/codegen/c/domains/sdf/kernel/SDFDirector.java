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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
//// SDFDirector

/**
   Code generator helper associated with the SDFDirector class. This class
   is also associated with a code generator.
   FIXME: Should associated with a static scheduling code generator.

   @author Ye Zhou
   @version $Id$
   @since Ptolemy II 5.0
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (eal)
*/

public class SDFDirector extends Director {

    /** Construct the code generator helper associated with the given SDFDirector.
     *  @param component The associated component.
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
                // Declare iteration outside of the loop to avoid 
                // "error: `for' loop initial declaration used outside C99 mode"
                // with gcc-3.3.3
                code.append("int iteration = 0;\n"
                            + "for (iteration = 0; iteration < "
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
                    Set inputAndOutputPortsSet = new HashSet();
                    inputAndOutputPortsSet.addAll(actor.inputPortList());
                    inputAndOutputPortsSet.addAll(actor.outputPortList());
                    Iterator inputAndOutputPorts = inputAndOutputPortsSet.iterator();
                    while (inputAndOutputPorts.hasNext()) {
                        IOPort port = (IOPort) inputAndOutputPorts.next();
                        for (int j = 0; j < port.getWidth(); j ++) {
                            //Channel channel = helperObject.getChannel(port, j);
                            int offset = helperObject.getOffset(port, j);
                            // FIXME: temporarily we don't consider cyclic buffer.
                            //offset = offset + DFUtilities.getRate(inputPort);
                            offset = (offset + DFUtilities.getRate(port))
                                % helperObject.getBufferSize(port);
                            helperObject.setOffset(port, j, offset);
                        }
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

    /** Generate the initialize code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the base class throws it.
     */
    public String generateInitializeCode() throws IllegalActionException {
        
        String initializeCode = super.generateInitializeCode();
        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof SampleDelay) {
                ArrayToken initialTokens = (ArrayToken) 
                        ((SampleDelay) actor).initialOutputs.getToken();
                int NumberOfInitialTokens = initialTokens.length();
                CodeGeneratorHelper actorHelper
                        = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                Iterator sinkChannels
                        = actorHelper.getSinkChannels(
                        ((SampleDelay) actor).output, 0).iterator();
                while (sinkChannels.hasNext()) {
                    Channel sinkChannel = (Channel) sinkChannels.next();
                    Actor sinkActor = (Actor) sinkChannel.port.getContainer();
                    CodeGeneratorHelper sinkActorHelper
                            = (CodeGeneratorHelper) _getHelper
                            ((NamedObj) sinkActor);
                    sinkActorHelper.setOffset(sinkChannel.port,
                            sinkChannel.channelNumber, NumberOfInitialTokens);
                }
            }
        }
        return initializeCode;
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number).
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel.
     *  @exception IllegalActionException if more than one relation is
     *   connected to the given channel, or if the bufferSize variable
     *   does not contain a token.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        int bufferSize = 1;
        List connectedRelations = getConnectedRelations(port, channelNumber);
        if (connectedRelations.size() > 1) {
            throw new IllegalActionException(super.getComponent(),
                    "more than one relation is connected to " 
                    + port.getFullName() + ", " + channelNumber);
        }
        if (connectedRelations.size() == 0) {
            return bufferSize;
        }
        IORelation relation = (IORelation) connectedRelations.get(0);
        Attribute buffer = relation.getAttribute("bufferSize");
        if (buffer != null) {
            bufferSize
                = ((IntToken) ((Variable) buffer).getToken()).intValue();
        }
        return bufferSize;
    }
    
    /** Get the set of relations connected to the given channel (i.e., given
     *  a port and a channel number). The set should contains at most one
     *  relation.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The set of relations that connect to the given channel.
     */
    public List getConnectedRelations(IOPort port, int channelNumber) {
        List connectedRelations = new LinkedList();
        Iterator relations = port.linkedRelationList().iterator();
        int channel = 0;
        while (relations.hasNext()) {
            IORelation relation = (IORelation) relations.next();
            int width = relation.getWidth();
            for (int i = 0; i < width; i ++, channel ++) {
                if (channel == channelNumber) {
                    connectedRelations.add(relation);
                }
            }
        }
        return connectedRelations;
    }
}
