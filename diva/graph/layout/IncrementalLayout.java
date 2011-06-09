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
 */
package diva.graph.layout;

/**
 * A layout engine which operates incrementally, based on the arrival
 * of new node information.  An incremental layout is simply a kind
 * of graph listener that updates the layout with every change in
 * the graph.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface IncrementalLayout extends GlobalLayout {
    /** Called in response to the given edge being given a figure.
     */
    public void edgeDrawn(Object edge);

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    public void edgeRouted(Object edge);

    /** Called in response to the given node being given a figure.
     */
    public void nodeDrawn(Object node);

    /** Called in response to the figure representing the
     *  given node being moved.
     */
    public void nodeMoved(Object node);
}
