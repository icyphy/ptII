/* A code generator for each actor in an SDF system.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

package ptolemy.domains.sdf.codegen;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A code generator for each actor in an SDF system.
 *
 *  @author Jeff Tsay
 */
public class ActorCodeGenerator implements JavaStaticSemanticConstants {

    public ActorCodeGenerator() {
    }
    
    public static String generateCode(PerActorCodeGeneratorInfo actorInfo) {
     
        Entity entity = actorInfo.actor;
        
        // finish filling in fields of actorInfo
        
        _makePortNameToPortMap(actorInfo);
        _makeParameterNameToTokenMap(actorInfo);
        
        // get the location of the source code for this actor                       
        
        Class actorClass = entity.getClass();
                                
        File sourceFile = SearchPath.NAMED_PATH.openSource(
         actorClass.getName());                
         
        if (sourceFile == null) {
           ApplicationUtility.error("source code not found for " +
            "entity " + entity);
        }
        
        String filename = sourceFile.toString();

        ApplicationUtility.trace("acg : parsing " + filename);        
        
        // parse each occurence of a file, because the AST will be modified below
        
        String actorName = entity.getName();
        
        // make a list of the compile unit node and compile unit nodes that 
        // contain superclasses
        List[] listArray = _makeUnitList(filename, 
         StringManip.unqualifiedPart(actorClass.getName()));
         
        List unitList = listArray[0];
        List classNameList = listArray[1]; 

        System.out.println("acg : specializing tokens " + filename);        
                
        Map declToTypeMap = SpecializeTokenVisitor.specializeTokens(unitList, actorInfo);

        System.out.println("acg : changing types " + filename);        
        TNLManip.traverseList(new ChangeTypesVisitor(), null, 
         TNLManip.cons(declToTypeMap), unitList);
        
        List renamedClassNameList = _renameUnitList(unitList, classNameList, actorName);
        
        _movePackage(unitList);
        
        _rewriteSources(unitList, renamedClassNameList);
        
        return "codegen." + (String) renamedClassNameList.get(0);    
    }
    
    public static void pass2(String sourceName, PerActorCodeGeneratorInfo actorInfo) {

        File sourceFile = SearchPath.NAMED_PATH.openSource(sourceName);
         
        if (sourceFile == null) {
           ApplicationUtility.error("regenerated source code not found for " +
            "entity " + actorInfo.actor + " in source file " + sourceFile);
        }

        String filename = sourceFile.toString();
        
        System.out.println("pass2() : sourceName = " + sourceName + ", filename = " + 
         filename);
                        
        List[] listArray = 
         _makeUnitList(filename, StringManip.unqualifiedPart(sourceName));
       
        List unitList = listArray[0];
        
        System.out.println("pass2() : unitList has length " + unitList.size());
                                                                                                
        Iterator unitItr = unitList.iterator();
        
        CompileUnitNode unitNode;
                                                                                                                                
        while (unitItr.hasNext()) {                        
           
           unitNode = (CompileUnitNode) unitItr.next();
           
           unitNode = StaticResolution.load(unitNode, 2);
        }
                                                        
        unitItr = unitList.iterator();
                                                                                                                        
        while (unitItr.hasNext()) {        
                                                   
           unitNode = (CompileUnitNode) unitItr.next();
                                                   
           // should redo resolution here
        
           // maybe it's ok not to redo field resolution, just invalidate types
           unitNode.accept(new RemovePropertyVisitor(), TNLManip.cons(TYPE_KEY));
                                        
           System.out.println("acg : transforming code " + filename);        
        
           unitNode = (CompileUnitNode) unitNode.accept(
            new ActorTransformerVisitor(actorInfo), null);
                  
           // regenerate the code          
                  
           String modifiedSourceCode = (String) unitNode.accept(
            new JavaCodeGenerator(), null);
         
           System.out.println(modifiedSourceCode);  
        }        
    }
    
    protected static void _makePortNameToPortMap(PerActorCodeGeneratorInfo actorInfo) {

        Iterator portItr = actorInfo.actor.portList().iterator();
           
        while (portItr.hasNext()) {              
           TypedIOPort port = (TypedIOPort) portItr.next();
           
           String portName = port.getName();
           
           actorInfo.portNameToPortMap.put(portName, port);
        }
    }
    
    protected static void _makeParameterNameToTokenMap(PerActorCodeGeneratorInfo actorInfo) {
        Iterator attributeItr = actorInfo.actor.attributeList().iterator();
    
        while (attributeItr.hasNext()) {
           Object attributeObj = attributeItr.next();
           if (attributeObj instanceof Parameter) {
              Parameter param = (Parameter) attributeObj;
              
              try {
                actorInfo.parameterNameToTokenMap.put(param.getName(), param.getToken());              
              } catch (IllegalActionException e) {
                ApplicationUtility.error("couldn't get token value for parameter " + param.getName());
              }
           }        
        }
    }
            
    protected CompileUnitNode parse(String filename) {
        JavaParser p = new JavaParser();

        try {
          p.init(filename);
        } catch (Exception e) {
          ApplicationUtility.error("error opening " + filename + " : " + e);
        }

        System.out.println("parsing  " + filename);        

        p.parse();
    
        return p.getAST();    
    }
    
    /** Make a list of CompileUnitNodes that contain the superclasses of
     *  the given className, while is found in the given fileName.
     *  The list should start from the argument class and go to 
     *  superclasses, until the super class is TypedAtomicActor or
     *  SDFAtomicActor. The CompileUnitNodes are cloned from those
     *  returned by StaticResolution so that they may be modified.
     */     
    protected static List[] _makeUnitList(String fileName, String className) {
        LinkedList retval = new LinkedList();
                    
        CompileUnitNode unitNode = 
         (CompileUnitNode) StaticResolution.load(fileName, 2).clone();
        
        retval.addLast(unitNode);
        
        LinkedList classNameList = new LinkedList();
        classNameList.addLast(className);       
        
        System.out.println("_makeUnitList() : className = " + className);
        
        boolean moreSuperClasses = true;
        do {
        
           ClassDecl superDecl = (ClassDecl)
            unitNode.accept(new FindSuperClassDecl(className), null);        
            
           if ((superDecl == ActorTransformerVisitor._SDF_ATOMIC_ACTOR_DECL) || 
               (superDecl == ActorTransformerVisitor._TYPED_ATOMIC_ACTOR_DECL) ||
               (superDecl == StaticResolution.OBJECT_DECL) || // just to be sure
               (superDecl == null)) {                         // just to be sure
               
              System.out.println("_makeUnitList() : super class = " + superDecl +
               " stopping.");
                              
              moreSuperClasses = false;                 
           } else {

              System.out.println("_makeUnitList() : super class = " + superDecl +
               " continuing.");           
           
              fileName = superDecl.fullName(File.separatorChar);
              
              // assume we are using the named package
              File file = SearchPath.NAMED_PATH.openSource(fileName);
              
              unitNode = (CompileUnitNode) StaticResolution.load(file, 2).clone();
              
              retval.addLast(unitNode);
              
              className = superDecl.getName();
              classNameList.addLast(className);      
           }                                     
        } while (moreSuperClasses);
        
        return new List[] { retval, classNameList };
    }
    
    protected static void _movePackage(List unitList) {
        Iterator unitItr = unitList.iterator();
        
        while (unitItr.hasNext()) {
           CompileUnitNode unitNode = (CompileUnitNode) unitItr.next();
           
           NameNode oldPackageName = (NameNode) unitNode.getPkg();

           // change the package           
           NameNode newPackageName = (NameNode) StaticResolution.makeNameNode("codegen");
           
           unitNode.setPkg(newPackageName);
           
           // add the old package to the list of import on demands
           List importList = unitNode.getImports();
           
           importList.add(new ImportOnDemandNode(oldPackageName));
           
           unitNode.setImports(importList);
        }    
    }
    
    protected static List _renameUnitList(List unitList, List classNameList, String actorName) {
        
        HashMap renameMap = new HashMap();
        Iterator classNameItr = classNameList.iterator();
        LinkedList renamedClassNameList = new LinkedList();
        
        //Iterator unitItr = unitList.iterator();
        
        while (classNameItr.hasNext()) {           
            String className = (String) classNameItr.next();                                                    
            String newClassName = "CG_" +  className + "_" + actorName;
                      
            System.out.println("changing classname from " + className + " to " + 
             newClassName);                                
             
            // add the mapping from the old class name to the new class name
            renameMap.put(className, newClassName);  
            
            renamedClassNameList.addLast(newClassName);
            
            // change the IDENT property of the CompileUnitNode
            //CompileUnitNode unitNode = (CompileUnitNode) unitItr.next();
            //unitNode.setProperty(IDENT_KEY, newClassName);                 
        }
        
        TNLManip.traverseList(new RenameJavaVisitor(), null, 
         TNLManip.cons(renameMap), unitList);            
         
        // invalidate DECL and ENVIRON properties, and load the nodes so
        // that the compiler does not attempt to          
        
        return renamedClassNameList;
    }
    
    protected static void _rewriteSources(List unitList, List classNameList) {
        System.out.println("classNameList = " + classNameList);
    
        Iterator unitItr = unitList.iterator();
        Iterator classNameItr = classNameList.iterator();
        JavaCodeGenerator jcg = new JavaCodeGenerator();
        
        while (unitItr.hasNext()) {
           CompileUnitNode unitNode = (CompileUnitNode) unitItr.next();

           String outCode = (String) unitNode.accept(jcg, null);
           
           String outFileName = "c:\\users\\ctsay\\ptII\\codegen\\" +  
            (String) classNameItr.next() + ".java";
            
           try {
             FileOutputStream outFile = new FileOutputStream(outFileName);
             outFile.write(outCode.getBytes());
             outFile.close();
           } catch (IOException e) {
             System.err.println("error opening/writing/closing output file "
              + outFileName);
             System.err.println(e.toString());
           }
        }                                                        
    }
    
    /** A JavaVisitor that finds the declaration of the superclass of a
     *  given type. The AST must have already gone through pass 1
     *  static resolution.
     */
    protected static class FindSuperClassDecl extends JavaVisitor {
        public FindSuperClassDecl(String className) {
            super(TM_CUSTOM);
            _className = className;           
        } 
        
        public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
            Iterator typeItr = node.getDefTypes().iterator(); 
            
            while (typeItr.hasNext()) {
               Object retval = ((TreeNode) typeItr.next()).accept(this, null);
            
               if ((retval != null) && (retval instanceof ClassDecl)) {
                  return retval;
               }
            }
            
            return null;
        }
        
        public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
            ClassDecl classDecl = (ClassDecl) JavaDecl.getDecl((NamedNode) node);
            
            if (classDecl.getName().equals(_className)) {
               return classDecl.getSuperClass();            
            }
            
            return null;
        }
        
        protected String _className;
    }                         
}