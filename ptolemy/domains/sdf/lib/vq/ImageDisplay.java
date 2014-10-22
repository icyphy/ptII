/* Display an Black and White image on the screen using the Picture class.

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
package ptolemy.domains.sdf.lib.vq;

import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageDisplay

/**
 Display an image on the screen using the ptolemy.media.Picture
 class.  For a sequence of images that are all the same size, this
 class will continually update the picture with new data.  If the
 size of the input image changes, then a new Picture object is
 created.  This class will only accept a IntMatrixToken on its
 input, and assumes that the input image contains greyscale pixel
 intensities between 0 and 255 (inclusive).  The token is
 read in postfire().

 <p>Note that this actor really should be replaced by a conversion
 actor that converts IntMatrixTokens to ImageTokens.  However,
 there is no easy way to do that without accessing the graphical
 context of the actor.  An alternative would be to use the
 Java Advanced Imaging package and create an actor like
 $PTII/ptolemy/actor/lib/jai/DoubleMatrixToJAI.java.

 @author Steve Neuendorffer, Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red
 */
public class ImageDisplay extends ptolemy.actor.lib.image.ImageDisplay {
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

        input.setTypeEquals(BaseType.INT_MATRIX);
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
    @Override
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
