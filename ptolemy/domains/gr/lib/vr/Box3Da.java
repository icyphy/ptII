/* A GR Shape consisting of a polyhedral box.

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

*/
package ptolemy.domains.gr.lib.vr;

import java.net.URL;

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.domains.gr.lib.GRShadedShape;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import javax.media.j3d.BranchGroup;


//////////////////////////////////////////////////////////////////////////
//// Box3D

/** This actor contains the geometry and appearance specifications for a
    box.  The output port is used to connect this actor to the Java3D scene
    graph. This actor will only have meaning in the GR domain.
    The parameters <i>xLength</i>, <i>yHeight</i>, and <i>zWidth</i>
    determine the dimensions of box. The rest of the parameters are
    described in the base class.

    @author Chamberlain Fong, Edward A. Lee
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Green (eal)
    @Pt.AcceptedRating Green (liuxj)
*/
public class Box3Da extends GRShadedShapea {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Box3Da(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xLength = new PortParameter(this, "xLength");
        xLength.setExpression("0.1");
        xLength.setTypeEquals(BaseType.DOUBLE);

        yHeight = new Parameter(this, "yHeight");
        yHeight.setExpression("0.1");
        yHeight.setTypeEquals(BaseType.DOUBLE);

        zWidth = new Parameter(this, "zWidth");
        zWidth.setExpression("0.1");
        zWidth.setTypeEquals(BaseType.DOUBLE);

        zWidth.moveToFirst();
        yHeight.moveToFirst();
        xLength.moveToFirst();

        // The flat parameter doesn't make much sense in this case.
        flat.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the box in the x-axis. This has type double
     *  with default 0.1.
     */
    public PortParameter xLength;

    /** The height of the box in the y-axis. This has type double
     *  with default 0.1.
     */
    public Parameter yHeight;

    /** The width of the box in the z-axis. This has type double
     *  with default 0.1.
     */
    public Parameter zWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the dimensions change, then update the box.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Check that a box has been previously created.
        if (_changesAllowedNow
                && ((attribute == xLength) || (attribute == yHeight)
                        || (attribute == zWidth))) {
            if (_scaleTransform != null) {
                float height = (float) (((DoubleToken) yHeight.getToken())
                        .doubleValue() / 2.0);

                float length = (float) (((DoubleToken) xLength.getToken())
                        .doubleValue() / 2.0);

                float width = (float) (((DoubleToken) zWidth.getToken())
                        .doubleValue() / 2.0);

                _scaleTransform.setScale(new Vector3d(length, height, width));

                // The following seems to be needed so the new scale
                // takes effect.
                ((TransformGroup) _containedNode).setTransform(_scaleTransform);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    public void initialize() throws IllegalActionException {
        super.initialize();
        _parameterPort = xLength.getPort();
      
    }
    
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
            _debug("Port width = " + _parameterPort.getWidth());
            _debug("Port hasToken = " + _parameterPort.hasToken(0));
            
        }
        
        if (_isSceneGraphInitialized){
            
            /** Updating the PortParameter, cannot call update()
             * because need to access token for condition 
             */
            if ((_parameterPort != null) && (_parameterPort.getWidth() > 0) && _parameterPort.hasToken(0)) {
                _doubleToken = (DoubleToken)(_parameterPort.get(0));
                xLength.setCurrentValue(_doubleToken);

                if (_debugging) {
                    _debug("Updated parameter value to: " + _doubleToken);
                }
            }else {
                if (_debugging) {
                    _debug("Did not update parameter");
                }   
            }
            
            if (_debugging) {
                _debug("Port value = " + _doubleToken.doubleValue());
            }
        	if (_doubleToken.doubleValue() > 0.0){
                
                /** Set _isSceneGraphInitialized back to false so 
                 * node can be sent. fire() will set it back to true
                 */
        		_isSceneGraphInitialized = false;
        		_createModel();
                 
                if (_debugging) {
                    _debug("Prefire returned true");
                    _debug("xLength = " + xLength);
                    
                }
               
                return true;   
        	}else {
        		
                if (_debugging) {
         
                    _debug("Prefire returned false");
					_debug("xLength = " + xLength);
                }
               
        		return false;
            }
        } 
        /** Should only reach this code during first prefire()
         * all other times _isSceneGraphInitialized should be true from 
         * fire method.
         */
        else {
            if (_debugging) {
                _debug("Prefire returned true");
                _debug("xLength = " + xLength);
            }
           
        	return true;   
        }
     
    }
    
    

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the box.
     *  @exception IllegalActionException If the value of some
     *   parameters can't be obtained.
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        _createBox();
    }

    /** Return the Java3D box.
     *  @return The Java3D box.
     */
    protected Node _getNodeObject() {
        //return _containedNode;
        return _branchGroup;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a box with the current parameter values.
     *  @exception IllegalActionException If the parameters are malformed.
     */
    private void _createBox() throws IllegalActionException {
        int primitiveFlags = Primitive.GENERATE_NORMALS;
        URL textureURL = texture.asURL();

        if ((textureURL != null) || _changesAllowedNow) {
            primitiveFlags = primitiveFlags | Primitive.GENERATE_TEXTURE_COORDS;
        }

        if (_changesAllowedNow) {
            // Sharing the geometry leads to artifacts when changes
            // are made at run time.
            primitiveFlags = primitiveFlags | Primitive.GEOMETRY_NOT_SHARED;
        }

        // Although it is completely undocument in Java3D, the "dimension"
        // parameters of the box are more like radii than like width,
        // length, and height. So we have to divide by two.
        float height = (float) (((DoubleToken) yHeight.getToken()).doubleValue() / 2.0);

        float length = (float) (((DoubleToken) xLength.getToken()).doubleValue() / 2.0);

        float width = (float) (((DoubleToken) zWidth.getToken()).doubleValue() / 2.0);

        if (_changesAllowedNow) {
            Box box = new Box(1.0f, 1.0f, 1.0f, primitiveFlags, _appearance);

            TransformGroup scaler = new TransformGroup();
            scaler.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            _scaleTransform = new Transform3D();
            _scaleTransform.setScale(new Vector3d(length, height, width));
            scaler.setTransform(_scaleTransform);
            scaler.addChild(box);
            _containedNode = scaler;
        } else {
            _containedNode = new Box(length, height, width, primitiveFlags,
                    _appearance);
            _scaleTransform = null;
        }
        _branchGroup = new BranchGroup();
        _branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        _branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        _branchGroup.addChild(_containedNode);
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** If changes to the dimensions are allowed, this is the transform
     *  that applies them.
     */
    private Transform3D _scaleTransform;

    /** The box. */
    private Node _containedNode;
    
    /** BranchGroup */
    private BranchGroup _branchGroup;
    
    private DoubleToken _doubleToken;
    
    private double _portValue;
    
    private ParameterPort _parameterPort;
}
