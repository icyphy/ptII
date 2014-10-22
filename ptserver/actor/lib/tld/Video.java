/*
 This class output a video frame as ByteArrayToken via its output port
 from a camera.

 The interface implementation contains logic how to do this.  It also
 accepts bounding box coordinates that need to be overlayed on top
 of the video.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptserver.actor.lib.tld;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Video

/**
 * This class output a video frame as ByteArrayToken via its output port
 * from a camera.
 *
 * The interface implementation contains logic how to do this.  It also
 * accepts bounding box coordinates that need to be overlayed on top
 * of the video.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class Video extends TypedAtomicActor implements PortablePlaceable {

    /**
     * Create new instance of the Video.
     * @param container The parent container.
     * @param name The name of the actor.
     * @exception IllegalActionException if there is a problem instantiating the object.
     * @exception NameDuplicationException if there is a problem instantiating the object.
     */
    public Video(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.GENERAL);
        _interface.init(this);
    }

    /**
     * The output port for the video frames.
     */
    public TypedIOPort output;

    /**
     * Place the actor into provided container.
     * Actually implementation is contained within interface implementation.
     */
    @Override
    public void place(PortableContainer container) {
        _interface.place(container);
    }

    /** Initialize the actor.
     * Actually implementation is contained within interface implementation.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _interface.initialize();
    }

    /** Stop the actor.
     * Actually implementation is contained within interface implementation.
     */
    @Override
    public void stop() {
        super.stop();
        _interface.stop();
    }

    /** Fire the actor.
     * The actual implementation is contained within the interface implementation.
     */
    @Override
    public void fire() throws IllegalActionException {
        _interface.fire();
    }

    /** Update the bounding box coordinates.
     * The actual implementation is contained within the interface implementation.
     * @param x1 top left x
     * @param y1 top left y
     * @param x2 bottom right x
     * @param y2 bottom right y
     */
    public void updateBoundingBox(float x1, float y1, float x2, float y2) {
        _interface.updateBoundingBox(x1, y1, x2, y2);
    }

    /**
     * The interface implementation.
     */
    protected VideoInterface _interface = PtolemyInjector.getInjector()
            .getInstance(VideoInterface.class);

}
