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
 * @version $Revision$
 * @rating Red
 */
public class BusContentionApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
   public void init() {
        super.init();
        _demo = new BusContentionApplication();
	_demo.initializeDemo();
    }
 
    /**
     */
    public void start() {
      // Do Nothing
    }

    /**
     */
    public void stop() {
        _demo.shutDown();
        _demo = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    BusContentionApplication _demo;

}
