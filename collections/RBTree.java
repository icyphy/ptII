/*
  File: RBTree.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  13Oct95  dl                 Changed protection statuses

*/

package collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * RedBlack trees.
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
**/

public class RBTree extends    UpdatableBagImpl
    implements UpdatableBag,
               ElementSortedCollection {

    // instance variables

    /**
     * @serial The root of the tree. Null if empty.
     **/

    protected RBCell tree_;

    /**
     * @serial The comparator to use for ordering.
     **/
    protected Comparator cmp_;

    // constructors

    /**
     * Make an empty tree.
     * Initialize to use DefaultComparator for ordering
     **/
    public RBTree() { this(null, null, null, 0); }

    /**
     * Make an empty tree, using the supplied element screener.
     * Initialize to use DefaultComparator for ordering
     **/

    public RBTree(Predicate s) { this(s, null, null, 0); }

    /**
     * Make an empty tree, using the supplied element comparator for ordering.
     **/
    public RBTree(Comparator c) { this(null, c, null, 0); }

    /**
     * Make an empty tree, using the supplied element screener and comparator
     **/
    public RBTree(Predicate s, Comparator c) { this(s, c, null, 0); }

    /**
     * Special version of constructor needed by clone()
     **/

    protected RBTree(Predicate s, Comparator cmp, RBCell t, int n) {
        super(s);
        count_ = n;
        tree_ = t;
        if (cmp != null) cmp_ = cmp;
        else cmp_ = new DefaultComparator();
    }

    /**
     * Make an independent copy of the tree. Does not clone elements.
     **/
    protected Object clone() throws CloneNotSupportedException {
        if (count_ == 0) return new RBTree(screener_, cmp_);
        else return new RBTree(screener_, cmp_, tree_.copyTree(), count_);
    }



    // Collection methods

    /**
     * Implements collections.Collection.includes.
     * Time complexity: O(log n).
     * @see collections.Collection#includes
     **/
    public synchronized boolean includes(Object element) {
        if (element == null || count_ == 0) return false;
        return tree_.find(element, cmp_) != null;
    }

    /**
     * Implements collections.Collection.occurrencesOf.
     * Time complexity: O(log n).
     * @see collections.Collection#occurrencesOf
     **/
    public synchronized int occurrencesOf(Object element) {
        if (element == null || count_ == 0) return 0;
        return tree_.count(element, cmp_);
    }

    /**
     * Implements collections.Collection.elements.
     * Time complexity: O(1).
     * @see collections.Collection#elements
     **/
    public synchronized CollectionEnumeration elements() {
        return new RBCellEnumeration(this, tree_);
    }

    // ElementSortedCollection methods


    /**
     * Implements collections.ElementSortedCollection.elementComparator
     * Time complexity: O(1).
     * @see collections.ElementSortedCollection#elementComparator
     **/
    public synchronized Comparator elementComparator() { return cmp_; }

    /**
     * Reset the comparator. Will cause a reorganization of the tree.
     * Time complexity: O(n log n).
     **/
    public synchronized void elementComparator(Comparator cmp) {
        if (cmp != cmp_) {
            if (cmp != null) cmp = cmp;
            else cmp_ = new DefaultComparator();
            if (count_ != 0) {       // must rebuild tree!
                incVersion();
                RBCell t = tree_.leftmost();
                tree_ = null;
                count_ = 0;
                while (t != null) {
                    add_(t.element(), false);
                    t = t.successor();
                }
            }
        }
    }


    // UpdatableCollection methods

    /**
     * Implements collections.UpdatableCollection.clear.
     * Time complexity: O(1).
     * @see collections.UpdatableCollection#clear
     **/
    public synchronized void clear() {
        setCount(0);
        tree_ = null;
    }

    /**
     * Implements collections.UpdatableCollection.exclude.
     * Time complexity: O(log n * occurrencesOf(element)).
     * @see collections.UpdatableCollection#exclude
     **/
    public synchronized void exclude(Object element) {
        remove_(element, true);
    }


    /**
     * Implements collections.UpdatableCollection.removeOneOf.
     * Time complexity: O(log n).
     * @see collections.UpdatableCollection#removeOneOf
     **/
    public synchronized void removeOneOf(Object element) {
        remove_(element, false);
    }

    /**
     * Implements collections.UpdatableCollection.replaceOneOf
     * Time complexity: O(log n).
     * @see collections.UpdatableCollection#replaceOneOf
     **/
    public synchronized void replaceOneOf(Object oldElement, Object newElement)
            throws IllegalElementException {
        replace_(oldElement, newElement, false);
    }

    /**
     * Implements collections.UpdatableCollection.replaceAllOf.
     * Time complexity: O(log n * occurrencesOf(oldElement)).
     * @see collections.UpdatableCollection#replaceAllOf
     **/
    public synchronized void replaceAllOf(Object oldElement,
            Object newElement)
            throws IllegalElementException {
        replace_(oldElement, newElement, true);
    }

    /**
     * Implements collections.UpdatableCollection.take.
     * Time complexity: O(log n).
     * Takes the least element.
     * @see collections.UpdatableCollection#take
     **/
    public synchronized Object take()
            throws NoSuchElementException {
        if (count_ != 0) {
            RBCell p = tree_.leftmost();
            Object v = p.element();
            tree_ = p.delete(tree_);
            decCount();
            return v;
        }
        checkIndex(0);
        return null; // not reached
    }


    // UpdatableBag methods

    /**
     * Implements collections.UpdatableBag.addIfAbsent
     * Time complexity: O(log n).
     * @see collections.UpdatableBag#addIfAbsent
     **/
    public synchronized void addIfAbsent(Object element)
            throws IllegalElementException {
        add_(element, true);
    }


    /**
     * Implements collections.UpdatableBag.add.
     * Time complexity: O(log n).
     * @see collections.UpdatableBag#add
     **/
    public synchronized void add(Object element)
            throws IllegalElementException {
        add_(element, false);
    }


    // helper methods

    private void add_(Object element, boolean checkOccurrence)
            throws IllegalElementException {
        checkElement(element);
        if (tree_ == null) {
            tree_ = new RBCell(element);
            incCount();
        }
        else {
            RBCell t = tree_;
            for (;;) {
                int diff = cmp_.compare(element, t.element());
                if (diff == 0 && checkOccurrence) return;
                else if (diff <= 0) {
                    if (t.left() != null)
                        t = t.left();
                    else {
                        tree_ = t.insertLeft(new RBCell(element), tree_);
                        incCount();
                        return;
                    }
                }
                else {
                    if (t.right() != null)
                        t = t.right();
                    else {
                        tree_ = t.insertRight(new RBCell(element), tree_);
                        incCount();
                        return;
                    }
                }
            }
        }
    }


    private void remove_(Object element, boolean allOccurrences) {
        if (element == null) return;
        while (count_ > 0) {
            RBCell p = tree_.find(element, cmp_);
            if (p != null) {
                tree_ = p.delete(tree_);
                decCount();
                if (!allOccurrences) return;
            }
            else break;
        }
    }

    private void replace_(Object oldElement, Object newElement, boolean allOccurrences)
            throws IllegalElementException {
        if (oldElement == null || count_ == 0 || oldElement.equals(newElement))
            return;
        while (includes(oldElement)) {
            removeOneOf(oldElement);
            add(newElement);
            if (!allOccurrences) return;
        }
    }

    // ImplementationCheckable methods

    /**
     * Implements collections.ImplementationCheckable.checkImplementation.
     * @see collections.ImplementationCheckable#checkImplementation
     **/
    public void checkImplementation()
            throws ImplementationError {

        super.checkImplementation();
        assert(cmp_ != null);
        assert(((count_ == 0) == (tree_ == null)));
        assert((tree_ == null || tree_.size() == count_));
        if (tree_ != null) {
            tree_.checkImplementation();
            Object last = null;
            RBCell t = tree_.leftmost();
            while (t != null) {
                Object v = t.element();
                if (last != null)
                    assert(cmp_.compare(last, v) <= 0);
                last = v;
                t = t.successor();
            }
        }
    }

}

