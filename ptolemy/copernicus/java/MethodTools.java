/* Transform Actors using Soot

 Copyright (c) 2001 The Regents of the University of California.
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

package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;


//////////////////////////////////////////////////////////////////////////
//// MethodTools
/**

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class MethodTools {    
    
    // Get the method with the given name in the given class 
    // (or one of its super classes).
    public static SootMethod searchForMethodByName(SootClass theClass, 
            String name) {
        while(theClass != null) {
            if(theClass.declaresMethodByName(name)) {
                return theClass.getMethodByName(name);
            }
            theClass = theClass.getSuperclass();
        }
        throw new RuntimeException("Method " + name + " not found in class "
                + theClass);
    }
    
    // Get the method in the given class that has the given name and will
    // accept the given argument list.
    public static SootMethod getMatchingMethod(SootClass theClass,
            String name, List args) {
        boolean found = false;
        SootMethod foundMethod = null;
        
        Iterator methods = theClass.getMethods().iterator();

        while(methods.hasNext()) {
            SootMethod method = (SootMethod) methods.next();
            
            if(method.getName().equals(name) &&
                    args.size() == method.getParameterCount()) {
                Iterator parameterTypes =
                    method.getParameterTypes().iterator();
                Iterator arguments = args.iterator();
                boolean isEqual = true;
                while(parameterTypes.hasNext()) {
                    Type parameterType = (Type)parameterTypes.next();
                    Local argument = (Local)arguments.next();
                    Type argumentType = argument.getType();
                    if(argumentType != parameterType) {
                        // This is inefficient.  Full type merging is 
                        // expensive and unnecessary.
                        isEqual = (parameterType == argumentType.merge(
                                parameterType, Scene.v()));
                    }
                    if(!isEqual) break;
                }
                if(isEqual && found)
                    throw new RuntimeException("ambiguous method");
                else {                    
                    found = true;
                    foundMethod = method;
                    break;
                }
            }
        }
        return foundMethod;
    }
}
    











