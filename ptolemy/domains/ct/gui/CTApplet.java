/* A base class for applets that use the DE domain.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.gui;

import java.awt.*;
import java.awt.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// CTApplet
/**
A base class for applets that use the CT domain.
It override the stop() method so that when the applet is stoped, it
will stop the current execution.

@author Jie Liu
@version $Id$
*/
public class CTApplet extends PtolemyApplet {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Each CT applet is in a new workspace. The name of the workspace
     *  is the classname of the applet.
     */
    public void init() {
        // Process the background parameter.
        _background = Color.white;
        try {
            String colorSpecification = getParameter("background");
            if (colorSpecification != null) {
                _background = Color.decode(colorSpecification);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);
        _workspace = new Workspace(getClass().getName());
        try {
            _manager = new Manager(_workspace, "TopLevelManager");
            _toplevel = new TypedCompositeActor(_workspace);
            _toplevel.setName("topLevel");
            _toplevel.setManager(_manager);
        } catch (Exception ex) {
            report("Setup of manager and top level actor failed:\n", ex);
        }        
    }

    /** Override start() so it won't start automatically.
     */
    public void start() {
    }

      
    /** Stop the execution. Call manager.finish(). If there's no manager,
     *  do nothing.
     */
    public void stop() {
        if(_manager != null) {
            _stop();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** @serial The workspace. */
    protected Workspace _workspace;
}
