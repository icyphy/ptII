/* A GR Shape consisting of a sphere

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.lib;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Font;

import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;

//////////////////////////////////////////////////////////////////////////
//// TextString3D

/** An actor that encapsulates 3D text shapes in the GR domain

@author C. Fong
@version $Id$
@since Ptolemy II 1.0
*/
public class TextString3D extends GRShadedShape {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TextString3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        text = new Parameter(this, "text", new StringToken("Ptolemy"));
    }

    public Parameter text;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated text
     *
     *  @exception IllegalActionException If the value of some parameters
     *  can't be obtained
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        Font3D font3D = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                new FontExtrusion());
        Text3D textGeom = new Text3D(font3D, new String(_getText()));
        textGeom.setAlignment(Text3D.ALIGN_CENTER);
        _containedNode = new Shape3D();
        _containedNode.setGeometry(textGeom);
        _containedNode.setAppearance(_appearance);
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D Text3D
     *
     *  @return the Java3D Text3D
     */
    protected Node _getNodeObject() {
        return (Node) _containedNode;
    }


    private String _getText() throws IllegalActionException {
        return ((StringToken) text.getToken()).stringValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Shape3D _containedNode;
}
