/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.domains.csp.guidemo;

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
import ptolemy.actor.util.PtolemyApplet;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// BusContentionApplet

/**
 *
 * @author John S. Davis II (davisj@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public class BusContentionApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
   public void init() {
        // Process the background parameter.
        _background = Color.white;
        try {
            String colorspec = getParameter("background");
            if (colorspec != null) {
                _background = Color.decode(colorspec);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);

        Manager manager = new Manager("manager");
	TypedCompositeActor topLevel = new TypedCompositeActor();
	try {
	    topLevel.setName("topLevel"); 
	    topLevel.setManager(manager);
	} catch( NameDuplicationException e ) {
	    System.err.println("NameDuplicationException thrown in the main \n" +
		    "method. Stack trace: \n");
            e.printStackTrace();
	} catch( IllegalActionException e ) {
	    System.err.println("IllegalActionException thrown in the main \n" +
		    "method. Stack trace: \n");
            e.printStackTrace();
	}

        _demo = new BusContentionApplication(manager, topLevel);
        Panel nullPanel = null;
	_demo.initializeDemo(this);
    }
 
    /**
     */
    public void start() {
    }
 
    /**
     */
    public void stop() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    BusContentionApplication _demo;

}
