/* An applet that uses Ptolemy II SDF domain.

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.lang.Math;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.vq.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// ExpressionApplet
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
	    setLayout(new BorderLayout());

            Panel controlpanel = new Panel();
            controlpanel.setLayout(new BorderLayout());
            add(controlpanel, "South");

            // Create a "Go" button.
            Panel runcontrols = new Panel();
            controlpanel.add("Center", runcontrols);
            runcontrols.add(_createRunControls(1));

	    Panel displayPanel = new Panel();
	    add(displayPanel, "North");
	    displayPanel.setLayout(new BorderLayout(15, 15));
	    displayPanel.setSize(420, 200);

	    Panel originalPanel = new Panel();
	    originalPanel.setSize(200, 200);
	    displayPanel.add(originalPanel, "West");

	    Panel compressedPanel = new Panel();
	    compressedPanel.setSize(200, 200);
	    displayPanel.add(compressedPanel, "East");

	    ImageSequence source = new ImageSequence(_toplevel, "Source");
            source.setBaseURL(getDocumentBase());

            ImagePartition part = new ImagePartition(_toplevel, "Part");

	    HTVQEncode encode = new HTVQEncode(_toplevel, "Encoder");
            encode.setBaseURL(getDocumentBase());

	    VQDecode decode = new VQDecode(_toplevel, "Decoder");
            decode.setBaseURL(getDocumentBase());

	    ImageUnpartition unpart =
		new ImageUnpartition(_toplevel, "Unpart");

	    ImageDisplay consumer = new ImageDisplay(_toplevel, "Compressed");
	    consumer.setPanel(compressedPanel);

	    ImageDisplay original = new ImageDisplay(_toplevel, "Original");
	    original.setPanel(originalPanel);

	    TypedIORelation r;
            r = (TypedIORelation) _toplevel.connect(
                    (TypedIOPort)source.getPort("image"),
                    (TypedIOPort)part.getPort("image"), "R1");
            ((TypedIOPort)original.getPort("image")).link(r);

            r = (TypedIORelation) _toplevel.connect(
                    (TypedIOPort)part.getPort("partition"),
                    (TypedIOPort)encode.getPort("imagepart"), "R2");

            r = (TypedIORelation) _toplevel.connect(
                    (TypedIOPort)encode.getPort("index"),
                    (TypedIOPort)decode.getPort("index"), "R3");

            r = (TypedIORelation) _toplevel.connect(
                    (TypedIOPort)decode.getPort("imagepart"),
                    (TypedIOPort)unpart.getPort("partition"), "R4");

            r = (TypedIORelation) _toplevel.connect(
                    (TypedIOPort)unpart.getPort("image"),
                    (TypedIOPort)consumer.getPort("image"), "R5");

        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     */
    protected void _go() {
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener executes the system when any parameter is changed.
     */
    class ParameterListener implements QueryListener {
        public void changed(String name) {
            _go();
        }
    }
}
