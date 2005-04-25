/*
 *	@(#)MouseRotate.java 1.1 00/09/20 15:49:51
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package vendors.vr;

import com.sun.j3d.utils.behaviors.mouse.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

/**
 * This is a modified version of the
 * com.sun.j3d.utils.behaviors.mouse.MouseRotate class which filters the
 * mouse events to only process the last mouse event.
 *
 * MouseRotate is a Java3D behavior object that lets users control the
 * rotation of an object via a mouse.
 * <p>
 * To use this utility, first create a transform group that this
 * rotate behavior will operate on. Then,
 *<blockquote><pre>
 *
 *   MouseRotate behavior = new MouseRotate();
 *   behavior.setTransformGroup(objTrans);
 *   objTrans.addChild(behavior);
 *   behavior.setSchedulingBounds(bounds);
 *
 *</pre></blockquote>
 * The above code will add the rotate behavior to the transform
 * group. The user can rotate any object attached to the objTrans.
 */

public class MouseRotate extends MouseBehavior {
    double x_angle, y_angle;
    double x_factor = .03;
    double y_factor = .03;

  private MouseBehaviorCallback callback = null;


  /**
   * Creates a rotate behavior given the transform group.
   * @param transformGroup The transformGroup to operate on.
   */
  public MouseRotate(TransformGroup transformGroup) {
    super(transformGroup);
  }

  /**
   * Creates a default mouse rotate behavior.
   **/
  public MouseRotate() {
      super(0);
  }

  /**
   * Creates a rotate behavior.
   * Note that this behavior still needs a transform
   * group to work on (use setTransformGroup(tg)) and
   * the transform group must add this behavior.
   * @param flags interesting flags (wakeup conditions).
   */
  public MouseRotate(int flags) {
      super(flags);
   }

  public void initialize() {
    super.initialize();
    x_angle = 0;
    y_angle = 0;
    if ((flags & INVERT_INPUT) == INVERT_INPUT) {
       invert = true;
       x_factor *= -1;
       y_factor *= -1;
    }
  }

  /**
   * Return the x-axis movement multipler.
   **/

  public double getXFactor() {
    return x_factor;
  }

  /**
   * Return the y-axis movement multipler.
   **/

  public double getYFactor() {
    return y_factor;
  }

  /**
   * Set the x-axis amd y-axis movement multipler with factor.
   **/

  public void setFactor( double factor) {
    x_factor = y_factor = factor;

  }

  /**
   * Set the x-axis amd y-axis movement multipler with xFactor and yFactor
   * respectively.
   **/

  public void setFactor( double xFactor, double yFactor) {
    x_factor = xFactor;
    y_factor = yFactor;
  }

  public void processStimulus (Enumeration criteria) {
      WakeupCriterion wakeup;
      AWTEvent[] event;
      int id;
      int dx, dy;

      while (criteria.hasMoreElements()) {
         wakeup = (WakeupCriterion) criteria.nextElement();
         if (wakeup instanceof WakeupOnAWTEvent) {
            event = ((WakeupOnAWTEvent)wakeup).getAWTEvent();

	    // Only process the last drag event to avoid queuing up
	    // lots of events when the per-frame compute time is high
	    MouseEvent lastDragEvent = null;
            for (int i=0; i<event.length; i++) {
	      processMouseEvent((MouseEvent) event[i]);

	      if (((buttonPress)&&((flags & MANUAL_WAKEUP) == 0)) ||
		  ((wakeUp)&&((flags & MANUAL_WAKEUP) != 0))){

		id = event[i].getID();
		if ((id == MouseEvent.MOUSE_DRAGGED) &&
		    !((MouseEvent)event[i]).isMetaDown() &&
		    !((MouseEvent)event[i]).isAltDown()){
		    lastDragEvent = (MouseEvent) event[i];
		} else if (id == MouseEvent.MOUSE_PRESSED) {
		    x_last = ((MouseEvent)event[i]).getX();
		    y_last = ((MouseEvent)event[i]).getY();
		}
	      }
	    }
	    if (lastDragEvent != null) {

                  x = lastDragEvent.getX();
                  y = lastDragEvent.getY();

                  dx = x - x_last;
                  dy = y - y_last;

		  if (!reset){
		    x_angle = dy * y_factor;
		    y_angle = dx * x_factor;

		    transformX.rotX(x_angle);
		    transformY.rotY(y_angle);

		    transformGroup.getTransform(currXform);

		    //Vector3d translation = new Vector3d();
		    //Matrix3f rotation = new Matrix3f();
		    Matrix4d mat = new Matrix4d();

		    // Remember old matrix
		    currXform.get(mat);

		    // Translate to origin
		    currXform.setTranslation(new Vector3d(0.0,0.0,0.0));
		    if (invert) {
			currXform.mul(currXform, transformX);
			currXform.mul(currXform, transformY);
		    } else {
			currXform.mul(transformX, currXform);
			currXform.mul(transformY, currXform);
		    }

		    // Set old translation back
		    Vector3d translation = new
		      Vector3d(mat.m03, mat.m13, mat.m23);
		    currXform.setTranslation(translation);

		    // Update xform
		    transformGroup.setTransform(currXform);

		    transformChanged( currXform );

                    if (callback!=null)
                        callback.transformChanged( MouseBehaviorCallback.TRANSLATE,
                                               currXform );


		  }
		  else {
		    reset = false;
		  }

                  x_last = x;
                  y_last = y;
	      }
         }
      }

      wakeupOn (mouseCriterion);

   }

  /**
    * Users can overload this method  which is called every time
    * the Behavior updates the transform
    *
    * Default implementation does nothing
    */
  public void transformChanged( Transform3D transform ) {
  }

  /**
    * The transformChanged method in the callback class will
    * be called every time the transform is updated
    */
  public void setupCallback( MouseBehaviorCallback callback ) {
      this.callback = callback;
  }
}
