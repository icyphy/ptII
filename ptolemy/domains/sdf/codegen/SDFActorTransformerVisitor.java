/* A Java AST visitor that transforms Actor code into code suitable
   for standalone execution (without dependancies on the ptolemy.actor
   and ptolemy.data packages), for SDF systems.

 Copyright (c) 2000 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A Java AST visitor that transforms Actor code into code suitable
 *  for standalone execution (without dependancies on the ptolemy.actor
 *  and ptolemy.data packages), for SDF systems.
 *
 *  @author Jeff Tsay
 */
public class SDFActorTransformerVisitor extends ActorTransformerVisitor {

    public SDFActorTransformerVisitor(ActorCodeGeneratorInfo actorInfo,
                                      PtolemyTypeVisitor typeVisitor) {
        super(actorInfo, typeVisitor);
        
        _sdfActorInfo = (SDFActorCodeGeneratorInfo) actorInfo;       
    }
                
    protected Object _actorClassDeclNode(ClassDeclNode node, LinkedList args) {
        ClassDeclNode retval = 
         (ClassDeclNode) super._actorClassDeclNode(node, args);
    
        // add the temporary variables _cg_chan_temp_r and _cg_chan_temp_w
        // if this is the base class

        if (_isBaseClass) {

           FieldDeclNode readTempField = new FieldDeclNode(PROTECTED_MOD,
            IntTypeNode.instance, 
            new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_r"),
            new IntLitNode("0"));
        
           FieldDeclNode writeTempField = new FieldDeclNode(PROTECTED_MOD,
            IntTypeNode.instance, 
            new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_w"),
            new IntLitNode("0"));
    
           List memberList = retval.getMembers();
            
           memberList.add(readTempField);
           memberList.add(writeTempField);        
        }
        
        return retval;
    }    
                
    protected Object _portFieldDeclNode(FieldDeclNode node, LinkedList args) {
        // replace the port with an array of offsets into the buffer
        return new FieldDeclNode(PROTECTED_MOD,
         TypeUtility.makeArrayType(IntTypeNode.instance, 1),
         new NameNode(AbsentTreeNode.instance, 
          "_cg_" + node.getName().getIdent() + "_offset"),
         AbsentTreeNode.instance);                     
    }
       
    protected Object _portMethodCallNode(MethodCallNode node, LinkedList args) {
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        MethodDecl methodDecl = (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);        

        ExprNode accessedObj = 
         (ExprNode) ExprUtility.accessedObject(fieldAccessNode);
        
        // call the super method which will do transformation of the
        // arguments and field access node
        Object superRetval = super._portMethodCallNode(node, args);
                        
        // we can only handle ports that are fields of the actor
        if (accessedObj.classID() != THISFIELDACCESSNODE_ID) return superRetval;
                                                
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
        
        String methodName = methodDecl.getName();        
        
        String varName = typedDecl.getName();
                
        TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);        

        List methodArgs = node.getArgs(); 
        ExprNode firstArg = null;
        
        if (methodArgs.size() > 0) {
           firstArg = (ExprNode) methodArgs.get(0);
        }

        if (methodName.equals("get")) {
           String[] bufferArray = (String[]) _sdfActorInfo.inputInfoMap.get(port);
                  
           if (bufferArray == null) {
              // port is not connected, return 0 since Token is resolved to int
              return new IntLitNode("0");
           }
                  
           int channel = -1;
                  
           if (!port.isMultiport()) {
              channel = 0;                  
           } else if (firstArg instanceof IntLitNode) { 
              // found a constant port number (it would help if constant folding
              // were done)
              channel = Integer.parseInt(((IntLitNode) firstArg).getLiteral());
           }
                  
           if (channel == -1) {
              // need to do a lookup of buffer for the port
                    
              // assign the channel to a dummy variable to avoid side effects
              AssignNode chanAssignNode = new AssignNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_r")),
               firstArg);
                      
              ArrayAccessNode findBufferNode = new ArrayAccessNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_chan_buffer")), 
               chanAssignNode);
                      
              PostIncrNode offsetIncrNode = new PostIncrNode(
               new ArrayAccessNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset")),
               new ObjectNode(
                new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_r"))));
 
              return new ArrayAccessNode(findBufferNode, offsetIncrNode);
                  
           } else {
              String bufferName = bufferArray[channel];
              PostIncrNode offsetIncrNode = new PostIncrNode(
               new ArrayAccessNode(new ObjectNode(
                new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset")),
               firstArg));
                     
              return new ArrayAccessNode(new ObjectNode(
               (NameNode) StaticResolution.makeNameNode("CG_Main." + bufferName)),
               offsetIncrNode);                  
           }
        } else if (methodName.equals("hasRoom")) {                                                   
           if ((port.getWidth() > 0) && port.isOutput()) {
              return new BoolLitNode("true");
           } else {
              return new BoolLitNode("false");
           }
        } else if (methodName.equals("hasToken")) {                                                   
           if ((port.getWidth() > 0) && port.isInput()) {
              return new BoolLitNode("true");
           } else {
              return new BoolLitNode("false");
           }
        } else if (methodName.equals("send")) {
           BufferInfo bufferInfo = (BufferInfo) _sdfActorInfo.outputInfoMap.get(port);
                  
           if (bufferInfo == null) {
              // port is not connected
                     
              // DANGER, neither of the method arguments to put() will be evaluated
                     
              return NullValue.instance;
           }
                              
           if (port.isMultiport()) {
              // assign the channel to a dummy variable to avoid side effects
              AssignNode chanAssignNode = new AssignNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_w")),
               firstArg);
                                    
              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
               "CG_Main." + bufferInfo.codeGenName));
                                    
              ArrayAccessNode bufferArrayAccessNode = new ArrayAccessNode(
               bufferObjectNode, chanAssignNode);
                      
              PostIncrNode offsetIncrNode = new PostIncrNode(
               new ArrayAccessNode(
                new ObjectNode(
                 new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset")),
                new ObjectNode(
                 new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_w"))));
                      
              return new AssignNode(
               new ArrayAccessNode(bufferArrayAccessNode, offsetIncrNode),
                (ExprNode) methodArgs.get(1));
                                                             
           } else {
              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
                "CG_Main." + bufferInfo.codeGenName));
                     
              PostIncrNode offsetIncrNode = new PostIncrNode(
               new ArrayAccessNode(new ObjectNode(
                new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset")),
                new IntLitNode("0")));
                     
              return new AssignNode(
               new ArrayAccessNode(bufferObjectNode, offsetIncrNode),
               (ExprNode) methodArgs.get(1));                                        
           }
        } 
                
        return superRetval;
    }
    
    protected SDFActorCodeGeneratorInfo _sdfActorInfo = null;    
}
