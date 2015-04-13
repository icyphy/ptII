/* A pool of functions used into Pthales Domain

 Copyright (c) 1997-2014 The Regents of the University of California.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.domains.pthales.kernel.PthalesDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
////PthalesIOPort

/**
 A PthalesIOPort is an element of ArrayOL in Ptolemy.
 It contains functions needed to use multidimensional arrays.

 @author Remi Barrere
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesIOPort {

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the base parameter. */
    public static String BASE = "base";

    /** Fixed variable (for compatibility ?). */
    public static Integer ONE = Integer.valueOf(1);

    /** The name of the pattern parameter. */
    public static String PATTERN = "pattern";

    /** The name of the tiling parameter. */
    public static String TILING = "tiling";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute total array size.
     *  @param port associated port
     *  @return array size
     */
    public static int getArraySize(IOPort port) {
        int val = 1;
        for (Integer size : getArraySizes(port).values()) {
            val *= size;
        }

        return val;
    }

    /** Compute array sizes (for each dimension).
     *  @param port associated port
     *  @return array sizes
     */
    public static LinkedHashMap<String, Integer> getArraySizes(IOPort port) {

        Actor actor = (Actor) port.getContainer();
        Integer[] repetitions = null;

        repetitions = PthalesAtomicActor
                .getRepetitions((ComponentEntity) actor);

        return getArraySizes(port, repetitions);
    }

    /** Compute array sizes (for each dimension).
     *  @param port associated port
     *  @return array sizes
     *  @param repetitions The repetitions values.
     */
    public static LinkedHashMap<String, Integer> getArraySizes(IOPort port,
            Integer[] repetitions) {
        LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Token> sizesToMap = new LinkedHashMap<String, Token>();

        LinkedHashMap<String, Integer[]> pattern = getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = getTiling(port);

        Set<String> patternDimensionNames = pattern.keySet();
        Set<String> tilingDimensionNames = tiling.keySet();
        int i = 0;

        for (String patternDimensionName : patternDimensionNames) {
            if (!tilingDimensionNames.contains(patternDimensionName)) {
                sizes.put(patternDimensionName,
                        pattern.get(patternDimensionName)[0]);
                sizesToMap.put(patternDimensionName,
                        new IntToken(pattern.get(patternDimensionName)[0]));
            } else {
                for (Object tilingDimensionName : tilingDimensionNames) {
                    if (tilingDimensionName.equals(patternDimensionName)) {
                        if (i < repetitions.length) {
                            sizes.put(
                                    patternDimensionName,
                                    pattern.get(patternDimensionName)[0]
                                            + (repetitions[i] - 1)
                                            * tiling.get(tilingDimensionName)[0]);
                            sizesToMap
                            .put(patternDimensionName,
                                    new IntToken(
                                            pattern.get(patternDimensionName)[0]
                                                    + (repetitions[i] - 1)
                                                    * tiling.get(tilingDimensionName)[0]));
                        } else {
                            // Not enough reps for tilings, rep = 1
                            sizes.put(patternDimensionName,
                                    pattern.get(patternDimensionName)[0]);
                            sizesToMap.put(patternDimensionName, new IntToken(
                                    pattern.get(patternDimensionName)[0]));
                        }
                    }
                }
            }
            i++;
        }

        if (repetitions != null) {
            i = 0;
            for (Object til : tilingDimensionNames) {
                if (i < repetitions.length
                        && !((String) til).startsWith("empty")
                        && !patternDimensionNames.contains(til)) {
                    sizes.put((String) til, repetitions[i] * tiling.get(til)[0]);
                    sizesToMap.put((String) til, new IntToken(repetitions[i]
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

            if (p == null) {
                throw new InternalErrorException(port, null,
                        "Could not create Parameter named \"size\"?");
            } else {
                p.setVisibility(Settable.FULL);
                p.setPersistent(true);
                if (p.getExpression().equals("")) {
                    p.setExpression(array.toString());
                }
            }
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

        return sizes;
    }

    /** Return the base of this port.
     *  @param port associated port
     *  @return base
     */
    public static LinkedHashMap<String, Integer[]> getBase(IOPort port) {
        return _parseSpec(port, BASE);
    }

    /** Computes data size produced for each iteration .
     *  @param port associated port
     *  @return data size
     */
    public static int getDataProducedSize(IOPort port) {
        int val = 1;
        for (int size : getDataProducedSizes(port)) {
            val *= size;
        }

        return val;
    }

    /** Computes data sizes (for each dimension) produced for each iteration.
     *  @param port associated port
     *  @return data sizes
     */
    public static Integer[] getDataProducedSizes(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

        Set<String> dims = pattern.keySet();
        Set<String> tilingSet = tiling.keySet();
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
                            // Data produced does depend of repetition, unlike addresses
                            myList.add(pattern.get(dim)[0]);
                        }

                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (String til : tilingSet) {
                if (i < rep.length && !til.startsWith("empty")
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

    /** Return dimension names, in order of production.
     *  @param port associated port
     *  @return dimension names
     */
    public static String[] getDimensions(IOPort port) {
        List myList = new ArrayList<String>();

        Set dims1 = getPattern(port).keySet();
        Set dims2 = getTiling(port).keySet();

        for (Object dim : dims1.toArray()) {
            myList.add(dim);
        }
        for (Object dim : dims2.toArray()) {
            if (!myList.contains(dim) && !((String) dim).startsWith("empty")) {
                myList.add(dim);
            }
        }

        String[] result = new String[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Returns tiling of external loops iterations.
     * @param port associated port
     * @param nb the number of used tilings (external ones)
     * @return tiling map
     */
    public static LinkedHashMap<String, Integer[]> getExternalTiling(
            IOPort port, int nb) {
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<String, Integer[]>();

        LinkedHashMap<String, Integer[]> tiling = getTiling(port);

        Object[] tilingSet = tiling.keySet().toArray();
        for (int i = 0; i < tilingSet.length; i++) {
            if (tilingSet.length - nb <= i) {
                result.put((String) tilingSet[i], tiling.get(tilingSet[i]));
            }
        }
        return result;
    }

    /** Compute pattern for external iteration.
     * @param port associated port
     * @return a hashmap of patterns for each dimension
     */
    public static LinkedHashMap<String, Integer[]> getInternalPattern(
            IOPort port) {
        LinkedHashMap<String, Integer[]> internalPattern = new LinkedHashMap<String, Integer[]>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

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
                if (i < rep.length && !dims.contains(til)
                        && !((String) til).startsWith("empty")) {
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

    /** Return the number of tokens that are logically treated
     *  as a single token. By default, that number is 1, but if the
     *  port has an attribute named "dataType" whose expression value
     *  begins with "Cpl" then that number is 2.
     *  FIXME: This is a hack. Why not use a token array with two tokens?
     *  @param port associated port
     *  @return the number of token needed to store the values
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

    /** Return the pattern of this port.
     *  @param port associated port
     *  @return pattern
     */
    public static LinkedHashMap<String, Integer[]> getPattern(IOPort port) {
        return _parseSpec(port, PATTERN);
    }

    /** Compute number of address needed for each iteration.
     *  @param port associated port
     *  @return number of address
     */
    public static int getPatternNbAddress(IOPort port) {
        int val = 1;
        for (int size : getPatternNbAddresses(port)) {
            val *= size;
        }

        return val;
    }

    /** Compute  number of address by dimension needed for each iteration.
     *  @param port associated port
     *  @return address array
     */
    public static Integer[] getPatternNbAddresses(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

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
                if (i < rep.length && !dims.contains(til)
                        && !((String) til).startsWith("empty")) {
                    myList.add(rep[i]);
                }
                i++;
            }
        }
        Integer[] result = new Integer[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Returns the tiling of this port.
     *  @param port associated port
     *  @return tiling
     */
    public static LinkedHashMap<String, Integer[]> getTiling(IOPort port) {
        return _parseSpec(port, TILING);
    }

    /** Reset the variable part of this type to the specified type.
     *  @param port associated port
     *  @exception IllegalActionException If the type is not settable,
     *   or the argument is not a Type.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public static void initialize(IOPort port) throws IllegalActionException,
    NameDuplicationException {

        if (port.getAttribute("base") == null) {
            new Parameter(port, "base");
        }

        if (port.getAttribute("pattern") == null) {
            new Parameter(port, "pattern");
        }

        if (port.getAttribute("tiling") == null) {
            new Parameter(port, "tiling");
        }

        if (port.getAttribute("dimensionNames") == null) {
            new StringParameter(port, "dimensionNames");
        }
        if (port.getAttribute("size") == null) {
            new Parameter(port, "size");
        }

        if (port.getAttribute("dataType") == null) {
            new StringParameter(port, "dataType");
        }

        if (port.getAttribute("dataTypeSize") == null) {
            new StringParameter(port, "dataTypeSize");
        }
    }

    /** Modify the pattern of the specified port with one dimension
     * (for propagate).
     * @param port associated port
     * @param dim dimension name
     * @param dimSize dimension size
     */
    public static void modifyPattern(IOPort port, String dim, int dimSize) {
        Attribute pattern = port.getAttribute(PATTERN);
        if (port.getAttribute(PATTERN) == null) {
            try {
                pattern = new Parameter(port, PATTERN);
            } catch (IllegalActionException e) {
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                e.printStackTrace();
            }
        }

        if (pattern instanceof Parameter) {
            ((Parameter) pattern).setExpression("[" + dim + "={" + dimSize
                    + ",1}]");
        }
    }

    /** Modify th pattern of the specified port with dimensions (after
     * propagate).
     * @param port associated port
     * @param dims dimension names
     * @param dimSizes dimension sizes
     */
    public static void modifyPattern(IOPort port, String[] dims, int[] dimSizes) {
        Attribute pattern = port.getAttribute(PATTERN);
        if (port.getAttribute(PATTERN) == null) {
            try {
                pattern = new Parameter(port, PATTERN);
            } catch (IllegalActionException e) {
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                e.printStackTrace();
            }
        }

        StringBuffer s = new StringBuffer("[");
        if (pattern instanceof Parameter) {
            for (int i = 0; i < dims.length; i++) {
                s.append(dims[i] + "={" + dimSizes[i] + ",1}");
                if (i < dims.length - 1) {
                    s.append(",");
                }
            }
        }
        s.append("]");
        ((Parameter) pattern).setExpression(s.toString());
    }

    /** Propagate the header through application relations
     * to update information.
     * @param portIn port used in input for propages
     * @param dims dimension names
     * @param sizes dimension sizes
     * @param headersize added header size
     * @param arraySizes sizes used to compute iterations
     */
    public static void propagateHeader(IOPort portIn, String[] dims,
            int[] sizes, int headersize,
            LinkedHashMap<String, Integer> arraySizes) {
        // Header
        if (portIn.getContainer() instanceof PthalesRemoveHeaderActor) {
            int sum = 1;
            for (int size : sizes) {
                sum *= size;
            }
            sum += headersize;

            // Input pattern
            PthalesIOPort.modifyPattern(portIn, "global", sum);

            // Output pattern
            PthalesIOPort.modifyPattern(
                    (IOPort) ((PthalesRemoveHeaderActor) portIn.getContainer())
                    .getPort("out"), dims, sizes);

            // Header found, update of all following Pthales actors
            propagateIterations(
                    (IOPort) ((PthalesRemoveHeaderActor) portIn.getContainer())
                    .getPort("out"),
                    arraySizes);
        }

        if (portIn.isOutput()) {
            if (!(portIn instanceof RefinementPort)) {
                for (IOPort port : (List<IOPort>) portIn.connectedPortList()) {
                    propagateHeader(port, dims, sizes, headersize, arraySizes);
                }
            }
            if (portIn instanceof RefinementPort
                    && portIn.getContainer() instanceof Refinement) {
                Refinement ref = (Refinement) portIn.getContainer();
                State state = ((ModalModel) ref.getContainer()).getController()
                        .currentState();
                if (state.getName().equals(ref.getName())) {
                    for (IOPort port : (List<IOPort>) portIn
                            .connectedPortList()) {
                        propagateHeader(port, dims, sizes, headersize,
                                arraySizes);
                    }

                }
            }
        }
        if (portIn.isInput()) {
            if (portIn.getContainer() instanceof CompositeActor) {
                for (Actor entity : (List<Actor>) ((CompositeActor) portIn
                        .getContainer()).entityList()) {
                    for (IOPort port : (List<IOPort>) entity.inputPortList()) {
                        IOPort port2 = port;
                        if (port2.connectedPortList().contains(portIn)) {
                            int sum = 1;
                            for (int size : sizes) {
                                sum *= size;
                            }
                            sum += headersize;

                            // If within Pthales domain, update of the port information
                            if (((CompositeActor) portIn.getContainer())
                                    .getDirector() instanceof PthalesDirector) {
                                PthalesIOPort.modifyPattern(portIn, "global",
                                        sum);
                            }
                            propagateHeader(port2, dims, sizes, headersize,
                                    arraySizes);
                        }
                    }
                }
            }
        }
    }

    /** Update actor iterations according to pattern and tiling information.
     * @param portIn port used in input for propages
     * @param sizes sizes used to compute iterations
     */
    public static void propagateIterations(IOPort portIn,
            LinkedHashMap<String, Integer> sizes) {
        // Iterations
        if (portIn.getContainer() instanceof PthalesCompositeActor) {
            // Iteration computation
            ((PthalesCompositeActor) portIn.getContainer())
            .computeSetIterations(portIn, sizes);

            // Once iterations are computed, output port can be computed
            for (IOPort portOut : (List<IOPort>) ((PthalesCompositeActor) portIn
                    .getContainer()).outputPortList()) {
                LinkedHashMap<String, Integer> outputs = PthalesIOPort
                        .getArraySizes(portOut);
                for (IOPort port : (List<IOPort>) portOut.connectedPortList()) {
                    propagateIterations(port, outputs);
                }
            }
        }

        if (portIn.isOutput()) {
            if (!(portIn instanceof RefinementPort)) {
                for (IOPort port : (List<IOPort>) portIn.connectedPortList()) {
                    propagateIterations(port, sizes);
                }
            }
            if (portIn instanceof RefinementPort
                    && portIn.getContainer() instanceof Refinement) {
                Refinement ref = (Refinement) portIn.getContainer();
                State state = ((ModalModel) ref.getContainer()).getController()
                        .currentState();
                if (state.getName().equals(ref.getName())) {
                    for (IOPort port : (List<IOPort>) portIn
                            .connectedPortList()) {
                        propagateIterations(port, sizes);
                    }

                }
            }
        }
        if (portIn.isInput()) {
            if (portIn.getContainer() instanceof CompositeActor) {
                for (Actor entity : (List<Actor>) ((CompositeActor) portIn
                        .getContainer()).entityList()) {
                    for (IOPort port : (List<IOPort>) entity.inputPortList()) {
                        IOPort port2 = port;
                        if (port2.connectedPortList().contains(portIn)) {
                            propagateIterations(port2, sizes);
                        }
                    }
                }
            }
        }
    }

    /** Check if data type is a structure.
     * If yes, gives the number of tokens needed to store all the data
     * By default, the return value is 1
     * @param port associated port
     */
    public static void setDataType(IOPort port) {
        Parameter p = (Parameter) port.getAttribute("dataType");
        if (p != null && port instanceof TypedIOPort) {
            ((TypedIOPort) port).setTypeEquals(BaseType.GENERAL);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or null if the parameter does not exist.
     *  @exception IllegalActionException If the parameter cannot be evaluated.
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
}
