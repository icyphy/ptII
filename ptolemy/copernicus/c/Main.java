/*
 Transform Actors using Soot and generate C code.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

package ptolemy.copernicus.c;

// FIXME: clean up import list.
import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.java.ActorTransformer;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.ImprovedDeadAssignmentEliminator;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.copernicus.java.CommandLineTransformer;
//FIXME
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.java.InlineDirectorTransformer;
import ptolemy.copernicus.java.ModelTransformer;



import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.jimple.toolkits.scalar.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/** Read in a MoML model, generate .c files with very few
    dependencies on Ptolemy II.

    @author Shuvra S. Bhattacharyya, Michael Wirthlin, Stephen Neuendorffer,
    Edward A. Lee, Christopher Hylands
    @version $Id$
*/

public class Main extends KernelMain {

    /** Read in a MoML model and generate Java classes for that model.
     *  @param args An array of Strings that control the transformation
     */
    public Main(String [] args) throws IllegalActionException {
        // args[0] contains the MoML class name.
        super(args[0]);
    }

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
	super.addTransforms();

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
	// -p wjtp.watchDog time:30000
        Scene.v().getPack("wjtp").add(new Transform("wjtp.watchDog",
                                              WatchDogTimer.v()));

        // Sanitize names of objects in the model.
        // We change the names to all be valid java identifiers
        // so that we can
        //      Scene.v().getPack("wjtp").add(new Transform("wjtp.ns",
        //         NameSanitizer.v(_toplevel)));

        // Create a class for the composite actor of the model, and
        // additional classes for all actors (both composite and
        // atomic) used by the model.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.mt", ModelTransformer.v(_toplevel)));
        
   
        // Inline the director into the composite actor.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.idt",
                        InlineDirectorTransformer.v(_toplevel)));
        
        // Add a command line interface (i.e. Main)
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.clt",
                        CommandLineTransformer.v(_toplevel)));

	// Generate the makefile files in outDir
        Scene.v().getPack("wjtp").add(new Transform("wjtp.makefileWriter",
                MakefileWriter.v(_toplevel)));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot1", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot1", ClassWriter.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffet",
                         FieldsForEntitiesTransformer.v(_toplevel)));
        
        // Infer the types of locals again, since replacing attributes
        // depends on the types of fields
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        
        // In each actor and composite actor, ensure that there
        // is a field for every attribute, and replace calls
        // to getAttribute with references to those fields.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffat",
                        FieldsForAttributesTransformer.v(_toplevel)));
        
        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffpt",
                        FieldsForPortsTransformer.v(_toplevel)));

        _addStandardOptimizations(Scene.v().getPack("wjtp"));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ls",
                        new TransformerAdapter(LocalSplitter.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ib",
                        InvocationBinder.v()));
        
        // Set about removing reference to attributes and parameters.
        // Anywhere where a method is called on an attribute or
        // parameter, replace the method call with the return value
        // of the method.  This is possible, since after
        // initialization attribute values are assumed not to
        // change.  (Note: There are certain cases where this is
        // not true, i.e. the expression actor.  Those will be
        // specially handled before this point, or we should detect
        // assignments to attributes and handle them differently.)
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.iat",
                        InlineParameterTransformer.v(_toplevel)));
      
        // Remove equality checks, which arise from inlining attributeChanged.
       //  Scene.v().getPack("wjtp").add(
//                 new Transform("wjtp.ta",
//                         new TransformerAdapter(TypeAssigner.v())));
//         Scene.v().getPack("wjtp").add(
//                 new Transform("wjtp.nee",
//                         NamedObjEqualityEliminator.v(_toplevel)));

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.itt",
                        InlineTokenTransformer.v(_toplevel)));
         
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ls",
                        new TransformerAdapter(LocalSplitter.v())));
        
        // While we still have references to ports, use the
        // resolved types of the ports and run a typing
        // algorithm to specialize the types of domain
        // polymorphic actors.  After this step, no
        // uninstantiable types should remain.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.tie",
                        new TransformerAdapter(
                                TokenInstanceofEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.cp",
                        new TransformerAdapter(CopyPropagator.v())));
        
        //       _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot2", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot2", ClassWriter.v()));
        
        // Set about removing references to ports.
        // Anywhere where a method is called on a port, replace the
        // method call with an inlined version of the method.
        // Currently this only deals with SDF, and turns
        // all gets and puts into reads and writes from circular
        // buffers.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ipt",
                        InlinePortTransformer.v(_toplevel)));
        
        // This appears again because Inlining the parameters
        // also inlines calls to connectionsChanged, which by default
        // calls getDirector...  This transformer removes
        // these method calls.
        // FIXME: This should be done in a better way...
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffat",
                        FieldsForAttributesTransformer.v(_toplevel)));

        // Deal with any more statically analyzeable token
        // references that were created.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.itt",
                        InlineTokenTransformer.v(_toplevel)));
        
        //Scene.v().getPack("wjtp").add(new Transform("wjtp.ta",
        //        new TransformerAdapter(TypeAssigner.v())));
        // Scene.v().getPack("wjtp").add(new Transform("wjtp.ibg",
        //        InvokeGraphBuilder.v()));
        // Scene.v().getPack("wjtp").add(new Transform("wjtp.si",
        //        StaticInliner.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot3", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot3", ClassWriter.v()));
           
        // Unroll loops with constant loop bounds.
        //Scene.v().getPack("jtp").add(new Transform("jtp.clu",
        //        ConstantLoopUnroller.v()));
        
        //     _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ls",
                        new TransformerAdapter(LocalSplitter.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ib",
                        InvocationBinder.v()));
          
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.nee",
                        NamedObjEqualityEliminator.v(_toplevel)));

        // Remove casts and instanceof Checks.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.cie",
                        new TransformerAdapter(
                                CastAndInstanceofEliminator.v())));

        _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
             
        // Remove Unreachable methods.  This happens BEFORE NamedObjElimination
        // so that we don't have to pick between multiple constructors, if
        // there are more than one.  I'm lazy and instead of trying to pick
        // one, lets use the only one that is reachable. 
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.umr", UnreachableMethodRemover.v()));

        
        // Remove references to named objects.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(_toplevel)));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ls",
                        new TransformerAdapter(LocalSplitter.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.cie",
                        new TransformerAdapter(
                                CastAndInstanceofEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ib",
                        InvocationBinder.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.noe",
                        NamedObjEliminator.v(_toplevel)));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.umr", UnreachableMethodRemover.v()));
        
        // Some cleanup.
        // Remove object creations that are now dead (i.e. aren't used
        // and have no side effects).  This currently only deals with
        // Token and Type constructors, since we know that these will
        // have no interesting side effects.  More complex analysis
        // is possible here, but not likely worth it.
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.dae",
                        new TransformerAdapter(
                                ImprovedDeadAssignmentEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.dae",
                        new TransformerAdapter(
                                ImprovedDeadAssignmentEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot4", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot4", ClassWriter.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ttn",
                        TokenToNativeTransformer.v(_toplevel)));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ufr", 
                        UnusedFieldRemover.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.smr",
                        SideEffectFreeInvocationRemover.v()));
        
        
        // Remove references to named objects.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(_toplevel)));
         
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
       _addStandardOptimizations(Scene.v().getPack("wjtp"));

   /*
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ts",
                        TypeSpecializer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.tie",
                        new TransformerAdapter(
                                TokenInstanceofEliminator.v())));
        
        // Some cleanup.
        // Remove object creations that are now dead (i.e. aren't used
        // and have no side effects).  This currently only deals with
        // Token and Type constructors, since we know that these will
        // have no interesting side effects.  More complex analysis
        // is possible here, but not likely worth it.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.dae",
                        new TransformerAdapter(
                                ImprovedDeadAssignmentEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.dae",
                        new TransformerAdapter(
                                ImprovedDeadAssignmentEliminator.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));

        // Remove unnecessary assignments using alias analysis.
        // This catches assignments to fields which other assignments miss.
        //  Scene.v().getPack("wjtp").add(
        //                 new Transform("wjtp.aae",
        //                         new TransformerAdapter(
        //                                 AliasAssignmentEliminator.v())));
         

        // Remove other useless getFoo() methods.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.smr",
                        SideEffectFreeInvocationRemover.v()));

        // Run the standard soot optimizations.  We explicitly specify
        // this instead of using soot's -O flag so that we can
        // have access to the result.
        _addStandardOptimizations(Scene.v().getPack("wjtp"));

        // Remove Unreachable methods.  This happens BEFORE NamedObjElimination
        // so that we don't have to pick between multiple constructors, if
        // there are more than one.  I'm lazy and instead of trying to pick
        // one, lets use the only one that is reachable. 
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.umr", UnreachableMethodRemover.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.nee",
                        NamedObjEqualityEliminator.v(_toplevel)));
        
        // Remove references to named objects.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(_toplevel)));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.noe",
                        NamedObjEliminator.v(_toplevel)));
        
        // We REALLY need to cleanup here or the code is not correct..
        _addStandardOptimizations(Scene.v().getPack("wjtp"));

        // Remove Unreachable methods.
        // FIXME: This has bugs...
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.umr", UnreachableMethodRemover.v()));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot6", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot6", ClassWriter.v()));
                 
        //    Scene.v().getPack("wjtp").add(new Transform("wjtp.ts",
        //                                               TypeSpecializer.v(_toplevel)));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.tie",
                        new TransformerAdapter(
                                TokenInstanceofEliminator.v())));
    
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ttn",
                        TokenToNativeTransformer.v(_toplevel)));
                    
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(_toplevel)));
         
        _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ufr", 
                        UnusedFieldRemover.v()));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.smr",
                        SideEffectFreeInvocationRemover.v()));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
         */
           
        // This snapshot should be last...
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.finalSnapshot",
                        JimpleWriter.v()));
     
        // And write C!
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.finalSnapshot", CWriter.v()));
             
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.watchDogCancel",
                        WatchDogTimer.v(), "cancel:true"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call soot.Main.main(), which does command line argument
     *  processing and then starts the transformation.  This method
     *  should be called after calling initialize() and addTransforms().
     *
     *  @param args Soot command line arguments to be passed
     *  to soot.Main.main().
     */
    public void generateCode(String [] args) {
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";

        // Rather than calling soot.Main.main() here directly, which
        // spawns a separate thread, we run this in the same thread
        //soot.Main.main(args);
        System.out.println("debug args = " + Arrays.asList(args));
        soot.Main.setReservedNames();
        soot.Main.setCmdLineArgs(args);
        soot.Main main = new soot.Main();
        soot.ConsoleCompilationListener consoleCompilationListener =
                new soot.ConsoleCompilationListener();
        soot.Main.addCompilationListener(consoleCompilationListener);
        // main.run(false);
        main.run();
    }


    /** Read in a MoML model, generate .class files for use with C.
     *  Arguments are passed on to soot, except for special non-Soot
     *  arguments that are used to configure the overall code generation
     *  process. Presently, two non-Soot argument are recognized:
     *  "-clooped," which indicates that schedule loops should be translated
     *  into loops in the generated code (FIXME: this is not supported yet);
     *  and "-cdebug" which turns on debugging output for c code generation.
     *  The first argument specifies the MoML model. This is followed
     *  by zero or more non-Soot arguments, after which the Soot arguments
     *  are listed.
     *  @param args Code generation arguments.
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
            throws IllegalActionException, NameDuplicationException {

        _writer = null;
        Main main = new Main(args);

        // Parse the model.
        CompositeActor toplevel = main.readInModel(args[0]);

        // Set of argument indices to skip when constructing the argument
        // list for Soot.
        int skipCount = 0;
        boolean[] skipArgument = new boolean[args.length];
        for (int i = 0; i < args.length; i++) {
            skipArgument[i] = false;
        }

        // Extract non-Soot arguments.
        int index = 1;    // Index into original argument array.
        boolean done = false; // Finished looking for non-Soot arguments.
        while (!done) {
            if (args.length <= index) {
                done = true;
            } else if (args[index].equals("-clooped")) {
                 _generateLoopedSchedule = true;
                 skipArgument[index++] = true;
                 skipCount++;
            } else if (args[index].equals("-cdebug")) {
                 _debug = true;
                 skipArgument[index++] = true;
                 skipCount++;
            } else {
                done = true;
            }
        }

        // Filter out arguments not meant for Soot.
        if (skipCount >= args.length) {
            throw new RuntimeException("Too many non-Soot arguments.");
        }
        String [] sootArguments = new String[args.length - skipCount];
        int sootIndex = 0;
        for (int i = 0; i < args.length; i++) {
            if (!skipArgument[i]) {
                sootArguments[sootIndex++] = args[i];
            }
        }

        // Create instance classes for the actors.
        main.initialize(toplevel);

        // Add Transforms to the Scene.
        main.addTransforms();

        // Ignore exceptions that pertain to writing unnecessary class files
        // after code generation is complete.
        try {
            main.generateCode(sootArguments);
        } catch (RuntimeException exception) {
            // Under debug mode, soot throws a RuntimeException if it
            // can't generate a class file. We wan't to generate (and
            // ignore) this exception here so that class files are not
            // generated. More specifically, we ignore any exceptions that
            // occur after code generation is completed.
            if ((_writer == null) || (!_writer.completedTransform())) {
                // In JDK1.4, we can include the cause
                throw new RuntimeException(exception.getMessage()
                        + ": " + exception.getClass().getName(), exception);
            } else if (_debug) {
                System.err.println("Warning: exception after code generation"
                        + " completed.\n" + exception + "\n");
                exception.printStackTrace();
            }
        }
    }

    /** Add transforms corresponding to the standard soot optimizations
     *  to the given pack.
     *  FIXME: copied from java/Main.java 
     */
    private void _addStandardOptimizations(Pack pack) {
        pack.add(new Transform("jop.cse",
                         new TransformerAdapter(CommonSubexpressionEliminator.v())));
        pack.add(new Transform("jop.cp",
                         new TransformerAdapter(CopyPropagator.v())));
        pack.add(new Transform("jop.cpf",
                         new TransformerAdapter(ConstantPropagatorAndFolder.v())));
        pack.add(new Transform("jop.cbf",
                         new TransformerAdapter(ConditionalBranchFolder.v())));
        pack.add(new Transform("jop.dae",
                         new TransformerAdapter(ImprovedDeadAssignmentEliminator.v())));
        pack.add(new Transform("jop.uce1",
                         new TransformerAdapter(UnreachableCodeEliminator.v())));
        pack.add(new Transform("jop.ubf1",
                         new TransformerAdapter(UnconditionalBranchFolder.v())));
        pack.add(new Transform("jop.uce2",
                         new TransformerAdapter(UnreachableCodeEliminator.v())));
        pack.add(new Transform("jop.ubf2",
                         new TransformerAdapter(UnconditionalBranchFolder.v())));
        pack.add(new Transform("jop.ule",
                         new TransformerAdapter(UnusedLocalEliminator.v())));
  }


    // Local debugging flag.
    private static boolean _debug = false;

    // Flags to control what gets generated.
    private boolean _generateJimple = false;
    private static boolean _generateLoopedSchedule = false;

    // The CWriter instance used to generate code.
    private static CWriter _writer = null;
}
