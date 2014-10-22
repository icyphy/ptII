/* An icon that displays a Concept.
 *
 * Below is the copyright agreement for the Ptolemy II system.
 *
 * Copyright (c) 2009-2014 The Regents of the University of California. All rights
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
package ptolemy.vergil.ontologies;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.NameIcon;

/** An icon that displays the name of the container in an appropriately sized
 *  rounded box. This is designed to be contained by an instance of Concept.
 *
 *  @author Ben Lickly, Edward A. Lee, Elizabeth Latronico, Charles Shelton, Man-kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConceptIcon extends NameIcon {

    /** Create a new icon with the given name in the given container.
     * The container is required to implement Settable, or an exception will be
     * thrown.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If thrown by the parent class or while
     * setting an attribute.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public ConceptIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Change the default rounding to 20.
        rounding.setExpression("20.0");
    }

    /** Return the fill color.
     *  This will return the color specified by the first instance of
     *  ColorAttribute in the container, or white if there is no such instance.
     *  @return The fill color.
     */
    @Override
    protected Paint _getFill() {
        NamedObj container = getContainer();
        List<ColorAttribute> colors = container
                .attributeList(ColorAttribute.class);
        if (colors.size() > 0) {
            return colors.get(0).asColor();
        }
        return Color.white;
    }

    /** Return the line width.
     *  If the concept is an unacceptable solution, this returns
     *  a thicker width.
     *  @return The line width.
     */
    @Override
    protected float _getLineWidth() {
        NamedObj container = getContainer();
        if (container instanceof FiniteConcept) {
            try {
                Concept element = (Concept) container;

                boolean isAcceptable = ((BooleanToken) element.isAcceptable
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
