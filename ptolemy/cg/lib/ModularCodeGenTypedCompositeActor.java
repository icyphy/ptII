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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.java.modular.ModularCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ModularCodeGenTypedCompositeActor

/**
A TypedCompositeActor with Lazy evaluation for Modular code generation.

 @author Bert Rodiers, Dai Bui
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class ModularCodeGenTypedCompositeActor extends
        ModularCodeGenLazyTypedCompositeActor {

    /** Construct a library in the default workspace with no container
     *  and an empty string as its name. Add the library to the
     *  workspace directory.  Increment the version number of the
     *  workspace.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCodeGenTypedCompositeActor() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a library in the specified workspace with no
     *  container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null,
     *  then use the default workspace. Add the actor to the workspace
     *  directory.  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCodeGenTypedCompositeActor(Workspace workspace)
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
    public ModularCodeGenTypedCompositeActor(CompositeEntity container,
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
            // FIXME: this breaks lazyness.  A better
            // solution would be to look up the tree for a parent
            if (((BooleanToken) recompileHierarchy.getToken()).booleanValue()) {
                List<?> entities = entityList(ModularCodeGenTypedCompositeActor.class);
                for (Object entity : entities) {
                    ((ModularCodeGenTypedCompositeActor) entity).recompileHierarchy
                    .setToken(new BooleanToken(true));
                }
            }
            //        } else if (attribute == recompileThisLevel) {
            //            if (((BooleanToken) recompileThisLevel.getToken()).booleanValue()) {
            //                populate();
            //            }
        } else if (attribute != recompileThisLevel) {
            // We don't support this yet. Enabling results in a recompilation when
            // opening the model since expressions are lazy, and the notification does
            // not happen when you parse the model, but when you read the model.
            //             _setRecompileFlag();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Get the ports belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    @Override
    public List portList() {
        Profile profile = _getProfile();
        if (_USE_PROFILE && profile != null) {
            try {
                for (Profile.Port port : profile.ports()) {
                    if (port.publisher()) {
                        //                        if (!portSet.contains(port.name())) {
                        IOPort newPort = (IOPort) getPort(port.name());
                        if (newPort == null) {
                            newPort = (IOPort) newPort(port.name());
                            new Parameter(newPort, "_hide", BooleanToken.TRUE);
                            newPort.setInput(port.input());
                            newPort.setOutput(port.output());
                            newPort.setMultiport(port.multiport());
                        }

                        NamedObj container = getContainer();
                        if (container instanceof CompositeActor) {
                            ((CompositeActor) container).registerPublisherPort(
                                    port.getPubSubChannelName(), newPort);
                        }
                        //                        }
                    }
                }
                return super.portList();
            } catch (IllegalActionException e) {
                profile = null;
                List<?> entities = entityList(ModularCodeGenTypedCompositeActor.class);
                for (Object entity : entities) {
                    ((ModularCodeGenTypedCompositeActor) entity).populate();
                }
            } catch (NameDuplicationException e) {
                profile = null;
            }
        }

        if (!_USE_PROFILE || profile == null) {
            populate();
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

            if (outputPortList().size() > 0) {
                argList.add(true);
            }

            _fire(argList);

        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Could not execute the generated code.");
        }
    }

    /** Create receivers and invoke the preinitialize() method of the
     *  local director. If this actor is not opaque, throw an
     *  exception.  This method also resets the protected variable
     *  _stopRequested to false, so if a derived class overrides this
     *  method, then it should also do that.  This method is
     *  read-synchronized on the workspace, so the preinitialize()
     *  method of the director need not be, assuming it is only called
     *  from here.
     *
     *  @exception IllegalActionException If there is no director, or
     *   if the director's preinitialize() method throws it, or if
     *   this actor is not opaque.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize(); // TODO only do when not generating code
        try {
            _generatingCode = true;
            if (_modelChanged()) {
                super.preinitialize(); //TODO optimize this for hierarchy
                executeChangeRequests();
                _generateCode();
            }
            String className = CodeGeneratorAdapter.generateName(this);
            if (_codeGenerator == null) {
                // This is needed for exporting to HTML.
                _createCodeGenerator();
            }
            URL url = _codeGenerator.codeDirectory.asFile().toURI().toURL();
            // FIXME: generateInSubdirectory fix
            if (((BooleanToken) _codeGenerator.generateInSubdirectory
                    .getToken()).booleanValue()) {
                className = className + "." + className;
                url = _codeGenerator.codeDirectory.asFile().getParentFile()
                        .toURI().toURL();
            }

            if (!url.getPath().endsWith("/")) {
                // URLClassLoader needs to end in a /, otherwise the
                // URL is assumed to be a jar file.
                url = new URL(url.toString() + "/");
            }
            URL[] urls = new URL[] { url };
            URLClassLoader classLoader = null;
            Class<?> classInstance = null;
            try {
                try {
                    classLoader = new URLClassLoader(urls);
                    classInstance = classLoader.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    // We couldn't load the class, maybe the code is not
                    // generated (for example the user might have given
                    // this model to somebody else. Regenerate it again.
                    _generateCode();
                    try {
                        classInstance = classLoader.loadClass(className);
                    } catch (ClassNotFoundException ex2) {
                        ex2.printStackTrace();
                        throw new ClassNotFoundException("Failed to load "
                                + className
                                + " using URLClassLoader based on "
                                + url
                                + ", urls were: "
                                + java.util.Arrays.deepToString(classLoader
                                        .getURLs()) + "\n" + ex2);
                    }
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

                //java.net.URLClassLoader is not present in Java 1.6.
            } finally {
                if (classLoader != null) {
                    try {
                        classLoader.close();
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed to close \""
                                        + (url == null ? "null" : url) + "\".");
                    }
                }
            }
            recompileThisLevel.setToken(new BooleanToken(false));
            recompileHierarchy.setToken(new BooleanToken(false));

            _compiled = true;
        } catch (Throwable throwable) {
            _objectWrapper = null;
            _fireMethod = null;
            throw new IllegalActionException(this, throwable,
                    "Failed to initialize.");
        } finally {
            _generatingCode = false;
        }
    }

    /** Create receivers and invoke the preinitialize() method of the
     *  local director. If this actor is not opaque, throw an
     *  exception.  This method also resets the protected variable
     *  _stopRequested to false, so if a derived class overrides this
     *  method, then it should also do that.  This method is
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
        Profile profile = _getProfile();
        if (!_USE_PROFILE || profile == null || _modelChanged()) {
            super.preinitialize();
        } else {
            // Read back the widths, types, rates, ...
            if (!_addedSubscribersFromProfile) {
                _addedSubscribersFromProfile = true;
                if (profile != null) {
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
        try {
            _createCodeGenerator();
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
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
        if (_compiled) {
            // Do this last so that we _modelChanged() can
            // traverse the hierarchy.
            _compiled = false;
            recompileThisLevel.setToken(new BooleanToken(false));
            recompileHierarchy.setToken(new BooleanToken(false));
        }
        if (_fireMethod == null) {
            super.wrapup();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _createCodeGenerator() throws IllegalActionException,
    NameDuplicationException {
        if (_codeGenerator == null) {
            _codeGenerator = new ModularCodeGenerator(this,
                    "ModularCodeGenerator");
            _codeGenerator.setPersistent(false);
            new Parameter(_codeGenerator, "_hide", BooleanToken.TRUE);
        }
    }

    /** Set up actor parameters.
     *  @exception IllegalActionException If a parameter cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the actor already has a
     *   parameter with this name.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.cg.lib.ModularCodeGenTypedCompositeActor");
    }

    /** Return true if the port is a is connected to a publisher.
     *  @param port The port to look up.
     *  @return Return true if the port is a is connected to a publisher.
     */
    @Override
    protected boolean _isPublishedPort(IOPort port) {
        // FIXME: this method might be slow.
        boolean isPublishPort = false;

        // Published ports are in stored in the immediate opaque parent's composite.
        NamedObj container = getContainer();
        while (container instanceof CompositeActor
                && !((CompositeActor) container).isOpaque()) {
            container = ((CompositeActor) container).getContainer();
        }

        if (container instanceof CompositeActor) {
            isPublishPort = ((CompositeActor) container).isPublishedPort(port);
        }

        return isPublishPort;
    }

    /** Generate code and create the profile.
     */
    private void _generateCode() throws KernelException {
        _createCodeGenerator();
        _codeGenerator.createProfile();
        int returnValue = 0;
        if ((returnValue = _codeGenerator.generateCode()) != 0) {
            throw new KernelException(this, null, "Failed to generate code, "
                    + "the return value of the last subprocess was "
                    + returnValue);
        }
    }

    /** Return the profile.
     *  The profile is a Java class that is the sanitized name of
     *  the name of this actor, followed by "_profile".  The Java
     *  class is searched for in $HOME/cg.  If the profile class
     *  is not found, and _USE_PROFILE is true, then recompilation
     *  will occur.
     *  @return The profile, if it is found, otherwise, return null.
     */
    private Profile _getProfile() {
        try {
            if (_profile != null || _modelChanged()) {
                // if _modelChanged => _profile == null
                return _profile;
            } else {
                String className = CodeGeneratorAdapter.generateName(this)
                        + "_profile";
                Class<?> classInstance = null;

                NamedObj toplevel = toplevel();
                FileParameter path = new FileParameter(toplevel,
                        toplevel.uniqueName("dummyParam"));
                path.setExpression("$HOME/cg/");
                URL url = path.asFile().toURI().toURL();
                path.setContainer(null); //Remove the parameter again.
                URL[] urls = new URL[] { url };

                URLClassLoader classLoader = null;
                try {
                    classLoader = new URLClassLoader(urls);
                    classInstance = classLoader.loadClass(className);
                    _profile = (Profile) classInstance.newInstance();
                    portList();
                    // java.net.URLClassLoader is not present in Java 1.6.
                } finally {
                    if (classLoader != null) {
                        try {
                            classLoader.close();
                        } catch (IOException ex) {
                            throw new IllegalActionException(this, ex,
                                    "Failed to close \""
                                            + (url == null ? "null" : url)
                                            + "\".");
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            try {
                if (_USE_PROFILE) {
                    _setRecompileFlag();
                }
                _profile = null;
            } catch (IllegalActionException e1) {
                throw new IllegalStateException(e1);
            }
        }
        return _profile;
    }

    /** Return true if the model has changed.
     *  @return true if the <i>recompileThisLevel</i> or
     *  <i>recompileHierarchy</i> parameters are true or if
     *  the <i>recompileHierarchy</i> parameter is true in any
     *  of the parent ModularCodeGenTypedCompositeActors.
     */
    private boolean _modelChanged() throws IllegalActionException {
        if (((BooleanToken) recompileThisLevel.getToken()).booleanValue()
                || ((BooleanToken) recompileHierarchy.getToken())
                .booleanValue()) {
            return true;
        }

        NamedObj container = getContainer();
        while (container != null) {
            if (container instanceof ModularCodeGenTypedCompositeActor) {
                if (((BooleanToken) ((ModularCodeGenTypedCompositeActor) container).recompileHierarchy
                        .getToken()).booleanValue()) {
                    return true;
                }
            }
            container = container.getContainer();
        }
        return false;
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
            NamedObj container = getContainer();
            while (container instanceof CompositeActor
                    && !((CompositeActor) container).isOpaque()) {
                container = ((CompositeActor) container).getContainer();
            }

            return ((CompositeActor) container).getPublishedPortChannel(port);
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if initialized() compiled this actor. */
    private boolean _compiled;

    private ModularCodeGenerator _codeGenerator = null;

    /** If true, then use the
     *  {@link ptolemy.cg.lib.Profile}, which contains
     *  meta information such as information about
     *  the ports. The default value of this
     *  variable is true.
     */
    static private final boolean _USE_PROFILE = true;

}
