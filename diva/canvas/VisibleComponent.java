/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/** A visible component is a canvas component that is painted onto a
 * graphics context. This interface contains a small set of methods
 * that are required by any such object.  It is also used as a tagging
 * interface so that paint routines can tell whether certain objects
 * want to be painted or not.
 *
 * @version        $Revision$
 * @author         John Reekie
 * @rating Yellow
 */
public interface VisibleComponent extends CanvasComponent {

  /** Test the visibility flag of this object. Note that this flag
   *  does not indicate whether the object is actually visible on
   *  the screen, as one of its ancestors may not be visible.
   */
  public boolean isVisible ();

  /** Paint this object onto a 2D graphics object. Implementors
   * should first test if the visibility flag is set, and
   * paint the object if it is.
   */
  public void paint (Graphics2D g2d);

  /** Paint this object onto a 2D graphics object, within the given
   * region.  Implementors should first test if the visibility flag is
   * set, and paint the object if it is. The provided region can be
   * used to optimize the paint, but implementors can assume that the
   * clip region is correctly set beforehand.
   */
  public void paint (Graphics2D g, Rectangle2D region);

  /** Set the visibility flag of this object. If the flag is false,
   * then the object will not be painted on the screen.
   */
  public void setVisible (boolean flag);
}


