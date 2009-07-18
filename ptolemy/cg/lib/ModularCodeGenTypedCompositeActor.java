/* A TypedCompositeActor with Lazy evaluation for Modular code generation. 

 Copyright (c) 2009 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.LazyTypedCompositeActor;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.modular.ModularCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
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


 @author Bert Rodiers, Dai Bui
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

public class ModularCodeGenTypedCompositeActor extends LazyTypedCompositeActor {

    /** Construct a library in the default workspace with no
     *  container and an empty string as its name. Add the library to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public ModularCodeGenTypedCompositeActor() {
        super();
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** Construct a library in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ModularCodeGenTypedCompositeActor(Workspace workspace) {
        super(workspace);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModularCodeGenTypedCompositeActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is ModularCodeGenTypedCompositeActor.
        // However, a parent class, TypedCompositeActor sets the classname
        // to TypedCompositeActor so that the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be ModularCodeGenTypedCompositeActor.
        setClassName("ptolemy.actor.ModularCodeGenTypedCompositeActor");
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  TypeAttribute, then it sets the type of the port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        super.attributeChanged(attribute);
        if (!_populating) { //TODO rodiers: This test is not enough.
            _modelChanged = true;
        }
    }
    
    /** Generate actor name from its class name
     * @param className  The class name of the actor
     * @return a String that declares the actor name
     */
    static public String classToActorName(String className) {
        return className + "_obj";
    }
    
    /** Invalidate the schedule and type resolution and create
     *  new receivers if the specified port is an opaque
     *  output port.  Also, notify the containers of any ports
     *  deeply connected on the inside by calling their connectionsChanged()
     *  methods, since their width may have changed.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);
        if (!_populating) { //TODO rodiers: This test is not enough.
            _modelChanged = true;
        }
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
    public void fire() throws IllegalActionException {
        try {
            // Invoke the native fire method
            if (_debugging) {
                _debug("ModularCodeGenerator: Calling fire method for generated code.");
            }

            List<Object> argList = new LinkedList<Object>();
            
            Iterator<?> inputPorts = inputPortList()
                .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) inputPorts.next();
                int rate = DFUtilities.getTokenConsumptionRate(port);
                Type type = ((TypedIOPort) port).getType();
                Object tokenHolder = null;

                int numberOfChannels = port.getWidth() < port.getWidthInside() ? port
                        .getWidth() : port.getWidthInside();

                if (type == BaseType.INT) {
                    tokenHolder = new int[numberOfChannels][];
                } else if (type == BaseType.DOUBLE) {
                    tokenHolder = new double[numberOfChannels][];
                    /*} else if (type == PointerToken.POINTER) {
                        tokenHolder = new int[numberOfChannels][];*/
                } else if (type == BaseType.BOOLEAN) {
                    tokenHolder = new boolean[numberOfChannels][];
                } else {
                    // FIXME: need to deal with other types
                }

                for (int i = 0; i < port.getWidth(); i++) {
                    try {
                        if (i < port.getWidthInside()) {

                            if (port.hasToken(i, rate)) {
                                Token[] tokens = port.get(i, rate);

                                if (_debugging) {
                                    _debug(getName(), "transferring input from "
                                            + port.getName());
                                }

                                if (type == BaseType.INT) {
                                    if (rate > 1) {
                                        int[] intTokens = new int[rate];
                                        for (int k = 0; k < rate; k++) {
                                            intTokens[k] = ((IntToken) tokens[k])
                                                    .intValue();
                                        }
                                        tokenHolder = intTokens;
                                    } else {
                                        tokenHolder = ((IntToken) tokens[0]).intValue();
                                    }
                                } else if (type == BaseType.DOUBLE) {
                                    if (rate > 1) {
                                        for (int k = 0; k < rate; k++) {
                                            double[] doubleTokens = new double[rate];
                                            doubleTokens[k] = ((DoubleToken) tokens[k])
                                                    .doubleValue();
                                            tokenHolder = doubleTokens;
                                        }
                                    } else {
                                        tokenHolder = ((DoubleToken) tokens[0]).doubleValue();
                                    }
                                } else if (type == BaseType.BOOLEAN) {
                                    if (rate > 1) {
                                        boolean[] booleanTokens = new boolean[rate];
                                        for (int k = 0; k < rate; k++) {
                                            booleanTokens[k] = ((BooleanToken) tokens[k])
                                                    .booleanValue();
                                        }
                                        tokenHolder = booleanTokens;
                                    } else {
                                        tokenHolder = ((BooleanToken) tokens[0]).booleanValue();
                                    }

                                } else {
                                    // FIXME: need to deal with other types
                                }
                                argList.add(tokenHolder);
                            } else {
                                throw new IllegalActionException(this, port,
                                        "Port should consume " + rate
                                                + " tokens, but there were not "
                                                + " enough tokens available.");
                            }

                        } else {
                            // No inside connection to transfer tokens to.
                            // In this case, consume one input token if there is one.
                            if (_debugging) {
                                _debug(getName(), "Dropping single input from "
                                        + port.getName());
                            }

                            if (port.hasToken(i)) {
                                port.get(i);
                            }
                        }
                    } catch (NoTokenException ex) {
                        // this shouldn't happen.
                        throw new InternalErrorException(this, ex, null);
                    }

                }
            }

            Object[]  tokensToAllOutputPorts = (Object[]) _fireMethod.invoke(
                    _objectWrapper, argList.toArray());
            
            int portNumber = 0;
            for (Object port : outputPortList()) {
                    IOPort iOPort = (IOPort) port;
                    _transferOutputs(iOPort, tokensToAllOutputPorts[portNumber++]);
            }
    
            if (_debugging) {
                _debug("ModularCodeGenerator: Done calling fire method for generated code.");
            }
            
        } catch (Throwable throwable) {
            // If we can't use the compiled code we directly
            // use the model.
            
            if (_debugging) {
                _debug("ModularCodeGenerator: Calling fire method for generated code failed.\n\t" +
                                "Reason: " + throwable.getMessage() + 
                		"\n\tCalling fire method in ptolemy.");
            }
            
            super.fire();
        }

    }
    
    //TODO rodiers
    public void generateCode() throws KernelException {
        if (_codeGenerator == null) {
            _codeGenerator = new ModularCodeGenerator(this, "ModularCodeGenerator");
            _codeGenerator.setPersistent(false);
            // TODO hide
        }
        // TODO: Test whether we need to generate code. 
        _codeGenerator.generateCode();
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
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        if (_modelChanged) {
            try {
                generateCode();
            } catch (KernelException e) {
                throw new IllegalActionException(this, e, "Can't generate code for " + getName());
            }
        }
        String className = ProgramCodeGeneratorAdapter.generateName(this);        
        Class<?> classInstance = null;
        URL url = null;
        try {
            url = _codeGenerator.codeDirectory.asFile().toURI().toURL();
            URL[] urls = new URL[] { url };

            ClassLoader classLoader = new URLClassLoader(urls);
            classInstance = classLoader.loadClass(className);

        } catch (MalformedURLException ex) {
            throw new IllegalActionException(this, ex,
                    "The class URL \"" + url + "\" for \""
                            + className + "\" is malformed");
        } catch (UnsupportedClassVersionError ex) {
            // This can occur if we have two different
            // machines sharing ~/codegen.
            throw new IllegalActionException(
                    this,
                    ex,
                    "Unsupported class version in the class \""
                            + className
                            + "\" from \""
                            + url
                            + "\".  Try deleting the \""
                            + className
                            + "\" class in \""
                            + url
                            + "\".\nThis problem can also occur "
                            + "if the version of java that is "
                            + "running Ptolemy and the version "
                            + "of javac used to compile the file "
                            + "to load into Ptolemy are different "
                            + "and java is of a later version."
                            + "\nTo see information about the "
                            + "version of Java used to run "
                            + "Ptolemy, use View -> JVM Properties."
                            + "  To see what version of javac "
                            + "was used, run \"java -version\".");
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot load the class \"" + className
                            + "\" from \"" + url + "\"");
        }

        try {
            _objectWrapper = classInstance.newInstance();
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Cannot instantiate the wrapper object.");
        }

        Method[] methods = classInstance.getMethods();
        Method intializeMethod = null;
        
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.equals("fire")) {
                _fireMethod = methods[i];
            }
            
            if (name.equals("initialize")) {
                intializeMethod = methods[i];
            }
        }
        if (_fireMethod == null) {
            throw new IllegalActionException(this,
                    "Cannot find fire "
                            + "method in the wrapper class.");
        }
        
        if (intializeMethod == null) {
            throw new IllegalActionException(this,
                    "Cannot find intialize "
                            + "method in the wrapper class.");
        }
        
        //initialize the generated object
        try {
            
            intializeMethod.invoke(
                _objectWrapper, (Object[]) null);
            if (_debugging) {
                _debug("ModularCodeGenerator: Done calling fire method for generated code.");
            }
        
        } catch (Throwable throwable) {
            // If we can't use the compiled code we directly
            //  use the model.
        
            if (_debugging) {
                _debug("ModularCodeGenerator: Calling intialize method for generated code failed.\n\t" +
                                "Reason: " + throwable.getMessage() + 
                        "\n\tCalling fire method in ptolemy.");
            }
        
        }
        _modelChanged = false;
    }


    private void _transferOutputs(IOPort port, Object outputTokens)
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

        } else if(type instanceof ArrayType) {

            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    type = ((ArrayType)type).getElementType();
                    try {
                        Object[][] tmpOutputTokens = (Object[][])outputTokens;
                        Class<?> tokenClass = tmpOutputTokens[i][k].getClass();
                        
                        Method getPayload;
                        getPayload = tokenClass.getMethod("getPayload", (Class[]) null);
                        
                        Object payload = null;
                        payload = (Object) getPayload.invoke(tmpOutputTokens[i][k], (Object[])null);
                        
                        Field objSize = (Field) (payload.getClass().getField("size"));
                        int size = objSize.getInt(payload);
                        
                        Field elementsField = (Field) (payload.getClass().getField("elements"));
                        Object[] elements = (Object[])elementsField.get(payload);
                        
                        Token[] convertedTokens = new Token[size];
                        
                        for(int j = 0; j < size; j++) {
                            Object element =  (Object)getPayload.invoke(elements[j], (Object[])null);
                            if(type == BaseType.INT) {
                                convertedTokens[j] = new IntToken(Integer.parseInt(element.toString())); 
                            } else if (type == BaseType.DOUBLE) {
                                convertedTokens[j] = new DoubleToken(Double.parseDouble(element.toString()));
                            } else if (type == BaseType.BOOLEAN) {
                                convertedTokens[j] = new BooleanToken(Boolean.parseBoolean(element.toString()));
                            } else {
                                //FIXME: need to deal with other types
                            }
                        }
                        
                        Token token = new ArrayToken(type, convertedTokens);
                        port.send(i, token);
                        
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        
                    } catch (NoSuchFieldException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                }
            }
        } else {
            // FIXME: need to deal with other types
        }
    }

    
    private ModularCodeGenerator _codeGenerator = null;
    
    private transient Method _fireMethod;
    
    private boolean _modelChanged = false;
    
    private Object _objectWrapper;
    
}
