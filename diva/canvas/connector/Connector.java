/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 *
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;

/** A Connector is a figure that draws itself between two
 * sites on other figures. To do so, it requires two references
 * to sites on other figures.
 *
 * @version $Id$
 * @author  Michael Shilman
 * @author  John Reekie
 */
public interface Connector extends Figure {
    // FIXME LabelFigure should be just Figure.  (or, all figures should
    // support auto-anchoring.
    // FIXME LabelFigure stuff is commented out because (apparently)
    // SketchConnector isn't smart enough to understand labels.

    /** Get the site that marks the "head" of the connector.
     */
    public Site getHeadSite();

    /** Get the labeling figure of this connector. The returned value
     * may be null.
     */

    //   public LabelFigure getLabelFigure ();
    /** Get the site that marks the "tail" of the connector.
     */
    public Site getTailSite();

    /** Inform the connector that the head site has moved.
     * The connector is expected to reroute itself using
     * an efficient means as is available.  Repaint the figure.
     */
    public void headMoved();

    /** Tell the connector to re-route itself. In general,
     * implementations of this method should be more efficient
     * than route().   Repaint the figure.
     */
    public void reroute();

    /** Tell the connector to route itself completely,
     * using all available information.  Repaint the figure.
     */
    public void route();

    /** Set the site that marks the "head" of the connector.
     */
    public void setHeadSite(Site s);

    /** Set the site that marks the "tail" of the connector.
     */
    public void setTailSite(Site s);

    /** Set the labeling figure of this connector. All connectors are
     * assumed to be able to display at least one label
     * in a useful way.
     */

    //public void setLabelFigure (LabelFigure f);
    /** Inform the connector that the tail site has moved.
     * The connector is expected to reroute itself using
     * an efficient means as is available.  Repaint the figure.
     */
    public void tailMoved();
}
