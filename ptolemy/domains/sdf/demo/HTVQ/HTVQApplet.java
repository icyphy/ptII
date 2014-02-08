/* An applet that uses Ptolemy II SDF domain.

 Copyright (c) 1999-2013 The Regents of the University of California.
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
package ptolemy.domains.sdf.demo.HTVQ;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ptolemy.actor.gui.AWTContainer;
import ptolemy.actor.gui.MoMLApplet;
import ptolemy.actor.lib.gui.SequencePlotter;
import ptolemy.domains.sdf.lib.vq.ImageDisplay;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// HTVQApplet

/**
 An applet that uses Ptolemy II SDF domain.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class HTVQApplet extends MoMLApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Place the placeable objects in a customized panel.
     */
    public void _createView() {
        super._createView();

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());

        // So that the background color comes through...
        displayPanel.setBackground(null);

        JPanel originalPanel = new JPanel();
        JPanel compressedPanel = new JPanel();

        //         JPanel prnPanel = new JPanel();

        //         // So the background shows through.
        //         prnPanel.setBackground(null);
        //         prnPanel.setLayout(new BorderLayout());
        //         prnPanel.add(new JLabel("SNR (dB)"), BorderLayout.NORTH);
        //         displayPanel.add(prnPanel, BorderLayout.SOUTH);

        CompositeEntity toplevel = (CompositeEntity) _toplevel;
        ImageDisplay consumer = (ImageDisplay) toplevel.getEntity("Compressed");
        compressedPanel.add(new JLabel("Compressed"), BorderLayout.NORTH);
        consumer.place(compressedPanel);
        displayPanel.add(compressedPanel, BorderLayout.EAST);
        consumer.setBackground(null);

        ImageDisplay original = (ImageDisplay) toplevel.getEntity("Original");
        originalPanel.add(new JLabel("Original"), BorderLayout.NORTH);
        original.place(originalPanel);
        displayPanel.add(originalPanel, BorderLayout.WEST);
        original.setBackground(null);

        SequencePlotter plot = (SequencePlotter) toplevel
                .getEntity("Signal To Noise Ratio");

        JPanel plotPanel = new JPanel();
        plot.place(new AWTContainer(plotPanel));
        plotPanel.setBackground(null);

        JPanel appletPanel = new JPanel();
        appletPanel.setLayout(new BorderLayout());
        appletPanel.setBackground(null);
        appletPanel.add(displayPanel, BorderLayout.NORTH);
        appletPanel.add(plotPanel, BorderLayout.SOUTH);

        getContentPane().add(appletPanel, BorderLayout.NORTH);

        // To control the position, we put this in its own panel.
        //         JPanel textPanel = new JPanel();
        //         prnPanel.add(textPanel, BorderLayout.SOUTH);
        //         prn.place(textPanel);
        //         textPanel.setBackground(null);
    }
}
