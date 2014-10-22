/* A class that contains a number of decorated attributes.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// DecoratorAttributes

/**
A container for attributes created by a decorator.
This is an attribute that will be contained by a target object that is
decorated by a decorator. The parameters that the decorator creates
will be contained by an instance of this object.
These attributes can be retrieved by using
{@link NamedObj#getDecoratorAttribute(Decorator,String)} or
{@link NamedObj#getDecoratorAttributes(Decorator)}.

@author Bert Rodiers
@author Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (rodiers)
 */
public class DecoratorAttributes extends Attribute {

    // FIXME: The decoratorName mechanism is very fragile.
    // If the decorator changes name, the connection will be lost.
    // If the container of the decorator changes name, it will again be lost.
    // Probably the first constructor should not set the name, and this should
    // be set only when MoML is to be exported.
    // The second constructor should immediately look for the decorator by name.
    // But it may not have been constructed yet. Hence, when a decorator is
    // constructed, it should look for decorated objects and establish the link.
    // This way, it doesn't matter which gets constructed first.

    /** Construct a DecoratorAttributes instance to contain the
     *  decorator parameter for the specified container provided
     *  by the specified decorator. This constructor is used
     *  when retrieving decorator parameters from a target
     *  NamedObj that does not yet have the decorator parameters.
     *  @param container The target for the decorator.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container. This should not occur.
     */
    public DecoratorAttributes(NamedObj container, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(container, container.uniqueName("DecoratorAttributesFor_"
                + decorator.getName()));
        _decorator = decorator;

        decoratorName = new StringAttribute(this, "decoratorName");
        decoratorName.setVisibility(Settable.NONE);
    }

    /** Construct a DecoratorAttributes instance with the given name
     *  and container.  This constructor is used when parsing MoML files,
     *  where it is assumed that the decorator is specified by name as
     *  the value of the {@link #decoratorName} parameter.
     *  @param container The container of this object.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratorAttributes(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        decoratorName = new StringAttribute(this, "decoratorName");
        decoratorName.setVisibility(Settable.NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the decorator relative to the top-level of
     *  the model, to be stored in a MoML file
     *  to re-establish the connection with a decorator after saving
     *  and re-parsing the file. The name is relative to the top-level,
     *  rather than the full name, so that SaveAs works (where the name
     *  of the toplevel changes).
     *  FIXME: However, if you save a submodel, then this will not work!
     *  This is a string that is not visible to the user.
     */
    public StringAttribute decoratorName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to establish a link to the decorator
     *  if the argument is decoratorName.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == decoratorName) {
            // The decorator name is being set.
            // Attempt to set the _decorator now, because its name
            // or the name of one of its containers may change later.
            // This will remain null if the decorator has not yet been
            // constructed.
            _decorator = getDecorator();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DecoratorAttributes newObject = (DecoratorAttributes) super
                .clone(workspace);
        newObject._decorator = null;
        return newObject;
    }

    /** Override the base class to first set the decoratorName attribute
     *  to the current name of the associated decorator, and then export
     *  using the superclass.
     *
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name of we use when exporting the description.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int)
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        try {
            _decorator = getDecorator();
        } catch (IllegalActionException e1) {
            throw new IOException("Export failed.", e1);
        }
        if (_decorator == null) {
            // No matching decorator is found. Discard the decorator attributes.
            return;
        }
        // Record the name relative to the toplevel entity so that even if you
        // do SaveAs (which changes the name of the toplevel) the decorator
        // can still be found.
        // FIXME: If you save a submodel, this will break the connection
        // to the decorator.
        try {
            decoratorName.setExpression(_decorator.getName(toplevel()));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        // Now invoke the superclass to export MoML.
        super.exportMoML(output, depth, name);
    }

    /** Return the decorator that is responsible for this DecoratorAttributes instance.
     *  @return The decorator, or null if there is none.
     *  @exception IllegalActionException If the decorator cannot be determined
     *   (e.g., a parameter cannot be evaluated).
     */
    public Decorator getDecorator() throws IllegalActionException {
        if (_decorator != null) {
            // There is a decorator associated associated with this DecoratorAttributes.
            // Check to see whether the decorator is still in scope. If it is not,
            // then see whether there is a decorator in scope whose name matches,
            // and establish a link with that one. This makes undo work. Specifically,
            // if you delete a decorator, then undo, the undo will actually create a
            // new decorator instance. This code re-establishes the connection.
            NamedObj decoratorContainer = _decorator.getContainer();
            if (decoratorContainer == null
                    || !decoratorContainer.deepContains(this)) {
                // Decorator is no longer in scope.  Fall into code below to try to find it by name.
                _decorator = null;
            }
        }
        if (_decorator == null) {
            // Retrieve the decorator using the decoratorName parameter.
            String name = decoratorName.getExpression();
            if (name != null && !name.equals("")) {
                // Find all the decorators in scope, and return the first one whose name matches.
                NamedObj container = getContainer().getContainer();
                boolean crossedOpaqueBoundary = false;
                while (container != null) {
                    List<Decorator> localDecorators = container
                            ._containedDecorators();
                    for (Decorator decorator : localDecorators) {
                        if (!crossedOpaqueBoundary
                                || decorator.isGlobalDecorator()) {
                            if (decorator.getName(toplevel()).equals(name)) {
                                // We have a match.
                                _decorator = decorator;
                                return _decorator;
                            }
                        }
                    }
                    // FIXME: kernel.util should not have a dependence on kernel classes.
                    if (container instanceof CompositeEntity
                            && ((CompositeEntity) container).isOpaque()) {
                        crossedOpaqueBoundary = true;
                    }
                    container = container.getContainer();
                }
            }
        }
        return _decorator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The decorator.*/
    protected Decorator _decorator = null;
}
