/* An PTMLObject is a nameable object in a schematic.

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
import ptolemy.schematic.xml.XMLElement;
import diva.util.*;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// PTMLObject
/**

The base class for objects parsed from PTML files.

@author Steve Neuendorffer
@version $Id$
*/
public class PTMLObject extends diva.util.BasicPropertyContainer
    implements Nameable {
    // FIXME Are Properties parameters???

    /**
     * Create a new PTMLObject with the name "PTMLObject" and an empty 
     *  description 
     */
    public PTMLObject () {
        this("PTMLObject");
    }

    /**
     * Create a new PTMLObject with the given name and an empty 
     *  description.
     */
    public PTMLObject (String name) {
        super();
        _name = name;
        _documentation = new String("");
    }

    /**
     * Add a new Parameter to the object.  The Parameter will be 
     * added at the end of the current parameters.  If a parameter with the 
     * same name already exists, then update the type and value of that
     * parameter, instead of appending the new one.
     * @exception IllegalActionException If the parameter has no name.
     * @exception NameDuplicationException If adding the parameter would
     * result in having two parameters with the same name.
     */
    public void addParameter (SchematicParameter t)
        throws IllegalActionException, NameDuplicationException {
        if(_parameters == null) {            
            // This is rather convoluted, since we don't want to
            // save the NamedList if the append is just going to throw
            // an exception.
            NamedList newparameters = new NamedList();
            newparameters.append(t);
            _parameters = newparameters;
        } else {
            _parameters.append(t);
        }
    }    

    /**
     * Test if this object contains the given Parameter.
     */
    public boolean containsParameter (SchematicParameter t) {
        if(_parameters == null) return false;
        return _parameters.includes(t);
    }

    /** Clone this object.  Return a new object of this type with 
     *  copies of all this object's parameters.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        try {
            PTMLObject newobj = (PTMLObject) super.clone();
            newobj._container = null;
            newobj.setName(_createUniqueName());
            newobj._parameters = null;
            Enumeration objects = parameters();
            while(objects.hasMoreElements()) {
                PTMLObject object = (PTMLObject)objects.nextElement();
                newobj.addParameter((SchematicParameter)object.clone());
            }
            return newobj;
        } catch (Exception e) {
            if(e instanceof CloneNotSupportedException)
                throw (CloneNotSupportedException)e;
            else 
                throw new CloneNotSupportedException(e.getMessage());
        }
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @see ptolemy.kernel.CompositeEntity#isAtomic
     *  @return True if this contains the argument, directly or indirectly.
     */
    public boolean deepContains(NamedObj inside) {
        // Start with the inside and check its containers in sequence.
        if (inside != null) {
            Nameable container = inside.getContainer();
            while (container != null) {
                if (container == this) {
                    return true;
                }
                container = container.getContainer();
            }
        }
        return false;
    }

    /** Get the parameter with the given name in this object.  If no
     * such parameter exists, then check recursively check all of this
     * associated objects.  These associated objects
     * are usually "templates" which provide some default parameters shared 
     * between many objects. In this base class, this is the same as calling 
     * getParameter().  If no such parameter exists in this object, or 
     * any of it's templates, then return null.
     */
    public SchematicParameter deepGetParameter(String name) {
        SchematicParameter param = getParameter(name);
        return param;
    }

    /**
     * Return an enumeration over the parameters in this object, and all the 
     * parameters in any objects associated with it.  These associated objects
     * are usually "templates" which provide some default parameters shared 
     * between many objects.
     * In this base class, this is the same as calling parameters().
     *
     * @return Enumeration of SchematicParameter.
     */
    public Enumeration deepParameters() {
        return parameters();
    }

    /** Return a description of the object. The general
     *  form of the description is a space-delimited list of the form
     *  "className fullName <i>keyword</i> field <i>keyword</i> field ...".
     *  If any of the items contain spaces, then they must be surrounded
     *  by braces, as in "{two words}". Return characters or newlines
     *  may be be used as delimiters as well. The fields are usually
     *  lists of descriptions of this same form, although different
     *  forms can be used for different keywords.  The keywords are
     *  extensible, but the following are in use: links, ports, entities,
     *  relations, attributes, and insidelinks, at least.
     *  @return A description of this object.
     */
    public String description() {
            return _description(0, 0);
    }

    /** Get the container of this PTMLObject.  
     * If there is no container, then
     * return null.
     */
    public Nameable getContainer() {
	return _container;
    }

    /**
     * Return a long description string of this PTMLObject
     */
    public String getDocumentation() {
        return _documentation;
    }

    /** Return a string of the form "name1.name2...nameN". Here,
     *  "nameN" is the name of this object, and the other names 
     *  are the names of the containers
     *  of this object, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in a runtime exception of class
     *  InvalidStateException.  
     *  This method is read-synchronized on the workspace.
     *  @return The full name of the object.
     */
    public String getFullName() {
        String fullName = getName();
        // Use a linked list to keep track of what we've seen already.
        LinkedList visited = new LinkedList();
        visited.insertFirst(this);
        Nameable container = getContainer();

        while (container != null) {
            if (visited.firstIndexOf(container) >= 0) {
                // Cannot use "this" as a constructor argument to the
                // exception or we'll get stuck infinitely
                // calling this method, since this method is used to report
                // exceptions.  InvalidStateException is a runtime
                // exception, so it need not be declared.
                throw new InvalidStateException(
                        "Container contains itself!");
            }
            fullName = container.getName() + "." + fullName;
            visited.insertFirst(container);
            container = container.getContainer();
        }
        return fullName;
    }

    /** Return the name of the object.
     */
    public String getName() {
        return _name;
    }

    /** Get the parameter with the given name, or null if no such parameter
     * exists.
     */
    public SchematicParameter getParameter(String name) {
        if(_parameters == null) {
            return null;
        }
        return (SchematicParameter)_parameters.get(name);
    }

    /**
     * Return an enumeration over the parameters in this object.
     *
     * @return Enumeration of Parameters.
     */
    public Enumeration parameters() {
        if(_parameters == null) {
            LinkedList l = new LinkedList();
            return l.elements();
        }
        return _parameters.elements();
    }

    /**
     * Remove a graphic element from the object. Throw an exception if
     * the parameter is not contained in this object.
     */
    public void removeParameter (SchematicParameter t)
            throws IllegalActionException {
        if(!containsParameter(t)) {
            throw new IllegalActionException(t, this, "removeParameter:" +
                    "Parameter not found in Object.");
        }
        _parameters.remove(t);
    }

    /** 
     * Specify the container of this object.  In general you should not
     * call this method directly, but call an add method on the container.
     * @param container The container.
     */
    public void setContainer(PTMLObject container) {
        _container = container;
    }

   /**
     * Set the string that contains the long 
     * description of this PTMLObject.
     */
    public void setDocumentation(String s) {
        _documentation = s;
    }

    /** Set the name of the ComponentEntity. If there is already
     *  a ComponentEntity of the container with the same name, throw an
     *  exception.
     *  @exception NameDuplicationException If there already is an entity
     *   in the container with the same name.
     */
    public void setName(String name) throws NameDuplicationException {
        if (name == null) {
            name = new String("");
        }
        PTMLObject container = (PTMLObject) getContainer();
        if((container != null)) {
            //            PTMLObject another = (PTMLObject)
            //    container.getObject(name);
            //if((another != null) && (another != this)) {
            //    throw new NameDuplicationException(container,
            //            "already contains an entity with the name "
            //            + name + ".");
            // }
        }
        _name = name;
    }

    /** 
     * Set a parameter in this object with the given name, and value.
     * If a parameter already exists with the name, then alter that parameter
     * to have the new value, but leave the type unchanged.  If no parameter
     * exists in this object with the given name, but some associated template
     * does have such a parameter, then add a new parameter with the given name
     * and value and type inferred from the template's parameter.
     *
     * @exception IllegalActionException If name is null, or no valid
     * parameter exists.
     */
    public void setParameter(String name, String value) 
        throws IllegalActionException {
        SchematicParameter parameter = deepGetParameter(name);
        if(parameter == null) {
            throw new IllegalActionException("No parameter found with name " +
                    name);
        }
        setParameter(name, value, parameter.getType());
    }

    /** 
     * Set a parameter in this object with the given name, value and type.
     * If a parameter already exists with the name, then alter that parameter
     * to have the new value and type.
     * Otherwise, create a new parameter with the given characteristics and
     * add it to this object.
     * @exception IllegalActionException If name is null.
     */
    public void setParameter(String name, String value, String type) 
        throws IllegalActionException {
        SchematicParameter parameter = getParameter(name);
        if(parameter == null) {
            parameter = new SchematicParameter(name);
            try {
                addParameter(parameter);
            }
            catch (NameDuplicationException ex) {
                // This should never happen.
                ex.printStackTrace();
                throw new InternalErrorException(ex.getMessage());
            }
        }
        parameter.setValue(value);
        parameter.setType(type);
    }

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    /** Return a unique name for a new object. 
     */
    protected String _createUniqueName() {
        // FIXME This is such a ridiculously lame way to do this.
	// This doesn't actually create a unique name across all 
	// executions.
        return getClass().getName() + _createUniqueID();
    }

    /** Return a unique ID for a new object.
     */
    protected long _createUniqueID() {
        // FIXME as above
        return PTMLObject._uniqueID++;
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
    protected String _description(int indent, int bracket) {
        String result = _getIndentPrefix(indent);
        if (bracket == 1 || bracket == 2) result += "{";
        result += getClass().getName() + " {";
        result += getFullName() + "}";
        
        result += " parameters {\n";
        result += _enumerationDescription(parameters(), indent);
        
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    /** Return a description of the enumeration by appropriately calling 
     *  _description on each element of the enumeration.  
     *  @param enum An enumeration of PTMLObjects.
     */
    protected String _enumerationDescription(Enumeration enum, int indent) {
        String result = "";
        while(enum.hasMoreElements()) {
            PTMLObject obj = (PTMLObject) enum.nextElement();
            result += obj._description(indent + 1, 2) + "\n";
        }
        return result;
    }

    /** Return a description of the given object, or null if the object is null
     */
    protected String _getDescription(PTMLObject object, int indent) {
        String result;
        if(object == null) 
            result = _getIndentPrefix(indent + 1) + "null\n";
        else
            result = object._description(indent + 1, 0) + "\n";
        return result;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "    ";
        }
        return result;
    }

    private String _documentation;
    private Nameable _container;
    private String _name;
    private NamedList _parameters;

    private static long _uniqueID = 1;
}

