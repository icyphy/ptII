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

//////////////////////////////////////////////////////////////////////////
//// StaticResolution
/**
 *  Methods to aid in the static resolution of names and types in a Java
 *  program. The code was mostly converted from the Titanium project.
 *
 *  @author Jeff Tsay
 */
public class StaticResolution implements JavaStaticSemanticConstants {

    public static final void buildEnvironments() {
        
        Iterator nodeItr = pass0ResolvedList.iterator();
        
        while (nodeItr.hasNext()) {
           CompileUnitNode node = (CompileUnitNode) nodeItr.next();
           node.accept(new ResolveClassVisitor(), null);              
        }
           
        nodeItr = pass0ResolvedList.iterator();
        
        while (nodeItr.hasNext()) {
           CompileUnitNode node = (CompileUnitNode) nodeItr.next();
           node.accept(new ResolveInheritanceVisitor(_defaultTypePolicy), null);              
              
           String filename = (String) node.getDefinedProperty(IDENT_KEY);
              
           if (filename != null) {
              allPass1ResolvedMap.put(filename, node);
           }
        }

        pass0ResolvedList.clear();                    
    }

    public static TypeVisitor getDefaultTypeVisitor() {
        return _defaultTypeVisitor;
    }

    public static PackageDecl importPackage(Environ env, NameNode name) {
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
        
        return decl;
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
    public static boolean invalidateCompileUnit(String canonicalFilename, int pass) {
        if ((pass < 0) || (pass > 2)) {
           throw new IllegalArgumentException("invalid pass number : " + pass);
        }
        
        String noExtensionFilename = 
         StringManip.partAfterLast(canonicalFilename, '.');
        
        boolean found = false;
        
        if (pass == 2) {
           found = (allPass2ResolvedMap.remove(noExtensionFilename) != null);        
        } 
        
        if (found || (pass == 1)) {
           found = (allPass1ResolvedMap.remove(noExtensionFilename) != null);        
        }
    
        if (found || (pass == 0)) {
           found = (allPass0ResolvedMap.remove(noExtensionFilename) != null);        
        }
        
        return found;
    }
       
     
     

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
               newCategories |= (categories & CG_USERTYPE);
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
	             ApplicationUtility.error("cannot select " + name.getIdent() +
                  " from non-reference type represented by " + type);
              } else if (type instanceof ArrayTypeNode) {
                 possibles = ARRAY_CLASS_DECL.getEnviron().lookupFirstProper(name.getIdent(),
                  categories & (CG_FIELD | CG_METHOD));                  
              } else {
                 // what is this for ???
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

        // ApplicationUtility.trace("findPossibles for " + nameString(name) + " ok");

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
                res = new TypeFieldAccessNode(name, currentClass);
             } else {
                res = new ThisFieldAccessNode(name);
                res.setProperty(THIS_CLASS_KEY, currentClass);
                // FIXME what's wrong with a normal constructor
             }
          } else if ((JavaDecl.getDecl(qualifier).category & CG_USERTYPE) != 0) {      
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
     *  @throws NoSuchElementException if the declaration cannot be found.     
     */
    public static JavaDecl findDecl(CompileUnitNode compileUnit,
     String qualifiedName, int category) {
        Environ env = (Environ) compileUnit.getDefinedProperty(ENVIRON_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);
        
        // is this really necessary?
        PackageDecl pkgDecl = 
         (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);
        
        EnvironIter environIter = findPossibles(nameNode, env, null, 
         pkgDecl, category);
              
        // no check for multiple matches (this should be handled by
        // the static resolution passes)
        return (JavaDecl) environIter.nextDecl();                  
    }

    /** Return the method or constructor declaration with the given qualified name, 
     *  category, and method arguments, which must be defined in the given 
     *  CompileUnitNode, which must have gone through pass 1 static
     *  resolution.  
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
     *  @throws NoSuchElementException if the declaration cannot be found.
     */
    public static MemberDecl findInvokableDecl(CompileUnitNode compileUnit,
     String qualifiedName, int category, List methodArgs, 
     TypeVisitor typeVisitor) {
        Environ env = (Environ) compileUnit.getDefinedProperty(ENVIRON_KEY);
        NameNode nameNode = (NameNode) makeNameNode(qualifiedName);
        
        // is this really necessary?
        PackageDecl pkgDecl = 
         (PackageDecl) compileUnit.getDefinedProperty(PACKAGE_KEY);
        
        EnvironIter environIter = findPossibles(nameNode, env, null, 
         pkgDecl, category);
         
        ResolveFieldVisitor resolveFieldVisitor = 
         new ResolveFieldVisitor(typeVisitor);
         
        // no check for multiple matches (this should be handled by
        // the static resolution passes)         
        return (MemberDecl) resolveFieldVisitor.resolveCall(environIter, methodArgs);       
    }
    
    /** Load the source file with the given filename. The filename may be
     *  relative or absolute. If primary is true, do full resolution of the 
     *  source. Otherwise do partial resolution only.
     */ 
    public static CompileUnitNode load(String filename, int pass) {    
        return load(new File(filename), pass);
    }

    public static CompileUnitNode load(File file, int pass) {        
        try {
          return loadCanonical(file.getCanonicalPath(), pass);
        } catch (IOException ioe) {
          ApplicationUtility.error(ioe.toString());
        } 
        return null; 
    }

    /** Load the source file with the given canonical filename. If
     *  primary is true, do full resolution of the source. Otherwise
     *  do partial resolution only.
     */ 
    public static CompileUnitNode loadCanonical(String filename, int pass) {        
        ApplicationUtility.trace(">loading " + filename);
  
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
        
    /** Load a CompileUnitNode that has been parsed. Go through all passes
     *  of static resolution up to the argument pass number. 
     *
     */
    public static CompileUnitNode load(CompileUnitNode node, int pass) {                                                                        
        switch (pass) {
          case 0:
          return resolvePass0(node);
              
          case 1:
          node = resolvePass0(node);          
          return resolvePass1(node);              
           
          case 2:                    
          node = resolvePass0(node);                    
          node = resolvePass1(node);          
          return resolvePass2(node);              
          
          default:
          throw new IllegalArgumentException("invalid pass number (" + pass + ")");
        }                 
    }
    
    /** Do pass 0 resolution on a CompileUnitNode that just been built by
     *  the parser. Return the resolved node. If a source file with the same 
     *  canonical filename has already been pass 0 resolved, return the previous 
     *  node. 
     */
    public static CompileUnitNode resolvePass0(CompileUnitNode node) {    
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

    /** Do pass 1 resolution on a CompileUnitNode. If a source file with the same 
     *  canonical filename has already been pass 1 resolved, return the previous 
     *  node. 
     */
    public static CompileUnitNode resolvePass1(CompileUnitNode node) {    
    
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

        
    /** Do pass 2 resolution on a CompileUnitNode that has already been pass 1 
     *  resolved. Return the resolved node. If a source file with the same 
     *  canonical filename has already been pass 2 resolved, return the previous 
     *  node. 
     */
    public static CompileUnitNode resolvePass2(CompileUnitNode node) {        
    
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

    public static void setDefaultTypeVisitor(TypeVisitor typeVisitor) {
        _defaultTypeVisitor = typeVisitor;
        _defaultTypePolicy = typeVisitor.typePolicy();
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
    public static TypePolicy _defaultTypePolicy = _defaultTypeVisitor.typePolicy();
        
    static {
        SYSTEM_PACKAGE  = new PackageDecl("", null);
        UNNAMED_PACKAGE = new PackageDecl("", SYSTEM_PACKAGE);

        // dummy environment
        Environ env = new Environ();        

        ApplicationUtility.trace("--- loading java.lang package ---");

        NameNode javaLangName = (NameNode) makeNameNode("java.lang");
        JAVA_LANG_PACKAGE = importPackage(env, javaLangName);

        ApplicationUtility.trace("--- require class on Object ---");

        OBJECT_DECL = _requireClass(env, "Object");
        
        ApplicationUtility.trace("--- done require class on Object ---");
        
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
         
        // give the CompileUnitNode a dummy name so it can be retrieved 
        arrayCompileUnitNode.setProperty(IDENT_KEY, "<array>");
        
        // resolve the names of the virtual class  
        load(arrayCompileUnitNode, 0);

        ARRAY_CLASS_DECL = (ClassDecl) JavaDecl.getDecl((NamedNode) arrayClassNode);
        ARRAY_LENGTH_DECL = (FieldDecl) JavaDecl.getDecl((NamedNode) arrayLengthNode);
        ARRAY_CLONE_DECL  = (MethodDecl) JavaDecl.getDecl((NamedNode) arrayCloneNode);
        
        STRING_DECL = _requireClass(env, "String");    
        STRING_TYPE = STRING_DECL.getDefType();
    
        CLASS_DECL  = _requireClass(env, "Class");
        CLASS_TYPE  = CLASS_DECL.getDefType();

        ApplicationUtility.trace("--- 1st buildEnvironments ---");
        
        buildEnvironments();
    }
}
