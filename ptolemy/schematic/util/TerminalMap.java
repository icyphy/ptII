/* A map between the schematic terminals and schematic ports 

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import java.util.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// TerminalMap
/**
A Terminal map represents the relationship between the terminals and 
ports of an entity.  It allows the terminal relationships stored within the
schematic to be mapped into relations between ports.

@author Steve Neuendorffer
@version $Id$
*/
public class TerminalMap {

    /**
     * Create a new TerminalMap object wtih no set attributes.
     */
    public TerminalMap () {
    }

    /**
     * Add a new port to the template. The port name must be unique within this
     * entity template.
     */
    public void addMap (String terminalName, String portName) {
        _map.put(terminalName, portName);
    }

    /**
     * Get the name of the port that is mapped to the terminal with this name.
     */
    public String getPort (String terminalName) {
        return (String) _map.get(terminalName);
    }

    /**
     * Get the names of the terminals that are mapped to this port.
     */
    public Enumeration getTerminals (String portName) {
        LinkedList list = new LinkedList();
        Iterator keys = _map.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if(_map.get(key).equals(portName))
                list.add(key);
        }
        return Collections.enumeration(list);
    }
        

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        return "TerminalMap";
    }

    private HashMap _map = new HashMap();
}






