/* Controller for Factor Oracles.

 Copyright (c) 1999-2013 The Regents of the University of California.
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
 
import ptolemy.domains.modal.modal.ModalRefinement;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

// NOTE: This class duplicates code in ModalController and Refinement, but
// because of the inheritance hierarchy, there appears to be no convenient
// way to share the code.

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

 @author Edward A. Lee
 @version $Id: ModalRefinement.java 66458 2013-05-31 00:23:14Z cxh $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class OracleModel extends ModalRefinement{

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
    public OracleModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        //_init();
        
    }
    
    public OracleModel(CompositeEntity container, String name, 
            Object[] trainingSequence, double repetitionFactor, boolean pitch, boolean validate)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init(trainingSequence, repetitionFactor, pitch, validate);
        // TODO Auto-generated constructor stub
    }

    public void setController( FactorOracle f){
        _controller = f;
    }
   
    
///////////////////////////////////////////////////////////////////
////                         private methods                   ////
// Initialize the model.
    
    /** Create a Ptera controller within this Ptera modal model. A subclass may
     *  return a different controller to be used in this Ptera modal model.
     *
     *  @return A controller to be used in this modal model.
     *  @exception IllegalActionException If the Ptera modal model is
     *   incompatible with the controller.
     *  @exception NameDuplicationException If the name of the controller
     *   collides with a name already in the container.
     */
    protected FactorOracle _createController(Object[] trainingSequence, double repetitionFactor, boolean pitch, boolean validate)
            throws IllegalActionException, NameDuplicationException {
        //return new FactorOracle(this, "_Controller");
        return new FactorOracle(this, "_Controller", trainingSequence, 
                repetitionFactor, pitch, validate);
    }

    
    private void _init(Object[] trainingSequence, double repetitionFactor, boolean pitch, boolean validate) throws IllegalActionException,
            NameDuplicationException {
       
        // Set the director before changing directorClass, because changing the
        // latter causes a ChangeRequest to be issued (in superclass'
        // attributeChanged(), which then causes the expanded node in the actor
        // library to be collapsed immediately.
        
        setClassName("org.ptolemy.machineImprovisation.OracleModel");
        ComponentEntity controller = getEntity("_Controller");
        if (controller != null) {
            controller.setContainer(null);
        }
        _controller = _createController(trainingSequence,  
                repetitionFactor, pitch, validate);
    }
        }