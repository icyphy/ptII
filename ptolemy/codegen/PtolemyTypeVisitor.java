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
        _actorInfo = actorInfo;    
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        MethodDecl decl = (MethodDecl) JavaDecl.getDecl(node.getMethod());
        
        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();
        
        // if this is a static method call, we can't do any more.
        if (!(fieldAccessNode instanceof TypeFieldAccessNode)) {

           String methodName = decl.getName();        

           ExprNode accessedObj = (ExprNode) ExprUtility.accessedObject(fieldAccessNode);   

           TypeNode accessedObjType = type(accessedObj);
                      
           int accessedObjKind = kind(accessedObjType); 
           
           if (isSupportedTokenKind(accessedObjKind)) {
                                                                                                                 
              if (methodName.equals("convert") || methodName.equals("one") || 
                  methodName.equals("oneRight") || methodName.equals("zero")) {
                 return _setType(node, accessedObjType); 
              }
           
              if (methodName.equals("getElementAsToken")) {
                 return _setType(node, 
                  typeNodeForKind(kindOfMatrixElement(accessedObjKind)));
              }

              List methodArgs = node.getArgs();
                                      
              if (methodArgs.size() == 1) {
                 ExprNode firstArg = (ExprNode) methodArgs.get(0);
                 int firstArgKind = kind(type(firstArg));              
                 
                 if (methodName.equals("add") || methodName.equals("addReverse") ||
                     methodName.equals("subtract") || methodName.equals("subtractReverse") ||
                     methodName.equals("multiply") || methodName.equals("multiplyReverse") || 
                     methodName.equals("divide") || methodName.equals("divideReverse") ||
                     methodName.equals("modulo") || methodName.equals("moduloReverse"))  {
                    TypeNode retval = typeNodeForKind(
                     moreGeneralTokenKind(accessedObjKind, firstArgKind));
                    return _setType(node, retval);
                 }                                 
              }
                                                             
           } else if (accessedObjKind == PtolemyTypeVisitor.TYPE_KIND_PARAMETER) {
              if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
                 if (methodName.equals("getToken")) {
                    String paramName = fieldAccessNode.getName().getIdent();           
                    Token token = (Token) _actorInfo.parameterNameToTokenMap.get(paramName);                                      
                    if (token != null) {
                       return _setType(node, typeNodeForTokenType(token.getType()));
                    }
                 }
              }
           } else if (isSupportedPortKind(accessedObjKind)) {
              if (accessedObj.classID() == THISFIELDACCESSNODE_ID) {
                 if (methodName.equals("get")) {
                  
                    TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                    String portName = typedDecl.getName();
                 
                    TypedIOPort port = (TypedIOPort) _actorInfo.portNameToPortMap.get(portName);                
            
                    if (port != null) {
                       return _setType(node, typeNodeForTokenType(port.getType()));                        
                    }    
                 } 
              }
           }                
        }
                                      
        return _setType(node, decl.getType());
    }

    public boolean isConcreteTokenKind(int kind) {
        if (!isSupportedTokenKind(kind)) {
           return false;
        }
        return _IS_CONCRETE_TOKEN[kind - TYPE_KIND_TOKEN];
    }

    public boolean isMatrixTokenKind(int kind) {
        return ((kind >= TYPE_KIND_MATRIX_TOKEN) && 
                (kind <= TYPE_KIND_FIX_MATRIX_TOKEN)); 
    }

    public boolean isScalarTokenKind(int kind) {
        return ((kind >= TYPE_KIND_SCALAR_TOKEN) && 
                (kind <= TYPE_KIND_FIX_TOKEN)); 
    }

    /** Return true iff the kind represents a TypedAtomicActor or a subclass of it
     *  that is valid in the given domain. This method should be overridden in 
     *  derived classes.
     */
    public boolean isSupportedActorKind(int kind) {
        return (kind == TYPE_KIND_TYPED_ATOMIC_ACTOR);
    }

    /** Return true iff the kind represents a TypedIOPort or a subclass of it
     *  that is valid in the given domain. This method should be overridden in 
     *  derived classes.
     */
    public boolean isSupportedPortKind(int kind) {
        return (kind == TYPE_KIND_TYPED_IO_PORT);
    }
    
    public boolean isSupportedTokenKind(int kind) {
        return ((kind >= TYPE_KIND_TOKEN) && 
                (kind <= TYPE_KIND_FIX_MATRIX_TOKEN));
    }

    /** Return an integer representing the user type that has the specified ClassDecl, 
     *  which may be a special type in Ptolemy. If the type is not a special type, 
     *  return the integer given by super.kindOfTypeNameNode(classDecl.getDefType()).
     */     
    public int kindOfClassDecl(ClassDecl classDecl) {    
        for (int i = 0; i < _KNOWN_CLASS_DECLS.length; i++) {
            if (classDecl == _KNOWN_CLASS_DECLS[i]) {
               return _KNOWN_KINDS[i];
            }
        }         
        return super.kindOfTypeNameNode(classDecl.getDefType());
    }
    
    /** Return the kind of token that would be returned by getElementAsToken() on 
     *  a Token of the given kind. Throw an IllegalArgumentException if the kind is
     *  not a MatrixToken kind. If kind is TYPE_KIND_MATRIX_TOKEN, return
     *  TYPE_KIND_INT.
     */
    public int kindOfMatrixElement(int kind) {
        if ((kind < TYPE_KIND_MATRIX_TOKEN) || 
            (kind > TYPE_KIND_FIX_MATRIX_TOKEN)) {
           throw new IllegalArgumentException("matrixElementTokenKind() : kind (" 
            + kind + ") is not a MatrixToken kind,");
        }

        return _MATRIX_ELEMENT_TOKEN_KINDS[kind - TYPE_KIND_MATRIX_TOKEN];
    }
                              
    /** Return the kind corresponding to a type in Ptolemy. Type should not be null. */
    
    public int kindOfTokenType(Type type) {    
        for (int i = 0; i < _KNOWN_TOKEN_TYPES.length; i++) {
            if (type == _KNOWN_TOKEN_TYPES[i]) {
               return i + TYPE_KIND_TOKEN;
            }            
        }
        ApplicationUtility.error("kindOfTokenType(): type unknown, type = " + type);
        return TYPE_KIND_UNKNOWN;
    }

    /** Return an integer representing the type, which may be a special type
     *  in Ptolemy. If the type is not a special type, return the integer given by
     *  super.kind(type).
     */ 
    public int kindOfTypeNameNode(TypeNameNode type) {                       
        ClassDecl classDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) type);
        return kindOfClassDecl(classDecl); 
    }

    /** Return the kind of the token that is more general (the other kind should
     *  be convertible to the returned kind). The kinds must both be those of concrete 
     *  Token subclasses. 
     */
    public int moreGeneralTokenKind(int kind1, int kind2) {
        // handle only half the symmetric cases
        if (kind2 < kind1) return moreGeneralTokenKind(kind2, kind1);
        
        if (kind1 == kind2) return kind1;
        
        // kind1 < kind2
        switch (kind1) {
                              
          case TYPE_KIND_INT_TOKEN:          
          if ((kind2 != TYPE_KIND_DOUBLE_TOKEN) && (kind2 != TYPE_KIND_LONG_TOKEN) &&
              (kind2 != TYPE_KIND_COMPLEX_TOKEN)) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = IntToken, kind2 = " + 
              kind2);          
          }
          return kind2;
              
          case TYPE_KIND_DOUBLE_TOKEN:          
          if (kind2 != TYPE_KIND_COMPLEX_TOKEN) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = DoubleToken, kind2 = " + 
              kind2);          
          }
          return TYPE_KIND_COMPLEX_TOKEN;
          
          case TYPE_KIND_INT_MATRIX_TOKEN:          
          if ((kind2 != TYPE_KIND_DOUBLE_MATRIX_TOKEN) && (kind2 != TYPE_KIND_LONG_TOKEN) &&
              (kind2 != TYPE_KIND_COMPLEX_TOKEN)) {
             ApplicationUtility.error("moreSpecificTokenKind() : kind1 = IntMatrixToken, kind2 = " + 
              kind2);          
          }
          return kind2;
              
          case TYPE_KIND_DOUBLE_MATRIX_TOKEN:          
          if (kind2 != TYPE_KIND_COMPLEX_MATRIX_TOKEN) {
             ApplicationUtility.error("moreGeneralTokenKind() : kind1 = DoubleMatrixToken, kind2 = " + 
              kind2);          
          }
          return TYPE_KIND_COMPLEX_MATRIX_TOKEN;

          // abstract types
          case TYPE_KIND_TOKEN: 
          case TYPE_KIND_SCALAR_TOKEN:
          case TYPE_KIND_MATRIX_TOKEN:

          // types that are already as general as possible
          case TYPE_KIND_BOOLEAN_TOKEN: 
          case TYPE_KIND_LONG_TOKEN:          
          case TYPE_KIND_COMPLEX_TOKEN:          
          case TYPE_KIND_FIX_TOKEN:                    
          case TYPE_KIND_BOOLEAN_MATRIX_TOKEN: 
          case TYPE_KIND_LONG_MATRIX_TOKEN:          
          case TYPE_KIND_COMPLEX_MATRIX_TOKEN:          
          case TYPE_KIND_FIX_MATRIX_TOKEN:                              
          case TYPE_KIND_OBJECT_TOKEN:                    
          case TYPE_KIND_STRING_TOKEN: // change this when we change the type lattice                                                   
          ApplicationUtility.error("moreGeneralTokenKind() : kind1 = " + kind1 + 
           " kind2 = " + kind2);                                                                                          
           
          default:
          ApplicationUtility.error("moreGeneralTokenKind() : kind unknown :  " + kind1);
        }            
        return TYPE_KIND_UNKNOWN;
    }
    
    /** Return a new TypeNameNode corresponding to a token type in Ptolemy. 
     *  If the argument type is BaseType.NAT, return a clone of 
     *  DUMMY_LOWER_BOUND_TYPE. The argument should not be null. 
     */    
    public TypeNameNode typeNodeForTokenType(Type type) {           
        if (type == BaseType.NAT) {
           return (TypeNameNode) DUMMY_LOWER_BOUND_TYPE.clone();        
        }         
    
        return typeNodeForKind(kindOfTokenType(type));        
    }
    
    /** Return a new TypeNameNode that corresponds to the type indicated by
     *  the kind. The TypeNameNode must be reallocated so that later operations
     *  on the node do not affect TypeNameNode stored in this class.
     */
    public TypeNameNode typeNodeForKind(int kind) {
        return (TypeNameNode) _KNOWN_TYPENAMENODES[kind - TYPE_KINDS].clone();    
    }
    
    protected ActorCodeGeneratorInfo _actorInfo;
        
    // mathematical kinds    
    public static final int TYPE_KIND_COMPLEX              = TYPE_KINDS;      
    public static final int TYPE_KIND_FIX_POINT            = TYPE_KIND_COMPLEX + 1;      
    
    // actor kind
    public static final int TYPE_KIND_TYPED_ATOMIC_ACTOR   = TYPE_KIND_FIX_POINT + 1;      
    
    // token kinds        
    public static final int TYPE_KIND_TOKEN                = TYPE_KIND_TYPED_ATOMIC_ACTOR + 1;      
    public static final int TYPE_KIND_BOOLEAN_TOKEN        = TYPE_KIND_TOKEN + 1;
    public static final int TYPE_KIND_SCALAR_TOKEN         = TYPE_KIND_BOOLEAN_TOKEN + 1;
    public static final int TYPE_KIND_INT_TOKEN            = TYPE_KIND_SCALAR_TOKEN + 1;
    public static final int TYPE_KIND_DOUBLE_TOKEN         = TYPE_KIND_INT_TOKEN + 1;
    public static final int TYPE_KIND_LONG_TOKEN           = TYPE_KIND_DOUBLE_TOKEN + 1;
    public static final int TYPE_KIND_COMPLEX_TOKEN        = TYPE_KIND_LONG_TOKEN + 1;
    public static final int TYPE_KIND_FIX_TOKEN            = TYPE_KIND_COMPLEX_TOKEN + 1;    
    public static final int TYPE_KIND_OBJECT_TOKEN         = TYPE_KIND_FIX_TOKEN + 1;                                 
    public static final int TYPE_KIND_STRING_TOKEN         = TYPE_KIND_OBJECT_TOKEN + 1;                  
    public static final int TYPE_KIND_MATRIX_TOKEN         = TYPE_KIND_STRING_TOKEN + 1;
    public static final int TYPE_KIND_BOOLEAN_MATRIX_TOKEN = TYPE_KIND_MATRIX_TOKEN + 1;    
    public static final int TYPE_KIND_INT_MATRIX_TOKEN     = TYPE_KIND_BOOLEAN_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_DOUBLE_MATRIX_TOKEN  = TYPE_KIND_INT_MATRIX_TOKEN + 1; 
    public static final int TYPE_KIND_LONG_MATRIX_TOKEN    = TYPE_KIND_DOUBLE_MATRIX_TOKEN + 1;    
    public static final int TYPE_KIND_COMPLEX_MATRIX_TOKEN = TYPE_KIND_LONG_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_FIX_MATRIX_TOKEN     = TYPE_KIND_COMPLEX_MATRIX_TOKEN + 1;

    // parameter kind
    public static final int TYPE_KIND_PARAMETER            = TYPE_KIND_FIX_MATRIX_TOKEN + 1;
    
    // port kind    
    public static final int TYPE_KIND_TYPED_IO_PORT        = TYPE_KIND_PARAMETER + 1;
        
    // actor type                
    public static final ClassDecl TYPED_ATOMIC_ACTOR_DECL;
    public static final TypeNameNode TYPED_ATOMIC_ACTOR_TYPE;
    
    // mathematical types
        
    public static final ClassDecl COMPLEX_DECL;
    public static final TypeNameNode COMPLEX_TYPE;
    
    public static final ClassDecl FIX_POINT_DECL;
    public static final TypeNameNode FIX_POINT_TYPE;
     
    // token types 
     
    public static final ClassDecl TOKEN_DECL;    
    public static final TypeNameNode TOKEN_TYPE;

    public static final ClassDecl SCALAR_TOKEN_DECL;
    public static final TypeNameNode SCALAR_TOKEN_TYPE;
    
    public static final ClassDecl BOOLEAN_TOKEN_DECL;
    public static final TypeNameNode BOOLEAN_TOKEN_TYPE;
        
    public static final ClassDecl INT_TOKEN_DECL;
    public static final TypeNameNode INT_TOKEN_TYPE;
        
    public static final ClassDecl DOUBLE_TOKEN_DECL;
    public static final TypeNameNode DOUBLE_TOKEN_TYPE;
        
    public static final ClassDecl LONG_TOKEN_DECL;
    public static final TypeNameNode LONG_TOKEN_TYPE;
    
    public static final ClassDecl COMPLEX_TOKEN_DECL;
    public static final TypeNameNode COMPLEX_TOKEN_TYPE;

    public static final ClassDecl FIX_TOKEN_DECL;
    public static final TypeNameNode FIX_TOKEN_TYPE;

    public static final ClassDecl STRING_TOKEN_DECL;
    public static final TypeNameNode STRING_TOKEN_TYPE;

    public static final ClassDecl OBJECT_TOKEN_DECL;
    public static final TypeNameNode OBJECT_TOKEN_TYPE;
            
    public static final ClassDecl MATRIX_TOKEN_DECL;
    public static final TypeNameNode MATRIX_TOKEN_TYPE;            
            
    public static final ClassDecl BOOLEAN_MATRIX_TOKEN_DECL;
    public static final TypeNameNode BOOLEAN_MATRIX_TOKEN_TYPE;
        
    public static final ClassDecl INT_MATRIX_TOKEN_DECL;
    public static final TypeNameNode INT_MATRIX_TOKEN_TYPE;
       
    public static final ClassDecl DOUBLE_MATRIX_TOKEN_DECL;
    public static final TypeNameNode DOUBLE_MATRIX_TOKEN_TYPE;
    
    public static final ClassDecl LONG_MATRIX_TOKEN_DECL;
    public static final TypeNameNode LONG_MATRIX_TOKEN_TYPE;   
            
    public static final ClassDecl COMPLEX_MATRIX_TOKEN_DECL;
    public static final TypeNameNode COMPLEX_MATRIX_TOKEN_TYPE;
            
    public static final ClassDecl FIX_MATRIX_TOKEN_DECL;
    public static final TypeNameNode FIX_MATRIX_TOKEN_TYPE;
    
    // a lower bound for type resolution                                 
    public static final ClassDecl DUMMY_LOWER_BOUND; 
    public static final TypeNameNode DUMMY_LOWER_BOUND_TYPE;

    // parameter type
    public static final ClassDecl PARAMETER_DECL;
    public static final TypeNameNode PARAMETER_TYPE;
        
    // port type
    public static final ClassDecl TYPED_IO_PORT_DECL;                      
    public static final TypeNameNode TYPED_IO_PORT_TYPE;                      
        
    public static final Integer PTOLEMY_TRANSFORMED_KEY = 
     new Integer(RESERVED_JAVA_PROPERTIES);
                             
    /** An array indexed by (kind - TYPE_KINDS) containing known declarations 
     *  of types in Ptolemy. 
     */                             
    protected static final ClassDecl[] _KNOWN_CLASS_DECLS;
    
    /** An array indexed by (kind - TYPE_KINDS) containing known types in 
     *  Ptolemy. 
     */                                 
    protected static final TypeNameNode[] _KNOWN_TYPENAMENODES;
    
    /** An array indexed by (kind - TYPE_KINDS) containing kinds of types in 
     *  Ptolemy. 
     */
    protected static final int[] _KNOWN_KINDS;
    
    /** An array indexed by (kind - TYPE_KIND_TOKEN) that is the corresponding
     *  Ptolemy type of the kind of token.
     */   
    protected static final Type[] _KNOWN_TOKEN_TYPES;

    /** An array indexed by (kind - TYPE_KIND_TOKEN) that indicates whether
     *  or not the token kind is concrete.
     */    
    protected static final boolean[] _IS_CONCRETE_TOKEN;
    
    /** An array indexed by (kind - TYPE_KIND_MATRIX_TOKEN) that gives the
     *  the kind of the token that would be returned by getElementAsToken()
     *  on a token of the index kind.
     */     
    protected static final int[] _MATRIX_ELEMENT_TOKEN_KINDS;
                                                                                          
    static {
                                            
        CompileUnitNode typedAtomicActorUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.actor.TypedAtomicActor", true), 1);

        TYPED_ATOMIC_ACTOR_DECL = (ClassDecl) StaticResolution.findDecl(
         typedAtomicActorUnit, "TypedAtomicActor", CG_CLASS, null, null);
         
        TYPED_ATOMIC_ACTOR_TYPE = TYPED_ATOMIC_ACTOR_DECL.getDefType();         
                                            
        CompileUnitNode complexUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.math.Complex", true), 1);

        COMPLEX_DECL = (ClassDecl) StaticResolution.findDecl(complexUnit,
         "Complex", CG_CLASS, null, null);
         
        COMPLEX_TYPE = COMPLEX_DECL.getDefType();         
         
        CompileUnitNode fixPointUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.math.FixPoint", true), 1);

        FIX_POINT_DECL = (ClassDecl) StaticResolution.findDecl(fixPointUnit,
         "Complex", CG_CLASS, null, null);
         
        FIX_POINT_TYPE = FIX_POINT_DECL.getDefType();         
                  
        CompileUnitNode tokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.Token", true), 1);

        TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(tokenUnit, 
         "Token", CG_CLASS, null, null);          
         
        TOKEN_TYPE = TOKEN_DECL.getDefType();
                  
        CompileUnitNode booleanTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.BooleanToken", true), 1);
         
        BOOLEAN_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(booleanTokenUnit, 
         "BooleanToken", CG_CLASS, null, null);      
         
        BOOLEAN_TOKEN_TYPE = BOOLEAN_TOKEN_DECL.getDefType();
        
        CompileUnitNode scalarTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ScalarToken", true), 1);
         
        SCALAR_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(booleanTokenUnit, 
         "ScalarToken", CG_CLASS, null, null);      
         
        SCALAR_TOKEN_TYPE = SCALAR_TOKEN_DECL.getDefType();
                              
        CompileUnitNode intTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.IntToken", true), 1);
         
        INT_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(intTokenUnit, 
         "IntToken", CG_CLASS, null, null);  
         
        INT_TOKEN_TYPE = INT_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode doubleTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.DoubleToken", true), 1);
         
        DOUBLE_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(doubleTokenUnit, 
         "DoubleToken", CG_CLASS, null, null);          

        DOUBLE_TOKEN_TYPE = DOUBLE_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode longTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.LongToken", true), 1);
         
        LONG_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(doubleTokenUnit, 
         "LongToken", CG_CLASS, null, null);          

        LONG_TOKEN_TYPE = LONG_TOKEN_DECL.getDefType();        
                                    
        CompileUnitNode complexTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ComplexToken", true), 1);
         
        COMPLEX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(complexTokenUnit, 
         "ComplexToken", CG_CLASS, null, null);          
         
        COMPLEX_TOKEN_TYPE = COMPLEX_TOKEN_DECL.getDefType();        
        
        CompileUnitNode fixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.FixToken", true), 1);
         
        FIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(fixTokenUnit, 
         "FixToken", CG_CLASS, null, null);          
         
        FIX_TOKEN_TYPE = FIX_TOKEN_DECL.getDefType();        
        
        CompileUnitNode objectTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ObjectToken", true), 1);
         
        OBJECT_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(objectTokenUnit, 
         "ObjectToken", CG_CLASS, null, null);          
         
        OBJECT_TOKEN_TYPE = OBJECT_TOKEN_DECL.getDefType();        

        CompileUnitNode stringTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.StringToken", true), 1);
         
        STRING_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(stringTokenUnit, 
         "StringToken", CG_CLASS, null, null);          
         
        STRING_TOKEN_TYPE = STRING_TOKEN_DECL.getDefType();        

        CompileUnitNode matrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.MatrixToken", true), 1);
         
        MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         matrixTokenUnit,  "MatrixToken", CG_CLASS, null, null);          
         
        MATRIX_TOKEN_TYPE = MATRIX_TOKEN_DECL.getDefType();        
                         
        CompileUnitNode booleanMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.BooleanMatrixToken", true), 1);
         
        BOOLEAN_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         booleanMatrixTokenUnit,  "BooleanMatrixToken", CG_CLASS, null, null);          
         
        BOOLEAN_MATRIX_TOKEN_TYPE = BOOLEAN_MATRIX_TOKEN_DECL.getDefType();        
                                 
        CompileUnitNode intMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.IntMatrixToken", true), 1);
                  
        INT_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         intMatrixTokenUnit,  "IntMatrixToken", CG_CLASS, null, null);          
         
        INT_MATRIX_TOKEN_TYPE = INT_MATRIX_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode doubleMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.DoubleMatrixToken", true), 1);
                  
        DOUBLE_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         doubleMatrixTokenUnit,  "DoubleMatrixToken", CG_CLASS, null, null);          
         
        DOUBLE_MATRIX_TOKEN_TYPE = DOUBLE_MATRIX_TOKEN_DECL.getDefType();
        
        CompileUnitNode longMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.LongMatrixToken", true), 1);
                  
        LONG_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         longMatrixTokenUnit,  "LongMatrixToken", CG_CLASS, null, null);          
         
        LONG_MATRIX_TOKEN_TYPE = LONG_MATRIX_TOKEN_DECL.getDefType();
                                   
        CompileUnitNode complexMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ComplexMatrixToken", true), 1);                 

        COMPLEX_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         complexMatrixTokenUnit,  "ComplexMatrixToken", CG_CLASS, null, null);          
         
        COMPLEX_MATRIX_TOKEN_TYPE = COMPLEX_MATRIX_TOKEN_DECL.getDefType();                           
        
        CompileUnitNode fixMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.FixMatrixToken", true), 1);
         
        FIX_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         booleanMatrixTokenUnit,  "FixMatrixToken", CG_CLASS, null, null);          
         
        FIX_MATRIX_TOKEN_TYPE = FIX_MATRIX_TOKEN_DECL.getDefType();                
                                
        DUMMY_LOWER_BOUND = new ClassDecl("DummyLowerBound", null);                                                
        NameNode dummyName = new NameNode(AbsentTreeNode.instance, "DummyLowerBound");
        dummyName.setProperty(DECL_KEY, DUMMY_LOWER_BOUND);
        DUMMY_LOWER_BOUND_TYPE = new TypeNameNode(dummyName);                
                
        CompileUnitNode parameterUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.expr.Parameter", true), 1);
         
        PARAMETER_DECL = (ClassDecl) StaticResolution.findDecl(
         parameterUnit,  "Parameter", CG_CLASS, null, null);          
         
        PARAMETER_TYPE = PARAMETER_DECL.getDefType();                
                                        
        CompileUnitNode typedIOPortUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.actor.TypedIOPort", true), 1);
         
        TYPED_IO_PORT_DECL = (ClassDecl) StaticResolution.findDecl(
         typedIOPortUnit,  "TypedIOPort", CG_CLASS, null, null);          
         
        TYPED_IO_PORT_TYPE = TYPED_IO_PORT_DECL.getDefType();                
                        
        _KNOWN_CLASS_DECLS = new ClassDecl[] { 
         COMPLEX_DECL, FIX_POINT_DECL, 
         TYPED_ATOMIC_ACTOR_DECL,         
         TOKEN_DECL, BOOLEAN_TOKEN_DECL, SCALAR_TOKEN_DECL, 
         INT_TOKEN_DECL, DOUBLE_TOKEN_DECL, LONG_TOKEN_DECL, 
         COMPLEX_TOKEN_DECL, FIX_TOKEN_DECL, OBJECT_TOKEN_DECL, 
         STRING_TOKEN_DECL, MATRIX_TOKEN_DECL, 
         BOOLEAN_MATRIX_TOKEN_DECL, INT_MATRIX_TOKEN_DECL, 
         DOUBLE_MATRIX_TOKEN_DECL, LONG_MATRIX_TOKEN_DECL, 
         COMPLEX_MATRIX_TOKEN_DECL, FIX_MATRIX_TOKEN_DECL, 
         PARAMETER_DECL, 
         TYPED_IO_PORT_DECL };
         
        _KNOWN_TYPENAMENODES = new TypeNameNode[] { 
         COMPLEX_TYPE, FIX_POINT_TYPE, 
         TYPED_ATOMIC_ACTOR_TYPE,
         TOKEN_TYPE, BOOLEAN_TOKEN_TYPE, SCALAR_TOKEN_TYPE, 
         INT_TOKEN_TYPE, DOUBLE_TOKEN_TYPE, LONG_TOKEN_TYPE,
         COMPLEX_TOKEN_TYPE, FIX_TOKEN_TYPE, OBJECT_TOKEN_TYPE, 
         STRING_TOKEN_TYPE, MATRIX_TOKEN_TYPE, 
         BOOLEAN_MATRIX_TOKEN_TYPE, INT_MATRIX_TOKEN_TYPE, 
         DOUBLE_MATRIX_TOKEN_TYPE, LONG_MATRIX_TOKEN_TYPE, 
         COMPLEX_MATRIX_TOKEN_TYPE, FIX_MATRIX_TOKEN_TYPE, 
         PARAMETER_TYPE, 
         TYPED_IO_PORT_TYPE };
         
        _KNOWN_KINDS = new int[] { 
         TYPE_KIND_COMPLEX, TYPE_KIND_FIX_POINT, 
         TYPE_KIND_TYPED_ATOMIC_ACTOR, 
         TYPE_KIND_TOKEN, TYPE_KIND_BOOLEAN_TOKEN, TYPE_KIND_SCALAR_TOKEN, 
         TYPE_KIND_INT_TOKEN, TYPE_KIND_DOUBLE_TOKEN, TYPE_KIND_LONG_TOKEN, 
         TYPE_KIND_COMPLEX_TOKEN, TYPE_KIND_FIX_TOKEN, TYPE_KIND_OBJECT_TOKEN, 
         TYPE_KIND_STRING_TOKEN, TYPE_KIND_MATRIX_TOKEN, 
         TYPE_KIND_BOOLEAN_MATRIX_TOKEN, TYPE_KIND_INT_MATRIX_TOKEN, 
         TYPE_KIND_DOUBLE_MATRIX_TOKEN, TYPE_KIND_LONG_MATRIX_TOKEN, 
         TYPE_KIND_COMPLEX_MATRIX_TOKEN, TYPE_KIND_FIX_MATRIX_TOKEN, 
         TYPE_KIND_PARAMETER, 
         TYPE_KIND_TYPED_IO_PORT };
         
        _KNOWN_TOKEN_TYPES = new Type[] { 
         BaseType.GENERAL, BaseType.BOOLEAN, BaseType.SCALAR, 
         BaseType.INT, BaseType.DOUBLE, BaseType.LONG, 
         BaseType.COMPLEX, BaseType.FIX, BaseType.OBJECT, 
         BaseType.STRING, BaseType.MATRIX, 
         BaseType.BOOLEAN_MATRIX, BaseType.INT_MATRIX, 
         BaseType.DOUBLE_MATRIX, BaseType.LONG_MATRIX, 
         BaseType.COMPLEX_MATRIX, BaseType.FIX_MATRIX };
   
        _IS_CONCRETE_TOKEN = new boolean[] {
         false, true, false, 
         true, true, true, 
         true, true, true, 
         true, false, 
         true, true,
         true, true, 
         true, true };  
        
        _MATRIX_ELEMENT_TOKEN_KINDS = new int[] {
         // the first entry is a patch that allows code containing unresolved 
         // MatrixTokens to work correctly
         TYPE_KIND_TOKEN, TYPE_KIND_INT_TOKEN, 
         TYPE_KIND_BOOLEAN_TOKEN, TYPE_KIND_INT_TOKEN, 
         TYPE_KIND_DOUBLE_TOKEN, TYPE_KIND_LONG_TOKEN, 
         TYPE_KIND_COMPLEX_TOKEN, TYPE_KIND_FIX_TOKEN };
    }    
}