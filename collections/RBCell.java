/*
  File: RBCell.java

  Originally written by Doug Lea and released into the public domain. 
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics 
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file

*/
  
package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * RBCell implements basic capabilities of Red-Black trees,
 * an efficient kind of balanced binary tree. The particular
 * algorithms used are adaptations of those in Corman,
 * Lieserson, and Rivest's <EM>Introduction to Algorithms</EM>.
 * This class was inspired by (and code cross-checked with) a 
 * similar class by Chuck McManis. The implementations of
 * rebalancings during insertion and deletion are
 * a little trickier than those versions since they
 * don't swap cell contents or use a special dummy nilnodes. 
 * <P>
 * It is a pure implementation class. For harnesses, see:
 * @see RBTree
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/




public class RBCell extends Cell implements ImplementationCheckable  {
  static final boolean RED   = false;
  static final boolean BLACK = true;

/**
 * The node color (RED, BLACK)
**/

  protected boolean color_;

/**
 * Pointer to left child
**/

  protected RBCell  left_;

/**
 * Pointer to right child
**/

  protected RBCell  right_;

/**
 * Pointer to parent (null if root)
**/

  private RBCell  parent_;

/**
 * Make a new cell with given element, null links, and BLACK color.
 * Normally only called to establish a new root.
**/

  public RBCell(Object element) { 
    super(element);
    left_ = null; right_ = null; parent_ = null; color_ = BLACK;
  }

/**
 * Return a new RBCell with same element and color as self,
 * but with null links. (Since it is never OK to have
 * multiple identical links in a RB tree.)
**/
  protected Object clone() throws CloneNotSupportedException { 
    RBCell t = new RBCell(element());
    t.color_ = color_;
    return t;
  }

  
/** 
 * Return left child (or null)
**/

  public final RBCell left()   { return left_; }

/** 
 * Return right child (or null)
**/

  public final RBCell right()  { return right_; }

/** 
 * Return parent (or null)
**/
  public final RBCell parent() { return parent_; }


/**
 * @see collections.ImplementationCheckable.checkImplementation.
**/
  public void checkImplementation() 
  throws ImplementationError {

   // It's too hard to check the property that every simple
   // path from node to leaf has same number of black nodes.
   // So restrict to the following

    assert(parent_ == null || 
      this == parent_.left_ || 
      this == parent_.right_);

    assert(left_ == null ||
      this == left_.parent_);

    assert(right_ == null ||
      this == right_.parent_);

    assert(color_ == BLACK || 
      (colorOf(left_) == BLACK) && (colorOf(right_) == BLACK));

    if (left_ != null) left_.checkImplementation();
    if (right_ != null) right_.checkImplementation();
  }
      
/**
 * Implements collections.ImplementationCheckable.assert.
 * @see collections.ImplementationCheckable#assert
**/
  public void assert(boolean pred) 
  throws ImplementationError {
    ImplementationError.assert(this, pred);
  }


/**
 * Return the minimum element of the current (sub)tree
**/
  
  public final RBCell leftmost() {
    RBCell p = this;
    for ( ;  p.left_ != null; p = p.left_) {}
    return p;
  }

/**
 * Return the maximum element of the current (sub)tree
**/
  public final RBCell rightmost() {
    RBCell p = this;
    for ( ; p.right_ != null; p = p.right_) {}
    return p;
  }

/**
 * Return the root (parentless node) of the tree
**/
  public final RBCell root() {
    RBCell p = this;
    for ( ; p.parent_ != null; p = p.parent_) {}
    return p;
  }

/**
 * Return true if node is a root (i.e., has a null parent)
**/

  public final boolean isRoot() { return parent_ == null; }


/**
 * Return the inorder successor, or null if no such
**/

  public final RBCell successor() {
    if (right_ != null)
      return right_.leftmost();
    else {
      RBCell p = parent_;
      RBCell ch = this;
      while (p != null && ch == p.right_) { ch = p; p = p.parent_; }
      return p;
    }
  }

/**
 * Return the inorder predecessor, or null if no such
**/

  public final RBCell predecessor() {
    if (left_ != null)
      return left_.rightmost();
    else {
      RBCell p = parent_;
      RBCell ch = this;
      while (p != null && ch == p.left_) { ch = p; p = p.parent_; }
      return p;
    }
  }

/**
 * Return the number of nodes in the subtree
**/
  public final int size() {
    int c = 1;
    if (left_ != null) c += left_.size();
    if (right_ != null) c += right_.size();
    return c;
  }


/**
 * Return node of current subtree containing element as element(), 
 * if it exists, else null. 
 * Uses Comparator cmp to find and to check equality.
**/

  public RBCell find(Object element, Comparator cmp) {
    RBCell t = this;
    for (;;) {
      int diff = cmp.compare(element, t.element());
      if (diff == 0) return t;
      else if (diff < 0) t = t.left_;
      else t = t.right_;
      if (t == null) return null;
    }
  }


/**
 * Return number of nodes of current subtree containing element.
 * Uses Comparator cmp to find and to check equality.
**/
  public int count(Object element, Comparator cmp) {
    int c = 0;
    RBCell t = this;
    while (t != null) {
      int diff = cmp.compare(element, t.element());
      if (diff == 0) {
        ++c;
        if (t.left_ == null)
          t = t.right_;
        else if (t.right_ == null)
          t = t.left_;
        else {
          c += t.right_.count(element, cmp);
          t = t.left_;
        }
      }
      else if (diff < 0) t = t.left_;
      else  t = t.right_;
    }
    return c;
  }




/**
 * Return a new subtree containing each element of current subtree
**/

  public RBCell copyTree() {
    RBCell t = null;
    try {
      t = (RBCell)(clone());
    } catch (CloneNotSupportedException ex) {}

    if (left_ != null) {
      t.left_ = left_.copyTree();
      t.left_.parent_ = t;
    }
    if (right_ != null) {
      t.right_ = right_.copyTree();
      t.right_.parent_ = t;
    }
    return t;
  }


/**
 * There's no generic element insertion. Instead find the
 * place you want to add a node and then invoke insertLeft
 * or insertRight.
 * <P>
 * Insert cell as the left child of current node, and then
 * rebalance the tree it is in.
 * @param cell the cell to add
 * @param root, the root of the current tree
 * @return the new root of the current tree. (Rebalancing
 * can change the root!)
**/


  public RBCell insertLeft(RBCell cell, RBCell root) {
    left_ = cell;
    cell.parent_ = this;
    return cell.fixAfterInsertion(root);
  }

/**
 * Insert cell as the right child of current node, and then
 * rebalance the tree it is in.
 * @param cell the cell to add
 * @param root, the root of the current tree
 * @return the new root of the current tree. (Rebalancing
 * can change the root!)
**/

  public RBCell insertRight(RBCell cell, RBCell root) {
    right_ = cell;
    cell.parent_ = this;
    return cell.fixAfterInsertion(root);
  }

    
/**
 * Delete the current node, and then rebalance the tree it is in
 * @param root the root of the current tree
 * @return the new root of the current tree. (Rebalancing
 * can change the root!)
**/


  public RBCell delete(RBCell root) {

    // handle case where we are only node
    if (left_ == null && right_ == null && parent_ == null) return null;

    // if strictly internal, swap places with a successor
    if (left_ != null && right_ != null) {
      RBCell s = successor();
      // To work nicely with arbitrary subclasses of RBCell, we don't want to
      // just copy successor's fields. since we don't know what
      // they are.  Instead we swap positions in the tree.
      root = swapPosition(this, s, root);
    }

    // Start fixup at replacement node (normally a child).
    // But if no children, fake it by using self

    if (left_ == null && right_ == null) {
      
      if (color_ == BLACK) 
        root = this.fixAfterDeletion(root);

      // Unlink  (Couldn't before since fixAfterDeletion needs parent ptr)

      if (parent_ != null) {
        if (this == parent_.left_) 
          parent_.left_ = null;
        else if (this == parent_.right_) 
          parent_.right_ = null;
        parent_ = null;
      }

    }
    else {
      RBCell replacement = left_;
      if  (replacement == null) replacement = right_;
       
      // link replacement to parent 
      replacement.parent_ = parent_;

      if (parent_ == null)             root = replacement; 
      else if (this == parent_.left_)  parent_.left_  = replacement;
      else                             parent_.right_ = replacement;

      left_ = null;
      right_ = null;
      parent_ = null;

      // fix replacement
      if (color_ == BLACK) 
        root = replacement.fixAfterDeletion(root);
      
    }

    return root;
  }

/**
 * Swap the linkages of two nodes in a tree.
 * Return new root, in case it changed.
**/

  static RBCell swapPosition(RBCell x, RBCell y, RBCell root) {

   /* Too messy. TODO: find sequence of assigments that are always OK */

    RBCell px = x.parent_; 
    boolean xpl = px != null && x == px.left_;
    RBCell lx = x.left_;
    RBCell rx = x.right_;

    RBCell py = y.parent_;
    boolean ypl = py != null && y == py.left_;
    RBCell ly = y.left_;
    RBCell ry = y.right_;

    if (x == py) {
      y.parent_ = px;
      if (px != null) if (xpl) px.left_ = y; else px.right_ = y;
      x.parent_ = y;
      if (ypl) { 
        y.left_ = x; 
        y.right_ = rx; if (rx != null) rx.parent_ = y;
      }
      else {
        y.right_ = x;
        y.left_ = lx;   if (lx != null) lx.parent_ = y;
      }
      x.left_ = ly;   if (ly != null) ly.parent_ = x;
      x.right_ = ry;  if (ry != null) ry.parent_ = x;
    }
    else if (y == px) {
      x.parent_ = py;
      if (py != null) if (ypl) py.left_ = x; else py.right_ = x;
      y.parent_ = x;
      if (xpl) { 
        x.left_ = y; 
        x.right_ = ry; if (ry != null) ry.parent_ = x;
      }
      else {
        x.right_ = y;
        x.left_ = ly;   if (ly != null) ly.parent_ = x;
      }
      y.left_ = lx;   if (lx != null) lx.parent_ = y;
      y.right_ = rx;  if (rx != null) rx.parent_ = y;
    }
    else {
      x.parent_ = py; if (py != null) if (ypl) py.left_ = x; else py.right_ = x;
      x.left_ = ly;   if (ly != null) ly.parent_ = x;
      x.right_ = ry;  if (ry != null) ry.parent_ = x;
      
      y.parent_ = px; if (px != null) if (xpl) px.left_ = y; else px.right_ = y;
      y.left_ = lx;   if (lx != null) lx.parent_ = y;
      y.right_ = rx;  if (rx != null) rx.parent_ = y;
    }

    boolean c = x.color_; x.color_ = y.color_; y.color_ = c;

    if (root == x) root = y;
    else if (root == y) root = x;
    return root;
  }



/**
 * Return color of node p, or BLACK if p is null
 * (In the CLR version, they use
 * a special dummy `nil' node for such purposes, but that doesn't
 * work well here, since it could lead to creating one such special
 * node per real node.)
 *
**/

  static boolean colorOf(RBCell p) { return (p == null)? BLACK : p.color_; }

/**
 * return parent of node p, or null if p is null
**/
  static RBCell  parentOf(RBCell p) { return (p == null)? null: p.parent_; }

/**
 * Set the color of node p, or do nothing if p is null
**/

  static void    setColor(RBCell p, boolean c) { if (p != null)  p.color_ = c; }

/**
 * return left child of node p, or null if p is null
**/

  static RBCell  leftOf(RBCell p) { return (p == null)? null: p.left_; }

/**
 * return right child of node p, or null if p is null
**/

  static RBCell  rightOf(RBCell p) { return (p == null)? null: p.right_; }

      
  /** From CLR **/
  protected final RBCell rotateLeft(RBCell root) {
    RBCell r = right_;
    right_ = r.left_;
    if (r.left_ != null) r.left_.parent_ = this;
    r.parent_ = parent_;
    if (parent_ == null) root = r;
    else if (parent_.left_ == this) parent_.left_ = r;
    else parent_.right_ = r;
    r.left_ = this;
    parent_ = r;
    return root;
  }

  /** From CLR **/
  protected final RBCell rotateRight(RBCell root) {
    RBCell l = left_;
    left_ = l.right_;
    if (l.right_ != null) l.right_.parent_ = this;
    l.parent_ = parent_;
    if (parent_ == null) root = l;
    else if (parent_.right_ == this) parent_.right_ = l;
    else parent_.left_ = l;
    l.right_ = this;
    parent_ = l;
    return root;
  }


  /** From CLR **/
  protected final RBCell fixAfterInsertion(RBCell root) {
    color_ = RED;
    RBCell x = this;
    
    while (x != null && x != root && x.parent_.color_ == RED) {
      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
        RBCell y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else {
          if (x == rightOf(parentOf(x))) {
            x = parentOf(x);
            root = x.rotateLeft(root);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) 
            root = parentOf(parentOf(x)).rotateRight(root);
        }
      }
      else {
        RBCell y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        }
        else {
          if (x == leftOf(parentOf(x))) {
            x = parentOf(x);
            root = x.rotateRight(root);
          }
          setColor(parentOf(x),  BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) 
            root = parentOf(parentOf(x)).rotateLeft(root);
        }
      }
    }
    root.color_ = BLACK;
    return root;
  }
  


  /** From CLR **/
  protected final RBCell fixAfterDeletion(RBCell root) {
    RBCell x = this;
    while (x != root && colorOf(x) == BLACK) {
     if (x == leftOf(parentOf(x))) {
       RBCell sib = rightOf(parentOf(x));
       if (colorOf(sib) == RED) {
         setColor(sib, BLACK);
         setColor(parentOf(x), RED);
         root = parentOf(x).rotateLeft(root);
         sib = rightOf(parentOf(x));
       }
       if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
         setColor(sib,  RED);
         x = parentOf(x);
       }
       else {
         if (colorOf(rightOf(sib)) == BLACK) {
           setColor(leftOf(sib), BLACK);
           setColor(sib, RED);
           root = sib.rotateRight(root);
           sib = rightOf(parentOf(x));
         }
         setColor(sib, colorOf(parentOf(x)));
         setColor(parentOf(x), BLACK);
         setColor(rightOf(sib), BLACK);
         root = parentOf(x).rotateLeft(root);
         x = root;
       }
     }
     else {
       RBCell sib = leftOf(parentOf(x));
       if (colorOf(sib) == RED) {
         setColor(sib, BLACK);
         setColor(parentOf(x), RED);
         root = parentOf(x).rotateRight(root);
         sib = leftOf(parentOf(x));
       }
       if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
         setColor(sib,  RED);
         x = parentOf(x);
       }
       else {
         if (colorOf(leftOf(sib)) == BLACK) {
           setColor(rightOf(sib), BLACK);
           setColor(sib, RED);
           root = sib.rotateLeft(root);
           sib = leftOf(parentOf(x));
         }
         setColor(sib, colorOf(parentOf(x)));
         setColor(parentOf(x), BLACK);
         setColor(leftOf(sib), BLACK);
         root = parentOf(x).rotateRight(root);
         x = root;
       }
     }
   }
    setColor(x, BLACK);
    return root;
  }
           
}
