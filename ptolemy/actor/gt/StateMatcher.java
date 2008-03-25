/*

@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StateMatcher extends State implements TypedActor {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public StateMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void addInitializable(Initializable initializable) {
    }

    public void fire() throws IllegalActionException {
    }

    public Director getDirector() {
        return null;
    }

    public Director getExecutiveDirector() {
        return null;
    }

    public FunctionDependency getFunctionDependency() {
        return null;
    }

    public Manager getManager() {
        return null;
    }

    public void initialize() throws IllegalActionException {
    }

    public List<?> inputPortList() {
        return _EMPTY_LIST;
    }

    public boolean isFireFunctional() {
        return false;
    }

    public boolean isStrict() {
        return false;
    }

    public int iterate(int count) throws IllegalActionException {
        return 0;
    }

    public Receiver newReceiver() throws IllegalActionException {
        return null;
    }

    public List<?> outputPortList() {
        return _EMPTY_LIST;
    }

    public boolean postfire() throws IllegalActionException {
        return false;
    }

    public boolean prefire() throws IllegalActionException {
        return false;
    }

    public void preinitialize() throws IllegalActionException {
    }

    public void removeInitializable(Initializable initializable) {
    }

    public void stop() {
    }

    public void stopFire() {
    }

    public void terminate() {
    }

    public List<?> typeConstraintList() throws IllegalActionException {
        return _EMPTY_LIST;
    }

    public void wrapup() throws IllegalActionException {
    }

    private static final List<?> _EMPTY_LIST = new LinkedList<Object>();
}
