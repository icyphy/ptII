/* Generic actor for Pthales objects.

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
package ptolemy.domains.pthales.lib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FloatToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.pthales.kernel.PthalesDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 An element of ArrayOL in Ptolemy.

 <p>ArrayOL "is a high-level visual language dedicated to
 multidimensional intensive signal processing applications."

 <p>In the name of this actor, "Generic" means that the same actor
 is used to implement different functions. This actor calls a JNI
 function when fired, with arguments in correct orders. These function
 and arguments are parameters of the actor.

 <p>For details about ArrayOL, see:
 P. Boulet, <a href="http://hal.inria.fr/inria-00128840/en">Array-OL Revisited, Multidimensional Intensive Signal Processing Specification</a>,INRIA, Sophia Antipolis, France, 2007.

 @author Remi Barrere
 @see ptolemy.actor.TypedAtomicActor
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesGenericActor extends PthalesAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesGenericActor() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PthalesGenericActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesGenericActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the contents of the array and then call JNI function.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    @Override
    public void fire() throws IllegalActionException {

        // Variables
        List<TypedIOPort> portsIn = null;
        List<TypedIOPort> portsOut = null;
        int portNumber;

        // Input ports
        portsIn = inputPortList();
        // Token Arrays from simulation
        Token[] tokensIn = null;
        // Real Arrays
        float[][] realIn = new float[portsIn.size()][];

        // output ports
        portsOut = outputPortList();
        // In the output case, each array is produced independantly
        Token[] tokensOut = null;
        // Real Array (only one)
        float[][] realOut = new float[portsOut.size()][];

        // BEFORE CALLING TASK //

        portNumber = 0;
        // Input ports created and filled before elementary task called
        for (IOPort port : portsIn) {
            int dataSize = PthalesIOPort.getDataProducedSize(port)
                    * PthalesIOPort.getNbTokenPerData(port);
            tokensIn = new FloatToken[dataSize];
            tokensIn = port.get(0, dataSize);

            // Call array conversion
            realIn[portNumber] = convertToken(tokensIn);

            portNumber++;
        }

        portNumber = 0;
        // Outputs ports arrays created before elementary task called
        for (IOPort port : portsOut) {
            realOut[portNumber] = new float[PthalesIOPort
                                            .getDataProducedSize(port)
                                            * PthalesIOPort.getNbTokenPerData(port)];
            portNumber++;
        }

        ///////////////////////////////////////
        // Call to elemetary task (JNI or JAVA)
        ///////////////////////////////////////
        Object[] args = null;
        String function = ((Parameter) getAttribute("function"))
                .getExpression();

        try {
            PthalesDirector director = (PthalesDirector) getDirector();
            String libName = director.getLibName();
            if (libName.length() > 0) {
                Class c = Class.forName("ptolemy.domains.pthales.JNI."
                        + libName);
                Method[] methods = c.getMethods();

                for (Method method : methods) {
                    if (method.getName().equals(function)) {
                        try {
                            // Arguments convertion and format as a list
                            args = _convertArguments(realIn, realOut);
                            if (method.getParameterTypes().length == args.length) {
                                // JNI Function call with arguments
                                method.invoke(c, args);

                            } else {
                                throw new IllegalActionException(this,
                                        "Wrong argument number calling "
                                                + method.getName());
                            }
                            // Function call is done
                            break;

                        } catch (IllegalArgumentException e) {
                            // FIXME: Don't print a stack trace, instead
                            // this method should catch all these and
                            // then throw an IllegalActionException.
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // AFTER CALLING TASK //

        portNumber = 0;
        // Output ports write
        for (IOPort port : portsOut) {
            int dataSize = PthalesIOPort.getDataProducedSize(port)
                    * PthalesIOPort.getNbTokenPerData(port);

            tokensOut = convertReal(realOut[portNumber]);
            for (int i = 0; i < port.getWidth(); i++) {
                port.send(i, tokensOut, dataSize);
            }
            portNumber++;
        }
    }

    /** Always return true in this base class, indicating
     *  that execution can continue into the next iteration.
     *  @return Always return true in this base class, indicating
     *  that execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // FIXME: This should either call super.postfire()
        // or else print the debugging message as AtomicActor.postfire() does.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    @Override
    protected void _initialize() throws IllegalActionException,
    NameDuplicationException {

        super._initialize();

        if (getAttribute("arguments") == null) {
            Parameter arguments = new StringParameter(this, "arguments");
            arguments.setExpression("");
        }
        if (getAttribute("function") == null) {
            Parameter function = new StringParameter(this, "function");
            function.setExpression("");
        }
    }

    /** Function which convert a list of arguments into real arguments and length
     * that will be used for JNI function call.
     * @param in input arguments
     * @param out output arguments
     * @return A list of arguments to be used for the JNI function call.
     */
    protected Object[] _convertArguments(float[][] in, float[][] out) {
        // FIXME: prepend an underscore to the name of this protected method.
        List objs = new ArrayList();

        int numIn = 0;
        int numOut = 0;
        int increase = 0;

        String arguments = ((Parameter) getAttribute("arguments"))
                .getExpression();

        String[] listArgs = arguments.split(";");

        for (int i = 0; i < listArgs.length; i++) {
            // Argument is a port : check input or output
            if (listArgs[i].equals("port")) {
                if (listArgs[i + 1].equals("OUT")) {
                    Integer[] sizes = PthalesIOPort
                            .getDataProducedSizes(outputPortList().get(numOut));
                    for (int size : sizes) {
                        if (size > 1) {
                            objs.add(size);
                        }
                    }
                    objs.add(out[numOut]);
                    numOut++;
                }
                if (listArgs[i + 1].equals("IN")) {
                    Integer[] sizes = PthalesIOPort
                            .getDataProducedSizes(inputPortList().get(numIn));
                    for (int size : sizes) {
                        if (size > 1) {
                            objs.add(size);
                        }
                    }
                    objs.add(in[numIn]);
                    numIn++;
                }
                increase = 1;
            }

            // Argument is parameter => converted into type
            if (listArgs[i].equals("parameter")) {
                if (listArgs[i + 1].equals("int")) {
                    try {
                        objs.add(Integer.parseInt(listArgs[i + 2]));
                    } catch (NumberFormatException e) {
                        objs.add(0);
                    }
                } else if (listArgs[i + 1].equals("long")) {
                    objs.add(Long.parseLong(listArgs[i + 2]));
                } else if (listArgs[i + 1].equals("double")
                        || listArgs[i + 1].equals("Spldouble")) {
                    objs.add(Double.parseDouble(listArgs[i + 2]));
                } else if (listArgs[i + 1].equals("float")
                        || listArgs[i + 1].equals("Splfloat")) {
                    objs.add(Float.parseFloat(listArgs[i + 2]));
                } else {
                    // Type is not a primitive =>  string
                    objs.add(listArgs[i]);
                }
                increase = 2;
            }

            i += increase;
        }

        return objs.toArray();
    }
}
