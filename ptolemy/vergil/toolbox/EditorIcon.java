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

@ProposedRating Red (eal@eecs.berkeley.edu)
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

An icon is the graphical representation of a schematic entity.
EditorIcons are stored hierarchically in icon libraries.   Every icon has a
name, along with a graphical representation.
EditorIcons are capable of creating a visual representation representing
the icon as either a swing icon, or as a diva figure.
In general, one or the other will form the basis of the visual representation, 
and the other will be created from the first, using either a SwingWrapper
or a FigureIcon.  This class assumes that the figure forms the basis, 
so it is only necessary to override createBackgroundFigure.  If the reverse
is true, then you should override createBackgroundFigure and createIcon to
avoid unnecessarily converting to a figure and back again.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class EditorIcon extends Icon {

    /**
     * Create a new icon with the name "_icon" in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public EditorIcon(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        this(container, "_icon");
    }

    /**
     * Create a new icon with the given name in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public EditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Create a figure based on this icon.  The figure will be a
     * Composite Figure with the figure returned by createBackgroundFigure
     * as its background.
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
     * Create the background figure based on this icon.  This should
     * manufacture a new figure each time, since figures are cheap and contain
     * their own location.
     */
    public Figure createBackgroundFigure() {
	return _createDefaultBackgroundFigure();
    }

    /** 
     * Create a new graphical icon that represents this class visually.  This
     * method will generally want to cache the swing icon that is created,
     * since swing icons are expensive and don't contain state.
     * The default implementation in this base class creates the Icon
     * from the background figure, so it is not necessary to override this 
     * method in most cases.
     * @exception UnsupportedOperationException If a swing icon cannot be
     * created.
     */
    public javax.swing.Icon createIcon() {
        // First check to see if the icon has a rendering cached.
        NamedObj renderedObject=
            getAttribute("renderedIcon");
        if(renderedObject != null &&
                renderedObject instanceof Variable) {
            Variable renderedVariable = (Variable)renderedObject;
            try {
                ObjectToken token =
                    (ObjectToken)renderedVariable.getToken();
                return (javax.swing.Icon)token.getValue();
            } 
            catch (Exception ex) {
                // Ignore... we'll fall through to the next if
                // statement and rerender.
            }
        }
        
        // No cached object, so render the icon.
        try {
            Figure figure = createBackgroundFigure();
            javax.swing.Icon newIcon = new FigureIcon(figure, 20, 15);
            Variable renderedVariable = 
                new Variable(this, "renderedIcon");
            renderedVariable.setToken(new ObjectToken(newIcon));
            return newIcon;
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Icon could not create " +
                    "Swing Icon:" + ex.getMessage());
        }
    }

    /**
     * The default background figure, if nothing else is available.
     */
    protected static Figure _createDefaultBackgroundFigure() {
	return new BasicRectangle(0, 0, 60, 40, Color.white, 1);
    }

}
