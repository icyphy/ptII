/* Main for applet generation

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

package ptolemy.copernicus.applet;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

//////////////////////////////////////////////////////////////////////////
//// Main
/**
Read in a MoML model and generate a .html file
that will run the model as an applet

@author Christopher Hylands
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


    /** Add transforms to the Scene.
     */
    public void addTransforms() {
        super.addTransforms();

        Pack pack = PackManager.v().getPack("wjtp");

        // Generate the makefile files in outDir
        addTransform(pack, "wjtp.makefileWriter",
                MakefileWriter.v(_toplevel));

        // Generate the applet files in outDir
        addTransform(pack, "wjtp.appletWriter",
                AppletWriter.v(_toplevel));

    }

    /** Read in a MoML model, generate java files
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
            throws IllegalActionException, NameDuplicationException {

        long startTime = System.currentTimeMillis();

        Main main = new Main(args);

        // Parse the model.
        CompositeActor toplevel = main.readInModel(args[0]);

        // Create instance classes for the actors.
        main.initialize(toplevel);

        // Add Transforms to the Scene.
        main.addTransforms();

        main.generateCode(args);

        // Print out memory usage info
        System.out.println(ptolemy.actor.Manager.timeAndMemory(startTime));

        //WatchDogTimer.v().cancel();

        // For some reason, we need to call exit here, perhaps because
        // the WatchDog timer thread is still running in the background?
        System.exit(0);
    }
}













