/* Methods to aid in the static resolution of names and types in a Java
program.

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

@author Jeff Tsay, Christopher Hylands
@version $Id$
 */
public class StaticResolution implements JavaStaticSemanticConstants {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public static final void buildEnvironments() {

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

            String filename = (String) node.getDefinedProperty(IDENT_KEY);

            if (filename != null) {
                allPass1ResolvedMap.put(filename, node);
            }
        }

        pass0ResolvedList.clear();
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
        if ((pass < 0) || (pass > 2)) {
            throw new IllegalArgumentException("invalid pass number : " +
					       pass);
        }

        String noExtensionFilename =
            StringManip.partBeforeLast(canonicalFilename, '.');

        boolean found = false;

        if (pass == 2) {
            found = (allPass2ResolvedMap.remove(noExtensionFilename) != null);

            if (!found) {
                ApplicationUtility.warn("couldn't invalidate " +
					noExtensionFilename);
                Set keySet = allPass2ResolvedMap.keySet();
                ApplicationUtility.warn("pass 2 resolved files: " +
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

                Environ pkgEnv = pkgDecl.getEnviron();

                String className = StringManip.baseFilename(noExtensionFilename);

                ClassDecl classDecl =
		    (ClassDecl) pkgEnv.lookupProper(className,
						    CG_USERTYPE);

                if (classDecl != null) {
                    classDecl.invalidate();
                } else {
                    ApplicationUtility.warn("invalidateCompileUnit(): could" +
					    " not find ClassDecl associated" +
					    " with class " + className);
                }
            }
        }

        if (found || (pass == 0)) {
            found = (allPass0ResolvedMap.remove(noExtensionFilename) != null);
        }

        return found;
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

    public static final TreeNode resolveAName(NameNode name, Environ env,
            TypeNameNode currentClass, JavaDecl currentPackage,
            int categories) {

        ApplicationUtility.trace("StaticResolution.resolveAName(): " +
				 nameString(name));

        // Check to whether or not we have already resolved the name.
        if (name.hasProperty(DECL_KEY)) {
            ApplicationUtility.trace("decl already defined");
            return name;
        }

        //System.out.println("StaticResolution.resolveAName(): " +
	//			 nameString(name));

        EnvironIter possibles = _findPossibles(name, env, currentClass,
                currentPackage, categories);

        if (((categories & CG_METHOD) == 0) && possibles.moreThanOne()) {
            if ((categories & CG_USERTYPE) != 0) {
                ApplicationUtility.warn("ambiguous reference to " +
					name.getIdent() +
					", using most specific one.");

            } else {
                ApplicationUtility.error("ambiguous reference to " +
					 name.getIdent() +
					 " in environment " + env);
            }
        }

        JavaDecl d = (JavaDecl) name.getDefinedProperty(DECL_KEY);

        switch (d.category) {

        case CG_CLASS:
        case CG_INTERFACE:
            {
                ClassDecl classDecl = (ClassDecl) d;
		//System.out.println("StaticResolution.resolveAName(): " +
		//		   "about to call classDecl.loadSource() " +
		//		   d.category + " " + classDecl.getName() );

                classDecl.loadSource();
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
                            ApplicationUtility.error(classDecl.getName() +
						     " not accessible");
                        }
                    }
                }

                ApplicationUtility.trace("resolveAName " + nameString(name) +
                        " (user type) ok");
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
                ApplicationUtility.trace("resolveAName " + nameString(name) +
                        " (field or method) ok");
                return res;
            }

        case CG_LOCALVAR:
        case CG_FORMAL:
            //ApplicationUtility.trace("resolveAName " + nameString(name) +
            // " (local var or formal param) ok");
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
        Environ env = (Environ) compileUnit.getDefinedProperty(ENVIRON_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);

        // is this really necessary?
        PackageDecl pkgDecl =
            (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);

        EnvironIter environIter = _findPossibles(nameNode, env, null,
                pkgDecl, category);

        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (JavaDecl) environIter.nextDecl();
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
        Environ env = (Environ) compileUnit.getDefinedProperty(ENVIRON_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);

        // is this really necessary?
        PackageDecl pkgDecl =
            (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);

        EnvironIter environIter = _findPossibles(nameNode, env, null,
                pkgDecl, category);

        ResolveFieldVisitor resolveFieldVisitor =
            new ResolveFieldVisitor(typeVisitor);

        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (MemberDecl)
	    resolveFieldVisitor.resolveCall(environIter, methodArgs);
    }

    
    /** Load the source file with the given filename. The filename may be
     *  relative or absolute.
     */
    public static CompileUnitNode load(String filename, int pass) {
        return load(new File(filename), pass);
    }

    /** Load the class by name.  The classname does not have a
     *  .class or .java suffix.  The classname should include
     *  the package name, for example "java.lang.Object"
     */
    public static CompileUnitNode loadClass(String className, int pass) {
        CompileUnitNode loadedAST =
            (CompileUnitNode) allPass0ResolvedMap.get(className);

        if (loadedAST == null) {
            loadedAST = JavaParserManip.parseCanonical(className, false);

            if (loadedAST == null) {
                ApplicationUtility.error("Couldn't load " + className);
            }
        }
        return load(loadedAST, pass);
    }

    public static CompileUnitNode load(File file, int pass) {
        try {
            return _loadCanonical(file.getCanonicalPath(), pass);
        } catch (IOException ioe) {
            ApplicationUtility.error(ioe.toString());
        }
        return null;
    }

    /** Load a CompileUnitNode that has been parsed. Go through all passes
     *  of static resolution up to the argument pass number.
     *
     */
    public static CompileUnitNode load(CompileUnitNode node, int pass) {
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

    public static final PackageDecl SYSTEM_PACKAGE;
    public static final PackageDecl UNNAMED_PACKAGE;
    public static final PackageDecl JAVA_LANG_PACKAGE;

    public static final ClassDecl OBJECT_DECL;
    public static final ClassDecl STRING_DECL;
    public static final ClassDecl CLASS_DECL;
    public static final ClassDecl CLONEABLE_DECL;

    public static final TypeNameNode OBJECT_TYPE;
    public static final TypeNameNode STRING_TYPE;
    public static final TypeNameNode CLASS_TYPE;
    public static final TypeNameNode CLONEABLE_TYPE;

    public static final ClassDecl  ARRAY_CLASS_DECL;
    public static final FieldDecl  ARRAY_LENGTH_DECL;
    public static final MethodDecl ARRAY_CLONE_DECL;

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

    static {
 	long startTime= System.currentTimeMillis();
        System.out.println("StaticResolution<static>: --- Creating two new PackageDecls ---" + (System.currentTimeMillis() - startTime) + " ms");
        SYSTEM_PACKAGE  = new PackageDecl("", null);
        UNNAMED_PACKAGE = new PackageDecl("", SYSTEM_PACKAGE);
        ApplicationUtility.trace("StaticResolution<static>: " +
                "SYSTEM_PACKAGE: " +  SYSTEM_PACKAGE.getEnviron().toString());
        ApplicationUtility.trace("StaticResolution<static>: " +
                "UNNAMED_PACKAGE: " + UNNAMED_PACKAGE.getEnviron().toString());

        // dummy environment
        Environ env = new Environ();

        System.out.println("StaticResolution<static>: --- loading java.lang package ---" + (System.currentTimeMillis() - startTime));

	// JAVA_LANG_PACKAGE is only used in FindExtraImportsVisitor
	NameNode javaLangName = (NameNode) makeNameNode("java.lang");
        System.out.println("StaticResolution<static>: env: " + env.toString());
	JAVA_LANG_PACKAGE = _importPackage(env, javaLangName);


        System.out.println("StaticResolution<static>: --- require class on Object ---" + (System.currentTimeMillis() - startTime));

        OBJECT_DECL = _requireClass(env, "Object");

        System.out.println("StaticResolution<static>: --- done require class on Object ---" + (System.currentTimeMillis() - startTime));

        OBJECT_TYPE = OBJECT_DECL.getDefType();

        System.out.println("StaticResolution<static>: --- require class on Cloneable ---" + (System.currentTimeMillis() - startTime));
        CLONEABLE_DECL = _requireClass(env, "Cloneable");
        CLONEABLE_TYPE = CLONEABLE_DECL.getDefType();

        // virtual class for arrays

        System.out.println("StaticResolution<static>: --- virtual class for arrays ---" + (System.currentTimeMillis() - startTime));
        List arrayClassMembers = new LinkedList();

        FieldDeclNode arrayLengthNode = new FieldDeclNode(PUBLIC_MOD | FINAL_MOD,
                IntTypeNode.instance, new NameNode(AbsentTreeNode.instance, "length"),
                AbsentTreeNode.instance);

        // clone() method has an empty body for now
        MethodDeclNode arrayCloneNode = new MethodDeclNode(PUBLIC_MOD | FINAL_MOD,
                new NameNode(AbsentTreeNode.instance, "clone"), new LinkedList(),
                new LinkedList(), new BlockNode(new LinkedList()), OBJECT_TYPE);

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
        System.out.println("StaticResolution<static>: --- load arrayCompileUnitNode ---" + (System.currentTimeMillis() - startTime));
        load(arrayCompileUnitNode, 0);

        ARRAY_CLASS_DECL = (ClassDecl) JavaDecl.getDecl((NamedNode) arrayClassNode);
        ARRAY_LENGTH_DECL = (FieldDecl) JavaDecl.getDecl((NamedNode) arrayLengthNode);
        ARRAY_CLONE_DECL  = (MethodDecl) JavaDecl.getDecl((NamedNode) arrayCloneNode);

        System.out.println("StaticResolution<static>: --- require class on String ---" + (System.currentTimeMillis() - startTime));
        STRING_DECL = _requireClass(env, "String");
        STRING_TYPE = STRING_DECL.getDefType();

        System.out.println("StaticResolution<static>: --- require class on Class ---" + (System.currentTimeMillis() - startTime));
        CLASS_DECL  = _requireClass(env, "Class");
        CLASS_TYPE  = CLASS_DECL.getDefType();

        System.out.println("StaticResolution<static>: --- 1st buildEnvironments ---" + (System.currentTimeMillis() - startTime));
        buildEnvironments();
        System.out.println("StaticResolution<static>: --- after buildEnvironments ---" + (System.currentTimeMillis() - startTime));
    }

    public static PackageDecl _importPackage(Environ env, NameNode name) {
	// The getEnviron() call is what loads up the environment with
	// info about what is in the package by looking in the
	// appropriate directory
        resolveAName(name, SYSTEM_PACKAGE.getEnviron(), null, null,
                CG_PACKAGE);

        PackageDecl decl = (PackageDecl) name.getDefinedProperty(DECL_KEY);

        Environ packageEnv = decl.getEnviron();

        Iterator declItr = packageEnv.allProperDecls();

        while (declItr.hasNext()) {
            JavaDecl type = (JavaDecl) declItr.next();
            if (type.category != CG_PACKAGE) {
                env.add(type); // conflicts appear on use only
            }
        }
	
        ApplicationUtility.trace("StaticResolution._importPackage(" +
                name.getIdent() + ") :" +
                env.toString());
        return decl;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private static EnvironIter _findPossibles(NameNode name, Environ env,
					    TypeNameNode currentClass,
					    JavaDecl currentPackage,
					    int categories) {

	//System.out.println("StaticResolution._findPossibles():" +  name.getIdent());
        EnvironIter possibles = new EnvironIter();

        if (name.getQualifier() == AbsentTreeNode.instance) {
            if ((categories &
                    (CG_FIELD | CG_METHOD | CG_LOCALVAR |
		     CG_FORMAL | CG_USERTYPE)) != 0) {
                possibles = env.lookupFirst(name.getIdent(), categories);
            } else {
                //System.out.println("StaticResolution._findPossibles(): looking up package " + name.getIdent());
                possibles = ((Environ) SYSTEM_PACKAGE.getEnviron())
                    .lookupFirst(name.getIdent(), categories);
            }
        } else {
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

            name.setQualifier(
                    resolveAName((NameNode) name.getQualifier(),
				 env, currentClass,
				 currentPackage, newCategories));

            JavaDecl container = JavaDecl.getDecl(name.getQualifier());

            if (container.hasEnviron()) {

                if ((categories & CG_USERTYPE) != 0) {

                    possibles = container.getTypeEnviron().lookupFirstProper(
                            name.getIdent(), categories);

                } else {
                    possibles = container.getEnviron().lookupFirstProper(
                            name.getIdent(), categories);
                }

            } else if (container instanceof TypedDecl) {
                TypedDecl typedContainer = (TypedDecl) container;
                TypeNode type = typedContainer.getType();
                if (type instanceof PrimitiveTypeNode) {
                    ApplicationUtility.error("cannot select " +
					     name.getIdent() +
					     " from non-reference type" +
					     " represented by " + type);
                } else if (type instanceof ArrayTypeNode) {
                    possibles =
			ARRAY_CLASS_DECL.getEnviron().lookupFirstProper(
                            name.getIdent(),
			    categories & (CG_FIELD | CG_METHOD));
                } else {
                    // what is this for ???
                    Environ e = JavaDecl.getDecl(type).getEnviron();
                    possibles = e.lookupFirstProper(name.getIdent(),
                            categories & (CG_FIELD | CG_METHOD | CG_USERTYPE));
                }
            }
        }

        if (!possibles.hasNext() & ((categories & CG_CLASS) == 1)) { 
//  	    // Use reflection
//  	    ClassDeclNode classDeclNode =
//  		ASTReflect.lookupClassDeclNode(name.getIdent());
//  	    // FIXME: what if this is an interface
//              ClassDecl classDecl = new ClassDecl(name.getIdent(),
//  				      CG_CLASS,
//  				      new TypeNameNode(classDeclNode.getName()),
//  				      classDeclNode.getModifiers(),
//  				      classDeclNode, null);
//  	    System.out.println("possibles.hasNext false, reflection: " + classDecl);
//  	    Environ environ = new Environ();
//  	    classDecl.setEnviron(environ);
//  	    environ.add(classDecl);
//  	    possibles = ((Environ) environ).lookupFirst(name.getIdent(), categories);
	}

        if (!possibles.hasNext()) {
            String message = "";
            if ((categories & CG_PACKAGE) != 0) {
                message += "\n\nClasspath error?\n\n";
            }
	    String envString = env.toString();
	    if (envString.length() > 100 ) {
		envString = new String(envString.substring(100) + "...");
	    }

            message += "Symbol name: \"" +
                name.getIdent() + "\" is undefined in the environment.\n" +
                "Able to find: " + envString + "\n" +
		"Current Class: " +
		((currentClass == null) ? "null " : currentClass.toString()) +
		"Current Package: " +
		((currentPackage == null) ?
		 "null " : currentPackage.fullName()) +
		" categories :" + categories;
	    if (name.getIdent().equals("java")) {
		message += "\nSince the missing symbol is 'java', perhaps\n" +
		    "you don't have the the .jskel files set up for\n" +
		    "the java sources or the directory that contains\n" +
		    "those .jskel files is not in the CLASSPATH?";
	    }
            ApplicationUtility.error(message);
        }

        JavaDecl d = (JavaDecl) possibles.head();
        name.setProperty(DECL_KEY, d);

        // ApplicationUtility.trace("_findPossibles for " + nameString(name) + " ok");

        return possibles;
    }

    // Load the source file with the given canonical filename. If
    // primary is true, do full resolution of the source. Otherwise
    // do partial resolution only.
    private static CompileUnitNode _loadCanonical(
            String filename, int pass) {
        //System.out.println("StaticResolution._loadCanonical: " + filename);
	System.out.print(".");

        String noExtensionName = StringManip.partBeforeLast(filename, '.');

        CompileUnitNode loadedAST =
            (CompileUnitNode) allPass0ResolvedMap.get(noExtensionName);

        if (loadedAST == null) {
            loadedAST = JavaParserManip.parseCanonical(filename, false);

            if (loadedAST == null) {
                ApplicationUtility.error("Couldn't load " + filename);
            }
        }
        return load(loadedAST, pass);
    }


    // Load classes into the environment.
    // This method is only called a few times to bootstrap the JDK system
    // classes like Object
    private static final ClassDecl _requireClass(Environ env, String name) {
	ClassDecl classDecl = null;
	System.out.println("StaticResolution._requireClass() " + name);
        Decl decl = env.lookup(name);

        if (decl == null) {
	    System.out.println("StaticResolution:_requireClass(): using refl");
	    // Use reflection
	    Class myClass = ASTReflect.lookupClass(name);
	    if (myClass.isInterface()) {
		InterfaceDeclNode interfaceDeclNode =
		    ASTReflect.ASTInterfaceDeclNode(myClass);
		// FIXME: seems like this should be something other
		// than classDecl, perhaps interfaceDecl or userTypeDecl?
		classDecl = new ClassDecl(name,
					  CG_INTERFACE,
					  new TypeNameNode(interfaceDeclNode.getName()),
					  interfaceDeclNode.getModifiers(),
					  interfaceDeclNode, null);

		//interfaceDecl =
		//   (ClassDecl) interfaceDeclNode.getDefinedProperty(DECL_KEY);
		if (classDecl == null) {
		    ApplicationUtility.error("could not find class or " +
					     "interface \"" + name + 
					     "\" in bootstrap environment: "
					     + env);
		}
		env.add(classDecl);
	    } {
		ClassDeclNode classDeclNode =
		    ASTReflect.ASTClassDeclNode(myClass);

		classDecl = new ClassDecl(name,
					  CG_CLASS,
					  new TypeNameNode(classDeclNode.getName()),
					  classDeclNode.getModifiers(),
					  classDeclNode, null);

		//classDecl =
		//   (ClassDecl) classDeclNode.getDefinedProperty(DECL_KEY);
		if (classDecl == null) {
		    ApplicationUtility.error("could not find class or " +
					     "interface \"" + name + 
					     "\" in bootstrap environment: "
					     + env);
		}
		env.add(classDecl);
	    }
            // No need to call loadSource(), we have already loaded
            // the class declarations via reflection.   Note that
            // method bodies are not available under reflection.
            return classDecl;
        } else {
	    if ((decl.category & (CG_CLASS | CG_INTERFACE)) == 0) {
		ApplicationUtility.error("fatal error: " + decl.getName() +
					 " should be a class or interface");
	    }
	    classDecl = (ClassDecl) decl;
	}
	System.out.println("StaticResolution._requireClass() loadSource()");
        classDecl.loadSource();
	System.out.println("\nStaticResolution._requireClass() -- leaving");
        return classDecl;
    }

    // Do pass 0 resolution on a CompileUnitNode that just been built by
    // the parser. Return the resolved node. If a source file with the same
    //  canonical filename has already been pass 0 resolved, return the
    //  previous node.
    private static CompileUnitNode _resolvePass0(CompileUnitNode node) {
        String filename = (String) node.getProperty(IDENT_KEY);

        if (filename != null) {
            CompileUnitNode pass0ResolvedNode =
                (CompileUnitNode) allPass0ResolvedMap.get(filename);

            if (pass0ResolvedNode != null) {
                return pass0ResolvedNode;
            }
        }

        node.accept(new PackageResolutionVisitor(), null);

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

        buildEnvironments();

        String filename = (String) node.getProperty(IDENT_KEY);

        if (filename != null) {
            // allow the node returned by pass 1 to be a different one
            node = (CompileUnitNode) allPass1ResolvedMap.get(filename);
        }

        if (node == null) {
            ApplicationUtility.error("Couldn't find " + filename +
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
}
