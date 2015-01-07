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
 *
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;

/** An object that is used to get suitable "target" sites
 * for connectors. This class provides a way for connections
 * to be connected to figures without knowing too many specifics
 * about the figure being connected. Instances of this object
 * are often given to controller objects (as in model-view-controller)
 * so they can set up view construction and interaction. In this
 * same package, an instance is required by ConnectorManipulator.
 *
 * @version $Id$
 * @author John Reekie
 */
public interface ConnectorTarget {
    /** Return a suitable site to connect a connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to disallow the
     * connection.  This method is primarily useful for manually
     * creating new figures.
     */
    public Site getHeadSite(Figure f, double x, double y);

    /** Return a suitable site to connect a connector's tail to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to disallow the
     * connection.  This method is primarily useful for manually
     * creating new figures.
     */
    public Site getTailSite(Figure f, double x, double y);

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.
     * @deprecated Use getHeadSite that takes a connector.
     */
    @Deprecated
    public Site getHeadSite(Site s, double x, double y);

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().
     * @deprecated Use getTailSite that takes a connector.
     */
    @Deprecated
    public Site getTailSite(Site s, double x, double y);

    /** Return a suitable site to connect the given connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site.
     */
    public Site getHeadSite(Connector c, Figure f, double x, double y);

    /** Return a suitable site to connect the given connector's tail to,
     * based on this figure and location. Return null if there
     * is no suitable site.
     */
    public Site getTailSite(Connector c, Figure f, double x, double y);

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.
     */
    public Site getHeadSite(Connector c, Site s, double x, double y);

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().
     */
    public Site getTailSite(Connector c, Site s, double x, double y);
}
