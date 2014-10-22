/*
@Copyright (c) 2010-2014 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */
package ptdb.gui;

import java.util.ArrayList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// PTDBContainedFramesManager

/**
 * The manager to manage the contained frames inside the PTDB window frames.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class PTDBContainedFramesManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a new frame to the contained frame list in this manager.
     *
     * @param containedFrame The frame to be added into this frame as the
     * contained frame.
     */
    public void addContainedFrame(PTDBBasicFrame containedFrame) {

        if (_containedFrames == null) {
            _containedFrames = new ArrayList<PTDBBasicFrame>();
        }

        _containedFrames.add(containedFrame);
    }

    /**
     * Close all the contained frames in this manager.
     */
    public void closeContainedFrames() {

        if (_containedFrames != null) {
            for (PTDBBasicFrame containedFrame : _containedFrames) {
                if (containedFrame != null) {
                    containedFrame.closeFrame();
                }

            }
        }

    }

    /**
     * Get the list of frames contained in this frame.
     *
     * @return The list of contained frames in this frame.
     */
    public List<PTDBBasicFrame> getContainedFrames() {
        return _containedFrames;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List<PTDBBasicFrame> _containedFrames;

}
