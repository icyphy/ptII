/* Code generator helper class associated with the HDFDirector class.

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
package ptolemy.codegen.c.domains.hdf.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.domains.sdf.kernel.SDFDirector;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// HDFDirector

/**
 Code generator helper class associated with the HDFDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class HDFDirector extends SDFDirector {
    /** Construct the code generator helper associated with the given HDFDirector.
     *  @param director The associated ptolemy.domains.hdf.kernel.HDFDirector
     */
    public HDFDirector(ptolemy.domains.hdf.kernel.HDFDirector director) {
        super(director);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the code for the firing of actors according to the HDF
     *  schedule.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the HDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated helper.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
    }

    /** Generate the preinitialize code for this director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.domains.hdf.kernel.HDFDirector director = (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();

        CompositeActor container = (CompositeActor) director.getContainer();

        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        List actors = container.deepEntityList();

        int numberOfActors = actors.size();

        int[] divisors = new int[numberOfActors];

        int numberOfConfigurationsOfContainer = 1;

        for (int i = numberOfActors - 1; i >= 0; i--) {
            Actor actor = (Actor) actors.get(i);
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            int[][] rates = helper.getRates();
            divisors[i] = numberOfConfigurationsOfContainer;

            if (rates != null) {
                numberOfConfigurationsOfContainer *= rates.length;
                divisors[i] = numberOfConfigurationsOfContainer;
            }
        }

        _schedules = new Schedule[numberOfConfigurationsOfContainer];

        int[][] containerRates = new int[numberOfConfigurationsOfContainer][];
        int[] configurationIndex = new int[numberOfActors];

        for (int i = 0; i < numberOfConfigurationsOfContainer; i++) {
            int remainder = i;

            for (int j = 0; j < (numberOfActors - 1); j++) {
                configurationIndex[j] = remainder / divisors[j + 1];
                remainder = remainder % divisors[j + 1];
            }

            configurationIndex[numberOfActors - 1] = remainder;

            for (int j = 0; j < numberOfActors; j++) {
                Actor actor = (Actor) actors.get(i);
                CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                int[][] rates = helper.getRates();

                if (rates != null) {
                    int[] portRates = rates[configurationIndex[j]];
                    List ports = ((Entity) actor).portList();

                    for (int k = 0; k < portRates.length; k++) {
                        IOPort port = (IOPort) ports.get(k);

                        if (port.isInput()) {
                            DFUtilities.setTokenConsumptionRate(port,
                                    portRates[k]);
                        } else {
                            DFUtilities.setTokenProductionRate(port,
                                    portRates[k]);
                        }
                    }
                }
            }

            //director.invalidateSchedule();
            _schedules[i] = director.getScheduler().getSchedule();

            for (int j = 0; j < numberOfActors; j++) {
                Actor actor = (Actor) actors.get(i);
                CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                Iterator inputPorts = actor.inputPortList().iterator();

                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();

                    for (int k = 0; k < inputPort.getWidth(); k++) {
                        int newCapacity = getBufferSize(inputPort, k);
                        int oldCapacity = helper.getBufferSize(inputPort, k);

                        if (newCapacity > oldCapacity) {
                            helper.setBufferSize(inputPort, k, newCapacity);
                        }
                    }
                }
            }

            Iterator outputPorts = container.outputPortList().iterator();

            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();

                for (int k = 0; k < outputPort.getWidthInside(); k++) {
                    int newCapacity = getBufferSize(outputPort, k);
                    int oldCapacity = containerHelper.getBufferSize(outputPort,
                            k);

                    if (newCapacity > oldCapacity) {
                        containerHelper.setBufferSize(outputPort, k,
                                newCapacity);
                    }
                }
            }

            List externalPorts = container.portList();
            int[] externalPortRates = new int[externalPorts.size()];

            for (int portNumber = 0; portNumber < externalPorts.size(); portNumber++) {
                IOPort externalPort = (IOPort) externalPorts.get(portNumber);
                externalPortRates[portNumber] = DFUtilities
                        .getRate(externalPort);
            }

            containerRates[i] = externalPortRates;
        }

        containerHelper.setRates(containerRates);

        return code.toString();
    }

    protected void _checkBufferSize(StringBuffer initializeCode)
            throws IllegalActionException {
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();

        Iterator outputPorts = container.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            _checkBufferSize(outputPort, initializeCode);
        }

        Iterator actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Iterator inputPorts = actor.inputPortList().iterator();

            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                _checkBufferSize(inputPort, initializeCode);
            }
        }
    }

    // we could record total number of tokens transferred in each port for each
    // configuration and then check if a variale is needed for each offset. For
    // now we always use variables.
    protected void _checkBufferSize(IOPort port, StringBuffer initializeCode)
            throws IllegalActionException {
        CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int length = 0;

        if (port.isInput()) {
            length = port.getWidth();
        } else {
            length = port.getWidthInside();
        }

        for (int channel = 0; channel < length; channel++) {
            // Increase the buffer size of that channel to the power of two.
            int bufferSize = _ceilToPowerOfTwo(actorHelper.getBufferSize(port,
                    channel));
            actorHelper.setBufferSize(port, channel, bufferSize);

            StringBuffer channelReadOffset = new StringBuffer();
            StringBuffer channelWriteOffset = new StringBuffer();
            channelReadOffset.append(port.getFullName().replace('.', '_'));
            channelWriteOffset.append(port.getFullName().replace('.', '_'));

            if (port.getWidth() > 1) {
                channelReadOffset.append("_" + channel);
                channelWriteOffset.append("_" + channel);
            }

            channelReadOffset.append("_readoffset");
            channelWriteOffset.append("_writeoffset");

            String channelReadOffsetVariable = channelReadOffset.toString();
            String channelWriteOffsetVariable = channelWriteOffset.toString();

            // At this point, all offsets are 0 or the number of
            // initial tokens of SampleDelay.
            initializeCode.append("int " + channelReadOffsetVariable + " = "
                    + actorHelper.getReadOffset(port, channel) + ";\n");
            initializeCode.append("int " + channelWriteOffsetVariable + " = "
                    + actorHelper.getWriteOffset(port, channel) + ";\n");

            // Now replace these concrete offsets with the variables.
            actorHelper.setReadOffset(port, channel, channelReadOffsetVariable);
            actorHelper.setWriteOffset(port, channel,
                    channelWriteOffsetVariable);
        }
    }

    protected Schedule[] _schedules;
}
