/* A class that contains a number of decorated attributes.

 Copyright (c) 2009-2011 The Regents of the University of California.
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

package ptolemy.domains.giotto.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

////DecoratedAttributesImplementation2
// This implementation leverages the code written by Bert Rodiers in DecoratedAttributesImplementation.java

/**
A class that decorates all the actors seen by the Giotto director.
This is a modification of Bert's DecoratedAttributesImplementation class.

The functionality is divided in two classes (DecoratedAttributes
and this class) to solve dependency issues.
See DecoratedAttributes for more information.

@author  Shanna-Shaye Forbes based on the DecoratedAttributesImplementation.java by Bert Rodiers.
@version $Id$
@since Ptolemy II 8.1
@Pt.ProposedRating Red (sssf)
@Pt.AcceptedRating Red (sssf)
 */

public class GiottoDecoratedAttributesImplementation2 extends
        DecoratedAttributes {

    /** Construct a DecoratedAttribute from the container and the decorator.
     *  @param container The container this object.
     *  @param decorator The decorator.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GiottoDecoratedAttributesImplementation2(NamedObj container,
            Decorator decorator) throws IllegalActionException,
            NameDuplicationException {
        super(container, decorator.getFullName().replaceAll("\\.", "_"));

        //Get all the actors seen by the Director and add a WCET attribute paramater equal to the default execution time value
        //also add a parameter to the container that has the WCET for all the actors inside... ie this will propogate information
        //out
        if (_debugging) {
            _debug("inside GiottoDecoratedAttributesImplementation Constructor.");
            _debug("container has name " + container.getDisplayName()
                    + " decorator has name " + decorator.getDisplayName());
        }
        Parameter dummyParam;
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) container
                .getContainer()).deepEntityList()) {
            NamedObj temp = (NamedObj) actor;
            if (_debugging) {
                _debug("temp has name " + temp.getDisplayName());
            }

            try {

                dummyParam = new Parameter(temp, "WCET");
                dummyParam.setTypeEquals(BaseType.DOUBLE);
                dummyParam.setExpression(Double.toString(0.0));

            } catch (NameDuplicationException e) {
                if (_debugging) {
                    _debug(actor.getFullName()
                            + " already had wcet parameter appended");
                }
            }

            try {
                dummyParam = new Parameter(temp, "executionTime");
                dummyParam.setTypeEquals(BaseType.DOUBLE);
                dummyParam.setExpression(Double.toString(0.0));

            } catch (NameDuplicationException e) {// "ExecutionTime parameter already exists so just determine the value of WCET with _getWCET and set it"

                if (_debugging) {
                    _debug(actor.getFullName()
                            + " already had et parameter appended");
                }
            }

        }
        // end of label all the actors with WCET

        double dirWCET = 0.0;//tempDir.getWCET();
        NamedObj parentContainer = null;
        try {
            //add parameter to the container if it's not already there
            if (_debugging) {
                _debug("the container is "
                        + container.getContainer().getDisplayName()
                        + " it should get value " + dirWCET);
            }

            parentContainer = container.getContainer();

            if (parentContainer.getClassName().contains("Refinement")) {

                parentContainer = parentContainer.getContainer();
            }

            dummyParam = new Parameter(parentContainer, "executionTime");
            dummyParam.setTypeEquals(BaseType.DOUBLE);
            dummyParam.setExpression(Double.toString(dirWCET));

            dummyParam = new Parameter(parentContainer, "WCET");
            dummyParam.setTypeEquals(BaseType.DOUBLE);
            dummyParam.setExpression(Double.toString(dirWCET));

            parentContainer = container.getContainer().getContainer();

            if (parentContainer instanceof ptolemy.domains.modal.modal.ModalModel) {
                dummyParam = new Parameter(parentContainer, "WCET");
                dummyParam.setTypeEquals(BaseType.DOUBLE);
                dummyParam.setExpression(Double.toString(dirWCET));
            }

        } catch (NameDuplicationException e) {
            if (_debugging) {
                _debug("container already had wcet parameter set so updating the value to "
                        + dirWCET);
            }
            dummyParam = (Parameter) parentContainer.getAttribute("WCET");
            dummyParam.setTypeEquals(BaseType.DOUBLE);
            dummyParam.setExpression(Double.toString(dirWCET));

            parentContainer = container.getContainer().getContainer();

            if (parentContainer instanceof ptolemy.domains.modal.modal.ModalModel) {

                try {
                    dummyParam = new Parameter(parentContainer, "WCET");
                    dummyParam.setTypeEquals(BaseType.DOUBLE);
                    dummyParam.setExpression(Double.toString(dirWCET));
                } catch (NameDuplicationException E) {
                    dummyParam = (Parameter) parentContainer
                            .getAttribute("WCET");
                    dummyParam.setTypeEquals(BaseType.DOUBLE);
                    dummyParam.setExpression(Double.toString(dirWCET));
                }
            }
        }
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
    public GiottoDecoratedAttributesImplementation2(
            NamedObj containerOfDecorator, String name)
            throws IllegalActionException, NameDuplicationException {
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
        if (_debugging) {
            _debug("attribute changed method called");
        }
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
     *  @exception CloneNotSupportedException If unable to clone
     *  from the super class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GiottoDecoratedAttributesImplementation2 newObject = (GiottoDecoratedAttributesImplementation2) super
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
            if (p.getName().equals("_decorator")) {
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
