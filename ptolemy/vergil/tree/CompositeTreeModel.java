/* A tree model for Ptolemy II Composites, for use with JTree.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.vergil.tree;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// CompositeTreeModel

/**
 A tree model for Ptolemy II models.  Nodes in this tree contain
 only CompositeEntities.

 <p>The indexes of the attributes are 0 to a-1, where a is the
 number of attributes.  The indexes of the ports are a to a+p-1,
 where p is the number of ports, and so on.
 Subclasses may return a subset of the attributes, ports, and
 relations by overriding the protected methods that list these
 contained objects.

 @author Christopher Brooks, based on FullTreeModel by Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class CompositeTreeModel extends EntityTreeModel {
    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public CompositeTreeModel(CompositeEntity root) {
        super(root);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  In this base class, a child is a contained entity.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    @Override
    public Object getChild(Object parent, int index) {
        if (index > getChildCount(parent)) {
            return null;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.entityList(CompositeActor.class).get(index);
    }

    /** Return the number of children of the given parent, which in
     *  this base class is the number of contained entities.
     *  @param parent A parent node.
     *  @return The number of contained entities.
     */
    @Override
    public int getChildCount(Object parent) {
        if (!(parent instanceof CompositeEntity)) {
            return 0;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.entityList(CompositeActor.class).size();
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child or is not an instance
     *  of CompositeEntity return -1.
     *  @param parent The parent, which is usually a CompositeEntity.
     *  @param child The child.
     *  @return The index of the specified child.
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (!(parent instanceof CompositeEntity)) {
            return -1;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.entityList(CompositeActor.class).indexOf(child);
    }

    /** Return true if the object is a leaf node.  In this base class,
     *  an object is a leaf node if it is not an instance of CompositeEntity.
     *  @param object The object in question.
     *  @return True if the node has no children.
     */
    @Override
    public boolean isLeaf(Object object) {
        if (!(object instanceof CompositeEntity)) {
            return true;
        }

        // NOTE: The following is probably not a good idea because it
        // will force evaluation of the contents of a Library prematurely.
        if (getChildCount(object) == 0) {
            return true;
        }
        return false;
    }

}
