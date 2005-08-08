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
package diva.graph.modular;

import java.util.Iterator;

/**
 * Models of composite object.
 *
 * @author Michael Shilman (contributor Edward A. Lee)
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface CompositeModel {
    /**
     * Return an iterator over the nodes that this graph contains.
     */
    public Iterator nodes(Object composite);
    
    /**
     * Return an iterator over the nodes that should
     * be rendered prior to the edges. This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodesBeforeEdges(Object composite);

    /**
     * Provide an iterator over the nodes that should
     * be rendered after to the edges. This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodesAfterEdges(Object composite);

    /**
     * Return a count of the nodes this graph contains.
     */
    public int getNodeCount(Object composite);
}
