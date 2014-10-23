/* An aggregation of typed actors, with ports using ArrayOL information.

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////PthalesCompositeActor

/**
 A composite actor imposes the use of PthalesIOPort
 as they contain needed values used by PThalesDirector.
 A PthalesCompositeActor can contain actors from different model (as SDF),
 but the port must be a PthalesIOPort, because of the ArrayOL parameters.

 @author R&eacute;mi Barr&egrave;re
 @see ptolemy.actor.TypedIOPort
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesCompositeActor extends TypedCompositeActor {
    /** Construct a PthalesCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesCompositeActor() throws NameDuplicationException,
    IllegalActionException {
        super();

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    /** Construct a PthalesCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PthalesCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    /** Construct a PthalesCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesCompositeActor(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is PthalesCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as PthalesCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be PthalesCompositeActor.
        setClassName("ptolemy.domains.pthales.lib.PthalesCompositeActor");

        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of times this actor is fired.
     *  The initial default value is an array with one element,
     *  the integer "1".
     */
    public Parameter repetitions;

    ///////////////////////////////////////////////////////////////////
    ////                     public method                         ////

    /** Compute iteration number of the actor,
     * which is the number of times internal entities are called.
     * and set corresponding attribute.
     * @param portIn the input port
     * @param sizes dimensions sizes of the input
     * @return An array of iterations.  The length of the
     * array is equal to the tiling on the input port.
     */
    public Integer[] computeIterations(IOPort portIn,
            LinkedHashMap<String, Integer> sizes) {

        List<Integer> repetition = new ArrayList<Integer>();

        // Simple example : pattern is fixed and iterations
        LinkedHashMap<String, Integer[]> patternDims = PthalesIOPort
                .getInternalPattern(portIn);
        LinkedHashMap<String, Integer[]> tilingDims = PthalesIOPort
                .getTiling(portIn);
        LinkedHashMap<String, Integer[]> baseDims = PthalesIOPort
                .getBase(portIn);

        // Input array dimension
        Object[] dims = tilingDims.keySet().toArray();

        for (int i = 0; i < tilingDims.size(); i++) {

            // No tiling => no repetition on dimension => next dim
            if (tilingDims.get(dims[i]) != null) {

                int nb = 1;
                int jump = 1;

                if (patternDims.get(dims[i]) != null) {

                    nb = (patternDims.get(dims[i])[0] - 1)
                            * patternDims.get(dims[i])[1] + 1;

                    if (baseDims != null && baseDims.containsKey(dims[i])) {
                        nb += baseDims.get(dims[i])[0];
                    }

                    jump = tilingDims.get(dims[i])[0];
                }
                int val = (int) Math.floor((sizes.get(dims[i]) - nb)
                        / (double) jump) + 1;

                repetition.add(val);
            }
        }

        return repetition.toArray(new Integer[repetition.size()]);
    }

    /** Compute iteration number of the actor,
     * which is the number of times internal entities are called.
     * and set corresponding attribute
     * @param portIn the input port
     * @param sizes dimensions sizes of the input
     */
    public void computeSetIterations(IOPort portIn,
            LinkedHashMap<String, Integer> sizes) {
        setIterations(computeIterations(portIn, sizes));
    }

    /** Set iteration number of the actor,
     * which is the number of times internal entities are called.
     * and set corresponding attribute
     * @param repetition The number of times this actor is fired.
     */
    public void setIterations(Integer[] repetition) {
        StringBuffer repetitionStringBuffer = new StringBuffer("{");

        for (int i = 0; i < repetition.length; i++) {
            repetitionStringBuffer.append(repetition[i]);
            if (i < repetition.length - 1) {
                repetitionStringBuffer.append(",");
            }
        }

        repetitionStringBuffer.append("}");

        Attribute repetitions = getAttribute(PthalesCompositeActor._REPETITIONS);
        if (repetitions != null && repetitions instanceof Parameter) {
            ((Parameter) repetitions).setExpression(repetitionStringBuffer
                    .toString());
        }
    }

    /** Create a new PthalesIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return A new PthalesIOPort.
     *  @exception NameDuplicationException If this actor already has a
     *   port with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();

            TypedIOPort port = new TypedIOPort(this, name, false, false);
            PthalesIOPort.initialize(port);

            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set specific attributes common to all Pthales composite actors.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected void _initialize() throws IllegalActionException,
    NameDuplicationException {

        if (getAttribute("_iconDescription") != null) {
            ((SingletonConfigurableAttribute) getAttribute("_iconDescription"))
            .setExpression("<svg width=\"60\" height=\"40\"><polygon points=\"2.54167,37.2083 13.9198,20.0125 2.54167,2.45833 46.675,2.45833 57.7083,20.0125 47.0198,37.2083\"style=\"fill:#c0c0ff;stroke:#000080;stroke-width:1\"/><text x=\"18\" y=\"31\" style=\"fill:#000080;font-size:35\">H</text></svg>");
        }
        if (getAttribute("repetitions") == null) {
            repetitions = new Parameter(this, "repetitions");
            repetitions.setExpression("{1}");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the total repetitions parameter. */
    protected static String _REPETITIONS = "repetitions";
}
