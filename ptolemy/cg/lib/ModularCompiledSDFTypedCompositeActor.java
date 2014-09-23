/* A TypedCompositeActor with Lazy evaluation for Modular code generation.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.cg.lib;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.java.modular.ModularSDFCodeGenerator;
import ptolemy.cg.lib.Profile.FiringFunction;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ModularCodeGenTypedCompositeActor

/**
An aggregation of typed actors with lazy evaluation. The contents of
this actor can be created in the usual way via visual editor by dragging
in other actors and ports and connecting them. When it exports a MoML
description of itself, it describes its ports and parameters in the
usual way, but contained actors, relations, and their interconnections
are exported within &lt;configure&gt; &lt;/configure&gt; tags.
When reloading the MoML description, evaluation of the MoML
within the configure tags is deferred until there is an explicit
request for the contents. This behavior is useful for large
complicated models where the time it takes to instantiate the
entire model is large. It permits opening and browsing the model
without a long wait. However, the cost comes typically when
running the model. The instantiation time will be added to
the time it takes to preinitialize the model.
<p>
The lazy contents of this composite are specified via the configure()
method, which is called by the MoML parser and passed MoML code.
The MoML is evaluated lazily; i.e. it is not actually evaluated
until there is a request for its contents, via a call to
getEntity(), numEntities(), entityList(), relationList(),
or any related method. You can also force evaluation
of the MoML by calling populate(). Accessing the attributes
or ports of this composite does not trigger a populate() call,
so a visual editor can interact with the actor from the outside
in the usual way, enabling connections to its ports, editing
of its parameters, and rendering of its custom icon, if any.
<p>
The configure method can be passed a URL or MoML text or both.
If it is given MoML text, that text will normally be wrapped in a
processing instruction, as follows:
<pre>
&lt;?moml
&lt;group&gt;
... <i>MoML elements giving library contents</i> ...
&lt;/group&gt;
?&gt;
</pre>
The processing instruction, which is enclosed in "&lt;?" and "?&gt;"
prevents premature evaluation of the MoML.  The processing instruction
has a <i>target</i>, "moml", which specifies that it contains MoML code.
The keyword "moml" in the processing instruction must
be exactly as above, or the entire processing instruction will
be ignored.  The populate() method
strips off the processing instruction and evaluates the MoML elements.
The group element allows the library contents to be given as a set
of elements (the MoML parser requires that there always be a single
top-level element, which in this case will be the group element).
<p>
One subtlety in using this class arises because of a problem typical
of lazy evaluation.  A number of exceptions may be thrown because of
errors in the MoML code when the MoML code is evaluated.  However,
since that code is evaluated lazily, it is evaluated in a context
where these exceptions are not expected.  There is no completely
clean solution to this problem; our solution is to translate all
exceptions to runtime exceptions in the populate() method.
This method, therefore, violates the condition for using runtime
exceptions in that the condition that causes these exceptions to
be thrown is not a testable precondition.
<p>
A second subtlety involves cloning.  When this class is cloned,
if the configure MoML text has not yet been evaluated, then the clone
is created with the same (unevaluated) MoML text, rather than being
populated with the contents specified by that text.  If the object
is cloned after being populated, the clone will also be populated.
Cloning is used in actor-oriented classes to create subclasses
or instances of a class.  When a LazyTypedCompositeActor contained
by a subclass or an instance is populated, it delegates to the
instance in the class definition. When that instance is populated,
all of the derived instances in subclasses and instances of the
class will also be populated as a side effect.
<p>
A third subtlety is that parameters of this actor cannot refer to
contained entities or relations, nor to attributes contained by
those. This is a rather esoteric use of expressions, so
this limitation may not be onerous. You probably didn't know
you could do that anyway.  An attempt to make such references
will simply result in the expression failing to evaluate.


 @author Dai Bui
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

public class ModularCompiledSDFTypedCompositeActor extends
        ModularCodeGenLazyTypedCompositeActor {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCompiledSDFTypedCompositeActor()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCompiledSDFTypedCompositeActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCompiledSDFTypedCompositeActor(CompositeEntity container,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  TypeAttribute, then it sets the type of the port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == recompileHierarchy) {
            // We will set the recompileHierarchy of all directly contained
            // ModularCodeGenTypedCompositeActors.
            // These will then do the same.
            if (((BooleanToken) recompileHierarchy.getToken()).booleanValue()) {
                List<?> entities = entityList(ModularCompiledSDFTypedCompositeActor.class);
                for (Object entity : entities) {
                    ((ModularCompiledSDFTypedCompositeActor) entity).recompileHierarchy
                            .setToken(new BooleanToken(true));
                }
            }
        } else if (attribute != recompileThisLevel) {
            // We don't support this yet. Enabling results in a recompilation when
            // opening the model since expressions are lazy, and the notification does
            // not happen when you parse the model, but when you read the model.
            //_setRecompileFlag();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Create receivers for each port. If the port is an
     *  input port, then receivers are created for outside
     *  connections. If it is an output port, then receivers
     *  are created for inside connections. This method replaces
     *  any pre-existing receivers, so any data they contain
     *  will be lost.
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {
        if (_modelChanged()) {
            super.createReceivers();
        } else {
            if (workspace().getVersion() != _receiversVersion) {
                Iterator<?> ports = portList().iterator();

                try {
                    workspace().getWriteAccess();
                    while (ports.hasNext()) {
                        IOPort onePort = (IOPort) ports.next();
                        if (onePort.isInput()) {
                            onePort.createReceivers();
                        }
                    }
                    _receiversVersion = workspace().getVersion();
                } finally {
                    // Note that this does not increment the workspace version.
                    // We have not changed the structure of the model.
                    workspace().doneTemporaryWriting();
                }
            }
        }
    }

    /** Get the ports belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    @Override
    public List portList() {
        Profile profile = getProfile();
        if (_USE_PROFILE && profile != null) {
            List<TypedIOPort> ports = new LinkedList<TypedIOPort>(
                    super.portList());
            HashSet<String> portSet = new HashSet<String>();
            for (Object port : ports) {
                portSet.add(((NamedObj) port).getName());
            }
            try {
                for (Profile.Port port : profile.ports()) {
                    if (port.publisher()) {
                        if (!portSet.contains(port.name())) {
                            IOPort newPort = new TypedIOPort(this, port.name());
                            new Parameter(newPort, "_hide", BooleanToken.TRUE);
                            newPort.setInput(port.input());
                            newPort.setOutput(port.output());
                            ports.add(new TypedIOPort(this, port.name()));
                            NamedObj container = getContainer();
                            if (container instanceof CompositeActor) {
                                ((CompositeActor) container)
                                        .registerPublisherPort(
                                                port.getPubSubChannelName(),
                                                newPort);
                            }
                        }
                    }
                }
                return ports;
            } catch (IllegalActionException e) {
                profile = null;
            } catch (NameDuplicationException e) {
                profile = null;
            }
        }
        if (!_USE_PROFILE || profile == null) {
            populate();
            List<?> entities = entityList(ModularCompiledSDFTypedCompositeActor.class);
            for (Object entity : entities) {
                ((ModularCompiledSDFTypedCompositeActor) entity).populate();
            }
        } else {
            System.err.println("Error");
        }
        return super.portList();

    }

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_fireMethod == null) {
            if (_debugging) {
                _debug("ModularCodeGenerator: No generated code. Calling simulation fire method.");
            }
            System.out
                    .println("ModularCodeGenerator: No generated code. Calling simulation fire method.");
            super.fire();
            return;
        }
        try {
            // Invoke the native fire method
            if (_debugging) {
                _debug("ModularCodeGenerator: Calling fire method for generated code.");
            }

            List<Object> argList = new LinkedList<Object>();

            _fire(argList);

            if (_debugging) {
                _debug("ModularCodeGenerator: Done calling fire method of generated code.");
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Could not execute the generated code.");
        }
    }

    /** Create receivers and invoke the
     *  preinitialize() method of the local director. If this actor is
     *  not opaque, throw an exception.  This method also resets
     *  the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.  This method is
     *  read-synchronized on the workspace, so the preinitialize()
     *  method of the director need not be, assuming it is only called
     *  from here.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    @Override
    public void initialize() throws IllegalActionException {
        //        super.initialize(); // TODO only do when not generating code
        try {
            _generatingCode = true;
            _createCodeGenerator();
            if (_modelChanged()) {
                super.initialize();
                _generateCode();
            }
            String className = CodeGeneratorAdapter.generateName(this);
            Class<?> classInstance = null;
            URL url = _codeGenerator.codeDirectory.asFile().toURI().toURL();
            URL[] urls = new URL[] { url };

            URLClassLoader classLoader = null;
            try {
                classLoader = new URLClassLoader(urls);
                classInstance = classLoader.loadClass(className);
            } catch (ClassNotFoundException ex) {
                // We couldn't load the class, maybe the code is not
                // generated (for example the user might have given
                // this model to somebody else. Regenerate it again.
                _generateCode();
                classInstance = classLoader.loadClass(className);
            // java.net.URLClassLoader is not present in Java 1.6.
//             } finally {
//                 if (classLoader != null) {
//                     try {
//                         classLoader.close();
//                     } catch (IOException ex) {
//                         throw new IllegalActionException(this, ex,
//                                 "Failed to close \""
//                                         + (url == null ? "null" : url) + "\".");
//                     }
//                 }
            }

            _objectWrapper = classInstance.newInstance();

            Method[] methods = classInstance.getMethods();
            Method initializeMethod = null;

            for (Method method : methods) {
                String name = method.getName();
                if (name.equals("fire")) {
                    _fireMethod = method;
                }

                if (name.equals("initialize")) {
                    initializeMethod = method;
                }
            }
            if (_fireMethod == null) {
                throw new IllegalActionException(this, "Cannot find fire "
                        + "method in the wrapper class.");
            }

            if (initializeMethod == null) {
                throw new IllegalActionException(this,
                        "Cannot find initialize "
                                + "method in the wrapper class.");
            }

            //initialize the generated object
            initializeMethod.invoke(_objectWrapper, (Object[]) null);
            if (_debugging) {
                _debug("ModularCodeGenerator: Done calling initilize method for generated code.");
            }
            recompileThisLevel.setToken(new BooleanToken(false));
            recompileHierarchy.setToken(new BooleanToken(false));
        } catch (Throwable throwable) {
            System.err.println(throwable);
            _objectWrapper = null;
            _fireMethod = null;
        } finally {
            _generatingCode = false;
        }
    }

    /** Link the subscriberPort with a already registered "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @exception NameDuplicationException If there are name conflicts
     *          as a result of the added relations or ports.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    @Override
    public IOPort linkToPublishedPort(String name, IOPort subscriberPort)
            throws IllegalActionException, NameDuplicationException {
        try {
            ++_creatingPubSub;

            if (_publisherRelations != null
                    && _publisherRelations.containsKey(name)) {
                IOPort port = getPublishedPort(name);

                if (port != null && port.getContainer() == null) {
                    // The user deleted the port.
                    port.setContainer(this);
                    port.liberalLink(_publisherRelations.get(name));
                }

                return super.linkToPublishedPort(name, subscriberPort);
            } else {
                NamedObj container = getContainer();
                if (!isOpaque() && container instanceof CompositeActor) {
                    // Published ports are not propagated if this actor
                    // is opaque.
                    return ((CompositeActor) container).linkToPublishedPort(
                            name, subscriberPort);
                } else if (!(container instanceof CompositeActor)) {
                    throw new IllegalActionException(subscriberPort,
                            "No matching publisher port");
                } else {
                    IOPort stubPort;
                    if (!(_subscriberPorts != null && _subscriberPorts
                            .containsKey(name))) {
                        stubPort = new TypedIOPort(this,
                                uniqueName("subscriberStubPort"));
                        stubPort.setMultiport(true);
                        stubPort.setInput(true);
                        stubPort.setPersistent(false);
                        new Parameter(stubPort, "_hide", BooleanToken.TRUE);

                        if (_subscriberPorts == null) {
                            _subscriberPorts = new HashMap<String, IOPort>();
                        }
                        _subscriberPorts.put(name, stubPort);

                        IORelation relation = new TypedIORelation(this,
                                this.uniqueName("subscriberRelation"));

                        // Prevent the relation and its links from being exported.
                        relation.setPersistent(false);

                        if (_subscriberRelations == null) {
                            _subscriberRelations = new HashMap<String, IORelation>();
                        }

                        _subscriberRelations.put(name, relation);

                        // Prevent the relation from showing up in vergil.
                        new Parameter(relation, "_hide", BooleanToken.TRUE);
                        stubPort.liberalLink(relation);
                        subscriberPort.liberalLink(relation);

                        Director director = getDirector();
                        if (director != null) {
                            director.invalidateSchedule();
                            director.invalidateResolvedTypes();
                        }
                    } else {
                        stubPort = _subscriberPorts.get(name);
                        if (stubPort.getContainer() == null) {
                            // The user deleted the port.
                            stubPort.setContainer(this);
                            stubPort.liberalLink(_subscriberRelations.get(name));
                        }
                    }
                    return ((CompositeActor) container).linkToPublishedPort(
                            name, stubPort);
                }
            }
        } finally {
            --_creatingPubSub;
        }
    }

    /** Create receivers and invoke the
     *  preinitialize() method of the local director. If this actor is
     *  not opaque, throw an exception.  This method also resets
     *  the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.  This method is
     *  read-synchronized on the workspace, so the preinitialize()
     *  method of the director need not be, assuming it is only called
     *  from here.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        Profile profile = getProfile();
        if (!_USE_PROFILE || profile == null || _modelChanged()) {
            //could not find the profile yet
            //do whatever initialization we need
            //code generation is done initialization
            //preinitialization (create schedule) -> type, width inferences -> initialization (code generation)

            //create a director for default preinitialize
            Nameable container = getContainer();
            if (container instanceof Actor) {
                if (getDirector() == ((Actor) container).getDirector()) {
                    try {
                        Director director = new SDFDirector(this,
                                "SDF Director");
                        setDirector(director);

                    } catch (NameDuplicationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        throw new IllegalActionException(this,
                                "Cannot init a new SDF director");
                    }
                }
            }

            //default preinitialize
            super.preinitialize();

            //we could do it our own

            //create the schedule

        } else {
            // Read back the widths, types, rates, ...
            if (!_addedSubscribersFromProfile) {
                _addedSubscribersFromProfile = true;
                if (profile != null) {
                    for (FiringFunction firing : profile.firings()) {
                        System.out.print(firing.firingIndex + ": ");
                        System.out.println(firing.ports);
                    }

                    List ports = new LinkedList(super.portList());
                    HashSet<String> portSet = new HashSet<String>();
                    for (Object port : ports) {
                        portSet.add(((NamedObj) port).getName());
                    }
                    try {
                        for (Profile.Port port : profile.ports()) {
                            for (Object actorPort : ports) {
                                if (port.name().equals(
                                        ((NamedObj) actorPort).getName())) {
                                    DFUtilities
                                            .setRateVariable(
                                                    (IOPort) actorPort,
                                                    port.input() ? "tokenConsumptionRate"
                                                            : "tokenProductionRate",
                                                    port.rate());
                                    ((TypedIOPort) actorPort)
                                            .setTypeEquals(JavaCodeGenerator
                                                    .codeGenTypeToPtType(port
                                                            .type()));
                                    ((IOPort) actorPort).setDefaultWidth(port
                                            .width());
                                    break;
                                }
                            }

                            if (port.subscriber()
                                    && !portSet.contains(port.name())) {
                                IOPort newPort = new TypedIOPort(this,
                                        port.name());
                                new Parameter(newPort, "_hide",
                                        BooleanToken.TRUE);
                                newPort.setInput(port.input());
                                newPort.setOutput(port.output());
                                DFUtilities.setRateVariable(newPort, port
                                        .input() ? "tokenConsumptionRate"
                                        : "tokenProductionRate", port.rate());
                                NamedObj container = getContainer();
                                if (container instanceof CompositeActor) {
                                    ((CompositeActor) container)
                                            .linkToPublishedPort(
                                                    port.getPubSubChannelName(),
                                                    newPort);
                                }
                            }
                        }
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(this, e,
                                "Can't preinitialize.");
                    }

                }
            }

            // FIXME port.getTypeTerm().setValue(resolvedType);
        }
    }

    /** Register a "published port" coming from a publisher. The name
     *  is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param port The published port.
     *  @exception NameDuplicationException If the published port
     *          is already registered.
     *  @exception IllegalActionException If the published port can't
     *          be added.
     */
    @Override
    public void registerPublisherPort(String name, IOPort port)
            throws NameDuplicationException, IllegalActionException {
        try {
            ++_creatingPubSub;
            if (_subscriberPorts != null && _subscriberPorts.containsKey(name)) {
                // The model was run with a subscriber without a publisher. This resulted
                // in a number of stub subscriber ports. These should be cleaned up since
                // we now have a publisher.
                this.unlinkToPublishedPort(name, _subscriberPorts.get(name));
            }

            if (_publishedPorts != null && _publishedPorts.containsKey(name)) {
                throw new NameDuplicationException(this, port,
                        "There is already a published port with name " + name);
            }

            NamedObj container = getContainer();
            if (container != null) {
                IOPort stubPort = new TypedIOPort(this,
                        uniqueName("publisherStubPort"));
                stubPort.setMultiport(true);
                stubPort.setOutput(true);
                stubPort.setPersistent(false);
                new Parameter(stubPort, "_hide", BooleanToken.TRUE);

                IORelation relation = new TypedIORelation(this,
                        uniqueName("publisherRelation"));

                // Prevent the relation and its links from being exported.
                relation.setPersistent(false);
                // Prevent the relation from showing up in vergil.
                new Parameter(relation, "_hide", BooleanToken.TRUE);
                stubPort.liberalLink(relation);
                port.liberalLink(relation);
                if (_publisherRelations == null) {
                    _publisherRelations = new HashMap<String, IORelation>();
                }
                _publisherRelations.put(name, relation);
                if (_publishedPorts == null) {
                    _publishedPorts = new HashMap<String, Set<IOPort>>();
                }
                Set<IOPort> portList = _publishedPorts.get(name);
                if (portList == null) {
                    portList = new LinkedHashSet<IOPort>();
                    _publishedPorts.put(name, portList);
                }
                portList.add(stubPort);

                if (container instanceof CompositeActor) {
                    ((CompositeActor) container).registerPublisherPort(name,
                            stubPort);
                }
            }
        } finally {
            --_creatingPubSub;
        }
    }

    /** Request that execution of the current iteration complete.
     *  do nothing in this case
     */
    @Override
    public void stopFire() {
        if (_debugging) {
            _debug("Called stopFire()");
        }
    }

    /** Unlink the subscriberPort with a already registered "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    @Override
    public void unlinkToPublishedPort(String name, IOPort subscriberPort)
            throws IllegalActionException {
        try {
            ++_creatingPubSub;
            if (_subscriberRelations != null) {
                IORelation relation = _subscriberRelations.get(name);
                if (relation != null) {
                    relation.setContainer(null);
                    _subscriberRelations.remove(name);
                }
            }
            if (_subscriberPorts != null) {
                IOPort port = _subscriberPorts.get(name);
                if (port != null) {
                    port.setContainer(null);
                    NamedObj container = getContainer();
                    if (container instanceof CompositeActor) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                name, port);
                    }
                    _subscriberPorts.remove(name);
                }
            }
            Director director = getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }

            if (_publisherRelations != null
                    && _publisherRelations.containsKey(name)) {
                super.unlinkToPublishedPort(name, subscriberPort);
            } else {
                NamedObj container = getContainer();
                if (!isOpaque() && container instanceof CompositeActor) {
                    // Published ports are not propagated if this actor
                    // is opaque.
                    ((CompositeActor) container).unlinkToPublishedPort(name,
                            subscriberPort);
                }
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e,
                    "Can't unlink the container");
        } finally {
            --_creatingPubSub;
        }
    }

    /** Unregister a "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber. This will be the port
     *          that should be removed
     *  @param publisherPort The publisher port.
     *  @exception IllegalActionException If thrown by the parent method.
     *  @exception NameDuplicationException If thrown by the parent method.
     */
    @Override
    public void unregisterPublisherPort(String name, IOPort publisherPort)
            throws IllegalActionException, NameDuplicationException {
        try {
            ++_creatingPubSub;
            NamedObj container = getContainer();
            if (container != null) {
                if (_publishedPorts != null) {
                    try {
                        getPublishedPort(name).setContainer(null); // Remove stubPort
                    } catch (IllegalActionException e) {
                        // Should not happen.
                        throw new IllegalStateException(e);
                    } catch (NameDuplicationException e) {
                        // Should not happen.
                        throw new IllegalStateException(e);
                    }
                }
                super.unregisterPublisherPort(name, publisherPort);
                if (container instanceof CompositeActor) {
                    ((CompositeActor) container).unregisterPublisherPort(name,
                            publisherPort);
                }
            }
        } finally {
            --_creatingPubSub;
        }
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container.   In this base class wrapup() is called on the
     *  associated actors in the order of their creation.  If the container
     *  is not an instance of CompositeActor, then this method does nothing.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_fireMethod == null) {
            super.wrapup();
        }
    }

    /** Return the profile for the composite actor.
     * @return The profile.
     */
    public Profile getProfile() {
        if (_profile == null) {
            String className = CodeGeneratorAdapter.generateName(this)
                    + "_profile";
            Class<?> classInstance = null;

            NamedObj toplevel = toplevel();
            FileParameter path;
            URL url = null;
            URLClassLoader classLoader = null;
            try {
                path = new FileParameter(toplevel,
                        toplevel.uniqueName("dummyParam"));
                path.setExpression("$HOME/cg/");
                url = path.asFile().toURI().toURL();
                path.setContainer(null); //Remove the parameter again.
                URL[] urls = new URL[] { url };

                classLoader = new URLClassLoader(urls);
                classInstance = classLoader.loadClass(className);
                _profile = (Profile) classInstance.newInstance();

            } catch (Throwable throwable) {
                throw new InternalErrorException(this, throwable,
                        "Failed to get the profile.");
            // java.net.URLClassLoader is not present in Java 1.6.
//             } finally {
//                 if (classLoader != null) {
//                     try {
//                         classLoader.close();
//                     } catch (IOException ex) {
//                         throw new InternalErrorException(this, ex,
//                                 "Failed to close \""
//                                         + (url == null ? "null" : url) + "\".");
//                     }
//                 }
            }
        }

        return _profile;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _createCodeGenerator() throws IllegalActionException,
            NameDuplicationException {
        if (_codeGenerator == null) {
            _codeGenerator = new ModularSDFCodeGenerator(this,
                    "ModularSDFCodeGenerator");
            _codeGenerator.setPersistent(false);
            new Parameter(_codeGenerator, "_hide", BooleanToken.TRUE);
        }
    }

    /** Set up actor parameters.
     */
    private void _init() {
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** Return true if the port is a is connected to a publisher.
     *  @param port The port to look up.
     *  @return Return true if the port is a is connected to a publisher.
     */
    @Override
    protected boolean _isPublishedPort(IOPort port) {
        // FIXME: this method might be slow

        // FindBugs reported "ptolemy.actor.IOPort is incompatible
        // with expected argument type
        // java.util.List<ptolemy.actor.IOPort> in
        // ptolemy.cg.lib.ModularCompiledSDFTypedCompositeActor._isPublishedPort(IOPort)"

        // This is because _publishedPort is declared in
        // CompositeActor to be a Map<String, List<IOPort>>

        //return _publishedPorts != null && _publishedPorts.containsValue(port);
        if (_publishedPorts == null) {
            return false;
        }
        for (String name : _publishedPorts.keySet()) {
            if (_publishedPorts.get(name).contains(port)) {
                return true;
            }
        }
        return false;
    }

    private void _generateCode() throws KernelException {
        _createCodeGenerator();
        _codeGenerator.createProfile();
        //        _codeGenerator.generateCode();

    }

    //     private Profile _getProfile() {
    //         try {
    //             if (_profile != null || _modelChanged()) {
    //                 // if _modelChanged => _profile == null
    //                 return _profile;
    //             } else {
    //                 String className = CodeGeneratorAdapter.generateName(this)
    //                         + "_profile";
    //                 Class<?> classInstance = null;

    //                 NamedObj toplevel = toplevel();
    //                 FileParameter path = new FileParameter(toplevel,
    //                         toplevel.uniqueName("dummyParam"));
    //                 path.setExpression("$HOME/cg/");
    //                 URL url = path.asFile().toURI().toURL();
    //                 path.setContainer(null); //Remove the parameter again.
    //                 URL[] urls = new URL[] { url };

    //                 ClassLoader classLoader = new URLClassLoader(urls);
    //                 classInstance = classLoader.loadClass(className);
    //                 _profile = (Profile) classInstance.newInstance();
    //             }
    //         } catch (Throwable throwable) {
    //             try {
    //                 if (_USE_PROFILE) {
    //                     _setRecompileFlag();
    //                 }
    //                 _profile = null;
    //             } catch (IllegalActionException e1) {
    //                 throw new IllegalStateException(e1);
    //             }
    //         }
    //         return _profile;
    //     }

    private boolean _modelChanged() throws IllegalActionException {
        return ((BooleanToken) recompileThisLevel.getToken()).booleanValue()
                || ((BooleanToken) recompileHierarchy.getToken())
                        .booleanValue();
    }

    /** Return the name of a Publisher or Subscriber channel name.
     *  @param port The port.
     *  @param publisher True if the corresponding Publisher should
     *  be returned.
     *  @param subscriber True if the corresponding Subscriber should
     *  be returned.
     *  @return the name of the channel.
     */
    @Override
    protected String _pubSubChannelName(IOPort port, boolean publisher,
            boolean subscriber) {
        // FIXME: this method might be slow
        if (subscriber) {
            for (Map.Entry<String, IOPort> element : _subscriberPorts
                    .entrySet()) {
                if (element.getValue() == port) {
                    return element.getKey();
                }
            }
        } else if (publisher) {
            for (Entry<String, Set<IOPort>> element : _publishedPorts
                    .entrySet()) {
                if (element.getValue().contains(port)) {
                    return element.getKey();
                }
            }
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ModularSDFCodeGenerator _codeGenerator = null;

    //private boolean _isOpaque = true;

    private long _receiversVersion;

    private Map<String, IORelation> _subscriberRelations;

    static private boolean _USE_PROFILE = true;

}
