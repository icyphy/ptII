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
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////SDFDirector

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
    *  @param sdfDirector The associated 
    *  ptolemy.domains.sdf.kernel.SDFDirector
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
           CodeGeneratorHelper helperObject = 
                   (CodeGeneratorHelper) _getHelper((NamedObj) actor);

           for (int i = 0; i < firing.getIterationCount(); i++) {
               helperObject.generateFireCode(code);

               // update buffer offset after firing each actor once
               Set inputPortsSet = new HashSet();
               inputPortsSet.addAll(actor.inputPortList());

               Iterator inputPorts = inputPortsSet.iterator();

               while (inputPorts.hasNext()) {
                   IOPort port = (IOPort) inputPorts.next();
                   int rate = DFUtilities.getRate(port);
                   _updatePortOffset(port, code, rate);                        
               }
                 
               Set outputPortsSet = new HashSet();
               outputPortsSet.addAll(actor.outputPortList());

               Iterator outputPorts = outputPortsSet.iterator();

               while (outputPorts.hasNext()) {
                   IOPort port = (IOPort) outputPorts.next();
                   int rate = DFUtilities.getRate(port);
                   _updateConnectedPortsOffset(port, code, rate);                        
               }
           }
       }

   }
   

   /** Generate the initialize code for the associated SDF director.
    *  @return The generated initialize code.
    *  @exception IllegalActionException If the base class throws it.
    * FIXME: should tell exactly why it throws it.
    */
   public String generateInitializeCode() throws IllegalActionException {
       StringBuffer initializeCode = new StringBuffer();
       initializeCode.append(super.generateInitializeCode());
           
       ptolemy.actor.CompositeActor compositeActor 
               = (ptolemy.actor.CompositeActor) getComponent().getContainer();
       
       ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper
               = (ptolemy.codegen.c.actor.TypedCompositeActor)
               _getHelper(compositeActor);
       
       Iterator outputPorts = compositeActor.outputPortList().iterator();
       while (outputPorts.hasNext()) {
           IOPort outputPort = (IOPort) outputPorts.next();
           int rate = DFUtilities.getTokenInitProduction(outputPort);
 
           for (int i = 0; i < outputPort.getWidthInside(); i++) {
            
               if (i < outputPort.getWidth()) {   
                   String name = outputPort.getName();
                   if (outputPort.isMultiport()) {
                       name = name + '#' + i;   
                   }
        
                   for (int k = 0; k < rate; k++) {
       
                       initializeCode.append(compositeActorHelper
                               .getReference(name + "," + k));
                       initializeCode.append(" = ");
                       initializeCode.append(compositeActorHelper
                               .getReference("@" + name + "," + k));
                       initializeCode.append(";\n");
                   }    
               }
           }
           _updatePortOffset(outputPort, initializeCode, rate);
           _updateConnectedPortsOffset(outputPort, initializeCode, rate);
           
           int totalTokens = DFUtilities.getRate(outputPort);
           _checkBufferSize(outputPort, initializeCode, totalTokens);
       }
             
       
       Iterator actors = ((CompositeActor) getComponent().getContainer())
               .deepEntityList().iterator();
       while (actors.hasNext()) {
           Actor actor = (Actor) actors.next();
           Variable firings = (Variable) ((NamedObj) actor)
                   .getAttribute("firingsPerIteration");
           int firingsPerIteration = ((IntToken) firings.getToken())
                   .intValue();
           Set inputPortsSet = new HashSet();
           inputPortsSet.addAll(actor.inputPortList());
           //ioPortsSet.addAll(actor.outputPortList());

           Iterator inputPorts = inputPortsSet.iterator();

           while (inputPorts.hasNext()) {
               IOPort port = (IOPort) inputPorts.next();
               int totalTokens = DFUtilities.getRate(port)
                       * firingsPerIteration;
               _checkBufferSize(port, initializeCode, totalTokens);               
           }
       }
       
       return initializeCode.toString();
   }

   /** Generate code for transferring enough tokens to complete an internal 
    *  iteration.
    *  @param inputPort The port to transfer tokens.
    *  @param code The string buffer that the generated code is appended to.
    *  @exception IllegalActionException
    */
   public void generateTransferInputsCode(IOPort inputPort, StringBuffer code) 
           throws IllegalActionException {
    
       int rate = DFUtilities.getTokenConsumptionRate(inputPort);  
       
       ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper
               = (ptolemy.codegen.c.actor.TypedCompositeActor)
               _getHelper(getComponent().getContainer());

       for (int i = 0; i < inputPort.getWidth(); i++) {
           if (i < inputPort.getWidthInside()) {
            
               String name = inputPort.getName();
               if (inputPort.isMultiport()) {
                   name = name + '#' + i;   
               }
 
               for (int k = 0; k < rate; k++) { 
                   code.append(compositeActorHelper
                            .getReference("@" + name + "," + k));
                   code.append(" = ");
                   code.append(compositeActorHelper
                            .getReference(name + "," + k));
                   code.append(";\n");
               }    
           }
       } 
 
       _updateConnectedPortsOffset(inputPort, code, rate);
       
   }

   /** Generate code for transferring enough tokens to fulfill the output 
    *  production rate.
    *  @param outputPort The port to transfer tokens.
    *  @param code The string buffer that the generated code is appended to.
    *  @exception IllegalActionException
    */
   public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
           throws IllegalActionException {

       int rate = DFUtilities.getTokenProductionRate(outputPort);
    
       ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper
               = (ptolemy.codegen.c.actor.TypedCompositeActor)
               _getHelper(getComponent().getContainer());
    
       for (int i = 0; i < outputPort.getWidthInside(); i++) {
           if (i < outputPort.getWidth()) {   
               String name = outputPort.getName();
               if (outputPort.isMultiport()) {
                   name = name + '#' + i;   
               }
     
               for (int k = 0; k < rate; k++) {
    
                   code.append(compositeActorHelper
                           .getReference(name + "," + k));
                   code.append(" = ");
                   code.append(compositeActorHelper
                           .getReference("@" + name + "," + k));
                   code.append(";\n");
               }    
           }
       } 
       
       _updatePortOffset(outputPort, code, rate);
   }

   

   /** Return the buffer size of a given channel (i.e, a given port
    *  and a given channel number). The default value is 1. If the
    *  port is an output port, then the buffer size is obtained
    *  from the inside receiver. If it is an input port, then it
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
           receivers = port.getInsideReceivers();
       } else {
           throw new IllegalActionException(port,
                   "Port is neither an input nor an output.");
       }
       try {
           int size = 0;
           for (int copy = 0; 
               copy < receivers[channelNumber].length; copy++) {
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
   
   /** Check for each channel of the given port to see if variables are needed
    *  for recording read offset and write offset. If the buffer size of a 
    *  channel divides the total number of tokens transferred in one firing 
    *  of this director, then there is no need for the variables. Otherwise the
    *  integer offsets are replaced with variables and the code to initialize 
    *  these variables are generated.
    *   
    *  @param port The port to be checked.
    *  @param initializeCode The string buffer that the generated code 
    *   is appended to.
    *  @param totalTokens The number of tokens transferred in one firing of 
    *   this director.
    *  @exception IllegalActionException
    */
   protected void _checkBufferSize(IOPort port, StringBuffer initializeCode, 
           int totalTokens) throws IllegalActionException {
    
       CodeGeneratorHelper actorHelper = 
           (CodeGeneratorHelper) _getHelper(port.getContainer());   
    
       int length = 0;
       if (port.isInput()) {
           length = port.getWidth();
       } else {
           length = port.getWidthInside();
       } 
       for (int channel = 0; channel < length; channel++) {
           int portOffset = 
                   totalTokens % getBufferSize(port, channel);

           if (portOffset != 0) {
               // Increase the buffer size of that channel to the
               // power of two.
               int bufferSize = _ceilToPowerOfTwo(getBufferSize(port,
                       channel));
               actorHelper.setBufferSize(port, channel, bufferSize);
            
               //As optimization, we could check again if the new 
               //bufferSize divides totalTokens. If yes, we could 
               //avoid using a variable to represent the offset.

               // Declare the channel offset variables.
               //FIXME: should factor out this code, 
               // see CodeGenerateorHelper.getReference()
               StringBuffer channelReadOffset = new StringBuffer();
               StringBuffer channelWriteOffset = new StringBuffer();
               channelReadOffset.append(port.getFullName().replace('.',
                       '_'));
               channelWriteOffset.append(port.getFullName().replace('.',
                       '_'));

               if (port.getWidth() > 1) {
                   channelReadOffset.append("_" + channel);
                   channelWriteOffset.append("_" + channel);
               }

               channelReadOffset.append("_readoffset");
               channelWriteOffset.append("_writeoffset");

               String channelReadOffsetVariable = 
                       channelReadOffset.toString();
               String channelWriteOffsetVariable = 
                       channelWriteOffset.toString();

               // At this point, all offsets are 0 or the number of
               // initial tokens of SampleDelay.
               initializeCode.append("int " + channelReadOffsetVariable
                       + " = " + actorHelper.getReadOffset(port, channel)
                       + ";\n");
               initializeCode.append("int " + channelWriteOffsetVariable
                       + " = " + actorHelper.getWriteOffset(port, channel)
                       + ";\n");
            

               // Now replace these concrete offsets 
               // with the variables.
               actorHelper.setReadOffset(port, channel,
                       channelReadOffsetVariable);
               actorHelper.setWriteOffset(port, channel,
                       channelWriteOffsetVariable);
           }
       }
   }
   
   /** Update the offsets of the buffer associated with the given port.
    * 
    *  @param port
    *  @param code
    *  @exception IllegalActionException
    */
   protected void _updatePortOffset(IOPort port, StringBuffer code, int rate) 
           throws IllegalActionException {
    
       CodeGeneratorHelper helperObject = 
               (CodeGeneratorHelper) _getHelper(port.getContainer());
       
       int length = 0;
       if (port.isInput()) {
           length = port.getWidth();
       } else {
           length = port.getWidthInside();
       }
    
       for (int j = 0; j < length; j++) {
           // Update the offset for each channel.
           if (helperObject.getReadOffset(port, j) 
                   instanceof Integer) {
               int offset = ((Integer) helperObject
                       .getReadOffset(port, j)).intValue();
               offset = (offset + rate)
                       % helperObject.getBufferSize(port, j);
               helperObject.setReadOffset(port, j, new Integer(
                       offset));
           } else {
               int modulo = helperObject.getBufferSize(port, j) - 1;
               String offsetVariable = 
                       (String) helperObject.getReadOffset(port, j);
               code.append((String) 
                       offsetVariable + " = (" + offsetVariable 
                       + " + " + rate 
                       + ")&" + modulo + ";\n");
           }
       }
   }
   
   /** Update the offsets of the buffers associated with the ports connected 
    *  with the given port in its downstream.
    * 
    *  @param port
    *  @param code
    *  @throws IllegalActionException
    */
   protected void _updateConnectedPortsOffset(IOPort port, 
           StringBuffer code, int rate) throws IllegalActionException {
   
       CodeGeneratorHelper helperObject = 
               (CodeGeneratorHelper) _getHelper(port.getContainer());
       
       int length = 0;
       if (port.isInput()) {
           length = port.getWidthInside();
       } else {
           length = port.getWidth();
       }

       for (int j = 0; j < length; j++) {
           List sinkChannels = helperObject.getSinkChannels(port, j);

           for (int k = 0; k < sinkChannels.size(); k++) {
               Channel channel = (Channel) sinkChannels.get(k);
               IOPort sinkPort = (IOPort) channel.port;
               int sinkChannelNumber = channel.channelNumber;
        
               Object offsetObject = helperObject
                       .getWriteOffset(sinkPort, sinkChannelNumber);
               if (offsetObject instanceof Integer) {
                   int offset = ((Integer) offsetObject).intValue();
                   offset = (offset + rate)
                           % helperObject.getBufferSize
                           (sinkPort, sinkChannelNumber);
                   helperObject.setWriteOffset(sinkPort, 
                           sinkChannelNumber, new Integer(offset));
               } else {
                   int modulo = helperObject.getBufferSize
                           (sinkPort, sinkChannelNumber) - 1;
                   String offsetVariable = 
                           (String) helperObject.getWriteOffset
                           (sinkPort, sinkChannelNumber);
                   code.append((String) 
                           offsetVariable + " = (" + offsetVariable 
                           + " + " + rate 
                           + ")&" + modulo + ";\n");
               }
           }
       }    
   }

   //////////////////////////////////////////////////////////////////////////
   ////                          private methods                         ////

   /** Return the minimum number of power of two that is greater than or
    *  equal to the given integer.
    *  @param value The given integer.
    *  @return the minimum number of power of two that is greater than or
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
           powerOfTwo <<=  1;
       }

       return powerOfTwo;
   }
}
