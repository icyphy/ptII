/* Static class for relation node.

 Copyright (c) 2006-2013 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
////RelationNode

/**
 An instance of the RelationNode class stores the type and difference
 information of a relation. For more details of type, see {@link RelationType}.
 For more details of difference, see
 {@link ptolemy.domains.modal.kernel.ParseTreeEvaluatorForGuardExpression}.
 The instance stores the information of two evaluations of the transition,
 the current evaluation and the previous evaluation.

 @author  Haiyang Zheng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public final class RelationNode {

    /** Construct a relation node with given type and difference
     *  information.
     *  @param type The type, one of {@link ptolemy.domains.modal.kernel.RelationType}.
     *  @param difference The difference.
     */
    public RelationNode(int type, double difference) {
        // FIXME: Perhaps the type argument should be a type safe enum?
        _currentType = type;
        _previousType = type;
        _difference = difference;
        _previousDifference = difference;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the relation node by setting the former type and difference
     *  information to RelationType.INVALID and 0.0 respectively.
     */
    public void reset() {
        _previousType = RelationType.INVALID;
        _previousDifference = 0.0;
    }

    /** Update the previous type and difference information of this relation
     *  node with the current evaluation result.
     */
    public void commit() {
        _previousType = _currentType;
        _previousDifference = _difference;
    }

    /** Return the difference information from the current evaluation of the
     *  relation node.
     *  @return The current difference information.
     *  @see #setDifference(double)
     */
    public double getDifference() {
        return _difference;
    }

    /** Return the difference information from the previous evaluation of the
     *  relation node.
     *  @return The previous difference information.
     */
    public double getPreviousDifference() {
        return _previousDifference;
    }

    /** Return true if the relation node has its type changed, and if the
     *  current type is equal/inequal or the current type changes from
     *  less_than to bigger_than or bigger_than to less_than. This is used
     *  to detect whether a continuous variable crosses a level.
     *  @return True If event has been detected.
     */
    public boolean hasEvent() {
        if (typeChanged()) {
            return _previousType * _currentType == RelationType.LESS_THAN
                    * RelationType.GREATER_THAN;
        }
        return false;
    }

    /** Set the type information with the current evaluation result of the
     *  relation node.
     *  @param type The current type information.
     */
    public void setType(int type) {
        _currentType = type;
    }

    /** Set the difference information with the current evaluation result
     *  of the relation node.
     *  @param difference The current difference information.
     *  @see #getDifference()
     */
    public void setDifference(double difference) {
        _difference = difference;
    }

    /** Return true if the type changed and the previous type
     *  information is valid.
     *  @return True If the type changed and the previous type
     *  information is valid.
     */
    public boolean typeChanged() {
        return _previousType != RelationType.INVALID
                && _previousType != _currentType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                  ////
    private int _currentType;

    private double _difference;

    private double _previousDifference;

    private int _previousType;
}
