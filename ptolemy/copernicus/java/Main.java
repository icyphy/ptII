/* Transform Actors using Soot

   Copyright (c) 2001-2003 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;

import ptolemy.copernicus.kernel.AliasAssignmentEliminator;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.ImprovedDeadAssignmentEliminator;
import ptolemy.copernicus.kernel.InstanceEqualityEliminator;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.LibraryUsageReporter;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.copernicus.c.CWriter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.StaticMethodBinder;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;

import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/**
   Read in a MoML model and generate Java classes for that model.

   @author Stephen Neuendorffer, Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.0
 */
public class Main extends KernelMain {

    /** Read in a MoML model.
     *  @param args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  command line options, see the superclass documentation for details.
     *  @exception IllegalActionException If the model cannot be parsed.
     */
    public Main(String [] args) throws IllegalActionException {
        // args[0] contains the MoML class name.
        super(args[0]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add to the scene a standard set of transformations that are useful
     *  for optimizing efficiency.
     *  @param toplevel The composite actor we are generating code for.
     */
    public static void addStandardTransforms(CompositeActor toplevel) {

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
        // -p wjtp.watchDog time:30000
        Scene.v().getPack("wjtp").add(new Transform("wjtp.watchDog",
                WatchDogTimer.v()));

        // Sanitize names of objects in the model.
        // We change the names to all be valid java identifiers
        // so that we can
        //      Scene.v().getPack("wjtp").add(new Transform("wjtp.ns",
        //         NameSanitizer.v(toplevel)));

        // Create a class for the composite actor of the model, and
        // additional classes for all actors (both composite and
        // atomic) used by the model.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.mt", ModelTransformer.v(toplevel)));


        // Inline the director into the composite actor.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.idt",
                        InlineDirectorTransformer.v(toplevel)));

        // Add a command line interface (i.e. Main)
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.clt",
                        CommandLineTransformer.v(toplevel)));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ta",
                        new TransformerAdapter(TypeAssigner.v())));
        _addStandardOptimizations(Scene.v().getPack("wjtp"));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot1", JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot1", ClassWriter.v()));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffet",
                        FieldsForEntitiesTransformer.v(toplevel)));

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
                        FieldsForAttributesTransformer.v(toplevel)));

        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffpt",
                        FieldsForPortsTransformer.v(toplevel)));

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
                        InlineParameterTransformer.v(toplevel)));

        // Remove equality checks, which arise from inlining attributeChanged.
        //  Scene.v().getPack("wjtp").add(
        //                 new Transform("wjtp.ta",
        //                         new TransformerAdapter(TypeAssigner.v())));
        //         Scene.v().getPack("wjtp").add(
        //                 new Transform("wjtp.nee",
        //                         NamedObjEqualityEliminator.v(toplevel)));

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.itt",
                        InlineTokenTransformer.v(toplevel)));

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
                        InlinePortTransformer.v(toplevel)));

        // This appears again because Inlining the parameters
        // also inlines calls to connectionsChanged, which by default
        // calls getDirector...  This transformer removes
        // these method calls.
        // FIXME: This should be done in a better way...
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ffat",
                        FieldsForAttributesTransformer.v(toplevel)));

        // Deal with any more statically analyzeable token
        // references that were created.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.itt",
                        InlineTokenTransformer.v(toplevel)));

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
                        NamedObjEqualityEliminator.v(toplevel)));

        // Remove casts and instanceof Checks.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.cie",
                        new TransformerAdapter(
                                CastAndInstanceofEliminator.v())));

        _addStandardOptimizations(Scene.v().getPack("wjtp"));
        
        // Remove Unreachable methods.  This happens BEFORE
        // NamedObjElimination so that we don't have to pick between
        // multiple constructors, if there are more than one.  I'm
        // lazy and instead of trying to pick one, lets use the only
        // one that is reachable.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.umr", UnreachableMethodRemover.v()));

        // Remove references to named objects.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(toplevel)));
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
                        NamedObjEliminator.v(toplevel)));

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
                        TokenToNativeTransformer.v(toplevel)));
       
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ufr",
                        UnusedFieldRemover.v()));
        
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.smr",
                        SideEffectFreeInvocationRemover.v()));


        // Remove references to named objects.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.ee",
                        ExceptionEliminator.v(toplevel)));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.doe",
                        new TransformerAdapter(
                                DeadObjectEliminator.v())));
        _addStandardOptimizations(Scene.v().getPack("wjtp"));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.smr",
                        SideEffectFreeInvocationRemover.v()));


        // This snapshot should be last...
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.finalSnapshot",
                        JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.lur",
                        LibraryUsageReporter.v()));
    }


    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        super.addTransforms();
        addStandardTransforms(_toplevel);
        // And write C!
        //      Scene.v().getPack("wjtp").add(
        //                 new Transform("wjtp.finalSnapshot", CWriter.v()));

        // Generate the makefile files in outDir
        Scene.v().getPack("wjtp").add(new Transform("wjtp.makefileWriter",
                MakefileWriter.v(_toplevel)));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.watchDogCancel",
                        WatchDogTimer.v(), "cancel:true"));

    }

    /** Read in a MoML model, generate java files.
     */
    public static void main(String[] args) {
        String modelName = args[0];
        try {
            long startTime = System.currentTimeMillis();

            Main main = new Main(args);

            // Parse the model.
            CompositeActor toplevel = main.readInModel(modelName);

            // Create instance classes for the actors.
            main.initialize(toplevel);

            // Add Transforms to the Scene.
            main.addTransforms();

            main.generateCode(args);

            // Print out memory usage info
            System.out.println(modelName + " "
                    + ptolemy.actor.Manager.timeAndMemory(startTime));
            // We need to call exit here if we are running codegen on
            // a model that uses Swing.  Useful models that use the
            // plotter fall in this category.
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Code generation of '" + modelName
                    + "' failed:");
            ex.printStackTrace(System.err);
            System.err.flush();
            System.exit(2);
        }
    }

    /** Add transforms corresponding to the standard soot optimizations
     *  to the given pack.
     */
    private static void _addStandardOptimizations(Pack pack) {
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
}
