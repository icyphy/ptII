/* A list of GTIngredients.

@Copyright (c) 2007-2012 The Regents of the University of California.
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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// GTIngredientList

/**
 A list of GTIngredients. Such a list is contained in a GTIngredientsAttribute,
 which is associated with special entities in a transformation rule, such as
 AtomicActorMatcher and CompositeActorMatcher. GTIngredients in this list are
 either all criteria or all operations.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTIngredientList extends LinkedList<GTIngredient> {

    /** Construct an empty GTIngredientList contained by the given attribute.
     *
     *  @param owner The attribute that contains this list of ingredients.
     */
    public GTIngredientList(GTIngredientsAttribute owner) {
        _owner = owner;
    }

    /** Construct a GTIngredientList contained by the given attribute with some
     *  initial ingredients.
     *
     *  @param owner The attribute that contains this list of ingredients.
     *  @param ingredients The initial ingredients, which should be all criteria
     *   or all operations.
     */
    public GTIngredientList(GTIngredientsAttribute owner,
            GTIngredient... ingredients) {
        for (GTIngredient rule : ingredients) {
            add(rule);
        }
        _owner = owner;
    }

    /** Construct a GTIngredientList contained by the given attribute, and add
     *  all the ingredients in the template to this one.
     *
     *  @param owner The attribute that contains this list of ingredients.
     *  @param template Another GTIngredientList whose ingredients are to be
     *   added to this one. The references of those ingredients are copied. No
     *   cloning is done.
     */
    public GTIngredientList(GTIngredientsAttribute owner,
            GTIngredientList template) {
        super(template);
        _owner = owner;
    }

    /** Get the GTIngredientsAttribute that contain this list.
     *
     *  @return The GTIngredientsAttribute that contain this list.
     */
    public GTIngredientsAttribute getOwner() {
        return _owner;
    }

    /** Parse the given expression and construct a GTIngredientList in the given
     *  GTIngredientsAttribute.
     *
     *  @param owner The attribute that contains the constructed list.
     *  @param expression The expression to parse.
     *  @return The constructed list.
     *  @exception MalformedStringException If the expression is malformed.
     */
    public static GTIngredientList parse(GTIngredientsAttribute owner,
            String expression) throws MalformedStringException {
        GTIngredientList list = new GTIngredientList(owner);
        int startPos = 0;
        while (startPos < expression.length()) {
            int endPos = GTIngredient._findMatchingParen(expression, startPos);
            if (endPos < 0) {
                throw new MalformedStringException(expression);
            }

            String ruleString = expression.substring(startPos + 1, endPos);
            int separator = ruleString.indexOf(GTIngredient.FIELD_SEPARATOR);
            GTIngredient rule = null;
            if (separator >= 0) {
                String ruleClassName = ruleString.substring(0, separator);
                try {
                    Class<?> namedClass = Class.forName(ruleClassName);
                    Class<? extends GTIngredient> ruleClass = namedClass
                            .asSubclass(GTIngredient.class);
                    String values = ruleString.substring(separator + 1);
                    rule = ruleClass.getConstructor(GTIngredientList.class,
                            String.class).newInstance(list, values);
                } catch (ClassNotFoundException e) {
                } catch (IllegalAccessException e) {
                } catch (InstantiationException e) {
                } catch (InvocationTargetException e) {
                } catch (NoSuchMethodException e) {
                }
            }

            if (rule != null) {
                list.add(rule);
            }
            startPos = endPos + 1;
        }
        return list;
    }

    /** Return a string that describes all the ingredients within this list.
     *
     *  @return The string that describes all the ingredients within this list.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (GTIngredient rule : this) {
            buffer.append('(');
            buffer.append(rule.getClass().getName());
            buffer.append(GTIngredient.FIELD_SEPARATOR);
            buffer.append(rule.getValues());
            buffer.append(')');
        }
        return buffer.toString();
    }

    /** Check validity of the contained ingredients.
     *
     *  @exception ValidationException If some ingredients are invalid.
     */
    public void validate() throws ValidationException {
        int i = 0;
        for (GTIngredient rule : this) {
            i++;
            try {
                rule.validate();
            } catch (ValidationException e) {
                throw new ValidationException("Rule " + i + ": "
                        + e.getMessage());
            }
        }
    }

    /** The attribute that contain this list of ingredients.
     */
    private GTIngredientsAttribute _owner;
}
