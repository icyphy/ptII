/*
  File: Collection.java

  Originally written by Doug Lea and released into the public domain.
  Thanks for the assistance and support of Sun Microsystems Labs, Agorics
  Inc, Loral, and everyone contributing, testing, and using this code.

  History:
  Date     Who                What
  24Sep95  dl@cs.oswego.edu   Create from collections.java  working file
  14dec95  dl                 Declare as a subinterface of Cloneable

*/

package collections;

import java.util.Enumeration;

/**
 * Collection is the base interface for most classes in this package.
 *
 * @author Doug Lea
 * @version 0.93
 *
 * <P> For an introduction to this package see <A HREF="index.html"> Overview </A>.
 *
**/
public interface Collection extends ImplementationCheckable, Cloneable {

    /**
     * public version of java.lang.Object.clone
     * All Collections implement clone. But this is a protected method.
     * Duplicate allows public access.
     * @see java.lang.Object#clone
     **/

    public Collection duplicate();

    /**
     * Report the number of elements in the collection.
     * No other spurious effects.
     * @return number of elements
     **/
    public int         size();

    /**
     * Report whether this collection has no elements.
     * Behaviorally equivalent to <CODE>size() == 0</CODE>.
     * @return true iff size() == 0
     **/

    public boolean     isEmpty();


    /**
     * Report whether the collection COULD contain element,
     * i.e., that it is valid with respect to the Collection's
     * element screener if it has one.
     * Always returns false if element == null.
     * A constant function: if canInclude(v) is ever true it is always true.
     * (This property is not in any way enforced however.)
     * No other spurious effects.
     * @return true if non-null and passes element screener check
     **/
    public boolean     canInclude(Object element);


    /**
     * Report the number of occurrences of element in collection.
     * Always returns 0 if element == null.
     * Otherwise Object.equals is used to test for equality.
     * @param element the element to look for
     * @return the number of occurrences (always nonnegative)
     **/
    public int         occurrencesOf(Object element);

    /**
     * Report whether the collection contains element.
     * Behaviorally equivalent to <CODE>occurrencesOf(element) &gt;= 0</CODE>.
     * @param element the element to look for
     * @return true iff contains at least one member that is equal to element.
     **/
    public boolean     includes(Object element);

    /**
     * Return an enumeration that may be used to traverse through
     * the elements in the collection. Standard usage, for some
     * collection c, and some operation `use(Object obj)':
     * <PRE>
     * for (Enumeration e = c.elements(); e.hasMoreElements(); )
     *   use(e.nextElement());
     * </PRE>
     * (The values of nextElement very often need to
     * be coerced to types that you know they are.)
     * <P>
     * All Collections return instances
     * of CollectionEnumeration, that can report the number of remaining
     * elements, and also perform consistency checks so that
     * for UpdatableCollections, element enumerations may become
     * invalidated if the collection is modified during such a traversal
     * (which could in turn cause random effects on the collection.
     * TO prevent this,  CollectionEnumerations
     * raise CorruptedEnumerationException on attempts to access
     * nextElements of altered Collections.)
     * Note: Since all collection implementations are synchronizable,
     * you may be able to guarantee that element traversals will not be
     * corrupted by using the java <CODE>synchronized</CODE> construct
     * around code blocks that do traversals. (Use with care though,
     * since such constructs can cause deadlock.)
     * <P>
     * Guarantees about the nature of the elements returned by  nextElement of the
     * returned Enumeration may vary across sub-interfaces.
     * In all cases, the enumerations provided by elements() are guaranteed to
     * step through (via nextElement) ALL elements in the collection.
     * Unless guaranteed otherwise (for example in Seq), elements() enumerations
     * need not have any particular nextElement() ordering so long as they
     * allow traversal of all of the elements. So, for example, two successive
     * calls to element() may produce enumerations with the same
     * elements but different nextElement() orderings.
     * Again, sub-interfaces may provide stronger guarantees. In
     * particular, Seqs produce enumerations with nextElements in
     * index order, ElementSortedCollections enumerations are in ascending
     * sorted order, and KeySortedCollections are in ascending order of keys.
     * @return an enumeration e such that
     * <PRE>
     *   e.numberOfRemainingElements() == size() &&
     *   foreach (v in e) includes(e)
     * </PRE>
     **/

    public CollectionEnumeration elements();

    /**
     * Report whether other has the same element structure as this.
     * That is, whether other is of the same size, and has the same
     * elements() properties.
     * This is a useful version of equality testing. But is not named
     * `equals' in part because it may not be the version you need.
     * <P>
     * The easiest way to describe this operation is just to
     * explain how it is interpreted in standard sub-interfaces:
     * <UL>
     *  <LI> Seq and ElementSortedCollection: other.elements() has the
     *        same order as this.elements().
     *  <LI> Bag: other.elements has the same occurrencesOf each element as this.
     *  <LI> Set: other.elements includes all elements of this
     *  <LI> Map: other includes all (key, element) pairs of this.
     *  <LI> KeySortedCollection: other includes all (key, element)
     *       pairs as this, and with keys enumerated in the same order as
     *       this.keys().
     *</UL>
     * @param other, a Collection
     * @return true if considered to have the same size and elements.
     **/

    public boolean sameStructure(Collection other);

    /**
     * Construct a new Collection that is a clone of self except
     * that it does not include any occurrences of the indicated element.
     * It is NOT an error to exclude a non-existent element.
     *
     * @param element the element to exclude from the new collection
     * @return a new Collection, c, with the sameStructure as this
     * except that !c.includes(element).
     **/
    public Collection  excluding(Object element);


    /**
     * Construct a new Collection that is a clone of self except
     * that it does not include an occurrence of the indicated element.
     * It is NOT an error to remove a non-existent element.
     *
     * @param element the element to exclude from the new collection
     * @return a new Collection, c, with the sameStructure as this
     * except that c.occurrencesOf(element) == max(0,occurrencesOf(element)-1)
     **/
    public Collection  removingOneOf(Object element);

    /**
     * Construct a new Collection that is a clone of self except
     * that one occurrence of oldElement is replaced with
     * newElement.
     * It is NOT an error to replace a non-existent element.
     *
     * @param oldElement the element to replace
     * @param newElement the replacement
     * @return a new Collection, c, with the sameStructure as this, except:
     * <PRE>
     * let int delta = oldElement.equals(newElement)? 0 :
     *               max(1, this.occurrencesOf(oldElement) in
     *  c.occurrencesOf(oldElement) == this.occurrencesOf(oldElement) - delta &&
     *  c.occurrencesOf(newElement) ==  (this instanceof Set) ?
     *         max(1, this.occurrencesOf(oldElement) + delta):
     *                this.occurrencesOf(oldElement) + delta) &&
     * </PRE>
     * @exception IllegalElementException if includes(oldElement) and !canInclude(newElement)
     **/
    public Collection  replacingOneOf(Object oldElement, Object newElement)
            throws IllegalElementException;

    /**
     * Construct a new Collection that is a clone of self except
     * that all occurrences of oldElement are replaced with
     * newElement.
     * It is NOT an error to convert a non-existent element.
     *
     * @param oldElement the element to replace
     * @param newElement the replacement
     * @return a new Collection, c, with the sameStructure as this except
     * <PRE>
     * let int delta = oldElement.equals(newElement)? 0 :
     occurrencesOf(oldElement) in
     *  c.occurrencesOf(oldElement) == this.occurrencesOf(oldElement) - delta &&
     *  c.occurrencesOf(newElement) ==  (this instanceof Set) ?
     *         max(1, this.occurrencesOf(oldElement) + delta):
     *                this.occurrencesOf(oldElement) + delta)
     * </PRE>
     * @exception IllegalElementException if includes(oldElement) and !canInclude(newElement)
     **/

    public Collection  replacingAllOf(Object oldElement, Object newElement)
            throws IllegalElementException;


}
