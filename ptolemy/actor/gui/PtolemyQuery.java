/* A subclass of Query that provides a method to automatically set
 * a Variable when an entry is changed.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import java.util.*;
import ptolemy.data.expr.*;
import ptolemy.gui.*;
import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.Documentation;

//////////////////////////////////////////////////////////////////////////
//// PtolemyQuery
/**
This class provides a method to create a mapping from a Query entry to
a Variable. The Variable will be automatically set each time the
corresponding Query entity changes. To use this class, first add an entry to
the query, and then use the attachParameter method in this class
to associate a variable to that entry.

@author Brian K. Vogel
@version $Id$
*/
public class PtolemyQuery extends Query
    implements QueryListener, ValueListener {

    /** Construct a panel with no queries in it.
     */
    public PtolemyQuery() {
	super();
	this.addQueryListener(this);
	_parameters = new HashMap();
    }

    /** Attach a variable <i>var</i> to an entry, <i>entryName</i>,
     *  of a Query. After attaching the <i>var</i> to the entry,
     *  automatically set <i>var</i> when <i>entryName</i> changes.
     *  If <i>var</i> has previously been attached to an entry,
     *  then the old value is replaced with <i>entryName</i>.
     *  @param var The variable to attach to an entry.
     *  @param entryName The entry to attach the variable to.
     */
    public void attachParameter(Variable var, String entryName) {
	_parameters.put(entryName, var);
        var.addValueListener(this);
        String tip = Documentation.consolidate(var);
        if (tip != null) {
            setToolTip(entryName, tip);
        }
    }

    /** Set the Variable to the value of the Query entry. This
     *  method is called whenever an entry changes.
     *  @param name The entry that has changed.
     */
    public void changed(String name)  {
	// Check if the entry that changed is in the mapping.
	if (_parameters.containsKey(name)) {
	    // Set the variable.
            Variable var = (Variable)(_parameters.get(name));

            // Temporarily disable listening, so when we are notified
            // back, we ignore it.
            _listening = false;
            var.setExpression(stringValue(name));

            // Force evaluation, but ignore errors for now.
            try {
	      var.getToken();
            } catch (IllegalActionException ex) {}
            _listening = true;
	}
    }

    /** Notify this query that the value of the specified variable has
     *  changed.  This is called by an attached parameter when its
     *  value changes.
     *  @param variable The variable that has changed.
     */
    public void valueChanged(Variable variable) {
        if (_listening) {
            set(variable.getName(), variable.stringRepresentation());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap _parameters;
    private boolean _listening = true;
}
