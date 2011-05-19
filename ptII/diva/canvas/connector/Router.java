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

import java.awt.Shape;

/** A Router is an object that can be used to help a connector
 * route itself. Specific implementations of Router are used
 * by connectors according to the Shape that they use to draw
 * themselves.
 *
 * @version $Id$
 * @author  Michael Shilman
 * @author  John Reekie
 */
public interface Router {
    /** Reroute the given Shape, given that the head site moved.
     * The router can assume that the tail site has not moved.
     * The shape is modified by the router.
     */
    public void rerouteHead(Connector c, Shape s);

    /** Reroute the given Shape, given that the tail site moved.
     * The router can assume that the head site has not moved.
     * The shape is modified by the router.
     */
    public void rerouteTail(Connector c, Shape s);

    /** Reroute the given shape, given that both the head the tail
     * sites moved. The shape is modified by the router.
     */
    public void reroute(Connector c, Shape s);

    /** Route the given connector, returning a shape of the
     * appropriate type that it can used to draw itself with.
     */
    public Shape route(Connector c);
}
