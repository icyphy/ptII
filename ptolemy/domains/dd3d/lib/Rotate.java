/* An actor that rotates the input 3D shape

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.domains.dd3d.kernel.*;
import ptolemy.domains.dt.kernel.DTDebug;

import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// Rotate

/** Conceptually, this actor takes 3D geometry in its input and produces a rotated
version in its output. In reality, this actor encapsulates a Java3D TransformGroup
which is converted into a node in the resulting Java3D scene graph. This actor will
only have meaning in the DD3D domain. Scaling can be done uniformly or non-uniformly.
Uniform scaling scales the input geometry equally in all directions. Uniform scaling 
is done through modification of the <i>scaleFactor</i> parameter. Non-uniform scaling
involves preferential scaling of the input geometry in a specified Cartesian axis. 
Non-uniform scaling is done through modification of the <i>xScale<i>, <i>yScale<i/>,
and <i>zScale<i/> parameters. 
@author C. Fong
*/
public class Rotate extends Transform {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Rotate(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

	    angle = new TypedIOPort(this, "angle",true,false);

   	    angle.setTypeEquals(BaseType.DOUBLE);
	    initialAngle = new Parameter(this, "initialAngle", new DoubleToken(0.0));
	    axisDirectionX = new Parameter(this, "axisDirectionX", new DoubleToken(0.0));
  	    axisDirectionY = new Parameter(this, "axisDirectionY", new DoubleToken(1.0));
  	    axisDirectionZ = new Parameter(this, "axisDirectionZ", new DoubleToken(0.0));
  	    baseX = new Parameter(this, "baseX", new DoubleToken(0.0));
  	    baseY = new Parameter(this, "baseY", new DoubleToken(0.0));
  	    baseZ = new Parameter(this, "baseZ", new DoubleToken(0.0));
  	    
  	    Transform3D _rotation = new Transform3D();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    public TypedIOPort angle;
   
    public Parameter initialAngle; 
    public Parameter axisDirectionX;
    public Parameter axisDirectionY;
    public Parameter axisDirectionZ;
    
    public Parameter baseX;
    public Parameter baseY;
    public Parameter baseZ;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Rotate newobj = (Rotate)super.clone(workspace);
        return newobj;
    }
    
    
    /**
     */
    public void initialize() throws IllegalActionException {

        double _xAxis = ((DoubleToken) axisDirectionX.getToken()).doubleValue();
        double _yAxis = ((DoubleToken) axisDirectionY.getToken()).doubleValue();
        double _zAxis = ((DoubleToken) axisDirectionZ.getToken()).doubleValue();
        double _baseX = ((DoubleToken) baseX.getToken()).doubleValue();
        double _baseY = ((DoubleToken) baseY.getToken()).doubleValue();
        double _baseZ = ((DoubleToken) baseZ.getToken()).doubleValue();
        double originalAngle = ((DoubleToken) initialAngle.getToken()).doubleValue();

        _topTranslate = new TransformGroup();
        _middleRotate = new TransformGroup();
        _middleRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _bottomTranslate = new TransformGroup();
        
        Transform3D topTransform = new Transform3D();
        topTransform.setTranslation(new Vector3d(_baseX,_baseY,_baseZ));
        _topTranslate.setTransform(topTransform);
        
        Quat4d quat = new Quat4d();
        quat.set(new AxisAngle4d(_xAxis,_yAxis,_zAxis,originalAngle));
        _rotation.set(quat);
        _middleRotate.setTransform(_rotation);
        
        Transform3D bottomTransform = new Transform3D();
        bottomTransform.setTranslation(new Vector3d(-_baseX,-_baseY,-_baseZ));
        _bottomTranslate.setTransform(bottomTransform);
        _topTranslate.addChild(_middleRotate);
        _middleRotate.addChild(_bottomTranslate);
    }
    
    /**
     */
    public void fire() throws IllegalActionException {
        if (angle.getWidth() != 0) {
            if (angle.hasToken(0)) {
                double in = ((DoubleToken)angle.get(0)).doubleValue();
                double originalAngle = ((DoubleToken) initialAngle.getToken()).doubleValue();
                _xAxis = ((DoubleToken) axisDirectionX.getToken()).doubleValue();
                _yAxis = ((DoubleToken) axisDirectionY.getToken()).doubleValue();
                _zAxis = ((DoubleToken) axisDirectionZ.getToken()).doubleValue();
        
                Quat4d quat = new Quat4d();
                quat.set(new AxisAngle4d(_xAxis,_yAxis,_zAxis,in+originalAngle));
                _rotation.set(quat);
                _middleRotate.setTransform(_rotation);
            }
        }
    }
    
    /**
     */
    public void addChild(Node node) {
        _bottomTranslate.addChild(node);
    }
    
    /**
     */
    public Node getNodeObject() {
        return (Node) _topTranslate;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _xAxis;
    private double _yAxis;
    private double _zAxis;
    private double _baseX;
    private double _baseY;
    private double _baseZ;
    private TransformGroup _topTranslate;
    private TransformGroup _middleRotate;
    private TransformGroup _bottomTranslate;
    private Transform3D _rotation;
}
