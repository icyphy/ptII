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
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.CompositeActorApplication;
import ptolemy.codegen.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFCodeGenerator
/** A code generator for SDF.
 *
 *  @author Jeff Tsay
 */
public class SDFCodeGenerator extends CompositeActorApplication 
     implements JavaStaticSemanticConstants {

    public SDFCodeGenerator(String[] args) throws Exception {
        super(args, false);                         
    }
    
    public void generateCode() throws IllegalActionException {
        // assume just one model on the command line
        
        TypedCompositeActor compositeActor = (TypedCompositeActor) _models.get(0);
        
        try {
          // initialize the model to ensure type resolution and scheduling are done
          compositeActor.getManager().initialize();
        } catch (Exception e) {
          ApplicationUtility.error("could not initialize composite actor");         
        }
                
        // get the schedule for the model
        SDFDirector dir = (SDFDirector) compositeActor.getDirector();
        
        SDFScheduler scheduler = (SDFScheduler) dir.getScheduler();
        
        Enumeration schedule = schedule = scheduler.schedule();
                              
        // build a mapping between each actor and the firing count
  
        TypedAtomicActor lastActor = null;
        
        while (schedule.hasMoreElements()) {
        
            TypedAtomicActor actor = (TypedAtomicActor) schedule.nextElement();
            
            _actorList.addLast(actor);
            
            // see if this is the first appearance of this actor                                    
            if (_actorSet.add(actor)) { 
               SDFActorCodeGeneratorInfo actorInfo = new SDFActorCodeGeneratorInfo();                       
               
               actorInfo.actor = actor;                       
               actorInfo.disjointAppearances = 1;                                             
               actorInfo.totalFirings = scheduler.getFiringCount(actor);               
               
               _makeBufferInfo(actor, actorInfo);
                              
               _actorInfoMap.put(actor, actorInfo);            
      
               ApplicationUtility.trace("actor " + actor + " fires " + 
                actorInfo.totalFirings + " time(s).");
                                                        
            }  else {
               if (actor != lastActor) {
                  // update the disjoint appearance count
                  SDFActorCodeGeneratorInfo actorInfo = 
                   (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);
                   
                  actorInfo.disjointAppearances++; 
               }                                            
            }
            lastActor = actor;
        }           
        
        Iterator actorItr = _actorSet.iterator();
        
        ActorCodeGenerator actorCodeGen = 
         new ActorCodeGenerator(_codeGenClassFactory);
         
        LinkedList renamedSourceList = new LinkedList();
        
        while (actorItr.hasNext()) {
             TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();  
             SDFActorCodeGeneratorInfo actorInfo = 
              (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);        
                          
             _makeInputInfo(actor, actorInfo);
                          
             String renamedSource = actorCodeGen.generateCode(actorInfo);
             renamedSourceList.addLast(renamedSource);
        }                                                           
        
        actorItr = _actorSet.iterator();
        Iterator renamedSourceItr = renamedSourceList.iterator();
        
        while (actorItr.hasNext()) {
             TypedAtomicActor actor = (TypedAtomicActor) actorItr.next();  
             SDFActorCodeGeneratorInfo actorInfo = 
              (SDFActorCodeGeneratorInfo) _actorInfoMap.get(actor);        
              
             String renamedSource = (String) renamedSourceItr.next(); 
                                                   
             actorCodeGen.pass2(renamedSource, actorInfo);
        }                
        
        _generateMain();
    }

    /** Generate the main class. */
    protected void _generateMain() {
    
        LinkedList memberList = new LinkedList();
        
        Iterator bufferItr = _bufferInfoMap.values().iterator();
        PtolemyTypeIdentifier typeID = 
         _codeGenClassFactory.createPtolemyTypeIdentifier();
                           
        while (bufferItr.hasNext()) {        
           BufferInfo bufferInfo = (BufferInfo) bufferItr.next();
           
           TypeNode dataTypeNode = 
            typeID.encapsulatedDataType(bufferInfo.type);
           
           int bufferWidth = bufferInfo.width;             
           int bufferDimension = (bufferWidth <= 1) ? 1 : 2;
           
           TypeNode typeNode = TypeUtility.makeArrayType(dataTypeNode,
            bufferDimension);
            
           int bufferLength = bufferInfo.length;
           
           LinkedList dimExprList = TNLManip.cons(new IntLitNode(
            String.valueOf(bufferLength)));
           
           if (bufferDimension > 1) {
              dimExprList.addFirst(new IntLitNode(
               String.valueOf(bufferWidth)));           
           } 
           
           TypeNode dataBaseTypeNode = TypeUtility.arrayBaseType(dataTypeNode);
           int dataTypeDims = TypeUtility.arrayDimension(dataTypeNode);
           
           AllocateArrayNode allocateArrayNode = new AllocateArrayNode(
            dataBaseTypeNode, dimExprList, dataTypeDims, AbsentTreeNode.instance);
            
           FieldDeclNode fieldDeclNode = new FieldDeclNode(
            PUBLIC_MOD | STATIC_MOD | FINAL_MOD, typeNode,
            new NameNode(AbsentTreeNode.instance, bufferInfo.codeGenName),
            allocateArrayNode);    
           
           memberList.add(fieldDeclNode);           
        }        
    
        ClassDeclNode classDeclNode = new ClassDeclNode(PUBLIC_MOD,
         new NameNode(AbsentTreeNode.instance, "CG_Main"),
         new LinkedList(), memberList, 
         (TypeNameNode) StaticResolution.OBJECT_TYPE.clone());
         
        // bring in imports for Complex and FixPoint (remove unnecessary ones later)
        LinkedList importList = new LinkedList();
        
        importList.add(new ImportNode((NameNode)
         StaticResolution.makeNameNode("ptolemy.math.Complex")));
        importList.add(new ImportNode((NameNode)
         StaticResolution.makeNameNode("ptolemy.math.FixPoint")));
                          
        CompileUnitNode unitNode = new CompileUnitNode(
         new NameNode(AbsentTreeNode.instance, "codegen"),
         importList, TNLManip.cons(classDeclNode));
                    
        String outFileName = "c:\\users\\ctsay\\ptII\\codegen\\" +  "CG_Main.java";
        
        JavaCodeGenerator.writeCompileUnitNodeList(TNLManip.cons(unitNode),
         TNLManip.cons(outFileName));                             
    }
    
    /** Figure out which buffers are connected to each input port of a given 
     *  TypedAtomicActor, and add the information to the instance of 
     *  SDFActorCodeGeneratorInfo argument.
     */
    protected void _makeInputInfo(TypedAtomicActor actor, 
         SDFActorCodeGeneratorInfo actorInfo) throws IllegalActionException {
     
        // iterate over the ports of this actor
        Iterator portItr = actor.portList().iterator();
           
        while (portItr.hasNext()) {              
           TypedIOPort port = (TypedIOPort) portItr.next();
           
           // we are only concerned with input ports
           if (port.isInput()) {
                      
              int inputWidth = port.getWidth();

              List connectedPortList = port.connectedPortList();
                                           
              String[] bufferNames = new String[inputWidth];
              int[] bufferLengths = new int[inputWidth];
              
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
                      " of actor " + actor + '.');
                  }
                                     
                  BufferInfo bufferInfo = 
                   (BufferInfo) _bufferInfoMap.get(outputPort);
                   
                  bufferLengths[channel] = bufferInfo.length;
                   
                  if (bufferInfo.width == 1) {
                     bufferNames[channel] = bufferInfo.codeGenName;                  
                  } else {
                     bufferNames[channel] = 
                      bufferInfo.codeGenName + '[' + outputChannel + ']';                  
                  } 
                   
              } // for (int channel = 0; channel < inputWidth; channel++) ...


              ApplicationUtility.trace("connected buffers for port " + port.getName() + 
               " of actor " + actor.getName());
              for (int ch = 0; ch < inputWidth; ch++) {
                 System.out.println("ch " + ch + ": " + bufferNames[ch]);
              }
              
              actorInfo.inputBufferNameMap.put(port, bufferNames);
              actorInfo.inputBufferLengthMap.put(port, bufferLengths);
              
              
           } // if (port.isInput()) ...                                          
        } // while (portItr.hasNext()) ...                                     
    }
         
    protected void _makeBufferInfo(TypedAtomicActor actor, 
         SDFActorCodeGeneratorInfo actorInfo) throws IllegalActionException {
        
        int firings = actorInfo.totalFirings;
                 
        Iterator portItr = actor.portList().iterator();
           
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
                 
              actorInfo.outputInfoMap.put(port, bufferInfo);
              
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
        SDFCodeGenerator codeGen = null;
    
        try {
            codeGen = new SDFCodeGenerator(args);
            
            codeGen.generateCode();        
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }               
    }    

    protected HashSet _actorSet = new HashSet();
    protected LinkedList _actorList = new LinkedList();        
        
    /** A map containing instances of SDFActorCodeGeneratorInfo,
     *  using the corresponding Actors as keys.
     */
    protected HashMap _actorInfoMap = new HashMap();
    
    /** A map containing instances of BufferInfo, using the corresponding
     *  ports as keys. This map contains all BufferInfos for all output
     *  ports in the CompositeActor.
     */
    protected HashMap _bufferInfoMap = new HashMap();
    
    protected CodeGeneratorClassFactory _codeGenClassFactory = 
     SDFCodeGeneratorClassFactory.getInstance();
    
    /** A non-decreasing number used for globally unique labeling. */
    protected int labelNum = 0;    
}