/* A model for an attribute as a diva graph node.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.vergil.kernel;

import java.util.Iterator;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.NamedObjNodeModel;
import diva.util.NullIterator;

//////////////////////////////////////////////////////////////////////////
//// AttributeNodeModel

/**
 A model for an attribute as a diva graph node.
 This is used for visible attributes.

 @author  Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (yourname)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class AttributeNodeModel extends NamedObjNodeModel {
    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @param node The node to be deleted.
     *  @return A valid MoML string.
     */
    @Override
    public String getDeleteNodeMoML(Object node) {
        NamedObj attribute = ((Locatable) node).getContainer();
        return "<deleteProperty name=\"" + attribute.getName() + "\"/>\n";
    }

    /** Return the graph parent of the given node.
     *  @param node The node, which is assumed to be an instance of Locatable.
     *  @return The container of the location's container, which should be
     *   the root of the graph.
     */
    @Override
    public Object getParent(Object node) {
        return ((Locatable) node).getContainer().getContainer();
    }

    /** Return an iterator over the edges coming into the given node.
     *  @param node The node.
     *  @return A NullIterator, since no edges are attached to attributes.
     */
    @Override
    public Iterator inEdges(Object node) {
        return new NullIterator();
    }

    /** Return an iterator over the edges coming out of the given node.
     *  @param node The node.
     *  @return A NullIterator, since no edges are attached to attributes.
     */
    @Override
    public Iterator outEdges(Object node) {
        return new NullIterator();
    }

    /** Remove the given node from the model.  The node is assumed
     *  to be an instance of Locatable belonging to an attribute.
     *  The removal is accomplished by queueing a change request
     *  with the container.
     *  @param eventSource The source of the remove event (ignored).
     *  @param node The node.
     */
    @Override
    public void removeNode(final Object eventSource, final Object node) {
        NamedObj attribute = ((Locatable) node).getContainer();
        NamedObj container = attribute.getContainer();
        ;

        String moml = getDeleteNodeMoML(node);

        // Note: The source is NOT the graph model.
        ChangeRequest request = new MoMLChangeRequest(this, container, moml);
        container.requestChange(request);
    }
}
