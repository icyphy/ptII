/* Attribute that defines a concept function by instantiating a ConceptFunction
 * Java class.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ClassConceptFunctionDefinitionAttribute

/** Attribute that defines a concept function by instantiating a ConceptFunction
 *  Java class.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ClassConceptFunctionDefinitionAttribute extends
ConceptFunctionDefinitionAttribute {

    /** Construct the ClassConceptFunctionDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ClassConceptFunctionDefinitionAttribute(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        conceptFunctionClassName = new StringAttribute(this,
                "conceptFunctionClassName");
        constructorArguments = new Parameter(this, "constructorArguments");
        constructorArguments.setTypeEquals(new ArrayType(BaseType.GENERAL));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the ConceptFunction class to be instantiated when a
     *  createConceptFunction() is called.
     */
    public StringAttribute conceptFunctionClassName;

    /** The array of arguments to be passed to the constructor when
     *  instantiating the ConceptFunction.
     */
    public Parameter constructorArguments;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the concept function defined by this attribute by instantiating
     *  an object of the class specified in the conceptFunctionClassName
     *  StringAttribute with the constructor arguments given in the
     *  constructorArguments parameter.
     *
     *  @return The concept function.
     *  @exception IllegalActionException If there is an error
     *   creating the conceptFunction.
     */
    @Override
    public ConceptFunction createConceptFunction()
            throws IllegalActionException {
        if (!(workspace().getVersion() == _conceptFunctionVersion)) {
            String conceptFunctionClassNameString = conceptFunctionClassName
                    .getValueAsString();
            if (conceptFunctionClassNameString == null
                    || conceptFunctionClassNameString.equals("")) {
                throw new IllegalActionException(this,
                        "ConceptFunction class name not specified.");
            }

            Class<? extends ConceptFunction> conceptFunctionClass = null;
            try {
                // Verify that the conceptFunctionClassName correctly
                // specifies an existing actor class.
                conceptFunctionClass = Class.forName(
                        conceptFunctionClassNameString).asSubclass(
                                ConceptFunction.class);
            } catch (ClassNotFoundException classEx) {
                throw new IllegalActionException(this, classEx,
                        "ConceptFunction class "
                                + conceptFunctionClassNameString
                                + " not found.");
            }
            _cachedConceptFunction = _createConceptFunctionInstance(conceptFunctionClass);
            _conceptFunctionVersion = workspace().getVersion();
        }
        return _cachedConceptFunction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the array of classes in the argumentClasses variable is
     *  compatible with the constructor argument list types in the
     *  constructorArgumentClasses variable.
     *  @param argumentClasses The array of classes to be tested against the
     *   argument list for a constructor.
     *  @param constructorArgumentClasses The array of classes that defined the
     *   argument list types for the constructor.
     *  @return true if the classes in argumentClasses can be assigned to the
     *   classes in constructorArgumentClasses and false otherwise.
     */
    private boolean _argumentsMatchConstructorClasses(Class[] argumentClasses,
            Class[] constructorArgumentClasses) {
        for (int i = 0; i < argumentClasses.length; i++) {
            Class constructorArgClass = constructorArgumentClasses[i];
            Class argClass = argumentClasses[i];
            if (!constructorArgClass.isAssignableFrom(argClass)) {
                return false;
            }
        }
        return true;
    }

    /** Return an instance of the specified ConceptFunction class. The constructor
     *  used will be determined by the array of objects in the constructorArguments
     *  parameter.
     *  @param conceptFunctionClass The class of the actor to be instantiated.
     *  @return A new instance of the specified actor class.
     *  @exception IllegalActionException If the actor class cannot be instantiated.
     */
    private ConceptFunction _createConceptFunctionInstance(
            Class<? extends ConceptFunction> conceptFunctionClass)
                    throws IllegalActionException {

        _setConstructorArgsArrays();

        Constructor<? extends ConceptFunction> conceptFunctionConstructor = null;
        try {
            conceptFunctionConstructor = conceptFunctionClass
                    .getConstructor(_constructorArgTypes);
        } catch (NoSuchMethodException ex) {
            // If there is no constructor that matches the object types exactly,
            // search for a constructor that is compatible if its argument
            // types are superclasses of the actual object types.
            conceptFunctionConstructor = _findCompatibleConstructor(
                    conceptFunctionClass, _constructorArgTypes);
        }

        ConceptFunction conceptFunctionInstance = null;
        try {
            conceptFunctionInstance = conceptFunctionConstructor
                    .newInstance(_constructorArgObjects);
        } catch (InvocationTargetException ex) {
            throw new IllegalActionException(this, ex, "Exception thrown when "
                    + "trying to call the constructor for the "
                    + "ConceptFunction class " + conceptFunctionClass + ".");
        } catch (IllegalArgumentException ex) {
            throw new IllegalActionException(this, ex, "Invalid argument "
                    + "passed to the constructor for the ConceptFunction "
                    + "class " + conceptFunctionClass + ".");
        } catch (InstantiationException ex) {
            throw new IllegalActionException(this, ex, "Unable to instantiate"
                    + " the ConceptFunction class " + conceptFunctionClass
                    + ".");
        } catch (IllegalAccessException ex) {
            throw new IllegalActionException(this, ex, "Do not have access "
                    + " the constructor for the ConceptFunction class "
                    + conceptFunctionClass + " within this method.");
        }

        return conceptFunctionInstance;
    }

    /** Find a constructor for the given ConceptFunction class that is compatible
     *  with the given array of argument types. Return the first constructor
     *  that can be called with an array of objects that match the types in the
     *  given array. These types may be subclasses of the types declared in
     *  the actual constructor.
     *  @param conceptFunctionClass The ConceptFunction class within which a
     *   suitable constructor should be found.
     *  @param constructorArgTypes The array of argument type classes that
     *   the constructor must be able to use to instantiate a ConceptFunction
     *   object of the given class.
     *  @return The constructor that matches the input argument type array if
     *   it exists.
     *  @exception IllegalActionException Thrown if no suitable constructor can
     *   be found for the given ConceptFunction class.
     */
    private Constructor<? extends ConceptFunction> _findCompatibleConstructor(
            Class<? extends ConceptFunction> conceptFunctionClass,
            Class[] constructorArgTypes) throws IllegalActionException {

        Constructor<? extends ConceptFunction> allConstructors[] = (Constructor<? extends ConceptFunction>[]) conceptFunctionClass
                .getConstructors();
        for (Constructor<? extends ConceptFunction> constructor : allConstructors) {
            Class argumentClasses[] = constructor.getParameterTypes();
            if (_argumentsMatchConstructorClasses(constructorArgTypes,
                    argumentClasses)) {
                return constructor;
            }
        }

        throw new IllegalActionException(this, "Could not find a constructor"
                + " method for the concept function class "
                + conceptFunctionClass + " with the given array of arguments.");
    }

    /** Set the array of objects that should be passed to the constructor
     *  of the ConceptFunction, and the array of argument types necessary for
     *  the constructor. These objects are taken from the array of tokens
     *  contained in the constructorArguments parameter.
     *  @exception IllegalActionException Thrown if the the constructorArguments
     *   parameter does not have a token specified.
     */
    private void _setConstructorArgsArrays() throws IllegalActionException {
        ArrayToken constructorArgArrayToken = (ArrayToken) constructorArguments
                .getToken();
        if (constructorArgArrayToken == null) {
            throw new IllegalActionException(this,
                    "The constructor arguments array is not specified.");
        } else {
            Token[] constructorArgTokens = constructorArgArrayToken
                    .arrayValue();
            int arraySize = constructorArgTokens.length;
            _constructorArgObjects = new Object[arraySize];
            _constructorArgTypes = new Class[arraySize];
            for (int i = 0; i < constructorArgTokens.length; i++) {
                _getCorrectObjectFromToken(constructorArgTokens[i], i);
            }
        }
    }

    /** Get the correct object from the given token for the specified index
     *  in the constructor arguments array. Also set the class type for the
     *  constructor types array. For any primitive Java types represented by
     *  Ptolemy tokens, the primitive class type must be specified for the
     *  constructor argument type array, and not the Object wrapper type. If
     *  this is not done, the correct constructor will not be found when the
     *  constructor has any Java primitive data type arguments in its
     *  signature.
     *  @param token The token that represents one element of the constructor
     *   arguments object array specified by the user in the Ptolemy model.
     *  @param arrayIndex The index location in the argument list for the
     *   constructor
     *  @exception IllegalActionException Thrown if the Ptolemy token is not
     *   recognized and cannot be converted to the correct Java object.
     */
    private void _getCorrectObjectFromToken(Token token, int arrayIndex)
            throws IllegalActionException {
        Object value = null;
        if (token instanceof ObjectToken) {
            value = ((ObjectToken) token).getValue();
            _constructorArgTypes[arrayIndex] = value.getClass();
        } else if (token instanceof BooleanToken) {
            value = ((BooleanToken) token).booleanValue();
            _constructorArgTypes[arrayIndex] = java.lang.Boolean.TYPE;
        } else if (token instanceof StringToken) {
            value = ((StringToken) token).stringValue();
            _constructorArgTypes[arrayIndex] = value.getClass();
        } else if (token instanceof IntToken) {
            value = ((IntToken) token).intValue();
            _constructorArgTypes[arrayIndex] = java.lang.Integer.TYPE;
        } else if (token instanceof LongToken) {
            value = ((LongToken) token).longValue();
            _constructorArgTypes[arrayIndex] = java.lang.Long.TYPE;
        } else if (token instanceof DoubleToken) {
            value = ((DoubleToken) token).doubleValue();
            _constructorArgTypes[arrayIndex] = java.lang.Double.TYPE;
        } else if (token instanceof FloatToken) {
            value = ((FloatToken) token).floatValue();
            _constructorArgTypes[arrayIndex] = java.lang.Float.TYPE;
        } else if (token instanceof ShortToken) {
            value = ((ShortToken) token).shortValue();
            _constructorArgTypes[arrayIndex] = java.lang.Short.TYPE;
        } else {
            throw new IllegalActionException(this, "Unrecognized token type "
                    + token.getType() + " for this token: " + token);
        }

        _constructorArgObjects[arrayIndex] = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current version of the concept function. */
    private Long _conceptFunctionVersion = -1L;

    /** The last valid ConceptFunction object created by this attribute. */
    private ConceptFunction _cachedConceptFunction;

    /** The array of objects to be passed to the constructor for the
     *  ConcepFunction.
     */
    private Object[] _constructorArgObjects;

    /** The array of class types for the objects to be passed to the
     *  ConceptFunction constructor.
     */
    private Class[] _constructorArgTypes;
}
