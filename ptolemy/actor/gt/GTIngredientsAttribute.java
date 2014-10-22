/* An attribute that contains a list of GTIngredients and is associated with an
   entity in a transformation rule.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

import java.util.Collection;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// GTIngredientsAttribute

/**
 An attribute that contains a list of GTIngredients and is associated with an
 entity in a transformation rule.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTIngredientsAttribute extends StringAttribute {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public GTIngredientsAttribute() {
        _init();
    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GTIngredientsAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public GTIngredientsAttribute(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GTIngredientsAttribute newObject = (GTIngredientsAttribute) super
                .clone(workspace);
        try {
            newObject._parse();
        } catch (MalformedStringException ex) {
            throw new CloneNotSupportedException("Failed to clone: " + ex);
        }
        return newObject;
    }

    /** Parse the expression of this attribute if necessary and return the
     *  up-to-date ingredient list contained in this attribute.
     *
     *  @return The ingredient list.
     *  @exception MalformedStringException If error occurs while parsing the
     *   expression.
     */
    public GTIngredientList getIngredientList() throws MalformedStringException {
        if (!_parsed) {
            _parse();
        }
        return _ingredientList;
    }

    /** Set the expression of this attribute and defers parsing to the time when
     *  the ingredient list is obtained with {@link #getIngredientList()} or
     *  when {@link #validate()} is invoked.
     *
     *  @param expression The new expression.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
        _parsed = false;
        if (!(getContainer() instanceof GTEntity)) {
            setPersistent(!expression.equals(""));
        }
        super.setExpression(expression);
    }

    /** Parse the expression if necessary.
     *
     *  @return Always null.
     *  @exception IllegalActionException If error occurs while parsing the
     *   expression.
     */
    @Override
    public Collection<?> validate() throws IllegalActionException {
        if (!_parsed) {
            try {
                _parse();
            } catch (MalformedStringException e) {
                throw new IllegalActionException(e.getMessage());
            }
        }
        return null;
    }

    /** Initialize this attribute when the constructor is executed.
     */
    private void _init() {
        setClassName("ptolemy.actor.gt.GTIngredientsAttribute");
        setVisibility(EXPERT);
    }

    /** Parse the expression.
     *
     *  @exception MalformedStringException If error occurs while parsing the
     *   expression.
     */
    private void _parse() throws MalformedStringException {
        _parsed = true;
        _ingredientList = GTIngredientList.parse(this, super.getExpression());
    }

    /** The ingredient list obtained from the last expression parsing.
     */
    private GTIngredientList _ingredientList = null;

    /** Indicate whether the current expression has been parsed.
     */
    private boolean _parsed = false;

}
