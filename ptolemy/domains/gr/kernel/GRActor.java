/* GR Actor
 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import javax.media.j3d.*;

//////////////////////////////////////////////////////////////////////////
//// GRActor
/**
A base class for all GR actors. This is an abstract class that is never
used as a standalone actor in a Ptolemy model.

@author C. Fong
@version $Id$
*/
public class GRActor extends TypedAtomicActor {

    /** Create a new GRActor in the specified container with the specified
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
    public GRActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _allowAttributeChanges = false;
        _isSceneGraphInitialized = false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /*
     *  
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (_allowAttributeChanges == false ) {
            // FIXME: handle this
            // throw new IllegalActionException(
            //  "attribute change currently not allowed for this actor");
        }   
    }

    /*  Initialize the scene graph if it is not yet initialized.
     *  
     *  @exception IllegalActionException if an error occurs 
     *    in the scene graph initialization.
     */
    public void fire() throws IllegalActionException {
        if (!_isSceneGraphInitialized) {
            _makeSceneGraphConnection();
            _isSceneGraphInitialized = true;
        }
    }

    /*  Check whether the current director is a GRDirector. If not,
     *  throw an illegal action exception. Create the Java3D geometry
     *  and appearance for this actor.
     *
     *  @exception IllegalActionException if the current director
     *    is not a GRDirector
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (!(getDirector() instanceof GRDirector)) {
            throw new IllegalActionException(
                      "GR Actors can only be used under a GR Director");
        }
        _createModel();
    }

    
    /** Reset this actor back to uninitialized state to prepare for
     *  the next execution.
     *
     *  @exception IllegalActionException if the base class throws it
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isSceneGraphInitialized = false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /* Add the node argument as a child to the encapsulated Java3D node
     * in this actor. Derived GR Actors should override this method
     *
     * @exception IllegalActionException always thrown for this base class
     */
    protected void _addChild(Node node) throws IllegalActionException {
        throw new IllegalActionException("GR domain actor" + this +
                " cannot have children");
    }

    /*  Base method for creating the geometry and appearance of
     *  of a GR actor.  Derived GR actors should override this method
     *
     *  @exception IllegalActionException not thrown for this base class
     */
    protected void _createModel() throws IllegalActionException {
    }
    
    /* Return the Java3D node associated with this actor. Derived
     * GR Actors should override this method.
     * 
     * @return The Java3D node associated with this actor
     */
    protected Node _getNodeObject() {
        return null;
    }
    
    /* Base method for creating the scene graph. Derived GR Actor should
     * override this method
     *
     * @exception IllegalActionException always thrown for thsi base class
     */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        throw new IllegalActionException("GR domain actor" + this +
                " failed to make scene graph connection ");
    }

    /* Start the Java3D renderer. This method will be overridden by some
     * derived GR Actors.
     */
    protected void _startRenderer() {
    }


    /* Stop the Java3D renderer. This method will be overridden by some
     * derived GR Actors.
     */
    protected void _stopRenderer() {
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    // The root of the scene graph DAG, if there is one
    protected static GRActor _root;
    
    // Boolean variable to determine whether scene graph is initialized
    protected boolean _isSceneGraphInitialized;

    // Boolean variable to determine whether attribute changes are allowed   
    // For speed reasons, attribute changes may be disallowed in some models
    protected boolean _allowAttributeChanges;
}
