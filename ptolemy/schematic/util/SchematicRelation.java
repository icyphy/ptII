/* A SchematicRelation represents a relation in a schematic.

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
//// SchematicRelation
/**

A SchematicRelation represents a relation in a Ptolemy II schematic.
It contains links, which specify the topology of the schematic.
Every link has a name that specifies the unique port within a schematic that
it is connected to.   A link name is formed by period concatenating the
entity name and the port name that the link is connected to, such as
"entity.port".
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
public class SchematicRelation extends PTMLObject {

    /**
     * Create a new SchematicRelation object.
     */
    public SchematicRelation () {
        this("relation");
    }

    /**
     * Create a new SchematicRelation object.
     */
    public SchematicRelation (String name) {
        super(name);
        _links = (CircularList) new CircularList();
	_terminals = (CircularList) new CircularList();
	setWidth(1);
    }

    /**
     * Add a new link to this relation. 
     */
    public void addLink (SchematicLink link) {
        _links.insertLast(link);
    }

    /**
     * Add a new terminal to this relation. 
     */
    public void addTerminal (Terminal term) {
        _terminals.insertLast(term);
    }

    /**
     * Test if this relation contains the given link.
     */
    public boolean containsLink (SchematicLink link) {
        return _links.includes(link);
    }

   /**
     * Test if this relation contains the given terminal.
     */
    public boolean containsTerminal (Terminal term) {
        return _terminals.includes(term);
    }

    /**
     * @return The width of this relation.
     */
    public int getWidth() {
        return _width;
    }

    /**
     * Return an enumeration over the links in this relation. \
     *
     * @return An Enumeration of SchematicLink
     */
    public Enumeration links () {
        return _links.elements();
    }

    /**
     * Return an enumeration over the terminals in this relation. \
     *
     * @return An Enumeration of Terminal
     */
    public Enumeration terminals () {
        return _terminals.elements();
    }

    /**
     * Remove the given link from this relation.
     */
    public void removeLink(SchematicLink link) {
        _links.removeOneOf(link);
    }

    /**
     * Remove the given link from this relation.
     */
    public void removeTerminal(Terminal term) {
        _terminals.removeOneOf(term);
    }

    /**
     * Set the width of this relation.
     */
    public void setWidth(int width) {
	_width = width;
    }

    /**
     * Return a string representing this relation.
     */
    public String toString() {
        Enumeration enumterminals = terminals();
        String str = getName() + "({";
        while(enumterminals.hasMoreElements()) {
            Terminal term = (Terminal) enumterminals.nextElement();
            str += "\n..." + term.toString();
        }
        str += "}{";
	Enumeration enumlinks = links();
        while(enumlinks.hasMoreElements()) {
	    SchematicLink link = 
		(SchematicLink) enumlinks.nextElement();
	    str += "\n..." + link.toString();
	}
	str += "})";
	return str;
    }

    int _width;
    CircularList _links;
    CircularList _terminals;
}

