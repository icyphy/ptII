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

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.AbstractConnector;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.connector.StraightConnector;
import diva.graph.EdgeRenderer;

/**
 * A basic implementation of the EdgeRenderer interface.
 * This renderer creates straight-line connectors with
 * an arrow at the head.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class BasicEdgeRenderer implements EdgeRenderer {
    /**
     * Render a visual representation of the given edge.
     */
    @Override
    public Connector render(Object edge, Site tailSite, Site headSite) {
        AbstractConnector c;
        Figure tf = tailSite.getFigure();
        Figure hf = headSite.getFigure();

        //if the edge is a self loop, create an ArcConnector instead!
        if (tf != null && hf != null && tf == hf) {
            c = new ArcConnector(tailSite, headSite);
        } else {
            c = new StraightConnector(tailSite, headSite);
        }

        Arrowhead arrow = new Arrowhead(headSite.getX(), headSite.getY(),
                headSite.getNormal());
        c.setHeadEnd(arrow);
        return c;
    }
}
