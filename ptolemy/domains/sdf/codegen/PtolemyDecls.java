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

package ptolemy.domains.sdf.codegen;

import java.util.LinkedList;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A class containing declarations created by the compiler of
 *  of known fields and methods in the ptolemy.actor and ptolemy.data
 *  packages.
 *
 *  @author Jeff Tsay
 */
public class PtolemyDecls implements JavaStaticSemanticConstants {

    public static boolean isConcreteTokenType(int kind) {
        if (kind < TypeUtility.TYPE_KINDS) {
           return false;
        }
        return _isConcreteToken[kind - TypeUtility.TYPE_KINDS];
    }

    public static boolean isScalarTokenType(int kind) {
        return ((kind >= TYPE_KIND_SCALAR_TOKEN) && 
                (kind <= TYPE_KIND_FIX_TOKEN)); 
    }
    
    public static boolean isSupportedTokenType(int kind) {
        return ((kind >= TYPE_KIND_TOKEN) && 
                (kind <= TYPE_KIND_FIX_MATRIX_TOKEN));
    }

    /** Return an integer representing the type, which may be a special type
     *  in Ptolemy. If the type is not a special type, return the integer given by
     *  TypeUtility.kind(type).
     */ 
    public static int kind(TypeNode type) {              
         switch (type.classID()) {
                                           
           case TYPENAMENODE_ID:
           ClassDecl classDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) type);
           return kind(classDecl); 
             
           // no break here
                       
           default:
           // primitive types, arrays, and user types unknown to us
           return TypeUtility.kind(type);
         }            
    }

    /** Return an integer representing the user type that has the specified ClassDecl, 
     *  which may be a special type in Ptolemy. If the type is not a special type, 
     *  return the integer given by TypeUtility.kind(type).
     */     
    public static int kind(ClassDecl classDecl) {    
        for (int i = 0; i < _knownClassDecls.length; i++) {
            if (classDecl == _knownClassDecls[i]) {
               return _knownKinds[i];
            }
        }         
        return TypeUtility.kind(classDecl.getDefType());
    }
        
    /** Return the kind corresponding to a type in Ptolemy. Type should not be null. */
    public static int kindOfTokenType(Type type) {
        for (int i = 0; i < _knownTokenTypes.length; i++) {
            if (type == _knownTypes[i]) {
               return _knownKinds[i];
            }            
        }
        ApplicationUtility.error("kindOfTokenType(): type unknown");
        return TypeUtility.TYPE_KIND_UNKNOWN;
    }
    
    /** Return a TypeNameNode corresponding to a token type in Ptolemy. Type should not be null. */    
    public static TypeNameNode typeNodeForTokenType(Type type) {
        if (type == BaseType.NAT) {
           return DUMMY_LOWER_BOUND_TYPE;        
        }         
    
        for (int i = 0; i < _knownTokenTypes.length; i++) {
            if (type == _knownTokenTypes[i]) {
               return _knownTypes[i];
            }            
        }
        ApplicationUtility.error("typeNodeForTokenType(): type unknown : " + type);
        return null;
    }
    
    public static TypeNameNode typeNodeForKind(int kind) {
        return _knownTypes[kind - TypeUtility.TYPE_KINDS];    
    }
    
    // mathematical kinds
    
    public static final int TYPE_KIND_COMPLEX              = TypeUtility.TYPE_KINDS;      
    public static final int TYPE_KIND_FIX_POINT            = TYPE_KIND_COMPLEX + 1;      
    
    // token kinds
        
    public static final int TYPE_KIND_TOKEN                = TYPE_KIND_FIX_POINT + 1;      
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

    // port kinds
    
    public static final int TYPE_KIND_TYPED_IO_PORT        = TYPE_KIND_FIX_MATRIX_TOKEN + 1;
    public static final int TYPE_KIND_SDF_IO_PORT          = TYPE_KIND_TYPED_IO_PORT + 1;
    
        
    public static final MethodDecl fireDecl;
    
    //public static final ClassDecl actorDecl;
    
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

    public static final ClassDecl TYPED_IO_PORT_DECL;                      
    public static final TypeNameNode TYPED_IO_PORT_TYPE;                      
    
    // a lower bound for type resolution                                 
    public static final ClassDecl DUMMY_LOWER_BOUND; 
    public static final TypeNameNode DUMMY_LOWER_BOUND_TYPE;

    // port types
    
    public static final ClassDecl SDF_IO_PORT_DECL;                      
    public static final TypeNameNode SDF_IO_PORT_TYPE;                          
                             
    protected static final ClassDecl[] _knownClassDecls;
    protected static final TypeNameNode[] _knownTypes;
    protected static final int[] _knownKinds;
    protected static final Type[] _knownTokenTypes;
    protected static final boolean[] _isConcreteToken;
                                   
                                   
    //public static final ClassDecl actorDecl;
        
        
    static {
        
        CompileUnitNode actorUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.actor.Actor", true), 1);
        
        fireDecl = (MethodDecl) StaticResolution.findDecl(actorUnit,
         "Actor.fire", CG_METHOD, new LinkedList());                          
         
        CompileUnitNode complexUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.math.Complex", true), 1);

        COMPLEX_DECL = (ClassDecl) StaticResolution.findDecl(complexUnit,
         "Complex", CG_CLASS, null);
         
        COMPLEX_TYPE = COMPLEX_DECL.getDefType();         
         
        CompileUnitNode fixPointUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.math.FixPoint", true), 1);

        FIX_POINT_DECL = (ClassDecl) StaticResolution.findDecl(fixPointUnit,
         "Complex", CG_CLASS, null);
         
        FIX_POINT_TYPE = FIX_POINT_DECL.getDefType();         
                  
        CompileUnitNode tokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.Token", true), 1);

        TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(tokenUnit, 
         "Token", CG_CLASS, null);          
         
        TOKEN_TYPE = TOKEN_DECL.getDefType();
                  
        CompileUnitNode booleanTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.BooleanToken", true), 1);
         
        BOOLEAN_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(booleanTokenUnit, 
         "BooleanToken", CG_CLASS, null);      
         
        BOOLEAN_TOKEN_TYPE = BOOLEAN_TOKEN_DECL.getDefType();
        
        CompileUnitNode scalarTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ScalarToken", true), 1);
         
        SCALAR_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(booleanTokenUnit, 
         "ScalarToken", CG_CLASS, null);      
         
        SCALAR_TOKEN_TYPE = SCALAR_TOKEN_DECL.getDefType();
                              
        CompileUnitNode intTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.IntToken", true), 1);
         
        INT_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(intTokenUnit, 
         "IntToken", CG_CLASS, null);  
         
        INT_TOKEN_TYPE = INT_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode doubleTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.DoubleToken", true), 1);
         
        DOUBLE_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(doubleTokenUnit, 
         "DoubleToken", CG_CLASS, null);          

        DOUBLE_TOKEN_TYPE = DOUBLE_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode longTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.LongToken", true), 1);
         
        LONG_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(doubleTokenUnit, 
         "LongToken", CG_CLASS, null);          

        LONG_TOKEN_TYPE = LONG_TOKEN_DECL.getDefType();        
                                    
        CompileUnitNode complexTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ComplexToken", true), 1);
         
        COMPLEX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(complexTokenUnit, 
         "ComplexToken", CG_CLASS, null);          
         
        COMPLEX_TOKEN_TYPE = COMPLEX_TOKEN_DECL.getDefType();        
        
        CompileUnitNode fixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.FixToken", true), 1);
         
        FIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(fixTokenUnit, 
         "FixToken", CG_CLASS, null);          
         
        FIX_TOKEN_TYPE = FIX_TOKEN_DECL.getDefType();        
        
        CompileUnitNode objectTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ObjectToken", true), 1);
         
        OBJECT_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(objectTokenUnit, 
         "ObjectToken", CG_CLASS, null);          
         
        OBJECT_TOKEN_TYPE = OBJECT_TOKEN_DECL.getDefType();        

        CompileUnitNode stringTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.StringToken", true), 1);
         
        STRING_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(stringTokenUnit, 
         "StringToken", CG_CLASS, null);          
         
        STRING_TOKEN_TYPE = STRING_TOKEN_DECL.getDefType();        

        CompileUnitNode matrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.MatrixToken", true), 1);
         
        MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         matrixTokenUnit,  "MatrixToken", CG_CLASS, null);          
         
        MATRIX_TOKEN_TYPE = MATRIX_TOKEN_DECL.getDefType();        
                         
        CompileUnitNode booleanMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.BooleanMatrixToken", true), 1);
         
        BOOLEAN_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         booleanMatrixTokenUnit,  "BooleanMatrixToken", CG_CLASS, null);          
         
        BOOLEAN_MATRIX_TOKEN_TYPE = BOOLEAN_MATRIX_TOKEN_DECL.getDefType();        
                                 
        CompileUnitNode intMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.IntMatrixToken", true), 1);
                  
        INT_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         intMatrixTokenUnit,  "IntMatrixToken", CG_CLASS, null);          
         
        INT_MATRIX_TOKEN_TYPE = INT_MATRIX_TOKEN_DECL.getDefType();        
                  
        CompileUnitNode doubleMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.DoubleMatrixToken", true), 1);
                  
        DOUBLE_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         doubleMatrixTokenUnit,  "DoubleMatrixToken", CG_CLASS, null);          
         
        DOUBLE_MATRIX_TOKEN_TYPE = DOUBLE_MATRIX_TOKEN_DECL.getDefType();
        
        CompileUnitNode longMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.LongMatrixToken", true), 1);
                  
        LONG_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         longMatrixTokenUnit,  "LongMatrixToken", CG_CLASS, null);          
         
        LONG_MATRIX_TOKEN_TYPE = LONG_MATRIX_TOKEN_DECL.getDefType();
                                   
        CompileUnitNode complexMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.ComplexMatrixToken", true), 1);                 

        COMPLEX_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         complexMatrixTokenUnit,  "ComplexMatrixToken", CG_CLASS, null);          
         
        COMPLEX_MATRIX_TOKEN_TYPE = COMPLEX_MATRIX_TOKEN_DECL.getDefType();                           
        
        CompileUnitNode fixMatrixTokenUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.data.FixMatrixToken", true), 1);
         
        FIX_MATRIX_TOKEN_DECL = (ClassDecl) StaticResolution.findDecl(
         booleanMatrixTokenUnit,  "FixMatrixToken", CG_CLASS, null);          
         
        FIX_MATRIX_TOKEN_TYPE = FIX_MATRIX_TOKEN_DECL.getDefType();                
                                
        DUMMY_LOWER_BOUND = new ClassDecl("DummyLowerBound", null);                                                
        NameNode dummyName = new NameNode(AbsentTreeNode.instance, "DummyLowerBound");
        dummyName.setProperty(DECL_KEY, DUMMY_LOWER_BOUND);
        DUMMY_LOWER_BOUND_TYPE = new TypeNameNode(dummyName);                
                
        CompileUnitNode typedIOPortUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.actor.TypedIOPort", true), 1);
         
        TYPED_IO_PORT_DECL = (ClassDecl) StaticResolution.findDecl(
         typedIOPortUnit,  "TypedIOPort", CG_CLASS, null);          
         
        TYPED_IO_PORT_TYPE = TYPED_IO_PORT_DECL.getDefType();                
        
        CompileUnitNode sdfIOPortUnit = StaticResolution.load(
         SearchPath.NAMED_PATH.openSource("ptolemy.domains.sdf.kernel.SDFIOPort", true), 1);
         
        SDF_IO_PORT_DECL = (ClassDecl) StaticResolution.findDecl(
         sdfIOPortUnit,  "SDFIOPort", CG_CLASS, null);          
         
        SDF_IO_PORT_TYPE = SDF_IO_PORT_DECL.getDefType();                                
                
        _knownClassDecls = new ClassDecl[] { 
         COMPLEX_DECL, FIX_POINT_DECL, TOKEN_DECL, BOOLEAN_TOKEN_DECL,
         SCALAR_TOKEN_DECL, INT_TOKEN_DECL, DOUBLE_TOKEN_DECL, LONG_TOKEN_DECL, 
         COMPLEX_TOKEN_DECL, FIX_TOKEN_DECL, OBJECT_TOKEN_DECL, STRING_TOKEN_DECL, 
         MATRIX_TOKEN_DECL, BOOLEAN_MATRIX_TOKEN_DECL, INT_MATRIX_TOKEN_DECL, 
         DOUBLE_MATRIX_TOKEN_DECL, LONG_MATRIX_TOKEN_DECL, COMPLEX_MATRIX_TOKEN_DECL,
         FIX_MATRIX_TOKEN_DECL, TYPED_IO_PORT_DECL, SDF_IO_PORT_DECL };
         
        _knownTypes = new TypeNameNode[] { 
         COMPLEX_TYPE, FIX_POINT_TYPE, TOKEN_TYPE, BOOLEAN_TOKEN_TYPE,
         SCALAR_TOKEN_TYPE, INT_TOKEN_TYPE, DOUBLE_TOKEN_TYPE, LONG_TOKEN_TYPE,
         COMPLEX_TOKEN_TYPE, FIX_TOKEN_TYPE, OBJECT_TOKEN_TYPE, STRING_TOKEN_TYPE,
         MATRIX_TOKEN_TYPE, BOOLEAN_MATRIX_TOKEN_TYPE, INT_MATRIX_TOKEN_TYPE, 
         DOUBLE_MATRIX_TOKEN_TYPE, LONG_MATRIX_TOKEN_TYPE, COMPLEX_MATRIX_TOKEN_TYPE,
         FIX_MATRIX_TOKEN_TYPE, TYPED_IO_PORT_TYPE, SDF_IO_PORT_TYPE };
         
        _knownKinds = new int[] { 
         TYPE_KIND_COMPLEX, TYPE_KIND_FIX_POINT, TYPE_KIND_TOKEN, TYPE_KIND_BOOLEAN_TOKEN,
         TYPE_KIND_SCALAR_TOKEN, TYPE_KIND_INT_TOKEN, TYPE_KIND_DOUBLE_TOKEN, TYPE_KIND_LONG_TOKEN, 
         TYPE_KIND_COMPLEX_TOKEN, TYPE_KIND_FIX_TOKEN, TYPE_KIND_OBJECT_TOKEN, TYPE_KIND_STRING_TOKEN, 
         TYPE_KIND_MATRIX_TOKEN, TYPE_KIND_BOOLEAN_MATRIX_TOKEN, TYPE_KIND_INT_MATRIX_TOKEN, 
         TYPE_KIND_DOUBLE_MATRIX_TOKEN, TYPE_KIND_LONG_MATRIX_TOKEN, TYPE_KIND_COMPLEX_MATRIX_TOKEN,
         TYPE_KIND_FIX_MATRIX_TOKEN, TYPE_KIND_TYPED_IO_PORT, TYPE_KIND_SDF_IO_PORT };
         
        _knownTokenTypes = new Type[] { 
         null, null, BaseType.GENERAL, BaseType.BOOLEAN, 
         BaseType.SCALAR, BaseType.INT, BaseType.DOUBLE, BaseType.LONG, 
         BaseType.COMPLEX, BaseType.FIX, BaseType.OBJECT, BaseType.STRING, 
         BaseType.MATRIX, BaseType.BOOLEAN_MATRIX, BaseType.INT_MATRIX, 
         BaseType.DOUBLE_MATRIX, BaseType.LONG_MATRIX, BaseType.COMPLEX_MATRIX,
         BaseType.FIX_MATRIX, null, null };

        _isConcreteToken = new boolean[] {
         false, false, false, false,
         false, true, true, true, 
         true, true, true, true,
         false, true, true,
         true, true, true,
         true, false, false };  
    }    

}