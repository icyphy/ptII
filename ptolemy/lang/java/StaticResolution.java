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

import ptolemy.lang.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Iterator;


public class StaticResolution {

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
            (JavaDecl.CG_FIELD | JavaDecl.CG_METHOD | JavaDecl.CG_LOCALVAR |
             JavaDecl.CG_FORMAL | JavaDecl.CG_USERTYPE)) != 0) {
          possibles = env.lookupFirst(name.getIdent(), categories);
       } else {
          //ApplicationUtility.trace("looking up package");
          possibles = ((Environ) SYSTEM_PACKAGE.getEnviron())
           .lookupFirst(name.getIdent(), categories);
       }
    } else {
       int newCategories = 0;
       if ((categories &
           (JavaDecl.CG_USERTYPE | JavaDecl.CG_PACKAGE)) != 0) {
          newCategories |= JavaDecl.CG_PACKAGE;
       }

       // for inner classes
       if ((categories & JavaDecl.CG_USERTYPE) != 0) {
          newCategories |= JavaDecl.CG_USERTYPE;
       }

       if ((categories & (JavaDecl.CG_FIELD | JavaDecl.CG_METHOD)) != 0) {
          newCategories |= (JavaDecl.CG_USERTYPE | JavaDecl.CG_FIELD |
                            JavaDecl.CG_LOCALVAR | JavaDecl.CG_FORMAL);
       }

       name.setQualifier(
        resolveAName((NameNode) name.getQualifier(), env, currentClass,
         currentPackage, newCategories));

       JavaDecl container = JavaDecl.getDecl(name.getQualifier());

       if (container.hasEnviron()) {
          possibles = container.getEnviron().
           lookupFirstProper(name.getIdent(), categories);
       } else if (container instanceof TypedDecl) {
          TypedDecl typedContainer = (TypedDecl) container;
          TypeNode type = typedContainer.getType();
          if (type instanceof PrimitiveTypeNode) {
	          ApplicationUtility.error("cannot select " + name.getIdent() +
              " from non-reference type represented by " +
              type.getClass().getName());
          } else {
             Environ e = JavaDecl.getDecl(type).getEnviron();
             possibles = e.lookupFirstProper(name.getIdent(),
              categories & (JavaDecl.CG_FIELD | JavaDecl.CG_METHOD));
          }
       }
    }

    if (!possibles.hasNext()) {
       ApplicationUtility.error(name.getIdent() + " undefined in environ " +
        env.toString());
    }

    JavaDecl d = (JavaDecl) possibles.head();
    name.setProperty("decl", d);

    ApplicationUtility.trace("findPossibles for " + nameString(name) + " ok");

    return possibles;
  }

  public static final TreeNode resolveAName(NameNode name, Environ env,
   TypeNameNode currentClass, JavaDecl currentPackage,
   int categories) {

    ApplicationUtility.trace("resolveAName : " + nameString(name));

    if (name.hasProperty("decl")) {
       ApplicationUtility.trace("decl already defined");
       return name;
    }

    EnvironIter possibles = findPossibles(name, env, currentClass, 
     currentPackage, categories);

    if (possibles.moreThanOne() && ((categories & JavaDecl.CG_METHOD) == 0)) {
       throw new RuntimeException("ambiguous reference to " + name.getIdent());
    }

    JavaDecl d = (JavaDecl) name.getDefinedProperty("decl");

    switch (d.category) {

    case JavaDecl.CG_CLASS:
    case JavaDecl.CG_INTERFACE:
    {
      ClassDecl cd = (ClassDecl) d;
      cd.loadSource();
      int modifiers = cd.getModifiers();
      boolean isPublic = ((modifiers & Modifier.PUBLIC_MOD) != 0);
      boolean isStatic = ((modifiers & Modifier.STATIC_MOD) != 0);

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

    case JavaDecl.CG_FIELD:
    case JavaDecl.CG_METHOD:
    {
      TreeNode res;
      TreeNode qualifier = name.getQualifier();
      MemberDecl md = (MemberDecl) d;
      if (qualifier == AbsentTreeNode.instance) {
         if ((md.getModifiers() & Modifier.STATIC_MOD) != 0) {
            res = new TypeFieldAccessNode(currentClass, name);
         } else {
            res = new ThisFieldAccessNode(currentClass, name);
         }
      } else if ((JavaDecl.getDecl(qualifier).category & JavaDecl.CG_USERTYPE) != 0) {      
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

    case JavaDecl.CG_LOCALVAR:
    case JavaDecl.CG_FORMAL:
    //ApplicationUtility.trace("resolveAName " + nameString(name) +
    // " (local var or formal param) ok");
    return new ObjectNode(name);

    default:
    return name;
    }
  }

  public static MethodDecl resolveCall(EnvironIter methods, LinkedList args) {

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
   
  public static final void importOnDemand(CompileUnitNode node,
   PackageDecl importedPackage) {

    ApplicationUtility.trace("importOnDemand : importing " +
     importedPackage.toString());
     
    LinkedList importedPackages =
     (LinkedList) node.getDefinedProperty("importedPackages");

    // ignore duplicate imports
    if (importedPackages.contains(importedPackage)) {
       ApplicationUtility.warn("importOnDemand : ignoring duplicated package "
        + importedPackage.toString());
       return;
    }

    importedPackages.addLast(importedPackage);

    Environ myEnviron = (Environ) node.getDefinedProperty(StaticResolution.ENVIRON_KEY);

    Environ importEnv = myEnviron.parent().parent();

    Environ pkgEnv = importedPackage.getEnviron();

    Iterator envItr = pkgEnv.allProperDecls();

    while (envItr.hasNext()) {
      JavaDecl type = (JavaDecl) envItr.next();

      if (type.category != JavaDecl.CG_PACKAGE) {
         ApplicationUtility.trace("importOnDemand: adding " + type.toString());
         importEnv.add(type); // conflicts appear on use only
      }
    }

    ApplicationUtility.trace("importOnDemand : finished" +
     importedPackage.toString());
  }

  // we can get rid of this by added java.lang to the import list
  public static final void importOnDemand(CompileUnitNode node,
   String[] qualName) {
    NameNode name = (NameNode) makeNameNode(qualName);

    resolveAName(name, SYSTEM_PACKAGE.getEnviron(), null, null,
     JavaDecl.CG_PACKAGE);

    importOnDemand(node, (PackageDecl) name.getDefinedProperty("decl"));
  }

  public static final TreeNode makeNameNode(String[] qualName) {
    TreeNode retval = AbsentTreeNode.instance;

    for (int i = 0; i < qualName.length; i++) {
      retval = new NameNode(retval, qualName[i]);
    }

    return retval;
  }

  public static void buildEnvironments() {
    Iterator itr = recentFiles.iterator();
    LinkedList list2 = new LinkedList();

    while (!recentFiles.isEmpty()) {
      CompileUnitNode cun = (CompileUnitNode) recentFiles.removeFirst();
      list2.addLast(cun);

      //cun.accept(new ResolveClassVisitor(), null);
    }

    while (!list2.isEmpty()) {
      CompileUnitNode cun = (CompileUnitNode) list2.removeFirst();

      //cun.accept(new ResolveInheritanceVisitor(), null);
    }

    //recentFiles.clear();
  }

  public static CompileUnitNode parse(File file) {

    parser p = new parser();

    try {
      p.init(file.toString());
    } catch (Exception e) {
      ApplicationUtility.error("error opening " + file.toString() + " : " +
       e.toString());
    }

    p.yyparse();

    CompileUnitNode loadedAST = p.getAST();

    // need to run package resolution??
    //

    return loadedAST;
  }

  public static void load(String name, boolean primary) {
    load(name, new File(name), primary);
  }

  public static void load(String name, File file, boolean primary) {
    System.out.println(">loading " + name);
  
    CompileUnitNode loadedAST = null;
    // look for an incomplete parse tree in the lazily resolved list
/*    CompileUnitNode foundCun = null;

    Iterator lazyItr = lazilyResolvedFiles.iterator();
    while ((foundCun == null) && lazyItr.hasNext()) {
       CompileUnitNode cun = (CompileUnitNode) lazyItr.next();
       String id = StringManip.rawFilename(
        (String) cun.getDefinedProperty("ident"));

       if (id.equals(StringManip.rawFilename(name))) {
          foundCun = cun;
       }
    }

    if (foundCun != null) {
       if (!primary) {
          // already lazily resolved this file
          return;
       }

       foundCun.setProperty("fullResolve", Boolean.TRUE);

       lazilyResolvedFiles.remove(foundCun);
       fullyResolvedFiles.addLast(foundCun);

       // run package resolution here for now
       foundCun.accept(new PackageResolutionVisitor(), null);

       recentFiles.addLast(foundCun);
       unresolvedFiles.addLast(foundCun);

       return;
    }

    // look for an complete parse tree in the fully resolved list
    foundCun = null;
    Iterator fullItr = fullyResolvedFiles.iterator();
    while ((foundCun == null) && fullItr.hasNext()) {
       CompileUnitNode cun = (CompileUnitNode) fullItr.next();
       String id = StringManip.rawFilename(
        (String) cun.getDefinedProperty("ident"));

       if (id.equals(StringManip.rawFilename(name))) {
          foundCun = cun;
       }
    }

    if (foundCun != null) {
       return; // already resolved file
    }

    if (!file.isFile()) {
       ApplicationUtility.error("Couldn't load " + name);
    } */

    loadedAST = parse(file);

    if (loadedAST == null) {
       ApplicationUtility.error("Couldn't load " + name);
    }

    loadedAST.setProperty(IDENT_KEY, name);
    loadedAST.setProperty("fullResolve", new Boolean(primary));
    //loadedAST.setProperty("fullResolve", new Boolean(true));

    // run package resolution here for now
    loadedAST.accept(new PackageResolutionVisitor(), null);    
    loadedAST.accept(new ResolveClassVisitor(), null);
    loadedAST.accept(new ResolveInheritanceVisitor(), null);

 	 allFiles.addLast(loadedAST);

    recentFiles.addLast(loadedAST);
    
    if (primary) {
       unresolvedFiles.addLast(loadedAST);
    }
  }

  public static void declResolution() {
    buildEnvironments();

    LinkedList filesToResolve = new LinkedList(); 

    while (!unresolvedFiles.isEmpty()) {

      CompileUnitNode unitNode = (CompileUnitNode) unresolvedFiles.removeFirst();
      
      ApplicationUtility.enableTrace = true;
      ApplicationUtility.trace("resolveName on " +
       (String) unitNode.getDefinedProperty(IDENT_KEY));
      ApplicationUtility.enableTrace = false;
          
      unitNode.accept(new ResolveNameVisitor(), null);     
      
      filesToResolve.addLast(unitNode);
    }
      
    while (!filesToResolve.isEmpty()) {
        CompileUnitNode unitNode = (CompileUnitNode) filesToResolve.removeFirst();

        ApplicationUtility.trace("resolveField on " +       
         (String) unitNode.getDefinedProperty(IDENT_KEY));        
        
        unitNode.accept(new ResolveFieldVisitor(), null);                    
        
        fullyResolvedFiles.addLast(unitNode);
    }
  }

  protected static final void _importPackage(Environ env, NameNode name) {

    resolveAName(name, SYSTEM_PACKAGE.getEnviron(), null, null,
     JavaDecl.CG_PACKAGE);

    PackageDecl decl = (PackageDecl) name.getDefinedProperty("decl");

    Environ packageEnv = decl.getEnviron();

    Iterator declItr = packageEnv.allProperDecls();

    while (declItr.hasNext()) {
       JavaDecl type = (JavaDecl) declItr.next();
       if (type.category != JavaDecl.CG_PACKAGE) {
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

    if ((decl.category & (JavaDecl.CG_CLASS | JavaDecl.CG_INTERFACE)) == 0) {
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
 
  public static final LinkedList allFiles = new LinkedList();
  public static final LinkedList recentFiles = new LinkedList();
  public static final LinkedList unresolvedFiles = new LinkedList();

  public static final LinkedList lazilyResolvedFiles = new LinkedList();
  public static final LinkedList fullyResolvedFiles = new LinkedList();

  //public static CompileUnitNode loadedAST = null;

  // keys for property map
  public static final Integer IDENT_KEY = new Integer(0);

  public static final Integer ENVIRON_KEY = new Integer(2);
    
  static {
    SYSTEM_PACKAGE  = new PackageDecl("", null);
    UNNAMED_PACKAGE = new PackageDecl("", null);
    //UNNAMED_PACKAGE.setEnviron(new Environ());

    Environ env = new Environ();

    _importPackage(env,
     new NameNode(new NameNode(AbsentTreeNode.instance, "java"), "lang"));

    OBJECT_DECL = _requireClass(env, "Object");
    STRING_DECL = _requireClass(env, "String");    
    CLASS_DECL  = _requireClass(env, "Class");
    
    //buildEnvironments();
  }
}
