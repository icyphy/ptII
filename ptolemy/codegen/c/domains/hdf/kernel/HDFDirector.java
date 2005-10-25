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
import ptolemy.actor.sched.Firing;
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
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
            (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
    
        CompositeActor container = (CompositeActor) director.getContainer();
    
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        int numberOfActors = container.deepEntityList().size();
        int[] configurationIndex = new int[numberOfActors];
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        for (int order = 0; order < _schedules.length; order++) {
            
            int remainder = order;
            for (int i = 0; i < numberOfActors - 1; i++) {
                configurationIndex[i] = remainder / _divisors[i+1];
                remainder = remainder % _divisors[i+1]; 
            }    
            configurationIndex[numberOfActors - 1] = remainder;
            
            code.append("case " + order + ":\n");

            Iterator actorsToFire = _schedules[order].iterator();
            while (actorsToFire.hasNext()) {
                Firing firing = (Firing) actorsToFire.next();
                Actor actor = firing.getActor();
                
                int actorNumber = 0;
                Iterator actors = container.deepEntityList().iterator();
                while(actors.hasNext()) {
                    if (actors.next() == actor) {
                        break;   
                    }
                    actorNumber++;
                }

                CodeGeneratorHelper helper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                int[][] rates = helper.getRates();
                
                for (int i = 0; i < firing.getIterationCount(); i++) {
            
                    helper.generateFireCode(code);
                    
                    // update buffer offset after firing each actor once
                    Iterator ports = ((Entity) actor).portList().iterator();
                    int j = 0;
                    while (ports.hasNext()) {                    
                        IOPort port = (IOPort) ports.next();
                        int rate;
                        if (rates != null) {
                            rate = rates[configurationIndex[actorNumber]][j];    
                        } else {
                            rate = DFUtilities.getRate(port);
                        }
                        if (port.isInput()) {
                            _updatePortOffset(port, code, rate);     
                        } else {
                            _updateConnectedPortsOffset(port, code, rate);   
                        } 
                        j++;
                    }
                    
                    /*
                    Iterator inputPorts = actor.inputPortList().iterator();
                    while (inputPorts.hasNext()) {
                        IOPort port = (IOPort) inputPorts.next();
                        int rate = DFUtilities.getRate(port);
                        _updatePortOffset(port, code, rate);                        
                    }
              
                    Iterator outputPorts = actor.outputPortList().iterator();
                    while (outputPorts.hasNext()) {
                        IOPort port = (IOPort) outputPorts.next();
                        int rate = DFUtilities.getRate(port);
                        _updateConnectedPortsOffset(port, code, rate);                        
                    }
                    */
                }
            } 
            code.append("break;\n");
        }
        code.append("}\n");
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
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        
        CompositeActor container = (CompositeActor) director.getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        List actors = container.deepEntityList();
        
        int numberOfActors = actors.size();
        
        _divisors = new int[numberOfActors];
        
        int numberOfConfigurationsOfContainer = 1;
            
        for (int i = numberOfActors - 1; i >= 0; i--) {
            Actor actor = (Actor) actors.get(i);
            CodeGeneratorHelper helper = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            int[][] rates = helper.getRates(); 
            _divisors[i] = numberOfConfigurationsOfContainer;
            if (rates != null) {
                numberOfConfigurationsOfContainer *= rates.length;
                _divisors[i] = numberOfConfigurationsOfContainer;
            }    
        }
        
        _schedules = new Schedule[numberOfConfigurationsOfContainer];  
        int[][] containerRates = new int[numberOfConfigurationsOfContainer][];
        int[] configurationIndex = new int[numberOfActors];
        for (int i = 0; i < numberOfConfigurationsOfContainer; i++) {
            int remainder = i;
            for (int j = 0; j < numberOfActors - 1; j++) {
                configurationIndex[j] = remainder / _divisors[j+1];
                remainder = remainder % _divisors[j+1]; 
            }    
            configurationIndex[numberOfActors - 1] = remainder;
            
            // update port rates of all actors for current configuration.
            int j = 0;
            Iterator actorsIterator = actors.iterator();
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next();
                CodeGeneratorHelper helper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                int[][] rates = helper.getRates(); 
                if (rates != null) {
                    int[] portRates = rates[configurationIndex[j]];
                    Iterator ports = ((Entity) actor).portList().iterator();
                    int k = 0;
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        if (port.isInput()) {
                            DFUtilities.setTokenConsumptionRate(port, portRates[k]);
                        } else {
                            DFUtilities.setTokenProductionRate(port, portRates[k]);                     
                        }
                        k++;
                    }
                }
                j++;
            }
            
            //director.invalidateSchedule();
            _schedules[i] = director.getScheduler().getSchedule();
            
            _updatePortBufferSize();
            
            // record external port rates for current configuration.
            List externalPorts = container.portList();
            int[] externalPortRates = new int[externalPorts.size()];
            Iterator externalPortsIterator = externalPorts.iterator();
            int portNumber = 0;
            while (externalPortsIterator.hasNext()) {
                IOPort externalPort = (IOPort) externalPortsIterator.next();
                externalPortRates[portNumber] = DFUtilities.getRate(externalPort);
                portNumber++;
            }
            containerRates[i] = externalPortRates;              
        }
        
        containerHelper.setRates(containerRates);
                    
        return code.toString();
    }
    

	/** Generate code for transferring enough tokens to complete an internal 
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code) 
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the inside */\n\n");
    
        CompositeActor container = (CompositeActor) getComponent().getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(container);
        
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while (containerPorts.hasNext()) {
            if (containerPorts.next() == inputPort) {
                break;
            }
            portNumber++;
        }
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        for (int order = 0; order < _schedules.length; order++) {
            
            int[][] rates = containerHelper.getRates();
            int rate = rates[order][portNumber];
            
            code.append("case " + order + ":\n");

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
             
                    String name = inputPort.getName();
                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
  
                    for (int k = 0; k < rate; k++) { 
                        code.append(containerHelper
                                 .getReference("@" + name + "," + k));
                        code.append(" = ");
                        code.append(containerHelper
                                 .getReference(name + "," + k));
                        code.append(";\n");
                    }    
                }
            }
            
            // The offset of the input port itself is updated by outside director.
            _updateConnectedPortsOffset(inputPort, code, rate);
            
            code.append("break;\n");
        }
        code.append("}\n");
        
    }

    /** Generate code for transferring enough tokens to fulfill the output 
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the outside */\n\n");

        CompositeActor container = (CompositeActor) getComponent().getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(container);
        
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while (containerPorts.hasNext()) {
            if (containerPorts.next() == outputPort) {
                break;
            }
            portNumber++;
        }
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        for (int order = 0; order < _schedules.length; order++) {
            
            int[][] rates = containerHelper.getRates();
            int rate = rates[order][portNumber];
            
            code.append("case " + order + ":\n");
            
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {   
                    String name = outputPort.getName();
                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
      
                    for (int k = 0; k < rate; k++) {
     
                        code.append(containerHelper
                                .getReference(name + "," + k));
                        code.append(" = ");
                        code.append(containerHelper
                                .getReference("@" + name + "," + k));
                        code.append(";\n");
                    }    
                }
            } 
                    
            // The offset of the ports connected to the output port is 
            // updated by outside director.
            _updatePortOffset(outputPort, code, rate);
            
            code.append("break;\n");
        }    
        code.append("}\n");
    }
    
    
        
    protected void _checkBufferSize(StringBuffer initializeCode) 
               throws IllegalActionException {
    
        CompositeActor container 
	            = (CompositeActor) getComponent().getContainer();

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
     
        CodeGeneratorHelper actorHelper = 
            (CodeGeneratorHelper) _getHelper(port.getContainer());   
     
        int length = 0;
        if (port.isInput()) {
            length = port.getWidth();
        } else {
            length = port.getWidthInside();
        } 
        for (int channel = 0; channel < length; channel++) {
            
            // Increase the buffer size of that channel to the power of two.
            int bufferSize = _ceilToPowerOfTwo(actorHelper
                    .getBufferSize(port, channel));
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
            actorHelper.setWriteOffset(port, channel, channelWriteOffsetVariable);
            }
        }

    protected void _updatePortBufferSize() throws IllegalActionException {
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
    
        CompositeActor container = (CompositeActor) director.getContainer();
    
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        Iterator actors = container.deepEntityList().iterator();
        
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int k = 0; k < inputPort.getWidth(); k++) {
                    int newCapacity = getBufferSize(inputPort, k);
                    int oldCapacity = actorHelper.getBufferSize(inputPort, k);
                    if (newCapacity > oldCapacity) {
                        actorHelper.setBufferSize(inputPort, k, newCapacity);        
                    }
                }
            }
        }
        
        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int k = 0; k < outputPort.getWidthInside(); k++) {
                int newCapacity = getBufferSize(outputPort, k);
                int oldCapacity = containerHelper.getBufferSize(outputPort, k);
                if (newCapacity > oldCapacity) {
                    containerHelper.setBufferSize(outputPort, k, newCapacity);        
                }
            }
        }
    }

    
    protected Schedule[] _schedules;
    
    private int[] _divisors;

}
