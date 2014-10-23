/* Controller for hierarchical factor oracle modal models.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package org.ptolemy.machineImprovisation;

import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

// The controller to be used within the hierarchical pitch factor oracle.

///////////////////////////////////////////////////////////////////
//// ModalRefinement

/**
 This modal model actor supports mirroring of its ports in its container
 (which is required to be a refinement of a state in a ModalModel).
 This in turn assures
 mirroring of ports in each of the refinements.
 <p>
 Note that this actor has no attributes of its own.
 Requests for attributes are delegated to the container.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class FactorOracleTop extends ModalModel {

    /**
     * Constructs a FactorOracleTop object.
     *
     * @param workspace  The workspace
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public FactorOracleTop(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init(null, 1.0, false, false);
    }

    /** Construct a modal controller with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public FactorOracleTop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init(null, 1.0, false, false);
    }

    /**
     * Constructs a FactorOracleTop object.
     *
     * @param container         The container.
     * @param name              Name of this actor.
     * @param trainingSequence  The training sequence as an Object array.
     * @param repetitionFactor  a double indicating the repetition factor on the forward links.
     * @param pitch             a boolean -- true if music pitch specifications are being used.
     * @param validate          a boolean -- true if pitches are subject to validation.
     * @exception NameDuplicationException ...
     * @exception IllegalActionException ...
     */
    public FactorOracleTop(CompositeEntity container, String name,
            Object[] trainingSequence, double repetitionFactor, boolean pitch,
            boolean validate) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        _init(trainingSequence, repetitionFactor, pitch, validate);
    }

    /**
     * Constructs a FactorOracleTop object.
     *
     * @param workspace         The workspace.
     * @param trainingSequence  The training sequence as an Object array.
     * @param repetitionFactor  a double indicating the repetition factor on the forward links.
     * @param pitch             a boolean -- true if music pitch specifications are being used.
     * @param validate          a boolean -- true if pitches are subject to validation.
     * @exception NameDuplicationException ...
     * @exception IllegalActionException ...
     */
    public FactorOracleTop(Workspace workspace, Object[] trainingSequence,
            double repetitionFactor, boolean pitch, boolean validate)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);
        _init(trainingSequence, repetitionFactor, pitch, validate);
    }

    /**
     * Specify the controller.
     * @param f The controller
     */
    public void setController(FactorOracle f) {
        _controller = f;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    //

    /**
     * Create a Factor Oracle to be used within the hierarchical FO
     * that is used for generation of improvised jazz "licks".
     *
     * @param trainingSequence the training sequence
     * @param repetitionFactor the repetition factor on the forward links
     * @param pitch boolean indicating a pitch oracle
     * @param validate boolean indicating whether the pitches should be validated
     * @return A controller to be used in the top level hierarchical FO
     * @exception IllegalActionException If the modal model is
     *   incompatible with the controller.
     * @exception NameDuplicationException If the name of the controller
     *   collides with a name already in the container.
     */
    protected FactorOracle _createController(Object[] trainingSequence,
            double repetitionFactor, boolean pitch, boolean validate)
                    throws IllegalActionException, NameDuplicationException {
        //return new FactorOracle(this, "_Controller");
        return new FactorOracle(this, "_Controller", trainingSequence,
                repetitionFactor, pitch, validate);
    }

    /** Override the base class to ensure that the _controller private
     *  variable is reset to the controller of the cloned object.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return The new Entity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FactorOracleTop newModel = (FactorOracleTop) super.clone(workspace);
        newModel._controller = (FactorOracle) newModel.getEntity("_Controller");

        try {
            // Validate the directorClass parameter so that the director
            // gets created in the clone.
            newModel.directorClass.validate();
            newModel.executeChangeRequests();
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(
                    "Failed to validate the director of the clone of "
                            + getFullName());
        }
        return newModel;
    }

    private void _init(Object[] trainingSequence, double repetitionFactor,
            boolean pitch, boolean validate) throws IllegalActionException,
            NameDuplicationException {

        setClassName("org.ptolemy.machineImprovisation.FactorOracleTop");
        ComponentEntity controller = getEntity("_Controller");
        if (controller != null) {
            controller.setContainer(null);
        }
        _controller = _createController(trainingSequence, repetitionFactor,
                pitch, validate);
    }
}
