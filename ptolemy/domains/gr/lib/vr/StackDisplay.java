/* Display a java.awt.Image

@Copyright (c) 1998-2005 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

PT_COPYRIGHT_VERSION 2
COPYRIGHTENDKEY
*/
package ptolemy.domains.gr.lib.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.Picture;

import ij.gui.StackWindow;
import ij.ImagePlus;
//////////////////////////////////////////////////////////////////////////
//// StackDisplay

/**
   Display an image on the screen using the ptolemy.media.Picture
   class.  For a sequence of images that are all the same size, this class
   will continually update the picture with new data.   If the size of the
   input image changes, then a new Picture object is created.  This class
   will only accept a IntMatrixToken on its input, and assumes that the
   input image contains greyscale pixel intensities between 0 and 255 (inclusive).

   @author James Yeh, Edward A. Lee
   @version $Id$
   @since Ptolemy II 3.0
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red
*/
public class StackDisplay extends Sink {
    // FIXME:
    // This actor and sdf.lib.vq.StackDisplay are very similar except that this
    // actor takes an Object token that wraps a java.awt.Image object.
    // That actor should be removed, and instead, we need an actor that
    // converts matrices to java.awt.Image.
    // FIXME: We need to create an ImageEffigy and ImageTableau,
    // similar to TokenEffigy and MatrixTokenTableau, and then associate
    // them with this class in ways similar to what MatrixViewer does.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StackDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);

        _oldxsize = 0;
        _oldysize = 0;
        _frame = null;
        _container = null;

        _windowProperties = new WindowPropertiesAttribute(this,
                "_windowProperties");

        _paneSize = new SizeAttribute(this, "_paneSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then removes association with graphical objects
     *  belonging to the original class.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StackDisplay newObject = (StackDisplay) super.clone(workspace);

        newObject._container = null;
        newObject._frame = null;
        newObject._oldxsize = 0;
        newObject._oldysize = 0;
        //newObject._picture = null;

        return newObject;
    }

    /** Fire this actor.
     *  Consume an IntMatrixToken from the input port.  If the image is
     *  not the same size as the previous image, or this is the first
     *  image, then create a new Picture object to represent the image,
     *  and put it in the appropriate container (either the container
     *  set using place, or the frame created during the initialize
     *  phase).
     *  Convert the pixels from greyscale to RGBA triples (setting the
     *  image to be opaque) and update the picture.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
      //  for(int i =0; i<50; i++){
                if (_debugging) {
                        _debug("StackDisplay actor firing");
                }

                if (input.hasToken(0)) {
                        ObjectToken objectToken = (ObjectToken) input.get(0);
           
                        //ImageToken imageToken;
                        ImagePlus imagePlus;
                        imagePlus = (ImagePlus)objectToken.getValue();
           
                        //FIXME What type of catch do I need?
                        /*   try {
                         imageToken = (ImageToken) token;
                         } catch (ClassCastException ex) {
                             throw new IllegalActionException(this, ex,
                        "Failed to cast " + token.getClass()
                        + " to an ImageToken.\nToken was: " + token);
                  }*/
           

                        //FIXME Do I need a container and a frame?
                        //_container = _frame = new StackWindow(imagePlus);
                        _frame = new StackWindow(imagePlus);
                        //_frame.showSlice(i);
        //                System.out.println("Slice shown = slice " + i );
                //}
        
        }
      
   }

    /** Get the background */
    public Color getBackground() {
        return _container.getBackground();
    }

    /** Initialize this actor.
     *  If place has not been called, then create a frame to display the
     *  image in.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _oldxsize = 0;
        _oldysize = 0;

        //FIXME Do I need a container and a frame?
       /*   if (_container == null) {
                  _container = _frame = new StackWindow(null);
            //_container = _frame.getContentPane();
        }

          if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }*/
    }

  

    /** Set the background */
    public void setBackground(Color background) {
        _container.setBackground(background);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A specification of the size of the pane if it is in its own window. */
    protected SizeAttribute _paneSize;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The container for the image display. */
    private Container _container;

    /** The frame, if one is used. */
    private StackWindow _frame;

    /** The horizontal size of the previous image. */
    private int _oldxsize = 0;

    /** The vertical size of the previous image. */
    private int _oldysize;

    // FIXME: Probably don't want to use Picture here.

    /** A panel that displays the image. */
    private Picture _picture;

    /** A specification for the window properties of the frame. */
    private WindowPropertiesAttribute _windowProperties;

    private int _index = 0;
   
}
