/* A FSMTransition connects two FSMStates.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMTransition
/**
A FSMTransition connects two FSMStates. It has a trigger event, a trigger
condition, and a set of trigger actions.

@author Brian K. Vogel
@version $Id$
*/
public class HDFFSMTransition extends FSMTransition {

    /** Construct a transition in the default workspace with an empty string
     *  as its name. Add the transition to the directory of the workspace.
     */
    public HDFFSMTransition() {
        super();
    }

    /** Construct a transition in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the transition to the workspace directory.
     *  @param workspace The workspace that will list the transition.
     */
    public HDFFSMTransition(Workspace workspace) {
	super(workspace);
    }

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public HDFFSMTransition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        HDFFSMTransition newobj = (HDFFSMTransition)super.clone(ws);
        newobj._stateVersion = -1;
        if (_trigger != null) {
            newobj._trigger = (VariableList)newobj.getAttribute("Trigger");
            newobj._te = (Variable)newobj._trigger.getAttribute("TriggerEvent");
            newobj._tc = (Variable)newobj._trigger.getAttribute("TriggerCondition");
            newobj._actions = (VariableList)newobj.getAttribute("Actions");
            newobj._localVarUpdates = (VariableList)newobj.getAttribute("LocalVarUpdates");
        }
        return newobj;
    }


}
