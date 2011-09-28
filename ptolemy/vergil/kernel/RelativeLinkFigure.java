/*

Copyright (c) 2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.vergil.kernel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import ptolemy.kernel.util.RelativeLocation;

import diva.canvas.AbstractFigure;

/**
 * A figure for drawing a link between a relative locatable and its referenced object.
 * The link is represented by a straight thin line.
 * 
 * FIXME: Some artifacts are visible when the relative locatable object is dragged, because
 *        the clipping region seems not to be updated quickly enough.
 *
 * @author Miro Spoenemann
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (msp)
 * @Pt.AcceptedRating Red (msp)
 */
public class RelativeLinkFigure extends AbstractFigure {
    
    /**
     * Construct a figure to draw the link of a relative locatable object.
     * 
     * @param location The location of the relative locatable object.
     */
    public RelativeLinkFigure(RelativeLocation location) {
        _relativeLocation = location;
        _line = new Line2D.Double();
        String relativeToName = _relativeLocation.relativeTo.getExpression();
        // If the relativeTo reference is empty, then the location is absolute.
        if (relativeToName != null && relativeToName.length() > 0) {
            double[] offset = _relativeLocation.getRelativeLocation();
            _line.x2 = -offset[0];
            _line.y2 = -offset[1];
        }
    }

    /** Get the outline shape of this figure. This implementation returns a line.
     * 
     * @return A line, which may have length 0 if the related object has no valid reference.
     */
    @Override
    public Shape getShape() {
        return _line;
    }

    /** Paint the figure. This implementation paints a line if the related object has
     *  a valid reference, and it paints nothing if it hasn't. If the length of the
     *  line exceeds a specific threshold, it is drawn with a different color to
     *  highlight that the reference will eventually be broken.
     * 
     * @param g The graphics context used for painting.
     */
    @Override
    public void paint(Graphics2D g) {
        String relativeToName = _relativeLocation.relativeTo.getExpression();
        if (relativeToName != null && relativeToName.length() > 0) {
            // The line should span from the current location of the parent object,
            // found at (0,0) due to the transform applied to the graphics, to the
            // current location of the referenced object, which is at the negative
            // relative location in the local transform.
            double[] offset = _relativeLocation.getRelativeLocation();
            _line.x2 = -offset[0];
            _line.y2 = -offset[1];
            double distance = Math.sqrt(_line.x2 * _line.x2 + _line.y2 * _line.y2);
            if (distance <= RelativeLocation.BREAK_THRESHOLD) {
                g.setColor(NORMAL_COLOR);
            } else {
                g.setColor(THRESHOLD_COLOR);
            }
            g.setStroke(STROKE);
            g.draw(_line);
        } else {
            // The relative location does not have a valid reference, so reset
            // the cached location vector and draw nothing.
            _line.x2 = 0;
            _line.y2 = 0;
        }
    }

    /** Do nothing. FIXME: Should we do anything here?
     *
     * @param at an affine transformation
     */
    @Override
    public void transform(AffineTransform at) {
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The relative location represented by this figure. */
    private RelativeLocation _relativeLocation;
    
    /** The line used for drawing. */
    private Line2D.Double _line;
    
    /** The stroke used for drawing the line. */
    private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND, 1.0f, new float[] { 2.0f, 2.0f }, 0.0f);
    
    /** The normal color of the line. */
    private static final Color NORMAL_COLOR = new Color(180, 180, 0);
    
    /** The color used when the line is longer that a specific threshold. */
    private static final Color THRESHOLD_COLOR = new Color(250, 50, 0);

}
