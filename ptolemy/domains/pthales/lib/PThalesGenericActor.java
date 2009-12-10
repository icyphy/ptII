/* Generic actor for Pthales objects.

 Copyright (c) 1997-2006 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.pthales.kernel.PthalesDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
A PThalesGenericActor is an element of ArrayOL in Ptolemy.
It is an actor where generic means that the same actor is used to 
implement different functions. This actor calls a JNI function when fired,
with arguments in correct orders. These function and arguments are parameters
of the actor.
@author Rémi Barrère
@see ptolemy.actor.TypedAtomicActor
*/

public class PThalesGenericActor extends TypedAtomicActor implements
        PthalesActorInterface {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public PThalesGenericActor() throws IllegalActionException,
            NameDuplicationException {
        super();

        _initialize();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public PThalesGenericActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);

        _initialize();
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
    public PThalesGenericActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
 
    /** a String that is used to fill arguments when calling the function
     */
    public Parameter arguments;

    /** the name of the function to call when the actor is fired
     */
    public Parameter function;

    /** the number of times this actor is fired
     */
    public Parameter repetitions;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This method
     *  sets _internalRepetitions, _totalRepetitions and _externalRepetitions.   
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PThalesGenericActor newObject = (PThalesGenericActor) super.clone(workspace);

        _internalRepetitions = parseRepetitions(INTERNAL_REPETITIONS);
        _totalRepetitions = parseRepetitions(PthalesActorInterface.REPETITIONS);
        _externalRepetitions = new Integer[_totalRepetitions.length
                - _internalRepetitions.length];
        for (int i = 0; i < _totalRepetitions.length
                 - _internalRepetitions.length; i++) {
            _externalRepetitions[i] = _totalRepetitions[i
                    + _internalRepetitions.length];
        }

        return newObject;
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            PThalesIOPort port = new PThalesIOPort(this, name, false, false);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the argument is the <i>init</i> parameter, then reset the
     *  state to the specified value.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If <i>init<i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */

    /** Read all the array then (should) call JNI function 
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {

        // Variables
        List<PThalesIOPort> portsIn = null;
        List<PThalesIOPort> portsOut = null;
        int portNumber;

        // Input ports 
        portsIn = inputPortList();
        // Token Arrays from simulation
        Token[] tokensIn = null;
        // Real Arrays 
        float[][] realIn = new float[portsIn.size()][];

        // ouput ports
        portsOut = outputPortList();
        // In the output case, each array is produced independantly
        Token[] tokensOut = null;
        // Real Array (only one) 
        float[][] realOut = new float[portsOut.size()][];

        // BEFORE CALLING TASK //

        portNumber = 0;
        // Input ports created and filled before elementary task called 
        for (PThalesIOPort port : portsIn) {
            int dataSize = port.getDataProducedSize();
            tokensIn = new FloatToken[dataSize];
            tokensIn = port.get(0, dataSize);

            // Call array conversion
            realIn[portNumber] = convertToken(tokensIn);

            portNumber++;
        }

        portNumber = 0;
        // Outputs ports arrays created before elementary task called 
        for (PThalesIOPort port : portsOut) {
            realOut[portNumber] = new float[port.getDataProducedSize()];
            portNumber++;
        }

        ///////////////////////////////////////
        // Call to elemetary task (JNI or JAVA) 
        ///////////////////////////////////////
        Object[] args = null;

        try {
            PthalesDirector director = (PthalesDirector) getDirector();
            String libName = director.getLibName();
            if (libName.length() > 0) {
                Class c = Class.forName("ptolemy.domains.pthales.JNI."
                        + libName);
                Method[] methods = c.getMethods();

                for (Method method : methods) {
                    if (method.getName().equals(_function)) {
                        try {
                            // Arguments convertion and format as a list
                            args = convertArguments(realIn, realOut);
                            if (method.getParameterTypes().length == args.length) {
                                // JNI Function call with arguments 
                                method.invoke(c, args);

                                // Function call is ok
                                break;
                            }

                        } catch (IllegalArgumentException e) {
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
        for (PThalesIOPort port : portsOut) {
            int dataSize = port.getDataProducedSize();

            tokensOut = convertReal(realOut[portNumber]);
            for (int i = 0; i < port.getWidth(); i++) {
                port.send(i, tokensOut, dataSize);
            }
            portNumber++;
        }
    }

    /** 
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == getAttribute(PthalesActorInterface.REPETITIONS)) {
            _totalRepetitions = parseRepetitions(PthalesActorInterface.REPETITIONS);
            computeIterations();
        }
        if (attribute == getAttribute(INTERNAL_REPETITIONS)) {
            _internalRepetitions = parseRepetitions(INTERNAL_REPETITIONS);
            computeIterations();
        }
        if (attribute == function) {
            _function = function.getExpression();
        }
        if (attribute == arguments) {
            _arguments = arguments.getExpression();
        }
    }

    /** Conversion from Tokens to array used in JNI function 
     * @param tokensIn
     */
    public float[] convertToken(Token[] tokensIn) {
        float[] realIn;

        int nbData = tokensIn.length;
        realIn = new float[nbData];
        for (int i = 0; i < nbData; i++) {
            realIn[i] = ((FloatToken) tokensIn[i]).floatValue();
        }

        return realIn;
    }

    /** Conversion from array used in JNI function to Tokens
     * @param realOut
     */
    public FloatToken[] convertReal(float[] realOut) {
        FloatToken[] tokensOut;

        int nbData = realOut.length;
        tokensOut = new FloatToken[nbData];
        for (int i = 0; i < nbData; i++) {
            tokensOut[i] = new FloatToken(realOut[i]);
        }

        return tokensOut;
    }

    /** Unused
     */
    public boolean postfire() throws IllegalActionException {

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////              abstract methods implementation              ////

    public int getIterations() {
        return _iterations;
    }

    public Integer[] getInternalRepetitions() {
        return _internalRepetitions;
    }

    public Integer[] getExternalRepetitions() {
        return _externalRepetitions;
    }

    public Integer[] getRepetitions() {
        return _totalRepetitions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or an empty array if the parameter does not exist.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    protected Integer[] parseRepetitions(String name) {
        Integer[] result = new Integer[0];
        Attribute attribute = getAttribute(name);
        if (attribute != null && attribute instanceof Parameter) {
            Token token = null;
            try {
                token = ((Parameter) attribute).getToken();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            if (token instanceof ArrayToken) {
                int len = ((ArrayToken) token).length();
                result = new Integer[len];
                for (int i = 0; i < len; i++) {
                    result[i] = new Integer(((IntToken) ((ArrayToken) token)
                            .getElement(i)).intValue());
                }
            }
        }

        return result;
    }

    /** Function which convert a list of arguments into real arguments
     * that will be used for JNI function call.
     * @param in
     * @param out
     * @return A list of arguments to be used for the JNI function call.
     */
    protected Object[] convertArguments(float[][] in, float[][] out) {
        List objs = new ArrayList();

        int numIn = 0;
        int numOut = 0;

        int increase = 0;
        String[] listArgs = _arguments.split(";");

        for (int i = 0; i < listArgs.length; i++) {
            // Argument is a port : check input or output
            if (listArgs[i].equals("port")) {
                if (listArgs[i + 1].equals("OUT")) {
                    Integer[] sizes = ((PThalesIOPort) outputPortList().get(
                            numOut)).getDataProducedSizes();
                    for (int size : sizes) {
                        if (size > 1)
                            objs.add(size);
                    }
                    objs.add(out[numOut]);
                    numOut++;
                }
                if (listArgs[i + 1].equals("IN")) {
                    Integer[] sizes = ((PThalesIOPort) inputPortList().get(
                            numIn)).getDataProducedSizes();
                    for (int size : sizes) {
                        if (size > 1)
                            objs.add(size);
                    }
                    objs.add(in[numIn]);
                    numIn++;
                }
                increase = 1;
            }

            // Argument is parameter => converted into type
            if (listArgs[i].equals("parameter")) {
                if (listArgs[i + 1].equals("int")) {
                    objs.add(Integer.parseInt(listArgs[i + 2]));
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

    /** Compute external iterations each time 
     *  an attribute used to calculate it has changed 
     */
    protected void computeIterations() {
        
        // if no total repetition, no way to calculate
        if (_totalRepetitions != null && _totalRepetitions.length > 0) {

            // Internal Loops only used INSIDE the function
            int internal = 1;
            if (_internalRepetitions != null) {
                for (Integer iter : _internalRepetitions) {
                    internal *= iter;
                }
            }

            // All loops are used to build array
            int iterationCount = 1;
            for (Integer iter : _totalRepetitions) {
                iterationCount *= iter;
            }

            // Iteration is only done on external loops
            _iterations = iterationCount / internal;

            _externalRepetitions = new Integer[_totalRepetitions.length
                    - _internalRepetitions.length];
            for (int i = 0; i < _totalRepetitions.length
                     - _internalRepetitions.length; i++) {
                _externalRepetitions[i] = _totalRepetitions[i
                        + _internalRepetitions.length];
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected String _function = "";

    protected String _arguments = "";

    /** Iteration informations. */
    protected int _iterations = 0;

    protected Integer[] _externalRepetitions = null;

    protected Integer[] _internalRepetitions = new Integer[0];

    protected Integer[] _totalRepetitions = null;
    

    /** The name of the internal repetitions parameter. */
    protected static String INTERNAL_REPETITIONS = "internalRepetitions";

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        if (getAttribute("arguments") == null) {
            arguments = new StringParameter(this, "arguments");
            arguments.setExpression("");
        }
        if (getAttribute("function") == null) {
            function = new StringParameter(this, "function");
            function.setExpression("");
        }
        if (getAttribute("repetitions") == null) {
            repetitions = new Parameter(this, "repetitions");
            repetitions.setExpression("{1}");
        }
    }
}
