/* A RelationList object contains the information of the relations of a
 guard expression.

 Copyright (c) 2003-2013 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.LinkedList;
import java.util.ListIterator;

///////////////////////////////////////////////////////////////////
//// RelationList

/**
 A RelationList object contains a list of relations of a guard expression.
 Relations are comparison operations, for example "x &ge; 10", where x is
 a variable.  This class provides facilities for measuring how far the
 expression is from the threshold; for example, if x = 7, then the
 distance to the threshold is 3.  Moreover, it provides a mechanism
 for recording the current distance (via a "commit") and then later
 accessing that distance and comparing it against the current distance.
 The information
 includes relation type and the difference information. (See
 {@link ParseTreeEvaluatorForGuardExpression} for the detailed explanation
 of relation type and difference.) This attribute is non-persistent and will
 not be exported into MoML.
 <p>
 This class is designed to be used with ParseTreeEvaluatorForGuardExpression.
 The common usage would be like:
 <p>
 <i>Construct a relation list for a transition with the first argument of the
 constructor as that transition.</i>
 <pre>
 _relationList = new RelationList();
 </pre>
 <p>
 <i>Associate the relation list with the an object of
 ParseTreeEvaluatorForGuardExpression</i>
 <pre>
 _parseTreeEvaluator =
 new ParseTreeEvaluatorForGuardExpression(_relationList, getErrorTolerance());
 </pre>
 <p>
 See {@link Transition} for the detailed usage.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class RelationList {
    /** Construct a relation list.
     */
    public RelationList() {
        _relationList = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a relation node with the given type and difference
     *  information of a relation, and add it to the end of the relation
     *  list.
     *  @param type The relation type of the relation.
     *  @param difference The difference of the relation.
     */
    public void addRelation(int type, double difference) {
        _relationList.add(new RelationNode(type, difference));
    }

    /** Reset the relation list by resetting each relation node.
     */
    public void resetRelationList() {
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            ((RelationNode) relations.next()).reset();
        }
    }

    /** Record the current relation values so that when getPreviousMaximumDistance()
     *  is called, these recorded values are used.
     *  @see #getPreviousMaximumDistance()
     */
    public void commitRelationValues() {
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            ((RelationNode) relations.next()).commit();
        }
    }

    /** Destroy the relation list by deleting all the contained elements.
     */
    public void destroy() {
        _relationList.clear();
    }

    /** Return the previous difference of the relation that has the
     *  maximum current difference.  This is the value as of the last
     *  call to commitRelationValues().
     *  @see #commitRelationValues()
     *  @return The previous distance of a relation.
     */
    public double getPreviousMaximumDistance() {
        return ((RelationNode) _relationList.get(_maximumDifferenceIndex))
                .getPreviousDifference();
    }

    /** Return true if there exists an event caused by the type change of
     *  any relation.
     *  @return True If there exits an event.
     */
    public boolean hasEvent() {
        boolean result = false;
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext() && !result) {
            result = result || ((RelationNode) relations.next()).hasEvent();
        }
        return result;
    }

    /** Return true if the relation list is empty.
     *  @return True If the relation list is empty.
     */
    public boolean isEmpty() {
        return _relationList.size() == 0;
    }

    /** Return the number of relations in the relation list.
     *  @return the number of relations in the relation list.
     */
    public int length() {
        return _relationList.size();
    }

    /** Return the maximum current difference of all the relations by iterating
     *  the relation list.
     *  @return maximumDistance The maximum current distance.
     */
    public double getMaximumDifference() {
        double maxDifference = 0.0;
        double difference = 0.0;
        int index = 0;
        _maximumDifferenceIndex = 0;
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            RelationNode relation = (RelationNode) relations.next();
            difference = Math.abs(relation.getDifference());
            if (relation.typeChanged() && difference > maxDifference) {
                maxDifference = difference;
                _maximumDifferenceIndex = index;
            }
            index++;
        }
        return maxDifference;
    }

    /** Update the relation in the relation list referred by the
     *  relation index argument with the given type and difference
     *  information.
     *  @param relationIndex The position of the relation in the
     *  relation list.
     *  @param type The current type of the relation.
     *  @param difference The current difference of the relation.
     */
    public void setRelation(int relationIndex, int type, double difference) {
        RelationNode relationNode = (RelationNode) _relationList
                .get(relationIndex);
        relationNode.setType(type);
        relationNode.setDifference(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    // The index for the relation with the maximum current difference.
    private int _maximumDifferenceIndex;

    // The relation list.
    private LinkedList _relationList;
}
