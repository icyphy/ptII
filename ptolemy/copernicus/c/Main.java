/*
 Transform Actors using Soot and generate C code.

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

package ptolemy.copernicus.c;

// FIXME: clean up import list.
import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.kernel.util.IllegalActionException;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

//////////////////////////////////////////////////////////////////////////
//// Main
/** Read in a MoML model, generate .c files with very few
    dependencies on Ptolemy II.

    @author Shuvra S. Bhattacharyya, Michael Wirthlin, Stephen Neuendorffer,
    Edward A. Lee, Christopher Hylands
    @version $Id$
*/

public class Main extends ptolemy.copernicus.java.Main {
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
        // super.addTransforms();
        addStandardTransforms(_toplevel);

        Pack pack = PackManager.v().getPack("wjtp");

        addTransform(pack, "wjtp.finalSnapshotC", CWriter.v());

        addTransform(pack, "wjtp.watchDogCancel",
                WatchDogTimer.v(), "cancel:true");
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
    public static void main(String[] args) {

        String modelName = args[0];
        try {
            long startTime = System.currentTimeMillis();

            Main main = new Main(args);

            // Parse the model.
            CompositeActor toplevel = main.readInModel(modelName);

            // Create instance classes for the actors.
            main.initialize(toplevel);

            // Parse any copernicus args.
            String[] sootArgs = _parseArgs(args);

            // Add Transforms to the Scene.
            main.addTransforms();

            // Generate C code
            main.generateCode(sootArgs);

            // Print out memory usage info
            System.out.println(modelName + " "
                    + ptolemy.actor.Manager.timeAndMemory(startTime));
            // We need to call exit here if we are running codegen on
            // a model that uses Swing.  Useful models that use the
            // plotter fall in this category.
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("C code generation of '" + modelName
                    + "' failed:");
            ex.printStackTrace(System.err);
            System.err.flush();
            System.exit(2);
        }

    }
    // Local debugging flag.
    private static boolean _debug = false;

    // The CWriter instance used to generate code.
    private static CWriter _writer = null;
}
