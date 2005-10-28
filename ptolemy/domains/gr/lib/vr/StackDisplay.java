/* Display a stack of images using NIH ImageJ.

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

import ij.ImagePlus;
import ij.gui.StackWindow;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// StackDisplay
public class StackDisplay extends Sink {
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

        return newObject;
    }

    /** Fire this actor.
     *  Display stack in ImageJ StackWindow
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("StackDisplay actor firing");
        }

        if (input.hasToken(0)) {
            ObjectToken objectToken = (ObjectToken) input.get(0);

            //ImageToken imageToken;
            ImagePlus imagePlus;
            imagePlus = (ImagePlus) objectToken.getValue();

            //FIXME What type of catch do I need?
            /*   try {
             imageToken = (ImageToken) token;
             } catch (ClassCastException ex) {
             throw new IllegalActionException(this, ex,
             "Failed to cast " + token.getClass()
             + " to an ImageToken.\nToken was: " + token);
             }*/
            /*_frame = */ new StackWindow(imagePlus);
        }
    }

    /** Initialize this actor.
     *  If place has not been called, then create a frame to display the
     *  image in.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        //_oldxsize = 0;
        //        _oldysize = 0;
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A specification of the size of the pane if it is in its own window. */
    protected SizeAttribute _paneSize;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The frame, if one is used. */
    //private StackWindow _frame;

    //private int _index = 0;
}
