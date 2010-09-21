/** An interface for decorators.

 Copyright (c) 2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
////Decorator

/**
A decorator is a class that decorates other NamedObj
with extra attributes that are specific to both the decorator
and the NamedObj.

<p>A NamedObj can contain DecoratedAttributes. These are attributes that are
added by another NamedObj, called a decorator to this NamedObj.
An example is for example a code generator. This one has specific attributes
for for example the generated code of the director in a model. These attributes
are added by the Decorator (the code generator), to the director ("this" object).
These attributes are stored seperately and can be retrieved by using
{@link NamedObj#getDecoratorAttributes(Decorator)} or
{@link NamedObj#getDecoratorAttributes(Decorator)}.

<p>For a description of a decorator pattern, see
<a href="http://en.wikipedia.org/wiki/Decorator_pattern">http://en.wikipedia.org/wiki/Decorator_pattern</a>.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public interface Decorator extends Nameable {

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributes createDecoratedAttributes(NamedObj target)
            throws IllegalActionException, NameDuplicationException;

    /** Set the current type of the decorated attributes.
     *  The type information of the parameters are not saved in the
     *  model hand hence this has to be reset when reading the model
     *  again.
     *  @param decoratedAttributes The decorated attributes.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     */
    public void setTypesOfDecoratedVariables(
            DecoratedAttributes decoratedAttributes)
            throws IllegalActionException;
}
