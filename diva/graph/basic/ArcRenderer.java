/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.graph.basic;

import java.awt.Font;

import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.EdgeRenderer;

/**
 * An EdgeRenderer that draws arcs. To do so, it creates a new
 * instance of ArcConnector and initializes it.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class ArcRenderer implements EdgeRenderer {
    /** Render a visual representation of the given edge.
     */
    @Override
    public Connector render(Object edge, Site tailSite, Site headSite) {
        // FIXME: Find a way to set the curvature (the third argument).
        ArcConnector c = new ArcConnector(tailSite, headSite);
        Arrowhead arrow = new Arrowhead(headSite.getX(), headSite.getY(),
                headSite.getNormal());
        c.setHeadEnd(arrow);

        Object p = "edge"; //edge.getProperty("label");
        String label = p.toString();
        LabelFigure labelFigure = new LabelFigure(label);
        String fontname = labelFigure.getFont().getFontName();
        labelFigure.setFont(new Font(fontname, Font.ITALIC, 14));
        c.setLabelFigure(labelFigure);
        return c;
    }
}
