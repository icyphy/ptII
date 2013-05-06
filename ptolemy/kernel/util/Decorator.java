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

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Decorator

/**
A decorator is a class that decorates other instances of NamedObj
with extra attributes that are specific to both the decorator
and the NamedObj. Those extra attributes are contained by an
attribute of class {@link DecoratorAttributes} that is created
by calling {@link #createDecoratorAttributes(NamedObj)} and specifying
the object that will contain the additional attributes.
The decorated NamedObj will contain these instances of DecoratorAttributes.
These attributes are stored separately and can be retrieved by using
{@link NamedObj#getDecoratorAttributes(Decorator)} or
{@link NamedObj#getDecoratorAttributes(Decorator)}.

<p>For a description of a decorator pattern, see
<a href="http://en.wikipedia.org/wiki/Decorator_pattern">http://en.wikipedia.org/wiki/Decorator_pattern</a>.

@author Bert Rodiers
@author Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public interface Decorator extends Nameable {

    /** Create and return the decorated attributes for the target NamedObj.
     *  Implementations of this method should create an Attribute that implements
     *  {@link DecoratorAttributes}. Implementations should populate that attribute
     *  with parameters that have appropriate default values.
     *  <p>
     *  This method is called if {@link NamedObj#getDecoratorAttribute(Decorator, String)}
     *  or {@link NamedObj#getDecoratorAttributes(Decorator)} is called,
     *  and the specified target object does not already have
     *  decorated attributes for this decorator.
     *  <p>
     *  The implementer of this method is responsible for ensuring that any
     *  object returned by {@link #decoratedObjects()}, when passed as an argument
     *  to this method, will not result in a null returned value.
     *
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or null if the
     *   specified NamedObj is not decorated by this decorator.
     */
    public DecoratorAttributes createDecoratorAttributes(NamedObj target);
    
    /** Return a list of the objects that this decorator decorates. This could
     *  be, for example, all of the entities contained by the container of
     *  this decorator. An implementer of this method is responsible for ensuring
     *  that {@link #createDecoratorAttributes(NamedObj)} will not return null
     *  for any object included in the returned list.
     *  @return A list of the objects decorated by this decorator.
     */
    public List<NamedObj> decoratedObjects();
    
    /** Return true if this decorator should decorate objects across
     *  opaque hierarchy boundaries. That is, return true to make this
     *  decorator visible to objects even within opaque composites.
     *  @return True if decorator is global.
     */
    public boolean isGlobalDecorator();
}
