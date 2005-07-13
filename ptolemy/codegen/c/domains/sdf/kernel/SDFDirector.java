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
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFReceiver;
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
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector extends Director {
    /** Construct the code generator helper associated with the given
     *  SDFDirector.
     *  @param component The associated component.
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the code for the firing of actors according to the SDF
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
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();

            if (iterationCount <= 0) {
                code.append("while (true) {\n");
            } else {
                // Declare iteration outside of the loop to avoid
                // "error: `for' loop initial declaration used outside C99
                // mode" with gcc-3.3.3
                code.append("for (iteration = 0; iteration < " + iterationCount
                        + "; iteration ++) {\n");
            }

            // Generate code for one iteration.
            Schedule schedule = ((StaticSchedulingDirector) getComponent())
                    .getScheduler().getSchedule();

            Iterator actorsToFire = schedule.iterator();

            while (actorsToFire.hasNext()) {
                Firing firing = (Firing) actorsToFire.next();
                Actor actor = firing.getActor();

                // FIXME: Before looking for a helper class, we should check to
                // see whether the actor contains a code generator attribute.
                // If it does, we should use that as the helper.
                CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

                for (int i = 0; i < firing.getIterationCount(); i++) {
                    helperObject.generateFireCode(code);

                    Set inputAndOutputPortsSet = new HashSet();
                    inputAndOutputPortsSet.addAll(actor.inputPortList());
                    inputAndOutputPortsSet.addAll(actor.outputPortList());

                    Iterator inputAndOutputPorts = inputAndOutputPortsSet
                            .iterator();

                    while (inputAndOutputPorts.hasNext()) {
                        IOPort port = (IOPort) inputAndOutputPorts.next();

                        for (int j = 0; j < port.getWidth(); j++) {
                            // Update the offset for each channel.
                            if (helperObject.getOffset(port, j) instanceof Integer) {
                                int offset = ((Integer) helperObject.getOffset(
                                        port, j)).intValue();
                                offset = (offset + DFUtilities.getRate(port))
                                        % helperObject.getBufferSize(port, j);
                                helperObject.setOffset(port, j, new Integer(
                                        offset));
                            } else {
                                // FIXME: didn't write "% portBufferSize" here.
                                String temp = (String) helperObject.getOffset(
                                        port, j)
                                        + " += "
                                        + DFUtilities.getRate(port)
                                        + ";\n";
                                code.append(temp);
                            }
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
        StringBuffer initializeCode = new StringBuffer();
        initializeCode.append(super.generateInitializeCode());

        Iterator actors = ((CompositeActor) _codeGenerator.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            Variable firings = (Variable) ((NamedObj) actor)
                    .getAttribute("firingsPerIteration");
            int firingsPerIteration = ((IntToken) firings.getToken())
                    .intValue();
            Set ioPortsSet = new HashSet();
            ioPortsSet.addAll(actor.inputPortList());
            ioPortsSet.addAll(actor.outputPortList());

            Iterator ioPorts = ioPortsSet.iterator();

            while (ioPorts.hasNext()) {
                IOPort port = (IOPort) ioPorts.next();
                int totalTokens = DFUtilities.getRate(port)
                        * firingsPerIteration;

                for (int channel = 0; channel < port.getWidth(); channel++) {
                    int portOffset = totalTokens % getBufferSize(port, channel);

                    if (portOffset != 0) {
                        // Increase the buffer size of that channel to the
                        // power of two.
                        int bufferSize = _ceilToPowerOfTwo(getBufferSize(port,
                                channel));
                        actorHelper.setBufferSize(port, channel, bufferSize);

                        // Declare the channel offset variables.
                        StringBuffer channelOffset = new StringBuffer();
                        channelOffset.append(port.getFullName().replace('.',
                                '_'));

                        if (port.getWidth() > 1) {
                            channelOffset.append("_" + channel);
                        }

                        channelOffset.append("_offset");

                        String channelOffsetVariable = channelOffset.toString();

                        // At this point, all offsets are 0 or the number of
                        // initial tokens of SampleDelay.
                        initializeCode.append("int " + channelOffsetVariable
                                + " = " + actorHelper.getOffset(port, channel)
                                + ";\n");

                        // Now replace these concrete offsets with the variables.
                        actorHelper.setOffset(port, channel,
                                channelOffsetVariable);
                    }
                }
            }
        }

        return initializeCode.toString();
    }

    /** Generate the preinitialize code for the associated SDF director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the base class throws it.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        Attribute iterations = getComponent().getAttribute("iterations");
        if (iterations != null) {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();
            if (iterationCount > 0) {
                // Since there is only one director, we can declare this
                // "iteration" variable. Alternatively, we can use $actorSymbol
                // to define it in a "preinit" codeblock and calls 
                // processCode(preinitializeCode);
                code.append("int iteration = 0;\n");
            }
        }
        return code.toString();
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number). The default value is 1. If the
     *  port is an output port, then the buffer size is obtained
     *  from the remote receiver. If it is an input port, then it
     *  is obtained from the specified port.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel.
     *  @exception IllegalActionException If the channel number is
     *   out of range or if the port is neither an input nor an
     *   output.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {

        Receiver[][] receivers = null;
        if (port.isInput()) {
            receivers = port.getReceivers();
        } else if (port.isOutput()) {
            receivers = port.getRemoteReceivers();
        } else {
            throw new IllegalActionException(port,
                    "Port is neither an input nor an output.");
        }
        try {
            int size = 0;
            for (int copy = 0; copy < receivers[channelNumber].length; copy++) {
                int copySize = ((SDFReceiver) receivers[channelNumber][copy])
                        .getCapacity();
                if (copySize > size) {
                    size = copySize;
                }
            }
            return size;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(port, "Channel out of bounds: "
                    + channelNumber);
        }
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

            for (int i = 0; i < width; i++, channel++) {
                if (channel == channelNumber) {
                    connectedRelations.add(relation);
                }
            }
        }

        return connectedRelations;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                          private methods                         ////

    /** Return the minimum number of power of two that is greater than or
     *  equal to the given integer.
     *  @param value The given integer.
     *  @return the minumber number of power of two that is greater than or
     *   equal to the given integer.
     *  @exception IllegalActionException If the given integer is not positive.
     */
    private int _ceilToPowerOfTwo(int value) throws IllegalActionException {
        if (value < 1) {
            throw new IllegalActionException(getComponent(),
                    "The given integer must be a positive integer.");
        }

        int powerOfTwo = 1;

        while (value > powerOfTwo) {
            powerOfTwo = powerOfTwo << 1;
        }

        return powerOfTwo;
    }
}
