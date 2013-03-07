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

package ptolemy.kernel.util;

import java.io.IOException;
import java.io.Writer;

///////////////////////////////////////////////////////////////////
////DecoratedAttributes

/**
An abstract class that represents a number of decorated attributes.

<p>A NamedObj can contain DecoratedAttributes. These are attributes that are
added by another NamedObj, called a decorator to this NamedObj.
An example is a code generator that has specific attributes such as
parameters that control the code code generator. These attributes
are added by the Decorator (the code generator), to the director (the "this" object).
These attributes are stored separately and can be retrieved by using
{@link NamedObj#getDecoratorAttributes(Decorator)} or
{@link NamedObj#getDecoratorAttributes(Decorator)}.</p>

<p>A DecoratedAttributes instance has a decorator and
decorated attributes.
The container of this class is the object that is being decorated.</p>

<p>The implementation class is {@link ptolemy.kernel.DecoratedAttributesImplementation},
the functionality is divided in two classes to solve dependency issues.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public abstract class DecoratedAttributes extends Attribute {

    /** Construct a DecoratedAttributes instance with the given name
     *  and the container of the decorator.  The container argument
     *  must not be null, or a NullPointerException will be thrown.
     *  This attribute will use the workspace of the container for
     *  synchronization and version counts.  If the name argument is
     *  null, then the name is set to the empty string.  Increment the
     *  version of the workspace.
     *  @param container The container of this object.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributes(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Export the Decorated attributes. This is a special method
     *  where we don't store the Decorated Attributes directly in the
     *  container, but we do it is another container (typically the container
     *  of the decorator) to make sure that when the model is parsed again,
     *  both the container of this object and the decorator are already parsed
     *  and hence all links can be corrected.
     *  @param container The container in which we store the the attribute.
     *          This typically is the container of the decorator.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @exception InvalidStateException If a recursive structure is
     *   encountered, where this object directly or indirectly contains
     *   itself. Note that this is a runtime exception so it need not
     *   be declared explicitly.
     */
    public void exportToMoML(NamedObj container, Writer output, int depth)
            throws InvalidStateException, IOException {
        if (this.attributeList().isEmpty()) {
            return;
        }
        StringAttribute attribute = (StringAttribute) this
                .getAttribute("_decorator");
        if (attribute == null) {
            try {
                attribute = new StringAttribute(this, "_decorator");
                attribute.setVisibility(Settable.NONE);
            } catch (IllegalActionException e) {
                throw new InvalidStateException(this, e,
                        "Can't export the decorated attributes.");
            } catch (NameDuplicationException e) {
                throw new InvalidStateException(this, e,
                        "Can't export the decorated attributes.");
            }
        }
        try {
            attribute.setExpression(getDecorator().getName(toplevel()));
        } catch (IllegalActionException e) {
            throw new InvalidStateException(this, e,
                    "Can't export the decorated attributes.");
        }
        exportMoML(output, depth, getName(container));
    }

    /** Return the decorator.
     *  @return The decorator.
     */
    abstract public Decorator getDecorator();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the decorator path. It is the full path of the
     *  decorator in the model.
     *  This variable is used for persistence to recreate
     *  the code generator after having parsed the model.
     *  @return The full path of the decorator in the model.
     */
    protected abstract StringAttribute _decoratorPath();

    /** Register this attribute again to the container. This is
     *  necessary, since when opening the model, the decorator is not
     *  directly know, the container doesn't have the mapping between
     *  attribute and decorator.
     *  @exception NameDuplicationException If the container already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    final protected void _register() throws NameDuplicationException,
            IllegalActionException {
        getContainer()._addAttribute(this);
    }
}
