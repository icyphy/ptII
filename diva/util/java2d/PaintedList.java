/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.util.java2d;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Iterator;

/** A class that collects a number of PaintedObjects. This is a
 * low-level utility class intended for constructing graphical
 * objects from some kind of external list.
 *
 * @version        $Revision$
 * @author         John Reekie
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public class PaintedList implements PaintedObject {

    /** The list of PaintedObjects
     */
    public ArrayList paintedObjects = new ArrayList();

    /** Add a new element to the list. The element is added to the
     * back of the list, and so will be painted over the top of
     * the elements already in the list.
     */
    public void add (PaintedObject po) {
        paintedObjects.add(po);
    }

    /** Get the bounding box of the list of painted objects.  Clients
     * should cache the bounding box if performance is important.
     */
    public Rectangle2D getBounds () {
        if (paintedObjects.size() == 0) {
            return new Rectangle2D.Double();
        }
        Iterator i = paintedObjects.iterator();
        PaintedObject o = (PaintedObject) i.next();
        Rectangle2D bounds = o.getBounds();

        while (i.hasNext()) {
            o = (PaintedObject) i.next();
            Rectangle2D.union(bounds, o.getBounds(), bounds);
        }
        return bounds;
    }

    /** Paint the list of objects. Objects are painted from the
     * <i>front</i> of the list backwards.
     */
    public void paint (Graphics2D g) {
        for (Iterator i = paintedObjects.iterator(); i.hasNext(); ) {
            PaintedObject o = (PaintedObject) i.next();
            o.paint(g);
        }
    }
}


