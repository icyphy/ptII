/* An icon that renders the value of the container.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import ptolemy.kernel.util.*;
import ptolemy.kernel.util.NamedObj.*;
import ptolemy.kernel.util.Settable.*;

import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;

//////////////////////////////////////////////////////////////////////////
//// ValueIcon
/**
An icon that displays the value of the container, which is assumed
to be an instance of Settable. 

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ValueIcon extends XMLIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public ValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a background figure based on this icon, which is a text
     *  element with the name of the container, a colon, and its value.
     *  @return A figure for this icon.
     */
    public Figure createBackgroundFigure() {
        return createFigure();
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  container name and value, separated by a colon.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        Settable container = (Settable)getContainer();
        String name = container.getName();
        String value = container.getExpression();
        LabelFigure label = new LabelFigure(name + ": " + value,
                _labelFont, 1.0, SwingConstants.SOUTH_WEST);
        return label;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
}
