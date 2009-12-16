/* An TypedIOPort with ArrayOL informations

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypeEvent;
import ptolemy.actor.TypeListener;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
////PthalesIOPort

/**
 A PthalesIOPort is an element of ArrayOL in Ptolemy.
 It contains data needed to determine access to data
 using multidimensional arrays.

 @author Remi Barrere
 @see ptolemy.actor.TypedIOPort
 @version $Id$
 @since Ptolemy II 8.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesIOPort extends TypedIOPort {

    /** Construct a PthalesIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public PthalesIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }

    /** Construct a PthalesIOPort with no container and no name that is
     *  neither an input nor an output.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public PthalesIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);

        // Add parameters for PThales Domain
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must
     *  do this yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  The new port will have the same type as this one, but will not
     *  have any type listeners and type constraints attached to it.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new PthalesIOPort.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PthalesIOPort newObject = (PthalesIOPort) super.clone(workspace);

        // set _declaredType and _resolvedType
        if (_declaredType instanceof StructuredType
                && !_declaredType.isConstant()) {
            newObject._declaredType = (Type) ((StructuredType) _declaredType)
                    .clone();
            newObject._resolvedType = newObject._declaredType;
        }

        newObject._typeTerm = null;
        newObject._typeListeners = new LinkedList<TypeListener>();
        newObject._constraints = new HashSet<Inequality>();
        return newObject;
    }

    /** Returns the number of addresses needed to access all of the
     *  datas for all iteration
     *  @return the number of tokens needed
     *  FIXME: should be deleted if empty tilings are taken in account
     */
    public static int getAddressNumber(IOPort port) {
        int valuePattern = 1;
        Actor actor = (Actor) port.getContainer();

        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor)
            rep = PthalesGenericActor.getRepetitions((AtomicActor) actor);
        if (actor instanceof CompositeActor)
            rep = PthalesCompositeActor.getRepetitions((CompositeActor) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();

        int i = 0;
        for (Object dimension : dims) {
            if (!tilingSet.contains(dimension)) {
                valuePattern *= pattern.get(dimension)[0]
                        * pattern.get(dimension)[1];
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dimension) && pattern.get(dimension) != null) {
                        if (i < rep.length) {
                            valuePattern *= pattern.get(dimension)[0]
                                    * pattern.get(dimension)[1] + rep[i]
                                    * tiling.get(til)[0] - 1;
                        } else {
                            // Not enough reps for tilings, rep = 1
                            valuePattern *= pattern.get(dimension)[0]
                                    * pattern.get(dimension)[1]
                                    + tiling.get(til)[0] - 1;
                        }
                    }
                }
            }
            i++;
        }

        int sizeRepetition = 1;
        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !dims.contains(til)) {
                    sizeRepetition *= rep[i];
                }
                i++;
            }
        }

        return valuePattern * sizeRepetition;
    }

    /** Return the base of this port. 
     *  @return base 
     */
    public static LinkedHashMap<String, Integer[]> getBase(IOPort port) {
        return _parseSpec(port, BASE);
    }

    /** Compute total array size.
     *  @return array size
     */
    public static int getArraySize(IOPort port) {
        int val = 1;
        for (Integer size : getArraySizes(port).values()) {
            val *= size;
        }

        return val;
    }

    /** Computes array sizes (for each dimension)
     *  @return array sizes
     */
    public static LinkedHashMap<String, Integer> getArraySizes(IOPort port) {
        LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Token> sizesToMap = new LinkedHashMap<String, Token>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor)
            rep = PthalesGenericActor.getRepetitions((AtomicActor) actor);
        if (actor instanceof CompositeActor)
            rep = PthalesCompositeActor.getRepetitions((CompositeActor) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim)) {
                sizes.put((String) dim, pattern.get(dim)[0]);
                sizesToMap.put((String) dim, new IntToken(pattern.get(dim)[0]));
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            sizes.put((String) dim, pattern.get(dim)[0]
                                    + rep[i] * tiling.get(til)[0] - 1);
                            sizesToMap.put((String) dim, new IntToken(pattern
                                    .get(dim)[0]
                                    + rep[i] * tiling.get(til)[0] - 1));
                        } else {
                            // Not enough reps for tilings, rep = 1
                            sizes.put((String) dim, pattern.get(dim)[0]
                                    + tiling.get(til)[0] - 1);
                            sizesToMap.put((String) dim, new IntToken(pattern
                                    .get(dim)[0]
                                    + tiling.get(til)[0] - 1));
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !((String) til).startsWith("empty")
                        && !dims.contains(til)) {
                    sizes.put((String) til, rep[i] * tiling.get(til)[0]);
                    sizesToMap.put((String) til, new IntToken(rep[i]
                            * tiling.get(til)[0]));
                }
                i++;
            }
        }

        // Size written if not already set
        try {
            OrderedRecordToken array = new OrderedRecordToken(sizesToMap);
            // Write into parameter
            Parameter p = (Parameter) port.getAttribute("size");
            if (p == null) {
                try {
                    // if parameter does not exist, creation
                    p = new Parameter(port, "size");
                } catch (NameDuplicationException e) {
                    e.printStackTrace();
                }
            }

            p.setVisibility(Settable.FULL);
            p.setPersistent(true);
            if (p.getExpression().equals(""))
                p.setExpression(array.toString());

        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

        return sizes;
    }

    /** Compute number of address needed for each iteration 
     *  @return number of address
     */
    public static int getPatternNbAddress(IOPort port) {
        int val = 1;
        for (int size : getPatternNbAddresses(port)) {
            val *= size;
        }

        return val;
    }

    /** Compute  number of address by dimension needed for each iteration
     *  @return address array
     */
    public static Integer[] getPatternNbAddresses(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor)
            rep = PthalesGenericActor
                    .getInternalRepetitions((AtomicActor) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim) || rep.length == 0) {
                myList.add(pattern.get(dim)[0]);
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            myList.add(pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1);
                        } else {
                            myList.add(pattern.get(dim)[0] + tiling.get(til)[0]
                                    - 1);
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !dims.contains(til) && !((String)til).startsWith("empty")) {
                    myList.add(rep[i]);
                }
                i++;
            }
        }
        Integer[] result = new Integer[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Computes data size produced for each iteration 
     *  @return data size
     */
    public static int getDataProducedSize(IOPort port) {
        int val = 1;
        for (int size : getDataProducedSizes(port)) {
            val *= size;
        }

        return val;
    }

    /** Computes data sizes (for each dimension) produced for each iteration
     *  @return data sizes
     */
    public static Integer[] getDataProducedSizes(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor)
            rep = PthalesGenericActor
                    .getInternalRepetitions((AtomicActor) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim)) {
                myList.add(pattern.get(dim)[0]);
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            myList.add(pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1);
                        } else {
                            myList.add(pattern.get(dim)[0] + tiling.get(til)[0]
                                    - 1);
                        }

                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !((String) til).startsWith("empty")
                        && !dims.contains(til)) {
                    myList.add(rep[i] * tiling.get(til)[0]);
                }
                i++;
            }
        }
        Integer[] result = new Integer[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Compute pattern for external iteration
     *  @return data sizes
     */
    public static LinkedHashMap<String, Integer[]> getInternalPattern(
            IOPort port) {
        LinkedHashMap<String, Integer[]> internalPattern = new LinkedHashMap<String, Integer[]>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor)
            rep = PthalesGenericActor
                    .getInternalRepetitions((AtomicActor) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();

        int i = 0;
        Integer[] res;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim) || rep.length == 0) {
                internalPattern.put((String) dim, pattern.get(dim));
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        res = new Integer[2];
                        res[1] = tiling.get(til)[1];

                        if (i < rep.length) {
                            res[0] = pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1;
                            internalPattern.put((String) dim, res);
                        } else {
                            res[0] = pattern.get(dim)[0] + tiling.get(til)[0]
                                    - 1;
                            internalPattern.put((String) dim, res);
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !dims.contains(til) && !((String)til).startsWith("empty")) {
                    res = new Integer[2];
                    res[0] = rep[i];
                    res[1] = tiling.get(til)[1];
                    internalPattern.put((String) til, res);
                }
                i++;
            }
        }

        return internalPattern;
    }

    /** Returns tiling of external loops iterations
     * @param port
     * @param nb
     * @return tiling map 
     */
    public static LinkedHashMap<String, Integer[]> getExternalTiling(
            IOPort port, int nb) {
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<String, Integer[]>();

        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        Object[] tilingSet = tiling.keySet().toArray();
        for (int i = 0; i < tilingSet.length; i++) {
            if (tilingSet.length - nb <= i)
                result.put((String) tilingSet[i], tiling.get(tilingSet[i]));
        }
        return result;
    }

    /** Check if data type is a structure.
     * If yes, gives the number of tokens needed to store all the data
     * By default, the return value is 1
     * @return the number of token needed to store the values
     */
    public static int getNbTokenPerData(IOPort port) {
        Parameter p = (Parameter) port.getAttribute("dataType");
        if (p != null) {
            if (p.getExpression().startsWith("Cpl")) {
                return 2;
            }
        }
        return 1;
    }

    /** Returns dimension names, in order of production 
     *  @return dimension names
     */
    public static String[] getDimensions(IOPort port) {
        List myList = new ArrayList<String>();

        Set dims1 = _getPattern(port).keySet();
        Set dims2 = _getTiling(port).keySet();

        for (Object dim : dims1.toArray()) {
            myList.add((String) dim);
        }
        for (Object dim : dims2.toArray()) {
            if (!myList.contains(dim) && !((String) dim).startsWith("empty"))
                myList.add(dim);
        }

        String[] result = new String[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** If the port is an output, return the remote receivers that can
     *  receive from the port.  For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  The length
     *  of the array is the width of the port (the number of channels).
     *  It is an array of arrays, each of which represents a group of
     *  receivers that receive data from the same channel.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return The receivers for output data, or an empty array if there
     *   are none.
     * @exception IllegalActionException
     */
    public Receiver[][] getRemoteReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            if (!isOutput()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = getWidth();

            if (width <= 0) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // For opaque port, try the cached _farReceivers
            // Check validity of cached version
            if (isOpaque() && (_farReceiversVersion == _workspace.getVersion())) {
                return _farReceivers;
            }

            // If not an opaque port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][0];
            Iterator<?> relations = linkedRelationList().iterator();
            int index = 0;
            // Hypothese : 1 input per relation

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    Receiver[][] deepReceivers = relation.deepReceivers(this);

                    if (deepReceivers != null) {
                        for (int i = 0; i < deepReceivers.length; i++) {
                            farReceivers[index] = deepReceivers[i];
                            index++;
                        }
                    } else {
                        // create a number of null entries in farReceivers
                        // corresponding to the width of relation r
                        index += relation.getWidth();
                    }
                }
            }

            // For an opaque port, cache the result.
            if (isOpaque()) {
                _farReceiversVersion = _workspace.getVersion();
                _farReceivers = farReceivers;
            }

            return farReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Check if data type is a structure.
     * If yes, gives the number of tokens needed to store all the data
     * By default, the return value is 1
     */
    public void setDataType() {
        Parameter p = (Parameter) getAttribute("dataType");
        if (p != null) {
            if (p.getExpression().equals("Cplfloat")
                    || p.getExpression().equals("Splfloat")
                    || p.getExpression().equals("float")) {
                setTypeEquals(BaseType.FLOAT);
            }
            if (p.getExpression().equals("Cpldouble")
                    || p.getExpression().equals("Spldouble")
                    || p.getExpression().equals("double")) {
                setTypeEquals(BaseType.DOUBLE);
            }
            if (p.getExpression().equals("Cplint")
                    || p.getExpression().equals("Splint")
                    || p.getExpression().equals("int")) {
                setTypeEquals(BaseType.INT);
            }
        }
    }

    /** If the specified port has the specified rate parameter, then
     *  record that parameter in the portRates data structure. Otherwise,
     *  create a new rate parameter with value 1 and record that in the
     *  the portRates data structure.
     *  @param portRates The data structure into which to record.
     *  @param port The port.
     *  @param description The name of the parameter.
     *  @throws IllegalActionException
     */
    public void createPortRateParameter(Map<IOPort, Variable> portRates,
            IOPort port, String description) throws IllegalActionException {
        Variable portRate = DFUtilities.getRateVariable(port, description);
        if (portRate == null) {
            portRate = DFUtilities.setRate(port, description, 1);
        }
        portRates.put(port, portRate);
    }

    /** Get the port rate parameter for the specified port, if it
     *  exists, and return its value, or null if it does not exist.
     *  @param port The port.
     *  @param description The name of the parameter.
     *  @return The port rate parameter.
     *  @throws IllegalActionException If evaluating the rate parameter
     *   fails (e.g. due to a malformed expression).
     */
    public Integer getDeclaredPortRate(IOPort port, String description)
            throws IllegalActionException {
        int result = 0;
        if (port instanceof PthalesIOPort) {
            result = getArraySize(port);
        }
        return result;
    }

    /** Attribute update
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == getAttribute("pattern")) {
            _pattern = _parseSpec(this, PATTERN);

        }
        if (attribute == getAttribute("tiling")) {
            _tiling = _parseSpec(this, TILING);

        }
        if (attribute == getAttribute("base")) {
            _base = _parseSpec(this, BASE);
        }
        if (attribute == getAttribute("dataType")) {
            setDataType();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Reset the variable part of this type to the specified type.
     *  @exception IllegalActionException If the type is not settable,
     *   or the argument is not a Type.
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {

        Actor actor = (Actor) getContainer();

        if (getAttribute("base") == null) {
            new Parameter(this, "base");
        }

        if (getAttribute("pattern") == null) {
            new Parameter(this, "pattern");
        }

        if (getAttribute("tiling") == null) {
            new Parameter(this, "tiling");
        }

        if (getAttribute("dimensionNames") == null) {
            new StringParameter(this, "dimensionNames");
        }
        if (getAttribute("size") == null) {
            new Parameter(this, "size");
        }

        // Useless parameters for CompositeActors
        if (actor instanceof PthalesGenericActor) {
            if (getAttribute("dataType") == null) {
                new StringParameter(this, "dataType");
            }

            if (getAttribute("dataTypeSize") == null) {
                new StringParameter(this, "dataTypeSize");
            }
        }
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** parameters */
    protected LinkedHashMap<String, Integer[]> _base = null;

    protected LinkedHashMap<String, Integer[]> _pattern = null;

    protected LinkedHashMap<String, Integer[]> _tiling = new LinkedHashMap<String, Integer[]>();

    ///////////////////////////////////////////////////////////////////
    ////                      public variables                  ////

    /** The name of the base parameter. */
    public static String BASE = "base";

    /** The name of the pattern parameter. */
    public static String PATTERN = "pattern";

    /** The name of the tiling parameter. */
    public static String TILING = "tiling";

    public static Integer ONE = new Integer(1);

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Notify the type listener about type change.
  
    /** returns the pattern of this port 
     *  @return pattern 
     */
    public static LinkedHashMap<String, Integer[]> _getPattern(IOPort port) {
        return _parseSpec(port, PATTERN);
    }

    /** returns the tiling of this port 
     *  @return tiling 
     */
    public static LinkedHashMap<String, Integer[]> _getTiling(IOPort port) {
        return _parseSpec(port, TILING);
    }
    
    
    /**
     * @param oldType
     * @param newType
     */
    private void _notifyTypeListener(Type oldType, Type newType) {
        if (_typeListeners.size() > 0) {
            TypeEvent event = new TypeEvent(this, oldType, newType);
            Iterator<TypeListener> listeners = _typeListeners.iterator();

            while (listeners.hasNext()) {
                (listeners.next()).typeChanged(event);
            }
        }
    }

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or null if the parameter does not exist.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    private static LinkedHashMap<String, Integer[]> _parseSpec(IOPort port,
            String name) {
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<String, Integer[]>();
        Attribute attribute = port.getAttribute(name);
        if (attribute instanceof Parameter) {
            Token token = null;
            try {
                token = ((Parameter) attribute).getToken();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            if (token != null) {
                if (token instanceof OrderedRecordToken) {
                    Set<String> fieldNames = ((OrderedRecordToken) token)
                            .labelSet();
                    for (String fieldName : fieldNames) {
                        Token value = ((OrderedRecordToken) token)
                                .get(fieldName);
                        Integer[] values = new Integer[2];
                        if (value instanceof IntToken) {
                            values[0] = ((IntToken) value).intValue();
                            values[1] = ONE;
                        } else if (value instanceof ArrayToken) {
                            if (((ArrayToken) value).length() != 2) {
                                // FIXME: Need a better error message here.
                            }
                            // FIXME: Check that tokens are IntToken
                            values[0] = ((IntToken) ((ArrayToken) value)
                                    .getElement(0)).intValue();
                            values[1] = ((IntToken) ((ArrayToken) value)
                                    .getElement(1)).intValue();
                        }
                        result.put(fieldName, values);
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Type _declaredType = BaseType.UNKNOWN;

    private Type _resolvedType = BaseType.UNKNOWN;

    private TypeTerm _typeTerm = null;

    // Listeners for type change.
    private List<TypeListener> _typeListeners = new LinkedList<TypeListener>();

    // type constraints
    private Set<Inequality> _constraints = new HashSet<Inequality>();

    /** To avoid creating this repeatedly, we use a single version. */
    private static final Receiver[][] _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;

    private transient long _farReceiversVersion = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class TypeTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return PthalesIOPort.this;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getType();
        }

        /** Return this TypeTerm in an array if this term represent
         *  a type variable. This term represents a type variable
         *  if the type of this port is not set through setTypeEquals().
         *  If the type of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
         *  @param type A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object type) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("TypeTerm.initialize: "
                        + "Cannot initialize a constant type.");
            }

            if (!(type instanceof Type)) {
                throw new IllegalActionException("TypeTerm.initialize: "
                        + "The argument is not a Type.");
            }

            Type oldType = _resolvedType;

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type) type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType) _resolvedType).initialize((Type) type);
            }

            if (!oldType.equals(_resolvedType)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Test if the type of this TypedIOPort can be changed.
         *  The type can be changed if setTypeEquals() is not called,
         *  or called with a BaseType.UNKNOWN argument.
         *  @return True if the type of this TypedIOPort can be changed;
         *   false otherwise.
         */
        public boolean isSettable() {
            return !_declaredType.isConstant();
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            return isTypeAcceptable();
        }

        /** Set the type of this port.
         *  @param type A Type.
         *  @exception IllegalActionException If the new type violates
         *   the declared type of this port.
         */
        public void setValue(Object type) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "TypedIOPort$TypeTerm.setValue: The type is not "
                                + "settable.");
            }

            if (!_declaredType.isSubstitutionInstance((Type) type)) {
                throw new IllegalActionException("Type conflict on port "
                        + PthalesIOPort.this.getFullName() + ".\n"
                        + "Declared type is " + _declaredType.toString()
                        + ".\n"
                        + "The connection or type constraints, however, "
                        + "require type " + type.toString());
            }

            Type oldType = _resolvedType;

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type) type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType) _resolvedType)
                        .updateType((StructuredType) type);
            }

            if (!oldType.equals(type)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Override the base class to give a description of the port
         *  and its type.
         *  @return A description of the port and its type.
         */
        public String toString() {
            return "(" + PthalesIOPort.this.toString() + ", " + getType() + ")";
        }
    }
}
