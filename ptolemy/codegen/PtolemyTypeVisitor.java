/* A class containing declarations created by the compiler of
   of known fields and methods in the ptolemy.actor and ptolemy.data
   packages.

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

package ptolemy.codegen;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

/** A class containing declarations created by the compiler of
 *  of known fields and methods in the ptolemy.actor and ptolemy.data
 *  packages.
 *
 *  @author Jeff Tsay
 */
public class PtolemyTypeVisitor extends TypeVisitor
    implements JavaStaticSemanticConstants {

    public PtolemyTypeVisitor(ActorCodeGeneratorInfo actorInfo) {
        this(actorInfo, new PtolemyTypePolicy(new PtolemyTypeIdentifier()));
    }

    public PtolemyTypeVisitor(ActorCodeGeneratorInfo actorInfo,
            PtolemyTypePolicy typePolicy) {
        super(typePolicy);
        _actorInfo = actorInfo;

        _ptolemyTypePolicy = typePolicy;

        _ptolemyTypeID =
            (PtolemyTypeIdentifier) typePolicy.typeIdentifier();
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        MethodDecl decl = (MethodDecl) JavaDecl.getDecl(node.getMethod());

        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        // if this is a static method call, we can't do any more.
        if (!(fieldAccessNode instanceof TypeFieldAccessNode)) {

            String methodName = decl.getName();

            ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

            TypeNode accessedObjType = type(accessedObj);

            int accessedObjKind = _ptolemyTypeID.kind(accessedObjType);

            if (_ptolemyTypeID.isSupportedTokenKind(accessedObjKind)) {

                if (methodName.equals("convert") || methodName.equals("one") ||
                        methodName.equals("oneRight") || methodName.equals("zero")) {
                    return _setType(node, accessedObjType);
                }

                if (methodName.equals("getElementAsToken")) {
                    return _setType(node,
                            _ptolemyTypeID.typeNodeForKind(
                                    _ptolemyTypeID.kindOfMatrixElement(accessedObjKind)));
                }

                List methodArgs = node.getArgs();

                if (methodArgs.size() == 1) {
                    ExprNode firstArg = (ExprNode) methodArgs.get(0);
                    int firstArgKind = _ptolemyTypeID.kind(type(firstArg));

                    if (methodName.equals("add") || methodName.equals("addReverse") ||
                            methodName.equals("subtract") || methodName.equals("subtractReverse") ||
                            methodName.equals("multiply") || methodName.equals("multiplyReverse") ||
                            methodName.equals("divide") || methodName.equals("divideReverse") ||
                            methodName.equals("modulo") || methodName.equals("moduloReverse"))  {
                        TypeNode retval = _ptolemyTypeID.typeNodeForKind(
                                _ptolemyTypePolicy.moreGeneralTokenKind(accessedObjKind, firstArgKind));
                        return _setType(node, retval);
                    }
                }

            } else if (accessedObjKind == PtolemyTypeIdentifier.TYPE_KIND_PARAMETER) {
                if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
                    if (methodName.equals("getToken")) {
                        String paramName = fieldAccessNode.getName().getIdent();
                        Token token = (Token) _actorInfo.parameterNameToTokenMap.get(paramName);
                        if (token != null) {
                            return _setType(node,
                                    _ptolemyTypeID.typeNodeForTokenType(token.getType()));
                        }
                    }
                }
            } else if (_ptolemyTypeID.isSupportedPortKind(accessedObjKind)) {
                if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
                    if (methodName.equals("get")) {

                        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                        String portName = typedDecl.getName();

                        TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(portName);

                        if (port != null) {
                            return _setType(node,
                                    _ptolemyTypeID.typeNodeForTokenType(port.getType()));
                        }
                    }
                }
            }
        }

        return _setType(node, decl.getType());
    }

    protected ActorCodeGeneratorInfo _actorInfo;

    protected PtolemyTypePolicy _ptolemyTypePolicy;
    protected PtolemyTypeIdentifier _ptolemyTypeID;
}
