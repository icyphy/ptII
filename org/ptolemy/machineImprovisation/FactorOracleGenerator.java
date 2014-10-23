/** A Factor Oracle (FO) builder from an input sequence.

Copyright (c) 2013-2014 The Regents of the University of California.
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
package org.ptolemy.machineImprovisation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.modal.modal.ModalTableauFactory;

/** A Factor Oracle (FO) builder from an input sequence. Given an input
 * of type String, recognizes a set of alphabet by recognizing the
 * distinct characters in the symbol and builds a factor oracle data
 * structure.
 *
 * <p>In the future, this actor will be replaced by an on-line algorithm
 * which adds to the data structure as more symbols are received at
 * the input and the string requirement will be replaced by a music
 * sequence specification.</p>
 *
 * <p>This actor builds a factor oracle data structure that represents
 * a finite acyclic automaton that contains at least all the suffixes of
 * a given input sequence.</p>

 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating non
 */
public class FactorOracleGenerator extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FactorOracleGenerator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        noteSequence = new TypedIOPort(this, "noteSequence", true, false);
        noteSequence.setTypeEquals(BaseType.OBJECT);

        repetitionProbability = new PortParameter(this, "repetitionFactor");
        repetitionProbability.setTypeEquals(BaseType.DOUBLE);
        repetitionProbability.setExpression("0.9");

        usePitchSpecs = new Parameter(this, "usePitchSpecs");
        usePitchSpecs.setTypeEquals(BaseType.BOOLEAN);
        usePitchSpecs.setExpression("true");

        _adjacencyList = new HashMap<Integer, List<Integer>>();

        pitchMoML = new TypedIOPort(this, "pitchMoML", false, true);
        pitchMoML.setTypeEquals(BaseType.STRING);

        durationMoML = new TypedIOPort(this, "durationMoML", false, true);
        durationMoML.setTypeEquals(BaseType.STRING);

        _pitchSequence = new LinkedList();
        _durationSequence = new LinkedList();
        _durationOracles = new LinkedList<FactorOracle>();
        _pitchLicks = new LinkedList<List>();

    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == repetitionProbability) {
            double token = ((DoubleToken) repetitionProbability.getToken())
                    .doubleValue();
            if (token > 1.0 || token < 0.0) {
                throw new IllegalActionException(this,
                        "Repetition factor must be in range [0.0,1.0].");
            }
            _repetitionFactor = token;
        } else if (attribute == usePitchSpecs) {
            boolean usePitch = ((BooleanToken) usePitchSpecs.getToken())
                    .booleanValue();
            _usePitch = usePitch;
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The ordered note sequence consisting of {@link Note} objects.
     */
    public TypedIOPort noteSequence;

    /**
     * The repetition probability of the forward links in the Factor Oracle
     */
    public PortParameter repetitionProbability;

    /**
     * Parameter indicating whether to check pitch specifications
     */
    public Parameter usePitchSpecs;

    /**
     * The pitch oracle in MoML format
     */
    public TypedIOPort pitchMoML;

    /**
     * The duration oracle in MoML format
     */
    public TypedIOPort durationMoML;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException {

        super.fire();

        repetitionProbability.update();

        if (noteSequence.hasToken(0)) {
            Note incomingNote = (Note) ((ObjectToken) noteSequence.get(0))
                    .getValue();

            double currentNoteDuration = incomingNote.getDuration();
            String currentNoteName = incomingNote.getName();
            // add new note to the list

            //FIXME: Termination = "T" note received notes received
            // if we receive a termination note, construct the oracle
            if (currentNoteName.equals(MusicSpecs.TERMINATION_NOTE_SYMBOL)) {
                if (_pitchLicks.isEmpty() || _pitchLicks.get(0).isEmpty()) {
                    if (!_pitchSequence.isEmpty()) {
                        LinkedList s = new LinkedList();
                        for (int i = 0; i < _pitchSequence.size(); i++) {
                            s.add(_pitchSequence.get(i));
                        }
                        _pitchLicks.add(s);
                        _pitchSequence.clear();
                    }
                }
                if (!_pitchLicks.isEmpty()) {
                    try {
                        _constructNewFactorOracle();
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(
                                this,
                                "Attempted to create object with duplicate name while creating new Factor Oracle");
                    }
                    this.reset();
                }
            } else {
                if (currentNoteName.equals("R")) {
                    _durationSequence.add(-currentNoteDuration);
                } else {
                    _durationSequence.add(currentNoteDuration);
                }
                if (!incomingNote.isRest()) {
                    _pitchSequence.add(currentNoteName);
                }
                // identify licks
                // enforcing at least four notes in a lick for now. because notes by themselves as licks
                // are infeasible.
                if ((currentNoteDuration >= 2.0 || incomingNote.isRest())
                        && _pitchSequence.size() >= 4) {
                    // long notes and a duration of 2 beats trigger new FO generation

                    // add the new FO sequence to the list of lists
                    List subSequence = new LinkedList();
                    for (int i = 0; i < _pitchSequence.size(); i++) {
                        subSequence.add(_pitchSequence.get(i));
                    }
                    if (!subSequence.isEmpty()) {
                        _pitchLicks.add(subSequence);
                        // no notion of licks for the durations
                        _pitchSequence.clear();
                    }
                }
            }
        }
        if (!_durationMoMLString.equals("")) {
            //_actorAsString = _completeDurationOracle.exportMoML();
            durationMoML.send(0, new StringToken(_durationMoMLString));
            _durationMoMLString = "";
        }

        if (!_pitchMoMLString.equals("")) {
            //_actorAsString = _completeDurationOracle.exportMoML();
            pitchMoML.send(0, new StringToken(_pitchMoMLString));
            _pitchMoMLString = "";
        }

    }

    /**
     * Reset the factor oracle generator by discarding cached data.
     */
    public void reset() {
        _durationOracles.clear();
        _pitchSequence.clear();
        _durationSequence.clear();
    }

    public void wrapup() throws IllegalActionException {
        try {
            if (_completeDurationOracle != null) {
                _completeDurationOracle.setContainer(null);
                _completeDurationOracle = null;
            }

            if (_completePitchOracle != null) {
                _completePitchOracle.setContainer(null);
                _completePitchOracle = null;
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this);
        }

    }

    private void _constructNewFactorOracle() throws NameDuplicationException,
    IllegalActionException {
        _addPitchFO(_pitchLicks);
        _addDurationFO(_durationSequence);
        _pitchLicks.clear();
        _durationSequence.clear();
    }

    /**
     * Get a list of transitions originating from node
     * @param node Node index
     * @return a List of states which can be reached from current node
     */
    protected List<Integer> _getTransitionsFrom(Integer node) {
        List<Integer> _transitions = (List<Integer>) _adjacencyList.get(node);
        return _transitions;
    }

    private void _addPitchFO(final List<List> _pitchSequences)
            throws IllegalActionException, NameDuplicationException {
        _completePitchOracle = new FactorOracleTop(this.workspace());
        _completePitchOracle.setName(uniqueName("PitchOracle"));
        new ModalTableauFactory(_completePitchOracle, "_tableauFactory");

        List<State> stateList = new LinkedList<State>();
        for (int i = 0; i < _pitchSequences.size(); i++) {
            String foName = ("Lick" + i);
            State s = new State(_completePitchOracle.getController(), (foName));
            // set the first added FO to be the initial state.
            if (i == 0) {
                s.isInitialState.setExpression("true");
                (_completePitchOracle.getController()).initialStateName
                        .setExpression(foName);
            }
            // this will be the refinement of the state
            String refinementName = "m" + (foName);
            // refinements of the Lick states are contained by the OracleModel(ModalModel)
            OracleModel fo = new OracleModel(_completePitchOracle,
                    refinementName, _pitchSequences.get(i).toArray(),
                    _repetitionFactor, true, _usePitch);
            new ModalTableauFactory(fo, "_tableauFactory");
            fo.getController().initialStateName.setExpression("S0");
            s.refinementName.setExpression(refinementName);
            stateList.add(s);
            //set state location
            Double vertical = i * 150.0 + 300.0;
            Double horizontal = 100.0;
            Location l = (Location) s.getAttribute("_location");
            if (l == null) {
                l = new Location(s, "_location");
            }
            l.setExpression("{" + horizontal.toString() + ","
                    + vertical.toString() + "}");
        }

        //construct the transitions between factor oracles
        for (int i = 0; i < stateList.size() - 1; i++) {
            String relationName = "relation_" + i;
            Transition t = new Transition(_completePitchOracle.getController(),
                    relationName);
            (t.exitAngle).setExpression("0.0");

            t.guardExpression.setExpression("probability(" + _repetitionFactor
                    + ")" + "& ( startLick )");
            t.history.setExpression("true");
            stateList.get(i).outgoingPort.link(t);
            stateList.get(i + 1).incomingPort.link(t);
        }

        double creativityProbability = (1.0 - _repetitionFactor)
                / (stateList.size() - 1);
        // do not construct additional connections for the corner case
        if (Math.abs(creativityProbability) > 1E-6) {
            for (int i = 0; i < stateList.size(); i++) {
                for (int j = 0; j < stateList.size(); j++) {
                    if (i == stateList.size() - 1) {
                        creativityProbability = 1.0 / (stateList.size() - 1);
                    }
                    if (i != j && i != j - 1) {
                        String relationName = "relation_" + i + "_" + j;
                        Transition t = new Transition(
                                _completePitchOracle.getController(),
                                relationName);
                        (t.exitAngle).setExpression("0.7");
                        (t.guardExpression).setExpression("probability("
                                + creativityProbability + ")" + "&startLick");
                        t.history.setExpression("true");

                        stateList.get(i).outgoingPort.link(t);
                        stateList.get(j).incomingPort.link(t);

                    }
                }
            }
        }

        // add new port to the modal model
        ModalPort p = (ModalPort) _completePitchOracle.newPort("input");
        p.setInput(true);
        p.createReceivers();
        ModalPort o = (ModalPort) _completePitchOracle.newPort("output");
        o.setOutput(true);
        o.createReceivers();
        _pitchMoMLString = _completePitchOracle.exportMoML();
    }

    private void _addDurationFO(final List<List> _durationSequences)
            throws NameDuplicationException, IllegalActionException {
        _completeDurationOracle = new FactorOracleTop(this.workspace(),
                ((List) _durationSequences).toArray(), _repetitionFactor,
                false, false);
        _completeDurationOracle.setName(uniqueName("DurationOracle"));
        new ModalTableauFactory(_completeDurationOracle, "_tableauFactory");

        String sName = "S0";
        State s = null;
        if (_completeDurationOracle.getController().getEntity(sName) == null) {
            s = new State(_completeDurationOracle.getController(), sName);
        } else {
            s = (State) _completeDurationOracle.getController()
                    .getEntity(sName);
        }
        s.isInitialState.setExpression("true");
        (_completeDurationOracle.getController()).initialStateName
                .setExpression(sName);

        //create input/output ports
        TypedIOPort output = (TypedIOPort) _completeDurationOracle
                .newPort("output");
        output.setTypeEquals(BaseType.DOUBLE);
        (output).setOutput(true);

        output.propagateExistence();
        TypedIOPort input = (TypedIOPort) _completeDurationOracle
                .newPort("input");
        (input).setInput(true);
        input.createReceivers();
        input.propagateExistence();
        input.propagateValue();
        _durationMoMLString = _completeDurationOracle.exportMoML();
    }

    private List _pitchSequence;

    /* The adjacency list given on the Factor Oracle graph structure */
    private HashMap _adjacencyList;

    /* The repetition factor that determines the probability of moving along the original sequence in the FO */
    private double _repetitionFactor;
    private List _durationSequence;
    /** Stores all the pitch FOs that have been generated so far **/
    private List<List> _pitchLicks;
    /** Stores all duration FOs that have been generated so far **/
    private List<FactorOracle> _durationOracles;
    private FactorOracleTop _completeDurationOracle;
    private FactorOracleTop _completePitchOracle;
    private boolean _usePitch;
    private String _durationMoMLString = "";
    private String _pitchMoMLString = "";
}
