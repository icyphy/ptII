/* A demo of CSP.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.BusContention;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.canvas.connector.*;
import diva.util.gui.TutorialWindow;

import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.gui.PtolemyApplet;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// BusContentionApplet

/** A model of hardware subsystems accessing a shared resource using
 *  rendezvous. The model shows the use of timed CSP to
 *  deterministically handle nondeterministic events. 
 *  <p>
 *  The applet consists of a controller, three processors and a memory
 *  block. At randomly selected points in time, each processor can
 *  request permission from the controller to access the memory. The
 *  processors each have priorities associated with them, and in cases
 *  where there is a simultaneous memory access request, the controller
 *  grants permission to the processor with the highest priority. 
 *  <p>
 *  All communication between actors in a CSP model of computation
 *  occurs via rendezvous. Rendezvous is an atomic form of
 *  communication. This model uses a timed extension to CSP, so each
 *  rendezvous logically occurs at a specific point in time. 
 *  <p>
 *  Because of the atomic nature of rendezvous, when the controller
 *  receives a request for access, it cannot know whether there is
 *  another, higher priority request pending at the same time. To
 *  overcome this difficulty, an alarm is employed. The alarm is started
 *  by the controller immediately following the first request for memory
 *  access. It is awakened when time is ready to advance (the model
 *  blocks on delays). This indicates to the controller that no more
 *  memory requests will occur at the given point in time. Hence, the
 *  alarm uses centralized time to make deterministic an inherently
 *  non-deterministic activity. 
 *  <p>
 *  In the applet, each of the initially blue processors (the circular
 *  nodes) can be in one of three states. The color yellow indicates
 *  that a processor is in state 1 and is waiting for the controller to
 *  give it permission to access memory. The color green indicates that
 *  a processor has been granted permission to access memory. The color
 *  red indicates that the processor has been denied memory access.
 *
 *  @author John S. Davis II (davisj@eecs.berkeley.edu)
 *  @version $Id$ *
 */
public class BusContentionApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet. This method is called by the browser
     *  or applet viewer to inform this applet that it has been
     *  loaded into the system. It is always called before
     *  the first time that the start() method is called.
     *  In this base class, this method creates a new workspace,
     *  and creates a manager and a top-level composite actor
     *  in the workspace, both of which are accessible
     *  to derived classes via protected members.
     *  It also processes a background color parameter.
     *  If the background color parameter has not been set, then the
     *  background color is set to white.  Then it creates an
     *  instance of BusContentionApplication and initializes it.
     */
    public void init() {
        super.init();
        _demo = new BusContentionApplication(_manager, _toplevel);
        _demo.initializeDemo(this);
    }
 
    /** Start the applet.  This overrides the base class to avoid
     *  executing the model immediately.
     */
    public void start() {
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    BusContentionApplication _demo;
}
