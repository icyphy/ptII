/* A SchematicLink represents a link in a relation.

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

import java.util.Enumeration;
import collections.CircularList;

//////////////////////////////////////////////////////////////////////////
//// SchematicLink
/**

A SchematicLink represents a relation in a Ptolemy II schematic.

<!-- schematic relations will be parsed into class SchemticRelation -->
<!ELEMENT relation (link)*>
<!ATTLIST relation
name ID #REQUIRED>
<!ELEMENT link EMPTY>
<!ATTLIST link
name CDATA #REQUIRED>


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicLink extends PTMLObject {

    /**
     * Create a new SchematicLink object with the name "link".
     */
    public SchematicLink () {
        this("link");
    }

    /**
     * Create a new SchematicLink object with the given name.
     * The terminals associated with this object are created
     */
    public SchematicLink (String name) {
        super(name);
	_to = new Terminal("to_terminal");
	_from = new Terminal("from_terminal");
    }

    public Terminal getTo() {
	return _to;
    }

    public Terminal getFrom() {
	return _from;
    }

    /** 
     * Set the from terminal of this Link to the given non-null terminal.  
     * If the given terminal is null, then do nothing.
     */
    public void setFrom(Terminal term) {
	if(term != null) _from = term;
    }

    /** 
     * Set the to terminal of this Link to the given non-null terminal.  
     * If the given terminal is null, then do nothing.
     */
    public void setTo(Terminal term) {
	if(term != null) _to = term;
    }
   
    /** 
     * Return a string representation of the link
     */
    public String toString() {
        String str = getName() + "(" + _to.toString() + 
	    ", " + _from.toString() + ")";
        return str;
    }
    
    Terminal _to;
    Terminal _from;
    
}

