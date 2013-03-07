/* BoundingBox class is simple sink that accepts bounding coordinates from
 * the server and passes them to the video class.

 Copyright (c) 2011-2012 The Regents of the University of California.
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
import ptolemy.data.MatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// BoundingBox
/**
 * BoundingBox class is simple sink that accepts bounding coordinates from
 * the server and passes them to the video class.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class BoundingBox extends TypedAtomicActor {

    /**
     * Creates new bounding box actor.
     * @param container The parent container.
     * @param name The name of the actor.
     * @exception IllegalActionException if there is a problem instantiating the object.
     * @exception NameDuplicationException if there is a problem instantiating the object.
     */
    public BoundingBox(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new TypedIOPort(this, "input", true, false);
        _input.setTypeEquals(BaseType.GENERAL);
    }

    /**
     * Read matrix token from the port containing four coordinates
     * and pass them to the video actor.
     *
     * Note: the actor is hardcoded to find video actor within it's container.
     */
    @Override
    public void fire() throws IllegalActionException {
        MatrixToken result = (MatrixToken) _input.get(0);
        CompositeEntity container = (CompositeEntity) this.getContainer();
        Video video = (Video) container.getEntity("Video");

        double[][] coords = result.doubleMatrix();
        assert coords.length == 4;
        video.updateBoundingBox((float) coords[0][0], (float) coords[1][0],
                (float) coords[2][0], (float) coords[3][0]);
    }

    /**
     * The input port of the actor accepting bounding box coordinates.
     */
    private TypedIOPort _input;
}
