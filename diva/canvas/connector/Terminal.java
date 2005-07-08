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

/** A Terminal is a figure that provides a visible place for connectors
 * to connect to. In general, connectors are able to connect to
 * anything, but in certain types of diagrams, such as circuit
 * schematics, connectors are expected to connect to objects
 * that are clearly identifiable as a "connection point." Terminals
 * serve this purpose.
 *
 * <p> The terminal interface exposes access to two sites: The
 * "attach" site is the site on some other figure that the terminal
 * positions itself on, and the "connect" site is the site
 * that connectors can connect to.
 *
 * @version $Id$
 * @author  John Reekie
 */
public interface Terminal extends Figure {
    /** Get the site that the terminal is attached to.
     */
    public Site getAttachSite();

    /** Get the site that a connector can connect to.
     */
    public Site getConnectSite();

    /** Tell the terminal to relocate itself because the
     * attachment site (or the figure that owns it) has moved.
     */
    public void relocate();

    /** Set the site that the terminal is attached to.
     */
    public void setAttachSite(Site s);
}
