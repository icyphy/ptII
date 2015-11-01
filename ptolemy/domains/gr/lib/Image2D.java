/* Create a figure from a user specified image file.

 Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIALS DAMAGES
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
package ptolemy.domains.gr.lib;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.toolbox.ImageFigure;

///////////////////////////////////////////////////////////////////
//// Image2D

/**
 Create a figure from a user specified image file in GIF, JPEG, or PNG
 format.

 @author Ismael M. Sarmiento, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (ismael)
 @Pt.AcceptedRating Yellow (chf)
 */
public class Image2D extends GRActor2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Image2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileOrURL = new FileParameter(this, "fileOrURL");

        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(Scene2DToken.TYPE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The location of the image file upon which to base the figure.
     */
    public FileParameter fileOrURL;

    /** The output port that produces the figure.
     */
    public TypedIOPort sceneGraphOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the imageFigure.
     *  @exception IllegalActionException If the base class throws such
     *  an exception.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _figure = _createFigure();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the figure for this actor based on the user specified image
     *  file.
     *  @return The ImageFigure containing the image.
     *  @exception IllegalActionException If the selected file does not exist.
     */
    protected ImageFigure _createFigure() throws IllegalActionException {
        URL url = fileOrURL.asURL();

        Image image = Toolkit.getDefaultToolkit().createImage(url);

        ImageFigure figure = new ImageFigure(image);

        return figure;
    }

    /** Set up the scene graph connections of this actor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new Scene2DToken(_figure));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The figure which contains the image file. */
    protected ImageFigure _figure;
}
