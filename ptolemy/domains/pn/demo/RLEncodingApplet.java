/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
*/

package ptolemy.domains.pn.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.media.Picture;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// RLEncodingApplet
/**
An example to test the PN domain. This example tests the PN INterleaving
example.
@author Mudit Goel
@version $Id$
*/

public class RLEncodingApplet extends Applet implements Runnable {


    /** Initialize the applet.
     */
    public void init() {

        // Process the background parameter.
        Color background = Color.white;
        try {
            String colorspec = getParameter("background");
            if (colorspec != null) {
                background = Color.decode(colorspec);
            }
        } catch (Exception ex) {}
        setBackground(background);

        // Initialization
        _goButton = new Button("Go");
        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(1,2));
        add(appletPanel, "Center");

        _imgin = new Picture(_SIZE,_SIZE);
        appletPanel.add(_imgin, "Center");
        _imgout = new Picture(_SIZE,_SIZE);
        appletPanel.add(_imgout, "West");

        // Adding a control panel in the applet panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding go button in the control panel.
        controlPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());
        // Done adding go button

        try {
            CompositeActor c1 = new CompositeActor();
            _manager = new Manager();
            c1.setManager(_manager);
            BasePNDirector local = new BasePNDirector("Local");
            local.addProcessListener(new DefaultPNListener());
            c1.setDirector(local);
            
            PNImageSource a1 = new PNImageSource(c1, "A1");
            Parameter p1 = (Parameter)a1.getAttribute("Image_file");
            p1.setToken(new StringToken("/users/ptII/ptolemy/domains/pn/lib/test/ptII.pbm"));
            MatrixUnpacker a2 = new MatrixUnpacker(c1, "A2");
            RLEncoder a3 = new RLEncoder(c1, "A3");
            RLDecoder a4 = new RLDecoder(c1, "A4");
            MatrixPacker a5 = new MatrixPacker(c1, "A5");
            PNImageSink a6 = new PNImageSink(c1, "A6");
            p1 = (Parameter)a6.getAttribute("Output_file");
            p1.setToken(new StringToken("/tmp/image.pbm"));
            //ImageDisplay a7 = new ImageDisplay(c1, "dispin");
            //ImageDisplay a8 = new ImageDisplay(c1, "dispout");
            
            ImageDisplay a7 = new ImageDisplay(c1, "dispin", _imgin);
            ImageDisplay a8 = new ImageDisplay(c1, "dispout", _imgout);
            
            IOPort portin = (IOPort)a1.getPort("output");
            IOPort portout = (IOPort)a2.getPort("input");
            ComponentRelation rel = c1.connect(portin, portout);
            (a7.getPort("image")).link(rel);
            
            portin = (IOPort)a2.getPort("output");
            portout = (IOPort)a3.getPort("input");
            c1.connect(portin, portout);
            
            portin =(IOPort) a2.getPort("dimensions");
            portout = (IOPort)a3.getPort("dimensionsIn");
            c1.connect(portin, portout);
            
            portin = (IOPort)a3.getPort("dimensionsOut");
            portout = (IOPort)a4.getPort("dimensionsIn");
            c1.connect(portin, portout);
            
            portin = (IOPort)a3.getPort("output");
            portout = (IOPort)a4.getPort("input");
            c1.connect(portin, portout);
            
            portin = (IOPort)a4.getPort("dimensionsOut");
            portout = (IOPort)a5.getPort("dimensions");
            c1.connect(portin, portout);
            
            portin = (IOPort)a4.getPort("output");
            portout = (IOPort)a5.getPort("input");
            c1.connect(portin, portout);
            
            portin = (IOPort)a5.getPort("output");
            portout = (IOPort)a6.getPort("input");
            rel = c1.connect(portin, portout);        
            (a8.getPort("image")).link(rel);

            System.out.println("Connections made");
            Parameter p = (Parameter)local.getAttribute("Initial_queue_capacity");
            p.setToken(new IntToken(500));     

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }    

    
    /** Run the simulation.
     */
    public void run() {

        try {
            //manager.startRun();
            _manager.run();
            System.out.println("Bye World\n");
            return;
        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }   

    /*private */Manager _manager;
    /*private*/ boolean _isSimulationRunning = false;
    Thread simulationThread;
    int _SIZE = 150;
    Picture _imgin;
    Picture _imgout;
    Button _goButton;

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            
            if (_isSimulationRunning) {
                System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(RLEncodingApplet.this);
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(RLEncodingApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new InternalErrorException("Error in GoButton" + 
                       "Listener class : " + e.getMessage()); 
            }

        }
    }

    private class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }

}


