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

/** An abstract implementation of the ConnectorTarget interface.  Most
 * connector targets do only basic filtering on a connector, by disallowing
 * the head and tail of a connector from attaching to the same figure.  Most
 * connector targets also have the same behavior for heads and for tails.
 * This class allows connector targets to only implement a single method to
 * use this functionality.
 *
 * @version $Id$
 * @author John Reekie
 */
public abstract class AbstractConnectorTarget implements ConnectorTarget {
    /** Return true if the given connector can be connected to the given
     * figure.  In this base class return true if the tail of the connector
     * is not attached to the same figure.
     */
    public boolean acceptHead(Connector c, Figure f) {
        if (c != null && c.getTailSite().getFigure() == f) {
            return false;
        }

        return true;
    }

    /** Return true if the given connector can be connected to the given
     * figure.  In this base class return true if the head of the connector
     * is not attached to the same figure.
     */
    public boolean acceptTail(Connector c, Figure f) {
        if (c != null && c.getHeadSite().getFigure() == f) {
            return false;
        }

        return true;
    }

    /** Return a suitable site to connect a connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to disallow the
     * connection.  This method is primarily useful for manually
     * creating new figures.
     */
    @Override
    public abstract Site getHeadSite(Figure f, double x, double y);

    /** Return a suitable site to connect a connector's tail to, based
     * on this figure and location. Return null if there is no
     * suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to
     * disallow the connection.  This method is primarily useful for
     * manually creating new figures.  In this base class, assume that
     * tails are treated the same way as heads, so call the
     * getHeadSite method with the same arguments.
     * @deprecated Use getTailSite that takes a connector.
     */
    @Deprecated
    @Override
    public Site getTailSite(Figure f, double x, double y) {
        return getHeadSite(f, x, y);
    }

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.  In this base class, just
     * return the same site.
     * @deprecated Use getHeadSite that takes a connector.
     */
    @Deprecated
    @Override
    public Site getHeadSite(Site s, double x, double y) {
        return s;
    }

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().  In this base class, just
     * return the same site.
     * @deprecated Use getTailSite that takes a connector.
     */
    @Deprecated
    @Override
    public Site getTailSite(Site s, double x, double y) {
        return s;
    }

    /** Return a suitable site to connect the given connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site. In this base class, return null if the acceptHead
     * method called with the connector and the figure returns false.
     * Otherwise call the method that just takes a figure.
     */
    @Override
    public Site getHeadSite(Connector c, Figure f, double x, double y) {
        if (acceptHead(c, f) == false) {
            return null;
        }

        return getHeadSite(f, x, y);
    }

    /** Return a suitable site to connect the given connector's tail to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In this base class, return null if the acceptHead
     * method called with the connector and the figure returns false.
     * Otherwise call the method that just takes a figure.
     */
    @Override
    public Site getTailSite(Connector c, Figure f, double x, double y) {
        if (acceptTail(c, f) == false) {
            return null;
        }

        return getTailSite(f, x, y);
    }

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.
     * In this base class, just return the site.
     */
    @Override
    public Site getHeadSite(Connector c, Site s, double x, double y) {
        return s;
    }

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().
     * In this base class, just return the site.
     */
    @Override
    public Site getTailSite(Connector c, Site s, double x, double y) {
        return s;
    }
}
