/* Methods to aid in the static resolution of names and types in a Java
program.

Copyright (c) 1998-2001 The Regents of the University of California.
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// StaticResolution
/** Methods to aid in the static resolution of names and types in a Java
program.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay, Christopher Hylands, Shuvra S. Bhattacharyya 
@version $Id$
 */
public class StaticResolution implements JavaStaticSemanticConstants {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Invoke class and inheritance resolution on all 
     *  CompileUnitNodes that have only been parsed
     *  and have undergone package resolution, but not including nodes that
     *  have undergone later stages of static resolution.
     */
    public static final void buildScopes() {

        // FIXME: what if the compile unit has allready been pass1-resolved
        // (it is already in the pass1 map)? We don't need to re-do this then.
        Iterator nodeItr = pass0ResolvedList.iterator();
        while (nodeItr.hasNext()) {
            CompileUnitNode node = (CompileUnitNode) nodeItr.next();
            node.accept(new ResolveClassVisitor(), null);
        }

        nodeItr = pass0ResolvedList.iterator();
        while (nodeItr.hasNext()) {
            CompileUnitNode node = (CompileUnitNode) nodeItr.next();
            node.accept(new ResolveInheritanceVisitor(_defaultTypePolicy),
                    null);

            if (!node.hasProperty(IDENT_KEY)) {
                throw new RuntimeException("StaticResolution.buildScopes(): "
                + "missing identifier property."
                + "\nA dump of the offending AST subtree follows.\n"
                + node.toString());
            } else {
                String className = (String) node.getDefinedProperty(IDENT_KEY);

                if (className != null) {
                    allPass1ResolvedMap.put(className, node);
                }
            }
        }

        pass0ResolvedList.clear();
    }
    
    /** Disable shallow loading of abstract syntax trees. */
    public static void disableShallowLoading() {
        _shallowLoadingEnabled = false;
    }

    /** Enable shallow loading of abstract syntax trees whenever possible */
    public static void enableShallowLoading() {
        _shallowLoadingEnabled = true;
    }

    /** Return 'true' if and only if shallow loading is presently enabled.
     *  @return 'true'if and only if shallow loading is presently enabled.
     */
    public static boolean shallowLoadingEnabled() {
        return _shallowLoadingEnabled; 
    }

    /** Get the default type visitor. */
    public static TypeVisitor getDefaultTypeVisitor() {
        return _defaultTypeVisitor;
    }

    /** Uncache the resolved compile node with the given canonical filename
     *  (disregarding the file extension), if the node has undergone a static
     *  resolution pass greater than or equal to the argument pass. This method
     *  must be used with care because references to declarations originally
     *  created in resolving the node may be left in other compile unit nodes.
     *  Because declarations are only equal reflexively, dangling references to
     *  declarations will cause problems. Return true iff a node with the given
     *  canonical filename (disregarding the file extension) was removed
     *  from the cache.
     */
    public static boolean invalidateCompileUnit(String canonicalFilename,
            int pass) {
        if (traceLoading)
            System.out.println("invalidateCompileUnit called on " + canonicalFilename); 
	// This is called from ptolemy.codegen.ActorCodeGenerator
        if ((pass < 0) || (pass > 2)) {
            throw new IllegalArgumentException("invalid pass number : " +
                    pass);
        }

	String noExtensionFilename = canonicalFilename;

	if (canonicalFilename.indexOf(File.separatorChar) != -1 &&
                canonicalFilename.indexOf('.') != -1) {
	    // This is probably a real filename
	    noExtensionFilename =
		StringManip.partBeforeLast(canonicalFilename, '.');
	}

        boolean found = false;

        if (pass == 2) {
            found = (allPass2ResolvedMap.remove(noExtensionFilename) != null);

            if (!found) {
                System.err.println("Warning: couldn't invalidate " +
                        noExtensionFilename);
                Set keySet = allPass2ResolvedMap.keySet();
                System.err.println("Warning: pass 2 resolved files: " +
                        keySet.toString());
	    }
        }

        if (found || (pass == 1)) {
            CompileUnitNode unitNode = (CompileUnitNode)
                allPass1ResolvedMap.remove(noExtensionFilename);

            found = (unitNode != null);

            if (found) {
                PackageDecl pkgDecl =
                    (PackageDecl) unitNode.getDefinedProperty(PACKAGE_KEY);

                Scope pkgScope = pkgDecl.getScope();

                //String className = StringManip.baseFilename(noExtensionFilename);
		// FIXME: we should change the name of noExtensionFilename
		// to className everywhere in this method.
		String className = noExtensionFilename;
                ClassDecl classDecl =
		    (ClassDecl) pkgScope.lookupLocal(StringManip.unqualifiedPart(className),
                            CG_USERTYPE);

                if (classDecl != null) {
                    classDecl.invalidate();
                } else {
                    System.err.println("Warning: StaticResolution.invalidate" +
                            "CompileUnit(): could" +
                            " not find ClassDecl associated" +
                            " with class " + className +
                            " pkgScope: " + pkgScope +
                            " " + unitNode.toString());
                }
            }
        }

        if (found || (pass == 0)) {
            found = (allPass0ResolvedMap.remove(noExtensionFilename) != null);
        }

        return found;
    }

    /** Return a string representation of the list of loaded classes and interfaces, 
     *  the associated
     *  hash map keys, and the loading statuses. This is useful for diagnostic
     *  purposes. Note that if a given compile unit contains multiple user
     *  type definitions (e.g., an interface and a class definition), then
     *  there will accordingly be multiple listings with the same hash map key.
     *  @return The string representation of loaded classes and interfaces.
     */
    public static String getLoadedASTNames() {
        StringBuffer names = new StringBuffer("AST names (key : name (mode))\n");
        Iterator keys = allPass0ResolvedMap.keySet().iterator();
        while (keys.hasNext()) {
            Object nextKey = keys.next();
            CompileUnitNode unit = (CompileUnitNode) allPass0ResolvedMap.get(nextKey);
            Iterator definedTypes = NodeUtil.getDefinedTypes(unit);
            while (definedTypes.hasNext()) {
                UserTypeDeclNode declaration = (UserTypeDeclNode)(definedTypes.next());
                names.append(nextKey + ": " + ASTReflect.getFullyQualifiedName(
                        declaration) + " (" + ASTReflect.getLoadingMode(declaration) + 
                        ")\n");
            }
        }
        return names.toString();

    }

    /** Returns a String representation of node, with qualifiers separated
     *	by periods, if node is a NameNode. If node is AbsentTreeNode.instance,
     *  return "absent name".
     */
    public static String nameString(TreeNode node) {
        if (node == AbsentTreeNode.instance) {
            return "absent name";
        }

        // For temporarily messed up qualifiers produced by resolveAName()
        if (node instanceof NamedNode) {
            node = ((NamedNode) node).getName();
        }

        NameNode name = (NameNode) node;

        TreeNode qualifier = name.getQualifier();
        String identString = name.getIdent();
        if (qualifier == AbsentTreeNode.instance) {
            return identString;
        } else {
            return nameString(qualifier) + "." + identString;
        }
    }

    public static final TreeNode resolveAName(NameNode name, Scope scope,
            TypeNameNode currentClass, JavaDecl currentPackage,
            int categories) {

        if (debugLoading) {
            System.out.println("StaticResolution.resolveAName(): " + " " 
                    + nameString(name));
        }

        // Check to whether or not we have already resolved the name.
        if (name.hasProperty(DECL_KEY)) {
            return name;
        }

        ScopeIterator possibles = _findPossibles(name, scope, currentClass,
                currentPackage, categories);

        if (((categories & CG_METHOD) == 0) && possibles.moreThanOne()) {
            if ((categories & (CG_USERTYPE | CG_PACKAGE)) != 0 ) {
                System.err.println("Warning: ambiguous reference to '" +
                        name.getIdent() +
                        // " in " +
                        //((currentPackage == null) ?
                        //        "?" : currentPackage.fullName()) +
                        //"." +
                        //((currentClass == null) ?
                        //        "?" : currentClass.toString()) +
                        "', using most specific one.");
            } else {
                throw new RuntimeException("ambiguous reference to '"
                        + name.getIdent()
                        + "' which is not a usertype or package "
                        + "in scope " + scope);
            }
        }

        JavaDecl d = (JavaDecl) name.getDefinedProperty(DECL_KEY);

        switch (d.category) {

        case CG_CLASS:
        case CG_INTERFACE:
            {
                ClassDecl classDecl = (ClassDecl) d;

                if (debugLoading) {
		            System.out.println("StaticResolution.resolveAName(): " +
				    "about to call classDecl.loadSource() " +
		 		    d.category + " " + classDecl.getName() );
                }

                try {
                    classDecl.loadSource();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load source: " + e);
                }
                int modifiers = classDecl.getModifiers();
                boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
                boolean isStatic = ((modifiers & STATIC_MOD) != 0);

                // check access : public?
                if (!isPublic) {
                    JavaDecl container = classDecl.getContainer();

                    // a top-level class in the same package?
                    if (container != currentPackage) {

                        // inner class, in the same package?
                        // FIXME : this check is too simple
                        if (!classDecl.deepContainedBy(currentPackage)) {
                            throw new RuntimeException(classDecl.getName() +
                                    " is not accessible. The container " +
                                    container + " is not the same as the " +
                                    "currentPackage " + currentPackage +
                                    " and this classDecl is not contained " +
                                    " by the current package" + classDecl);
                        }
                    }
                }

                if (debugLoading) {
                    System.out.println("resolveAName " + nameString(name) +
                            " (user type) ok, returning ...");
                }
                return name;
            }

        case CG_FIELD:
        case CG_METHOD:
            {
                TreeNode res;
                TreeNode qualifier = name.getQualifier();
                MemberDecl md = (MemberDecl) d;
                if (qualifier == AbsentTreeNode.instance) {
                    if ((md.getModifiers() & STATIC_MOD) != 0) {
                        res = new TypeFieldAccessNode(name, currentClass);
                    } else {
                        res = new ThisFieldAccessNode(name);
                        res.setProperty(THIS_CLASS_KEY, currentClass);
                        // FIXME what's wrong with a normal constructor
                    }
                } else if ((JavaDecl.getDecl(qualifier).category &
                        CG_USERTYPE) != 0) {
                    res = new TypeFieldAccessNode(name,
                            new TypeNameNode((NameNode) qualifier));
                } else {
                    res = new ObjectFieldAccessNode(name, qualifier);
                }

                name.setQualifier(AbsentTreeNode.instance);
                if (debugLoading) {
                    System.out.println("resolveAName " + nameString(name) +
                            " (field or method) ok, returning ...");
                }
                return res;
            }

        case CG_LOCALVAR:
        case CG_FORMAL:
            if (debugLoading) {
                System.out.println("resolveAName " + nameString(name) +
                        " (local var or formal param) ok, returning ...");
            }
            return new ObjectNode(name);

        default:
            return name;
        }
    }

    /** Return a NameNode corresponding to the name, which is qualified
     *  by '.'. If the name is the empty string, return an AbsentTreeNode.
     */
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

    /** Return the declaration with the given qualified name, category,
     *  and method arguments, which must be defined in the given
     *  CompileUnitNode, which must have gone through pass 1 static
     *  resolution.
     *  @param compileUnit The compile unit node in which to look for the
     *   declaration.
     *  @param qualifiedName The string representing the fully qualified
     *   name of the declaration to look for.
     *  @param category The category of the declaration to look for,
     *   either CG_CLASS, CG_INTERFACE, or CG_FIELD.
     *  @return The declaration that was searched for.
     *  @exception NoSuchElementException if the declaration cannot be found.
     */
    public static JavaDecl findDecl(CompileUnitNode compileUnit,
            String qualifiedName, int category) {
        Scope scope = (Scope) compileUnit.getDefinedProperty(SCOPE_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);

        // is this really necessary?
        PackageDecl pkgDecl =
            (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);

        ScopeIterator scopeIter = _findPossibles(nameNode, scope, null,
                pkgDecl, category);

        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (JavaDecl) scopeIter.nextDecl();
    }

    /** Return the method or constructor declaration with the given
     *	qualified name, category, and method arguments, which must be
     *  defined in the given CompileUnitNode, which must have gone through
     *  pass 1 static resolution.
     *  @param compileUnit The compile unit node in which to look for the
     *   declaration.
     *  @param qualifiedName The string representing the fully qualified
     *   name of the declaration to look for.
     *  @param category The category of the declaration to look for,
     *   either CG_METHOD or CG_CONSTRUCTOR.
     *  @param methodArgs A list of TypeNodes of the parameters of the
     *   method or constructor.
     *  @param typeVisitor The TypeVisitor to use to get type information.
     *  @return The declaration that was searched for.
     *  @exception NoSuchElementException if the declaration cannot be found.
     */
    public static MemberDecl findInvokableDecl(CompileUnitNode compileUnit,
            String qualifiedName, int category, List methodArgs,
            TypeVisitor typeVisitor) {
        Scope scope = (Scope) compileUnit.getDefinedProperty(SCOPE_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);

        // is this really necessary?
        PackageDecl pkgDecl =
            (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);

        ScopeIterator scopeIter = _findPossibles(nameNode, scope, null,
                pkgDecl, category);

        ResolveFieldVisitor resolveFieldVisitor =
            new ResolveFieldVisitor(typeVisitor);

        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (MemberDecl)
	    resolveFieldVisitor.resolveCall(scopeIter, methodArgs);
    }


    /** Load a class (if it has not already been loaded) 
     *  by name with a specified level of static resolution.  
     *  The classname does not have a .class or .java suffix.  
     *  The classname should include
     *  the package name, for example "java.lang.Object."
     *  @param className The class name.
     *  @param pass The level of static resolution to be ensured (0, 1, or 2).
     *  @return The loaded and resolved CompileUnitNode associated with the
     *  the specified class name.
     */
    public static CompileUnitNode loadClassName(String className, int pass) {
        if (traceLoading) 
            System.out.println("StaticResolution.loadClassName: " + className);

        CompileUnitNode loadedAST =
            (CompileUnitNode) allPass0ResolvedMap.get(className);

        if (loadedAST == null) {
            loadedAST =
                    JavaParserManip.parseCanonicalClassName(className, false);

            if (loadedAST == null) {
                throw new RuntimeException("Couldn't load " + className);
            }
        }
        return loadCompileUnit(loadedAST, pass);
    }

    /** Load the abstract syntax associated with the given file.
        FIXME: this should probably throw the IOException 

        @param file The given file.
        @param pass The level of desired static resolution (0, 1, or 2).
        @param className The class or interface name (if we know it) of the 
        declaration that we are loading, or null (if we do not know the
        class or interface name). Note: if the file has been loaded before,
        then it is safest to specify the class name: the class name is used
        to look up the class, and determine whether or not its AST is already
        loaded (cached). 
       
     */
    public static CompileUnitNode loadFile(File file, int pass, String className) { 
        if (traceLoading) System.out.println("StaticResolution.loadFile:" +
                file.getName());
        try {
            return _loadCanonicalFile(file.getCanonicalPath(), pass, className);
        } catch (Exception ex) {
	    ex.printStackTrace();
            throw new RuntimeException("StaticResolution.loadFile(" +
				       file + ", " + pass + "). Could not load file.");
        }
    }

    /** Load the source file with the given filename. The filename may be
     *  relative or absolute. The 'pass' and 'className' arguments are as in
     *  loadFile.
     */
    public static CompileUnitNode loadFileName(String filename, int pass,
            String className) {
        return loadFile(new File(filename), pass, className);
    }

    /** Load a CompileUnitNode that has been parsed. Go through all passes
     *  of static resolution up to the argument pass number.
     *
     */
    public static CompileUnitNode loadCompileUnit(CompileUnitNode node,
            int pass) {

        if (traceLoading) 
            System.out.println("Starting loadCompileUnit(); " +
                    "the allParsedMap size is " + JavaParserManip.allParsedMap.size());

        switch (pass) {
        case 0:
            return _resolvePass0(node);

        case 1:
            node = _resolvePass0(node);
            return _resolvePass1(node);

        case 2:
            node = _resolvePass0(node);
            node = _resolvePass1(node);
            return _resolvePass2(node);

        default:
            throw new IllegalArgumentException("invalid pass number (" +
                    pass + ")");
        }

    }

    /** Set the default type visitor. This is used to change the type
     *  "personality" of the compiler.
     */
    public static void setDefaultTypeVisitor(TypeVisitor typeVisitor) {
        _defaultTypeVisitor = typeVisitor;
        _defaultTypePolicy = typeVisitor.typePolicy();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // Outer-level packages have SYSTEM_PACKAGE as their container.
    public static PackageDecl SYSTEM_PACKAGE;
    // UNNAMED_PACKAGE is contained by SYSTEM_PACKAGE.
    public static PackageDecl UNNAMED_PACKAGE;
    public static PackageDecl JAVA_LANG_PACKAGE;

    public static ClassDecl OBJECT_DECL;
    public static ClassDecl STRING_DECL;
    public static ClassDecl CLASS_DECL;
    public static ClassDecl CLONEABLE_DECL;

    public static TypeNameNode OBJECT_TYPE;
    public static TypeNameNode STRING_TYPE;
    public static TypeNameNode CLASS_TYPE;
    public static TypeNameNode CLONEABLE_TYPE;

    public static ClassDecl  ARRAY_CLASS_DECL;
    public static FieldDecl  ARRAY_LENGTH_DECL;

    // FIXME: Where is ARRAY_CLONE_DECL used? It looks like we can remove it.
    public static MethodDecl ARRAY_CLONE_DECL;

    /** A List containing values of CompileUnitNodes that have only been parsed
     *  and have undergone package resolution, but not including nodes that
     *  have undergone later stages of static resolution.
     */
    public static final List pass0ResolvedList = new LinkedList();

    /** A Map containing values of CompileUnitNodes that have only been parsed
     *  and have undergone package resolution, including nodes that
     *  have undergone later stages of static resolution, indexed by the
     *  canonical filename of the source file, without the filename extension.
     */
    public static final Map allPass0ResolvedMap = new HashMap();

    /** A Map containing values of CompileUnitNodes that have undergone
     *  and have undergone package, class and inheritance resolution,
     *  including nodes that have undergone later stages of static
     *  resolution.
     */
    public static final Map allPass1ResolvedMap = new HashMap();

    /** A Map containing values of all CompileUnitNodes that have undergone
     *  name and field resolution, indexed by the canonical filename of the
     *  source file, without the filename extension.
     */
    public static final Map allPass2ResolvedMap = new HashMap();

    public static TypeVisitor _defaultTypeVisitor = new TypeVisitor();
    public static TypePolicy _defaultTypePolicy =
    _defaultTypeVisitor.typePolicy();
   
    /** Generate debugging output associated with loading of ASTs.
     *  FIXME: remove when we are done debugging the loading of ASTs.
     */ 
    public static boolean debugLoading = false; 

     /** A flag that indicates whether or not we should allow deep loading
      *  of ASTs for user-defined classes (classes that are not core ptolemy
      *  classes or Java system classes). Note classes that cannot be loaded
      *  deeply also cannot be loaded shallowly (we don't have facilities for
      *  shallow-to-full conversion), and thus if this flag is turned off,
      *  user-defined classes are loaded in full form. Generally, this
      *  flag should be turned off when generating code from Ptolemy II models
      *  (to enable type specialization), but it may be possible to
      *  leave it on with JavaConverters, which transform Java source files
      *  into alternative formats.
      */
     public static boolean enableDeepUserASTs = false;

     /** A flag for enabling the display of diagnostic information 
      *  pertaining to the times at which ASTs are loaded. A 'true' setting
      *  enables the diagnostic output.
      */
     public static boolean traceLoading = false; 


    /* Perform necessary initialization functions for static resolution. This
     * method should be called once before any attempt is made to perform
     * any type of static resolution. Subsequent calls to the method will
     * be ignored, unless the call is made through the 'redoSetup' method.
     */
    public static void setup() {
        // Proceed only if this is first attempt to initialize static resolution
        if (_alreadyInitialized) return;
 	    long startTime= System.currentTimeMillis();
        System.out.println("StaticResolution<setup>: " 
                + "--- Creating SYSTEM_PACKAGE PackageDecl ---" 
                + (System.currentTimeMillis() - startTime) + " ms");
        SYSTEM_PACKAGE  = new PackageDecl("", null);
        System.out.println("StaticResolution<setup>: "
                + "--- Creating UNNAMED_PACKAGE PackageDecl ---" 
                + (System.currentTimeMillis() - startTime) + " ms");
        UNNAMED_PACKAGE = new PackageDecl("", SYSTEM_PACKAGE);

        // dummy scope
        Scope scope = new Scope();

        System.out.println("StaticResolution<setup>: --- loading java.lang package ---" 
                + (System.currentTimeMillis() - startTime));

	    // JAVA_LANG_PACKAGE is only used in FindExtraImportsVisitor
	    NameNode javaLangName = (NameNode) makeNameNode("java.lang");
        System.out.println("StaticResolution<setup>: scope: " + scope.toString());
	    JAVA_LANG_PACKAGE = _importPackage(scope, javaLangName);

        System.out.println("StaticResolution<setup>: --- require class on Object ---" + 
                (System.currentTimeMillis() - startTime));

        OBJECT_DECL = _requireClass(scope, "Object");

        System.out.println("\nStaticResolution<setup>: --- done require class on Object ---" + (System.currentTimeMillis() - startTime));

        OBJECT_TYPE = OBJECT_DECL.getDefType();

        System.out.println("StaticResolution<setup>: --- require class on Cloneable ---" + (System.currentTimeMillis() - startTime));
        CLONEABLE_DECL = _requireClass(scope, "Cloneable");
        CLONEABLE_TYPE = CLONEABLE_DECL.getDefType();

        // virtual class for arrays

        System.out.println("StaticResolution<setup>: --- virtual class for arrays ---" + (System.currentTimeMillis() - startTime));
        List arrayClassMembers = new LinkedList();

        FieldDeclNode arrayLengthNode = new FieldDeclNode(PUBLIC_MOD | FINAL_MOD,
                IntTypeNode.instance, new NameNode(AbsentTreeNode.instance, "length"),
                AbsentTreeNode.instance);

        // clone() method has an empty body for now
        MethodDeclNode arrayCloneNode = new MethodDeclNode(PUBLIC_MOD | FINAL_MOD,
                new NameNode(AbsentTreeNode.instance, "clone"), new LinkedList(),
        //      new LinkedList(), new BlockNode(new LinkedList()), OBJECT_TYPE);
        // Use a null object to denote an empty body to distinguish this from a
        // body with an empty statement list.
                new LinkedList(), null, OBJECT_TYPE);

        arrayClassMembers.add(arrayLengthNode);
        arrayClassMembers.add(arrayCloneNode);

        ClassDeclNode arrayClassNode = new ClassDeclNode(PUBLIC_MOD,
                new NameNode(AbsentTreeNode.instance, "<array>"),
                TNLManip.addFirst(CLONEABLE_TYPE), arrayClassMembers, OBJECT_TYPE);

        CompileUnitNode arrayCompileUnitNode = new CompileUnitNode(
                javaLangName, new LinkedList(), TNLManip.addFirst(arrayClassNode));

        // give the CompileUnitNode a dummy name so it can be retrieved
        arrayCompileUnitNode.setProperty(IDENT_KEY, "<array>");

        // resolve the names of the virtual class
        System.out.println("StaticResolution<setup>: --- load arrayCompileUnitNode ---" + (System.currentTimeMillis() - startTime));
        loadCompileUnit(arrayCompileUnitNode, 0);

        ARRAY_CLASS_DECL = (ClassDecl) JavaDecl.getDecl((NamedNode) arrayClassNode);
        ARRAY_LENGTH_DECL = (FieldDecl) JavaDecl.getDecl((NamedNode) arrayLengthNode);
        ARRAY_CLONE_DECL  = (MethodDecl) JavaDecl.getDecl((NamedNode) arrayCloneNode);

        System.out.println("StaticResolution<setup>: --- require class on String ---" + (System.currentTimeMillis() - startTime));
        STRING_DECL = _requireClass(scope, "String");
        STRING_TYPE = STRING_DECL.getDefType();

        System.out.println("StaticResolution<setup>: --- require class on Class ---" + (System.currentTimeMillis() - startTime));
        CLASS_DECL  = _requireClass(scope, "Class");
        CLASS_TYPE  = CLASS_DECL.getDefType();

        if (traceLoading) {
            System.out.println("The SYSTEM_PACKAGE scope is:\n" + 
                    SYSTEM_PACKAGE.getScope().toString());
            System.out.println("The UNNAMED_PACKAGE scope is:\n" + 
                    UNNAMED_PACKAGE.getScope().toString());
            System.out.println("The JAVA_LANG_PACKAGE scope is:\n" + 
                    JAVA_LANG_PACKAGE.getScope().toString());
        }

        System.out.println("StaticResolution<setup>: --- 1st buildScopes ---" + (System.currentTimeMillis() - startTime));
        buildScopes();
        System.out.println("StaticResolution<setup>: --- after buildScopes ---" + (System.currentTimeMillis() - startTime));
        _alreadyInitialized = true;
    }

    /** Force re-initialization of static resolution even if the setup() method
     *  has already been called. This method should be used with caution, as
     *  multiple re-initializations can lead to unpredictable behavior.
     */
    public static void redoSetup() {
        _alreadyInitialized = false;
        setup();
    }

    public static PackageDecl _importPackage(Scope scope, NameNode name) {
	// The getScope() call is what loads up the scope with
	// info about what is in the package by looking in the
	// appropriate directory
        if (debugLoading) {
            System.out.println("Calling resolveAName from " + 
                    "StaticResolution._importPackage(" + name.getIdent() + ")");
        }
        resolveAName(name, SYSTEM_PACKAGE.getScope(), null, null,
                CG_PACKAGE);

        PackageDecl decl = (PackageDecl) name.getDefinedProperty(DECL_KEY);

        Scope packageScope = decl.getScope();

        Iterator declItr = packageScope.allLocalDecls();

        while (declItr.hasNext()) {
            JavaDecl type = (JavaDecl) declItr.next();
            if (type.category != CG_PACKAGE) {
                scope.add(type); // conflicts appear on use only
            }
        }

        return decl;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The DECL_KEY property of the given NameNode is set to the first
     *  declaration in the list of 'possibles' that is found. 
     */
    private static ScopeIterator _findPossibles(NameNode name, Scope scope,
            TypeNameNode currentClass, JavaDecl currentPackage, int categories) {

	    if (debugLoading) 
            System.out.println("Begin StaticResolution._findPossibles(): name = " 
                    +  name.getIdent() + "; package = " + currentPackage 
                    + "; code = " + "; cat = " + categories);

        ScopeIterator possibles = new ScopeIterator();

        if (name.getQualifier() == AbsentTreeNode.instance) {
            if ((categories &
                    (CG_FIELD | CG_METHOD | CG_LOCALVAR |
                            CG_FORMAL | CG_USERTYPE)) != 0) {
                possibles = scope.lookupFirst(name.getIdent(), categories);
                if (debugLoading) 
                    System.out.println("Looked up scope for special "
                            + "categories: " + "possibles.hasNext() = " 
                            + possibles.hasNext());


            } else {
                possibles = ((Scope) SYSTEM_PACKAGE.getScope())
                    .lookupFirst(name.getIdent(), categories);
            }
        } else {
            // The given NameNode has a qualifier

            int newCategories = 0;
            if ((categories &
                    (CG_USERTYPE | CG_PACKAGE)) != 0) {
                newCategories |= CG_PACKAGE;
            }

            // for inner classes
            if ((categories & CG_USERTYPE) != 0) {
                newCategories |= (categories & CG_USERTYPE);
            }

            if ((categories & (CG_FIELD | CG_METHOD)) != 0) {
                newCategories |= (CG_USERTYPE | CG_FIELD |
                        CG_LOCALVAR | CG_FORMAL);
            }

            NameNode qualifier = (NameNode) name.getQualifier();
            if (debugLoading) {
                System.out.println("Calling resolveAName on qualifier from " + 
                        "StaticResolution._findPossibles(" + qualifier.getIdent() + ")");
            }
            name.setQualifier(
                    resolveAName(qualifier, scope, currentClass,
                            currentPackage, newCategories));

            JavaDecl container = JavaDecl.getDecl(qualifier);

            // If the container is a class or interface, we need to make
            // sure it is deeply or fully-loaded at this point.
            if (debugLoading) {
                System.out.println("findPossibles: qualifier's declaration class is: "
                        + ((container == null) ? "null" 
                        : container.getClass().getName()));
            }
            ClassDecl classDeclaration = null;
            if (container instanceof TypedDecl) 
                classDeclaration = NodeUtil.typedDeclToClassDecl((TypedDecl)container);
            else if (container instanceof ClassDecl)
                classDeclaration = (ClassDecl)container;
            if (classDeclaration != null) {
                UserTypeDeclNode classSource = 
                        (UserTypeDeclNode)(classDeclaration.getSource()); 
                if (traceLoading)
                    System.out.println("findPossibles: Need to ensure deep loading "
                            + "for type of " + qualifier.getIdent()
                            + ", which is: "
                            + ASTReflect.getFullyQualifiedName(classSource));
                ASTReflect.ensureDeepLoading(classSource); 
                container = JavaDecl.getDecl((TreeNode)classSource);
            }

            if (container.hasScope()) {
                if ((categories & CG_USERTYPE) != 0) {
                    possibles = container.getTypeScope().lookupFirstLocal(
                            name.getIdent(), categories);

                } else {
                    possibles = container.getScope().lookupFirstLocal(
                            name.getIdent(), categories);
                }

            } else if (container instanceof TypedDecl) {
                TypedDecl typedContainer = (TypedDecl) container;
                TypeNode type = typedContainer.getType();
                if (type instanceof PrimitiveTypeNode) {
                    throw new RuntimeException("cannot select " +
                            name.getIdent() +
                            " from non-reference type" +
                            " represented by " + type);
                } else if (type instanceof ArrayTypeNode) {
                    possibles =
			ARRAY_CLASS_DECL.getScope().lookupFirstLocal(
                                name.getIdent(),
                                categories & (CG_FIELD | CG_METHOD));
                } else {
                    // what is this for ???
                    Scope e = JavaDecl.getDecl(type).getScope();
                    possibles = e.lookupFirstLocal(name.getIdent(),
                            categories & (CG_FIELD | CG_METHOD | CG_USERTYPE));
                }
            }
        }

        if (!possibles.hasNext() & ((categories & CG_CLASS) == 1) && currentPackage != null) {
	    System.out.println("StaticResolution_findPossibles(): looking " +
			       "up " + name.getIdent() +
			       "\nwith reflection. Current Class: " +
			       ((currentClass == null) ?
				"null " : currentClass.toString()) +
			       " Current Package: " +
			       ((currentPackage == null) ?
				"null " : currentPackage.fullName()) +
			       "\n categories :" + categories);
  	    // Use reflection
  	    ClassDeclNode classDeclNode =
  		ASTReflect.lookupClassDeclNode(currentPackage.fullName() + "." + name.getIdent());
  	    // FIXME: what if this is an interface
            ClassDecl classDecl = new ClassDecl(name.getIdent(),
                    CG_CLASS,
                    new TypeNameNode(classDeclNode.getName()),
                    classDeclNode.getModifiers(),
                    classDeclNode, currentPackage);
  	    //System.out.println("possibles.hasNext false, reflection: " + classDecl);
  	    classDecl.setScope(scope);
  	    scope.add(classDecl);
  	    possibles = ((Scope) scope).lookupFirst(name.getIdent(), categories);
	}

        if (!possibles.hasNext()) {
            String message = "";
            if ((categories & CG_PACKAGE) != 0) {
                message += "\n\nClasspath error?\n\n";
            }
	        String scopeString = scope.toString();

            message += "Symbol name: \"" +
                name.getIdent() + "\" is undefined in the scope.\n" +
                "Able to find: " + scopeString + "\n" +
		"Current Class: " +
		((currentClass == null) ? "null " : currentClass.toString()) +
		"Current Package: " +
		((currentPackage == null) ?
                        "null " : currentPackage.fullName()) +
		" categories :" + categories;
	    if (name.getIdent().equals("java")) {
		message += "\nSince the missing symbol is 'java', perhaps\n" +
		    "the SearchPath static constructor is not able to\n" +
                    "find your JDK runtime jar file?";
	    }
            throw new RuntimeException(message);
        }

        JavaDecl d = (JavaDecl) possibles.peek();
        name.setProperty(DECL_KEY, d);

        if (traceLoading) 
            System.out.println("_findPossibles for " + nameString(name) + " finished");

        return possibles;
    } // _findPossibles()



    // Load the source (AST) associated with the given canonical filename (if 
    // the source is not already available),
    // and perform a level of resolution as specified by 'pass.' 
    private static CompileUnitNode _loadCanonicalFile(String filename, int pass,
            String className) 
    {
        if (traceLoading) 
            System.out.println("StaticResolution._loadCanonicalFile: " + filename
                    + "; pass = " + pass + "; class = " + ((className == null) ?
                    "null" : className));  

        String noExtensionName = StringManip.partBeforeLast(filename, '.');

        CompileUnitNode loadedAST = null;
 
        if (className != null)
            loadedAST = (CompileUnitNode) allPass0ResolvedMap.get(className);

        // The following does not make sense since class names (.-separated)
        // are used as keyes to the allPass0ResolvedMap
        // loadedAST = (CompileUnitNode) allPass0ResolvedMap.get(noExtensionName);

        if (loadedAST == null) {
            if (traceLoading) {
                System.out.println("_loadCanonicalFile: " + noExtensionName
                        + " not found.\n" + getLoadedASTNames()); 
            }
            // Parse the Java to load a full AST.
            if (traceLoading) System.out.println("_loadCanonicalFile"
                    + ": calling JavaParserManip.parseCanonicalFileName on " 
                    + filename);
            loadedAST = JavaParserManip.parseCanonicalFileName(filename, false);
        }
        else {
            if (traceLoading)
                System.out.println("_loadCanonicalFile: " + noExtensionName 
                        + " was found"); 
        }

        if (loadedAST == null) {
            throw new RuntimeException("_loadCanonicalFile: Could not load " 
                    + filename);
        }
        CompileUnitNode compileUnit = loadCompileUnit(loadedAST, pass);

        return compileUnit;
    }

    // Load classes into the scope.
    // This method is only called a few times to bootstrap the JDK system
    // classes like Object. Shallow AST loading is used here if possible.
    private static final ClassDecl _requireClass(Scope scope, String name) {

        // FIXME: clean up the code in this routine that is commented out.

	    ClassDecl classDecl = null;
        if (traceLoading) 
	        System.out.println("StaticResolution._requireClass() " + name);
        Decl decl = scope.lookup(name);
      
        // Don't do anything if the class has already been loaded. 
        TreeNode source; 
        if ((decl instanceof ClassDecl) && 
                ((source = ((ClassDecl)decl).getSource()) != null)) {
            if (!(source instanceof UserTypeDeclNode)) {
                throw new RuntimeException("StaticResolution.requireClass(): "
                + "Class '" + name + "' has invalid source node type"
                + "\nA dump of the offending AST subtree follows.\n"
                + source.toString());
            }
            if (traceLoading) {
	            System.out.println("StaticResolution._requireClass(): ignoring load "
                        + "request because " + name + " has already been loaded (using " 
                        + ASTReflect.getLoadingMode((UserTypeDeclNode)source) + "mode)"); 
            }
            return (ClassDecl)decl;
        }
     
        if (decl == null) {
	        System.out.println("StaticResolution:_requireClass(): using reflection");
	        // Use reflection
	        Class myClass = ASTReflect.lookupClass(name);
	        if (myClass.isInterface()) {
		        InterfaceDeclNode interfaceDeclNode = 
                        ASTReflect.ASTInterfaceDeclNode(myClass);
		        classDecl = new ClassDecl(name,
                            CG_INTERFACE,
                            new TypeNameNode(interfaceDeclNode.getName()),
                            interfaceDeclNode.getModifiers(),
                            interfaceDeclNode, null);
    
		        //interfaceDecl =
		        //   (ClassDecl) interfaceDeclNode.getDefinedProperty(DECL_KEY);
		        if (classDecl == null) {
		            throw new RuntimeException("could not find class or " +
                                    "interface \"" + name +
                                    "\" in bootstrap scope: "
                                    + scope);
		        }
		        scope.add(classDecl);
	        } else {

		        ClassDeclNode classDeclNode = ASTReflect.ASTClassDeclNode(myClass);
    
		        classDecl = new ClassDecl(name,
                            CG_CLASS,
                            new TypeNameNode(classDeclNode.getName()),
                            classDeclNode.getModifiers(),
                            classDeclNode, null);

		        //classDecl =
		        //   (ClassDecl) classDeclNode.getDefinedProperty(DECL_KEY);
		        if (classDecl == null) {
		            throw new RuntimeException("could not find class or " +
                                    "interface \"" + name +
                                    "\" in bootstrap scope: "
                                    + scope);
		        }
		        scope.add(classDecl);
	        }
    
            // No need to call loadSource(), we have already loaded
            // the class declarations via reflection.   Note that
            // method bodies are not available under reflection.
            return classDecl;

        } else {
	        if ((decl.category & (CG_CLASS | CG_INTERFACE)) == 0) {
		    throw new RuntimeException("fatal error: " + decl.getName() +
                            " should be a class or interface");
	        }
	        classDecl = (ClassDecl) decl;
            if (traceLoading) {
	            System.out.println("StaticResolution._requireClass(): about to "
                        + "call loadSource() on '" + classDecl.getName() + "'");
            }
            try {
                classDecl.loadSource();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load source: " + e);
            }
            return classDecl;
        }
    }

    // Do pass 0 resolution on a CompileUnitNode that just been built by
    // the parser. Return the resolved node. If a source file with the same
    // canonical filename has already been pass 0 resolved, return the
    // previous node.
    private static CompileUnitNode _resolvePass0(CompileUnitNode node) {
        String filename = (String) node.getProperty(IDENT_KEY);

        if (traceLoading)  
            System.out.println("StaticResolution._resolvePass0: " + filename);

        if (filename != null) {
            CompileUnitNode pass0ResolvedNode =
                (CompileUnitNode) allPass0ResolvedMap.get(filename);

            if (pass0ResolvedNode != null) {
                return pass0ResolvedNode;
            }
        }

        if (traceLoading) System.out.println("resolvePass0: starting package resolution");
        node.accept(new PackageResolutionVisitor(), null);
        if (traceLoading) System.out.println("resolvePass0: finished package resolution");

        pass0ResolvedList.add(node);

        if (filename != null) {
            allPass0ResolvedMap.put(filename, node);
        }

        return node;
    }

    // Do pass 1 resolution on a CompileUnitNode. If a source file
    // with the same canonical filename has already been pass 1
    // resolved, return the previous node.
    private static CompileUnitNode _resolvePass1(CompileUnitNode node) {

        String filename = (String) node.getProperty(IDENT_KEY);
        buildScopes();  

        if (filename != null) {
            // allow the node returned by pass 1 to be a different one
            node = (CompileUnitNode) allPass1ResolvedMap.get(filename);
        }

        if (node == null) {
            throw new RuntimeException("Couldn't find " + filename +
                    " in pass 1 resolved map.");
        }

        return node;
    }


    // Do pass 2 resolution on a CompileUnitNode that has already
    // been pass 1 resolved. Return the resolved node. If a source
    // file with the same canonical filename has already been pass 2
    // resolved, return the previous node.

    private static CompileUnitNode _resolvePass2(CompileUnitNode node) {

        String filename = (String) node.getProperty(IDENT_KEY);

        if (traceLoading)  
            System.out.println("StaticResolution._resolvePass2: " + filename);

        if (filename != null) {
            CompileUnitNode pass2ResolvedNode =
                (CompileUnitNode) allPass2ResolvedMap.get(filename);

            if (pass2ResolvedNode != null) {
                return pass2ResolvedNode;
            }
        }

        node.accept(new ResolveNameVisitor(), null);
        node.accept(new ResolveFieldVisitor(_defaultTypeVisitor), null);

        if (filename != null) {
            allPass2ResolvedMap.put(filename, node);
        }

        return node;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A flag that indicates whether or not initialization of static resolution
     *  has been performed.
     */
    private static boolean _alreadyInitialized = false;
   
    /** A flag that indicates whether or not we should allow shallow loading
     *  of ASTs. If the flag is 'false', then 'full' versions of method bodies are
     *  loaded via reflection. These full versions include class internals,
     *  except for method bodies. If the flag is 'true', then most subtrees
     *  of ASTs for loaded classes are excluded, unless the internals of the
     *  classes are explicitly referenced (e.g., by accesses to fields,
     *  methods, or constructors). Shallow loading greatly reduces the run-time
     *  and memory requirements of the code generator.
     */
     private static boolean _shallowLoadingEnabled = false;

}
