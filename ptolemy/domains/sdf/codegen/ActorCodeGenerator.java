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
import java.util.Map;

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
public class ActorCodeGenerator {
    public ActorCodeGenerator(Entity entity) {
        _entity = entity;
    }
    
    public void generateCode(PerActorCodeGeneratorInfo actorInfo) {
    
        // get the location of the source code for this actor                       
                        
        File sourceFile = SearchPath.NAMED_PATH.openSource(
         _entity.getClass().getName());                
         
        if (sourceFile == null) {
           ApplicationUtility.error("source code not found for " +
            "entity " + _entity);
        }

        System.out.println("acg : loading " + sourceFile.toString());
        
        unitNode = StaticResolution.load(sourceFile, 2);     
        
        LinkedList visitorArgs = TNLManip.cons(actorInfo);
        
        Map declToTypeMap = (Map) unitNode.accept(new SpecializeTokenVisitor(), visitorArgs);

        unitNode = (CompileUnitNode) unitNode.accept(new ChangeTypesVisitor(), 
         TNLManip.cons(declToTypeMap));
        
        unitNode = (CompileUnitNode) unitNode.accept(new ActorTransformerVisitor(),
         visitorArgs);
         
         
        String modifiedSourceCode = (String) unitNode.accept(
         new JavaCodeGenerator(), null);
         
        System.out.println(modifiedSourceCode);  
    }
    
    protected final Entity _entity;
    protected CompileUnitNode unitNode;
}