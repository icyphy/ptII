/* A tableau representing an image token display.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.image;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.actor.gui.TokenTableau;
import ptolemy.data.ImageToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.media.Picture;

///////////////////////////////////////////////////////////////////
//// TokenTableau

/**
 A tableau representing an image displayed in a top-level window.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see TokenEffigy
 */
public class ImageTableau extends TokenTableau {
    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public ImageTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a new tableau for the model represented by the given effigy,
     *  using the specified frame.
     *  @param container The container.
     *  @param name The name.
     *  @param frame The frame to use.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public ImageTableau(Effigy container, String name, TableauFrame frame)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, frame);
    }

    /** Construct a new tableau for the model represented by the given effigy,
     *  using the specified frame.
     *  @param container The container.
     *  @param name The name.
     *  @param frame The frame to use.
     *  @param width The width of the picture display in pixels.
     *  @param height The height of the picture display in pixels.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public ImageTableau(Effigy container, String name, TableauFrame frame,
            int width, int height) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, frame);
        _oldxsize = width;
        _oldysize = height;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display the specified token.
     *  If the token is not an ImageToken, do nothing.
     *  @param token The token to display.
     *  @exception IllegalActionException If the token is not an ImageToken.
     */
    @Override
    public void append(Token token) throws IllegalActionException {
        if (token instanceof ImageToken) {
            display((ImageToken) token);
        }
    }

    /** Display the specified tokens.
     *  If the display is not a MatrixPane, or the tokens are not
     *  instances of MatrixToken, do nothing.
     *  @exception IllegalActionException If the tokens are not
     *  instances of ImageToken.
     */
    @Override
    public void append(List list) throws IllegalActionException {
        Iterator tokens = list.iterator();

        while (tokens.hasNext()) {
            Object token = tokens.next();

            if (token instanceof ImageToken) {
                display((ImageToken) token);
            }
        }
    }

    /** Return true if this tableau can display the specified token.
     *  @param token A candidate token to display.
     *  @return True, since this tableau can display any token.
     */
    public static boolean canDisplay(Token token) {
        if (token instanceof ImageToken) {
            return true;
        } else {
            return false;
        }
    }

    /** Clear the display.
     */
    @Override
    public void clear() {
        if (_picture != null) {
            // FIXME: How to do this?
            // _picture.clear();
        }
    }

    /** Create an image display to view the picture.
     *  This is called in the constructor.
     *  @param frame The frame to use, or null if none is specified.
     *  @exception IllegalActionException If the frame cannot be created.
     */
    @Override
    public void createFrame(TableauFrame frame) throws IllegalActionException {
        TokenEffigy effigy = (TokenEffigy) getContainer();

        if (frame == null) {
            // The second argument prevents a status bar.
            frame = new TableauFrame(this, null);
        }

        setFrame(frame);
        _picture = new Picture(_oldxsize, _oldysize);
        frame.getContentPane().add(_picture, BorderLayout.CENTER);

        // Display current data.
        Iterator tokens = effigy.getTokens().iterator();

        while (tokens.hasNext()) {
            Object token = tokens.next();
            display((ImageToken) token);
        }
    }

    /** Display the specified token.
     *  @param token The token to append.
     *  @exception IllegalActionException If the token is null or
     *  not an ImageToken.
     */
    public void display(ImageToken token) throws IllegalActionException {
        Image image = token.asAWTImage();

        if (image == null) {
            throw new IllegalActionException(this,
                    "ImageTableau: input image was null!");
        } else {
            int xsize = image.getWidth(null);
            int ysize = image.getHeight(null);

            if (_oldxsize != xsize || _oldysize != ysize) {
                if (_debugging) {
                    _debug("Image size has changed.");
                }

                _oldxsize = xsize;
                _oldysize = ysize;

                Container container = _picture.getParent();
                Container top = container.getParent();

                while (top.getParent() != null) {
                    top = top.getParent();
                }

                JFrame castTop = (JFrame) top;

                castTop.getContentPane().remove(_picture);

                _picture = new Picture(xsize, ysize);
                _picture.setImage(image);
                _picture.setBackground(null);

                // FIXME: This messes up the menus!
                castTop.getContentPane().add(_picture, BorderLayout.CENTER);

                // FIXME: All the below appear to be required only by superstition.
                castTop.getContentPane().validate();

                // container.invalidate();
                // container.repaint();
                // container.doLayout();
                castTop.pack();
            } else {
                _picture.setImage(image);
            }

            // display it.
            _picture.displayImage();
            _picture.repaint();

            Thread.yield();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The horizontal size of the previous image. */
    private int _oldxsize = 0;

    /** The vertical size of the previous image. */
    private int _oldysize = 0;

    /** The frame for displaying the image. */
    private Picture _picture;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a token tableau.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "tokenTableau", then return that tableau; otherwise, create
         *  a new instance of TokenTableau in the specified
         *  effigy, and name it "tokenTableau".  If the specified
         *  effigy is not an instance of TokenEffigy, then do not
         *  create a tableau and return null. It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy, which is expected to be a TokenEffigy.
         *  @return An instance of TokenTableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof TokenEffigy) {
                // First see whether the effigy already contains an
                // TokenTableau.
                TokenTableau tableau = (TokenTableau) effigy
                        .getEntity("tokenTableau");

                if (tableau != null) {
                    return tableau;
                }

                // NOTE: Normally need to check effigy tokens for
                // compatibility here, but they are always compatible,
                // so we don't bother.
                return new TokenTableau(effigy, "tokenTableau");
            }

            return null;
        }
    }
}
