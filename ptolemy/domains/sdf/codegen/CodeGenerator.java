/* A code generator for SDF.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.codegen;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.CompositeActorApplication;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator
/** A code generator for SDF.
 *
 *  @author Jeff Tsay
 */
public class CodeGenerator extends CompositeActorApplication {

    public CodeGenerator(String[] args) throws Exception {
        super(args, false);                         
    }
    
    public void generateCode() throws IllegalActionException {
        // assume just one model on the command line
        
        TypedCompositeActor compositeActor = (TypedCompositeActor) _models.get(0);
        
        // start the model to ensure the schedule is computed
        startRun(compositeActor);        
        waitForFinish();
                
        // get the schedule for the model
        SDFDirector dir = (SDFDirector) compositeActor.getDirector();
        
        SDFScheduler scheduler = (SDFScheduler) dir.getScheduler();
        
        Enumeration schedule = schedule = scheduler.schedule();
                              
        // build a mapping between each entity and the firing count
  
        Entity lastEntity = null;
        
        while (schedule.hasMoreElements()) {
        
            Entity entity = (Entity) schedule.nextElement();
            
            _entityList.addLast(entity);
            
            // see if this is the first appearance of this entity                                    
            if (_entitySet.add(entity)) { 
               PerActorCodeGeneratorInfo actorInfo = new PerActorCodeGeneratorInfo();                       
                                                            
               actorInfo.disjointAppearances = 1;                                             
               actorInfo.totalFirings = scheduler.getFiringCount(entity);               
               
               _makeBufferInfo(entity, actorInfo);
                              
               _entityInfoMap.put(entity, actorInfo);            
      
               System.out.println("entity " + entity + " fires " + 
                actorInfo.totalFirings + " time(s).");
                                                        
            }  else {
               if (entity != lastEntity) {
                  // update the disjoint appearance count
                  PerActorCodeGeneratorInfo actorInfo = 
                   (PerActorCodeGeneratorInfo) _entityInfoMap.get(entity);
                   
                  actorInfo.disjointAppearances++; 
               }                                            
            }
            lastEntity = entity;
        }           
        
        Iterator entityItr = _entitySet.iterator();
        
        while (entityItr.hasNext()) {
             Entity entity = (Entity) entityItr.next();  
             PerActorCodeGeneratorInfo actorInfo = 
              (PerActorCodeGeneratorInfo) _entityInfoMap.get(entity);        
             
             _makeInputInfo(entity, actorInfo);
             
             ActorCodeGenerator actorCodeGen = new ActorCodeGenerator(entity);
             actorCodeGen.generateCode(actorInfo);
        }                                                           
    }

    /** Figure out which buffers are connected to each input port of a given 
     *  Entity, and add the information to the instance of 
     *  PerActorCodeGeneratorInfo argument.
     */
    protected void _makeInputInfo(Entity entity, 
         PerActorCodeGeneratorInfo actorInfo) throws IllegalActionException {
     
        // iterate over the ports of this entity
        Iterator portItr = entity.portList().iterator();
           
        while (portItr.hasNext()) {              
           TypedIOPort port = (TypedIOPort) portItr.next();
           
           // we are only concerned with input ports
           if (port.isInput()) {
                      
              int inputWidth = port.getWidth();

              List connectedPortList = port.connectedPortList();
                                           
              String[] bufferNames = new String[inputWidth];
              
              Receiver[][] receivers = port.getReceivers();
              
              for (int channel = 0; channel < inputWidth; channel++) {
                  // get the receiver for this channel
                  // we only support one reciever per channel
                  Receiver receiver = receivers[channel][0];
                  
                  // Find the output port for this channel and the channel
                  // number connecting it to this port.
                  // This is done by matching the receiver for this
                  // channel with a remoteReceiver of a connected output port                  
                  
                  int outputChannel = -1;
                  
                  // search all output ports connected to this port
                  Iterator connectedPortItr = connectedPortList.iterator();
                  
                  TypedIOPort outputPort = null;
                  
                  while (connectedPortItr.hasNext() && (outputPort == null)) {
                     TypedIOPort connectedPort = (TypedIOPort) connectedPortItr.next();
                                       
                     // search only output ports
                     if (connectedPort.isOutput()) {
                        int outputWidth = connectedPort.getWidth();
                        
                        Receiver[][] remoteReceiversArray = 
                         connectedPort.getRemoteReceivers();
 
                        // search all channels of the output port
                        int ch = 0;
                        do {
                                                      
                           Receiver[] remoteReceivers = remoteReceiversArray[ch];  
                           
                           int i = 0;
                           
                           // search all receivers in the same receiver group
                           while ((i < remoteReceivers.length) && (outputPort == null)) {
                           
                              Receiver remoteReceiver = remoteReceivers[i];
                              
                              if (receiver == remoteReceiver) {
                                 outputPort = connectedPort;
                                 outputChannel = ch;                                  
                              }
                              i++;
                           }                                                                                                             
                           ch++;
                        } while ((ch < outputWidth) && (outputPort == null));                     
                     }                  
                  }
                  
                  if (outputPort == null) {
                     throw new InternalError("could not find output port associated " +
                      "with channel " + channel + " for port " + port.getName() +
                      " of entity " + entity + '.');
                  }
                                     
                  BufferInfo bufferInfo = 
                   (BufferInfo) _bufferInfoMap.get(outputPort);
                   
                  if (bufferInfo.width == 1) {
                     bufferNames[channel] = bufferInfo.codeGenName;                  
                  } else {
                     bufferNames[channel] = 
                      bufferInfo.codeGenName + '[' + outputChannel + ']';                  
                  } 
                   
              } // for (int channel = 0; channel < inputWidth; channel++) ...


              System.out.println("connected buffers for port " + port.getName() + 
               " of entity " + entity.getName());
              for (int ch = 0; ch < inputWidth; ch++) {
                 System.out.println("ch " + ch + ": " + bufferNames[ch]);
              }
              
              actorInfo.inputInfoMap.put(port, bufferNames);
              
           } // if (port.isInput()) ...                                          
        } // while (portItr.hasNext()) ...                                     
    }
         
    protected void _makeBufferInfo(Entity entity, 
         PerActorCodeGeneratorInfo actorInfo) throws IllegalActionException {
        
        int firings = actorInfo.totalFirings;
                 
        Iterator portItr = entity.portList().iterator();
           
        while (portItr.hasNext()) {              
           TypedIOPort port = (TypedIOPort) portItr.next();
           
           // allocate one buffer for each output port  
           if (port.isOutput()) {
              
              BufferInfo bufferInfo = new BufferInfo();
              bufferInfo.name = port.getName();
              bufferInfo.codeGenName = _makeUniqueName(bufferInfo.name);
              bufferInfo.width = port.getWidth();
                 
              // set length of buffer = 
              // init token production + number of firings * token production rate                 
              // (a worst case length)
                
              int productionRate;
              int initProduction;
               
              if (port instanceof SDFIOPort) {
                 SDFIOPort sdfIOPort = (SDFIOPort) port;
                 productionRate = sdfIOPort.getTokenProductionRate();
                 initProduction = sdfIOPort.getTokenInitProduction();
              } else { 
                 // for non-SDFIOPorts, the production rate is assumed to be 1,
                 // and the init production rate is assumed to be 0
                 productionRate = 1;
                 initProduction = 0;
              }                 
              bufferInfo.length = initProduction + firings * productionRate;                 
              
              bufferInfo.type = port.getType();                                         
                 
              actorInfo.inputInfoMap.put(port, bufferInfo);
              
              _bufferInfoMap.put(port, bufferInfo);
           }                 
        }     
    }

    /** Given the name of an object, return a globally unique Java identifier 
     *  containing
     *  the name, in the format "_cg_NAME_#" where NAME is the argument value
     *  and # is a number.
     */
    protected String _makeUniqueName(String name) {
        String retval = "_cg_" + name + "_" + labelNum;
        labelNum++;
        return retval;
    }

    
    public static void main(String[] args) {
        CodeGenerator codeGen = null;
    
        try {
            codeGen = new CodeGenerator(args);
            
            codeGen.generateCode();        
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }               
    }    

    protected HashSet _entitySet = new HashSet();
    protected LinkedList _entityList = new LinkedList();        
        
    /** A map containing instances of PerActorCodeGeneratorInfo,
     *  using the corresponding Entity's as keys.
     */
    protected HashMap _entityInfoMap = new HashMap();
    
    /** A map containing instances of BufferInfo, using the corresponding
     *  ports as keys. This map contains all BufferInfo's for all output
     *  ports in the CompositeActor.
     */
    protected HashMap _bufferInfoMap = new HashMap();
    
    /** A non-decreasing number used for globally unique labeling. */
    protected int labelNum = 0;    
}