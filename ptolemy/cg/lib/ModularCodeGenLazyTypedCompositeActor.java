/* A TypedCompositeActor with Lazy evaluation for Modular code generation.

 Copyright (c) 2009-2013 The Regents of the University of California.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
import ptolemy.actor.LazyTypedCompositeActor;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.java.modular.ModularSDFCodeGenerator;
import ptolemy.cg.lib.Profile.FiringFunction;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ModularCodeGenLazyTypedCompositeActor

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
<group>
... <i>MoML elements giving library contents</i> ...
</group>
?&gt;
</pre>
The processing instruction, which is enclosed in "&lt;?" and "?&gt"
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
 @version $Id: ModularCompiledSDFTypedCompositeActor.java 67784 2013-10-26 16:53:27Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

public abstract class ModularCodeGenLazyTypedCompositeActor extends
        LazyTypedCompositeActor {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCodeGenLazyTypedCompositeActor() 
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
    public ModularCodeGenLazyTypedCompositeActor(Workspace workspace)
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
    public ModularCodeGenLazyTypedCompositeActor(CompositeEntity container,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A boolean parameter to enforce recompilation of this ModularCodeGenTypedCompositeActor
     *  and all contained ModularCodeGenTypedCompositeActors.
     */
    public Parameter recompileHierarchy;

    /** A boolean parameter to enforce recompilation of this ModularCodeGenTypedCompositeActor.
     */
    public Parameter recompileThisLevel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate actor name from its class name.
     * @param className  The class name of the actor
     * @return a String that declares the actor name
     */
    public static String classToActorName(String className) {
        return className + "_obj";
    }

    /** Convert this Ptolemy port to a port that will be saved in the profile.
     *  @param port The Ptolemy port.
     *  @exception IllegalActionException When the width can't be retrieved.
     *  @return The profile port for an I/O port.
     */
    public Profile.Port convertProfilePort(IOPort port)
            throws IllegalActionException {
        boolean publisher = _isPublishedPort(port);
        boolean subscriber = _isSubscribedPort(port);
        return new Profile.Port(port.getName(), publisher, subscriber,
                port.getWidth(), DFUtilities.getTokenConsumptionRate(port),
                JavaCodeGenerator.ptTypeToCodegenType(((TypedIOPort) port)
                        .getType()), port.isInput(), port.isOutput(),
                port.isMultiport(), _pubSubChannelName(port, publisher,
                        subscriber));
    }

    /** Return true if the port is a is connected to a publisher.
     *  @param port The port to look up.
     *  @return Return true if the port is a is connected to a publisher.
     */
    abstract protected boolean _isPublishedPort(IOPort port);

    /** Return true if the port is a is connected to a subscriber.
     *  @param port The port to look up.
     *  @return Return true if the port is a is connected to a subscriber.
     */
    abstract protected boolean _isSubscribedPort(IOPort port);

    /** Return the name of a Publisher or Subscriber channel name.
     *  @param port The port.
     *  @param publisher True if the corresponding Publisher should
     *  be returned.
     *  @param subscriber True if the corresponding Subscriber should
     *  be returned.
     *  @return the name of the channel.
     */
    abstract protected String _pubSubChannelName(IOPort port, boolean publisher,
            boolean subscriber);

    /** If configure is done, populating is not occurring,
     *  code is not being generated and Pub/Subs are not being
     *  created, then set the <i>recompileThisLevel</i> parameter
     *  to true.  Otherwise, do nothing.
     */
    protected void _setRecompileFlag() throws IllegalActionException {
        if (_configureDone && !_populating && !_generatingCode
                && _creatingPubSub == 0) {
            recompileThisLevel.setToken(new BooleanToken(true));
            _profile = null;
        }
    }

    protected void _transferOutputs(IOPort port, Object outputTokens)
            throws IllegalActionException {

        int rate = DFUtilities.getTokenProductionRate(port);
        Type type = ((TypedIOPort) port).getType();
        if (type == BaseType.INT) {

            int[][] tokens = (int[][]) outputTokens;
            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    Token token = new IntToken(tokens[i][k]);
                    port.send(i, token);
                }
            }

        } else if (type == BaseType.DOUBLE) {

            double[][] tokens = (double[][]) outputTokens;
            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    Token token = new DoubleToken(tokens[i][k]);
                    port.send(i, token);
                }
            }

            /*} else if (type == PointerToken.POINTER) {

                int[][] tokens = (int[][]) outputTokens;
                for (int i = 0; i < port.getWidthInside(); i++) {
                    for (int k = 0; k < rate; k++) {
                        Token token = new PointerToken(tokens[i][k]);
                        port.send(i, token);
                    }
                }
            */
        } else if (type == BaseType.BOOLEAN) {

            boolean[][] tokens = (boolean[][]) outputTokens;
            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    Token token = new BooleanToken(tokens[i][k]);
                    port.send(i, token);
                }
            }

        } else if (type instanceof ArrayType) {

            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    type = ((ArrayType) type).getElementType();
                    try {
                        Object[][] tmpOutputTokens = (Object[][]) outputTokens;
                        Class<?> tokenClass = tmpOutputTokens[i][k].getClass();

                        Method getPayload;
                        getPayload = tokenClass.getMethod("getPayload",
                                (Class[]) null);

                        Object payload = null;
                        payload = getPayload.invoke(tmpOutputTokens[i][k],
                                (Object[]) null);

                        Field objSize = payload.getClass().getField("size");
                        int size = objSize.getInt(payload);

                        Field elementsField = payload.getClass().getField(
                                "elements");
                        Object[] elements = (Object[]) elementsField
                                .get(payload);

                        Token[] convertedTokens = new Token[size];

                        for (int j = 0; j < size; j++) {
                            Object element = getPayload.invoke(elements[j],
                                    (Object[]) null);
                            if (type == BaseType.INT) {
                                convertedTokens[j] = new IntToken(
                                        Integer.parseInt(element.toString()));
                            } else if (type == BaseType.DOUBLE) {
                                convertedTokens[j] = new DoubleToken(
                                        Double.parseDouble(element.toString()));
                            } else if (type == BaseType.BOOLEAN) {
                                convertedTokens[j] = new BooleanToken(
                                        Boolean.parseBoolean(element.toString()));
                            } else {
                                //FIXME: need to deal with other types
                            }
                        }

                        Token token = new ArrayToken(type, convertedTokens);
                        port.send(i, token);

                    } catch (SecurityException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    } catch (NoSuchMethodException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    } catch (IllegalArgumentException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    } catch (IllegalAccessException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    } catch (InvocationTargetException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    } catch (NoSuchFieldException e) {
                        throw new IllegalActionException(this, e,
                                "Can't generate transfer code.");
                    }
                }
            }
        } else {
            // FIXME: need to deal with other types
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    protected boolean _addedSubscribersFromProfile = false;

    protected int _creatingPubSub = 0;

    protected boolean _generatingCode = false;

    protected transient Method _fireMethod;

    protected Profile _profile = null;

    protected Object _objectWrapper;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _init() throws IllegalActionException, NameDuplicationException {
        recompileHierarchy = new Parameter(this, "recompileHierarchy");
        recompileHierarchy.setExpression("true");
        recompileHierarchy.setTypeEquals(BaseType.BOOLEAN);

        recompileThisLevel = new Parameter(this, "recompileThisLevel");
        recompileThisLevel.setExpression("true");
        recompileThisLevel.setTypeEquals(BaseType.BOOLEAN);
    }
}