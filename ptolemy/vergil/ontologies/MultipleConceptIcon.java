/* An icon that displays multiple concepts.
 *
 * Below is the copyright agreement for the Ptolemy II system.
 *
 * Copyright (c) 2011-2014 The Regents of the University of California. All rights
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

import javax.swing.Icon;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;

/** An icon that displays multiple concepts, in a set of rounded boxes, with
 *  the uppermost box containing the name of the set of concepts.
 *  This is designed to be used within an instance of
 *  {@link ptolemy.data.ontologies.FlatTokenRepresentativeConcept}.
 *
 *  @author Ben Lickly, Edward A. Lee, Elizabeth Latronico, Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class MultipleConceptIcon extends ConceptIcon {

    /** Create a new icon with the given name in the given container. The
     *  container is required to implement Settable, or an exception will be
     *  thrown.
     *  @param container The specified container.
     *  @param name The specified name.
     *  @exception IllegalActionException If thrown by the parent class or while
     *  setting an attribute.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     */
    public MultipleConceptIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        translation = new Parameter(this, "translation");
        translation.setTypeEquals(BaseType.DOUBLE);
        translation.setExpression("10.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The amount of translation to apply to the background figure.
     *  This is a double that defaults to 10.0.
     */
    public Parameter translation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.
     *  This overrides the base class to draw a set of backgrounds.
     *  @return A new figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        CompositeFigure composite = new CompositeFigure();
        Figure background = super.createBackgroundFigure();
        double translationValue = 10.0;
        try {
            translationValue = ((DoubleToken) translation.getToken())
                    .doubleValue();
        } catch (IllegalActionException e) {
            // Ignore and use default value.
            e.printStackTrace();
        }
        background.translate(translationValue, translationValue);
        composite.add(background);
        composite.add(super.createBackgroundFigure());
        return composite;
    }

    /** Create an icon.
     *  This overrides the base class to draw a set of boxes to represent
     *  multiple concepts.
     *  @return The icon.
     */
    @Override
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }
        CompositeFigure composite = new CompositeFigure();
        composite.add(new RoundedRectangle(0, 0, 20, 10, _getFill(), 1.0f, 5.0,
                5.0));
        composite.add(new RoundedRectangle(-5, -5, 20, 10, _getFill(), 1.0f,
                5.0, 5.0));
        _iconCache = new FigureIcon(composite, 20, 15);
        return _iconCache;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the label to put on the specified background
     *  figure based on the specified name.
     *  @param background The background figure on which to put the label.
     *  @param name The name on which to base the label.
     *  @return The label figure.
     */
    @Override
    protected LabelFigure _getLabel(CompositeFigure background, String name) {
        LabelFigure result = super._getLabel(background, name);
        double translationValue = 10.0;
        try {
            translationValue = ((DoubleToken) translation.getToken())
                    .doubleValue();
        } catch (IllegalActionException e) {
            // Ignore and use default value.
            e.printStackTrace();
        }
        result.translate(-0.5 * translationValue, -0.5 * translationValue);
        return result;
    }
}
