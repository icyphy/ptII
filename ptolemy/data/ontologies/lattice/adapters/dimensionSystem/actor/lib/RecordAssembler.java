/*
 * An adapter class for ptolemy.actor.lib.RecordAssembler.
 * 
 * Copyright (c) 2006-2010 The Regents of the University of California. All
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
package ptolemy.data.ontologies.lattice.adapters.dimensionSystem.actor.lib;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.RecordProperty;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;

////RecordAssembler

/**
 * An adapter class for ptolemy.actor.lib.RecordAssembler.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class RecordAssembler extends AtomicActor {

    /**
     * Construct a RecordAssembler adapter for the staticDynamic lattice. This
     * set a permanent constraint for the output port to be STATIC, but does not
     * use the default actor constraints.
     * @param solver The given solver.
     * @param actor The given RecordAssembler actor
     * @exception IllegalActionException
     */
    public RecordAssembler(PropertyConstraintSolver solver,
            ptolemy.actor.lib.RecordAssembler actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.RecordAssembler actor = (ptolemy.actor.lib.RecordAssembler) getComponent();
        // add default constraints if no constraints specified in actor adapter

        Object[] portArray = actor.inputPortList().toArray();
        int size = portArray.length;
        String[] labels = new String[size];
        LatticeProperty[] properties = new LatticeProperty[size];

        // form the declared type for the output port
        for (int i = 0; i < size; i++) {
            labels[i] = ((Port) portArray[i]).getName();
            properties[i] = _lattice.getElement("UNKNOWN");
        }

        RecordProperty declaredProperty = new RecordProperty(_lattice, labels,
                properties);

        setEquals(actor.output, declaredProperty);
        //setAtLeast(actor.output, declaredProperty);

        // set the constraints between record fields and input ports
        //_ownConstraints = new HashSet<Inequality>();

        // since the output port has a clone of the above RecordType, need to
        // get the type from the output port.
        RecordProperty outputProperty = (RecordProperty) _solver
                .getProperty(actor.output);

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            String label = inputPort.getName();

            setAtLeast(outputProperty.getPropertyTerm(label), inputPort);
        }

        return super.constraintList();
    }
}
