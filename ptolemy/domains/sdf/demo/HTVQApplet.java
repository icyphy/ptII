/* An applet for a demo of HTVQ Video Compression
 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.sdf.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.vq.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// HTVQApplet
/**
An applet that uses SDF to simulate a Video Compression Scheme based on
a simple Hierarchical Table-Lookup Vector Quantization demo.

@author Steve Neuendorffer
@version $Id$
*/
public class HTVQApplet extends Applet implements Runnable {

    public static final boolean DEBUG = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

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

        Button _goButton = new Button("Go");
        setLayout(new BorderLayout());

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding go button in the control panel.
        controlPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());
        // Done adding go button

       Panel displayPanel = new Panel();
        add(displayPanel);
        displayPanel.setLayout(new BorderLayout(15, 15));
        displayPanel.setSize(420, 200);

        Panel originalPanel = new Panel();
        originalPanel.setSize(200, 200);
        displayPanel.add(originalPanel, "West");

        Panel compressedPanel = new Panel();
        compressedPanel.setSize(200, 200);
        displayPanel.add(compressedPanel, "East");

        validate();

        // Creating the topology.
        try {
            _manager = new Manager();
            TypedCompositeActor c = new TypedCompositeActor();
            SDFDirector d = new SDFDirector();
            SDFScheduler s = new SDFScheduler();
            TypedIORelation r;
            c.setDirector(d);
            c.setManager(_manager);
            d.setScheduler(s);
            d.setScheduleValid(false);

            ImageSequence source = new ImageSequence(c, "Source");
            ImagePartition part = new ImagePartition(c, "Part");
            HTVQEncode encode = new HTVQEncode(c, "Encoder");
            VQDecode decode = new VQDecode(c, "Decoder");
            ImageUnpartition unpart = new ImageUnpartition(c, "Unpart");
            ImageDisplay consumer = new ImageDisplay(c, "Compressed");
            ImageDisplay original = new ImageDisplay(c, "Original");
            consumer.setPanel(compressedPanel);
            original.setPanel(originalPanel);

            r = (TypedIORelation) c.connect(
                    (TypedIOPort)source.getPort("image"),
                    (TypedIOPort)part.getPort("image"), "R1");
            ((TypedIOPort)original.getPort("image")).link(r);
            r = (TypedIORelation) c.connect(
                    (TypedIOPort)part.getPort("partition"),
                    (TypedIOPort)encode.getPort("imagepart"), "R2");
            r = (TypedIORelation) c.connect(
                    (TypedIOPort)encode.getPort("index"),
                    (TypedIOPort)decode.getPort("index"), "R3");
            r = (TypedIORelation) c.connect(
                    (TypedIOPort)decode.getPort("imagepart"),
                    (TypedIOPort)unpart.getPort("partition"), "R4");
            r = (TypedIORelation) c.connect(
                    (TypedIOPort)unpart.getPort("image"),
                    (TypedIOPort)consumer.getPort("image"), "R5");

            Parameter p = (Parameter) d.getAttribute("Iterations");
            p.setToken(new IntToken(60));

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        validate();
    }

    /** Run the simulation.
     */
    public void run() {

        try {
                // Start the CurrentTimeThread.
            //             Thread ctt = new CurrentTimeThread();
            //  ctt.start();

            validate();
                _manager.run();


        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }



    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Manager _manager;
    private Thread simulationThread;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Given a string of the form "x1 x2 x3 x4 ..." where xi's are double
    // values, return an array of double.
    private double[] _string2DoubleArray(String s) {

        LinkedList temp = new LinkedList();
        s = s.trim();
        while (s.length() != 0) {
            int firstSpace = s.indexOf(' ');
            if (firstSpace == -1) {
                firstSpace = s.length();
            }
            String head = s.substring(0, firstSpace);
            Double xi = Double.valueOf(head);
            temp.insertLast(xi);
            s = s.substring(firstSpace);
            s = s.trim();
        }
        double[] retVal = new double[temp.size()];
        int index = 0;
        while (!temp.isEmpty()) {
            retVal[index] = ((Double)temp.take()).doubleValue();

            index++;
        }
        return retVal;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    // Show simulation progress.
    /*   private class CurrentTimeThread extends Thread {
        public void run() {
            while (simulationThread.isAlive()) {
                // get the current time from director.
                double currenttime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = "+currenttime);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        }
    }
    */

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(HTVQApplet.this);
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(HTVQApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}



