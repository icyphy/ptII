/* An icon that displays a specified java.awt.Image.

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

import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.ImageFigure;
import ptolemy.kernel.util.*;

import java.awt.Color;
import java.awt.Image;

//////////////////////////////////////////////////////////////////////////
//// ImageEditorIcon
/**
An icon that displays a specified java.awt.Image.
Note that this icon is not persistent, so an actor that uses
this icon should create it in its constructor.  It will not be
represented in the MoML file.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ImageEditorIcon extends EditorIcon {

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ImageEditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify an image to display.
     *  @param image The image to display.
     */
    public void setImage(Image image) {
        if (_imageFigure == null) {
            _imageFigure = new ImageFigure(image);
        } else {
            _imageFigure.repaint();
            _imageFigure.setImage(image);
            _imageFigure.repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new default background figure, which is a white box.
     *  Subclasses of this class should generally override
     *  the createBackgroundFigure method instead.  This method is provided
     *  so that subclasses are always able to create a default figure even if
     *  an error occurs or the subclass has not been properly initialized.
     *  @return A figure representing a rectangular white box.
     */
    protected Figure _createDefaultBackgroundFigure() {
        // NOTE: center at the origin.
        if (_imageFigure != null) {
            return _imageFigure;
        } else {
            return new BasicRectangle(-30, -30, 60, 60, Color.blue, 1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ImageFigure _imageFigure;
}
