/* Abstract base class that provides common main() functionality
 to be used by various backends.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
 */
package ptolemy.copernicus.kernel;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;
import soot.HasPhaseOptions;
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
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;

///////////////////////////////////////////////////////////////////
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
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class KernelMain {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new transform to the given pack, dealing properly with
     *  options specified in the transformer.
     */
    public static void addTransform(Pack pack, String name,
            Transformer transformer, String defaultOptions) {
        Transform t = new Transform(name, transformer);

        if (transformer instanceof HasPhaseOptions) {
            HasPhaseOptions options = (HasPhaseOptions) transformer;

            // Note: First appearance of an option has precendence
            t.setDefaultOptions(defaultOptions + " "
                    + options.getDefaultOptions() + " " + t.getDefaultOptions());
            t.setDeclaredOptions(options.getDeclaredOptions() + " "
                    + t.getDeclaredOptions());
        } else {
            t.setDefaultOptions(defaultOptions + " " + t.getDefaultOptions());
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
    public void compile(String modelName, CompositeEntity toplevel,
            GeneratorAttribute attribute) throws Exception {
        //  try {
        long startTime = System.currentTimeMillis();

        // Create instance classes for the actors.
        try {
            initialize(toplevel);
        } catch (Throwable ex) {
            System.out.println("initialize() failed: " + ex);
            System.out
                .println("If the model does not have a director, consider adding \n"
                        + "<property name=\"DoNothingDirector\" class=\"ptolemy.actor.DoNothingDirector\">\n"
                        + "</property>");
        }

        if (attribute.getParameter("outputDirectory").indexOf(" ") != -1) {
            throw new Exception("The outputDirectory contains one or more "
                    + "spaces.  Unfortunately, the Soot option passing "
                    + "mechanism does not handle spaces.  The value of "
                    + "the outputDirectory parameter was: \""
                    + attribute.getParameter("outputDirectory") + "\"");
        }

        // Parse any copernicus args.
        String[] sootArgs = _parseArgs(attribute);

        if (sootArgs == null) {
            throw new NullPointerException("Failed to parse args for "
                    + attribute + ", resulting arg array was null");
        }

        // Add Transforms to the Scene.
        addTransforms();

        // Execute the transforms.
        generateCode(sootArgs);

        // Reset the state of the manager.  We haven't actually done
        // anything, but the state of the manager must be reset.
        try {
            if (_toplevel instanceof CompositeActor) {
                ((CompositeActor)_toplevel).getManager().wrapup();
            }
        } catch (Exception exception) {
            // This could be a problem with NonStrictTest.
            throw new KernelRuntimeException(exception,
                    "Could not wrapup composite actor");
        }

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
        System.out.println("Copernicus.kernel.KernelMain.generateCode(" + java.util.Arrays.toString(args));
        if (!soot.options.Options.v().parse(args)) {
            throw new KernelRuntimeException("Option parse error");
        }
        String ptII = StringUtilities.getProperty("ptolemy.ptII.dir");
        String systemJar = "";
        try {
            systemJar = KernelMain._getSystemJar().getCanonicalPath();
        } catch (Throwable throwable) {
            throw new KernelRuntimeException(throwable, "Could not find rt.jar or classes.jar");
        }
        if (!soot.options.Options.v().parse(new String [] {
                            "-cp", ptII + ":" + systemJar,
                            "-w",
                            "-allow-phantom-refs"})) {
            throw new KernelRuntimeException("Option parse error");
        }
        PackManager.v().getPack("wjtp").apply();
    }

    /** Read in a MoML class, sanitize the top level name,
     *  initialize the model.  Usually initialize() is called after
     *  calling readInModel().
     *
     *  <p>If the director is an SDF director, then the number of
     *  iterations is handled specially.  If the director is an SDF
     *  director and a parameter called "copernicus_iterations" is
     *  present, then the value of that parameter is used as the
     *  number of iterations.  If the director is an SDF director, and
     *  there is no "copernicus_iterations" parameter but the
     *  "ptolemy.ptII.copernicusIterations" Java property is set, then
     *  the value of that property is used as the number of
     *  iterations.
     *
     *  @param toplevel The model we are generating code for.
     */
    public void initialize(CompositeEntity toplevel)
            throws IllegalActionException, NameDuplicationException {
        _toplevel = toplevel;

        // ptolemy.data.ontologies.Ontology is a CompositeEntity, not a CompositeActor.
        if (!(_toplevel instanceof CompositeActor)) {
            System.err.println("Warning: KernelMain.initialize(): toplevel " + _toplevel.getFullName()
                    + " is not a CompositeActor, it is a " + _toplevel.getClass());
        } else {
            // Applet codegen works with all directors, not just SDF.
            Director topLevelDirector = ((CompositeActor)_toplevel).getDirector();

            // FIXME: nearly duplicate code in java/TestApplication.java
            if (topLevelDirector != null && topLevelDirector instanceof SDFDirector) {
                SDFDirector director = (SDFDirector) topLevelDirector;
                Parameter iterations = (Parameter) director
                    .getAttribute("iterations");
                Parameter copernicus_iterations = (Parameter) director
                    .getAttribute("copernicus_iterations");

                // Set to be a large number of iterations, unless
                // copernicus_iterations is set.
                if (copernicus_iterations != null) {
                    iterations.setToken(copernicus_iterations.getToken());
                } else {
                    String copernicusIterations = StringUtilities
                        .getProperty("ptolemy.ptII.copernicusIterations");

                    if (copernicusIterations.length() > 0) {
                        System.out.println("KernelMain: "
                            + "Setting number of iterations to "
                            + copernicusIterations);
                        iterations.setToken(new IntToken(copernicusIterations));
                    }
                }
            }

            // Initialize the model to ensure type resolution and scheduling
            // are done.
            try {
                Manager manager = new Manager(((CompositeActor)_toplevel).workspace(), "manager");
                ((CompositeActor)_toplevel).setManager(manager);
                manager.preinitializeAndResolveTypes();
            } catch (Exception exception) {
                throw new IllegalActionException(_toplevel, exception,
                        "Could not initialize composite actor");
            }
        }
    }

    /** Return the model that we are generating code for.
     */
    public CompositeEntity toplevel() {
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

    /** The CompositeEntity we are generating code for.
     */
    protected CompositeEntity _toplevel;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the path name to the system jar file, usually rt.jar.
    private static File _getSystemJar()
        throws IOException, FileNotFoundException {
        String systemJarPathName = System.getProperty("java.home")
                + File.separator + "lib" + File.separator + "rt.jar";

        File systemJar = new File(systemJarPathName);

        // This would be a good place to search in other places, perhaps
        // by reading a property like ptolemy.system.jar
        // However, we should wait until this is a problem.
        // The code works with Sun JDK1.2 and 1.3 and IBM JDK1.3.
        if (!systemJar.isFile()) {
            // Try this for IBM JDK 1.4.1
            String systemJarPathName2 = System.getProperty(
                        "java.home") + File.separator + "lib" + File.separator
                    + "core.jar";
            systemJar = new File(systemJarPathName2);


            if (!systemJar.isFile()) {
                // Search for Classes.jar on the mac
                String systemJarPathName3 = System.getProperty("java.home")
                    + File.separator + "../Classes"
                    + File.separator + "classes.jar";
                systemJar = new File(systemJarPathName3);

                if (!systemJar.isFile()) {
                    throw new FileNotFoundException(systemJarPathName + " and "
                            + systemJarPathName2 + " and "
                            + systemJarPathName3
                            + " either do not exist or are not readable");
                } else {
                    systemJarPathName = systemJarPathName3;
                }
            } else {
                systemJarPathName = systemJarPathName2;
            }
        }

        if (!systemJar.canRead()) {
            throw new IOException("Can't read '" + systemJarPathName + "'");
        }

        return systemJar;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    public static class _IgnoreAllApplicationClasses extends SceneTransformer {
        /** Transform the Scene according to the information specified
         *  in the model for this transform.
         *  @param phaseName The phase this transform is operating under.
         *  @param options The options to apply.
         */
        @Override
        protected void internalTransform(String phaseName, Map options) {
            // For some reason, soot 2.0.1 gives java.lang.Object as
            // its own superclass!
            PtolemyUtilities.objectClass.setSuperclass(null);

            for (Iterator classes = Scene.v().getApplicationClasses()
                    .snapshotIterator(); classes.hasNext();) {
                SootClass theClass = (SootClass) classes.next();
                theClass.setLibraryClass();
            }
        }
    }
}
