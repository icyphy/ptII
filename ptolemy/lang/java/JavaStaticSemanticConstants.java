/*
Constants associated with Java static semantic analysis.

Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.lang.java;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.NodeClassID;

//////////////////////////////////////////////////////////////////////////
//// JavaStaticSemanticConstants
/** Constants associated with Java static semantic analysis.
 *
 *  @author Jeff Tsay
 */
public interface JavaStaticSemanticConstants extends NodeClassID {

    // modifiers

    /** No modifier. */
    public static final int NO_MOD           = 0;

    /** The 'public' modifier. */
    public static final int PUBLIC_MOD       = 0x1;

    /** The 'protected' modifier. */
    public static final int PROTECTED_MOD    = 0x2;

    /** The 'private' modifier. */
    public static final int PRIVATE_MOD      = 0x4;

    /** The 'abstract' modifier. */
    public static final int ABSTRACT_MOD     = 0x8;

    /** The 'final' modifier. */
    public static final int FINAL_MOD        = 0x10;

    /** The 'native' modifier. */
    public static final int NATIVE_MOD       = 0x20;

    /** The 'synchronized' modifier. */
    public static final int SYNCHRONIZED_MOD = 0x40;

    /** The 'transient' modifier. */
    public static final int TRANSIENT_MOD    = 0x80;

    /** The 'volatile' modifier. */
    public static final int VOLATILE_MOD     = 0x100;

    /** The 'static' modifier. */
    public static final int STATIC_MOD       = 0x200;

    /** The 'strictfp' modifier. */
    public static final int STRICTFP_MOD     = 0x400;

    // types of JavaDecls

    /** Type ClassDecl representing a class. */
    public static final int CG_CLASS = 0x1;

    /** Type ClassDecl representing an interface. */
    public static final int CG_INTERFACE = 0x2;

    /** Type FieldDecl */
    public static final int CG_FIELD = 0x4;

    /** Type MethodDecl representing a method. */
    public static final int CG_METHOD = 0x8;

    /** Type MethodDecl representing a constructor. */
    public static final int CG_CONSTRUCTOR = 0x10;

    /** Type LocalVarDecl. */
    public static final int CG_LOCALVAR = 0x20;

    /** Type FormalParameterDecl. */
    public static final int CG_FORMAL = 0x40;

    /** Type PackageDecl. */
    public static final int CG_PACKAGE = 0x80;

    /** Type StmtLblDecl. */
    public static final int CG_STMTLABEL = 0x100;

    /** A constant used to search for either a class or interface. */
    public static final int CG_USERTYPE = CG_CLASS | CG_INTERFACE;

    /** A constant used to search for either a method or constructor. */
    public static final int CG_INVOKABLE = CG_METHOD | CG_CONSTRUCTOR;

    // keys for property map

    /** The key that retrieves the canonical filename of the parsed Java file,
     *  set in a CompileUnitNode.
     */
    public static final Integer IDENT_KEY = new Integer(0);

    /** The key that retreives the PackageDecl that the compilation unit
     *  belongs to, set in CompileUnitNode.
     */
    public static final Integer PACKAGE_KEY = new Integer(1);

    /** The key that retreives the List of packages that a compile unit imports, set
     *  in a CompileUnitNode.
     */
    public static final Integer IMPORTED_PACKAGES_KEY = new Integer(2);

    /** The key that retrieves the JavaDecl associated with a TreeNode. */
    public static final Integer DECL_KEY = new Integer(4);

    /** The key that retrieves the Environ associated with a TreeNode. */
    public static final Integer ENVIRON_KEY = new Integer(5);

    /** The key that retrieves the TypeNameNode for the class associated with a ThisNode. */
    public static final Integer THIS_CLASS_KEY = new Integer(6);

    /** The key that retrieves the ClassDecl associated with the superclass,
     *  set in ClassDeclNode.
     */
    public static final Integer SUPERCLASS_KEY = new Integer(7);

    /** The key that retrieves the InterfaceDecl associated with the interfaces
     *  that a anonymous class implements, set in AllocateAnonymousClassNode.
     *  The value retrieved may be NullValue.instance.
     */
    public static final Integer INTERFACE_KEY = new Integer(8);

    /** The key that retrieves the MethodDecl associated with the constructor that
     *  is invoked in the creation of an anonymous class, set in
     *  AllocateAnonymousClassNode.
     */
    public static final Integer CONSTRUCTOR_KEY = new Integer(9);

    /** The key that retrieves the StmtDecl that is the destination of a jump,
     *  set in nodes that jump.
     */
    public static final Integer JUMP_DESTINATION_KEY = new Integer(10);

    /** The key that retrieves the resolved type of the ExprNode. */
    public static final Integer TYPE_KEY = new Integer(16);

    /** The number of properties reserved for static resolution of Java.
     *  This number can be used to start numbering extended properties.
     */
    public static final int RESERVED_JAVA_PROPERTIES = TYPE_KEY.intValue() + 1;
}
