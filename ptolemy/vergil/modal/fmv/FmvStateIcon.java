/* An icon specialized for states of a state machine with reachability and risk analysis.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal.fmv;

import java.awt.Color;
import java.awt.Paint;

import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.fmv.FmvState;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.modal.StateIcon;

/**
An icon specialized for states of a state machine with reachability and risk analysis.

@author Chihhong Patrick Cheng
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (patrickj)
@Pt.AcceptedRating Red (patrickj)
 */
public class FmvStateIcon extends StateIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FmvStateIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Return the paint to use to fill the icon.
     *  This class returns Color.white, unless the refinement name
     *  is not empty, in which case it returns a light green.
     *  @return The paint to use to fill the icon.
     */
    @Override
    protected Paint _getFill() {
        NamedObj container = getContainer();
        if (container instanceof FmvState) {
            try {
                if (((BooleanToken) ((FmvState) container).isReachabilityAnalysisState
                        .getToken()).booleanValue()) {
                    if (((BooleanToken) ((FmvState) container).isRiskAnalysisState
                            .getToken()).booleanValue()) {
                        // RED and GREEN : Use yellow color for the case where both two specification exist.
                        return Color.ORANGE;
                    } else {
                        // GREEN
                        return Color.GREEN;
                    }
                } else {
                    if (((BooleanToken) ((FmvState) container).isRiskAnalysisState
                            .getToken()).booleanValue()) {
                        // RED
                        return Color.RED;
                    } else {
                        // WHITE
                        return Color.white;
                    }
                }

            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return Color.white;
    }

}
