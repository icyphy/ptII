/* A SchematicDirector is an entity stored in a schematic

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
import diva.util.*;
import diva.graph.model.*;

//////////////////////////////////////////////////////////////////////////
//// SchematicDirector
/**
The SchematicDirector class represents an Director within a Schematic. 
A schematic director is immutably associated with a director template in some 
director library.  
The template specifies the immutable aspects of the director, 
such as its default parameters.  

@author Steve Neuendorffer
@version $Id$
*/
public class SchematicDirector extends PTMLTemplateObject {
    /**
     * Create a new SchematicDirector object with no template and
     * a unique name.
     */
    public SchematicDirector () {
        this("director", null);
	try {
	    setName(_createUniqueName());
	} catch (NameDuplicationException ex) {
	    throw new InternalErrorException("Unique name was not unique!");
	}
    }

    /**
     * Create a new SchematicDirector object with no template and
     * the given name.
     */
    public SchematicDirector (String name) {
        this(name, null);
    }

    /**
     * Create a new SchematicDirector object with the given name and director
     * template.
     */
    public SchematicDirector (String name, SchematicDirector et) {
        super(name, et);
    }

    /**
     * Clone this director.  Return a new SchematicDirector with a unique name.
     */
    public Object clone() throws CloneNotSupportedException {
       try {
           Object newobj = (SchematicDirector) super.clone();
           
           return newobj;
       } catch (Exception e) {
           if(e instanceof CloneNotSupportedException)
               throw (CloneNotSupportedException)e;
           else 
               throw new CloneNotSupportedException(e.getMessage());
       }
    }
    
    /**
     * Get a string representing the implementation of this director.  This
     * may be a java class name, or a URL for a PTML schematic object.
     */
    public String getImplementation () {
        if(hasTemplate() && (_implementation == null)) 
            return ((SchematicDirector) getTemplate()).getImplementation();
        else
            return _implementation;
    }

    /** 
     * Set the string that represents the implementation of this director.
     * Note that if this director
     * has a template, this corresponds to overriding the value that is 
     * set in the template, but does not affect the template in any way.
     * to return to the value set in the template, call this method with a 
     * null argument.
     * @see #getImplementation
     */
    public void setImplementation (String implementation) {
	_implementation = implementation;
    }

    /**
     * Return a string this representing Director.
     */
    protected String _description(int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(indent, 0);
        else 
            result += super._description(indent, 1);

 	result += " implementation {\n";
	if(_implementation == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _getIndentPrefix(indent + 1) + 
                _implementation + "\n";

        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    private String _implementation;

}






