/* A GR Shape consisting of a cylinder with a circular base

Copyright (c) 1998-2005 The Regents of the University of California.
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

@ProposedRating Red (chf)
@AcceptedRating Red (chf)
*/
package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.gr.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import com.sun.j3d.utils.geometry.*;

import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// Cylinder3D

/** This actor contains the geometry and appearance specifications for a GR
    cylinder.  The output port is used to connect this actor to the Java3D scene
    graph. This actor will only have meaning in the GR domain.


    @author C. Fong
*/
public class Led extends Box3D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Led(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        emissivity = new TypedIOPort(this, "emissivity");
        emissivity.setInput(true);
        emissivity.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort emissivity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    protected void _createAppearance() {
        super._createAppearance();
        _material = new Material();
        _material.setCapability(Material.ALLOW_COMPONENT_READ
                    | Material.ALLOW_COMPONENT_WRITE);
        _appearance = new Appearance();
        _appearance.setCapability(Appearance.ALLOW_MATERIAL_READ
                    | Appearance.ALLOW_MATERIAL_WRITE);

        _material.setDiffuseColor(_color);

        if (_shine > 1.0) {
            _material.setSpecularColor(whiteColor);
            _material.setShininess(_shine);
        } else {
            _material.setSpecularColor(_color);
        }

        /*
          if (_transparency > 0.0f) {
          TransparencyAttributes feature1 = new TransparencyAttributes();
          feature1.setTransparencyMode(feature1.BLENDED);
          feature1.setTransparency(_transparency);
          _appearance.setTransparencyAttributes(feature1);

          // Set up the polygon attributes
          PolygonAttributes feature2 = new PolygonAttributes();
          feature2.setCullFace(feature2.CULL_NONE);
          _appearance.setPolygonAttributes(feature2);
          }*/
        /*
          if (_wireframeMode) {
          PolygonAttributes feature1 = new PolygonAttributes();
          feature1.setPolygonMode(feature1.POLYGON_LINE);
          feature1.setCullFace(feature1.CULL_NONE);
          _appearance.setPolygonAttributes(feature1);
          }*/
        //_material.setEmissiveColor(0.0f,0.9f,0.0f);
        _appearance.setMaterial(_material);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated cylinder
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        System.out.println("create model");
        _containedNode.setCapability(Shape3D.ALLOW_APPEARANCE_READ
                    | Shape3D.ALLOW_APPEARANCE_WRITE);
    }

    public boolean prefire() throws IllegalActionException {
        boolean returnValue = super.prefire();
        returnValue = true;
        return returnValue;
    }

    /** Check the input ports for translation inputs.  Convert the translation
     *  tokens into a Java3D transformation.
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    public void fire() throws IllegalActionException {
        super.fire();

        if (emissivity.getWidth() != 0) {
            if (emissivity.hasToken(0)) {
                double in = ((DoubleToken) emissivity.get(0)).doubleValue();
                float inf = (float) in;

                //System.out.println(" firing led");
                _material.setEmissiveColor(0.0f, inf, 0.0f);
                _material.setDiffuseColor(0.0f, inf, 0.0f);
                _appearance.setMaterial(_material);

                //_containedNode.setAppearance(_appearance);
                //_appearance.getMaterial().setEmissiveColor(inf, inf, inf);
                //containedNode.setAppearance(_appearance);
                //unLitBoxTG.addChild(new Box(0.1f, 0.1f, 0.1f, Box.GENERATE_NORMALS, redGlowMat));
            }
        }
    }
}
