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

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/** An iterator over Polyline2D. This class is private
 * to this package.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class PolylineIterator implements PathIterator {
    /** The transformed coordinates being iterated.
     */
    private double[] _coords;

    /** The current coordinate index.
     */
    private int _index = 0;

    /** Create a new iterator over the given polyline and
     * with the given transform. If the transform is null,
     * that is taken to be the same as a unit Transform.
     */
    public PolylineIterator(Polyline2D pl, AffineTransform at) {
        int count = pl.getVertexCount() * 2;
        _coords = new double[count];

        if (pl instanceof Polyline2D.Float) {
            Polyline2D.Float f = (Polyline2D.Float) pl;

            if (at == null || at.isIdentity()) {
                for (int i = 0; i < count; i++) {
                    _coords[i] = f._coords[i];
                }
            } else {
                at.transform(f._coords, 0, _coords, 0, count / 2);
            }
        } else {
            Polyline2D.Double d = (Polyline2D.Double) pl;

            if (at == null || at.isIdentity()) {
                System.arraycopy(d._coords, 0, _coords, 0, count);
            } else {
                at.transform(d._coords, 0, _coords, 0, count / 2);
            }
        }
    }

    /** Get the current segment
     */
    @Override
    public int currentSegment(double[] coords) {
        coords[0] = this._coords[_index];
        coords[1] = this._coords[_index + 1];
        return _index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
    }

    /** Get the current segment
     */
    @Override
    public int currentSegment(float[] coords) {
        coords[0] = (float) this._coords[_index];
        coords[1] = (float) this._coords[_index + 1];
        return _index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
    }

    /** Return the winding rule. This is WIND_NON_ZERO.
     */
    @Override
    public int getWindingRule() {
        return PathIterator.WIND_NON_ZERO;
    }

    /** Test if the iterator is done.
     */
    @Override
    public boolean isDone() {
        return _index >= _coords.length;
    }

    /** Move the iterator along by one point.
     */
    @Override
    public void next() {
        _index += 2;
    }
}
