/* Build a factor oracle data structure that represents a finite acyclic
automaton that contains at least all the suffixes of a given input
sequence.

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Initializable;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Function;
import ptolemy.data.FunctionToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Build a factor oracle (FO) data structure that represents a finite acyclic
 * automaton that contains at least all the suffixes of a given input
 * sequence. The actor accepts a <i>name</i> for the Factor oracle, an
 * Object array, representing the input <i> trainingSequence </i> on which
 * the suffix tree will be built, and a <i>repetitionFactor</i> that is a
 * double in range [0.0,1.0], which is a measure of the probability of
 * the training sequence to be repeated at each transition. The output Factor
 * Oracle will be a probabilistic automaton that can generate at least all the
 * suffixes of the training string. If the <i> validatePitch </i> Parameter
 * is set to true, the factor oracle interprets the input string as a sequence
 * of notes and adds a check to the transitions to validate the note against
 * a specification.
 *
 <p> As an example, if a transition that would generate a "C4" upon firing, the
 guard expression would look like: <i> validatePitch("C4") &amp;&amp; probability(p) </i>,
 where p is a double in range [0.0,1.0]. This transition would be taken
 (i) if the probability() transition evaluates to true AND (ii) if
 * validatePitch() returns true.
 *
 <p>
 <b>References</b>
 <p>[1]
 C. Allauzen, M. Crochemore, and M. Raffinot. "Factor oracle: A new structure for pattern matching."
 <i>SOFSEM’99: Theory and Practice of Informatics </i>. Springer Berlin Heidelberg, 1999.

 @author Ilge Akkaya
 @version  $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class FactorOracle extends ModalController {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @param trainingSequence The input string that the oracle is built from
     *  @param repetitionFactor a double indicating the factor repetition probability
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FactorOracle(CompositeEntity container, String name,
            Object[] trainingSequence, double repetitionFactor)
            throws NameDuplicationException, IllegalActionException {
        this(container, name, trainingSequence, repetitionFactor, false, false);
    }

    /**
     * Constructs a FactorOracle object.
     *
     * @param container  The container
     * @param name       The name
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public FactorOracle(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        this(container, name, null, 1.0);
    }

    /**
     * Constructs a FactorOracle object.
     *
     * @param container         The Container
     * @param name              The name
     * @param trainingSequence  An object array containing the training sequence
     * @param repetitionFactor  a double indicating the factor repetition probability
     * @param symbolicOutput    a boolean that determines whether symbolic outputs should be produced
     * @param validateSymbols   a boolean -- true if symbol validation should be included in guard expressions
     * @exception IllegalActionException repetition factor range checking
     * @exception NameDuplicationException
     */
    public FactorOracle(CompositeEntity container, String name,
            Object[] trainingSequence, double repetitionFactor,
            boolean symbolicOutput, boolean validateSymbols)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("org.ptolemy.machineImprovisation.FactorOracle");

        if (repetitionFactor > 1.0 || repetitionFactor < 0.0) {
            throw new IllegalActionException(this,
                    "Repetition factor must be in range [0.0,1.0].");
        }

        _adjacencyList = new HashMap<Integer, List<Integer>>();
        _adjacencyListSymbols = new HashMap<Integer, List<Integer>>();

        _suffixLinks = new HashMap();
        _alphabet = new HashSet();
        _sequenceLength = 0;

        _repetitionFactor = repetitionFactor;

        if (trainingSequence != null) {
            _inputSequence = trainingSequence;
            _sequenceLength = _inputSequence.length;
        } else {
            _sequenceLength = 0;
            _inputSequence = new Object[1];
        }
        _symbolic = symbolicOutput;

        _validatePitch = validateSymbols;

        _learnFactorOracle();
        _buildFactorOracle();

        // this is for the inner class that checks validity of pitch in the current chord progression
        validatePitch = new Parameter(this, "validatePitch");
        validatePitch.setToken(new ChordFunctionToken());
        validatePitch.setVisibility(Settable.EXPERT);
        validatePitch.setPersistent(false);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The repetition probability P(moving along the original sequence rather than taking a jump along
     * a suffix link)*/
    public Parameter repetitionFactor;

    /**
     * Boolean that when set to true, enables the transitions to have a condition that validates each
     * pitch against a specification.
     */
    public Parameter validatePitch;

    /**
     * Current chord in improvisation
     */
    public TypedIOPort currentChord;

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {

        List<Initializable> oldInitializables = _initializables;
        _initializables = null;
        FactorOracle newObject = (FactorOracle) super.clone(workspace);
        _initializables = oldInitializables;
        // If the next line is uncommented, the InitializationBug.xml fails.
        // newObject._initializables = null;

        try {
            newObject.validatePitch
                    .setToken(newObject.new ChordFunctionToken());
        } catch (IllegalActionException e) {
            // Should not occur, because it didn't occur in the object being cloned.
            throw new CloneNotSupportedException(e.getMessage());
        }
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    private void _buildFactorOracle() throws NameDuplicationException,
            IllegalActionException {

        // create factor oracle transitions including the suffix links.
        _stateList = new HashMap();
        if (_adjacencyList.size() > 0) {
            for (int i = 0; i <= _adjacencyList.size(); i++) {
                _createNewState(i); //add state to the FO
            }
            _setTransitions();
        }
    }

    private void _createNewState(final int i) throws IllegalActionException,
            NameDuplicationException {
        final Double horizontal = i * HORIZONTAL_SPACING_PIXELS;
        final Double vertical = i * VERTICAL_SPACING_PIXELS;
        try {
            String name = (STATE_PREFIX + i);
            State s = new State(FactorOracle.this, name);
            // set location
            Location stateLocation = (Location) s.getAttribute("_location");
            if (stateLocation == null) {
                stateLocation = new Location(s, "_location");
            }
            stateLocation.setExpression("{" + horizontal.toString() + ","
                    + vertical.toString() + "}");

            if (i == 0) {
                s.isInitialState.setExpression("true");
                this.initialStateName.setExpression(s.getName());
            }
            _stateList.put(i, s);

        } catch (IllegalActionException e) {
            throw new IllegalActionException(this);
        } catch (NameDuplicationException e) {
            throw new NameDuplicationException(this,
                    "Element with name already exists in Factor Oracle");
        }
    }

    private void _learnFactorOracle() {

        for (int i = 0; i < _sequenceLength; i++) {
            Object p = _inputSequence[i];
            _alphabet.add(p);

            // add original transitions to graph
            List initialEdge = new LinkedList<Integer>();
            initialEdge.add(i + 1);
            _adjacencyList.put(i, initialEdge);

            List initialSymbol = new LinkedList<Character>();
            initialSymbol.add(_inputSequence[i]);
            _adjacencyListSymbols.put(i, initialSymbol);
        }

        // by definition, the suffix link from state zero is the bottom element (represented as -1)
        _suffixLinks.put(0, -1);

        for (int i = 1; i <= _sequenceLength; i++) {

            // already created the original links
            int l = (Integer) _suffixLinks.get(i - 1);
            // while previous node DOES exist and there is no w[i]-son of state l...
            Object wiSon = _inputSequence[i - 1];
            while (l != -1
                    && ((List) _adjacencyListSymbols.get(l)).contains(wiSon) == false) {
                List prevList = _getTransitionsFrom(l);
                prevList.add(i);
                List prevSymbols = (List<Character>) _adjacencyListSymbols
                        .get(l);
                prevSymbols.add(wiSon);
                // update adjacency list
                _adjacencyList.put(l, prevList);
                _adjacencyListSymbols.put(l, prevSymbols);
                l = (Integer) _suffixLinks.get(l);
            }
            if (l == -1) {
                _suffixLinks.put(i, 0);
            } else {
                Integer wiSonIndex = ((List) _adjacencyListSymbols.get(l))
                        .indexOf(wiSon);
                Integer wiSonValue = _getTransitionsFrom(l).get(wiSonIndex);
                _suffixLinks.put(i, wiSonValue);
            }
        }
    }

    private void _setTransitions() throws IllegalActionException,
            NameDuplicationException {

        String exitAngle;
        String outputExpression;

        for (int i = 0; i < _adjacencyList.size(); i++) {
            List destinations = (List) _adjacencyList.get(i);
            int nTransitions = destinations.size();
            // divide probability amongst all transitions from this state
            // if there is a suffix from this state to another, the destination will be >=0 ( -1 is reserved
            // for bottom)
            int hasSuffix = (Integer) _suffixLinks.get(i);
            int suffixCount = hasSuffix >= 0 ? 1 : 0;
            double precisionFactor = 1E12;

            for (int k = 0; k < nTransitions; k++) {
                // the destination node for this transition
                int j = (Integer) destinations.get(k);

                double _probability;
                if (i == j - 1) {
                    // if this is the ONLY transition enabled from this state, then the probability to the next has to be 1.
                    if (destinations.size() == 1 && suffixCount == 0) {
                        _probability = 1.0;
                    } else {
                        _probability = _repetitionFactor;
                    }
                    exitAngle = STRAIGHT_EXIT_ANGLE;
                } else {
                    // divide the improvisation probability amongst the other transitions
                    int numberOfBranches = nTransitions - (1 - suffixCount);
                    _probability = (1.0 - _repetitionFactor) / numberOfBranches;
                    // lose the higher digits to avoid overflow probability
                    _probability = (Math.round(_probability * precisionFactor
                            - 1))
                            / precisionFactor;
                    exitAngle = SKIP_EXIT_ANGLE;
                }
                //FIXME
                if (_probability > 0.0) {
                    String transitionProbabilityExpression = "probability("
                            + _probability + ")";

                    String relationName = "relation_" + i + j; //this will be unique. i:source state, j:destination state
                    // label the original string transitions with the repetition factor

                    String outputChar = " ";
                    // get the symbol to be produced, when this transition is taken
                    outputChar = ((List) (_adjacencyListSymbols.get(i))).get(k)
                            .toString();

                    // if chord progression specification exists, a check will be added in conjunction with the guard expression
                    String pitchValidationExpression = "validatePitch(\""
                            + outputChar + "\", input)";
                    // set the output expression for this transition

                    if (outputChar != null) {
                        if (_symbolic) {
                            outputExpression = "output = \""
                                    + outputChar.toString() + "\"";
                        } else {
                            outputExpression = "output = "
                                    + outputChar.toString();
                        }
                    } else {
                        outputExpression = "";
                    }

                    Transition t = new Transition(FactorOracle.this,
                            relationName);
                    (t.exitAngle).setExpression(exitAngle);
                    (t.outputActions).setExpression(outputExpression);
                    if (_validatePitch) {
                        (t.guardExpression)
                                .setExpression(transitionProbabilityExpression
                                        + "&" + pitchValidationExpression);
                    } else {
                        (t.guardExpression)
                                .setExpression(transitionProbabilityExpression);
                    }
                    ((State) _stateList.get(i)).outgoingPort.link(t);
                    ((State) _stateList.get(j)).incomingPort.link(t);
                }
            }
        }
        exitAngle = SUFFIX_EXIT_ANGLE;
        for (int i = 0; i < _suffixLinks.size(); i++) {
            int destination = (Integer) _suffixLinks.get(i);
            String relationName = "relation" + i + destination;
            if (destination >= 0) {
                Transition t = new Transition(FactorOracle.this, relationName);
                (t.exitAngle).setExpression(exitAngle);
                (t.defaultTransition).setExpression("true");
                (t.guardExpression).setExpression("input_isPresent");
                //(t.immediate).setExpression("true");
                ((State) _stateList.get(i)).outgoingPort.link(t);
                ((State) _stateList.get(destination)).incomingPort.link(t);
            }
        }
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

    /**
     * Input Sequence
     */
    private Object[] _inputSequence;
    /** The adjacency list given on the Factor Oracle graph structure */
    private HashMap _adjacencyList;

    /** The symbol map given on the Factor Oracle graph structure */
    private HashMap _adjacencyListSymbols;

    /** A map of all suffix links */
    private HashMap _suffixLinks;

    /** The input alphabet of the probabilistic automaton */
    private Set _alphabet;
    /**
     * A boolean that when true, symbols should be interpreted as symbolic inputs
     */
    private boolean _symbolic;

    /** Input string sequence length
     */
    private int _sequenceLength;
    /**
     * Repetition probability of the original string
     */
    private double _repetitionFactor;
    /**
     * List of states in the FO
     */
    private HashMap _stateList;

    /*
     * Boolean that when set, adds a validation check to each transition
     */
    private boolean _validatePitch = false;
    /**
     * Vertical spacing of states in the Factor Oracle in pixels
     */
    private static final double VERTICAL_SPACING_PIXELS = 0.0;
    /**
     * Horizontal spacing of states in the Factor Oracle in pixels
     */
    private static final double HORIZONTAL_SPACING_PIXELS = 150.0;

    private static final String STATE_PREFIX = "S";

    private static final String SUFFIX_EXIT_ANGLE = "-0.6";

    private static final String SKIP_EXIT_ANGLE = "0.7";

    private static final String STRAIGHT_EXIT_ANGLE = "0.0";

    private static Type[] _CHORD_FUNCTION_ARG_TYPE = { BaseType.STRING,
            BaseType.STRING };

    /**
     * An inner class that defines a Chord Function Token,  which is used in
     * pitch validation.
     * @author ilgea
     */
    protected class ChordFunctionToken extends FunctionToken {
        public ChordFunctionToken() {
            super(new ChordFunction(), new FunctionType(
                    _CHORD_FUNCTION_ARG_TYPE, BaseType.BOOLEAN));
        }
    }

    /** A function that evaluates to true if the "pitch" that would be produced
     * as a result of taking this transition satisfies chord specifications. In
     * the current application, the specifications are given by
     * {@link org.ptolemy.machineImprovisation.MusicSpecs#getChordPitches(String, boolean)}.
     */
    protected class ChordFunction implements Function {
        @Override
        public Token apply(Token[] arguments) throws IllegalActionException {

            if (arguments == null
                    || arguments.length != this.getNumberOfArguments()) {
                throw new IllegalArgumentException("Invalid arguments.");
            }
            if (arguments[0] instanceof StringToken
                    && arguments[1] instanceof StringToken) {
                String noteToTest = ((StringToken) arguments[0]).stringValue();
                String pureNote = noteToTest.substring(0,
                        noteToTest.length() - 1);
                String chordName = ((StringToken) arguments[1]).stringValue();
                List chordTones = MusicSpecs.getChordPitches(chordName, true);

                if (chordTones != null) {
                    System.out.println("Chord: " + chordName + " Tones:"
                            + chordTones.toString() + " Note being tested: "
                            + noteToTest);
                }

                if (chordTones.contains(pureNote)) {
                    System.out.println("Accepted Note: " + noteToTest);
                    //okay to play
                    return BooleanToken.TRUE;
                }
            }
            return BooleanToken.FALSE;
        }

        @Override
        public int getNumberOfArguments() {
            // TODO Auto-generated method stub
            return 2;
        }

        @Override
        public boolean isCongruent(Function function) {
            // TODO Auto-generated method stub
            return function instanceof ChordFunction;
        }

        public String toString() {
            return "function(t:string, s:string) boolean";
        }

        protected class ChordFunctionToken extends FunctionToken {
            public ChordFunctionToken() {
                super(new ChordFunction(), new FunctionType(
                        _CHORD_FUNCTION_ARG_TYPE, BaseType.BOOLEAN));
            }
        }

    }

}
