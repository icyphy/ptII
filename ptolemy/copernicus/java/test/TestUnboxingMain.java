/* Test for unboxing.

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java.test;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.java.TokenToNativeTransformer;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.GrimpTransformer;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;


//////////////////////////////////////////////////////////////////////////
//// TestUnboxingMain

/**
   Test for token unboxing.

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class TestUnboxingMain extends KernelMain {
    /** First argument is the output directory.
     *  Second argument is the class name.
     */
    public static void main(String[] args) {
        PtolemyUtilities.loadSootReferences();

        TestUnboxingMain main = new TestUnboxingMain();
        main.setOutputDirectory(args[0]);
        main.addTransforms();

        SootClass theClass = Scene.v().loadClassAndSupport(args[1]);
        theClass.setApplicationClass();
        main.generateCode(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public TestUnboxingMain() {
        _toplevel = new CompositeActor();
    }

    /** Add to the scene a standard set of transformations that are useful
     *  for optimizing efficiency.
     *  @param toplevel The composite actor we are generating code for.
     */
    public static void addStandardTransforms(CompositeActor model) {
        Pack pack = PackManager.v().getPack("wjtp");
        addTransform(pack, "wjtp.watchDog", WatchDogTimer.v(),
            "time:" + _watchDogTimeout);
        addTransform(pack, "wjtp.ttn", TokenToNativeTransformer.v(model)); // "debug:true level:1");

        addStandardOptimizations(pack, 8);

        addTransform(pack, "wjtp.ufr", UnusedFieldRemover.v());
        addStandardOptimizations(pack, 10);
    }

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        Pack pack = PackManager.v().getPack("wjtp");

        addStandardTransforms(_toplevel);
        addTransform(pack, "wjtp.gt", GrimpTransformer.v());
        addTransform(pack, "wjtp.finalSnapshotJimple", JimpleWriter.v(),
            "outDir:" + _outputDirectory);
        addTransform(pack, "wjtp.finalSnapshot", ClassWriter.v(),
            "outDir:" + _outputDirectory);
        addTransform(pack, "wjtp.watchDogCancel", WatchDogTimer.v(),
            "cancel:true");
    }

    /** Set the watchdog timeout.
     */
    public void setWatchDogTimeout(String string) {
        _watchDogTimeout = string;
    }

    /** Set the output directory.
     */
    public void setOutputDirectory(String string) {
        _outputDirectory = string;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    private static String _watchDogTimeout = "720000";
    private static String _outputDirectory = "test";
}
