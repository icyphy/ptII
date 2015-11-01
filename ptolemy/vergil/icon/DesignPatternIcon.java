/* The icon for groups representing design patterns.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.Color;

import javax.swing.Icon;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CompositeFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.gui.toolbox.FigureIcon;

//////////////////////////////////////////////////////////////////////////
//// GroupIcon

/**
 The icon for groups representing design patterns.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DesignPatternIcon extends NameIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DesignPatternIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Create an icon.
     *
     *  @return The icon.
     */
    @Override
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        BasicRectangle figure1 = new BasicRectangle(0, 0, 15, 10,
                Color.darkGray, 1.0f);
        BasicRectangle figure2 = new BasicRectangle(2, 2, 15, 10, Color.gray,
                1.0f);
        BasicRectangle figure3 = new BasicRectangle(5, 5, 15, 10, _getFill(),
                1.0f);
        CompositeFigure figure = new CompositeFigure();
        figure.add(figure1);
        figure.add(figure2);
        figure.add(figure3);

        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    /** The cached Swing icon. */
    //protected Icon _iconCache = null;
}
