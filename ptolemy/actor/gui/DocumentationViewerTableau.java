/* A tableau representing a documentation window.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// FIXME: Trim this.
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// DocumentationViewerTableau
/**
A tableau representing the documentation of a ptolemy class.  
The class documentation that is viewed is given by the <i>dottedClass</i>
parameter.  This class assumes
that class documentation for all classes has been created in the
<code>doc/codeDoc</code> directory relative to some point in the classpath.
In other words, if <i>dottedClass</i>
<code>ptolemy.gui.DocumentationViewer</code>, then this class will attempt 
to load the resource 
<code>doc/codeDoc/ptolemy/gui/DocumentationViewer.html</code>.
To automatically create documentation for the ptolemy tree in 
ptII/doc (which should be in the classpath), run make in that directory.
<p>
The constructor of this
class creates the viewing window. The window itself is an instance
of HTMLViewer, and can be accessed using the getFrame() method.
As with other tableaux, this is an entity that is contained by
an effigy of a model.
There can be any number of instances of this class in an effigy.

@author  Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Effigy
*/
public class DocumentationViewerTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    public DocumentationViewerTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        dottedClass = new StringAttribute(this, "dottedClass");

        HTMLViewer frame = new HTMLViewer();
	setFrame(frame);
	frame.setTableau(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The class to display. */
    public StringAttribute dottedClass;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>url</i> parameter, then open the
     *  specified URL and display its contents.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be opened,
     *   or if the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dottedClass) {
            String className = ((Settable)attribute).getExpression();
	    String docName = "doc.codeDoc." + className;
	    try {
                // NOTE: This cannot handle a URL that is relative to the
                // MoML file within which this attribute might be being
                // defined.  Is there any way to do that?
                URL toRead = getClass().getClassLoader().getResource(
		    docName.replace('.', '/') + ".html");
                ((HTMLViewer)getFrame()).setPage(toRead);
            } catch (IOException ex) {
                throw new IllegalActionException(this,
                "Cannot find documentation for: " + docName + 
		"\n" + ex.toString() + 
		"\nTry Running \"make\" in ptII/doc.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. This calls the
     *  base class and then sets the <code>url</code>
     *  public members to the parameters of the new object.
     *  @param ws The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        DocumentationViewerTableau newObject =
                 (DocumentationViewerTableau)super.clone(ws);
        newObject.dottedClass = 
	    (StringAttribute)newObject.getAttribute("dottedClass");
        return newObject;
    }

    /** Make this tableau visible by raising or deiconifying its window, or
     *  making it visible if it has not been previously made visible.
     *  This overrides the base class to center the Tableau on the screen.
     *  If no frame has been set, then do nothing.
     */
    public void show() {
        Top frame = (Top)getFrame();
        // Have to pack before centering.
        frame.pack();
        frame.centerOnScreen();
        super.show();
    }
}
