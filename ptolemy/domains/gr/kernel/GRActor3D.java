/* A base class for all 3D GR actors
 Copyright (c) 2003-2014 The Regents of the University of California.
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

import javax.media.j3d.Node;

import ptolemy.domains.gr.lib.ViewScreen3D;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// GRActor3D

/**
 An abstract base class for all 3D GR actors. Subclasses of this actor
 include geometry actors, transform actors, interaction actors, and the
 ViewScreen3D display actor.

 @see ptolemy.domains.gr.lib

 @author C. Fong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (liuxj)
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
     *  in this actor. Derived GR Actors should override this method if
     *  they aggregate graphical nodes to treat a group as a unit.
     *  @param node The node to add.
     *  @exception IllegalActionException Always thrown for this base class.
     */
    protected void _addChild(Node node) throws IllegalActionException {
        throw new IllegalActionException(this, "Cannot have child nodes.");
    }

    /** Return the Java3D node associated with this actor. Derived
     *  GR Actors should override this method.
     *  @return The Java3D node associated with this actor.
     */
    abstract protected Node _getNodeObject();

    /** Set the view screen that this actor is connected to.
     *  @param actor The view screen actor.
     *  @exception IllegalActionException If the given actor is not a
     *  ViewScreen3D.
     */
    @Override
    protected void _setViewScreen(GRActor actor) throws IllegalActionException {
        if (actor instanceof ViewScreen3D) {
            _viewScreen = (ViewScreen3D) actor;
        } else {
            throw new IllegalActionException(this, "Actor of class "
                    + actor.getClass().getName()
                    + " must be an instance of ViewScreen3D");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The actor displaying the scene, if there is one. */
    protected ViewScreen3D _viewScreen;
}
