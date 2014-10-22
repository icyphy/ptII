/* WatchDog timer for tests

 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package util.testsuite;

//////////////////////////////////////////////////////////////////////////
//// WatchDog

/** This class creates a Timer that calls System.exit() after
 a certain amount of time.

 @deprecated Use ptolemy.util.test.WatchDog instead.  This class is
 outside the ptolemy hierarchy.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@Deprecated
public class WatchDog extends ptolemy.util.test.WatchDog {
    /** Create a timer that will go off after timeToDie milliseconds.
     *  @param timeToDie The time in millesconds when the timer will
     *  go off.
     *  @deprecated Use ptolemy.util.test.WatchDog instead.
     */
    @Deprecated
    public WatchDog(final long timeToDie) {
        super(timeToDie);
    }

    /** Cancel the currently pending watchdog.
     *  @deprecated Use ptolemy.util.test.WatchDog instead.
     */
    @Deprecated
    @Override
    public void cancel() {
        super.cancel();
    }

    /** Determine whether the JVM will exit when the time interval
     *  has passed.  This method is used for testing this class.
     *  @param exitOnTimeOut True if the JVM will exit when
     *  the time interval has passed.
     *  @deprecated Use ptolemy.util.test.WatchDog instead.
     */
    @Deprecated
    @Override
    public void setExitOnTimeOut(boolean exitOnTimeOut) {
        super.setExitOnTimeOut(exitOnTimeOut);
    }
}
