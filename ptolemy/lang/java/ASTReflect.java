/* Use Reflection to get the AST

Copyright (c) 2000-2001 The Regents of the University of California.
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

import ptolemy.lang.StringManip;
import ptolemy.lang.TNLManip;
import ptolemy.lang.TreeNode;

import ptolemy.lang.java.nodetypes.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// ASTReflect.
/** Create an Abstract Syntax Tree (AST) for a class by using reflection.

These methods operate on Java code that has been compiled and is
available at runtime for inspection by using the Java reflection
capability.

In the Ptolemy II code generator, three modes of loading AST classes
supported. In "full" mode, the entire AST is loaded; in "deep" mode, 
method and constructor bodies are excluded; and in "shallow" mode,
field, method, and constructor declarations (members) are excluded 
entirely. The ASTReflect class supports shallow and deep loading,
and demand-driven conversion from shallowly-loaded ASTs to deeply-loaded
ones.

@version $Id$
@author Christopher Hylands and Shuvra S. Bhattacharyya 
 */
public final class ASTReflect {

    // private constructor prevent instantiation of this class
    private ASTReflect() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Use reflection to set the members and interfaces subtrees of the 
     *  AST asslociated with
     *  a class or interface declaration. 
     *  This ensures so-called "deep loading" of a class's AST
     *  for a class that was originally loaded shallowly. 
     *  @param declNode The shallowly-loaded AST for the class or interface whose members 
     *  we are adding by reflection.
     */
    public static void ASTAugmentWithMembers(UserTypeDeclNode declNode) {
        Class myClass = ASTReflect.lookupClass(
                ASTReflect.getFullyQualifiedName(declNode));
    
        if (StaticResolution.traceLoading)
            System.out.println("Starting shallow-->deep conversion on " + 
                    myClass.getName()); 
    
        LinkedList memberList = new LinkedList();

        // Get the AST for all the constructors.
        memberList.addAll(constructorsASTList(myClass));
    
        // Get the AST for all the methods.
        memberList.addAll(methodsASTList(myClass));
    
        // Get the AST for all the fields.
        memberList.addAll(fieldsASTList(myClass));

        // Get the AST for all the inner classes or inner interfaces
        memberList.addAll(innerClassesOrInterfacesASTList(myClass));    

        // Fill in the members list of the given class declaration
        declNode.setMembers(memberList);

        // Similarly, fill in the interface list	
        List interfaceList = _typeNodeList(myClass.getInterfaces());
        declNode.setInterfaces(interfaceList);
    }
 
    /** Use reflection to generate the ClassDeclNode of a class.
     *  @param myClass The class to be analyzed.
     *  @return The ClassDeclNode of that class.
     */
    public static ClassDeclNode ASTClassDeclNode(Class myClass) {

    if (StaticResolution.traceLoading)
        System.out.println("Starting deep loading of " + myClass.getName()); 

	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());
	NameNode className = (NameNode) _makeNameNode(myClass.getName());
	List interfaceList = _typeNodeList(myClass.getInterfaces());

	LinkedList memberList = new LinkedList();

	// Get the AST for all the constructors.
	memberList.addAll(constructorsASTList(myClass));

	// Get the AST for all the methods.
	memberList.addAll(methodsASTList(myClass));

	// Get the AST for all the fields.
	memberList.addAll(fieldsASTList(myClass));

	// Get the AST for all the inner classes or inner interfaces
	memberList.addAll(innerClassesOrInterfacesASTList(myClass));

	TreeNode superClass = null;
        if (myClass.getSuperclass() == null ) {
            // "If this Class represents either the Object class, an
            // interface, a primitive type, or void, then null is
            // returned"
            superClass = AbsentTreeNode.instance;
        } else {
            superClass =
		new TypeNameNode((NameNode)_makeNameNode(
                        myClass.getSuperclass().getName()));
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
     *  @param myClass The class to be analyzed.
     *  @return The CompileUnitNode of that class.
     */
    public static CompileUnitNode ASTCompileUnitNode(Class myClass) {

        NameNode packageName = null;
        if (myClass.getPackage() == null ) {
            // JDK1.2.2 getPackage returns null, but JDK1.3 does not?
            packageName =
                (NameNode) _makeNameNode(StringManip.partBeforeLast(
                        myClass.getName(), '.'));

        } else {
            packageName =
                (NameNode) _makeNameNode(myClass.getPackage().getName());
        }

	// FIXME: we are not trying to generate a list of imports here.
	// We could look at the return values and args and import
	// anything outside of the package and java.lang.

	CompileUnitNode compileUnitNode = null;
	if (myClass.isInterface()) {
	    InterfaceDeclNode interfaceDeclNode = null;
        if (StaticResolution.shallowLoadingEnabled()) 
		    interfaceDeclNode = ASTShallowInterfaceDeclNode(myClass);
        else interfaceDeclNode = ASTInterfaceDeclNode(myClass);
	    compileUnitNode =
		new CompileUnitNode(packageName, /*imports*/ new LinkedList(),
                TNLManip.addFirst(interfaceDeclNode));
	} else {
        ClassDeclNode classDeclNode = null;
        if (StaticResolution.shallowLoadingEnabled())
	        classDeclNode = ASTShallowClassDeclNode(myClass);
	    else classDeclNode = ASTClassDeclNode(myClass);

	    compileUnitNode = new CompileUnitNode(packageName,
                /*imports*/ new LinkedList(), TNLManip.addFirst(classDeclNode));
	}
	return compileUnitNode;
    }


    /** Generate the a shallow version of the declaration for a class.
     *  In particular, fill in only the super class subtree. 
     *  This is to facilitate demand-driven loading of AST subtrees.
     *  @param myClass The class to be analyzed.
     *  @return The "shallow" class declaration AST of that class.
     */
    public static ClassDeclNode ASTShallowClassDeclNode(Class myClass) {
    if (StaticResolution.traceLoading) 
         System.out.println("Starting shallow loading of class '" + 
                 myClass.getName() + "'"); 
    
	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());
	NameNode className = (NameNode) _makeNameNode(myClass.getName());

	LinkedList memberList = new LinkedList();

	TreeNode superClass = null;
        if (myClass.getSuperclass() == null ) {
            // "If this Class represents either the Object class, an
            // interface, a primitive type, or void, then null is
            // returned"
            superClass = AbsentTreeNode.instance;
        } else {
            superClass =
		new TypeNameNode((NameNode)_makeNameNode(
                        myClass.getSuperclass().getName()));
        }

	ClassDeclNode classDeclNode =
	    new ClassDeclNode(modifiers,
                    className,
                    null,
                    null,
                    superClass);

	return classDeclNode;
    }


    /** Generate the a shallow version of the declaration for an interface. 
     *  In particular, fill in only the super class subtree. 
     *  This is to facilitate demand-driven loading of AST subtrees.
     *  @param myClass The interface to be analyzed.
     *  @return The "shallow" interface declaration AST of that interface.
     */
    public static InterfaceDeclNode ASTShallowInterfaceDeclNode(Class myClass) {
    if (StaticResolution.traceLoading) 
         System.out.println("Starting shallow loading of interface '" + 
                 myClass.getName() + "'"); 
    
	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());
	NameNode className = (NameNode) _makeNameNode(myClass.getName());
	InterfaceDeclNode interfaceDeclNode =
	    new InterfaceDeclNode(modifiers,
                    className,
                    null,
                    null);

	return interfaceDeclNode;
    }

    /** Return a list of constructors where each element contains
     *  a ConstructorDeclNode for that constructor.
     *  @param myClass The class to be analyzed.
     *  @return The List of constructorDeclNodes for the class.
     */
    public static List constructorsASTList(Class myClass) {
	List constructorList = new LinkedList();
	Constructor constructors[] = myClass.getDeclaredConstructors();
	Constructor constructor = null;
	for(int i = 0; i < constructors.length; i++) {
	    int modifiers =
                Modifier.convertModifiers(constructors[i].getModifiers());
	    NameNode constructorName =
                (NameNode) _makeNameNode(constructors[i].getName());
	    List paramList = _paramList(constructors[i].getParameterTypes());
	    List throwsList =
                _typeNodeList(constructors[i].getExceptionTypes());

        // Note that because we are using reflection, we don't get
        // the bodies of the constructors. Instead, we attach a null
        // object as the body. Note that this is not symmetric with how
        // we handle omitted bodies when constructing method declaration ASTs
        // via reflection. This is because of a corresponding asymmetry in
        // how the ConstructorDeclNode constructor is defined (it requires
        // a BlockNode as the body). However, a block node with an empty
        // statement list is not equivalent to a block node that is omitted,
        // so we use a null object as the body here. FIXME: elminate this
        // asymmetry in the constructor definition (with respect to the
        // constructor for MethodDeclNode).
	    ConstructorDeclNode constructorDeclNode =
		new ConstructorDeclNode(modifiers,
                        constructorName,
                        paramList,
                        throwsList,
                        null  /* body */,
                        /* constructor call */
                        new SuperConstructorCallNode(new LinkedList())
					);
	    constructorList.add(constructorDeclNode);
	}
	return constructorList;
    }

    /** Given an AST node for a class or interface declaration, convert the loaded
     *  AST to its deep version if necessary (i.e., if it is loaded
     *  in shallow mode), and perform all necessary resolution 
     *  and scope building passes so that we can proceed with name/field resolution
     *  using the deeply-loaded AST. If the AST is already fully loaded, do
     *  nothing.
     *  This routine recursively ensures deep loading for all superclasses as
     *  well. 
     *  @param node The AST node for the given class or interface declaration.
     *  @return true if the AST for the given node, or an AST for at least one
     *  superclass of the node has changed as a result of ensuring deep loading.
     *  This means that the scope of relevant subclasses need to be rebuilt
     *  if we need updated scopes.
     *  @exception A RunTimeException is thrown if the declaration is null, or
     *  if it does not correspond to a class or interface declaration.
     */
    public static boolean ensureDeepLoading(TreeNode node) {

        boolean hasChanged = false;

        if (!(node instanceof UserTypeDeclNode)) {
            throw new RuntimeException("Invalid source node for class or interface "
                    + "declaration (" + ((node == null) ? "null"  : 
                    node.getClass().getName()) + ")"); 
        }
        UserTypeDeclNode declarationNode = (UserTypeDeclNode)(node);

        // First, ensure deep loading for all super classes.
        if (declarationNode instanceof ClassDeclNode) {
            ClassDecl superClass = ((ClassDecl)(JavaDecl.getDecl(
                    (TreeNode)declarationNode))).getSuperClass();
            if (superClass != null) 
                if (ensureDeepLoading(superClass.getSource())) hasChanged = true;
        }

        // Ensure deep loading for all interfaces.
        List interfaceList = declarationNode.getInterfaces();
        if (interfaceList != null) {
            Iterator interfaces = interfaceList.iterator();
            while (interfaces.hasNext()) {
                ClassDecl interfaceDeclaration = (ClassDecl) JavaDecl.getDecl(
                        (NamedNode) interfaces.next());
                if (ensureDeepLoading(interfaceDeclaration.getSource())) 
                    hasChanged = true;
            } 
        }

        // Ensure deep loading for the class or interface itself, and rebuild the scope
        // if necessary.
        if (ASTReflect.isShallow(declarationNode)) {
            _loadDeeply(declarationNode);
            hasChanged = true;
        }

        // Rebuild the scope --- even though this class has not had more
        // AST detail added, some super class has.
        else if (hasChanged) _rebuildScopes(declarationNode);

        return hasChanged;
    }

    /** Return a list of fields where each element contains
     *  a FieldDeclNode for that field.
     *  @param myClass The class to be analyzed.
     *  @return The List of FieldDeclNodes for the class.
     */
    public static List fieldsASTList(Class myClass) {
	List fieldList = new LinkedList();
	Field fields[] = myClass.getDeclaredFields();
	for(int i = 0; i < fields.length; i++) {
	    int modifiers =
                Modifier.convertModifiers(fields[i].getModifiers());
	    NameNode fieldName =
                (NameNode) _makeNameNode(fields[i].getName());
	    TypeNode defType = _definedType(fields[i].getType());

	    FieldDeclNode  fieldDeclNode =
		new FieldDeclNode(modifiers,
                        defType,
                        fieldName,
                        /* initExpr */
                        AbsentTreeNode.instance);

    	    fieldList.add(fieldDeclNode);
	}
	return fieldList;
    }

    /** Return a list of inner classes where each element contains
     *  a ClassDeclNode or an InterfaceDeclNode  for that inner class.
     *  @param myClass The class to be analyzed.
     *  @return The List of ClassDeclNodes or InterfaceDeclNodes
     *  for the inner classes and inner interfaces of the class.
     */
    public static List innerClassesOrInterfacesASTList(Class myClass) {
	List innerClassesOrInterfacesList = new LinkedList();
	// Handle inner classes and inner interfaces
	Class classes[] = myClass.getDeclaredClasses();
	for(int i = 0; i < classes.length; i++) {
            if (classes[i].isInterface()) {
                innerClassesOrInterfacesList.add(
                        ASTInterfaceDeclNode(classes[i]));
            } else {
                innerClassesOrInterfacesList.add(ASTClassDeclNode(classes[i]));
            }
	}
    	return innerClassesOrInterfacesList;
    }

    /** Return an AST that contains an interface declaration.
     *  @param myClass The class to be analyzed.
     *  @return The InterfaceDeclNode of the class.
     */
    public static InterfaceDeclNode ASTInterfaceDeclNode(Class myClass) {
	int modifiers =
	    Modifier.convertModifiers(myClass.getModifiers());
	NameNode className = (NameNode) _makeNameNode(myClass.getName());
	List interfaceList = _typeNodeList(myClass.getInterfaces());
	LinkedList memberList = new LinkedList();

	// Get the AST for all the constructor.
	memberList.addAll(constructorsASTList(myClass));

	// Get the AST for all the methods.
	memberList.addAll(methodsASTList(myClass));

	// Get the AST for all the fields.
	memberList.addAll(fieldsASTList(myClass));

	// Get the AST for all the inner classes and interfaces
	memberList.addAll(innerClassesOrInterfacesASTList(myClass));

	InterfaceDeclNode interfaceDeclNode =
	    new InterfaceDeclNode(modifiers,
                    className,
                    interfaceList,
                    memberList);
	return interfaceDeclNode;
    }

    /** Given a CompileUnitNode, return the fully-qualified name
     *  of the class being declared --- that is, the complete package name,
     *  followed by the class name (with a dot as a separator).
     *  If no package name is associated with the CompileUnitNode,
     *  then use the default package name for code generation. 
     *  @param loadedAST The CompileUnitNode of the class
     *  @return The fully qualified name of the class being declared. 
     */
    public static String getFullyQualifiedName(CompileUnitNode loadedAST) {
	    // FIXME: This get(0) worries me.
        StringBuffer packageBuffer =
	    new StringBuffer(((UserTypeDeclNode) loadedAST.
                    getDefTypes().get(0)).getName().getIdent());

        Object packageReturnValue = loadedAST.getPkg();
        if (!(packageReturnValue instanceof NameNode))
            packageBuffer.insert(0, 
                    JavaStaticSemanticConstants.DEFAULT_PACKAGE_NAME + '.');
        else {
	        NameNode packageNode = (NameNode) packageReturnValue;
	        while (packageNode.getQualifier() != AbsentTreeNode.instance) {
	            packageBuffer.insert(0, packageNode.getIdent() + '.');
	            packageNode = (NameNode) packageNode.getQualifier();
	        }
	        packageBuffer.insert(0, packageNode.getIdent() + '.');
        }
	    return packageBuffer.toString();
    }


    /** Return the associated fully qualified class name given a class declaration
     *  node.
     *  @param loadedClass The class declaration node.
     *  @return The fully qualified name of the class being declared.
     */
    public static String getFullyQualifiedName(UserTypeDeclNode loadedClass) {
        TreeNode parent = loadedClass.getParent();
        if (!(parent instanceof CompileUnitNode))  {
	        throw new RuntimeException("ASTReflect.getFullyQualifiedName(): "
                    + "Could not find the compilation unit (CompileUnitNode) "
                    + "associated with class '" + loadedClass.getName().getIdent()
                    + ". \nA dump of the offending AST subtree follows.\n"
                    + loadedClass.toString());
        }
        return ASTReflect.getFullyQualifiedName((CompileUnitNode)parent);
    }

    /** Return a string that indicates what loading mode was used to 
     *  load a given class or interface. The returned string is 
     *  either 'shallow', 'deep', or 'full'.
     *  @param loadedClass The class or interface whose loading mode is to be returned.
     *  @return A string that represents the loading mode of the given class or
     *  interface.
     *  @exception A run-time exception is thrown if the loading mode cannot
     *  be determined.
     */
    public static String getLoadingMode(UserTypeDeclNode loadedClass) {
        if (isShallow(loadedClass)) return "shallow";
        else if (isDeep(loadedClass)) return "deep";
        else if (isFull(loadedClass)) return "full";
        else throw new RuntimeException("ASTReflect.getLoadingMode(): "
            + "Class '" + getFullyQualifiedName(loadedClass) 
            + "' has unknown loading status."
            + "\nA dump of the offending AST subtree follows.\n"
            + loadedClass.toString());
    }

    /** Return a diagnostic message that provides information about the set 
     *  of compile units
     *  that have been loaded and undergone package resolution. At minimum,
     *  the number of ASTs loaded through each loading mode (shallow, deep, or full)
     *  is included in the diagnostic message. Additional information 
     *  can be included by setting one or more of the method arguments to 
     *  'true'.
     *  @param listClasses List the classes and interfaces (compile unit nodes)
     *  that have been loaded. The listing
     *  of compile unit nodes is grouped by loading mode --- full, deep, or shallow.
     *  @param dumpASTs Dump the abstract syntax tree for each compile unit node.
     *  This option can result in a very large amount of generated text. 
     *  @return The diagnostic message.
     */
    public static String getLoadingStatus(boolean listClasses, boolean dumpASTs) {
        LinkedList shallowASTs = new LinkedList();
        LinkedList deepASTs = new LinkedList();
        LinkedList fullASTs = new LinkedList();
        Collection compilationUnitList = StaticResolution.allPass0ResolvedMap.values();
        Iterator compilationUnits = compilationUnitList.iterator();
        while (compilationUnits.hasNext()) {
            UserTypeDeclNode declaration 
                    = NodeUtil.getDefinedType((CompileUnitNode)(compilationUnits.next()));
            String information = "";
            if (listClasses) {
                information += getFullyQualifiedName(declaration);
                if (declaration instanceof InterfaceDeclNode) 
                    information  += " (interface)";
            }
            if (dumpASTs) information  += declaration.toString();
            if (isShallow(declaration)) shallowASTs.add(information );
            else if (isDeep(declaration)) deepASTs.add(information );
            else if (isFull(declaration)) fullASTs.add(information );
	        else throw new RuntimeException("ASTReflect.getLoadingStatus(): "
                    + "Class '" + getFullyQualifiedName(declaration) 
                    + "' has unknown loading status."
                    + "\nA dump of the offending AST subtree follows.\n"
                    + declaration.toString());
        }
        StringBuffer status = new StringBuffer();
        status.append(shallowASTs.size() + deepASTs.size() + fullASTs.size() 
                + " loaded ASTs: " + shallowASTs.size() + " shallow, " 
                + deepASTs.size() + " deep, " + fullASTs.size() + " full.\n");
        if (listClasses || dumpASTs) {
            Iterator names;
            if (shallowASTs.size() > 0) {
                status.append("Shallowly loaded classes:\n");
                names = shallowASTs.iterator();
                while (names.hasNext()) status.append(names.next() + "\n"); 
                status.append("\n");
            }
            if (deepASTs.size() > 0) {
                status.append("Deeply loaded classes:\n");
                names = deepASTs.iterator();
                while (names.hasNext()) status.append(names.next() + "\n"); 
                status.append("\n");
            }
            if (fullASTs.size() > 0) {
                status.append("Fully loaded classes:\n");
                names = fullASTs.iterator();
                while (names.hasNext()) status.append(names.next() + "\n"); 
                status.append("\n");
            }
        }

        return status.toString();
    }

    /** Return a brief diagnostic message that 
     *  indicates the number of ASTs loaded through each loading mode 
     *  (shallow, deep, or full).
     *  @return The diagnostic message.
     */
    public static String getLoadingStatus() {
        return getLoadingStatus(false, false);
    }

    /** Return 'true' if and only if the given user type declaration has been
     *  loaded in deep form. If this method returns false, it means that 
     *  the declaration is null, or it has been loaded fully or shallowly.
     *  @param userTypeDeclNode The user type declaration.
     *  @return 'true' if and only the declaration has been loaded deeply.
     */
    public static boolean isDeep(UserTypeDeclNode node) {
        if (node == null) return false;
        else return (!(isShallow(node) || isFull(node)));
    }
    
    /** Return 'true' if and only if the given user type declaration has been
     *  loaded in shallow form.  If this method returns false, it means that 
     *  the declaration is null, or it has been loaded fully or deeply.
     *  @param userTypeDeclNode The user type declaration.
     *  @return 'true' if and only the declaration has been loaded shallowly.
     */
    public static boolean isShallow(UserTypeDeclNode node) {
        if (node == null) return false;
        else if (node.getMembers() == null) return true;
        else return false;
    }

    /** Return 'true' if and only if the given user type declaration has been
     *  loaded in full form. If this method returns false, it means that 
     *  the declaration is null, or it has been deeply or shallowly loaded, or
     *  the declaration effectively contains no methods or constructors (i.e.,
     *  there is nothing in the declaration that would make a difference
     *  between full and deep loading). In other words, the method returns
     *  'true' if and only if it finds at least one method or constructor 
     *  that has a non-empty body.
     *  @param userTypeDeclNode The user type declaration.
     *  @return 'true' if and only the declaration has been loaded fully.
     */
    public static boolean isFull(UserTypeDeclNode node) {
        if (node == null) return false;
        else {
            List memberList = node.getMembers();
            if (node.getMembers() == null) return false;
            else {
                Iterator members = memberList.iterator();
			    while (members.hasNext()) {
                    TreeNode member = (TreeNode) (members.next());
                    TreeNode body = null;
                    if (member instanceof ConstructorDeclNode) {
                        body = ((ConstructorDeclNode)member).getBody();
                    }
                    else if (member instanceof MethodDeclNode) {
                        body = ((MethodDeclNode)member).getBody();
                    }
                    if ((body != null) && !(body instanceof AbsentTreeNode))
                        return true;
                }
            }
            return false;
        }
    }

    /** Given a class name, try to find a class that corresponds with it
     *  by first looking in the set of currently loaded packages and then
     *  by searching the directories in SearchPath.NAMED_PATH.
     *  If a class is not found, return null.
     *  @param className The name of the class, which may or may
     *  not be fully qualified.
     *  @return The Class object.
     *  @see SearchPath
     */
    public static Class lookupClass(String className) {
        try {
            // The classname was something like java.lang.Object
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // The className was something like Object, so
            // we search the loaded packages.
            // FIXME: we could try optimizing this so we
            // look in java.lang first, which is where
            // vast majority of things will be found.
            Package packages[] = Package.getPackages();
            for(int i = 0; i < packages.length; i++) {
                String qualifiedName =
                    new String(packages[i].getName() + "." + className);
                try {
                    return Class.forName(qualifiedName);
                } catch (ClassNotFoundException ee) {
                    // Keep searching the packages.
                }
	    }

            // Ok, try the SearchPath
            // FIXME: This will not work if a jar file or zip file
            // is in the CLASSPATH.
            for (int i = 0; i < SearchPath.NAMED_PATH.size(); i++) {
                String candidate = (String) SearchPath.NAMED_PATH.get(i);
                System.out.println("ASTReflect.lookupClass: SearchPath: '"
				   + candidate
                                   + "' looking for: "
                                   + className);
                File file = new File(candidate + className + ".class");
                if (file.isFile()) {
                    String qualifiedName =
                        new String(candidate.replace(File.separatorChar, '.')+
                                "." + className);
                    System.out.println("ASTReflect.lookupClass: qualified: " +
                            qualifiedName);
                    try {
                        return Class.forName(qualifiedName);
                    } catch (ClassNotFoundException ee) {
                        // Keep searching the packages.
                    }
                }
            }
            // Generate a list of packages for use in the error message.
            StringBuffer packageBuffer = new StringBuffer();
            for(int i = 0; i < packages.length; i++) {
                packageBuffer.append(packages[i].getName() + " ");
            }
	    throw new RuntimeException("ASTReflect.lookupClass(): " +
                    "Could not find class '" + className +
                    "'. The package of this class has " +
                    "not yet been loaded, so we need to " +
                    "look in the searchPath. Looked in " +
                    packageBuffer);
	}
    }

    /** Lookup a class by name, return the ClassDeclNode
     *  @param className The name of the class.
     *  @return The ClassDeclNode that represents the class.
     */
    public static ClassDeclNode lookupClassDeclNode(String className) {
        return ASTClassDeclNode(lookupClass(className));
    }

    /** Return a list of methods for a class where each element contains
     *  a MethodDeclNode for that method
     *  @param myClass The class to be analyzed.
     *  @return The List of methods for the class.
     */
    public static List methodsASTList(Class myClass) {
	List methodList = new LinkedList();
	Method methods[] = myClass.getDeclaredMethods();
	for(int i = 0; i < methods.length; i++) {
	    if (! myClass.equals(methods[i].getDeclaringClass())) {
		// This method was declared in a parent class,
		// so we skip it
		continue;
	    }
	    int modifiers =
		Modifier.convertModifiers(methods[i].getModifiers());
	    NameNode methodName =
                (NameNode) _makeNameNode(methods[i].getName());
	    List paramList = _paramList(methods[i].getParameterTypes());
	    List throwsList = _typeNodeList(methods[i].getExceptionTypes());
	    TypeNode returnType = _definedType(methods[i].getReturnType());

	    MethodDeclNode methodDeclNode =
		new MethodDeclNode(modifiers,
                        methodName,
                        paramList,
                        throwsList,
                        AbsentTreeNode.instance /* body */,
                        returnType);

	    methodList.add(methodDeclNode);
	}
	return methodList;
    }


    /** Given a path name, try to find a class that corresponds with it
     *  by starting with the filename and adding directories from the
     *  path name until we find a class or run through all the directories.
     *  If the class is to be found, it must be compiled an loaded into
     *  the JVM runtime system at the time this method runs.
     *  If a class is not found, return null.
     *  @param pathName  The path name of the classname to lookup.  The
     *  path name consists of directories separated by File.separatorChar's
     *  with an optional suffix.
     *  @return The Class object that represents the class.
     */
    public static Class pathNameToClass(String pathName) {
        try {
            return Class.forName(new String(pathName));
        } catch (Exception e) {}

	    Class myClass = null;

	    // Try to find a class by starting with the file name
	    // and then adding directories as we go along

	    StringBuffer classname = null;
	    if(pathName.lastIndexOf('.') != -1) {
	        // Strip out anything after the last '.', such as .java
	        classname =
		    new StringBuffer(StringManip.baseFilename(
                            StringManip.partBeforeLast(pathName, '.')));
	    } else {
	        classname = new StringBuffer(StringManip.baseFilename(pathName));
	    }

	    // restOfPath contains the directories that we add one by one.
	    String restOfPath = pathName;

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
    private static TypeNode _definedType(Class myClass) {
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
		    NameNode className =
                        (NameNode) _makeNameNode(componentClass.getName());
		    baseType = new TypeNameNode(className);
		}
	    }
	    defType =
		new ArrayTypeNode(baseType);
	} else {
	    if (myClass.isPrimitive()) {
		return _primitiveTypeNode(myClass);
	    } else {
		NameNode className =
                    (NameNode) _makeNameNode(myClass.getName());
		defType = new TypeNameNode(className);
	    }

	}
	return defType;
    }

    /** Given an AST for an interface or class declaration that has been loaded 
     *  in shallow mode, convert the loaded
     *  AST to its deep version, and perform all necessary resolution 
     *  and scope building passes so that we can proceed with name/field resolution
     *  using the deeply-loaded AST.
     *  
     *  @param declNode The AST of the interface or class declaration that is to
     *  be loaded deeply.
     */
    private static void _loadDeeply(UserTypeDeclNode declarationNode) {
        if (StaticResolution.traceLoading) 
            System.out.println("ASTReflect._loadDeeply called on "
                    + declarationNode.getName().getIdent());

        ASTAugmentWithMembers(declarationNode);
        _rebuildScopes(declarationNode);
    }

    // Create a TreeNode that contains the qualifiedName split
    // into separate nodes.
    private static TreeNode _makeNameNode(String qualifiedName) {
        // FIXME: This is copied from StaticResolution.java because
        // we don't want to call StatiResolutoin._makeNameNode() and
        // cause StaticResolution to start reading in
        // all the java.lang packages.

        TreeNode retval = AbsentTreeNode.instance;

        int firstDotPosition;
        do {
            firstDotPosition = qualifiedName.indexOf('.');


//  	    if (firstDotPosition == -1) {
//  		// If we our out of dots, check for a $, which would
//  		// be part of an inner class name.
//  		firstDotPosition = qualifiedName.indexOf('$');
//  	    }

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
	    // The name of the parameter is not available via reflection.
	    NameNode name = new NameNode(AbsentTreeNode.instance,"");
	    TypeNode defType = _definedType(parameterClasses[i]);

	    paramList.add(new ParameterNode(modifier, defType, name));
	}
	return paramList;
    }

    // Given a primitive Class, return the primitive type.
    private static TypeNode _primitiveTypeNode(Class myClass) {
	TypeNode defType = null;
	// FIXME: I'll bet we could reorder these for better performance.
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

    /** Rebuild scopes for an AST that has had detail added through it
     *  through shallow-to-deep conversion
     *  @param node The root of the AST whose scope we want to rebuild.
     */
    private static void _rebuildScopes(UserTypeDeclNode node) {
        JavaDecl declaration = JavaDecl.getDecl((TreeNode)node);
        if (!(declaration instanceof ClassDecl)) {
	        throw new RuntimeException("_loadDeeply: "
                    + "Could not find the class declaration (ClassDecl) "
                    + "associated with class '" + node.getName().getIdent()
                    + ". The declaration's class is: " + ((declaration == null) ? "null"  
                    : declaration.getClass().getName()) 
                    + ".\nA dump of the offending AST subtree follows.\n"
                    + node.toString());
        }
        ClassDecl classDecl = (ClassDecl)declaration;

        // Perform static resolution on the deeply loaded AST
        TreeNode parent = node.getParent();
        if (!(parent instanceof CompileUnitNode))  {
	        throw new RuntimeException("_loadDeeply: "
                    + "Could not find the compilation unit (CompileUnitNode) "
                    + "associated with class '" + node.getName().getIdent()
                    + ". The parent class is: " + ((parent == null) ? "null"  
                    : parent.getClass().getName()) 
                    + ".\nA dump of the offending AST subtree follows.\n"
                    + node.toString());
        }
        if (StaticResolution.debugLoading) 
            System.out.println("_rebuildScopes: rebuilding scopes for '" 
                    + classDecl.fullName() + "'");

        // Make sure the scope gets rebuilt, including the adding of inherited scopes.
        classDecl.invalidateScope();
        classDecl.removeVisitor(ResolveInheritanceVisitor.visitorClass());
        if (parent.hasProperty(StaticResolution.IMPORTED_PACKAGES_KEY))
            parent.removeProperty(StaticResolution.IMPORTED_PACKAGES_KEY); 

        parent.accept(new PackageResolutionVisitor(), null);

        parent.accept(new ResolveClassVisitor(), null);
        
        parent.accept(new ResolveInheritanceVisitor(StaticResolution._defaultTypePolicy),
                null);

        // Make sure the compile unit is registered in the pass1-resolved set
        String className = (String) 
                parent.getDefinedProperty(JavaStaticSemanticConstants.IDENT_KEY);
        if ((className != null) && 
                (StaticResolution.allPass1ResolvedMap.get(className) == null)) {
            StaticResolution.allPass1ResolvedMap.put(className, parent);
        }

        if (StaticResolution.debugLoading) {
            System.out.println("The scope after re-building:\n" 
                    + ((classDecl.getScope() == null) ? "null." :
                    classDecl.getScope().toString(true)));
            System.out.println("_rebuildScopes: finished rebuilding scopes for '" 
                    + classDecl.fullName() + "'");
        }
       
    }

    // Given an an Array of classes, return a List of TypeNodes that
    // contain the names of the classes.
    private static List _typeNodeList(Class [] classes) {
	// Unfortunately, we can't use Arrays.asList() here since
	// what getParameterTypes returns of type Class[], and
	// what we want is a list of TypeNodes
        List classNameList = new LinkedList();
	for(int i = 0; i < classes.length; i++) {
	    NameNode className =
	    	(NameNode) _makeNameNode(classes[i].getName());
	    TypeNode classDeclNode = new TypeNameNode(className);
	    classNameList.add(classDeclNode);
	}
	return classNameList;
    }
}
