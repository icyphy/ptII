/* A finite concept in an ontology that represents a flat set of infinite
 * concepts that map to an interval of scalar numbers.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.data.ontologies;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// FlatScalarTokenRepresentativeConcept

/** A finite concept in an ontology that represents a flat set of infinite
 *  concepts that map to an interval of scalar numbers.
 *  @see FlatScalarTokenInfiniteConcept
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class FlatScalarTokenRepresentativeConcept extends
        FlatTokenRepresentativeConcept {

    /** Create a new FlatScalarTokenRepresentativeConcept with the specified
     *  name and ontology.
     *
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public FlatScalarTokenRepresentativeConcept(CompositeEntity ontology,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(ontology, name);

        // Parameters that determine the interval of scalar numbers covered by
        // the infinite concepts represented by this concept.
        leftEndPoint = new Parameter(this, "leftEndPoint");
        leftEndPoint.setTypeEquals(BaseType.SCALAR);
        leftIntervalClosed = new Parameter(this, "leftIntervalClosed");
        leftIntervalClosed.setTypeEquals(BaseType.BOOLEAN);
        leftIntervalClosed.setToken(BooleanToken.FALSE);

        rightEndPoint = new Parameter(this, "rightEndPoint");
        rightEndPoint.setTypeEquals(BaseType.SCALAR);
        rightIntervalClosed = new Parameter(this, "rightIntervalClosed");
        rightIntervalClosed.setTypeEquals(BaseType.BOOLEAN);
        rightIntervalClosed.setToken(BooleanToken.FALSE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   parameters and ports                    ////

    /** The value of the left endpoint of the scalar interval. */
    public Parameter leftEndPoint;

    /** Indicates whether or not the interval is closed on its left endpoint. */
    public Parameter leftIntervalClosed;

    /** The value of the right endpoint of the scalar interval. */
    public Parameter rightEndPoint;

    /** Indicates whether or not the interval is closed on its right endpoint. */
    public Parameter rightIntervalClosed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If there is a change to the parameters
     *  that specify the endpoints of the interval for the scalar infinite concepts
     *  represented by this concept, change the interval endpoint values or the
     *  flags that determine whether each endpoint is closed or open.
     *  @param attribute The attribute that has changed in this concept.
     *  @exception IllegalActionException Thrown if the interval is invalid (the specified
     *   left endpoint is greater than the right endpoint, or vice versa).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.equals(leftEndPoint)) {
            if (leftEndPoint.getToken() != null
                    && _rightEndPoint != null
                    && ((ScalarToken) leftEndPoint.getToken()).isGreaterThan(
                            _rightEndPoint).booleanValue()) {
                throw new IllegalActionException(this, "The left end point of "
                        + "the interval must be less than or equal to the "
                        + "right end point.");
            }
            _leftEndPoint = (ScalarToken) leftEndPoint.getToken();

            if (_leftEndPoint != null && _isInfinity(_leftEndPoint)) {
                leftIntervalClosed.setToken(BooleanToken.FALSE);
                leftIntervalClosed.setVisibility(Settable.NOT_EDITABLE);
            } else {
                leftIntervalClosed.setVisibility(Settable.FULL);
            }
        } else if (attribute.equals(rightEndPoint)) {
            if (rightEndPoint.getToken() != null
                    && _leftEndPoint != null
                    && ((ScalarToken) rightEndPoint.getToken()).isLessThan(
                            _leftEndPoint).booleanValue()) {
                throw new IllegalActionException(
                        this,
                        "The right end point of "
                                + "the interval must be greater than or equal to the "
                                + "left end point.");
            }
            _rightEndPoint = (ScalarToken) rightEndPoint.getToken();

            if (_rightEndPoint != null && _isInfinity(_rightEndPoint)) {
                rightIntervalClosed.setToken(BooleanToken.FALSE);
                rightIntervalClosed.setVisibility(Settable.NOT_EDITABLE);
            } else {
                rightIntervalClosed.setVisibility(Settable.FULL);
            }
        } else if (attribute.equals(leftIntervalClosed)) {
            _isLeftIntervalClosed = ((BooleanToken) leftIntervalClosed
                    .getToken()).booleanValue();
        } else if (attribute.equals(rightIntervalClosed)) {
            _isRightIntervalClosed = ((BooleanToken) rightIntervalClosed
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Determine whether or not the specified ScalarToken is within the interval
     *  specified by this representative concept.
     *  @param value The specified ScalarToken to be tested.
     *  @return True if the ScalarToken value is within the interval, false otherwise.
     *  @exception IllegalActionException Thrown if the interval endpoints have not
     *   been specified.
     */
    public boolean withinInterval(ScalarToken value)
            throws IllegalActionException {
        if (_leftEndPoint == null || _rightEndPoint == null) {
            throw new IllegalActionException(this,
                    "Both end points of the interval must have "
                            + "a scalar value specified.");
        }
        if (value.isLessThan(_rightEndPoint).booleanValue()
                && value.isGreaterThan(_leftEndPoint).booleanValue()) {
            return true;
        } else if (value.isEqualTo(_leftEndPoint).booleanValue()
                && _isLeftIntervalClosed) {
            return true;
        } else if (value.isEqualTo(_rightEndPoint).booleanValue()
                && _isRightIntervalClosed) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new FlatScalarTokenInfiniteConcept for the given concept string.
     *  @param infiniteConceptString The specified concept string that
     *   represents the FlatScalarTokenInfiniteConcept to be created.
     *  @return The newly created FlatScalarTokenInfiniteConcept object.
     *  @exception IllegalActionException Thrown if a valid
     *   FlatScalarTokenInfiniteConcept cannot be created.
     */
    @Override
    protected FlatScalarTokenInfiniteConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException {
        return (FlatScalarTokenInfiniteConcept) super
                ._createInfiniteConceptInstance(infiniteConceptString);
    }

    /** Return a new FlatScalarTokenInfiniteConcept for this representative with
     *  the given token value.
     *
     *  @param tokenValue The token value for the FlatTokenInfiniteConcept
     *          to be instantiated.
     *  @return A new FlatTokenInfiniteConcept
     *  @exception IllegalActionException Thrown if the FlatTokenInfiniteConcept
     *   cannot be created.
     */
    @Override
    protected FlatScalarTokenInfiniteConcept _instantiateFlatTokenInfiniteConcept(
            Token tokenValue) throws IllegalActionException {
        if (tokenValue instanceof ScalarToken) {
            return FlatScalarTokenInfiniteConcept
                    .createFlatScalarTokenInfiniteConcept(getOntology(), this,
                            (ScalarToken) tokenValue);
        } else {
            throw new IllegalActionException(this, "Cannot create a new "
                    + "FlatScalarTokenInfiniteConcept with a token value "
                    + "that is not a scalar token: " + tokenValue);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Determines whether or not the specified scalar token endpoint value
     *  is either positive or negative infinity, which implies that the interval
     *  must be open on its endpoint.
     *  @param value The specified scalar token to be tested.
     *  @return true if the token is either positive or negative infinity, false
     *   otherwise.
     *  @exception IllegalActionException Thrown if the token isEqualTo methods
     *   throw an exception.
     */
    private boolean _isInfinity(ScalarToken value)
            throws IllegalActionException {
        if (value.isEqualTo(new DoubleToken(Double.POSITIVE_INFINITY))
                .booleanValue()
                || value.isEqualTo(new DoubleToken(Double.NEGATIVE_INFINITY))
                        .booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicates whether or not the interval is closed on its left endpoint. */
    private boolean _isLeftIntervalClosed = false;

    /** Indicates whether or not the interval is closed on its right endpoint. */
    private boolean _isRightIntervalClosed = false;

    /** The value of the left endpoint of the scalar interval. */
    private ScalarToken _leftEndPoint = null;

    /** The value of the right endpoint of the scalar interval. */
    private ScalarToken _rightEndPoint = null;
}
