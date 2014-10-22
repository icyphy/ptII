/* An attribute for defining a data type for a port.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TypeAttribute

/**
 An attribute for defining a data type for a port.
 Use setExpression() to define a data type, as in for example,
 <pre>
 attribute.setExpression("double");
 </pre>
 The class TypedIOPort recognizes when this attribute is inserted
 and calls setTypeEquals() to define its type. In the above example,
 the type of the port is set to double.
 <p>
 The type can be given by any expression that can be evaluated.
 In fact, it would be equally valid to specify the type to be double
 using
 <pre>
 attribute.setExpression("0.0");
 </pre>
 The Constants class defines the constant "double" to have the value
 0.0, so that instead you may give the type by name.
 The Constants class defines for convenience the following
 constants: boolean, complex, double, fixedpoint, general,
 int, long, matrix, object, scalar, string, and unknown.
 The constant "unknown" has a rather special behavior, in that
 it sets the type of the port to be unknown, allowing type resolution
 to infer it.  The constant "matrix" designates a matrix
 without specifying an element type, in contrast to, for example,
 "[double]", which specifies a double matrix.
 Similarly, the constant "scalar" designates a scalar of any type
 (double, int, long, etc.).
 The constant "general" designates any type.
 <p>
 Since the type is given by a "prototype" (an expression with the
 appropriate type), any data type that can be given in an expression
 can be specified as a type. For structured types, follow the
 same syntax as in expressions. For example:
 <pre>
 {double} - double array
 [int]    - int matrix
 {field1 = string, field2 = int} - record with two fields
 </pre>
 <p>
 This attribute is a singleton in a strong sense.
 When its container is set, if the container contains any other instance
 of TypeAttribute, that other instance is removed.

 @author Edward A. Lee, Xiaojun Liu
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see TypedIOPort
 @see ptolemy.data.expr.Constants
 */
public class TypeAttribute extends Parameter {
    /** Construct a parameter in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public TypeAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given name contained by the specified
     *  port. The container argument must not be null, or a
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
    public TypeAttribute(TypedIOPort container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct an attribute with the given name contained by the specified
     *  port. The container argument must not be null, or a
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
    public TypeAttribute(Attribute container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to remove any other instances of TypeAttribute.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedIOPort, or if the base class throws it.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute that is not an instance
     *   of TypeAttribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        _checkContainer(container);
        try {
            workspace().getWriteAccess();
            if (container != null) {
                List<TypeAttribute> attributes = container
                        .attributeList(TypeAttribute.class);
                for (TypeAttribute attribute : attributes) {
                    if (attribute != this) {
                        try {
                            attribute.setContainer(null);
                        } catch (NameDuplicationException e) {
                            throw new InternalErrorException(e);
                        }
                    }
                }
            }
            NamedObj previousContainer = getContainer();
            if (container != previousContainer) {
                // Container is changing. Restore the previous
                // container to its previous type, and record
                // the previous type of the new container.
                if (previousContainer instanceof Typeable) {
                    // Notice that if the previous type is UNKNOWN, then this
                    // delegate to type resolution.
                    ((Typeable) previousContainer).setTypeEquals(_previousType);
                    if (previousContainer instanceof Actor) {
                        Director previousDirector = ((Actor) previousContainer)
                                .getDirector();
                        previousDirector.invalidateResolvedTypes();
                    }
                }
                if (container instanceof Typeable) {
                    InequalityTerm term = ((Typeable) container).getTypeTerm();
                    if (term.isSettable()) {
                        // If the type term is a variable, then we want to
                        // delegate to type resolution.
                        _previousType = BaseType.UNKNOWN;
                    } else {
                        // If the type term is a constant, then we want to record
                        // the constant.
                        _previousType = ((Typeable) container).getType();
                    }
                    if (container instanceof Actor) {
                        Director director = ((Actor) container).getDirector();
                        director.invalidateResolvedTypes();
                    }
                }
            }
            super.setContainer(container);
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a TypedIOPort or a Kepler PortAttribute.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedIOPort, or if the base class throws it.
     */
    @Override
    protected void _checkContainer(NamedObj container)
            throws IllegalActionException {
        if (container != null && !(container instanceof TypedIOPort)) {
            // FIXME: this is a bit of hack brought on by
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4767
            if (!container.getClass().getName()
                    .equals("org.kepler.moml.PortAttribute")) {
                throw new IllegalActionException(
                        container,
                        this,
                        "TypeAttribute can only be contained by instances "
                                + "of TypedIOPort or org.kepler.moml.PortAttribute.");

            }

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Previous resolved type before this attribute was added to its current container. */
    private Type _previousType = BaseType.UNKNOWN;
}
