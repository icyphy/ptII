/* Basic applet that constructs a Ptolemy II model from a MoML file.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.schematic.editor;

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
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.*;

//////////////////////////////////////////////////////////////////////////
//// MoMLViewerApplet
/**
This is an applet that constructs a Ptolemy II model from a MoML file.
"MoML" stands for "Modeling Markup Language." It is an XML language for
contructing Ptolemy II models.
The applet parameters are:
<ul>
<li><i>background</i>: The background color, typically given as a hex
number of the form "#<i>rrggbb</i>" where <i>rr</i> gives the red
component, <i>gg</i> gives the green component, and <i>bb</i> gives
the blue component.
<li><i>model</i>: The name of a URI (or URL) containing the
MoML file that defines the model.
<li><i>runControls</i>: The number of run controls to put on the screen.
The value must be an integer.
If the value is greater than zero, then a "Go" button
created.  If the value is greater than one, then a "Stop" button
is also created.
</ul>
Any entity that is created in parsing the MoML file that implements
the Placeable interface is placed in the applet.  Thus, entities
with visual displays automatically have their visual displays
appearing in the applet.
<p>
If the top-level object in the MoML file is an instance of
TypedCompositeActor, then the _toplevel protected member is set
to refer to it, and an instance of Manager is created for it.
Otherwise, the _toplevel member will be null.

@author  Edward A. Lee
@version $Id$
*/
public class MoMLViewerApplet extends MoMLApplet {
    /** Create a MoML parser and parse a file.
     */
    public void init() {
        super.init();
        JGraphViewer graphViewer = new JGraphViewer(_toplevel);
        graphViewer.setMinimumSize(new Dimension(200, 100));
        graphViewer.setPreferredSize(new Dimension(200, 100));
        getContentPane().add(graphViewer, BorderLayout.NORTH);
    }
}
