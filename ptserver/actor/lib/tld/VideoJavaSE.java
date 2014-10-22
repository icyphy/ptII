/*
 Empty implementation of the VideoInterface for JavaSE platform.

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

import ptolemy.actor.injection.PortableContainer;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// JavaSEVideo

/**
 * Empty implementation of the VideoInterface for JavaSE platform.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class VideoJavaSE implements VideoInterface {

    /** Empty implementation.
     * @param container The container to hold the actor.
     */
    @Override
    public void place(PortableContainer container) {
    }

    /**
     * Initialize the interface - empty implementation.
     * @param video the video whose interface is initialized.
     */
    @Override
    public void init(Video video) {

    }

    /**
     * Callback for video.initialize() - empty implementation.
     * @exception IllegalActionException if there is problem initializing.
     */
    @Override
    public void initialize() throws IllegalActionException {

    }

    /**
     * Callback for video.stop() - empty implementation.
     */
    @Override
    public void stop() {

    }

    /**
     * Callback for video.fire() - empty implementation.
     * @exception IllegalActionException if there is problem initializing.
     */
    @Override
    public void fire() throws IllegalActionException {

    }

    /**
     * Callback for video.updateBoundingBox(float, float, float, float) - empty implementation.
     * @param x1 top left x
     * @param y1 top left y
     * @param x2 bottom right x
     * @param y2 bottom right y
     */
    @Override
    public void updateBoundingBox(float x1, float y1, float x2, float y2) {

    }

}
