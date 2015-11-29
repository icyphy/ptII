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

import java.util.HashMap;

import diva.canvas.Figure;
import diva.canvas.Site;

/** An implementation of connector targets that finds center sites.
 *
 * @version $Id$
 * @author John Reekie
 * @author Michael Shilman
 */
public class CenterTarget extends AbstractConnectorTarget {
    /** The mapping from figures to sites. Ignore the
     * problem that none of the contents will ever get
     * garbage-collected.
     */
    private HashMap _siteMap = new HashMap();

    /** Return a center site located on the figure, if the figure is not a
     * connector.
     */
    @Override
    public Site getHeadSite(Figure f, double x, double y) {
        if (!(f instanceof Connector)) {
            if (_siteMap.containsKey(f)) {
                return (Site) _siteMap.get(f);
            } else {
                Site s = new CenterSite(f);
                _siteMap.put(f, s);
                return s;
            }
        }

        return null;
    }
}
