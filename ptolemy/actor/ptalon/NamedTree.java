package ptolemy.actor.ptalon;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is used for generic trees, where each node
 * has a name. Subclasses will typically should set TreeType to 
 * be the subclass itself.
 * 
 * For instance, IfTree extends NamedTree &lt IfTree &gt
 * 
 * @author acataldo
 *
 */
public abstract class NamedTree<TreeType extends NamedTree> {

    /**
     * Create a new tree with the specifed parent.  This
     * is null if the tree to create is a root.  
     * @param parent The parent for this tree.
     */
    public NamedTree(TreeType parent, String name) {
        _name = name;
        _parent = parent;
        _children = new LinkedList<TreeType>();
    }

    /**
     * Create a new child tree to this tree with the specified
     * name and return it. 
     * @param name The name of the child.
     * @return The child TreeType.
     */
    public abstract TreeType addChild(String name);

    /**
     * @param name The name for the desired child.
     * @return A child with the specified name, if there
     * is any, or null otherwise. 
     */
    public TreeType getChild(String name) {
        for (TreeType child : _children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * @return The children of this tree.
     */
    public List<TreeType> getChildren() {
        return _children;
    }

    /**
     * @return The name associated with this tree.
     */
    public String getName() {
        return _name;
    }

    /**
     * @return The parent of this tree.
     */
    public TreeType getParent() {
        return _parent;
    }

    /**
     * @return The ancestors of this tree, not including
     * this tree.
     */
    public List<TreeType> getProperAncestors() {
        LinkedList<TreeType> list = new LinkedList<TreeType>();
        TreeType next = _parent;
        while (next != null) {
            list.addFirst(next);
            next = (TreeType) next.getParent();
        }
        return list;
    }

    protected TreeType _parent;

    protected List<TreeType> _children;

    protected String _name;

}
