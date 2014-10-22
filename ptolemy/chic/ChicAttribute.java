/* An attribute used for storing an interface to be used with Chic.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.chic;

// Ptolemy imports
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

// Java imports
///////////////////////////////////////////////////////////////////
//// ChicAttribute

/**
 An attribute that has a string value which is meant to be used as an interface
 in Chic.
 Use setExpression() to define the value, as in for example
 <pre>
 attribute.setExpression("xxx");
 </pre>
 <p>The default value of the string contained by this attribute is the empty
 string.

 <p>By default, an instance of this class is fully visible in a user
 interface and it is annotated with a TextStyle attribute.  The
 visibility is indicated to the user interface when the user interface
 calls the getVisibility() method of this class and the value
 Settable.FULL is returned to the userInterface.

 <p>Note that the string value within ChicAttribute will not be parsed
 and you do not have to type a leading and a trailing double quote.

 <p>This class is an attribute that replaces any previously existing
 attribute of the same class in the container that has the same name.

 @author Eleftherios Matsikoudis
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ChicAttribute extends StringAttribute {
    /** Construct an attribute with the given name contained by the specified
     *  container and annotate it with a TextStyle attribute. The container
     *  argument must not be null, or a NullPointerException will be thrown.
     *  This attribute will use the workspace of the container for
     *  synchronization and version counts. If the name argument is null,
     *  then the name is set to the empty string. This attribute that replaces
     *  any previously existing attribute of the same class in the container
     *  that has the same name. The object is added to the directory of the
     *  workspace if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute of different class already in the container.
     */
    public ChicAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        new TextStyle(this, "style");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove any previous ChicAttribute in the container that has the
     *  same name as this attribute, and then call the base class method
     *  to set the container. If the container has an attribute with the
     *  same name that is not an instance of this class, throw an exception.
     *  If the container is not in the same workspace as this attribute,
     *  throw an exception. If this attribute is already contained by the
     *  NamedObj, do nothing. If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove this attribute from its
     *  container. It is not added to the workspace directory, so this
     *  could result in this object being garbage collected.
     *  <p>
     *  Note that since an ChicAttribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.
     *  <p>
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name, or there is an
     *   attribute with the same name that is not an instance of this class,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute that is not of class
     *   ChicAttribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        ChicAttribute previous = null;

        if (container != null) {
            previous = (ChicAttribute) container.getAttribute(getName(),
                    getClass());

            if (previous != null) {
                previous.setContainer(null);
            }
        }

        try {
            super.setContainer(container);
        } catch (IllegalActionException ex) {
            // Restore previous.
            if (previous != null) {
                previous.setContainer(container);
            }

            throw ex;
        } catch (NameDuplicationException ex) {
            // Restore previous.
            if (previous != null) {
                previous.setContainer(container);
            }

            throw ex;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
