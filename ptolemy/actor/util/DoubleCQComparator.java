/* A sample implementation of CQComparator class using Double as sort key

 Copyright (c) 1998 The Regents of the University of California.
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

package pt.actor.util;

//import pt.data.*;
//import java.io.Serializable;
//import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DoubleCQComparator
/**
A sample implementation of Sortable interface using time stamp as sort key

@author Lukito Muliadi
@version $Id$
@see CQComparator
*/
public class DoubleCQComparator implements CQComparator{
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Compare its two argument for order. Return a negative integer,
     *  zero, or a positive integer as the first argument is less than,
     *  equal to, or greater than the second.
     *  <p>
     *  Both arguments have to be instances of Double, otherwise a
     *  ClassCastException exception will be thrown.
     * @param object1 the first Double argument
     * @param object2 the second Double argument
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     * @exception ClassCastException object1 and object2 has to be instances
     *            of Double
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

    /** Given a Double key, a Double zero reference, and a Double
        bin width, return the index of the bin containing that key.
     *  <p>
     *  If the arguments are not instances of the aforementioned types, 
     *  then a ClassCastException will be thrown.
     * 
     *  @param key the Double key
     *  @param zeroReference the Double zero reference.
     *  @param binWidth the width of the CalendarQueue bin
     *  @return the index of the bin containing that key, according to the
     *          zero reference, and bin width.
     *  @exception ClassCastException key, zeroReference, and binWidth have
     *             to be instances of Double.
     */
    public int getBinIndex(Object key, Object zeroReference, Object binWidth) {
        Double a = (Double) key;
        Double w = (Double) binWidth;
        Double zero = (Double) zeroReference;

        return (int)((a.doubleValue() - zero.doubleValue())/w.doubleValue());
    }


    /** Given an array of Double objects, find the appropriate bin 
     *  width. By appropriate, the bin width is chosen such that on average
     *  the number of entry in all non-empty bins is equal to one.
     *  If the argument is null, return the default bin width which is 1.0
     *  in this particular case.
     *  <p>
     *  If the argument is not an instance of Double[], then a
     *  ClassCastException will be thrown.
     *
     *  @param keyArray : an array of Double objects.
     *  @return SortBinWidth object representing quantization bin width 
     *  @exception ClassCastException keyArray has to be an array of Double
     *
     */
    public Object getBinWidth(Object[] keyArray) {

        if ( keyArray == null ) {
            return new Double(1.0);
        }

        double[] diff = new double[keyArray.length - 1];

        double average = 0;
        for (int i = 1; i < keyArray.length; ++i) {
             diff[i-1] = ((Double)keyArray[i]).doubleValue() - 
                 ((Double)keyArray[i-1]).doubleValue();
            average = average + diff[i-1];
        }
        average = average / diff.length;
        double effAverage = 0;
        int nEffSamples = 0;
        for (int i = 1; i < keyArray.length; ++i) {
            if ( diff[i-1] < 2*average ) {
                nEffSamples++;
                effAverage = effAverage + diff[i-1];
            }
        }
        effAverage = effAverage / nEffSamples;
        return new Double(3.0 * effAverage);
        
    }
}









