/* A class that contains a set of constant time objects.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import ptolemy.actor.Director;


//////////////////////////////////////////////////////////////////////////
//// TimeConstants
/**
   A TimeConstants class contains a set of constant Time objects.
   
   @author Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public final class TimeConstants {
    
    /** Constructor that generate a set of useful constant time objects.
     *  @param container The director that contains this time object.
     */
    public TimeConstants(Director container) {
        _init(container);
        POSITIVE_INFINITY 
            = new Time(_container, Double.POSITIVE_INFINITY);

        NEGATIVE_INFINITY 
            = new Time(_container, Double.NEGATIVE_INFINITY);

        MAX_VALUE 
            = new Time(_container, Double.MAX_VALUE);

        MIN_VALUE 
            = new Time(_container, Double.MIN_VALUE);
        
        ZERO
            = new Time(_container, 0.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public  fields                   ////
    /** A constant holding the positive infinity of type
     *  <code>Time</code>. Its actual value depends on the time
     *  resolution, which defines the precision of this constant. 
     */
    public final Time POSITIVE_INFINITY;

    /** A constant holding the negative infinity of type
     *  <code>Time</code>. Its actual value depends on the time
     *  resolution, which defines the precision of this constant. 
     */
    public final Time NEGATIVE_INFINITY;
    
    /** A constant holding the largest positive finite value of type
     *  <code>Time</code>. Its actual value depends on the time
     *  resolution, which defines the precision of this constant. 
     */
    public final Time MAX_VALUE; 

    /** A constant holding the smallest positive nonzero value of type
     *  <code>Time</code>. Its actual value depends on the time
     *  resolution, which defines the precision of this constant. 
     */
    public final Time MIN_VALUE; 
    
    /** A constant holding the zero value of type
     *  <code>Time</code>. Although its time value is indepent of time
     *  resolution, the operations on this object depends on that. 
     */
    public final Time ZERO; 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the states. 
    private void _init(Director container) {
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The director that contains this time object.
    private Director _container;
}