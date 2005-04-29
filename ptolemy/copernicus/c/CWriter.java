/*
  A transformer that writes C source code.

  Copyright (c) 2002-2005 The University of Maryland.
  All rights reserved.
  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the above
  copyright notice and the following two paragraphs appear in all copies
  of this software.

  IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
  FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
  ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
  THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.

  THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
  PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
  MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package ptolemy.copernicus.c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import ptolemy.copernicus.kernel.MakefileWriter;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;


/** A transformer that writes C source code.
    @author Shuvra S. Bhattacharyya
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class CWriter extends SceneTransformer {
    /** Test if the internal transform associated with this writer has
     *  completed.
     *  @return True if the transform has completed.
     */
    public boolean completedTransform() {
        return _completedTransform;
    }

    /** Get the options associated with the C Writer.
     *  @return The options.
     */
    public String getDeclaredOptions() {
        // FIXME: conditionally allow the debug option.
        // return super.getDeclaredOptions() + " debug outDir";
        // The debug option suppresses exceptions, which in turn forces
        // repeated attempts to generate class files.
        //System.out.println("Options: + outDir targetPackage";
        return "outDir targetPackage";
    }

    /** Write out the C (.i, .h, interface Header) files.
     *  Sample option arguments:
     *  <code>-p wjtp.writeJimple1 outDir:jimple1</code>
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.writeJimple2</code>.
     *  @param options The options Map.  This method uses the
     *  <code>outdir</code> option to specify where the .jimple
     *  file should be written
     */
    public void internalTransform(String phaseName, Map options) {
        System.out.println("CWriter.internalTransform(" + phaseName + ", "
                + options + ")");

        // We use soot.Options to avoid confusion with
        // copernicus.c.options.
        String outDir = soot.PhaseOptions.getString(options, "outDir");
        String mainFile = soot.PhaseOptions.getString(options, "targetPackage")
            + ".Main";

        // Initialize generation of overridden methods.
        OverriddenMethodGenerator.init();

        // FIXME: Remove the next line if things don't break without it
        // _completedTransform = true;
        // We need to cache the classes up front to avoid a concurrent
        // modification exception.
        ArrayList classList = new ArrayList();

        /* Adds all classes to list. We just want main class.
           for (Iterator classes = Scene.v().getApplicationClasses().iterator();
           classes.hasNext(); classList.add(classes.next()));
        */
        classList.add(Scene.v().getMainClass());

        StringBuffer sourcesList = new StringBuffer();

        for (Iterator sootClasses = classList.iterator();
             sootClasses.hasNext();) {
            SootClass sootClass = (SootClass) sootClasses.next();

            // Determine the base of the source code file names.
            String fileName;

            /* FIXME: Changed this to enable code generation in correct
             * directory.
             if (!outDir.equals("")) {
             File outDirFile = new File(outDir);
             if (!outDirFile.isDirectory()) {
             outDirFile.mkdirs();
             }
             fileName = outDir + System.getProperty("file.separator");
             } else {
             fileName = "";
             }
            */
            fileName = CNames.classNameToFileName(sootClass.getName());

            //fileName += sootClass.getName();
            // FIXME: move these out of the loop?
            HeaderFileGenerator hGenerator = new HeaderFileGenerator();
            CodeFileGenerator cGenerator = new CodeFileGenerator();
            StubFileGenerator sGenerator = new StubFileGenerator();
            CNames.setup();

            RequiredFileGenerator RFG = new RequiredFileGenerator();

            String classPath = Scene.v().getSootClassPath();
            System.out.println("CWriter: soot class path = " + classPath);

            // Options for fast minimal code generation.
            Options.v().put("pruneLevel", "1");
            Options.v().put("compileMode", "full");
            Options.v().put("verbose", "true");
            Options.v().put("runtimeDir", "../../runtime");

            //Options.v().put("gcDir", null);
            RFG.init(classPath, sootClass.getName());

            // Figure out if this is the main class
            System.out.println("Main file: " + mainFile);
            System.out.println("Class name:" + sootClass.getName());

            boolean isMainClass = false;
            MainFileGenerator mGenerator = null;

            // FIXME: Testing purposes.
            if (true) {
                //if (mainFile.equals(sootClass.getName())) {
                isMainClass = true;
                mGenerator = new MainFileGenerator();
            }

            //generate the .i.h, .h, and .c files
            System.out.println("Generating C code files for " + fileName);

            String code = null;

            /* FIXME: Remove this code.
             * Not needed because RequiredFileGenerator will take care of
             * it.

             code = sGenerator.generate(sootClass);
             FileHandler.write(fileName
             + StubFileGenerator.stubFileNameSuffix(),
             code);
             code = hGenerator.generate(sootClass);
             FileHandler.write(fileName + ".h", code);
             code = cGenerator.generate(sootClass);
             FileHandler.write(fileName + ".c", code);
             sourcesList.append(" " + fileName + ".c");
            */

            // Generate all required files, including the files for the
            // main class.
            try {
                RequiredFileGenerator.generateTransitiveClosureOf(classPath,
                        sootClass.getName());
            } catch (IOException exception) {
                throw new RuntimeException("Could not generate transitive "
                        + "closure during required file generation");
            }

            // Generate a main file, containing a C main function,
            // if this is the main class.
            if (isMainClass) {
                code = mGenerator.generate(sootClass);
                FileHandler.write(fileName + "_main.c", code);
            }

            // Generate a makefile.
            MakeFileGenerator.generateMakeFile(classPath, sootClass.getName());

            System.out.println("Done generating C code files for " + fileName);
        }

        MakefileWriter.addMakefileSubstitution("@cFiles@",
                sourcesList.toString());

        _completedTransform = true;
    }

    /** Return a new CWriter.
     *  @return The new CWriter.
     */
    public static CWriter v() {
        return instance;
    }

    // Flag that indicates whether transform has been completed.
    private boolean _completedTransform = false;
    private static CWriter instance = new CWriter();

    private CWriter() {
    }
}
