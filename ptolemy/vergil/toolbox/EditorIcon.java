/* An Icon is the graphical representation of a schematic entity.

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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// EditorIcon
/**

An icon is the graphical representation of a schematic entity.
EditorIcons are stored hierarchically in icon libraries.   Every icon has a
name, along with a graphical representation.

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
        label.setSize(10);
	label.setPadding(1);
	label.setAnchor(SwingConstants.SOUTH_WEST);
	label.translateTo(backBounds.getX(), backBounds.getY());
        ((CompositeFigure)figure).add(label);
	return figure;
    }

    /**
     * Create the background figure based on this icon.
     */
    public Figure createBackgroundFigure() {
	return _createDefaultBackgroundFigure();
    }

    /**
     * The default background figure, if nothing else is available.
     */
    protected Figure _createDefaultBackgroundFigure() {
	// FIXME better default.
	return new BasicRectangle(0, 0, 20, 20, Color.green);
    }

}
