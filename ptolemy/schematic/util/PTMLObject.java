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
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;

//////////////////////////////////////////////////////////////////////////
//// PTMLObject
/**

A PTMLObject is the base class for
any object that can be pulled out of a Schematic file.  

@author Steve Neuendorffer
@version $Id$
*/
public class PTMLObject extends Object implements Nameable {

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
     * added at the end of the current parameters.
     *  @exception IllegalActionException If the parameter has no name.
     *  @exception NameDuplicationException If the name of the parameter
     *  coincides with the name of another parameter
     *  contained in this object.
     *
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

    /** Clone the object into the current workspace by calling the clone()
     *  method that takes a Workspace argument.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @see CompositeEntity.isAtomic
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
            return _description(0);
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

   /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may override this method to constrain the container
     *  to subclasses of CompositeEntity. This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace..
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(PTMLObject container)
            throws  NameDuplicationException {
        if(container == _container) return;
        //        PTMLObject other = container.getObject(getName());
        //if(other != null)
        //        throw new NameDuplicationException(
        //                container, "PTMLObject already contains an " +
        //                "object with name " + getName());
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

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
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
        String result = _getIndentPrefix(indent);
        result += getClass().getName() + "(";
        result += getFullName() + ")\n";
        result += _getIndentPrefix(indent) + "parameters\n";

        Enumeration params = parameters();
        while (params.hasMoreElements()) {
            SchematicParameter p = (SchematicParameter) params.nextElement();
            result += p._description(indent + 1) + "\n";
        }
        //        result += _getIndentPrefix(indent);

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
}

