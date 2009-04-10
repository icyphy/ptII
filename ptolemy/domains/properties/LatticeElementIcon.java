/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2009 The Regents of the University of California.
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
*/
package ptolemy.domains.properties;

import java.awt.Color;
import java.awt.Paint;

import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.fsm.StateIcon;

public class LatticeElementIcon extends StateIcon {

    public LatticeElementIcon(NamedObj container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    protected Paint _getFill() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element
                        .isAcceptableSolution.getToken()).booleanValue();

                if (!isAcceptable) {
                    return element.solutionColor.asColor().darker();
                } else {
                    return element.solutionColor.asColor();
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return Color.white;
    }

    protected float _getLineWidth() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element
                        .isAcceptableSolution.getToken()).booleanValue();

                if (!isAcceptable) {
                    return 3.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return 1.0f;
    }
}
