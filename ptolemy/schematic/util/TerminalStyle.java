/* A TerminalStyle represents a collection of terminals

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

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;

//////////////////////////////////////////////////////////////////////////
//// TerminalStyle
/**

An TerminalStyle is the graphical representation of a schematic entity.
TerminalStyles are stored hierarchically in TerminalStyle libraries.  
Every TerminalStyle has a 
name, along with a graphical representation.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class TerminalStyle extends PTMLObject {

    /**
     * Create a new TerminalStyle with the name "TerminalStyle". 
     * By default, the TerminalStyle contains no graphic
     * representations.
     */
    public TerminalStyle () {
        this("TerminalStyle");
    }

    /**
     * Create a new TerminalStyle with the given name.
     * By default, the TerminalStyle contains no graphic
     * representations.
     */
    public TerminalStyle (String name) {
        super(name);
        _terminals = (NamedList) new NamedList();
    }

    /**
     * Add a new Terminal to the TerminalStyle.  The Terminal will be 
     * added at the end of the current terminals.
     * @exception NameDuplicationException If an object with the same name as
     * terminal already exists in the terminal style.
     */
    public void addTerminal (Terminal t) 
            throws NameDuplicationException, IllegalActionException {
        _terminals.append(t);
    }

    /**
     * Test if this TerminalStyle contains the given Terminal.
     */
    public boolean containsTerminal (Terminal t) {
        return _terminals.includes(t);
    }

    /**
     * Return an enumeration over the terminals in this TerminalStyle.
     *
     * @return Enumeration of Terminals.
     */
    public Enumeration terminals() {
        return _terminals.elements();
    }

    /**
     * Remove a graphic element from the TerminalStyle. Throw an exception if
     * the Terminal is not contained in this TerminalStyle.
     */
    public void removeTerminal (Terminal t)
            throws IllegalActionException {
        try {
            _terminals.remove(t);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("removeTerminal:" +
                    "Terminal not found in TerminalStyle.");
        }
    }

    /**
     * Return a string representing this TerminalStyle.
     */
    public String toString() {
        Enumeration terms = terminals();
        String str = super.toString() + "(";
        while(terms.hasMoreElements()) {
            Terminal term = (Terminal) terms.nextElement();
            str += term.toString();
        }
        return str + ")";
    }

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int indent) {
        String result = super._description(indent);
	result += _getIndentPrefix(indent) + "terminals\n";
	Enumeration terminals = terminals();
        while (terminals.hasMoreElements()) {
            Terminal p = (Terminal) terminals.nextElement();
            result += p._description(indent + 1) + "\n";
        }
	
        //        result += _getIndentPrefix(indent);

        return result;
    }

    private NamedList _terminals;

}

