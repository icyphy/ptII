/* Attribute that contains attributes that identify the containing model.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.attributes;

import java.text.DateFormat;
import java.util.Date;

import ptolemy.kernel.Entity;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// IDAttribute
/**
This attribute identifies the containing model, showing its name, base
class, last modified date, and author information. Of these, only the
author information is editable.  For the others, they are inferred
from the model.  Unfortunately, if they change, the display will
not be updated, however, until the model is re-opened.
<p>
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class IDAttribute extends SingletonAttribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public IDAttribute(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        this.name = new StringAttribute(this, "name");
        this.name.setExpression(container.getName());
        // This should not be persistent, in case the name changes outside
        // of this parameter.
        this.name.setPersistent(false);
        // This should not be editable, since the name is set by saveAs.
        this.name.setVisibility(Settable.NOT_EDITABLE);
        
        // FIXME: Need to listen for changes to the name.
        // How to do that?
        
        boolean isClass = false;
        if (container instanceof Prototype) {
            isClass = ((Prototype)container).isClassDefinition();
        }
        
        String className = container.getClassName();

        baseClass = new StringAttribute(this, "baseClass");
        baseClass.setExpression(className);
        // This should not be persistent, because the base class
        // is set already, generally.
        baseClass.setPersistent(false);
        // Cannot change the base class.
        baseClass.setVisibility(Settable.NOT_EDITABLE);
        
        URIAttribute modelURI = (URIAttribute)container.getAttribute(
               "_uri", URIAttribute.class);
        if (modelURI != null) {
            StringAttribute definedIn = new StringAttribute(this, "definedIn");
            definedIn.setExpression(modelURI.getURI().toString());
            definedIn.setPersistent(false);
            definedIn.setVisibility(Settable.NOT_EDITABLE);
        }
        
        lastUpdated = new StringAttribute(this, "lastUpdated");
        setDate(null);
        lastUpdated.setVisibility(Settable.NOT_EDITABLE);
        
        author = new StringAttribute(this, "author");
        String userName = System.getProperty("user.name");
        if (userName != null) {
            author.setExpression(userName);
        }

        // Hide the name.
        new Attribute(this, "_hideName");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         attributes                        ////

    /** The author of the class. */
    public StringAttribute author;
    
    /** The base class of the containing class or entity. */
    public StringAttribute baseClass;
        
    /** A boolean indicating whether the container is a class or an
     *  instance.  This is a string that must have value "true" or
     *  "false".
     */
    public StringAttribute isClass;

    /** The date that this model was last updated. */
    public StringAttribute lastUpdated;
    
    /** The name of the containing class or entity. */
    public StringAttribute name;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is <i>name</i>,
     *  then change the name of the container to match.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == name) {
            Nameable container = getContainer();
            try {
                container.setName(name.getExpression());
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                "Cannot change the name of the container to match.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Set the date for the <i>lastUpdated</i> parameter.
     *  A null argument requests that the date be set to now.
     *  @param date The date to set.
     */
    public void setDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        try {
            lastUpdated.setExpression(
                    DateFormat.getDateTimeInstance().format(date));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }
}
