/* Abstract base class that provides common main() functionality
to be used by various backends.

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

package ptolemy.copernicus.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

import soot.HasPhaseOptions;
import soot.options.Options;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;
import soot.Transformer;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.microstar.xml.XmlException;

//////////////////////////////////////////////////////////////////////////
//// Main
/** 
Base class that provides common functionality to be used by various
code generators.  Particular code generators should extend this class
and generally override the addTransforms method to instantiate the
correct transforms and the _parseArgs method to extract arguments.
These subclasses should be not be instantiated directly, but will
instead be instantiated by the Copernicus class according to a
selected code generator.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0 */
public abstract class KernelMain {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new transform to the given pack, dealing properly with
     *  options specified in the transformer.
     */
    public static void addTransform(Pack pack, String name,
            Transformer transformer, String defaultOptions) {
        Transform t = new Transform(name, transformer);
        if(transformer instanceof HasPhaseOptions) {
            HasPhaseOptions options = (HasPhaseOptions) transformer;
            // Note: First appearance of an option has precendence
            t.setDefaultOptions(defaultOptions + " " + 
                    options.getDefaultOptions() + " " +
                    t.getDefaultOptions());
            t.setDeclaredOptions(options.getDeclaredOptions() + " " + 
                    t.getDeclaredOptions());
        } else {
            t.setDefaultOptions(defaultOptions + " " +
                    t.getDefaultOptions());
        }
        pack.add(t);
    }

    /** Add a new transform to the given pack, dealing properly with
     *  options specified in the transformer.
     */
    public static void addTransform(Pack pack, String name,
            Transformer transformer) {
        addTransform(pack, name, transformer, "");
    }

    /** Add transforms to the Scene.  Derived classes should do most
     *  of their added functionality in this method.
     */
    public abstract void addTransforms();

    /** Compile the given model with the given name.  This method
     *  invokes other methods of this class to actually perform the
     *  compilation.
     */
    public void compile(String modelName, CompositeActor toplevel,
            GeneratorAttribute attribute) throws Exception {
        //  try {
            long startTime = System.currentTimeMillis();

            // Create instance classes for the actors.
            initialize(toplevel);

            // Parse any copernicus args.
            String[] sootArgs = _parseArgs(attribute);
                
            // Add Transforms to the Scene.
            addTransforms();

            // Execute the transforms.
            generateCode(sootArgs);

            // Print out memory usage info
            System.out.println(modelName + " "
                    + ptolemy.actor.Manager.timeAndMemory(startTime));
            
//         } catch (Exception ex) {
//             System.err.println("Code generation of '" + modelName
//                     + "' failed:");
// //             ex.printStackTrace(System.err);
// //             System.err.flush();
// //             System.exit(2);
//         }
    }

    /** Call soot.Main.main(), which does command line argument
     *  processing and then starts the transformation.  This method
     *  should be called after calling initialize() and addTransforms().
     *
     *  @param args Soot command line arguments to be passed
     *  to soot.Main.main().
     */
    public void generateCode(String[] args) {
//         // This is rather ugly.  The moml Class is not a Java class, so
//         // soot won't recognize it.  However, if we give soot nothing, then
//         // it won't run.  Note that later we will call setLibraryClass() on
//         // this class so that we don't actually generate code for it.
//         args[0] = "java.lang.Object";

//         // As of soot 2.0.1, this is all that is required.
//         //        soot.Main.main(args);
//         if (!Options.v().parse(args))
//             throw new KernelRuntimeException(
//                     "Option parse error");
      
        PackManager.v().getPack("wjtp").apply();
  
        // Reset the state of the manager.  We haven't actually done
        // anything, but the state of the manager must be reset.
        try {
            _toplevel.getManager().wrapup();
        } catch (Exception exception) {
            throw new KernelRuntimeException(exception,
                    "Could not wrapup composite actor");
        }
    }

    /** Read in a MoML class, sanitize the top level name,
     *  initialize the model.  Usually initialize() is called after
     *  calling readInModel().
     *  @param toplevel The model we are generating code for.
     */
    public void initialize(CompositeActor toplevel)
            throws IllegalActionException, NameDuplicationException {
        _toplevel = toplevel;

        // Initialize the model to ensure type resolution and scheduling
        // are done.
        try {
            Manager manager = new Manager(_toplevel.workspace(), "manager");
            _toplevel.setManager(manager);
            manager.preinitializeAndResolveTypes();
        } catch (Exception exception) {
            throw new KernelRuntimeException(exception,
                    "Could not initialize composite actor");
        }
    }

    /** Return the model that we are generating code for.
     */
    public CompositeActor toplevel() {
        return _toplevel;
    }

    /** Add transforms corresponding to the standard soot optimizations
     *  to the given pack.
     */
    public static void addStandardOptimizations(Pack pack, int time) {
        List standardList = new LinkedList();
        standardList.add(CommonSubexpressionEliminator.v());
        standardList.add(CopyPropagator.v());
        standardList.add(ConstantPropagatorAndFolder.v());
        standardList.add(ConditionalBranchFolder.v());
        standardList.add(DeadAssignmentEliminator.v());
        standardList.add(UnreachableCodeEliminator.v());
        standardList.add(UnconditionalBranchFolder.v());
        standardList.add(UnreachableCodeEliminator.v());
        standardList.add(UnconditionalBranchFolder.v());
        standardList.add(UnusedLocalEliminator.v());
        addTransform(pack, "wjtp.StandardOptimizations" + time,
                new TransformerAdapter(standardList));
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Parse any code generator specific arguments.  Derived classes
     * should override this method to extract any code
     * generator-specific variables from the GeneratorAttribute.
     */ 
    protected String[] _parseArgs(GeneratorAttribute attribute)
            throws Exception {
        return new String[0];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the MoML class, either as a top level model or
     *  as an xml file that we are generating code for.
     */
    protected String _momlClassName;

    /** The CompositeActor we are generating code for.
     */
    protected CompositeActor _toplevel;

    /** The MoMLParser for parsing models.
     */
    protected MoMLParser _parser = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class _IgnoreAllApplicationClasses
        extends SceneTransformer {
        /** Transform the Scene according to the information specified
         *  in the model for this transform.
         *  @param phaseName The phase this transform is operating under.
         *  @param options The options to apply.
         */
        protected void internalTransform(String phaseName, Map options) {
            // For some reason, soot 2.0.1 gives java.lang.Object as
            // its own superclass!
            PtolemyUtilities.objectClass.setSuperclass(null);
            for (Iterator classes =
                     Scene.v().getApplicationClasses().snapshotIterator();
                 classes.hasNext();) {
                SootClass theClass = (SootClass)classes.next();
                theClass.setLibraryClass();
            }
        }
    }
}
