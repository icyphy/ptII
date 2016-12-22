/* The public particle class, currently used by the Optimizer.

   Copyright (c) 2014 The Regents of the University of California.
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
   2
 */
package org.ptolemy.machineLearning.particleFilter;

import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// Particle
/**
 * The public particle class, currently used by the Optimizer.
 *
 *  @author  Ilge Akkaya
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class Particle {
    /** Construct a Particle.
     *  @param size The size of the particle
     */
    public Particle(int size) {
        _particleValue = new LinkedList<Double>();
        _ssSize = size;
    }

    /** Construct a Particle.
     *  @param p The particle
     */
    public Particle(Particle p) {
        _weight = p._weight;
        _ssSize = p._ssSize;
        _particleValue = new LinkedList<Double>();
        List<Double> temp = p.getValue();
        for (int i = 0; i < temp.size(); i++) {
            _particleValue.add(temp.get(i));
        }
    }

    /** Adjust the weight.
     *  If w is greater than 0.0, then
     *  the weight is set to weight/w.
     *  @param w The weight.
     *  @return true if the weight was adjusted.
     */
    public boolean adjustWeight(double w) {
        // normalize weight
        if (w > 0.0) {
            _weight = _weight / w;
        } else {
            return false;
        }
        return true;
    }

    /** Return the size of the partile.
     *  @return The size.
     */
    public int getSize() {
        return _ssSize;
    }

    /** Return the value.
     *  @return The value.
     *  @see #setValue(LinkedList)
     */
    public List<Double> getValue() {
        List<Double> values = new LinkedList<Double>();
        for (int i = 0; i < _particleValue.size(); i++) {
            values.add(_particleValue.get(i));
        }
        return values;
    }

    /** Return the weight.
     *  @return the weight.
     *  @see #setWeight(double)
     */
    public double getWeight() {
        return _weight;
    }

    /** Set the value.
     *  @param l The value
     *  @see #getValue()
     */
    public void setValue(LinkedList<Double> l) {
        _particleValue = new LinkedList<Double>();

        for (int i = 0; i < l.size(); i++) {
            _particleValue.add(l.get(i));
        }
    }

    /** Set the weight.
     *  @param weight the weight.
     *  @see #getWeight()
     */
    public void setWeight(double weight) {
        _weight = weight;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The values. */
    private List<Double> _particleValue;

    /** The size. */
    private int _ssSize;

    /** The weight. */
    private double _weight;
}
