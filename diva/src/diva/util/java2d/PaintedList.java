/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
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
 * @version        $Id$
 * @author         John Reekie
 */
public class PaintedList implements PaintedObject {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05

    /** The list of PaintedObjects
     */
    public ArrayList paintedObjects = new ArrayList();

    /** Add a new element to the list. The element is added to the
     * back of the list, and so will be painted over the top of
     * the elements already in the list.
     */
    public void add(PaintedObject po) {
        paintedObjects.add(po);
    }

    /** Get the bounding box of the list of painted objects.  Clients
     * should cache the bounding box if performance is important.
     */
    @Override
    public Rectangle2D getBounds() {
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
    @Override
    public void paint(Graphics2D g) {
        for (Iterator i = paintedObjects.iterator(); i.hasNext();) {
            PaintedObject o = (PaintedObject) i.next();
            o.paint(g);
        }
    }
}
