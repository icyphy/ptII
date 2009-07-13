/*
 * An icon that displays a LatticeElement.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All rights
 * reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.domains.properties;

import java.awt.Color;
import java.awt.Paint;

import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.fsm.StateIcon;

/**
 * An icon that displays the name of the container in an appropriately sized
 * rounded box. This is designed to be contained by an instance of
 * LatticeElement.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeElementIcon extends StateIcon {

    /**
     * Create a new icon with the given name in the given container. The
     * container is required to implement Settable, or an exception will be
     * thrown.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If thrown by the parent class or while
     * setting an attribute.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public LatticeElementIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /**
     * Return the fill color. It returns the solution color of the lattice
     * element. If the lattice element is an unacceptable solution, return a
     * darker version of the solution color. If the solution color is not
     * specified, or if there is any problem getting it, this returns the color
     * white by default.
     * @return The fill color.
     */
    @Override
    protected Paint _getFill() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element.isAcceptableSolution
                        .getToken()).booleanValue();

                if (!isAcceptable) {
                    return element.solutionColor.asColor().darker();
                } else {
                    return element.solutionColor.asColor();
                }
            } catch (IllegalActionException ex) {
                // Ignore and return the default.
            }
        }
        return Color.white;
    }

    /**
     * Return the line width. If the lattice element is an unacceptable
     * solution, this returns a thicker width.
     * @return The line width.
     */
    @Override
    protected float _getLineWidth() {
        NamedObj container = getContainer();

        if (container instanceof LatticeElement) {
            try {
                LatticeElement element = (LatticeElement) container;

                boolean isAcceptable = ((BooleanToken) element.isAcceptableSolution
                        .getToken()).booleanValue();

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
