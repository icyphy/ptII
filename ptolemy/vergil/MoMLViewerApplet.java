/* Basic applet that constructs a Ptolemy II model from a MoML file.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.vergil;

import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Enumeration;
import javax.swing.*;
import java.awt.*;

import com.microstar.xml.XmlException;

import ptolemy.gui.*;
import ptolemy.actor.Configurable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.MoMLApplet;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.*;

//////////////////////////////////////////////////////////////////////////
//// MoMLViewerApplet
/**
A moml applet with visualization of the hierarchy.  In addition to the
features of MoMLApplet, this class adds a JModelViewer to the applet which
is a view of the toplevel structure of the model.

@see MoMLApplet
@author  Edward A. Lee
@version $Id$
*/
public class MoMLViewerApplet extends MoMLApplet {
    /** Create a MoML parser and parse a file.
     */
    public void init() {
        super.init();
        JModelViewer modelViewer = new JModelViewer(_toplevel);
        modelViewer.setMinimumSize(new Dimension(200, 100));
        modelViewer.setPreferredSize(new Dimension(200, 100));
        getContentPane().add(modelViewer, BorderLayout.NORTH);
    }
}
