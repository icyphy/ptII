/* A relationList contains the change of the relations.

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
@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.LinkedList;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// RelationList
/** 

A RelationList contains a list of relations of a guard expression.  It
provides facilities to access the former and current information of
each relation of a guard expression during its evaluation. The
information includes type and difference information. (See
ParseTreeEvaluatorForGuardExpression for detailed explanation of type
and difference.) This attribute is non-persistent and will not be
exported into MoML.

<p> This class is designed to be used with
 ParseTreeEvaluatorForGuardExpression.  The common usage would be
 like:

 <p> <i>// Construct a relation list for a transition.  // The first
  argument of the constructor is a // transition.</i> _relationList =
  new RelationList(this, "relationList");
 
<p> <i>// Associate the relation list with the
         ParseTreeEvaluatorForGuardExpression</i> _parseTreeEvaluator
         = new ParseTreeEvaluatorForGuardExpression(_relationList);

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 2.2
@see ParseTreeEvaluatorForGuardExpression
@see ptolemy.domains.fsm.kernel.Transition
*/
public class RelationList extends Attribute {

    /** Construct a relation list with the given name contained by
     *  the specified transition. The transition argument must not be
     *  null, or a NullPointerException will be thrown. This action
     *  will use the workspace of the transition for synchronization
     *  and version counts. If the name argument is null, then the
     *  name is set to the empty string.
     *  Increment the version of the workspace.
     *  This attribute is a non-persistent and it will not be exported
     *  into MoML file.
     *  @param transition The transition container.
     *  @param name The name of this relation list.
     *  @exception IllegalActionException If the relation list is not
     *   of an acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name.
     */
    public RelationList(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
        setPersistent(false);
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

    /** Clear the relation list by resetting each relation node.
     */
    public void clearRelationList() {
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            ((RelationNode)relations.next()).clear();
        }
    }

    /** Update the relation list by updating each relation node.
     */
    public void commitRelationValues() {
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            ((RelationNode)relations.next()).commit();
        }
    }

    /** Destroy the relation list by deleting all its elements.
     */
    public void destroy() {
        _relationList.clear();
    }

    /** Return the former difference of the relation which has the
     *  maximum current difference.
     *  @return The former distance of a relation.
     */
    public double getFormerMaximumDistance() {
        return ((RelationNode) _relationList.get(
                        _maximumDifferenceIndex)).getFormerDifference();
    }

    /** Return true if there is some event caused by some relation
     *  type change.  Return True If there is some event detected.
     */
    public boolean hasEvent(){
        boolean result = false;
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext() && !result) {
            result = result || ((RelationNode)relations.next()).hasEvent();
        }
        if (result && _debugging) {
            _debug("Detected event!");
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
    
    /** Iterating the relation list and get the maximum current
     *  difference of all the relations.
     *  @return maximumDistance The maximum current distance.
     */
    public double maximumDifference() {
        double maxDifference = 0.0;
        double difference = 0.0;
        int index = 0;
        _maximumDifferenceIndex = 0;
        ListIterator relations = _relationList.listIterator();
        while (relations.hasNext()) {
            RelationNode relation = ((RelationNode)relations.next());
            difference = Math.abs(relation.getDifference());
            if (relation.typeChanged() && difference > maxDifference) {
                maxDifference = difference;
                _maximumDifferenceIndex = index;
            }
            index++;
        }
        return maxDifference;
    }

    /** Return the index of the relation with the maximum current
     *  difference.
     *  @return index The index of the relation with maximum current
     *  distance.
     */
    public int maximumDifferenceIndex() {
        return _maximumDifferenceIndex;
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
        RelationNode relationNode = (RelationNode)
            _relationList.get(relationIndex);
        relationNode.setValue(type);
        relationNode.setDifference(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    // The index for the relation with the maximum current difference.
    private int _maximumDifferenceIndex;
    // The relation list.
    private LinkedList _relationList;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An inner class relation node store the type and difference information
     *  about a relation. It not only stores the current information but
     *  also the former one.
     */
    private class RelationNode {

        /** Constructor to construct a relation node with given type and
         *  difference information.
         */
        public RelationNode(int type, double difference) {
            _currentType = type;
            _formerType = type;
            _difference = difference;
            _formerDifference = difference;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Reset the relation node with setting the former type and difference
         *  information to 0 and 0.0 respectively. Note that the type having
         *  value as 0 indicating the former information is not valid.
         */
        public void clear() {
            _formerType = 0;
            _formerDifference = 0.0;
        }

        /** Update the relation node former type and difference information
         *  with the current information.
         */
        public void commit() {
            _formerType = _currentType;
            _formerDifference = _difference;
        }

        public int getCurrentValue() {
            return _currentType;
        }

        public double getDifference() {
            return _difference;
        }

        public double getFormerDifference() {
            return Math.abs(_formerDifference);
        }

        public int getFormerValue() {
            return _formerType;
        }

        /** Return true, if the relation node has its type changed, and if the
         *  current type is equal/inequal or the current type changes from
         *  less_than to bigger_than or bigger_than to less_than. This is used
         *  to detect whether a continuous variable cross a level.
         *  @return True If event has been detected.
         */
        public boolean hasEvent(){
            if (typeChanged()) {
                return  (_formerType * _currentType == 20);
            }
            return false;
        }

        public void setValue(int type) {
            _currentType = type;
        }

        public void setDifference(double difference) {
            _difference = difference;
        }

        /** Return true if type changed and the former type
         *  information is valid.
         *  @return True If the type changed and the former type
         *  information is valid.
         */
        public boolean typeChanged(){
            return (_formerType != 0) && (_formerType != _currentType);
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner fields            ////

        private int _currentType;
        private double _difference;
        private double _formerDifference;
        // the relations have five integer values with meanings:
        // 1: true; 2: false; 3: equal/inequal; 4: less_than: 5: bigger_than.
        private int _formerType;
    }
}
