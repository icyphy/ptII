/* A class that extends FIFOQueue for testing the clone method

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.actor.util.test;

import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.CalendarQueue;

///////////////////////////////////////////////////////////////////
//// FIFOQueue

/**

 This class is a test for the case whre FIFOQueue.clone() does not call super.clone().

  FindBugs reports this as:
  "clone method does not call super.clone()"

  <p>"This non-final class defines a clone() method that does
  not call super.clone(). If this class ("A") is extended by
  a subclass ("B"), and the subclass B calls super.clone(),
  then it is likely that B's clone() method will return an
  object of type A, which violates the standard contract for
  clone()."</p>

  <p>"If all clone() methods call super.clone(), then they are
  guaranteed to use Object.clone(), which always returns an
  object of the correct type."</p>

 @author Christopher
 @version $Id: DoubleCQComparator.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (liuj)

 */
public class FIFOQueueTest extends FIFOQueue {
    /** Return a clone of this object.
     *  @exception CloneNotSupportedException If thrown by
     *  a parent class.
     */   
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}