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
import diva.canvas.connector.*;
import diva.canvas.toolbox.*;
import diva.util.gui.TutorialWindow;

import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.actor.process.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

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
public class BusContentionApplet extends Applet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void init() {
        _demo = new BusContentionApplication();
	_demo.initializeDemo();
    }
 
    /**
     */
    public void start() {
        System.out.println("yo");
	_demo.runDemo();
    }

    /**
     */
    public void stop() {
    }
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    BusContentionApplication _demo;

}
