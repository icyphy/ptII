/* Butterfly application

 Copyright (c) 1999 The Regents of the University of California.
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

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.conversions.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// ButterflyApplication
/**
 Standalone Butterfly Application, useful for profiling

@author Christopher Hylands
@version : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp ButterflyApplet.java.java,v 1.1 1999/05/06 20:14:28 cxh Exp $
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

        Workspace w = new Workspace("w");
        TypedCompositeActor toplevel = new TypedCompositeActor(w);
        toplevel.setName("toplevel");
        SDFDirector director = new SDFDirector(toplevel, "director");
        Manager manager = new Manager(w, "manager");
        toplevel.setManager(manager);

        try {
	    Butterfly.init(toplevel, getContentPane(),
                    getContentPane().getSize());
	    director.iterations.setToken(new IntToken(1200));
	    manager.run();
        } catch (Exception ex) {
            System.err.println("Error constructing model." + ex);
        }

        // Map to the screen.
	show();
    }

    /** Create a new window with the Butterfly plot in it and map it
	to the screen.
    */
    public static void main(String arg[])
            throws IllegalActionException , NameDuplicationException {

        ButterflyApplication butterflyApplication = new ButterflyApplication();
    }
}
