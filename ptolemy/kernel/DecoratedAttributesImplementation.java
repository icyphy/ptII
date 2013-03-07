/* A class that contains a number of decorated attributes.

 Copyright (c) 2009-2013 The Regents of the University of California.
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

package ptolemy.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////DecoratedAttributesImplementation

/**
A class that represents a number of decorated attributes.

<p>The functionality is divided in two classes ({@link DecoratedAttributes}
and this class) to solve dependency issues.
See {@link DecoratedAttributes} for more information.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class DecoratedAttributesImplementation extends DecoratedAttributes {

    /** Construct a DecoratedAttribute from the container and the decorator.
     *  @param container The container this object.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributesImplementation(NamedObj container,
            Decorator decorator) throws IllegalActionException,
            NameDuplicationException {
        super(container, decorator.getFullName().replaceAll("\\.", "_"));
        _decorator = decorator;
    }

    /** Construct a DecoratedAttributes instance with the given name and the container of the decorator.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  WARNING: don't use this constructor director. This should only be used
     *  by the MoMLParser.
     *  @param containerOfDecorator The container of the decorator.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     *   @deprecated
     */
    public DecoratedAttributesImplementation(NamedObj containerOfDecorator,
            String name) throws IllegalActionException,
            NameDuplicationException {
        // FIXME: There should be a more elegant way to get this right.
        // This also only works for attributes not for entities.
        super(_getRealContainer(containerOfDecorator, name), name
                .substring(name.lastIndexOf(".") + 1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        StringAttribute decoratorPath = _decoratorPath();
        if (_decorator == null && attribute == decoratorPath) {
            _decorator = (Decorator) toplevel().getAttribute(
                    decoratorPath.getExpression());
            // We get here we we are reading a MoML file. The type information of the parameter
            // is not stored, so we need to create it again.
            _decorator.setTypesOfDecoratedVariables(this);
            decoratorPath.setVisibility(Settable.NONE);

            try {
                // Register this object again, since we now know
                // the decorator.
                _register();
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                        "Can't register this decorated attribute.");
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a DecoratedAttributes object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DecoratedAttributesImplementation newObject = (DecoratedAttributesImplementation) super
                .clone(workspace);
        newObject._decorator = _decorator;
        return newObject;
    }

    /** Return all the decorators for a given object.
     *  @param object The object.
     *  @return The decorators for the given object.
     */
    @SuppressWarnings("unchecked")
    public static List<Decorator> findDecorators(NamedObj object) {
        List<Decorator> result = new LinkedList<Decorator>();
        NamedObj container = object.getContainer();
        while (container != null) {
            result.addAll(container.attributeList(Decorator.class));
            if (container instanceof CompositeEntity) {
                if (((CompositeEntity) container).isOpaque()) {
                    break;
                }
            }
            container = container.getContainer();
        }
        return result;
    }

    /** Return the decorator.
     * @return The decorator.
     */
    public Decorator getDecorator() {
        return _decorator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an attribute.  This method should not be used directly.
     *  Instead, call setContainer() on the attribute.
     *  Derived classes may further constrain the class of the attribute.
     *  To do this, they should override this method to throw an exception
     *  when the argument is not an instance of the expected class.
     *  This method is write-synchronized on the workspace and increments its
     *  version number.
     *  @param p The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    protected void _addAttribute(Attribute p) throws NameDuplicationException,
            IllegalActionException {
        if (_decorator == null) {
            if (p.getName().equals("_decorator")
                    && p instanceof StringAttribute) {
                _decoratorPath = (StringAttribute) p;
                _decoratorPath.setVisibility(Settable.NONE);
            }
        }
        super._addAttribute(p);
    }

    /** Return the decorator path. It is the full path of the
     *  decorator in the model.
     *  This variable is used for persistence to recreate
     *  the code generator after having parsed the model.
     */
    protected StringAttribute _decoratorPath() {
        return _decoratorPath;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the real container of this object.
     *  @param containerOfCodeGenerator The container of the decorator.
     *  @param name The name of this attribute.
     *  @return The container of this object.
     */
    static private NamedObj _getRealContainer(
            NamedObj containerOfCodeGenerator, String name) {
        String elementName = name.substring(0, name.lastIndexOf("."));
        NamedObj object = containerOfCodeGenerator.getAttribute(elementName);
        if (object == null) {
            if (containerOfCodeGenerator instanceof CompositeEntity) {
                object = ((CompositeEntity) containerOfCodeGenerator)
                        .getEntity(elementName);
            }
        }
        return object;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The decorator.*/
    private Decorator _decorator = null;

    /** The decorator path. This variable is used for persistence to recreate
      * the code generator after having parsed the model.*/
    private StringAttribute _decoratorPath = null;
}
