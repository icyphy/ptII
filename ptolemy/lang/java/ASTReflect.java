/* Use Reflection to get the AST

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// ASTReflect.
/** Create an AST for a class by using reflection.
@Version: $Id$
@Author: Christopher Hylands
 */
public class ASTReflect {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an AST that contains a class declaration. */
    public static ClassDeclNode ASTClassDeclNode(Class myClass) {
	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());

	// Get the classname, and strip off the package. 
	String fullClassName = myClass.getName();
	NameNode className =
	    new NameNode(AbsentTreeNode.instance,
			 fullClassName.substring(1 + 
						 fullClassName.lastIndexOf('.')));

	// Unfortunately, we can't use Arrays.asList() here since
	// what getParameterTypes returns of type Class[], and
	// what we want is a list of InterfaceDeclNodes
	List interfaceList = new LinkedList();
	Class interfaceClasses[] = myClass.getInterfaces();
	for(int i = 0; i < interfaceClasses.length; i++) {
	    int interfaceModifiers =
		Modifier.convertModifiers(interfaceClasses[i].getModifiers());

	    //NameNode interfaceName = 
	    //	(NameNode) _makeNameNode(interfaceClasses[i].getName());

	    String fullInterfaceName = interfaceClasses[i].getName();
	    // FIXME: We should probably use the fully qualified name here?
	    TypeNode interfaceDeclNode =
		    new TypeNameNode(new NameNode(AbsentTreeNode.instance,
				 fullInterfaceName.substring(1 + 
							 fullInterfaceName.lastIndexOf('.')
							 )));
	    /* FIXME: The output of Skelton does not seem to use
	     * InterfaceDeclNode?
	     */
	    //InterfaceDeclNode interfaceDeclNode =
	    //	new InterfaceDeclNode(modifiers,
	    //			      interfaceName,
	    //			      /* interfaces */ new LinkedList(),
	    //			      /* members */ new LinkedList());

	    interfaceList.add(interfaceDeclNode);
	}

	LinkedList memberList = new LinkedList();

	// Get the AST for all the constructor.
	memberList.addAll(constructorsASTList(myClass));

	// Get the AST for all the methods.
	memberList.addAll(methodsASTList(myClass));

	// Get the AST for all the fields.
	memberList.addAll(fieldsASTList(myClass));

	// Get the AST for all the inner classes.
	//memberList.addAll(innerClassesASTList(myClass));

	// FIXME: If this is java.lang.Object, should we use AbsentTreeNode?
	TreeNode superClass =
	    (TreeNode) _makeNameNode(myClass.getSuperclass().getName());

	ClassDeclNode classDeclNode =  
	    new ClassDeclNode(modifiers,
			      className,
			      interfaceList,
			      memberList,
			      superClass);

	return classDeclNode;
    }


    /** Return a CompileUnitNode AST that contains the class, methods, fields
     *	constructors and inner classes.  This node includes information
     *  about the package.
     */  
    public static CompileUnitNode ASTCompileUnitNode(Class myClass) {
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
	    for(int i = 0; i < interfaces.length; i++) {
		System.out.println("implements " + interfaces[i].toString());
	    }
	    System.out.println("{");
	}

	ClassDeclNode classDeclNode = ASTClassDeclNode(myClass);
	NameNode packageName = 
	    (NameNode) _makeNameNode(myClass.getPackage().getName());

	// FIXME: we are not trying to generate a list of imports here
	// we could look at the return values and args and import
	// anything outside of the package.
	CompileUnitNode compileUnitNode =
			      new CompileUnitNode(packageName,
						  /*imports*/ new LinkedList(),
						  TNLManip.cons(classDeclNode));
	if (_debug) {
	    System.out.println("}");
	}
	return compileUnitNode;

    }

    /** Return a list that contains an AST for the constructors. */
    public static List constructorsASTList(Class myClass){
	List constructorList = new LinkedList(); 
        if (_debug) {
	    System.out.println(_indent + "// Constructors");
	}
	Constructor constructors[] = myClass.getDeclaredConstructors();
	Constructor constructor = null;
	for(int i = 0; i < constructors.length; i++) {
	    constructor = constructors[i];
	    if (_debug) {
		System.out.println(_indent +
				   constructor.getName() + "{}");
	    }
	    int modifiers =
		    Modifier.convertModifiers(constructor.getModifiers());

	    String fullConstructorName = constructor.getName();
	    NameNode constructorName =
		new NameNode(AbsentTreeNode.instance,
			     fullConstructorName.substring(1 + 
						     fullConstructorName.lastIndexOf('.')));

	    List paramList = _paramList(constructor.getParameterTypes());

	    // FIXME: call method.getExceptionTypes and convert it to a list.
	    List throwsList = new LinkedList();

	    ConstructorDeclNode constructorDeclNode = 
		new ConstructorDeclNode(modifiers,
					constructorName,
					paramList,
					throwsList,
					/* body */
					new BlockNode(new LinkedList()),
					/* constructor call*/
					new SuperConstructorCallNode(new LinkedList())
					);
	    constructorList.add(constructorDeclNode);
	}
	return constructorList;
    }

    /** Return a List containing an AST that describes the fields
     *	for myclass.
     */
    public static List fieldsASTList(Class myClass) {
	List fieldList = new LinkedList(); 
	if (_debug) {
	    System.out.println(_indent + "// Fields");
	}
	Field fields[] = myClass.getDeclaredFields();
	for(int i = 0; i < fields.length; i++) {
	    if (_debug) {
		System.out.println(_indent + fields[i].toString() + ";");
	    }
	    int modifiers =
		    Modifier.convertModifiers(fields[i].getModifiers());
	    TypeNode defType = _definedType(fields[i].getType());
	    String fullFieldName = fields[i].toString();
	    NameNode fieldName =
		new NameNode(AbsentTreeNode.instance,
			     fullFieldName.substring(1 + 
						     fullFieldName.lastIndexOf('.')));

	    FieldDeclNode  fieldDeclNode =
		new FieldDeclNode(modifiers,
				  defType,
				  fieldName,
				  AbsentTreeNode.instance/* initExpr */);

    	    fieldList.add(fieldDeclNode);
	}
	return fieldList;
    }

    /** Return a List containing an AST that describes the inner classes
     *	for myclass.
     */
    public static List innerClassesASTList(Class myClass) {
	List innerClassList = new LinkedList(); 
	// Handle inner classes
	Class classes[] = myClass.getClasses();
	if (classes.length > 0 && _debug) {
	    System.out.println(_indent + "// Inner classes");
	}
	for(int i = 0; i < classes.length; i++) {
	    innerClassList.add(ASTClassDeclNode(classes[i]));
	}
    	return innerClassList;
    }

    /** Return a List containing an AST that describes the methods
     *	for myclass.
     */
    public static List methodsASTList(Class myClass) {
	List methodList = new LinkedList(); 
	if (_debug)
	    System.out.println(_indent + "// Methods");
	Method methods[] = myClass.getDeclaredMethods();
	Method method = null;
	for(int i = 0; i < methods.length; i++) {
	    method = methods[i];
	    if (! myClass.equals(method.getDeclaringClass())) {
		// This method was declared in a parent class,
		// so we skip it
		continue;
	    } 
	    if (_debug)
		System.out.println(_indent + method + "{}");

	    // FIXME, we need to map java.lang.reflect.Modifier to 
	    // ptolemy.java.lang.Modifier.
	    int modifiers =
		Modifier.convertModifiers(method.getModifiers());

	    NameNode methodName =
		new NameNode(AbsentTreeNode.instance, method.getName());

	    List paramList = _paramList(method.getParameterTypes());

	    // FIXME: call method.getExceptionTypes and convert it to a list.
	    List throwsList = new LinkedList();

	    TypeNode returnType = _definedType(method.getReturnType());

	    MethodDeclNode methodDeclNode =
		new MethodDeclNode(modifiers,
				   methodName,
				   paramList,
				   throwsList,
				   AbsentTreeNode.instance /*body*/,
				   returnType);

	    methodList.add(methodDeclNode);
	}
	return methodList;
    }


    /** Print the AST for ptolemy.lang.java.Skeleton for testing purposes. */
    public static void main(String[] args) {
	try {
	    System.out.println("ast: " +
			       ASTCompileUnitNode(Class.forName("ptolemy.lang.java.test.ReflectTest")));
	    //System.out.println(ASTCompileUnitNode(Class.forName("ptolemy.lang.java.Skeleton")));
	} catch (Exception e) {
	    System.err.println("Error: " + e);
	    e.printStackTrace();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return a TypeNode containing the type of a class.
    // _definedType is used for method return types, method parameters
    // and fields.
    private final static TypeNode _definedType(Class myClass) {
	TypeNode defType = null;
	String fullClassName = null;
	if (myClass.isArray()) {
	    // Handle arrays.  
	    Class componentClass =
		myClass.getComponentType();
	    TypeNode baseType = null;
	    if (componentClass.isArray()) {
		// Arrays of Arrays
		baseType = _definedType(componentClass);
	    } else {
		if (componentClass.isPrimitive()) { 
		    baseType = _primitiveTypeNode(componentClass);
		} else {
		    fullClassName = componentClass.getName();
		    NameNode className =
			new NameNode(AbsentTreeNode.instance,
				     fullClassName.substring(1 + 
							     fullClassName.lastIndexOf('.')));
		    baseType = new TypeNameNode(className);
		}
	    }
	    defType =
		new ArrayTypeNode(baseType);
	} else {
	    if (myClass.isPrimitive()) { 
		return _primitiveTypeNode(myClass);
	    } else {
		fullClassName = myClass.getName();
		defType =
		    new TypeNameNode(new NameNode(AbsentTreeNode.instance,
				 fullClassName.substring(1 + 
							 fullClassName.lastIndexOf('.')
							 )));
	    }

	}
	return defType;
    }

    // FIXME: This is copied from StaticResolution.java because
    // we don't want to cause StaticResolution to start reading in
    // all the java.lang packages.
    // Create a TreeNode that contains the qualifiedName split
    // into separate nodes.
    private static final TreeNode _makeNameNode(String qualifiedName) {
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

    // Return a List of parameters
    private static List _paramList(Class [] parameterClasses) {
	// Unfortunately, we can't use Arrays.asList() here since
	// what getParameterTypes returns of type Class[], and
	// what we want is a list of ParameterNodes.

	List paramList = new LinkedList();

	for(int i = 0; i < parameterClasses.length; i++) {
	    // We could put all of this in the body of the paramList.add
	    // call, but we don't for readability reasons.
	    int modifier =
		Modifier.convertModifiers(parameterClasses[i].getModifiers());
	    TypeNode defType = _definedType(parameterClasses[i]);

	    // The name of the parameter is not available via reflection.
	    NameNode name = new NameNode(AbsentTreeNode.instance,"");
	    paramList.add(new ParameterNode(modifier, defType, name));
	}
	return paramList;
    }

    private static TypeNode _primitiveTypeNode(Class myClass) {
	TypeNode defType = null;
	// FIXME: I'll bet we could reorder these for better
	// performance
	if (!myClass.isPrimitive()) { 
	    throw new RuntimeException("Error: " + myClass +
				   " is not a primitive type like int");
	}
	if (myClass.equals(Boolean.TYPE)) {
	    defType = BoolTypeNode.instance;
	} else if (myClass.equals(Character.TYPE)) {
	    defType = CharTypeNode.instance;
	} else if (myClass.equals(Byte.TYPE)) {
	    defType = ByteTypeNode.instance;
	} else if (myClass.equals(Short.TYPE)) {
	    defType = ShortTypeNode.instance;
	} else if (myClass.equals(Integer.TYPE)) {
	    defType = IntTypeNode.instance;
	} else if (myClass.equals(Long.TYPE)) {
	    defType = LongTypeNode.instance;
	} else if (myClass.equals(Float.TYPE)) {
	    defType = FloatTypeNode.instance;
	} else if (myClass.equals(Double.TYPE)) {
	    defType = DoubleTypeNode.instance;
	} else if (myClass.equals(Void.TYPE)) {
	    defType = VoidTypeNode.instance;
	}
	return defType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // String to indent printed output with.
    private final static String _indent = new String("    ");

    // Set to true to turn on debugging messages
    private final static boolean _debug = false;
}




