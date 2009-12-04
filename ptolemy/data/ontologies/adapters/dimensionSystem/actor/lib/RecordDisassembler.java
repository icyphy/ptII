/*
 * An adapter class for ptolemy.actor.lib.RecordDisassembler.
 * 
 * Copyright (c) 2006-2009 The Regents of the University of California. All
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
package ptolemy.data.properties.lattice.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.RecordProperty;
import ptolemy.data.properties.lattice.PropertyTermManager.InequalityTerm;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

////RecordDisassembler

/**
 * An adapter class for ptolemy.actor.lib.RecordDisassembler.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class RecordDisassembler extends AtomicActor {

    /**
     * Construct a RecordDisassembler adapter for the staticDynamic lattice. This
     * set a permanent constraint for the output port to be STATIC, but does not
     * use the default actor constraints.
     * @param solver The given solver.
     * @param actor The given RecordDisassembler actor
     * @exception IllegalActionException
     */
    public RecordDisassembler(PropertyConstraintSolver solver,
            ptolemy.actor.lib.RecordDisassembler actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                            public methods                 ////

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.RecordDisassembler actor = (ptolemy.actor.lib.RecordDisassembler) getComponent();
        // add default constraints if no constraints specified in actor adapter

        Object[] portArray = actor.outputPortList().toArray();
        int size = portArray.length;
        String[] labels = new String[size];
        LatticeProperty[] properties = new LatticeProperty[size];

        // Constrain the property for the input port so that
        // it cannot be anything greater than a RecordProperty.
        for (int i = 0; i < size; i++) {
            labels[i] = ((Port) portArray[i]).getName();
            properties[i] = (LatticeProperty) _lattice.top();
        }

        RecordProperty declaredProperty = new RecordProperty(_lattice, labels,
                properties);

        setAtMost(actor.input, declaredProperty);

        // set the constraints between record fields and output ports

        // since the input port has a clone of the above RecordProperty, need to
        // get the type from the input port.
        //   RecordProperty inputProperties = (RecordProperty)input.getProperty();
        for (TypedIOPort outputPort : (List<TypedIOPort>) actor
                .outputPortList()) {
            String label = outputPort.getName();

            setAtLeast(outputPort, new PortFunction(label));
            //Inequality inequality = new Inequality(new PortFunction(label),
            //        outputPort.getPropertyTerm());
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the type of a
    // port and a parameter.
    // The function value is determined by:
    // f(input.getProperty(), name) =
    //     UNKNOWN,                  if input.getProperty() = UNKNOWN
    //     input.getProperty()[name] if input.getProperty() instanceof RecordToken.
    //
    private class PortFunction extends MonotonicFunction {
        private PortFunction(String name) {
            _name = name;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /**
         * Return the function result.
         * @return A Property.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            ptolemy.actor.lib.RecordDisassembler actor = (ptolemy.actor.lib.RecordDisassembler) getComponent();

            Property inputProperty = _solver.getProperty(actor.input);

            if (inputProperty == _lattice.bottom()) {
                return inputProperty;
            } else if (inputProperty instanceof RecordProperty) {
                RecordProperty property = (RecordProperty) inputProperty;
                Property fieldProperty = property.get(_name);

                if (fieldProperty == null) {
                    return _lattice.bottom();
                } else {
                    return fieldProperty;
                }
            } else {
                throw new IllegalActionException(actor,
                        "Invalid property for the input port");
            }
        }

        /**
         * Return an additional string describing the current value of this
         * function.
         */
        @Override
        public String getVerboseString() {
            ptolemy.actor.lib.RecordDisassembler actor = (ptolemy.actor.lib.RecordDisassembler) getComponent();
            Property inputProperty = _solver.getProperty(actor.input);

            if (inputProperty instanceof RecordProperty) {
                RecordProperty property = (RecordProperty) inputProperty;
                Property fieldProperty = property.get(_name);

                if (fieldProperty == null) {
                    return "Input Record doesn't have field named " + _name;
                }
            }

            return null;
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
        private final String _name;

        /*
         * (non-Javadoc)
         * @see ptolemy.data.properties.lattice.MonotonicFunction#_getDependentTerms()
         */
        @Override
        protected InequalityTerm[] _getDependentTerms() {
            ptolemy.actor.lib.RecordDisassembler actor = (ptolemy.actor.lib.RecordDisassembler) getComponent();

            return new InequalityTerm[] {
                (InequalityTerm) getPropertyTerm(actor.input)
            };
        }

        /*
         * (non-Javadoc)
         * @see ptolemy.data.properties.lattice.PropertyTerm#isEffective()
         */
        //@Override
        public boolean isEffective() {
            return true;
        }

        /*
         * (non-Javadoc)
         * @see ptolemy.data.properties.lattice.PropertyTerm#setEffective(boolean)
         */
        //@Override
        public void setEffective(boolean isEffective) {
        }
    }
}
