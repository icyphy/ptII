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
import java.io.File;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// ASTReflect.
/** Create an AST for a class by using reflection.
@version $Id$
@author Christopher Hylands
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
	// FIXME: should we use the full classname with package here?
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

	    NameNode interfaceName =
	    	(NameNode) _makeNameNode(interfaceClasses[i].getName());

	    TypeNode interfaceDeclNode =
                new TypeNameNode(interfaceName);

	    //String fullInterfaceName = interfaceClasses[i].getName();
	    // FIXME: We should probably use the fully qualified name here?
	    //TypeNode interfaceDeclNode =
            //    new TypeNameNode(new NameNode(AbsentTreeNode.instance,
            //            fullInterfaceName.substring(1 +
            //                    fullInterfaceName.lastIndexOf('.')
            //                                        )));

	    //TypeNode interfaceDeclNode =
            //    new TypeNameNode(new NameNode(AbsentTreeNode.instance,
            //           fullInterfaceName));

	    
	    // FIXME: The output of Skelton does not seem to use
	    // InterfaceDeclNode?
	    //
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
	memberList.addAll(innerClassesASTList(myClass));

	TypeNameNode superClass = null;
        if (myClass.getSuperclass() == null ) {
            // JDK1.2.2 getSuperclass can return null
            // FIXME: should this be java.lang.Object?
            superClass = new TypeNameNode((NameNode)_makeNameNode("Object"));
        } else {
            superClass =
		new TypeNameNode((NameNode)_makeNameNode(myClass.getSuperclass().getName()));
        }

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

        NameNode packageName = null;
        if (myClass.getPackage() == null ) {
            // JDK1.2.2 getPackage returns null
	    packageName = new NameNode(AbsentTreeNode.instance, "");
        } else {
            packageName =
                (NameNode) _makeNameNode(myClass.getPackage().getName());
        }

	// FIXME: we are not trying to generate a list of imports here
	// we could look at the return values and args and import
	// anything outside of the package.

	CompileUnitNode compileUnitNode = null;
	if (myClass.isInterface()) {
	    InterfaceDeclNode interfaceDeclNode =
		ASTInterfaceDeclNode(myClass);
	    compileUnitNode =
		new CompileUnitNode(packageName,
				    /*imports*/ new LinkedList(),
				    TNLManip.cons(interfaceDeclNode));
	} else {
	    ClassDeclNode classDeclNode =
		ASTClassDeclNode(myClass);
	    compileUnitNode =
		new CompileUnitNode(packageName,
				    /*imports*/ new LinkedList(),
				    TNLManip.cons(classDeclNode));
	}
	return compileUnitNode;
    }

    /** Return a list that contains an AST for the constructors. */
    public static List constructorsASTList(Class myClass) {
	List constructorList = new LinkedList();
	Constructor constructors[] = myClass.getDeclaredConstructors();
	Constructor constructor = null;
	for(int i = 0; i < constructors.length; i++) {
	    constructor = constructors[i];
	    int modifiers =
                Modifier.convertModifiers(constructor.getModifiers());
	    String fullConstructorName = constructor.getName();
	    // FIXME: should we use the full name with package here?
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
	Field fields[] = myClass.getDeclaredFields();
	for(int i = 0; i < fields.length; i++) {
	    int modifiers =
                Modifier.convertModifiers(fields[i].getModifiers());
	    TypeNode defType = _definedType(fields[i].getType());
	    String fullFieldName = fields[i].toString();
	    // FIXME: should we use the full name with package here?

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
	Class classes[] = myClass.getDeclaredClasses();
	for(int i = 0; i < classes.length; i++) {
	    innerClassList.add(ASTClassDeclNode(classes[i]));
	}
    	return innerClassList;
    }

    /** Return an AST that contains an interface declaration. */
    public static InterfaceDeclNode ASTInterfaceDeclNode(Class myClass) {
	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());

	// Get the classname, and strip off the package.
	String fullClassName = myClass.getName();
	// FIXME: should we use the full classname with package here?
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

	    NameNode interfaceName =
	    	(NameNode) _makeNameNode(interfaceClasses[i].getName());

	    TypeNode interfaceDeclNode =
                new TypeNameNode(interfaceName);

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
	memberList.addAll(innerClassesASTList(myClass));

	InterfaceDeclNode interfaceDeclNode =
	    new InterfaceDeclNode(modifiers,
				  className,
				  interfaceList,
				  memberList);
	return interfaceDeclNode;
    }

    /** Given a pathname, try to find a class that corresponds with it
     *  by starting with the filename and adding directories from the
     *  pathname until we find a class or run through all the directories.
     *  If a class is not found, return null.
     */

    public static Class lookupClass(String name) {
        try {
            // The classname was something like java.lang.Object
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            // The classname was something like Object, so
            // we search the loaded packages.
            // FIXME: we could try optimizing this so we
            // look in java.lang first, which is where
            // vast majority of things will be found.
            Package packages[] = Package.getPackages();
            for(int i = 0; i < packages.length; i++) {
                String qualifiedName =
                    new String(packages[i].getName() + "." + name);
                try {
                    return Class.forName(qualifiedName);
                } catch (ClassNotFoundException ee) {
                    // Keep searching the packages.
                }
	    } 
	    // FIXME: We need to do this part
	    throw new RuntimeException("ASTReflect.lookupClass(): " +
				       "Could not find class '" + name + 
				       "'. The package of this class has " +
				       "not yet been loaded, so we need to " +
				       "look in the searchPath");
	}
    }

    /** Lookup a class by name, return the ClassDeclNode
     */
    public static ClassDeclNode lookupClassDeclNode(String name) {
            return ASTClassDeclNode(lookupClass(name));
    }

    /** Return a List containing an AST that describes the methods
     *	for myclass.
     */
    public static List methodsASTList(Class myClass) {
	List methodList = new LinkedList();
	Method methods[] = myClass.getDeclaredMethods();
	Method method = null;
	for(int i = 0; i < methods.length; i++) {
	    method = methods[i];
	    if (! myClass.equals(method.getDeclaringClass())) {
		// This method was declared in a parent class,
		// so we skip it
		continue;
	    }
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


    /** Given a pathname, try to find a class that corresponds with it
     *  by starting with the filename and adding directories from the
     *  pathname until we find a class or run through all the directories.
     *  If a class is not found, return null.
     */
    public static Class pathnameToClass(String pathname) {
	Class myClass = null;
	// Try to find a class by starting with the file name
	// and then adding directories as we go along
	
	StringBuffer classname = null;
	if(pathname.lastIndexOf('.') != -1) {
	    // Strip out anything after the last '.', such as .java
	    classname =
		new StringBuffer(StringManip.baseFilename(StringManip.partBeforeLast(pathname, '.')));
	} else {
	    classname =	new StringBuffer(StringManip.baseFilename(pathname));
	}

	// restOfPath contains the directories that we add one by one.
	String restOfPath = pathname;

	while (true) {
	    try {
		myClass = Class.forName(new String(classname));
	    } catch (Exception e) {}

	    if (myClass != null) {
		// We found our class!
		return myClass;
	    }

	    if (restOfPath.lastIndexOf(File.separatorChar) == -1) { 
		// We are out of directories to try.
		return null;
	    }
	    // First time through, we pull off the file name, after that
	    // we pull off directories.
	    restOfPath = StringManip.partBeforeLast(restOfPath,
						    File.separatorChar);
	    classname.insert(0, StringManip.baseFilename(restOfPath) + ".");
	}
	
    }

    /** Print the AST of the command line argument for testing purposes. */
    public static void main(String[] args) {
	try {
	    System.out.println("ast: " +
                    ASTCompileUnitNode(lookupClass(args[0])));
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
			(NameNode) _makeNameNode(fullClassName);
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
		NameNode className =
		    (NameNode) _makeNameNode(fullClassName);
		defType = new TypeNameNode(className);
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

	//System.out.println("ASTReflect._makeNameNode("+ qualifiedName + ")");
        do {
            firstDotPosition = qualifiedName.indexOf('.');

	    // FIXME: should we not further qualify things in java.lang
	    // since they will be found anyway?
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
}
