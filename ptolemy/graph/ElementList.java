/* A list of graph elements.

   Copyright (c) 2001-2003 The University of Maryland
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   @ProposedRating Red (cxh@eecs.berkeley.edu)
   @AcceptedRating Red (cxh@eecs.berkeley.edu)
 */

package ptolemy.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

////////////////////////////////////////////////////////////////////////// //
//ElementList
/**
   A list of graph elements. This class manages the storage and weight
   information associated with a list of unique graph elements.
   This class is normally for use internally within graph classes.

   @author Shuvra S. Bhattacharyya
   @version $Id$
   @since Ptolemy II 2.0
 */
public class ElementList extends LabeledList {

    /** Construct an empty element list.
     *  @param descriptor A one-word description of the type of elements
     *  that are to be stored in this list.
     *  @param graph The graph associated with this element list.
     */
    public ElementList(String descriptor, Graph graph) {
        super();
        _descriptor = descriptor;
        _graph = graph;
        _weightMap = new HashMap();
        _unweightedSet = new HashSet();
    }

    /** Construct an empty element list with enough storage allocated for the
     *  specified number of elements.  Memory management is more
     *  efficient with this constructor if the number of elements is
     *  known.
     *  @param descriptor A one-word description of the type of elements
     *  that are to be stored in this list.
     *  @param graph The graph associated with this element list.
     *  @param elementCount The number of elements.
     */
    public ElementList(String descriptor, Graph graph, int elementCount) {
        super();
        _descriptor = descriptor;
        _graph = graph;
        _weightMap = new HashMap(elementCount);
        _unweightedSet = new HashSet(elementCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disassociate the given element from its weight information.
     *  @param element The element.
     *  @return True if the weight information was disassociated.
     */
    public boolean cancelWeight(Element element) {
        // FIXME: needs better documentation
        boolean removed = false;
        if (element.hasWeight()) {
            Object weight = element.getWeight();
            ArrayList sameWeightList = (ArrayList)(_weightMap.get(weight));
            if (sameWeightList == null) {
                return false;
            }
            removed = sameWeightList.remove(element);
            if (sameWeightList.size() == 0) {
                _weightMap.remove(weight);
            }
        } else {
            removed = _unweightedSet.remove(element);
        }
        return removed;
    }

    /** Given an element in this list, check if the weight has
     *  changed (since the element was added to the graph or was
     *  last validated, whichever is more recent), and if so,
     *  change the current mapping of a weight to the element or
     *  remove the element from the set of unweighted elements.
     *
     *  @param element The graph element.
     *  @return True if the weight associated with the element has
     *  changed as determined by the equals method.
     */
    public boolean changeWeight(Element element) {
        boolean weightValueHasChanged = false;
        boolean found = false;
        Object newWeight = element.hasWeight() ? element.getWeight() : null;
        if (_unweightedSet.contains(element)) {
            weightValueHasChanged = (newWeight != null);
            if (weightValueHasChanged) {
                _unweightedSet.remove(element);
                registerWeight(element);
            }
        } else {
            // Find the weight that was previously associated with this
            // element, if there was one.
            Iterator weights = _weightMap.keySet().iterator();
            Object nextWeight = null;
            List nextList = null;
            while (weights.hasNext() && !found) {
                nextWeight = weights.next();
                nextList = (List)_weightMap.get(nextWeight);
                found = nextList.contains(element);
            }
            if (found) {
                // Note that the weight can change without the weight
                // comparison here changing (if the change does not affect
                // comparison under the equals method).
                weightValueHasChanged = !nextWeight.equals(newWeight);
                if (weightValueHasChanged) {
                    nextList.remove(element);
                    if (nextList.size() == 0) {
                        _weightMap.remove(nextWeight);
                    }
                    registerWeight(element);
                }
            } else {
                // FIXME: use an internal error exception here.
                throw new RuntimeException("Internal error: the specified "
                        + _descriptor + " is neither unweighted nor associated "
                        + "with a weight."
                        + GraphException.elementDump(element, _graph));

            }
        }
        return weightValueHasChanged;
    }

    /** Clear all of the elements in this list.
     */
    public void clear() {
        super.clear();
        _weightMap.clear();
        _unweightedSet.clear();
    }

    /** Test if the specified object is an element weight in this
     *  list. Equality is
     *  determined by the <code>equals</code> method. If the specified
     *  weight is null, return false.
     *
     *  @param weight The element weight to be tested.
     *  @return True if the specified object is an element weight in this list.
     */
    public boolean containsWeight(Object weight) {
        // FIXME: on null, return true if there is an unweighted element.
        return _weightMap.containsKey(weight);
    }

    /** Return an element in this list that has a specified weight. If multiple
     *  elements have the specified weight, then return one of them
     *  arbitrarily. If the specified weight is null, return an unweighted
     *  element (again arbitrarily chosen if there are multiple unweighted
     *  elements).
     *  @param weight The specified weight.
     *  @return An element that has this weight.
     *  @exception GraphWeightException If the specified weight
     *  is not an element weight in this list or if the specified weight
     *  is null but the list does not contain any unweighted edges.
     */
    public Element element(Object weight) {
        Collection elements = elements(weight);
        if (elements.size() == 0) {
            throw new GraphWeightException(weight, null, _graph,
                    "Invalid weight argument, the number of elements for"
                    + " this weight is zero.");
        }
        return (Element)(elements.iterator().next());
    }

    /** Return all the elements in this list in the form of an unmodifiable
     *  collection.
     *  @return All the elements in this list.
     */
    public Collection elements() {
        return Collections.unmodifiableCollection(this);
    }

    /** Return all the elements in this graph that have a specified weight.
     *  The elements are returned in the form of an unmodifiable collection.
     *  If the specified weight is null, return all the unweighted elements.
     *  If no elements have the specified weight (or if the argument is null and
     *  there are no unweighted elements), return an empty collection.
     *  Each element in the returned collection is an instance of
     *  {@link Element}.
     *  @param weight The specified weight.
     *  @return The elements in this graph that have the specified weight.
     */
    public Collection elements(Object weight) {
        if (weight == null) {
            return Collections.unmodifiableCollection(_unweightedSet);
        } else {
            Collection sameWeightElements = (Collection)_weightMap.get(weight);
            if (sameWeightElements == null) {
                return _emptyCollection;
            } else {
                return Collections.unmodifiableCollection(sameWeightElements);
            }
        }
    }

    /** Associate a graph element to its weight given the relevant mapping of
     *  weights to elements, and the set of unweighted elements of the same
     *  type (nodes or edges). If the element is unweighted, add it to the set
     *  of unweighted elements.
     *  @param element The element.
     */
    public void registerWeight(Element element) {
        if (element.hasWeight()) {
            Object weight = element.getWeight();
            ArrayList sameWeightList = (ArrayList)(_weightMap.get(weight));
            if (sameWeightList == null) {
                sameWeightList = new ArrayList();
                _weightMap.put(weight, sameWeightList);
            }
            sameWeightList.add(element);
        } else {
            _unweightedSet.add(element);
        }
    }

    /** Remove an element from this list if it exists in the list.
     *  This is an <em>O(1)</em> operation.
     * @param element The element to be removed.
     * @return True if the element was removed.
     */
    public boolean remove(Element element) {
        boolean removed = super.remove(element);
        if (removed) {
            cancelWeight(element);
        }
        return removed;
    }

    /** Validate the weight of a given graph element, given the previous
     *  weight of that element.
     *  @param element The element.
     *  @param oldWeight The previous weight (null if the element was previously
     *  unweighted).
     */
    public boolean validateWeight(Element element, Object oldWeight) {
        boolean changed = false;
        Object newWeight = element.hasWeight() ? element.getWeight() : null;
        if (oldWeight == null) {
            if (!_unweightedSet.contains(element)) {
                // This 'dump' of a null weight will also dump the graph.
                throw new GraphWeightException(oldWeight, null, _graph,
                        "Incorrect previous weight specified.");
            }
            if (newWeight == null) {
                return false;
            }
            _unweightedSet.remove(element);
            changed = true;
        } else {
            // The weight may have changed in value even if comparison under
            // the equals method has not changed. Thus we proceed
            // with the removal unconditionally.
            List elementList = (List)_weightMap.get(oldWeight);
            if ((elementList == null) || !elementList.remove(element)) {
                throw new GraphWeightException(oldWeight, null, _graph,
                        "Incorrect previous weight specified.");
            }
            changed = !oldWeight.equals(newWeight);
        }
        registerWeight(element);
        return changed;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A one-word description of the type of elements stored in this list
    private String _descriptor;

    // The graph that this element list is associated with.
    private Graph _graph;

    // An unmodifiable, empty collection.
    private static final Collection _emptyCollection =
    Collections.unmodifiableCollection(new ArrayList(0));

    // The set of elements that do not have weights. Each member is an
    // Element.
    private HashSet _unweightedSet;

    // A mapping from element weights to the associated elements. Unweighted
    // elements are not represented in this map. Keys in this this map
    // are instances of of Object, and values instances of ArrayList
    // whose elements are instances of Element.
    private HashMap _weightMap;


}
