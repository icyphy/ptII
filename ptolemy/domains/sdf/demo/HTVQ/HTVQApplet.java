/* An applet that uses Ptolemy II SDF domain.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

package ptolemy.domains.sdf.demo.HTVQ;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Enumeration;
import java.lang.Math;
import java.net.URL;
import javax.swing.JPanel;
import javax.swing.JLabel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.vq.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// HTVQApplet
/**
An applet that uses Ptolemy II SDF domain.

@author Steve Neuendorffer
@version $Id$
*/
public class HTVQApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            getContentPane().add(_createRunControls(2), BorderLayout.SOUTH);

	    JPanel displayPanel = new JPanel();
	    getContentPane().add(displayPanel, BorderLayout.NORTH);
            // So that the background color comes through...
            displayPanel.setOpaque(false);

	    JPanel originalPanel = new JPanel();
            displayPanel.add(originalPanel, BorderLayout.WEST);

            JPanel compressedPanel = new JPanel();
	    displayPanel.add(compressedPanel, BorderLayout.CENTER);

            JPanel prnPanel = new JPanel();
            // So the background shows through.
            prnPanel.setOpaque(false);
            prnPanel.setLayout(new BorderLayout());
            prnPanel.add(new JLabel("SNR (dB)"), BorderLayout.NORTH);
            displayPanel.add(prnPanel, BorderLayout.EAST);

            Parameter blockWidth = new Parameter(_toplevel, "blockWidth",
                    new IntToken(4));
            Parameter blockHeight = new Parameter(_toplevel, "blockHeight",
                    new IntToken(2));

            URL baseURL = new URL(getDocumentBase(), "../../../../../");
	    ImageSequence source = new ImageSequence(_toplevel, "Source");
            source.setBaseURL(baseURL);

            //added PSNR actor
            PSNR snr = new PSNR(_toplevel, "PSNR");

            ImagePartition part = new ImagePartition(_toplevel, "Part");
            part.partitionColumns.setExpression("blockWidth");
            part.partitionRows.setExpression("blockHeight");

	    HTVQEncode encode = new HTVQEncode(_toplevel, "Encoder");
            encode.setBaseURL(baseURL);
            encode.blockCount.setExpression("176*144/blockWidth/blockHeight");
            encode.blockWidth.setExpression("blockWidth");
            encode.blockHeight.setExpression("blockHeight");

	    VQDecode decode = new VQDecode(_toplevel, "Decoder");
            decode.setBaseURL(baseURL);
            decode.blockCount.setExpression("176*144/blockWidth/blockHeight");
            decode.blockWidth.setExpression("blockWidth");
            decode.blockHeight.setExpression("blockHeight");

	    ImageUnpartition unPartition =
		new ImageUnpartition(_toplevel, "Unpart");
            unPartition.partitionColumns.setExpression("blockWidth");
            unPartition.partitionRows.setExpression("blockHeight");

	    ImageDisplay consumer = new ImageDisplay(_toplevel, "Compressed");
	    consumer.place(compressedPanel);

	    ImageDisplay original = new ImageDisplay(_toplevel, "Original");
	    original.place(originalPanel);

            // Display actor puts the text at the right of the
            // applet window. Text Area size is set to be 7*10 (row* column)
            // in order to fit well with the image size.
            Display prn = new Display(_toplevel, "Display");
            // To control the position, we put this in its own panel.
            JPanel textPanel = new JPanel();
            prnPanel.add(textPanel, BorderLayout.SOUTH);
            prn.place(textPanel);
            prn.textArea.setColumns(10);
            prn.textArea.setRows(7);

	    TypedIORelation r;
            r = (TypedIORelation) _toplevel.connect(
                    source.output, part.input, "R1");
            original.input.link(r);
            snr.signal.link(r);

            r = (TypedIORelation) _toplevel.connect(
                    part.output, encode.input, "R2");

            r = (TypedIORelation) _toplevel.connect(
                    encode.output, decode.input, "R3");

            r = (TypedIORelation) _toplevel.connect(
                    decode.output, unPartition.input, "R4");

            r = (TypedIORelation) _toplevel.connect(
                    unPartition.output, consumer.input, "R5");
            snr.distortedSignal.link(r);

            r = (TypedIORelation) _toplevel.connect(
                    snr.output, prn.input, "R6");


        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
        super._go();
    }
}
