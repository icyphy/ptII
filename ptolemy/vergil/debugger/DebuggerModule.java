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

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.graph.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.RelativeBundle;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.URL;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.fsm.kernel.FSMDirector;

//////////////////////////////////////////////////////////////////////////
//// DebuggerModule
/**
A modular Vergil package for the debugger
@author SUPELEC team
@version $Id$
@see DebuggerModule
@see ptolemy.vergil.debugger.DebuggerModule
*/
public class DebuggerModule implements Module {

    /** Constructor
     * @see ptolemy.vergil.debugger.DebuggerModule#DebuggerModule(VergilApplication application)
     * @param application : a reference to the current Vergil application
     */
    public DebuggerModule(VergilApplication application) {
	_application = application;
	Action action;

        JMenu menuDebugger = new JMenu("Debugger");
        menuDebugger.setMnemonic('E');
        _application.addMenu(menuDebugger);

        action = new AbstractAction ("Start Debugger") {
		public void actionPerformed(ActionEvent e) {
		    _debuggerFrame = new Pdb(_application);
		}
	    };
	_application.addMenuItem(menuDebugger, action, 'S',
				 "Create debugger panel");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the application that contains this module.
     * @see ptolemy.vergil.debugger.DebuggerModule#getApplication()
     * @return the vergil application that contains this module
     */	
    public Application getApplication() {
        return _application;
    }

    /** Return null.
     * @see ptolemy.vergil.debugger.DebuggerModule#getModuleResources()
     * @return null
     */
    public RelativeBundle getModuleResources() {
	return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The application that this package is associated with.
    private VergilApplication _application;

    // debugger
    private Pdb _debuggerFrame;
}
