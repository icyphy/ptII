/* Use Reflection to get the AST

Copyright (c) 2000  The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;
import java.lang.reflect.*;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ASTReflect.
/*
 */
public class ASTReflect {

    /** Return the AST of a class */
    public static CompileUnitNode ASTClass(Class myClass) {
	ASTPackage(myClass);
	ASTConstructors(myClass);
	ASTFields(myClass);
	//ASTMethods(myClass);
	ASTInnerClasses(myClass);

	if (_debug) {
	    System.out.println("}");
	}
	return (CompileUnitNode) null;
    }

    public static void ASTConstructors(Class myClass) {
	if (_debug) {
	    System.out.println(_indent + "// Constructors");
	    Constructor constructors[] = myClass.getConstructors();
	    for(int i=0; i<constructors.length; i++) {
		System.out.println(_indent +
				   constructors[i].toString() + "{}");
	    }
	}
    }

    public static void ASTFields(Class myClass) {
	if (_debug) {
	    System.out.println(_indent + "// Fields");
	    Field fields[] = myClass.getFields();
	    for(int i=0; i<fields.length; i++) {
		System.out.println(_indent + fields[i].toString() + ";");
	    }
	}
    }

    public static void ASTInnerClasses(Class myClass) {
	if (_debug) {
	    // Handle inner classes
	    Class classes[] = myClass.getClasses();
	    if (classes.length > 0 ) {
		System.out.println(_indent + "// Inner classes");
	    }
	    for(int i=0; i<classes.length; i++) {
		// Dealing with the _indent would be cool
		ASTClass(classes[i]);
	    }
	}
    }

    public static void ASTMethods(Class myClass, List memberList) {
	if (_debug)
	    System.out.println(_indent + "// Methods");
	Method methods[] = myClass.getMethods();
	Method method = null;
	for(int i=0; i<methods.length; i++) {
	    method = methods[i];
	    if (_debug)
		System.out.println(_indent + method + "{}");

	    // FIXME, we need to map java.lang.reflect.Modifier to 
	    // ptolemy.java.lang.Modifier.
	    int modifiers = Modifier.PUBLIC_MOD;

	    NameNode methodName =
		new NameNode(AbsentTreeNode.instance, method.getName());

	    // FIXME: call method.getParameterTypes and convert it to a list.
	    LinkedList params = new LinkedList();

	    // FIXME: call method.getExceptionTypes and convert it to a list.
	    LinkedList throwsList = new LinkedList();

	    TypeNameNode returnType =
		new TypeNameNode(
				 new NameNode(AbsentTreeNode.instance,
					      method.getReturnType().getName()));

	    MethodDeclNode methodDeclNode =
		new MethodDeclNode(modifiers,
				   methodName,
				   params,
				   throwsList,
				   AbsentTreeNode.instance /*body*/,
				   returnType);

	    memberList.add(methodDeclNode);
	}
    }

    /** Return an AST of the package and class information */
    public static void ASTPackage(Class myClass) {
	if (_debug) {
	    System.out.println("// " + myClass.toString());
	    System.out.println("package " +
			       myClass.getPackage().getName() + ";");
	
	    System.out.println(Modifier.toString(myClass.getModifiers()) +
			       " " + myClass.toString());
	    String superClass = myClass.getSuperclass().getName();
	    if (superClass.length() > 0 ) { 
		System.out.println("extends " + superClass);
	    }

	    Class interfaces[] = myClass.getInterfaces();
	    for(int i=0; i<interfaces.length; i++) {
		System.out.println("implements " + interfaces[i].toString());
	    }
	    System.out.println("{");
	}


	// FIXME, we need to map java.lang.reflect.Modifier to 
	// ptolemy.java.lang.Modifier.
	int modifiers = Modifier.PUBLIC_MOD;

	//NameNode className = 
	//    (NameNode) makeNameNode(myClass.getName());

	// Get the classname, and strip off the package. 
	String fullClassName = myClass.getName();
	NameNode className =
	    new NameNode(AbsentTreeNode.instance,
			 fullClassName.substring(1 + 
						 fullClassName.lastIndexOf('.')));

	// FIXME: convert from getInterfaces to list
	LinkedList interfaceList = new LinkedList();

	LinkedList memberList = new LinkedList();

	ASTMethods(myClass, memberList);

	TreeNode superClass =
	    (TreeNode) makeNameNode(myClass.getSuperclass().getName());

        //TreeNode superClass = new NameNode(AbsentTreeNode.instance,
	//				   myClass.getSuperclass().getName());

	ClassDeclNode classDeclNode =
	    new ClassDeclNode(modifiers,
			      className,
			      interfaceList,
			      memberList,
			      superClass);


	//NameNode packageName = new NameNode(AbsentTreeNode.instance,
	//				    myClass.getPackage().getName());

	NameNode packageName = 
	    (NameNode) makeNameNode(myClass.getPackage().getName());

	CompileUnitNode compileUnitNode =
			      new CompileUnitNode(packageName,
						  /*imports*/ new LinkedList(),
						  TNLManip.cons(classDeclNode));
	System.out.println("ast: " + compileUnitNode);
			      
    }

    public static void main(String[] args) {
	try {
	    ASTClass(Class.forName("ptolemy.lang.java.Skeleton"));
	} catch (Exception e) {
	    System.err.println("Error: " + e);
	}
    }

    // String to indent printed output with.
    private final static String _indent = new String("    ");

    private final static boolean _debug = true;

    // FIXME: This is copied from StaticResolution.java
    public static final TreeNode makeNameNode(String qualifiedName) {
        TreeNode retval = AbsentTreeNode.instance;

        int firstDotPosition;

        do {
            firstDotPosition = qualifiedName.indexOf('.');

            if (firstDotPosition > 0) {
                String ident = qualifiedName.substring(0, firstDotPosition);


                retval = new NameNode(retval, ident);

                qualifiedName = qualifiedName.substring(firstDotPosition + 1,
                        qualifiedName.length());

            } else {
                if (qualifiedName.length() > 0) {
                    return new NameNode(retval, qualifiedName);
                }
            }
        } while (firstDotPosition > 0);

        return retval;
    }
}




