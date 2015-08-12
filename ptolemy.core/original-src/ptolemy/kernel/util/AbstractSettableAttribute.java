/* Abstract base class for attributes that can have their values
 externally set.

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
package ptolemy.kernel.util;

import java.util.List;

///////////////////////////////////////////////////////////////////
//// AbstractSettableAttribute

/**
 This is an abstract base class for attributes that implement the
 Settable interface. In particular, it provides a default implementation
 of the getDefaultExpression() method.  If this object is derived from
 another, then the default value is obtained from that other.
 Otherwise, the default value is the first value set using
 setExpression().

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (hyzheng)
 */
public abstract class AbstractSettableAttribute extends Attribute implements
Settable {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public AbstractSettableAttribute() {
        super();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public AbstractSettableAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public AbstractSettableAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @param incrementWorkspaceVersion False to not add this to the workspace
     *   or do anything else that might change the workspace version number.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    protected AbstractSettableAttribute(NamedObj container, String name,
            boolean incrementWorkspaceVersion) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, incrementWorkspaceVersion);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the default value of this Settable, or null if no default
     *  has been set.  If this object is derived (in the actor-oriented
     *  class mechanism, see the Instantiable interface), then the default
     *  is the value of the object from which this is derived (the
     *  "prototype").  If this is not a derived object, then the default
     *  is the first value set using setExpression(), or null if
     *  setExpression() has not been called.
     *  @return The default value of this attribute, or null
     *   if there is none.
     *  @see #setExpression(String)
     *  @see Instantiable
     */
    @Override
    public String getDefaultExpression() {
        try {
            // Get the list of objects from which this is derived,
            // the first of which will be the immediate prototype.
            List prototypeList = getPrototypeList();

            if (prototypeList.size() > 0) {
                return ((Settable) prototypeList.get(0)).getExpression();
            }
        } catch (IllegalActionException e) {
            // This should not occur.
            throw new InternalErrorException(e);
        }

        return _default;
    }

    /** Get the value of the attribute, which is the evaluated expression.
     *  @return The same as getExpression().
     *  @see #getExpression()
     */
    @Override
    public String getValueAsString() {
        return getExpression();
    }

    /** Set the value of this attribute to the specified expression.
     *  This base class implementation merely records the first
     *  value to serve as the default value if needed.
     *  Subclasses are required to override this to also record
     *  the value and to call super.setExpression().
     *  @param expression The value of this attribute.
     *  @exception IllegalActionException If the expression is invalid
     *   (not thrown in this base class).
     *  @see #getDefaultExpression()
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
        if (_default == null) {
            _default = expression;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The default value.  */
    private String _default = null;
}
