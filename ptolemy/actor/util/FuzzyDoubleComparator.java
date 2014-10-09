/* Compare two Double objects with a fuzzy threshold.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Comparator;

///////////////////////////////////////////////////////////////////
//// FuzzyDoubleComparator

/**
 Compare two Double objects with respect to a fuzzy threshold.
 The threshold is set by setThreshold(). If the difference of the
 two double number is less than the threshold, then they are considered
 equal. The default value of the fuzzy threshold is 1e-10.
 <p>
 After Ptolemy II 4.1, this class is not used any more. Use
 {@link GeneralComparator} instead.

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 5.2
 @deprecated As Ptolemy II 4.1, use GeneralComparator instead.

 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
@Deprecated
public class FuzzyDoubleComparator implements Comparator {
    /** Construct a FuzzyDoubleComparator. The compare threshold is
     *  1e-10
     */
    public FuzzyDoubleComparator() {
        _threshold = 1.0e-10;
    }

    /** Construct a FuzzyDoubleComparator with the given threshold.
     *  @param threshold The threshold
     */
    public FuzzyDoubleComparator(double threshold) {
        _threshold = threshold;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Compare two objects according to a threshold.
     *  Return -1 if first &lt; second - threshold/2;
     *  return 1 if first &gt; second + threshold/2;
     *  return 0 otherwise.
     *
     *  <p>If any of the argument is not a Double object, a ClassCastException
     *  will be thrown.
     *  @param first The first Double object.
     *  @param second The second Double object.
     *  @return The comparison result, -1, 0, or 1.
     */
    @Override
    public int compare(Object first, Object second) {
        double firstValue = ((Double) first).doubleValue();
        double secondValue = ((Double) second).doubleValue();

        if (firstValue < secondValue - _threshold / 2.0) {
            return -1;
        } else if (firstValue > secondValue + _threshold / 2.0) {
            return 1;
        } else {
            return 0;
        }
    }

    /** Return the fuzziness threshold.
     *  @return The fuzziness threshold.
     *  @see #setThreshold(double)
     */
    public double getThreshold() {
        return _threshold;
    }

    /** Set the fuzziness threshold. The threshold is always positive.
     *  If the argument is negative, then its absolute value is taken.
     *  @param threshold The threshold.
     *  @see #getThreshold()
     */
    public void setThreshold(double threshold) {
        _threshold = Math.abs(threshold);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The threshold that controls the fuzziness. Default value 1e-10. */
    private double _threshold;
}
