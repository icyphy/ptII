/*
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY


 */
package diva.graph;
import diva.canvas.Site;
import diva.canvas.connector.Connector;

/**
 * A factory which creates a visual representation (EdgeFigure)
 * given an edge input.  The factory is not held responsible
 * for routing the edge, but simply for providing the
 * basic visual properties of the edge (color, line width,
 * dashes, etc.).  The client will set up the endpoints
 * of the edge, and then tell the edge to route itself.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public interface EdgeRenderer {
    /**
     * Render a visual representation of the given edge.
     */
    public Connector render(Object edge, Site tailSite, Site headSite);
}


