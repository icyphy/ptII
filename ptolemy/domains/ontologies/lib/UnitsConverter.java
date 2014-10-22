/* An actor that outputs a scaled and offset version of the input based on a
 * unit conversion factor specified in the unitSystem ontology.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.ontologies.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.lattice.unit.UnitConcept;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ObjectType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// UnitsConverter

/**
 * An actor that outputs a scaled and offset version of the input based on a
 * unit conversion factor specified in the unitSystem ontology.
 Produce an output token on each firing with a value that transforms
 the input value according to the conversion required between the specified
 input and output unit measurements. The unit measurements are taken from the unitSystem
 {@link ptolemy.data.ontologies.Ontology Ontology} and an
 {@link ptolemy.data.ontologies.OntologySolver OntologySolver}
 for the unitSystem ontology must be present in the model.
 For data types where multiplication is not commutative (such
 as matrices), whether the factor is multiplied on the left is controlled
 by the <i>scaleOnLeft</i> parameter. Setting the parameter to true means
 that the factor is  multiplied on the left, and the input
 on the right. Otherwise, the factor is multiplied on the right.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class UnitsConverter extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UnitsConverter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        unitSystemOntologySolver = new Parameter(this,
                "unitSystemOntologySolver");
        unitSystemOntologySolver.setTypeEquals(new ObjectType(
                OntologySolver.class));

        dimensionConcept = new StringAttribute(this, "dimensionConcept");
        inputUnitConcept = new StringAttribute(this, "inputUnitConcept");
        outputUnitConcept = new StringAttribute(this, "outputUnitConcept");

        scaleOnLeft = new Parameter(this, "scaleOnLeft");
        scaleOnLeft.setExpression("true");
        scaleOnLeft.setVisibility(Settable.EXPERT);

        conversionLabel = new StringAttribute(this, "conversionLabel");
        conversionLabel.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the dimension from which both units are derived. */
    public StringAttribute dimensionConcept;

    /** The conversion label string that will be displayed on the actor icon.
     *  It is of the form "inputUnits -&gt; outputUnits"
     *  For example, if the actor converts kilometers to miles, its icon label
     *  would be: "km -&gt; mi"
     *  This label will change when the user changes the unit conversion. */
    public StringAttribute conversionLabel;

    /** The input unit measurement for the actor. This specifies the units
     *  in which the input value is received. It will be a
     *  {@link ptolemy.data.ontologies.Concept Concept} in the
     *  unitSystem {@link ptolemy.data.ontologies.Ontology Ontology}.
     */
    public StringAttribute inputUnitConcept;

    /** The output unit measurement for the actor. This specifies the units
     *  to which the output value is transformed. It will be a
     *  {@link ptolemy.data.ontologies.Concept Concept} in the
     *  unitSystem {@link ptolemy.data.ontologies.Ontology Ontology}.
     */
    public StringAttribute outputUnitConcept;

    /** Multiply on the left.
     *  This parameter controls whether the units transformation factor is multiplied
     *  on the left. The default value is a boolean token of value true.
     *  Setting is to false will multiply the factor on the right.
     */
    public Parameter scaleOnLeft;

    /** The unitSystem ontology solver in the model that contains the unitSystem
     *  ontology.
     */
    public Parameter unitSystemOntologySolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the conversionLabel attribute value when either the inputUnitConcept
     *  or the outputUnitConcept attributes change. For any other attribute change
     *  call the superclass method.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException Thrown if there is a problem setting
     *   the expression of the conversionLabel attribute.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.equals(inputUnitConcept)
                || attribute.equals(outputUnitConcept)) {
            conversionLabel.setExpression(inputUnitConcept.getExpression()
                    + " -> " + outputUnitConcept.getExpression());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Transform the input value from its original units measurement to the output
     *  units measurement.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            Token in = input.get(0);
            Token result = null;

            if (_inputUnitConcept == null || _outputUnitConcept == null) {
                // If the units for the input and output have not been set, do
                // not modify the values.
                result = in;
            } else {
                ScalarToken inputFactor = _inputUnitConcept.getUnitFactor();
                ScalarToken inputOffset = _inputUnitConcept.getUnitOffset();
                ScalarToken outputFactor = _outputUnitConcept.getUnitFactor();
                ScalarToken outputOffset = _outputUnitConcept.getUnitOffset();

                Token valueSIUnits = null;

                if (((BooleanToken) scaleOnLeft.getToken()).booleanValue()) {
                    // Scale on the left.  Transform the input value
                    // from its original units to the SI units for
                    // this dimension.
                    valueSIUnits = inputFactor.multiply(in.add(inputOffset));

                    // Transform the value in SI units to the specified output units.
                    result = outputFactor.divideReverse(valueSIUnits).subtract(
                            outputOffset);
                } else {
                    // Scale on the right.  Transform the input value
                    // from its original units to the SI units for
                    // this dimension.
                    valueSIUnits = in.add(inputOffset).multiply(inputFactor);

                    // Transform the value in SI units to the specified output units.
                    result = valueSIUnits.divide(outputFactor).subtract(
                            outputOffset);
                }
            }
            output.send(0, result);
        }
    }

    /** Return the UnitInformation Concept in the unitSystem ontology for the
     *  specified string attribute which represents the unit specification
     *  for the either the actor's input or output port.
     *  @param fromInput true if we want the concept specified by the inputUnitConcept
     *   and false if we want the concept specified by the outputUnitConcept.
     *  @return The UnitInformation concept associated with this unit concept name.
     *  @exception IllegalActionException Thrown if the ontology solver has not
     *   been specified, the unit name cannot be found in the unitSystem ontology, or
     *   the attribute passed in is not one of the actor's inputUnitConcept
     *   or outputUnitConcept attributes.
     */
    public UnitConcept getUnitConcept(boolean fromInput)
            throws IllegalActionException {
        Ontology unitOntology = _getUnitOntology();
        if (unitOntology == null) {
            throw new IllegalActionException(this, "The unit system ontology "
                    + "solver has not been specified.");
        }

        StringAttribute unitConceptName = null;
        if (fromInput) {
            unitConceptName = inputUnitConcept;
        } else {
            unitConceptName = outputUnitConcept;
        }
        String unitName = unitConceptName.getValueAsString();
        if (unitName != null && !unitName.equals("")) {
            String dimensionConceptName = dimensionConcept.getValueAsString();
            if (dimensionConceptName == null) {
                dimensionConceptName = "";
            }

            Concept unitConcept = unitOntology
                    .getConceptByString(dimensionConceptName + "_" + unitName);
            if (unitConcept instanceof UnitConcept) {
                return (UnitConcept) unitConcept;
            } else {
                throw new IllegalActionException(this, "Could not find unit "
                        + "named: " + dimensionConceptName + "_" + unitName
                        + " in the ontology.");
            }
        } else {
            return null;
        }
    }

    /** Return the unitSystem ontology solver specified by the actor's
     *  unitSystemOntologySolver parameter.
     *  @return The unitSystem ontology solver, or null if it is not specified.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the ontology solver object from the parameter.
     */
    public OntologySolver getUnitOntologySolver() throws IllegalActionException {
        Token unitSolverToken = unitSystemOntologySolver.getToken();
        if (unitSolverToken != null) {
            return (OntologySolver) ((ObjectToken) unitSolverToken).getValue();
        } else {
            return null;
        }
    }

    /** Preinitialize the actor by setting the unit system ontology
     *  concepts for the input and output ports.
     *  @exception IllegalActionException Thrown if the input or output unit
     *   concepts are incorrectly specified.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _inputUnitConcept = getUnitConcept(true);
        _outputUnitConcept = getUnitConcept(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the unitSystem ontology from the ontology solver specified by the
     *  unitSystemOntologySolver parameter for this actor.
     *  @return The unitSystem ontology, or null if it is not specified or
     *   the ontology solver is not specified.
     *  @exception IllegalActionException Thrown if there is a problem getting the
     *   ontology object from the ontology solver.
     */
    private Ontology _getUnitOntology() throws IllegalActionException {
        OntologySolver unitSolver = getUnitOntologySolver();
        if (unitSolver != null) {
            return unitSolver.getOntology();
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ontology concept that represents the units specification for the
     *  input port.
     */
    private UnitConcept _inputUnitConcept;

    /** The ontology concept that represents the units specification for the
     *  output port.
     */
    private UnitConcept _outputUnitConcept;
}
