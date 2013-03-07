/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
/*
 * FormDebugPanel.java
 *
 * Stolen from FormLayout and tweaked by Kevin Routley.
 *
 * Created on March 23, 2005, 9:14 PM
 */

package org.mlc.swing.layout;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.FormLayout;

/**
 * [Taken from the FormLayout codebase. Hacked to work with @see ContainerLayout
    and deactivate() added.]
 <p>
 * A panel that paints grid bounds if and only if the panel's layout manager
 * is a {@link FormLayout}. You can tweak the debug paint process by setting
 * a custom grid color, painting optional diagonals and painting the grid
 * in the background.<p>
 *
 * This class is not intended to be extended. However, it is not
 * marked as <code>final</code> to allow users to subclass it for
 * debugging purposes. In general it is recommended to <em>use</em> JPanel
 * instances, not <em>extend</em> them. You can see this implementation style
 * in the Forms tutorial classes. Rarely there's a need to extend JPanel;
 * for example if you provide a custom behavior for
 * <code>#paintComponent</code> or <code>#updateUI</code>.
 *
 * @author  Karsten Lentzsch
@version $Id$
@since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class FormDebugPanel extends JPanel {

    /**
     * The default color used to paint the form's debug grid.
     */
    private static final Color DEFAULT_GRID_COLOR = Color.red;

    /**
     * Specifies whether the grid shall be painted in the background.
     * Is off by default and so the grid is painted in the foreground.
     */
    private boolean paintInBackground;

    /**
     * Specifies whether the container's diagonals should be painted.
     */
    private boolean paintDiagonals;

    /**
     * Holds the color used to paint the debug grid.
     */
    private Color gridColor = DEFAULT_GRID_COLOR;

    // Instance Creation ****************************************************

    /**
     * Constructs a FormDebugPanel with all options turned off.
     */
    public FormDebugPanel() {
        this(null);
    }

    /**
     * Constructs a FormDebugPanel on the given FormLayout instance
     * that paints the grid in the foreground and paints no diagonals.
     *
     * @param layout  the panel's FormLayout instance
     */
    public FormDebugPanel(FormLayout layout) {
        this(layout, false, false);
    }

    /**
     * Constructs a FormDebugPanel on the given FormLayout
     * using the specified settings that are otherwise turned off.
     *
     * @param paintInBackground
     *     true to paint grid lines in the background,
     *     false to paint the grid in the foreground
     * @param paintDiagonals
     *     true to paint diagonals,
     *     false to not paint them
     */
    public FormDebugPanel(boolean paintInBackground, boolean paintDiagonals) {
        this(null, paintInBackground, paintDiagonals);
    }

    /**
     * Constructs a FormDebugPanel on the given FormLayout using
     * the specified settings that are otherwise turned off.
     *
     * @param layout
     *     the panel's FormLayout instance
     * @param paintInBackground
     *     true to paint grid lines in the background,
     *     false to paint the grid in the foreground
     * @param paintDiagonals
     *     true to paint diagonals,
     *     false to not paint them
     */
    public FormDebugPanel(FormLayout layout, boolean paintInBackground,
            boolean paintDiagonals) {
        super(layout);
        setPaintInBackground(paintInBackground);
        setPaintDiagonals(paintDiagonals);
        setGridColor(DEFAULT_GRID_COLOR);
    }

    // Accessors ************************************************************

    /**
     * Specifies to paint in background or foreground.
     *
     * @param b    true to paint in the background, false for the foreground
     */
    public void setPaintInBackground(boolean b) {
        paintInBackground = b;
    }

    /**
     * Enables or disables to paint the panel's diagonals.
     *
     * @param b    true to paint diagonals, false to not paint them
     */
    public void setPaintDiagonals(boolean b) {
        paintDiagonals = b;
    }

    /**
     * Sets the debug grid's color.
     *
     * @param color  the color used to paint the debug grid
     */
    public void setGridColor(Color color) {
        gridColor = color;
    }

    // Painting *************************************************************

    /**
     * Paints the component and - if background painting is enabled - the grid
     *
     * @param g   the Graphics object to paint on
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (paintInBackground) {
            paintGrid(g);
        }
    }

    /**
     * Paints the panel. If the panel's layout manager is a
     * FormLayout it paints the form's grid lines.
     *
     * @param g   the Graphics object to paint on
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (!paintInBackground) {
            paintGrid(g);
        }
    }

    // KBR Add flag to allow consumer control over gridlines
    private boolean deactivated = false;

    public void deactivate(boolean turnoff) {
        deactivated = turnoff;
        repaint();
    }

    /**
     * Paints the form's grid lines and diagonals.
     *
     * @param g    the Graphics object used to paint
     */
    private void paintGrid(Graphics g) {

        if (deactivated) {
            return;
        }

        if (!(getLayout() instanceof ContainerLayout)) {
            return;
        }

        // KBR hack to work with FLM
        ContainerLayout mylayout = (ContainerLayout) getLayout();
        FormLayout.LayoutInfo layoutInfo = mylayout.getLayoutInfo(this);

        //        FormLayout.LayoutInfo layoutInfo = FormDebugUtils.getLayoutInfo(this);
        int left = layoutInfo.getX();
        int top = layoutInfo.getY();
        int width = layoutInfo.getWidth();
        int height = layoutInfo.getHeight();

        g.setColor(gridColor);
        // Paint the column bounds.
        for (int columnOrigin : layoutInfo.columnOrigins) {
            g.fillRect(columnOrigin, top, 1, height);
        }

        // Paint the row bounds.
        for (int rowOrigin : layoutInfo.rowOrigins) {
            g.fillRect(left, rowOrigin, width, 1);
        }

        if (paintDiagonals) {
            g.drawLine(left, top, left + width, top + height);
            g.drawLine(left, top + height, left + width, top);
        }
    }

}
