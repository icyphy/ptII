/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import java.util.Iterator;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

//////////////////////////////////////////////////////////////////////////
//// UpdatedValueIcon
/**
An icon that displays the value of an attribute of the container,
updating it as the value of the value of the attribute is updated.
The attribute is assumed to be an instance of Settable, and its name
is given by the parameter <i>attributeName</i>.  The width of the
display is fixed, and is given by the attribute <i>displayWidth</i>,
which is in "n" characters.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class UpdatedValueIcon extends AttributeValueIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public UpdatedValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to identify the named attribute in the
     *  container and to attach a listener to it.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == attributeName) {
            // If we were previously associated with an attribute,
            // remove the listener.
            if (_associatedAttribute != null) {
                _associatedAttribute.removeValueListener(this);
                _associatedAttribute = null;
            }
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                Attribute candidateAttribute = container.getAttribute(
                        attributeName.getExpression());
                if (candidateAttribute instanceof Settable) {
                    _associatedAttribute = (Settable)candidateAttribute;
                    _associatedAttribute.addValueListener(this);
                }
            }
            _updateFigures();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        UpdatedValueIcon newObject = (UpdatedValueIcon)super.clone(workspace);
        newObject._associatedAttribute = null;
        // Find the new associated attribute for the clone.
        try {
            newObject.attributeChanged(newObject.attributeName);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Create a new background figure.  This overrides the base class
     *  to draw a fixed-width box.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        // Measure width of a character.  Unfortunately, this
        // requires generating a label figure that we will not use.
        LabelFigure label = new LabelFigure("m",
                _labelFont, 1.0, SwingConstants.CENTER);
        Rectangle2D stringBounds = label.getBounds();
        try {
            int numberOfCharacters
                = ((IntToken)displayWidth.getToken()).intValue();
            // NOTE: Padding of 20.
            int width = (int)
                (stringBounds.getWidth() * numberOfCharacters + 20);
            return new BasicRectangle(0, 0, width, 30, Color.white, 1);
        } catch (IllegalActionException ex) {
            // Should not be thrown.
            throw new InternalErrorException(ex);
        }
    }

    /** React to the specified Settable has changing by requesting a
     *  repaint of the most recently constructed figures.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        if (settable == _associatedAttribute) {
            _updateFigures();
        } else {
            super.valueChanged(settable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the figures that were created by this icon to reflect the 
     *  new attribute value.  This method is called by this class in response
     *  to notification that attributes have changed.
     */
    protected void _updateFigures() {
        // Invoke in the swing thread.
        // NOTE: These requests could be consolidated, so that
        // if there is a string of them pending, only one gets
        // executed. However, this results in jerky updates, with
        // some values being skipped, so it seems like it's not
        // a good idea.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String string = _displayString();
                    Iterator figures = _liveFigureIterator();
                    while (figures.hasNext()) {
                        LabelFigure figure = (LabelFigure)figures.next();
                        figure.setString(string);
                    }
                }
            });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The attribute whose value is being represented. */
    protected Settable _associatedAttribute;
}
