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
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.LibraryUsageReporter;
import ptolemy.copernicus.kernel.MakefileWriter;
// import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.kernel.util.IllegalActionException;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.jimple.toolkits.scalar.CommonSubexpressionEliminator;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;

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

        Pack pack = PackManager.v().getPack("wjtp");

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
        // -p wjtp.watchDog time:30000
        addTransform(pack, "wjtp.watchDog", WatchDogTimer.v());

        // Sanitize names of objects in the model.
        // We change the names to all be valid java identifiers
        // so that we can
        //      pack.add(new Transform("wjtp.ns",
        //         NameSanitizer.v(toplevel)));

        // Create a class for the composite actor of the model, and
        // additional classes for all actors (both composite and
        // atomic) used by the model.
       addTransform(pack, "wjtp.mt", ModelTransformer.v(toplevel));


        // Inline the director into the composite actor.
       addTransform(pack, "wjtp.idt",
                        InlineDirectorTransformer.v(toplevel));

        // Add a command line interface (i.e. Main)
       addTransform(pack, "wjtp.clt",
                        CommandLineTransformer.v(toplevel));


       addTransform(pack, "wjtp.ta1",
                        new TransformerAdapter(TypeAssigner.v()));
        _addStandardOptimizations(pack, 1);
        
       addTransform(pack, "wjtp.snapshot1jimple", JimpleWriter.v());
       addTransform(pack, "wjtp.snapshot1", ClassWriter.v());
       
       addTransform(pack, "wjtp.ib1", InvocationBinder.v());
       
       addTransform(pack, "wjtp.ls7",
                        new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.ffet",
                        FieldsForEntitiesTransformer.v(toplevel));

        // Infer the types of locals again, since replacing attributes
        // depends on the types of fields
       addTransform(pack, "wjtp.ta2",
                        new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.cie1",
                        new TransformerAdapter(
                                CastAndInstanceofEliminator.v()));
        _addStandardOptimizations(pack, 2);
       
        // In each actor and composite actor, ensure that there
        // is a field for every attribute, and replace calls
        // to getAttribute with references to those fields.
       addTransform(pack, "wjtp.ffat1",
                        FieldsForAttributesTransformer.v(toplevel));
        
        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
       addTransform(pack, "wjtp.ffpt",
                        FieldsForPortsTransformer.v(toplevel));
        
       addTransform(pack, "wjtp.ls2",
                        new TransformerAdapter(LocalSplitter.v()));
        addTransform(pack, "wjtp.lns",
                        new TransformerAdapter(LocalNameStandardizer.v()));
         _addStandardOptimizations(pack, 3);
         
       addTransform(pack, "wjtp.ls3",
                        new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.ta3",
                        new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.ib2",
                        InvocationBinder.v());
        
        // Set about removing reference to attributes and parameters.
        // Anywhere where a method is called on an attribute or
        // parameter, replace the method call with the return value
        // of the method.  This is possible, since after
        // initialization attribute values are assumed not to
        // change.  (Note: There are certain cases where this is
        // not true, i.e. the expression actor.  Those will be
        // specially handled before this point, or we should detect
        // assignments to attributes and handle them differently.)
       addTransform(pack, "wjtp.iat",
                        InlineParameterTransformer.v(toplevel));
        
        // Remove equality checks, which arise from inlining attributeChanged.
        //  pack.add(
        //                 new Transform("wjtp.ta",
        //                         new TransformerAdapter(TypeAssigner.v()));
        //         pack.add(
        //                 new Transform("wjtp.nee",
        //                         NamedObjEqualityEliminator.v(toplevel));

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
       addTransform(pack, "wjtp.itt1",
                        InlineTokenTransformer.v(toplevel));
        
       addTransform(pack, "wjtp.ls4",
                        new TransformerAdapter(LocalSplitter.v()));

        // While we still have references to ports, use the
        // resolved types of the ports and run a typing
        // algorithm to specialize the types of domain
        // polymorphic actors.  After this step, no
        // uninstantiable types should remain.
       addTransform(pack, "wjtp.tie",
                        new TransformerAdapter(
                                TokenInstanceofEliminator.v()));
       addTransform(pack, "wjtp.ta4",
                        new TransformerAdapter(TypeAssigner.v()));

       addTransform(pack, "wjtp.cp1",
                        new TransformerAdapter(CopyPropagator.v()));
       
        //       _addStandardOptimizations(pack, 4);
        
       addTransform(pack, "wjtp.snapshot2jimple", JimpleWriter.v());
       addTransform(pack, "wjtp.snapshot2", ClassWriter.v());

        // Set about removing references to ports.
        // Anywhere where a method is called on a port, replace the
        // method call with an inlined version of the method.
        // Currently this only deals with SDF, and turns
        // all gets and puts into reads and writes from circular
        // buffers.
       addTransform(pack, "wjtp.ipt",
                        InlinePortTransformer.v(toplevel));

        // This appears again because Inlining the parameters
        // also inlines calls to connectionsChanged, which by default
        // calls getDirector...  This transformer removes
        // these method calls.
        // FIXME: This should be done in a better way...
       addTransform(pack, "wjtp.ffat2",
                        FieldsForAttributesTransformer.v(toplevel));
        
        // Deal with any more statically analyzeable token
        // references that were created.
       addTransform(pack, "wjtp.itt2",
                        InlineTokenTransformer.v(toplevel));
        
        //pack.add(new Transform("wjtp.ta",
        //        new TransformerAdapter(TypeAssigner.v()));
        // pack.add(new Transform("wjtp.ibg",
        //        InvokeGraphBuilder.v());
        // pack.add(new Transform("wjtp.si",
        //        StaticInliner.v());
        
       addTransform(pack, "wjtp.snapshot3jimple", JimpleWriter.v());
       addTransform(pack, "wjtp.snapshot3", ClassWriter.v());
       
        // Unroll loops with constant loop bounds.
        //Scene.v().getPack("jtp").add(new Transform("jtp.clu",
        //        ConstantLoopUnroller.v());
       
        //     _addStandardOptimizations(pack, 5);
       
       addTransform(pack, "wjtp.ls5",
               new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.ta5",
               new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.ib3",
               InvocationBinder.v());
       
       addTransform(pack, "wjtp.nee",
               NamedObjEqualityEliminator.v(toplevel));
       
       // Remove casts and instanceof Checks.
       addTransform(pack, "wjtp.cie2",
               new TransformerAdapter(
                       CastAndInstanceofEliminator.v()));
       
       _addStandardOptimizations(pack, 6);
       
       // Remove Unreachable methods.  This happens BEFORE
       // NamedObjElimination so that we don't have to pick between
       // multiple constructors, if there are more than one.  I'm
       // lazy and instead of trying to pick one, lets use the only
       // one that is reachable.
       addTransform(pack, "wjtp.umr1", UnreachableMethodRemover.v());
       
       // Remove references to named objects.
       addTransform(pack, "wjtp.ee1",
                        ExceptionEliminator.v(toplevel));
       addTransform(pack, "wjtp.ls6",
                        new TransformerAdapter(LocalSplitter.v()));
       addTransform(pack, "wjtp.cie3",
                        new TransformerAdapter(
                                CastAndInstanceofEliminator.v()));
       addTransform(pack, "wjtp.ta6",
                        new TransformerAdapter(TypeAssigner.v()));
       addTransform(pack, "wjtp.ib4",
                        InvocationBinder.v());
       addTransform(pack, "wjtp.noe",
                        NamedObjEliminator.v(toplevel));
         
       addTransform(pack, "wjtp.umr2", UnreachableMethodRemover.v());
        
        // Some cleanup.
        // Remove object creations that are now dead (i.e. aren't used
        // and have no side effects).  This currently only deals with
        // Token and Type constructors, since we know that these will
        // have no interesting side effects.  More complex analysis
        // is possible here, but not likely worth it.

       addTransform(pack, "wjtp.doe1",
                        new TransformerAdapter(
                                DeadObjectEliminator.v()));
       addTransform(pack, "wjtp.dae1",
                        new TransformerAdapter(
                                DeadAssignmentEliminator.v()));
       addTransform(pack, "wjtp.doe2",
                        new TransformerAdapter(
                                DeadObjectEliminator.v()));
       addTransform(pack, "wjtp.dae2",
                        new TransformerAdapter(
                                DeadAssignmentEliminator.v()));
       addTransform(pack, "wjtp.doe3",
                        new TransformerAdapter(
                                DeadObjectEliminator.v()));
        _addStandardOptimizations(pack, 7);
        
       addTransform(pack, "wjtp.snapshot4jimple", JimpleWriter.v());
       addTransform(pack, "wjtp.snapshot4", ClassWriter.v());
       
       addTransform(pack, "wjtp.ttn",
                        TokenToNativeTransformer.v(toplevel));
        
       addTransform(pack, "wjtp.ufr",
                        UnusedFieldRemover.v());

// FIXME!         pack.add(
//                 new Transform("wjtp.smr",
//                         SideEffectFreeInvocationRemover.v());


        // Remove references to named objects.
       addTransform(pack, "wjtp.ee2",
                        ExceptionEliminator.v(toplevel));

       addTransform(pack, "wjtp.doe4",
                        new TransformerAdapter(
                                DeadObjectEliminator.v()));
        _addStandardOptimizations(pack, 8);
//FIXME!         pack.add(
//                 new Transform("wjtp.smr",
//                         SideEffectFreeInvocationRemover.v());
 
       addTransform(pack, "wjtp.ffu",
                        FinalFieldUnfinalizer.v());
       addTransform(pack, "wjtp.umr3", 
                        UnreachableMethodRemover.v());
        addTransform(pack, "wjtp.cp2",
                new TransformerAdapter(CopyPropagator.v()));

        /**/

        // This snapshot should be last...
       addTransform(pack, "wjtp.finalSnapshotJimple",
                        JimpleWriter.v());
       addTransform(pack, "wjtp.lur",
                        LibraryUsageReporter.v());
    }


    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        super.addTransforms();
        addStandardTransforms(_toplevel);
 
        Pack pack = PackManager.v().getPack("wjtp");
 
        // And write C!
        //      pack.add(
        //                 new Transform("wjtp.finalSnapshot", CWriter.v());

        // Generate the makefile files in outDir
        addTransform(pack, "wjtp.makefileWriter",
                MakefileWriter.v(_toplevel));

        addTransform(pack, "wjtp.watchDogCancel",
                WatchDogTimer.v(), "cancel:true");
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
    private static void _addStandardOptimizations(Pack pack, int time) {
        addTransform(pack, "wjtp.SOcse" + time,
                new TransformerAdapter(CommonSubexpressionEliminator.v()));
        addTransform(pack, "wjtp.SOcp" + time,
                new TransformerAdapter(CopyPropagator.v()));
        addTransform(pack, "wjtp.SOcpf" + time,
                new TransformerAdapter(ConstantPropagatorAndFolder.v()));
        addTransform(pack, "wjtp.SOcbf" + time,
                new TransformerAdapter(ConditionalBranchFolder.v()));
        addTransform(pack, "wjtp.SOdae" + time,
                new TransformerAdapter(DeadAssignmentEliminator.v()));
        addTransform(pack, "wjtp.SOuce1" + time,
                new TransformerAdapter(UnreachableCodeEliminator.v()));
        addTransform(pack, "wjtp.SOubf1" + time,
                new TransformerAdapter(UnconditionalBranchFolder.v()));
        addTransform(pack, "wjtp.SOuce2" + time,
                new TransformerAdapter(UnreachableCodeEliminator.v()));
        addTransform(pack, "wjtp.SOubf2" + time,
                new TransformerAdapter(UnconditionalBranchFolder.v()));
        addTransform(pack, "wjtp.SOule" + time,
                new TransformerAdapter(UnusedLocalEliminator.v()));
    }
}
