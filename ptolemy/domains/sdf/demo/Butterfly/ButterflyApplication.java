/* Butterfly application

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Butterfly;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.IntToken;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//////////////////////////////////////////////////////////////////////////
//// ButterflyApplication
/**
Standalone Butterfly Application, useful for profiling.

@author Christopher Hylands
@version $Id$
*/
public class ButterflyApplication extends JFrame {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a Butterfly Application
     */
    public ButterflyApplication()
            throws IllegalActionException, NameDuplicationException {
	super("Butterfly");

        setSize(400, 400);

        // Handle window closing by exiting the application.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Workspace workspace = new Workspace("workspace");
        TypedCompositeActor toplevel = new TypedCompositeActor(workspace);
        toplevel.setName("toplevel");
        SDFDirector director = new SDFDirector(toplevel, "director");
        Manager manager = new Manager(workspace, "manager");
        toplevel.setManager(manager);

        try {
	    Butterfly butterfly = new Butterfly(toplevel, "butterfly",
						getContentPane());
	    director.iterations.setToken(new IntToken(1200));
	    // Map to the screen.
	    show();
	    manager.run();
        } catch (Exception exception) {
            System.err.println("Error constructing model: " + exception);
        }


    }

    /** Create a new window with the Butterfly plot in it and map it
	to the screen.
    */
    public static void main(String arg[])
            throws IllegalActionException , NameDuplicationException {

        ButterflyApplication butterflyApplication = new ButterflyApplication();
    }
}
