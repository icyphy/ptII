/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import diva.canvas.Figure;
import java.awt.Shape;

/**
 * An interface that defines the setShape() method. This method must
 * be implemented by any figure that can have a PathManipulator
 * wrapped around it.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface ShapedFigure extends Figure {
    /**
     * Set the shape of this figure. This method is used by
     * PathManipulators to implement reshaping of figures.
     */
    public void setShape (Shape s);
}


