/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;
import diva.graph.*;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.AbstractConnector;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.Connector;
import diva.canvas.connector.StraightConnector;
import diva.canvas.connector.Arrowhead;

/**
 * A basic implementation of the EdgeRenderer interface.
 * This renderer creates straight-line connectors with
 * an arrow at the head.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class BasicEdgeRenderer implements EdgeRenderer {
    /**
     * Render a visual representation of the given edge.
     */
    public Connector render(Object edge, Site tailSite, Site headSite) {
        AbstractConnector c;
        Figure tf = tailSite.getFigure();
        Figure hf = headSite.getFigure();

        //if the edge is a self loop, create an ArcConnector instead!
        if((tf != null)&&(hf != null)&&(tf == hf)){
            c = new ArcConnector(tailSite, headSite);
        }
        else {
            c = new StraightConnector(tailSite, headSite);
        }
        Arrowhead arrow = new Arrowhead(
                headSite.getX(), headSite.getY(), headSite.getNormal());
        c.setHeadEnd(arrow);
        return c;
    }
}


