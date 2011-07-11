/* A marker interface that aids to distinguish the Ptides directors from others.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

package ptolemy.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
////causalityMarker

/**
 This is a marker that stores list of a sets of dependent ports. Each set
 implies a pure event produced in this actor is causally related to events
 coming into the ports in this set. The reason to have a list of sets is
 that an actor may produce different pure events which are causally related
 to a subset of the input ports.
 <p>
 The set of ports is very closely related to equivalence class. The current
 understanding of this relationship (which may not be correct) is summarized
 as follows:
 <p>
 1. If two ports belong to the same set, then they should also belong to the
 same finite equivalence class.
 2. If two ports belong to the same finite equivalence class, they should also
 be in the same set.
 <p>
 From this summarization, it seems the causality marker is exactly the same as
 equivalence class. In the future, this class may be integrated as a part of
 the CausalityInterface. This may require ports to be created for pure events.

 @author Jia Zou
 @version $Id$
 @since Ptolemy II 8.0

 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating Red (jiazou)
*/

public class CausalityMarker extends Attribute {

    /** Construct a CausalityMarker. This creates a list of sets of ports.
     *  @param container The container for this marker
     *  @param name The name of this marker.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public CausalityMarker(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        causalityMarker = new ArrayList<Set<Port>>();
    }

    /** Add a set of ports into the list.
     *  @param dependentPorts set of ports to be added.
     */
    public void addDependentPortSet(Set<Port> dependentPorts) {
        causalityMarker.add(dependentPorts);
    }

    /** Check if this port is contained in the list of sets of ports.
     *  @param port to be checked.
     *  @return true if this port is contained in causality marker. Else
     *  return false.
     *  @exception IllegalActionException
     */
    public boolean containsPort(Port port) throws IllegalActionException {
        for (Set portSet : causalityMarker) {
            if (portSet.contains(port)) {
                return true;
            }
        }
        return false;
    }

    /** The list of sets of ports in the causality marker.
     */
    public List<Set<Port>> causalityMarker;
}
