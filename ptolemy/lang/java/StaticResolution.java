/*          
Methods to aid in the static resolution of names and types in a Java
program. The code was mostly converted from the Titanium project.

Copyright (c) 1998-1999 The Regents of the University of California.
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

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/**
 *  Methods to aid in the static resolution of names and types in a Java
 *  program. The code was mostly converted from the Titanium project.
 *
 *  @author Jeff Tsay
 */
public class StaticResolution implements JavaStaticSemanticConstants {

    /** Returns a String representation of node, with qualifiers separated by periods,
     *  if node is a NameNode. If node is AbsentTreeNode.instance, return "absent name".
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

    public static EnvironIter findPossibles(NameNode name, Environ env,
        TypeNameNode currentClass, JavaDecl currentPackage, int categories) {

        EnvironIter possibles = new EnvironIter();

        if (name.getQualifier() == AbsentTreeNode.instance) {
           if ((categories &
               (CG_FIELD | CG_METHOD | CG_LOCALVAR | CG_FORMAL | CG_USERTYPE)) != 0) {
              possibles = env.lookupFirst(name.getIdent(), categories);
           } else {
              //ApplicationUtility.trace("looking up package");
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
               newCategories |= CG_USERTYPE;
           }

           if ((categories & (CG_FIELD | CG_METHOD)) != 0) {
               newCategories |= (CG_USERTYPE | CG_FIELD |
                                 CG_LOCALVAR | CG_FORMAL);
           }

           name.setQualifier(
            resolveAName((NameNode) name.getQualifier(), env, currentClass,
             currentPackage, newCategories));

           JavaDecl container = JavaDecl.getDecl(name.getQualifier());

           if (container.hasEnviron()) {
              // qualifier is a package
              possibles = container.getEnviron().lookupFirstProper(
               name.getIdent(), categories);
                             
           } else if (container instanceof TypedDecl) {
              TypedDecl typedContainer = (TypedDecl) container;
              TypeNode type = typedContainer.getType();
              if (type instanceof PrimitiveTypeNode) {
	             ApplicationUtility.error("cannot select " + name.getIdent() +
                  " from non-reference type represented by " + type);
              } else if (type instanceof ArrayTypeNode) {
                 possibles = ARRAY_ENVIRON.lookupFirstProper(name.getIdent(),
                  categories & (CG_FIELD | CG_METHOD));                  
              } else {
                 // 
                 Environ e = JavaDecl.getDecl(type).getEnviron();
                 possibles = e.lookupFirstProper(name.getIdent(),
                  categories & (CG_FIELD | CG_METHOD | CG_USERTYPE));
              } 
           }
        }

        if (!possibles.hasNext()) {
           ApplicationUtility.error(name.getIdent() + " undefined in environ " +
            env.toString());
        }

        JavaDecl d = (JavaDecl) possibles.head();
        name.setProperty(DECL_KEY, d);

        ApplicationUtility.trace("findPossibles for " + nameString(name) + " ok");

        return possibles;
    }

    public static final TreeNode resolveAName(NameNode name, Environ env,
     TypeNameNode currentClass, JavaDecl currentPackage,
        int categories) {

        ApplicationUtility.trace("resolveAName : " + nameString(name));

        // check to whether or not we have already resolved the name
        if (name.hasProperty(DECL_KEY)) {
           ApplicationUtility.trace("decl already defined");
           return name;
        }

        EnvironIter possibles = findPossibles(name, env, currentClass, 
         currentPackage, categories);

        if (((categories & CG_METHOD) == 0) && possibles.moreThanOne()) {
           if ((categories & CG_USERTYPE) != 0) {
             ApplicationUtility.warn("ambiguous reference to " + name.getIdent() + 
              ", using most specific one.");
           
           } else { 
             ApplicationUtility.error("ambiguous reference to " + name.getIdent() +
              " in environment " + env);
           }
        }

        JavaDecl d = (JavaDecl) name.getDefinedProperty(DECL_KEY);

        switch (d.category) {

        case CG_CLASS:
        case CG_INTERFACE:
        {
          ClassDecl cd = (ClassDecl) d;
          cd.loadSource();
          int modifiers = cd.getModifiers();
          boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
          boolean isStatic = ((modifiers & STATIC_MOD) != 0);

          // check access : public?
          if (!isPublic) {
             JavaDecl container = cd.getContainer();

             // a top-level class in the same package?
             if (container != currentPackage) {

                // inner class, in the same package?
                // FIXME : this check is too simple
                if (!cd.deepContainedBy(currentPackage)) {
                   ApplicationUtility.error(cd.getName() + " not accessible");
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
                res = new TypeFieldAccessNode(currentClass, name);
             } else {
                res = new ThisFieldAccessNode(name);
                res.setProperty(THIS_CLASS_KEY, currentClass);
                // FIXME what's wrong with a normal constructor
             }
          } else if ((JavaDecl.getDecl(qualifier).category & CG_USERTYPE) != 0) {      
             res = new TypeFieldAccessNode(
                    new TypeNameNode((NameNode) qualifier), name);
          } else {
             res = new ObjectFieldAccessNode(qualifier, name);
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

    public static MethodDecl resolveCall(EnvironIter methods, List args) {

        Decl aMethod = methods.head();      
        Decl d;
   
        LinkedList types = new LinkedList();
      
        LinkedList argTypes = new LinkedList();
  
        Iterator argsItr = args.iterator();
      
        while (argsItr.hasNext()) {
           ExprNode expr = (ExprNode) argsItr.next();
           argTypes.addLast(TypeUtility.type(expr));
        }
      
        LinkedList matches = new LinkedList();
                
        while (methods.hasNext()) {
           MethodDecl method = (MethodDecl) methods.next();
           if (TypeUtility.isCallableWith(method, argTypes)) {
              matches.addLast(method);
           }          
        }
      
        if (matches.size() == 0) {
           ApplicationUtility.error("no matching " + aMethod.getName() +
            "(" + TNLManip.toString(argTypes) + ")");
        }
       
        Iterator matchesItr1 = matches.iterator();
      
        while (matchesItr1.hasNext()) {
           MethodDecl m1 = (MethodDecl) matchesItr1.next();
           Iterator matchesItr2 = matches.iterator();
           boolean thisOne = true;
         
           while (matchesItr2.hasNext()) {
              MethodDecl m2 = (MethodDecl) matchesItr2.next();
              if (m1 == m2) {
                continue; // get out of this inner loop      
              }
              if (!TypeUtility.isMoreSpecific(m1, m2) || TypeUtility.isMoreSpecific(m2, m1)) {
                 thisOne = false; // keep looking
                 continue; // get out of this inner loop      
              }             
           } 
                   
           if (thisOne) {
              return m1;
           }
        }
      
        ApplicationUtility.error ("ambiguous method call to " + aMethod.getName());
        return null;
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
     *  resolution. If the category is a method, the methodArgs
     *  parameter should be a List of TypeNode's. Otherwise, methodArgs
     *  will be ignored. Throw a NoSuchElementException if the 
     *  declaration cannot be found.
     */
    public static JavaDecl findDecl(CompileUnitNode compileUnit,
     String qualifiedName, int category, List methodArgs) {
        Environ env = (Environ) compileUnit.getDefinedProperty(ENVIRON_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);
        
        // is this really necessary?
        PackageDecl pkgDecl = 
         (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);
        
        EnvironIter environIter = findPossibles(nameNode, env, null, 
         pkgDecl, category);
         
        if (category == CG_METHOD) {
           return resolveCall(environIter, methodArgs);       
        }          
     
        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (JavaDecl) environIter.nextDecl();                  
    }
    
    /*
    public static TypeNameNode getUserTypeInCompileUnit(
     CompileUnitNode compileUnit, String qualifiedName, int category) {
        ClassDecl classDecl = 
         (ClassDecl) getDeclInCompileUnit(compileUnit, qualifiedName,
                      category, null);
          
                             
    }
    */
    
    /** Parse the file, doing no static resolution whatsoever. */
    public static CompileUnitNode parse(String filename) {
        JavaParser p = new JavaParser();

        try {
          p.init(filename);
        } catch (Exception e) {
          ApplicationUtility.error("error opening " + filename + " : " + e);
        }

        p.yyparse();

        return p.getAST();
    }

    /** Load the source file with the given filename. The filename may be
     *  relative or absolute. If primary is true, do full resolution of the 
     *  source. Otherwise do partial resolution only.
     */ 
    public static CompileUnitNode load(String filename, boolean primary) {
        return load(new File(filename), primary);
    }

    public static CompileUnitNode load(File file, boolean primary) {        
        try {
          return loadCanonical(file.getCanonicalPath(), primary);
        } catch (IOException ioe) {
          ApplicationUtility.error(ioe.toString());
        } 
        return null; 
    }

    /** Load the source file with the given canonical filename. If
     *  primary is true, do full resolution of the source. Otherwise
     *  do partial resolution only.
     */ 
    public static CompileUnitNode loadCanonical(String filename, 
     boolean primary) {        
        System.out.println(">loading " + filename);
  
        CompileUnitNode loadedAST = null;
    
        loadedAST = 
         (CompileUnitNode) partiallyResolvedMap.get(filename);           
                                  
        if (loadedAST == null) {
           loadedAST = parse(filename);
           
           if (loadedAST == null) {
              ApplicationUtility.error("Couldn't load " + filename);
           }

           loadedAST.setProperty(IDENT_KEY, filename);    
        
           return load(loadedAST, primary);                          
        } else {
           if (!primary) {
              return loadedAST;
           }
           
           return resolvePass2(loadedAST);
        }
    }
        
    /** Load a CompileUnitNode that has just been parsed. Fully resolve the
     *  node if primary is true, otherwise just partially resolve it.
     */
    public static CompileUnitNode load(CompileUnitNode node, boolean primary) {
        node.setProperty(FULL_RESOLVE_KEY, new Boolean(primary));
                                                                 
        node = resolvePass1(node);            
                                            
        if (primary) {
           node = resolvePass2(node);        
        }            
        
        return node;
    }
    
    /** Do pass 1 resolution on a CompileUnitNode that just been built by
     *  the parser. Return the resolved node. If a source file with the same 
     *  canonical filename has already been pass 1 resolved, return the previous 
     *  node. 
     */
    public static CompileUnitNode resolvePass1(CompileUnitNode node) {    
        String filename = (String) node.getProperty(IDENT_KEY);    
                   
        if (filename != null) {
           CompileUnitNode partiallyResolvedNode = 
            (CompileUnitNode) partiallyResolvedMap.get(filename);           
            
           if (partiallyResolvedNode != null) {
              return partiallyResolvedNode;
           }         
        }

        if (filename != null) {
           partiallyResolvedMap.put(filename, node);
        }            
         
        node.accept(new PackageResolutionVisitor(), null);    
        node.accept(new ResolveClassVisitor(), null);
        node.accept(new ResolveInheritanceVisitor(), null);
        
        //if (filename != null) {
        //   partiallyResolvedMap.put(filename, node);
        //}            
        
        return node;
    }
        
    /** Do pass 2 resolution on a CompileUnitNode that has already been pass 1 
     *  resolved. Return the resolved node. If a source file with the same 
     *  canonical filename has already been pass 2 resolved, return the previous 
     *  node. 
     */
    public static CompileUnitNode resolvePass2(CompileUnitNode node) {
        String filename = (String) node.getProperty(IDENT_KEY);    

        if (filename != null) {
           CompileUnitNode fullyResolvedNode = 
            (CompileUnitNode) fullyResolvedMap.get(filename);           
            
           if (fullyResolvedNode != null) {
              return fullyResolvedNode;
           }         
        }

        if (filename != null) {
           fullyResolvedMap.put(filename, node);              
        }                   
                
        node.accept(new ResolveNameVisitor(), null);     
        node.accept(new ResolveFieldVisitor(), null);                               
          
        //if (filename != null) {
        //   fullyResolvedMap.put(filename, node);              
        //}                   
        
        return node;
    }
    
    public static final void importPackage(Environ env, NameNode name) {

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
    }

    protected static final ClassDecl _requireClass(Environ env, String name) {
        Decl decl = env.lookup(name);

        if (decl == null) {
           ApplicationUtility.error("could not find class or interface \"" +
            name + "\" in bootstrap environment: " + env);
        }

        if ((decl.category & (CG_CLASS | CG_INTERFACE)) == 0) {
           ApplicationUtility.error("fatal error: " + decl.getName() +
	        " should be a class or interface");
        }

        ClassDecl cdecl = (ClassDecl) decl;

        cdecl.loadSource();

        return cdecl;
    }

    public static final PackageDecl SYSTEM_PACKAGE;
    public static final PackageDecl UNNAMED_PACKAGE;

    public static final ClassDecl OBJECT_DECL;
    public static final ClassDecl STRING_DECL;
    public static final ClassDecl CLASS_DECL; 
    public static final ClassDecl CLONEABLE_DECL;
  
    public static final TypeNameNode OBJECT_TYPE;
    public static final TypeNameNode STRING_TYPE;
    public static final TypeNameNode CLASS_TYPE;
    public static final TypeNameNode CLONEABLE_TYPE;
     
    public static final Environ    ARRAY_ENVIRON;
    public static final ClassDecl  ARRAY_CLASS_DECL;
    public static final FieldDecl  ARRAY_LENGTH_DECL;
    public static final MethodDecl ARRAY_CLONE_DECL;
                
    /** A Map containing values of CompileUnitNodes that have been partially 
     *  resolved (including those that are also fully resolved), indexed by the 
     *  absolute pathname of the source file.
     */
    public static final Map partiallyResolvedMap = new HashMap();
   
    /** A Map containing values of CompileUnitNodes that have been fully 
     *  resolved, indexed by the absolute pathname of the source file.
     */
    public static final Map fullyResolvedMap = new HashMap();
    
    static {
        SYSTEM_PACKAGE  = new PackageDecl("", null);
        UNNAMED_PACKAGE = new PackageDecl("", SYSTEM_PACKAGE);
        //UNNAMED_PACKAGE.setEnviron(new Environ());

        // dummy environment
        Environ env = new Environ();        

        NameNode javaLangName = (NameNode) makeNameNode("java.lang");
        importPackage(env, javaLangName);

        OBJECT_DECL = _requireClass(env, "Object");
        OBJECT_TYPE = OBJECT_DECL.getDefType();
        
        CLONEABLE_DECL = _requireClass(env, "Cloneable");
        CLONEABLE_TYPE = CLONEABLE_DECL.getDefType();        
        
        // virtual class for arrays
        
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
         TNLManip.cons(CLONEABLE_TYPE), arrayClassMembers, OBJECT_TYPE);
         
        CompileUnitNode arrayCompileUnitNode = new CompileUnitNode(
         javaLangName, new LinkedList(), TNLManip.cons(arrayClassNode));
        
        // resolve the names of the virtual class  
        load(arrayCompileUnitNode, false);

        ARRAY_CLASS_DECL = (ClassDecl) JavaDecl.getDecl((NamedNode) arrayClassNode);
        ARRAY_ENVIRON = ARRAY_CLASS_DECL.getEnviron();        
        ARRAY_LENGTH_DECL = (FieldDecl) JavaDecl.getDecl((NamedNode) arrayLengthNode);
        ARRAY_CLONE_DECL  = (MethodDecl) JavaDecl.getDecl((NamedNode) arrayCloneNode);
        
        STRING_DECL = _requireClass(env, "String");    
        STRING_TYPE = STRING_DECL.getDefType();
    
        CLASS_DECL  = _requireClass(env, "Class");
        CLASS_TYPE  = CLASS_DECL.getDefType();
    }
}
