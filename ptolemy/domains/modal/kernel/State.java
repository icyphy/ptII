/* A state in an FSMActor.

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.DesignPatternGetMoMLAction;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.ModalRefinement;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DropTargetHandler;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// State

/**
 A State has two ports: one for linking incoming transitions, the other for
 outgoing transitions. When the FSMActor containing a state is the mode
 controller of a modal model, the state can be refined by one or more
 instances of TypedActor. The refinements must have the same container
 as the FSMActor. During execution of a modal model, only the mode
 controller and the refinements of the current state of the mode
 controller react to input to the modal model and produce
 output. The outgoing transitions from a state are either preemptive or
 non-preemptive. When a modal model is fired, if a preemptive transition
 from the current state of the mode controller is chosen, the refinements of
 the current state are not fired. Otherwise the refinements are fired before
 choosing a non-preemptive transition.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Yellow (kienhuis)
 @see Transition
 @see FSMActor
 @see FSMDirector
 */
public class State extends ComponentEntity implements ConfigurableEntity,
DropTargetHandler, Flowable {

    /** Construct a state with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This state will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public State(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        incomingPort = new ComponentPort(this, "incomingPort");
        outgoingPort = new ComponentPort(this, "outgoingPort");
        refinementName = new StringAttribute(this, "refinementName");

        _attachText("_iconDescription", "<svg>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                + "</svg>\n");

        // Specify that the name should be centered in graphical displays.
        SingletonParameter center = new SingletonParameter(this, "_centerName");
        center.setExpression("true");
        center.setVisibility(Settable.EXPERT);

        isInitialState = new Parameter(this, "isInitialState");
        isInitialState.setTypeEquals(BaseType.BOOLEAN);
        isInitialState.setExpression("false");
        // If this is the only state in the container, then make
        // it the initial state.  For backward compatibility,
        // we do not do this if the container has a non-empty
        // value for initialStateName. In that case, we
        // make this the initial state if its name matches that
        // name.
        String initialStateName = "";
        if (container instanceof FSMActor) {
            initialStateName = ((FSMActor) container).initialStateName
                    .getExpression().trim();
            // If the container is an FSMActor, and this is the only
            // state in it, then make the state an initial state.
            // Also, for backward compatibility, if the container
            // has an initialStateName value, and that value matches
            // the name of this state, set the isInitialState parameter.
            if (initialStateName.equals("")) {
                if (container.entityList(State.class).size() == 1) {
                    isInitialState.setExpression("true");
                    // Have to force this to export to MoML, since
                    // the true value will otherwise be seen as the default.
                    isInitialState.setPersistent(true);
                }
            } else {
                // Backward compatibility scenario. The initial state
                // was given by a name in the container.
                if (initialStateName.equals(name)) {
                    isInitialState.setExpression("true");
                    // Have to force this to export to MoML, since
                    // the true value will otherwise be seen as the default.
                    isInitialState.setPersistent(true);
                }
            }
        }
        isFinalState = new Parameter(this, "isFinalState");
        isFinalState.setTypeEquals(BaseType.BOOLEAN);
        isFinalState.setExpression("false");

        saveRefinementsInConfigurer = new Parameter(this,
                "saveRefinementsInConfigurer");
        saveRefinementsInConfigurer.setTypeEquals(BaseType.BOOLEAN);
        saveRefinementsInConfigurer.setVisibility(Settable.EXPERT);
        saveRefinementsInConfigurer.setExpression("false");
        saveRefinementsInConfigurer.setPersistent(false);

        ContainmentExtender containmentExtender = new ContainmentExtender(this,
                "_containmentExtender");
        containmentExtender.setPersistent(false);

        _configurer = new Configurer(workspace());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The port linking incoming transitions.
     */
    public ComponentPort incomingPort = null;

    /** An indicator of whether this state is a final state.
     *  This is a boolean that defaults to false. Setting it to true
     *  will cause the containing FSMActor to return false from its
     *  postfire() method, which indicates to the director that the
     *  FSMActor should not be fired again.
     */
    public Parameter isFinalState;

    /** An indicator of whether this state is the initial state.
     *  This is a boolean that defaults to false, unless this state
     *  is the only one in the container, in which case it defaults
     *  to true. Setting it to true
     *  will cause this parameter to become false for whatever
     *  other state is currently the initial state in the same
     *  container.
     */
    public Parameter isInitialState;

    /** The port linking outgoing transitions.
     */
    public ComponentPort outgoingPort = null;

    /** Attribute specifying one or more names of refinements. The
     *  refinements must be instances of TypedActor and have the same
     *  container as the FSMActor containing this state, otherwise
     *  an exception will be thrown when getRefinement() is called.
     *  Usually, the refinement is a single name. However, if a
     *  comma-separated list of names is provided, then all the specified
     *  refinements will be executed.
     *  This attribute has a null expression or a null string as
     *  expression when the state is not refined.
     */
    public StringAttribute refinementName = null;

    /** A boolean attribute to decide refinements of this state should be
     *  exported as configurations of this state or not.
     */
    public Parameter saveRefinementsInConfigurer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>refinementName</i> attribute, record the change but do
     *  not check whether there is a TypedActor with the specified name
     *  and having the same container as the FSMActor containing this
     *  state.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == refinementName) {
            _refinementVersion = -1;
        } else if (attribute == isInitialState) {
            NamedObj container = getContainer();
            // Container might not be an FSMActor if, for example,
            // the state is in a library.
            if (container instanceof FSMActor) {
                if (((BooleanToken) isInitialState.getToken()).booleanValue()) {
                    // If there is a previous initial state, unset its
                    // isInitialState parameter.
                    if (((FSMActor) container)._initialState != null
                            && ((FSMActor) container)._initialState != this) {
                        ((FSMActor) container)._initialState.isInitialState
                        .setToken("false");
                    }
                    ((FSMActor) container)._initialState = this;
                    // If the initial state name of the container is set,
                    // unset it.
                    String name = ((FSMActor) container).initialStateName
                            .getExpression();
                    if (!name.equals("")) {
                        ((FSMActor) container).initialStateName
                        .setExpression("");
                    }
                }
            }
        }
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *  @param workspace The workspace for the new state.
     *  @return A new state.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        State newObject = (State) super.clone(workspace);
        newObject.incomingPort = (ComponentPort) newObject
                .getPort("incomingPort");
        newObject.outgoingPort = (ComponentPort) newObject
                .getPort("outgoingPort");
        newObject.refinementName = (StringAttribute) newObject
                .getAttribute("refinementName");
        newObject._configurer = new Configurer(newObject.workspace());
        newObject._nonpreemptiveTransitionList = new LinkedList();
        newObject._preemptiveTransitionList = new LinkedList();
        newObject._refinementVersion = -1;
        newObject._transitionListVersion = -1;
        newObject._nonErrorNonTerminationTransitionList = new LinkedList();
        return newObject;
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        refinementName.setExpression("");
        _configureSource = source;
        // Coverity: Avoid a call to configure() in MoMLParser
        // throwing a NPE if text is null.
        if (text != null) {
            text = text.trim();
            if (!text.equals("")) {
                MoMLParser parser = new MoMLParser(workspace());
                _configurer.removeAllEntities();
                parser.setContext(_configurer);
                parser.parse(base, source, new StringReader(text));
                _populateRefinements();
            }
        }
    }

    /** React to a list of objects being dropped onto a target.
     *
     *  @param target The target on which the objects are dropped.
     *  @param dropObjects The list of objects dropped onto the target.
     *  @param moml The moml string generated for the dropped objects.
     *  @exception IllegalActionException If the handling is unsuccessful.
     */
    @Override
    public void dropObject(NamedObj target, List dropObjects, String moml)
            throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof DropTargetHandler) {
            ((DropTargetHandler) container).dropObject(target, dropObjects,
                    moml);
        }
    }

    /** Return the list of outgoing error transitions from
     *  this state.
     *  @return The list of outgoing error transitions from
     *   this state.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    public List errorTransitionList() throws IllegalActionException {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _errorTransitionList;
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    @Override
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    @Override
    public String getConfigureText() {
        return null;
    }

    /** Get the {@link Configurer} object for this entity.
     */
    @Override
    public Configurer getConfigurer() {
        return _configurer;
    }

    /** Return the incoming port.
     *  @return The incoming port.
     */
    @Override
    public ComponentPort getIncomingPort() {
        return incomingPort;
    }

    /** Get a NamedObj with the given name in the refinement of this state, if
     *  any.
     *
     *  @param name The name of the NamedObj.
     *  @return The NamedObj in the refinement, or null if not found.
     *  @exception IllegalActionException If the refinement cannot be found, or
     *   if a comma-separated list is malformed.
     */
    public NamedObj getObjectInRefinement(String name)
            throws IllegalActionException {
        TypedActor[] refinements = getRefinement();
        if (refinements == null) {
            return null;
        }

        for (TypedActor refinement : refinements) {
            if (refinement instanceof NamedObj) {
                Attribute attribute = ((NamedObj) refinement)
                        .getAttribute(name);
                if (attribute != null) {
                    return attribute;
                } else if (refinement instanceof Entity) {
                    Port port = ((Entity) refinement).getPort(name);
                    if (port != null) {
                        return port;
                    } else if (refinement instanceof CompositeEntity) {
                        ComponentEntity entity = ((CompositeEntity) refinement)
                                .getEntity(name);
                        if (entity != null) {
                            return entity;
                        }
                        Relation relation = ((CompositeEntity) refinement)
                                .getRelation(name);
                        if (relation != null) {
                            return relation;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Return the outgoing port.
     *  @return The outgoing port.
     */
    @Override
    public ComponentPort getOutgoingPort() {
        return outgoingPort;
    }

    /** Return the refinements of this state. The names of the refinements
     *  are specified by the <i>refinementName</i> attribute. The refinements
     *  must be instances of TypedActor and have the same container as
     *  the FSMActor containing this state, otherwise an exception is thrown.
     *  This method can also return null if there is no refinement.
     *  This method is read-synchronized on the workspace.
     *  @return The refinements of this state, or null if there are none.
     *  @exception IllegalActionException If the specified refinement
     *   cannot be found, or if a comma-separated list is malformed.
     */
    public TypedActor[] getRefinement() throws IllegalActionException {
        if (_refinementVersion == workspace().getVersion()) {
            return _refinement;
        }

        try {
            workspace().getReadAccess();

            String names = refinementName.getExpression();

            if (names == null || names.trim().equals("")) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            StringTokenizer tokenizer = new StringTokenizer(names, ",");
            int size = tokenizer.countTokens();

            if (size <= 0) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            _refinement = new TypedActor[size];

            Nameable container = getContainer();
            TypedCompositeActor containerContainer = (TypedCompositeActor) container
                    .getContainer();
            int index = 0;

            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();

                if (name.equals("")) {
                    throw new IllegalActionException(this,
                            "Malformed list of refinements: " + names);
                }

                if (containerContainer == null) {
                    // If we are doing saveAs of ModalBSC and select
                    // submodel only, then some of the refinements might
                    // not yet have a container (containercontainer == null).
                    // ptolemy.vergil.modal.StateIcon._getFill() will call
                    // this and properly handles an IllegalActionException
                    throw new IllegalActionException(this, "Container of \""
                            + getFullName()
                            + "\" is null?  This is not always a problem.");
                }

                TypedActor element = (TypedActor) containerContainer
                        .getEntity(name);

                if (element == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "refinement with name \"" + name + "\" in "
                            + containerContainer.getFullName());
                }

                _refinement[index++] = element;
            }

            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the list of outgoing transitions from
     *  this state that are neither error nor termination transitions.
     *  @return A list of outgoing transitions from this state.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    public List nonErrorNonTerminationTransitionList()
            throws IllegalActionException {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _nonErrorNonTerminationTransitionList;
    }

    /** Return the list of non-preemptive outgoing transitions from
     *  this state. This list does not include error transitions
     *  and does include termination transitions.
     *  @return The list of non-preemptive outgoing transitions from
     *   this state.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    public List nonpreemptiveTransitionList() throws IllegalActionException {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }

        return _nonpreemptiveTransitionList;
    }

    /** Return the list of preemptive outgoing transitions from
     *  this state.
     *  @return The list of preemptive outgoing transitions from
     *   this state. This will be an empty list if there aren't any.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    public List preemptiveTransitionList() throws IllegalActionException {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _preemptiveTransitionList;
    }

    /** Return the list of termination transitions from
     *  this state.
     *  @return The list of termination transitions from
     *   this state. This will be an empty list if there aren't any.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    public List terminationTransitionList() throws IllegalActionException {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _terminationTransitionList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this class are the attributes plus the ports.  This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        boolean createConfigurer = false;
        try {
            createConfigurer = ((BooleanToken) saveRefinementsInConfigurer
                    .getToken()).booleanValue();
        } catch (IllegalActionException e) {
            // Ignore. Use false.
        }
        boolean configurePrinted = false;
        if (createConfigurer) {
            try {
                TypedActor[] actors = getRefinement();
                if (actors != null) {
                    for (TypedActor actor : actors) {
                        if (!configurePrinted) {
                            output.write(_getIndentPrefix(depth)
                                    + "<configure>\n");
                            configurePrinted = true;
                        }
                        if (actor instanceof FSMActor) {
                            ((FSMActor) actor).exportSubmodel(output,
                                    depth + 1, actor.getName());
                        } else {
                            ((NamedObj) actor).exportMoML(output, depth + 1);
                        }
                    }
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e,
                        "Unable to save refinements.");
            }
        }
        List<ComponentEntity> actors = _configurer.entityList();
        for (ComponentEntity actor : actors) {
            if (!configurePrinted) {
                output.write(_getIndentPrefix(depth) + "<configure>\n");
                configurePrinted = true;
            }
            if (actor instanceof FSMActor) {
                ((FSMActor) actor).exportSubmodel(output, depth + 1,
                        actor.getName());
            } else {
                ((NamedObj) actor).exportMoML(output, depth + 1);
            }
        }
        if (configurePrinted) {
            output.write(_getIndentPrefix(depth) + "</configure>\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Move the refinements in the configurer of this state to the closest
     *  modal model above this state in the model hierarchy.
     */
    private void _populateRefinements() throws IllegalActionException {
        CompositeEntity container = (CompositeEntity) getContainer();
        CompositeEntity modalModel = (CompositeEntity) container.getContainer();
        boolean isModalModelInvisible = modalModel != null
                && !modalModel.attributeList(InvisibleModalModel.class)
                .isEmpty();
        if (!(modalModel instanceof TypedCompositeActor)
                || isModalModelInvisible) {
            if (modalModel == null || isModalModelInvisible) {
                try {
                    if (modalModel == null) {
                        modalModel = new ModalModel(workspace());
                        new InvisibleModalModel(modalModel,
                                modalModel.uniqueName("_invisibleModalModel"));
                        container.setContainer(modalModel);
                    }
                } catch (NameDuplicationException e) {
                    // This should not happen.
                }
                saveRefinementsInConfigurer.setToken(BooleanToken.TRUE);
            } else {
                return;
            }
        }
        List<ComponentEntity> entities = new LinkedList<ComponentEntity>(
                _configurer.entityList());
        if (container instanceof RefinementActor) {
            RefinementActor actor = (RefinementActor) container;
            for (ComponentEntity entity : entities) {
                String oldName = entity.getName();
                String newName = modalModel.uniqueName(oldName);

                String refinements = refinementName.getExpression();
                String[] names = refinements.split("\\s*,\\s*");
                boolean changed = false;
                StringBuffer newRefinements = new StringBuffer();
                for (String part : names) {
                    if (newRefinements.length() > 0) {
                        newRefinements.append(", ");
                    }
                    if (part.equals(oldName)) {
                        changed = true;
                    } else {
                        newRefinements.append(part);
                    }
                }
                if (changed) {
                    refinementName.setExpression(newRefinements.toString());
                }

                actor.addRefinement(this, newName, null, entity.getClassName(),
                        null);

                String moml = new DesignPatternGetMoMLAction().getMoml(entity,
                        newName);
                try {
                    entity.setContainer(null);
                } catch (NameDuplicationException e) {
                    // Ignore.
                }

                UpdateContentsRequest request = new UpdateContentsRequest(this,
                        modalModel, newName, moml);
                modalModel.requestChange(request);
            }
        }
    }

    /** Update the cached transition lists. This method is read-synchronized on
     *  the workspace.
     *  @exception IllegalActionException If the parameters giving transition
     *   properties cannot be evaluated.
     */
    private void _updateTransitionLists() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            _nonpreemptiveTransitionList.clear();
            _preemptiveTransitionList.clear();
            _errorTransitionList.clear();
            _terminationTransitionList.clear();
            _nonErrorNonTerminationTransitionList.clear();

            // If this state is final, it should not have any outgoing
            // transitions.
            if (((BooleanToken) isFinalState.getToken()).booleanValue()) {
                if (outgoingPort.linkedRelationList().size() > 0) {
                    throw new IllegalActionException(this,
                            "Final state cannot have outgoing transitions");
                }
            }

            Iterator transitions = outgoingPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();

                if (transition.isErrorTransition()) {
                    // An error transition is required to not be preemptive
                    // or termination. Check that here.
                    if (transition.isPreemptive()) {
                        throw new IllegalActionException(transition,
                                "An error transition cannot also be preemptive.");
                    }
                    if (transition.isTermination()) {
                        throw new IllegalActionException(transition,
                                "An error transition cannot also be a termination transition.");
                    }
                    _errorTransitionList.add(transition);
                } else if (transition.isPreemptive()) {
                    // A preemptive transition is not allowed to be a termination transition.
                    if (transition.isTermination()) {
                        throw new IllegalActionException(transition,
                                "A preemptive transition cannot also be a termination transition.");
                    }
                    _preemptiveTransitionList.add(transition);
                    _nonErrorNonTerminationTransitionList.add(transition);
                } else if (transition.isTermination()) {
                    // Termination transitions are allowed to have output actions only
                    // all refinements of this state are state machine refinements.
                    TypedActor[] refinements = getRefinement();
                    if (refinements == null || refinements.length == 0) {
                        throw new IllegalActionException(transition,
                                "Termination transitions must come from states with refinements");
                    }
                    // There are refinements.
                    // Check that if there are output actions on the termination transition
                    // then all refinements are FSM refinements. This is because non-FSM
                    // refinements can only be known to have terminated in postfire, and that
                    // is too late to produce outputs for some domains (SR and Continuous, at least).
                    if (!transition.outputActions.getExpression().trim()
                            .equals("")) {
                        for (Actor refinementActor : refinements) {
                            if (!(refinementActor instanceof ModalRefinement)) {
                                throw new IllegalActionException(
                                        transition,
                                        "Termination transition cannot have output actions because "
                                                + "such a transition is taken in the postfire phase of execution.");
                            }
                        }
                    }
                    // Note that a transition does not appear on this list unless it is
                    // NOT a preemptive or error transition.
                    _terminationTransitionList.add(transition);
                    _nonpreemptiveTransitionList.add(transition);
                } else {
                    _nonpreemptiveTransitionList.add(transition);
                    _nonErrorNonTerminationTransitionList.add(transition);
                }
            }

            _transitionListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    // The source of the configuration, which is not used.
    private String _configureSource;

    // The Configurer object for this state.
    private Configurer _configurer;

    // Cached list of error transitions from this state
    private List _errorTransitionList = new LinkedList();

    // Cached list of outgoing transitions from this state that are
    // neither error nor termination transitions.
    private List _nonErrorNonTerminationTransitionList = new LinkedList();

    // Cached list of non-preemptive outgoing transitions from this state.
    private List _nonpreemptiveTransitionList = new LinkedList();

    // Cached list of preemptive outgoing transitions from this state.
    private List _preemptiveTransitionList = new LinkedList();

    // Cached reference to the refinement of this state.
    private TypedActor[] _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // Cached list of termination transitions from this state.
    private List _terminationTransitionList = new LinkedList();

    // Version of cached transition lists.
    private long _transitionListVersion = -1;

    ///////////////////////////////////////////////////////////////////
    //// InvisibleModalModel

    /**
     An attribute that marks a modal model is created because the designer opens
     a design pattern whose top-level is an FSMActor. In that case, a modal
     model is automatically created to contain the FSMActor, and this attribute
     is associated with the created (invisible) modal model.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class InvisibleModalModel extends SingletonAttribute {

        /** Construct an attribute with the given container and name.
         *  If an attribute already exists with the same name as the one
         *  specified here, that is an instance of class
         *  SingletonAttribute (or a derived class), then that
         *  attribute is removed before this one is inserted in the container.
         *  @param container The container.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the attribute cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the container already has an
         *   attribute with this name, and the class of that container is not
         *   SingletonAttribute.
         */
        InvisibleModalModel(CompositeEntity container, String name)
                throws NameDuplicationException, IllegalActionException {
            super(container, name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// UpdateContentsRequest

    /**
     A change request the updates the refinements of a state if it contains a
     configure element.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class UpdateContentsRequest extends ChangeRequest {

        /** Construct a request.
         *
         *  @param source The state that originates the request.
         *  @param modalModel The closest modal model that the source state is
         *   contained in.
         *  @param name The name of the refinement.
         *  @param moml The moml of the refinement.
         */
        public UpdateContentsRequest(State source, CompositeEntity modalModel,
                String name, String moml) {
            super(source, "Update contents of refinement " + name + ".");
            _modalModel = modalModel;
            _name = name;
            _moml = moml;
        }

        /** Execute the change.
         *  @exception Exception If the change fails.
         */
        @Override
        protected void _execute() throws Exception {
            ComponentEntity entity = _modalModel.getEntity(_name);
            MoMLChangeRequest request = new MoMLChangeRequest(this, entity,
                    _moml);
            request.execute();
        }

        // The name of the refinement.
        private String _name;

        // The closest modal model that the source state is ontained in.
        private CompositeEntity _modalModel;

        // The moml of the refinement.
        private String _moml;
    }
}
