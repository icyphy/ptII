/* A modular Vergil package for the Debugger.

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF
 THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELECSPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED
 TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.Vertex;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.vergil.*;
import ptolemy.vergil.debugger.*;
import ptolemy.vergil.ptolemy.*;

import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.RelativeBundle;
import java.util.*;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;

//////////////////////////////////////////////////////////////////////////
//// DebuggerModule
/**
A modular Vergil package for the debugger
@author SUPELEC team
@version $Id$
*/
public class DebuggerModule implements Module {

    /** 
     * Construct a new module with a reference to the given application.
     * @param application A reference to the current Vergil application.
     */
    public DebuggerModule(VergilApplication application) {
	_application = application;
	Action action;

        JMenu menuDebugger = new JMenu("Debugger");
        menuDebugger.setMnemonic('E');
        _application.addMenu(menuDebugger);

        action = new AbstractAction ("Start Debugging") {
		public void actionPerformed(ActionEvent e) {
		    Action executeAction = 
		    _application.getAction("Execute System");
		    executeAction.actionPerformed(e);
		    
		    System.out.println("debug");
		    PtolemyDocument d =
		    (PtolemyDocument) _application.getCurrentDocument();
		    if (d == null) {
			System.out.println("current doc is null");
			return;
		    }
		    try {
			System.out.println("current doc is not null");
			CompositeActor toplevel =
                        (CompositeActor) d.getModel();
			Director dir = toplevel.getDirector();
			DbgController controller = new DbgController();

			dir.addDebugListener(controller);
		    } catch (Exception ex) {
		    }
		}
	    };
	_application.addMenuItem(menuDebugger, action, 'D',
				 "Create debugger panel");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * Return the application that contains this module.
     * @return the vergil application that contains this module
     */	
    public Application getApplication() {
        return _application;
    }

    /** 
     * Return the resources that this module uses.
     * @return null
     */
    public RelativeBundle getModuleResources() {
	return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The application that this package is associated with.
    private VergilApplication _application;
}
