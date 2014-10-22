/*
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
package ptolemy.caltrop.ddi;

import ptolemy.actor.Initializable;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AbstractDDI

/**
 @author Christopher Chang
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class AbstractDDI implements DDI {

    /** Do nothing. Cal doesn't support this.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
    }

    /** Return true. Most actors are written so that the prefire() and
     *  fire() methods do not change the state of the actor. Hence, for
     *  convenience, this base class by default returns true. An actor
     *  that does change state in prefire() or fire() must override
     *  this method to return false.
     *
     *  @return True.
     */
    @Override
    public boolean isFireFunctional() {
        return true;
    }

    /** Return true in this base class. By default, actors do not
     *  check their inputs to see whether they are known.  They assume
     *  they are known.  A derived class that can tolerate unknown
     *  inputs should override this method to return false.
     *
     *  @return True always in this base class.
     */
    @Override
    public boolean isStrict() {
        return true;
    }

    @Override
    public int iterate(int i) throws IllegalActionException {
        return 0;
    }

    /** Do nothing. Cal doesn't support this.
     *  @param initializable The object whose methods should be invoked.
     *  @see #addInitializable(Initializable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void stopFire() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public void wrapup() throws IllegalActionException {
    }
}
