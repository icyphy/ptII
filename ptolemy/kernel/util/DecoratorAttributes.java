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


///////////////////////////////////////////////////////////////////
//// DecoratorAttributes

/**
A container for attributes created by a decorator.
This is an attribute that will be contained by a target object that is
decorated by a decorator. The parameters that the decorator creates
will be contained by an instance of this object.
These attributes can be retrieved by using
{@link NamedObj#getDecoratorAttribute(Decorator,String)} or
{@link NamedObj#getDecoratorAttributes(Decorator)}.</p>

@author Bert Rodiers
@author Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (rodiers)
*/
public class DecoratorAttributes extends Attribute {
    
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
     *   an attribute already in the container.
     */
    public DecoratorAttributes(NamedObj container, Decorator decorator)
            throws IllegalActionException, NameDuplicationException {
        super(container, container.uniqueName("DecoratorAttributesFor_" + decorator.getName()));
        _decorator = decorator;
        
        decoratorName = new StringAttribute(this, "decoratorName");
        decoratorName.setVisibility(Settable.NONE);
        // Record the name relative to the toplevel entity so that even if you
        // do SaveAs (which changes the name of the toplevel) the decorator
        // can still be found.
        // FIXME: If you save a submodel, this will break the connection
        // to the decorator.
        decoratorName.setExpression(decorator.getName(container.toplevel()));
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

    /** The full name of the decorator, to be stored in a MoML file
     *  to re-establish the connection with a decorator after saving
     *  and re-parsing the file. This is a string that is not visible
     *  to the user.
     */
    public StringAttribute decoratorName;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorator.
     *  @return The decorator, or null if there is none.
     */
    public Decorator getDecorator() {
        if (_decorator == null) {
            // Retrieve the decorator using the decoratorName parameter.
            String name = decoratorName.getExpression();
            if (name != null && !name.equals("")) {
                try {
                    // Name is relative to the toplevel.
                    _decorator = (Decorator)toplevel().getAttribute(name, Decorator.class);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
        }
        return _decorator;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The decorator.*/
    private Decorator _decorator = null;
}
