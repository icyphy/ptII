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
        LinkedList retval = new LinkedList();
        String varName = node.getName().getIdent();

        TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);

        int portWidth = port.getWidth();

        if (portWidth > 1) {

           // an array of offsets (minus 1) into the buffer for each channel

           ExprNode widthExprNode = new IntLitNode(String.valueOf(portWidth));

           LinkedList offsetInitList = new LinkedList();

           for (int i = 0; i < portWidth; i++) {
               offsetInitList.addLast(new IntLitNode("-1"));
           }

           retval.addLast(new FieldDeclNode(PROTECTED_MOD,
            TypeUtility.makeArrayType(IntTypeNode.instance, 1),
            new NameNode(AbsentTreeNode.instance,
             "_cg_" + varName + "_offset"), new ArrayInitNode(offsetInitList)));

           if (port.isInput()) {
              // an array of buffers associated with the input channels

              String[] bufferNames = (String[]) _sdfActorInfo.inputBufferNameMap.get(port);

              LinkedList arrayInitList = new LinkedList();

              for (int i = 0; i < bufferNames.length; i++) {
                  arrayInitList.addLast(new ObjectNode((NameNode)
                   StaticResolution.makeNameNode("CG_Main." + bufferNames[i])));
              }

              TypeNameNode portTypeNode = _typeID.typeNodeForTokenType(port.getType());
              TypeNode bufferArrayTypeNode = TypeUtility.makeArrayType(
               (TypeNode) portTypeNode.accept(this, args), 2);

              retval.addLast(new FieldDeclNode(PROTECTED_MOD | FINAL_MOD,
               bufferArrayTypeNode, new NameNode(AbsentTreeNode.instance,
                "_cg_" + varName + "_chan_buffer"),
               new ArrayInitNode(arrayInitList)));

              // an array of buffer lengths

              int[] bufferLengths =
               (int[]) _sdfActorInfo.inputBufferLengthMap.get(port);

              LinkedList lengthArrayInitList = new LinkedList();

              for (int i = 0; i < bufferLengths.length; i++) {
                  lengthArrayInitList.addLast(
                   new IntLitNode(String.valueOf(bufferLengths[i])));
              }

              retval.addLast(new FieldDeclNode(PROTECTED_MOD | FINAL_MOD,
               TypeUtility.makeArrayType(IntTypeNode.instance, 1),
               new NameNode(AbsentTreeNode.instance,
                "_cg_" + varName + "_chan_buffer_len"),
               new ArrayInitNode(lengthArrayInitList)));
           }

        } else {

           // an offset (minus 1) into the buffer

           retval.addLast(new FieldDeclNode(PROTECTED_MOD,
            IntTypeNode.instance,
            new NameNode(AbsentTreeNode.instance,
             "_cg_" + varName + "_offset"),
            new IntLitNode("-1")));
        }

        return retval;
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

        if (methodName.equals("broadcast")) {

           BufferInfo bufferInfo = (BufferInfo) _sdfActorInfo.outputInfoMap.get(port);

           if (bufferInfo == null) {
              // port is not connected
              // return the arguments which may have side effects
              return methodArgs;
           }

           String bufferLengthString = String.valueOf(bufferInfo.length);

           if (port.getWidth() > 1) {

              // a counter for the loop
              NameNode chanNameNode = new NameNode(
               AbsentTreeNode.instance, "_cg_chan_temp");

              ObjectNode chanObjectNode = new ObjectNode(chanNameNode);

              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
               "CG_Main." + bufferInfo.codeGenName));

              ArrayAccessNode bufferArrayAccessNode = new ArrayAccessNode(
               bufferObjectNode, chanObjectNode);

              // the previous offset
              ArrayAccessNode offsetNode = new ArrayAccessNode(
               new ObjectNode(
                new NameNode(AbsentTreeNode.instance,
                 "_cg_" + varName + "_offset")),
                (ExprNode) chanObjectNode.clone());

              // update and return the new offset
              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode(
                (ExprNode) offsetNode.clone(),
                new IntLitNode("1")), new IntLitNode(bufferLengthString)));

              ExprStmtNode putStmtNode = new ExprStmtNode(new AssignNode(
               new ArrayAccessNode(bufferArrayAccessNode, offsetUpdateNode),
                (ExprNode) methodArgs.get(0)));

              // wrap the put statement in a for loop

              List forInitList = TNLManip.cons(
               new LocalVarDeclNode(NO_MOD, IntTypeNode.instance,
               (NameNode) chanNameNode.clone(),
               new IntLitNode("0")));

              LTNode forTestExprNode = new LTNode(
               (ExprNode) chanObjectNode.clone(),
               new IntLitNode(String.valueOf(port.getWidth())));

              List forUpdateList = TNLManip.cons(new PostIncrNode(
               (ExprNode) chanObjectNode.clone()));

              // return value is not an expression, needs to get
              // handled by visitExprStmtNode()
              return new ForNode(forInitList, forTestExprNode,
               forUpdateList, putStmtNode);

           } else {

              // if there is only 1 channel, this is like a regular
              // send(0, t) call

              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
                "CG_Main." + bufferInfo.codeGenName));

              ObjectNode offsetNode = new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_" + varName +
                "_offset"));

              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode(
                (ExprNode) offsetNode.clone(),
                new IntLitNode("1")), new IntLitNode(bufferLengthString)));

              return new AssignNode(
               new ArrayAccessNode(bufferObjectNode, offsetUpdateNode),
               (ExprNode) methodArgs.get(0));
           }

        } else if (methodName.equals("get")) {
           String[] bufferArray = (String[]) _sdfActorInfo.inputBufferNameMap.get(port);
           int[] bufferLengthArray =
            (int[]) _sdfActorInfo.inputBufferLengthMap.get(port);

           if ((bufferArray == null) || (bufferArray.length < 1)) {
              // port is not connected, return 0 since Token is resolved to int
              return new IntLitNode("0");
           }

           int channel = -1;

           if (port.getWidth() <= 1) {
              channel = 0;
           } else if (firstArg instanceof IntLitNode) {
              // found a constant port number (it would help if constant folding
              // were done)
              channel = Integer.parseInt(((IntLitNode) firstArg).getLiteral());
           }

           if (channel == -1) {
              // need to do a lookup of buffer for the port

              ObjectNode chanObjectNode = new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_r"));

              // assign the channel to a dummy variable to avoid side effects
              AssignNode chanAssignNode = new AssignNode(chanObjectNode,
               firstArg);

              ArrayAccessNode findBufferNode = new ArrayAccessNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance,
                "_cg_" + varName + "_chan_buffer")),
               chanAssignNode);

              // need to do a lookup of the length of the buffer
              ArrayAccessNode findBufferLengthNode = new ArrayAccessNode(
               new ObjectNode(
                new NameNode(AbsentTreeNode.instance, "_cg_" + varName +
                 "_chan_buffer_len")),
                (ExprNode) chanObjectNode.clone());

              // the previous offset
              ArrayAccessNode offsetNode = new ArrayAccessNode(new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset")),
               (ExprNode) chanObjectNode.clone());

              // update and return the new offset

              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode((ExprNode) offsetNode.clone(),
                new IntLitNode("1")), findBufferLengthNode));

              return new ArrayAccessNode(findBufferNode, offsetUpdateNode);

           } else {
              String bufferName = bufferArray[channel];
              String bufferLengthString = String.valueOf(bufferLengthArray[channel]);

              ExprNode offsetNode = null;

              if (port.getWidth() > 1) {
                 offsetNode = new ArrayAccessNode(
                  new ObjectNode(new NameNode(AbsentTreeNode.instance,
                   "_cg_" + varName + "_offset")), firstArg);
              } else {
                 offsetNode = new ObjectNode(
                   new NameNode(AbsentTreeNode.instance,
                   "_cg_" + varName + "_offset"));
              }

              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode(
                (ExprNode) offsetNode.clone(),
                new IntLitNode("1")), new IntLitNode(bufferLengthString)));

              return new ArrayAccessNode(new ObjectNode(
               (NameNode) StaticResolution.makeNameNode("CG_Main." + bufferName)),
               offsetUpdateNode);
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
              // return the arguments which may have side effects
              return methodArgs;
           }

           String bufferLengthString = String.valueOf(bufferInfo.length);

           if (port.getWidth() > 1) {

              ObjectNode chanObjectNode = new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_chan_temp_w"));

               // assign the channel to a dummy variable to avoid side effects
              AssignNode chanAssignNode = new AssignNode(chanObjectNode,
               firstArg);

              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
               "CG_Main." + bufferInfo.codeGenName));

              ArrayAccessNode bufferArrayAccessNode = new ArrayAccessNode(
               bufferObjectNode, chanAssignNode);

              // the previous offset
              ArrayAccessNode offsetNode = new ArrayAccessNode(
               new ObjectNode(
                new NameNode(AbsentTreeNode.instance,
                 "_cg_" + varName + "_offset")),
                (ExprNode) chanObjectNode.clone());

              // update and return the new offset
              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode(
                (ExprNode) offsetNode.clone(),
                new IntLitNode("1")), new IntLitNode(bufferLengthString)));

              return new AssignNode(
               new ArrayAccessNode(bufferArrayAccessNode, offsetUpdateNode),
                (ExprNode) methodArgs.get(1));

           } else {
              ObjectNode bufferObjectNode = new ObjectNode(
               (NameNode) StaticResolution.makeNameNode(
                "CG_Main." + bufferInfo.codeGenName));

              ObjectNode offsetNode = new ObjectNode(
               new NameNode(AbsentTreeNode.instance, "_cg_" + varName + "_offset"));

              AssignNode offsetUpdateNode = new AssignNode(
               offsetNode, new RemNode(new PlusNode(
                (ExprNode) offsetNode.clone(),
                new IntLitNode("1")), new IntLitNode(bufferLengthString)));

              return new AssignNode(
               new ArrayAccessNode(bufferObjectNode, offsetUpdateNode),
               (ExprNode) methodArgs.get(1));
           }
        }

        return superRetval;
    }

    protected SDFActorCodeGeneratorInfo _sdfActorInfo = null;
}
