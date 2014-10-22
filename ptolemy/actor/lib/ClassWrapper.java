/* An actor that wraps (an instance of) a Java class.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ConversionUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// ClassWrapper

/**
 This actor wraps (an instance of) a Java class specified by the
 <i>className</i> parameter. The actor has no ports when created.
 If an input port is added to the actor, the name of the port is
 interpreted as the name of a method of the Java class. When the
 actor is fired and a token is received from this input port, the
 value of the token is treated as the argument(s) for invoking
 the method. If the method has a return value and the actor has
 an output port named <i>methodName</i>Result, the return value
 is wrapped in a token that is sent to the output port.
 <p>
 For example, suppose the specified class has a method named foo
 and the actor has an input port of the same name. If method foo
 takes no argument, the token received from port <i>foo</i> is
 treated as the trigger for invoking the method, and its content
 is ignored. If method foo takes arguments, the input token
 should be a record token whose field values are used as the
 arguments. The field labels of the record token should be "arg1",
 "arg2", etc. For example, if method foo takes two double arguments,
 the record token "{arg1 = 0.0, arg2 = 1.0}" can be the input.

 A special case is when method foo takes one argument, the token
 containing the argument value can be input directly, and does not
 need to be put into a record token.
 <p>
 FIXME: Need to set type constraints appropriately.
 Need (and how) to handle overloaded methods.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class ClassWrapper extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor, create
     *  the <i>className</i> parameter.
     *  @param container The container of this actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with the specified name.
     */
    public ClassWrapper(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        className = new StringAttribute(this, "className");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the Java class.
     */
    public StringAttribute className;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input port. If an input port
     *  has a token, the content of the token is used as argument(s)
     *  for invoking (on the wrapped instance or class) the method of
     *  the same name as the port. If the method has a return value,
     *  the value is wrapped in a token, and is sent to the output port
     *  named <i>methodName</i>Result.
     *  @exception IllegalActionException If the method invocation fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Iterator inPorts = inputPortList().iterator();

        while (inPorts.hasNext()) {
            IOPort inPort = (IOPort) inPorts.next();

            if (inPort.hasToken(0)) {
                _invoke(inPort, inPort.get(0));
            }
        }
    }

    /** Get the Class object of the specified class. Gather method
     *  invocation information corresponding to each input port. If
     *  at least one method corresponding to a port is not static,
     *  create an instance of the specified class.
     *  @exception IllegalActionException If the specified class cannot
     *   be loaded, or there is no method of the same name as an input
     *   port, or an instance of the class cannot be created.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            _class = Class.forName(className.getExpression());
        } catch (ClassNotFoundException ex) {
            throw new IllegalActionException(this, ex, "Cannot find specified "
                    + "class " + className.getExpression());
        }

        _methodTable = new Hashtable();

        Method[] methods = _class.getMethods();
        Iterator inPorts = inputPortList().iterator();
        boolean needInstance = false;

        while (inPorts.hasNext()) {
            IOPort inPort = (IOPort) inPorts.next();
            String portName = inPort.getName();
            Method m = null;

            for (int i = 0; i < methods.length; ++i) {
                if (methods[i].getName().equals(portName)) {
                    m = methods[i];
                    break;
                }
            }

            if (m == null) {
                throw new IllegalActionException(this, "The specified class "
                        + "does not have a method of the same name as input "
                        + "port " + portName);
            }

            Object[] methodInfo = new Object[3];
            methodInfo[0] = m;
            methodInfo[1] = m.getParameterTypes();

            IOPort outPort = (IOPort) getPort(portName + "Result");

            if (outPort != null && outPort.isOutput()) {
                methodInfo[2] = outPort;
            } else {
                methodInfo[2] = null;
            }

            _methodTable.put(inPort, methodInfo);

            if (!Modifier.isStatic(m.getModifiers())) {
                needInstance = true;
            }
        }

        _instance = null;

        if (needInstance) {
            try {
                // FIXME: here only try to use a constructor with no argument
                Constructor constructor = _class.getConstructor(new Class[0]);
                _instance = constructor.newInstance(new Object[0]);
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Cannot create an "
                        + "instance of the specified class");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Invoke on the wrapped instance the method with the same name as the
    // specified port, treating argv as the arguments.
    // NOTE: Should this method return a boolean, false when the invocation
    // fails? The actor may have an error output port, and send a token
    // to this port when invocation fails.
    private void _invoke(IOPort port, Token argv) throws IllegalActionException {
        // assert port.isInput()
        Object[] methodInfo = (Object[]) _methodTable.get(port);

        // when _methodTable is built, an entry for each input port is
        // guaranteed
        Method m = (Method) methodInfo[0];
        Class[] argTypes = (Class[]) methodInfo[1];
        int args = argTypes.length;
        IOPort outPort = (IOPort) methodInfo[2];

        // The following code is mostly copied from data.expr.ASTPtFunctionNode
        Object[] argValues = new Object[args];

        if (args > 0) {
            RecordToken argRecord = null;

            if (argv instanceof RecordToken) {
                argRecord = (RecordToken) argv;
            } else if (args > 1) {
                throw new IllegalActionException(this, "cannot convert "
                        + "input token to method call arguments.");
            }

            for (int i = 0; i < args; ++i) {
                Token arg = null;

                if (argRecord != null) {
                    arg = argRecord.get("arg" + (i + 1));
                } else {
                    // this is the case when the method takes one argument
                    // and the input token is not a record token
                    arg = argv;
                }

                if (argTypes[i].isAssignableFrom(arg.getClass())) {
                    argValues[i] = arg;
                } else {
                    argValues[i] = ConversionUtilities
                            .convertTokenToJavaType(arg);
                }
            }
        }

        Object result = null;

        try {
            result = m.invoke(_instance, argValues);
        } catch (InvocationTargetException ex) {
            // get the exception produced by the invoked function
            ex.getTargetException().printStackTrace();
            throw new IllegalActionException(this, ex.getTargetException(),
                    "Error invoking method " + m.getName());
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Error invoking method "
                    + m.getName());
        }

        Token resultToken = null;

        if (result == null) {
            // the method does not return value
            return;
        } else if (result instanceof Token) {
            resultToken = (Token) result;
        } else if (result instanceof Double) {
            resultToken = new DoubleToken(((Double) result).doubleValue());
        } else if (result instanceof Integer) {
            resultToken = new IntToken(((Integer) result).intValue());
        } else if (result instanceof Long) {
            resultToken = new LongToken(((Long) result).longValue());
        } else if (result instanceof String) {
            resultToken = new StringToken((String) result);
        } else if (result instanceof Boolean) {
            resultToken = new BooleanToken(((Boolean) result).booleanValue());
        } else if (result instanceof Complex) {
            resultToken = new ComplexToken((Complex) result);
        } else if (result instanceof FixPoint) {
            resultToken = new FixToken((FixPoint) result);
        } else {
            throw new IllegalActionException(this, "Result of method call "
                    + port.getName() + " is not a supported type: boolean, "
                    + "complex, fixpoint, double, int, long  and String, "
                    + "or a Token.");
        }

        if (outPort != null) {
            outPort.send(0, resultToken);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A hash table containing the method invocation information for the
    // input ports. For each input port, the entry in the hash table is
    // an array of three objects. The first is the Method object of the
    // method to be invoked. The second is the array of argument types
    // of the method. The third is the output port to which the return
    // value of the method is sent.
    private Hashtable _methodTable = null;

    // The instance of the specified class.
    private Object _instance = null;

    // The Class object of the specified class.
    private Class _class = null;
}
