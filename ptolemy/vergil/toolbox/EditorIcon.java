/* An Icon is the graphical representation of an entity.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.gui.toolbox.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// EditorIcon
/**
An icon represnts the visual representation of a schematic entity.
Every icon has a name, along with the data necessary for creating 
a visual representation.
EditorIcons are capable of creating a visual representation representing
the icon as either a Swing icon (e.g. an instance of javax.swing.Icon), 
or as a Diva figure (e.g. an instanceof of diva.canvas.Figure).
In general, one or the other will form the basis of the visual representation, 
and the other will be created from the first, using either a SwingWrapper
or a FigureIcon.  In other words, this class is a factory for visual
representations.
<p> In this base class, the visual representation as a Diva
figure is created by adding a label representing the name of the entity that
contains this icon to a background figure which is created by the 
createBackgroundFigure method.  The visual representation as a Swing icon
is created from the background figure using a FigureIcon.
Thus, most subclasses that which to modify the visual representation can 
simply override the createBackgroundFigure method.
<p> Subclasses that wish to create the figure or the icon in a different way
entirely (for example, starting with a Swing icon and creating the figure using
a SwingWrapper) should override both the createBackgroundFigure and 
createIcon methods.
<p>
This visual representation created by this base class is just a simple white
 box.  For a more interesting icon, see the XMLIcon class.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class EditorIcon extends Icon {

    /**
     * Create a new icon with the given name in the given container.
     * @param container The container.
     * @param name The name of the attribute.
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container.
     * @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public EditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Create a new Diva figure that visually represents this icon. 
     * The figure will be an instance of
     * CompositeFigure with the figure returned by createBackgroundFigure
     * as its background.  This method adds a LabelFigure to the
     * CompositeFigure that contains the name of the container of this icon.
     * Subclasses of this
     * class should never return null, even if the icon has not been properly
     * initialized. 
     * @return A new CompositeFigure.
     * @see diva.canvas.CompositeFigure
     */
    public Figure createFigure() {
	Figure background = createBackgroundFigure();
	Rectangle2D backBounds = background.getBounds();
        Figure figure = new CompositeFigure(background);
        Entity entity = (Entity) getContainer();
        LabelFigure label = new LabelFigure(entity.getName());
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
	label.setPadding(1);
	label.setAnchor(SwingConstants.SOUTH_WEST);
	label.translateTo(backBounds.getX(), backBounds.getY());
        ((CompositeFigure)figure).add(label);
	return figure;
    }

    /**
     * Create a new background figure based on this icon.  This should
     * manufacture a new figure each time, since figures are cheap and contain
     * their own location.  This base class returns a default background 
     * figure which is a simple white box.  Subclasses will generally override
     * this method to create more interesting figures.  Subclasses of this
     * class should never return null, even if the icon has not been properly
     * initialized. 
     * @return A new figure.
     */
    public Figure createBackgroundFigure() {
	return _createDefaultBackgroundFigure();
    }

    /** 
     * Create a new Swing icon that visually represents this icon.
     * The default implementation in this base class creates the Swing icon
     * from the background figure, so it is not necessary to override this 
     * method in most cases.  Note that the Swing icon does NOT include a
     * label for the name, since that is usually added separately in a
     * Swing component.
     * @return A new Swing Icon.
     */
    public javax.swing.Icon createIcon() {
	// In this class, we cache the rendered icon, since creating icons from
	// figures is expensive.
        if(_iconCache != null) {
	    return _iconCache;
        }
        
        // No cached object, so rerender the icon.
	Figure figure = createBackgroundFigure();
	_iconCache = new FigureIcon(figure, 20, 15);
	return _iconCache;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /**
     * Create a new default background figure.
     * Subclasses of this class should generally override 
     * the createBackgroundFigure method instead.  This method is provided
     * so that subclasses are always able to create a default figure even if
     * an error occurs or the subclass has not been properly initialized.
     * @return A figure representing a rectangular white box.
     */
    protected static Figure _createDefaultBackgroundFigure() {
	return new BasicRectangle(0, 0, 60, 40, Color.white, 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The cached Swing icon.
    private javax.swing.Icon _iconCache = null;
}
