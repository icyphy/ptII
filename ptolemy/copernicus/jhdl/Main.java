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

package ptolemy.copernicus.jhdl;

import ptolemy.actor.*;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.ActorTransformer;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.moml.*;

import byucc.jhdl.Version;

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

//////////////////////////////////////////////////////////////////////////
//// JHDLTransformer
/**

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
*/

public class Main {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    public static void main(String[] args) throws Exception{
        if(args.length == 0) {
            System.out.println("Syntax: java ptolemy.apps.soot.demo.codegen.Main momlClass"
                    + " [soot options]");
            System.exit(0);
        }            
        
        String source = "<entity name=\"ToplevelModel\""
            + "class=\"" + args[0] + "\"/>\n";
        MoMLParser parser = new MoMLParser();
        _toplevel = (CompositeActor)parser.parse(source);        
        // FIXME: Temporary hack because cloning doesn't properly clone
        // type constraints.
        CompositeActor modelClass = (CompositeActor)
            parser._searchForClass(args[0], _toplevel.getMoMLInfo().source);
        if(modelClass != null) {
            _toplevel = modelClass;
        }                          

        // Initialize the model to ensure type resolution and scheduling
        // are done.
        try {
            Manager manager = new Manager(_toplevel.workspace(), "manager");
            _toplevel.setManager(manager);
            manager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not initialize composite actor");
        }

        // Process the global options.
        // FIXME!!
        String options = "deep targetPackage:ptolemy.apps.soot.demo.SimpleAdd.cg";

        // A Hack to ignore the class we specify on the command
	// line. This is a soot problem that requires this hack.
	// We will provide soot with java.lang.Object as its
	// only application class in Main. The first transformation
	// will ignore all application classes (i.e. set them to
	// library classes)
        Scene.v().getPack("wjtp").add(new Transform("wjtp.hack", 
                new IgnoreAllApplicationClasses(), ""));
 
        // Create instance classes for actors.
	// This transformer takes no input as far as soot is concerned
	// (i.e. no application classes) and creates application
	// classes from the model. 
        Scene.v().getPack("wjtp").add(new Transform("wjtp.at", 
                ActorTransformer.v(_toplevel), options));

        // Add a transformer to convert each actor class to JHDL.
        // "wjtp" means "whole java tranformation package"
        // This transformer is required to be a scene transformer,
        // and it is applied before body transformers.
        // "jhdl" is Michael Wirthlin's hardware design language.
        // "wjtp.jhdl" is the name of the phase.
        Scene.v().getPack("wjtp").add(new Transform("wjtp.jhdl", 
                JHDLTransformer.v(_toplevel), options));
        
        // Add transformers to do other passes.
        // "jtp" mean "java tranformation package.
        // These transformers are required to be a body transformer,
        // and are applied after scene transformers.

        // First pass: "cpaf" = "constant propagator and folder"
        Scene.v().getPack("jtp").add(new Transform("jtp.cpaf",
                ConstantPropagatorAndFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.cbf",
                ConditionalBranchFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.uce",
                UnreachableCodeEliminator.v()));
	Scene.v().getPack("jtp").add(new Transform("jtp.cp",
                  CopyPropagator.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.dae",
                DeadAssignmentEliminator.v()));
           
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then 
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";
        soot.Main.main(args);
    }
    
    // Get the method with the given name in the given class 
    // (or one of its super classes).
    public static SootMethod _searchForMethodByName(SootClass theClass, 
            String name) {
        while(theClass != null) {
            System.out.println("checking " + theClass + " for " + name);
            if(theClass.declaresMethodByName(name)) {
                return theClass.getMethodByName(name);
            }
            theClass = theClass.getSuperclass();
            theClass.setLibraryClass();
        }
        throw new RuntimeException("Method " + name + " not found in class "
                + theClass);
    }
    
    // Get the method in the given class that has the given name and will
    // accept the given argument list.
    public static SootMethod _getMatchingMethod(SootClass theClass,
            String name, List args) {
        boolean found = false;
        SootMethod foundMethod = null;
        
        Iterator methods = theClass.getMethods().iterator();

        while(methods.hasNext())
            {
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
                            // expensive.
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

    private static class IgnoreAllApplicationClasses extends SceneTransformer {
        /** Transform the Scene according to the information specified
         *  in the model for this transform.
         *  @param phaseName The phase this transform is operating under.
         *  @param options The options to apply. 
         */
        protected void internalTransform(String phaseName, Map options) {
            for(Iterator classes = Scene.v().getApplicationClasses().snapshotIterator();
                classes.hasNext();) {
                ((SootClass)classes.next()).setLibraryClass();
            }
        }
    }

    private static CompositeActor _toplevel;
}
