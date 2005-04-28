/* A base class for all GR actors
   Copyright (c) 2003-2005 The Regents of the University of California.
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

import ptolemy.domains.gr.lib.ViewScreen3D;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.j3d.Node;


//////////////////////////////////////////////////////////////////////////
//// GRActor

/**
   A base class for all GR actors. This is an abstract class that is never
   used as a standalone actor in a Ptolemy model. Subclasses of this actor
   include Geometry actors, Transform actors, Interaction actors, and the
   ViewScreen3D display actor.

   @see ptolemy.domains.gr.lib

   @author C. Fong
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (chf)
   @Pt.AcceptedRating Yellow (cxh)
*/
abstract public class GRActor3D extends GRActor {
    /** Create a new GRActor3D in the specified container with the specified
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
    public GRActor3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor. Derived GR Actors should override this method
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    protected void _addChild(Node node) throws IllegalActionException {
        throw new IllegalActionException(this,
                "GR domain actor cannot have children");
    }

    /** Return the Java3D node associated with this actor. Derived
     *  GR Actors should override this method.
     *
     *  @return The Java3D node associated with this actor
     */
    abstract protected Node _getNodeObject();

    /** Set the view screen that this actor is connected to.
     *  @exception IllegalActionException If the given actor is not a
     *  ViewScreen3D.
     */
    protected void _setViewScreen(GRActor actor) throws IllegalActionException {
        if (actor instanceof ViewScreen3D) {
            _viewScreen = (ViewScreen3D) actor;
        } else {
            throw new RuntimeException("Actor " + getClass().getName()
                    + " can only be used with a ViewScreen3D");
        }
    }

    /** Start the Java3D renderer. This method will be overridden by some
     *  derived GR Actors.
     */
    protected void _startRenderer() {
    }

    /** Stop the Java3D renderer. This method will be overridden by some
     *  derived GR Actors.
     */
    protected void _stopRenderer() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    // The actor displaying the scene, if there is one
    protected ViewScreen3D _viewScreen;
}
