/*
 * An icon that displays a LatticeElement.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009-2010 The Regents of the University of California. All rights
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

import javax.swing.Icon;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.modal.StateIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;

/**
 * An icon that displays the name of the container in an appropriately sized
 * rounded box. This is designed to be contained by an instance of
 * LatticeElement.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class ConceptIcon extends StateIcon {

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
    public ConceptIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        isMultiple = new Parameter(this, "isMultiple");
        isMultiple.setTypeEquals(BaseType.BOOLEAN);
        isMultiple.setExpression("false");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator that the concept actually represents a multiplicity
     *  of concepts. This is boolean that defaults to false.
     */
    public Parameter isMultiple;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        try {
            if (((BooleanToken)isMultiple.getToken()).booleanValue()) {
                CompositeFigure composite = new CompositeFigure();
                Figure background = super.createBackgroundFigure();
                background.translate(10.0, 10.0);
                composite.add(background);
                composite.add(super.createBackgroundFigure());
                return composite;
            } else {
                return super.createBackgroundFigure();                
            }
        } catch (IllegalActionException e) {
            // If isMultiple cannot be evaluated, use the base class figure.
            return super.createBackgroundFigure();
        }
    }
    
    /** Create an icon. This overrides the base class to add visual
     *  indicator if <i>isMultiple</i> is true.
     *  @return The icon.
     */
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }
        try {
            if (((BooleanToken)isMultiple.getToken()).booleanValue()) {
                CompositeFigure composite = new CompositeFigure();
                composite.add(new RoundedRectangle(0, 0, 20, 10,
                        _getFill(), 1.0f, 5.0, 5.0));
                composite.add(new RoundedRectangle(-5, -5, 20, 10,
                        _getFill(), 1.0f, 5.0, 5.0));
                _iconCache = new FigureIcon(composite, 20, 15);
                return _iconCache;
            } else {
                return super.createIcon();
            }
        } catch (IllegalActionException e) {
            // If isMultiple cannot be evaluated, use the base class icon.
            return super.createIcon();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the fill color. This will return the color specified by
     *  the first instance of ColorAttribute in the container, or white
     *  if there is no such instance.
     *  @return The fill color.
     */
    protected Paint _getFill() {
        NamedObj container = getContainer();
        List<ColorAttribute> colors = container
                .attributeList(ColorAttribute.class);
        if (colors.size() > 0) {
            return colors.get(0).asColor();
        }
        return Color.white;
    }

    /** Get the label to put on the specified background
     *  figure based on the specified name.
     *  @param background The background figure on which to put the label.
     *  @param name The name on which to base the label.
     *  @return The label figure.
     */
    protected LabelFigure _getLabel(CompositeFigure background, String name) {
        try {
            if (((BooleanToken)isMultiple.getToken()).booleanValue()) {
                LabelFigure result = super._getLabel(background, name);
                result.translate(-5.0, -5.0);
                return result;
            } else {
                return super._getLabel(background, name);
            }
        } catch (IllegalActionException e) {
            // If isMultiple cannot be evaluated, use the base class icon.
            return super._getLabel(background, name);
        }
    }

    /** Return the line width. If the lattice element is an unacceptable
     *  solution, this returns a thicker width.
     *  @return The line width.
     */
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
