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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

/** A code generator for each actor in an SDF system.
 *
 *  @author Jeff Tsay
 */
public class ActorCodeGenerator implements JavaStaticSemanticConstants {
    public ActorCodeGenerator(Entity entity) {
        _entity = entity;
    }
    
    public void generateCode(PerActorCodeGeneratorInfo actorInfo) {
     
        // finish filling in fields of actorInfo
        
        _makePortNameToPortMap(actorInfo);
        _makeParameterNameToTokenMap(actorInfo);
        
        // get the location of the source code for this actor                       
                        
        File sourceFile = SearchPath.NAMED_PATH.openSource(
         _entity.getClass().getName());                
         
        if (sourceFile == null) {
           ApplicationUtility.error("source code not found for " +
            "entity " + _entity);
        }
        
        String filename = sourceFile.toString();

        ApplicationUtility.trace("acg : parsing " + filename);        
        
        // parse each occurence of a file, because the AST will be modified below
        
        JavaParser p = new JavaParser();

        try {
          p.init(filename);
        } catch (Exception e) {
          ApplicationUtility.error("error opening " + filename + " : " + e);
        }

        System.out.println("parsing  " + filename);        

        p.parse();
        
        unitNode = p.getAST();
        
        // rename the class
        String actorName = actorInfo.actor.getName();
        String actorClassName = StringManip.unqualifiedPart(actorInfo.actor.getClass().getName());
        String newActorName = "CG_" +  actorClassName + "_" + actorName;
         
        HashMap renameMap = new HashMap();
                
        renameMap.put(actorClassName, newActorName);
                
        // get the part of the filename before the last '.'
        filename = StringManip.partBeforeLast(filename, '.');
        
        unitNode.setProperty(IDENT_KEY, newActorName); 
        
        System.out.println("changing classname to  " + newActorName);        
                
        unitNode.accept(new RenameJavaVisitor(), TNLManip.cons(renameMap));
        
        System.out.println("acg : loading " + filename);        
                
        unitNode = StaticResolution.resolvePass2(unitNode);
                        
        LinkedList visitorArgs = TNLManip.cons(actorInfo);
        
        System.out.println("acg : specializing tokens " + filename);        
                
        Map declToTypeMap = (Map) unitNode.accept(new SpecializeTokenVisitor(), visitorArgs);

        System.out.println("acg : changing types " + filename);        

        unitNode = (CompileUnitNode) unitNode.accept(new ChangeTypesVisitor(), 
         TNLManip.cons(declToTypeMap));
        
        // should redo resolution here
                        
        System.out.println("acg : transforming code " + filename);        
        
        unitNode = (CompileUnitNode) unitNode.accept(new ActorTransformerVisitor(),
         visitorArgs);
                  
        // regenerate the code          
                  
        String modifiedSourceCode = (String) unitNode.accept(
         new JavaCodeGenerator(), null);
         
        System.out.println(modifiedSourceCode);  
    }
    
    protected void _makePortNameToPortMap(PerActorCodeGeneratorInfo actorInfo) {

        Iterator portItr = _entity.portList().iterator();
           
        while (portItr.hasNext()) {              
           TypedIOPort port = (TypedIOPort) portItr.next();
           
           String portName = port.getName();
           
           actorInfo.portNameToPortMap.put(portName,  port);
        }
    }
    
    protected void _makeParameterNameToTokenMap(PerActorCodeGeneratorInfo actorInfo) {
        Iterator attributeItr = _entity.attributeList().iterator();
    
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
    

    
    protected final Entity _entity;
    protected CompileUnitNode unitNode;
}