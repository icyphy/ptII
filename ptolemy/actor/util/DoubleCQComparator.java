/* A sample implementation of the CQComparator that operates on instances
   of Double.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

//////////////////////////////////////////////////////////////////////////
//// DoubleCQComparator
/**
This class implements the CQComparator interface. It compares instances
of Double. Therefore, all arguments passed to its methods have to be of
type Double (or Double[] for the getBinWidth() method). If this is violated,
a ClassCastException will be thrown. This class is used to test the
CalendarQueue.

@author Lukito Muliadi
@version $Id$
@see CQComparator
@see CalendarQueue
*/

public class DoubleCQComparator implements CQComparator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare the two argument. Return a negative integer,
     *  zero, or a positive integer as the first argument is less than,
     *  equal to, or greater than the second.
     *  Both arguments have to be instances of Double, otherwise a
     *  ClassCastException will be thrown.
     *  @param object1 The first Double.
     *  @param object2 The second Double.
     *  @return A negative integer, zero, or a positive integer if the first
     *   argument is less than, equal to, or greater than the second.
     *  @exception ClassCastException If either argument is not an instance
     *   of Double.
     */
    public int compare(Object object1, Object object2) {

        Double a = (Double) object1;
        Double b = (Double) object2;

        if ( a.doubleValue() < b.doubleValue() )  {
            return -1;
        } else if ( a.doubleValue() > b.doubleValue() ) {
            return 1;
        } else {
            return 0;
        }
    }

    /** Given an entry, a zero reference, and a bin width, return a
     *  virtual bin number for the entry.  The virtual bin number is a
     *  quantized double.  The calculation performed is:
     *  <p>
     *  <i>(entry - zeroReference) / binWidth</i>,
     *  </p>
     *  with the result cast to long.
     *  If the arguments are not instances of Double, then a
     *  ClassCastException will be thrown.
     *  @param entry The entry.
     *  @param zeroReference The zero reference.
     *  @param binWidth The width of the bin.
     *  @return The virtual bin number for the entry, according to the
     *   zero reference and the bin width.
     *  @exception ClassCastException If the arguments are not instances of
     *   Double.
     */
    public long getVirtualBinNumber(Object entry) {
        return (long)((((Double)entry).doubleValue()
                - _zeroReference.doubleValue())/
                _binWidth.doubleValue());
    }

    /** Given an array of Double objects, find the appropriate bin
     *  width. By 'appropriate', the bin width is chosen such that on average
     *  the number of entry in all non-empty bins is equal to one.
     *  If the argument is null, return the default bin width, which is 1.0
     *  for this implementation.
     *
     *  @param entryArray An array of Double objects.
     *  @return The bin width.
     *  @exception ClassCastException If one of the array elements is not
     *   an instance of Double.
     */
    public void setBinWidth(Object[] entryArray) {
        if ( entryArray == null ) {
            // Reset to default.
            _binWidth = new Double(1.0);
            return;
        }

        double[] diff = new double[entryArray.length - 1];

        double average = 0;
        for (int i = 1; i < entryArray.length; ++i) {
            diff[i-1] = ((Double)entryArray[i]).doubleValue() -
                ((Double)entryArray[i-1]).doubleValue();
            average = average + diff[i-1];
        }
        average = average / diff.length;
        double effAverage = 0;
        int nEffSamples = 0;
        for (int i = 1; i < entryArray.length; ++i) {
            if ( diff[i-1] < 2*average ) {
                nEffSamples++;
                effAverage = effAverage + diff[i-1];
            }
        }
        // To avoid returning NaN or 0.0 for the width, if this is
        // the result, leave the bin width unchanged.
        if (effAverage == 0.0 || nEffSamples == 0) {
            return;
        }
        effAverage = effAverage / nEffSamples;
        _binWidth = new Double(3.0 * effAverage);
    }

    /** Set the zero reference, to be used in calculating the virtual
     *  bin number.
     *  @exception ClassCastException If the argument is not an instance
     *   of Double.
     */
    public void setZeroReference(Object zeroReference) {
        _zeroReference = (Double) zeroReference;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The bin width.
    private Double _binWidth = new Double(1.0);

    // The zero reference.
    private Double _zeroReference = new Double(0.0);
}
