/* A class that represents a decorated attribute.

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
////DecoratedAttribute

/**
A class that a decorated attribute. A decorated attribute
has a decoratorName, which refers to the decorator and a
attribute, which is a decorated attribute.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class DecoratedAttribute {
    // Remark: There are multiple possible design for this, but they all have
    // some disadvantages.
    // A first option would be to make DecoratedAttribute interface. It then sounds
    // reasonable to inherit from Attribute (a DecoratedAttribute is also an Attribute).
    // However this means that DecoratedAttribute can no longer be an interface since
    // Attribute is also not an interface. Moreover if we would derive from Attribute,
    // this DecoratedAttribute couldn't be aParameter and we do want DecoratedAttribute
    // that are parameters.
    // The second option would be to create an interface Decorable,
    // create a class DecoratedAttribute that implements Decorable and extents Attribute
    // and create a class DecoratedParameter that implements Decorable and extents Parameter.
    // If we for example need to have a DecoratedVariable, we need to do the same thing and so on...
    // In other words we need to replicate the whole Parameter hierarchy and decorator specifics need to
    // be implemented in each implementation.
    // We opted for a third option. Created one class DecoratedAttribute that encapsulates an Attribute
    // (and hence it can also encapsulate a Parameter, a Varable, ...) and implement all common specifics
    // in that one class. The advantage is the we only need to create one class and have a very simple class
    // hierarchy. The disadvantage is that there is one level of indirection for the users of this class
    // and decorated attributes have to be treated in a special way.

    /** Construct a DecoratedAttribute from the name of the decorator and an attribute
     * (the actual decorated attribute).
     * @param decorator The decorator.
     * @param attribute The actual decorated attribute.
     */
    public DecoratedAttribute(Attribute attribute, Decorator decorator) {
        _decorator = decorator;
        _attribute = attribute;
        _attribute.setDisplayName(_attribute.getDisplayName() + " (decorated by " + _decorator.getDecoratorName() +  ")");
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorated attribute.
     * @return The decorated attribute.
     */
    public Attribute getAttribute() {
        return _attribute;
    }

    /** Return the name of the decorator.
     * @return The name of the decorator.
     */
    public String getDecoratorName() {
        return _decorator.getDecoratorName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The decorator name.*/
    private Attribute _attribute;

    /** The decorater.*/
    private Decorator _decorator;
}
