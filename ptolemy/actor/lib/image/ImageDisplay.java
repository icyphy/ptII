/* Display a java.awt.Image

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.image;

import java.awt.Color;
import java.awt.Container;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.lib.Sink;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ImageDisplay

/**
 Display an image on the screen using the ptolemy.media.Picture
 class.  For a sequence of images that are all the same size, this class
 will continually update the picture with new data.   If the size of the
 input image changes, then a new Picture object is created.  This class
 will only accept an ImageToken on its input.

 @author James Yeh, Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red
 */
public class ImageDisplay extends Sink implements Placeable {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: This is required to be an ImageToken, but
        // we don't see to have that class.
        input.setTypeEquals(BaseType.OBJECT);
        _getImplementation().initWindowAndSizeProperties();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Free up memory when closing. */
    public void cleanUp() {
        _getImplementation().setFrame(null);
        _getImplementation().cleanUp();
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then removes association with graphical objects
     *  belonging to the original class.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ImageDisplay newObject = (ImageDisplay) super.clone(workspace);

        newObject._implementation = null;
        try {
            // See _getImplementation():
            if (PtolemyInjector.getInjector() == null) {
                System.err
                        .println("Warning: main() did not call "
                                + "ActorModuleInitializer.initializeInjector(), "
                                + "so ImageDisplayInterface.clone() is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            newObject._implementation = PtolemyInjector.getInjector()
                    .getInstance(ImageDisplayInterface.class);
            newObject._implementation.init(newObject);
            newObject._implementation.initWindowAndSizeProperties();

        } catch (Exception e) {
            // This should not occur.
            throw new CloneNotSupportedException("Clone failed: " + e);
        }

        return newObject;
    }

    /** Get the background.
     *  @return The background color.
     *  @see #setBackground(Color)
     */
    public Color getBackground() {
        return _getImplementation().getBackground();
    }

    /** Initialize this actor.
     *  If place has not been called, then create a frame to display the
     *  image in.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _getImplementation().initializeEffigy();
    }

    /** Set the container that this actor should image display data in.  If place
     * is not called, then the actor will create its own frame for display.
     */
    @Override
    public void place(Container container) {
        _getImplementation().placeContainer(container);
    }

    /** Consume a token from the <i>input</i> port
     *  and display the token as an image.  If a token is not available,
     *  do nothing.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            final Token in = input.get(0);
            _getImplementation().display(in);
        }

        return super.postfire();
    }

    /** Set the background.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background) {
        _getImplementation().setBackground(background);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the right instance of the implementation depending upon the
     *  of the dependency specified through dependency injection.
     *  If the instance has not been created, then it is created.
     *  If the instance already exists then return the same.
     *
     *        <p>This code is used as part of the dependency injection needed for the
     *  HandSimDroid project, see $PTII/ptserver.  This code uses dependency
     *  inject to determine what implementation to use at runtime.
     *  This method eventually reads ptolemy/actor/ActorModule.properties.
     *  {@link ptolemy.actor.injection.ActorModuleInitializer#initializeInjector()}
     *  should be called before this method is called.  If it is not
     *  called, then a message is printed and initializeInjector() is called.</p>
     *
     *  @return the instance of the implementation.
     */
    protected ImageDisplayInterface _getImplementation() {
        if (_implementation == null) {
            if (PtolemyInjector.getInjector() == null) {
                System.err.println("Warning: main() did not call "
                        + "ActorModuleInitializer.initializeInjector(), "
                        + "so ImageDisplay is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            _implementation = PtolemyInjector.getInjector().getInstance(
                    ImageDisplayInterface.class);
            try {
                _implementation.init(this);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(this, e,
                        "Failed to initialize implementation");
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e,
                        "Failed to initialize implementation");
            }
        }
        return _implementation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Implementation of the ImageDisplayInterface
    private ImageDisplayInterface _implementation;

}
