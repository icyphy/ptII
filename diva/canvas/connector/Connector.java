/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;

/** A Connector is a figure that draws itself between two
 * sites on other figures. To do so, it requires two references
 * to sites on other figures.
 *
 * @version $Revision$
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 */
public interface Connector extends Figure {

    // FIXME LabelFigure should be just Figure.  (or, all figures should
    // support auto-anchoring.
    // FIXME LabelFigure stuff is commented out because (apparently)
    // SketchConnector isn't smart enough to understand labels.
    /** Get the site that marks the "head" of the connector.
     */
    public Site getHeadSite ();

    /** Get the labeling figure of this connector. The returned value
     * may be null.
     */
    //   public LabelFigure getLabelFigure ();

    /** Get the site that marks the "tail" of the connector.
     */
    public Site getTailSite ();

    /** Inform the connector that the head site has moved.
     * The connector is expected to reroute itself using
     * an efficient means as is available.  Repaint the figure.
     */
    public void headMoved ();

    /** Tell the connector to re-route itself. In general,
     * implementations of this method should be more efficient
     * than route().   Repaint the figure.
     */
    public void reroute ();

    /** Tell the connector to route itself completely,
     * using all available information.  Repaint the figure.
     */
    public void route ();

    /** Set the site that marks the "head" of the connector.
     */
    public void setHeadSite (Site s);

    /** Set the site that marks the "tail" of the connector.
     */
    public void setTailSite (Site s);

    /** Set the labeling figure of this connector. All connectors are
     * assumed to be able to display at least one label
     * in a useful way.
     */
    //public void setLabelFigure (LabelFigure f);

    /** Inform the connector that the tail site has moved.
     * The connector is expected to reroute itself using
     * an efficient means as is available.  Repaint the figure.
     */
    public void tailMoved ();
}


