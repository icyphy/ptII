/* A model for an attribute as a diva graph node.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel;

import diva.util.NullIterator;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.NamedObjNodeModel;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// AttributeNodeModel
/**
A model for an attribute as a diva graph node.
This is used for visible attributes.

@author  Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class AttributeNodeModel extends NamedObjNodeModel {

    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @return A valid MoML string.
     */
    public String getDeleteNodeMoML(Object node) {
        NamedObj attribute = (NamedObj)((Locatable)node).getContainer();

        NamedObj container = _getChangeRequestParent((NamedObj)getParent(node));

        StringBuffer moml = new StringBuffer();
        moml.append("<deleteProperty name=\"" +
                attribute.getName(container) +
                "\"/>\n");
        return moml.toString();
    }

    /** Return the graph parent of the given node.
     *  @param node The node, which is assumed to be an instance of Locatable.
     *  @return The container of the location's container, which should be
     *   the root of the graph.
     */
    public Object getParent(Object node) {
        return ((Locatable)node).getContainer().getContainer();
    }

    /** Return an iterator over the edges coming into the given node.
     *  @param node The node.
     *  @return A NullIterator, since no edges are attached to attributes.
     */
    public Iterator inEdges(Object node) {
        return new NullIterator();
    }

    /** Return an iterator over the edges coming out of the given node.
     *  @param node The node.
     *  @return A NullIterator, since no edges are attached to attributes.
     */
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
    public void removeNode(final Object eventSource, final Object node) {
        NamedObj attribute = (NamedObj)((Locatable)node).getContainer();

        NamedObj container = _getChangeRequestParent(attribute);

        String moml = "<deleteProperty name=\""
            + attribute.getName(container) + "\"/>\n";

        // Note: The source is NOT the graph model.
        ChangeRequest request =
            new MoMLChangeRequest(this, container, moml);
        container.requestChange(request);
    }
}
