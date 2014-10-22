/* A base class for all GR actors
 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.kernel;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// GRActor

/**
 A base class for all GR actors. This is an abstract class that is never
 used as a standalone actor in a Ptolemy model. Subclasses of this actor
 include Geometry actors, Transform actors, Interaction actors, and the
 ViewScreen display actor.

 @see ptolemy.domains.gr.lib

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (chf)
 @Pt.AcceptedRating Yellow (cxh)
 */
abstract public class GRActor extends TypedAtomicActor {
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
    ////                         public methods                    ////

    /** Initialize the scene graph if it is not yet initialized.
     *
     *  @exception IllegalActionException If an error occurs
     *    during the scene graph initialization.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!_isSceneGraphInitialized) {
            _makeSceneGraphConnection();
            _isSceneGraphInitialized = true;
        }
    }

    /** Check whether the current director is a GRDirector. If not,
     *  throw an illegal action exception.
     *
     *  @exception IllegalActionException If the current director
     *    is not a GRDirector.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isSceneGraphInitialized = false;

        /*if (!(getDirector() instanceof GRDirector)) {
         throw new IllegalActionException(this,
         "GR Actors can only be used under a GR Director");
         }*/
    }

    /** Reset this actor back to uninitialized state to prepare for
     *  the next execution.
     *
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isSceneGraphInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Setup the scene graph connections of this actor. Derived GR Actors
     *  should override this method.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    abstract protected void _makeSceneGraphConnection()
            throws IllegalActionException;

    /** Set the view screen that the actor is connected to.
     *  @param actor The actor.
     *  @exception IllegalActionException Thrown if there is a problem
     *  setting the view screen.
     */
    abstract protected void _setViewScreen(GRActor actor)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Indicator of whether the scene graph is initialized. */
    protected boolean _isSceneGraphInitialized;

    /** Boolean variable to determine whether attribute changes are
     * allowed. For speed reasons, attribute changes may be disallowed in
     * some models
     */
    protected boolean _allowAttributeChanges;
}
