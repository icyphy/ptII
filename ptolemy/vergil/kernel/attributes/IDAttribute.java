/* Attribute that contains attributes that identify the containing model.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import java.text.DateFormat;
import java.util.Date;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.BoxedValuesIcon;

///////////////////////////////////////////////////////////////////
//// IDAttribute

/**
 This attribute identifies the containing model, showing its name, base
 class, last modified date, author, and contributors information.
 Of these, only the contributors information is editable.
 For the others, they are inferred from either the model itself or the
 operations on the model.
 Unfortunately, the changes will not be shown on the display until the
 model is saved, closed and re-opened.
 <p>
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
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

        // name for the model
        this.name = new StringAttribute(this, "name");
        this.name.setExpression(container.getName());

        // This should not be persistent, in case the name changes outside
        // of this parameter.
        this.name.setPersistent(false);

        // This should not be editable, since the name is set by saveAs.
        this.name.setVisibility(Settable.NOT_EDITABLE);

        // FIXME: Need to listen for changes to the name.
        // How to do that?
        // The current design is also a solution in that the name of this
        // attribute and model must be consistent with the name of the file.
        // boolean isClass = false;
        //if (container instanceof InstantiableNamedObj) {
        /* isClass = */((InstantiableNamedObj) container).isClassDefinition();
        //}

        String className = container.getClassName();

        baseClass = new StringAttribute(this, "baseClass");
        baseClass.setExpression(className);

        // This should not be persistent, because the base class
        // is set already, generally.
        baseClass.setPersistent(false);

        // Cannot change the base class.
        baseClass.setVisibility(Settable.NOT_EDITABLE);

        URIAttribute modelURI = (URIAttribute) container.getAttribute("_uri",
                URIAttribute.class);

        if (modelURI != null) {
            StringAttribute definedIn = new StringAttribute(this, "definedIn");
            definedIn.setExpression(modelURI.getURI().toString());
            definedIn.setPersistent(false);
            definedIn.setVisibility(Settable.NOT_EDITABLE);
        }

        // The date when this model is created.
        // Actually, it is the date when this attribute is created.
        // We assume that when the model is created, this attribute
        // is also created.
        // We may force this to happen.:-) Further more, we may force
        // that only the top level contains an model ID.
        created = new StringAttribute(this, "created");
        created.setExpression(DateFormat.getDateTimeInstance().format(
                new Date()));
        created.setVisibility(Settable.NOT_EDITABLE);
        created.setPersistent(true);

        // The date when this model is modified.
        // Everytime the model gets modified, the updateContent method
        // defined below is called and the lastUpdated attribute gets
        // updated.
        lastUpdated = new StringAttribute(this, "lastUpdated");
        _updateDate();
        lastUpdated.setVisibility(Settable.NOT_EDITABLE);
        lastUpdated.setPersistent(true);

        // The name of the author who creates the model.
        // This attribute can not be changed so that the
        // intellectual property (IP) is preserved.
        author = new StringAttribute(this, "author");
        author.setVisibility(Settable.NOT_EDITABLE);

        String userName = null;

        try {
            userName = StringUtilities.getProperty("user.name");
        } catch (Exception ex) {
            System.out.println("Warning, in IDAttribute, failed to read "
                    + "'user.name' property (-sandbox or applets always cause "
                    + "this)");
        }

        if (userName != null) {
            author.setExpression(userName);
        }

        author.setPersistent(true);

        // The names of the contributors who modify the model.
        contributors = new StringAttribute(this, "contributors");

        String contributorsNames = "";
        contributors.setExpression(contributorsNames);
        author.setPersistent(true);

        // Hide the name of this ID attribute.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        BoxedValuesIcon icon = new BoxedValuesIcon(this, "_icon");
        icon.setPersistent(false);

        // No need to display any parameters when the "_showParameters"
        // preference asks for such display because presumably all the
        // parameters are reflected in the visual display already.
        Parameter hideAllParameters = new Parameter(this, "_hideAllParameters");
        hideAllParameters.setVisibility(Settable.EXPERT);
        hideAllParameters.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         attributes                        ////

    /** The author of the model. */
    public StringAttribute author;

    /** The contributors of the model. */
    public StringAttribute contributors;

    /** The date that this model was created. */
    public StringAttribute created;

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
    @Override
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

    /** Update the modification date of this attribute.
     */
    @Override
    public void updateContent() throws InternalErrorException {
        super.updateContent();
        _updateDate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the current date for the <i>lastUpdated</i> parameter.
     */
    private void _updateDate() {
        try {
            lastUpdated.setExpression(DateFormat.getDateTimeInstance().format(
                    new Date()));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }
}
