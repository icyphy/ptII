/* An Functional Mock-up Interface ScalarVariable.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package org.ptolemy.fmi;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import org.ptolemy.fmi.type.FMIType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

///////////////////////////////////////////////////////////////////
//// FMIScalarVariable

/**
 * An object that represents the ScalarVariable element of a
 * modelDescription.xml file contained within a
 * Functional Mock-up Interface .fmu XML file.
 *
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In that file,
 * the ModelVariables element may contain elements such as
 * ScalarVariable.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMIScalarVariable {

    /** Create an empty ScalarVariable. */
    public FMIScalarVariable() {
    }

    /** Create a ScalarVariable from an XML Element.
     *  @param fmiModelDescription the Model Description for this variable.
     *  @param element The XML Element that contains attributes.
     */
    public FMIScalarVariable(FMIModelDescription fmiModelDescription,
            Element element) {
        this.fmiModelDescription = fmiModelDescription;
        name = element.getAttribute("name");
        description = element.getAttribute("description");

        alias = Alias.noAlias;
        if (element.hasAttribute("alias")) {
            String attribute = element.getAttribute("alias");
            if (attribute.equals("alias")) {
                alias = Alias.alias;
            } else if (attribute.equals("negatedAlias")) {
                // In bouncingBall, 'g' has a negatedAlias.
                alias = Alias.negatedAlias;
            } else if (attribute.equals("noAlias")) {
                // FIXME: I'm not sure if alias="noAlias" ever appears
                alias = Alias.noAlias;
            } else {
                throw new IllegalArgumentException("alias \"" + attribute
                        + "\" must be one of alias, negatedAlias or noAlias"
                        + " in " + name + ", " + description);
            }
        }

        causality = Causality.internal;
        // We have fmiVersion=1.0, 1.5 and 2.0
        if (fmiModelDescription.fmiVersion.compareTo("2.0") >= 0) {
            causality = Causality.local;
        }

        if (element.hasAttribute("causality")) {
            String attribute = element.getAttribute("causality");

            // FIXME: Check this for 2.0:
            String choices = "calculatedParameter, input, internal, local, output, none or parameter";
            if (fmiModelDescription.fmiVersion.compareTo("2.0") < 0) {
                choices = "input,  internal, output, or none";
            }
            String message = "must be one of " + choices + " in " + name + ", "
                    + description;

            if (attribute.equals("calculatedParameter")) {
                // FMI-2.0rc1
                _fmi2AttributeCheck(fmiModelDescription, attribute, message);
                causality = Causality.calculatedParameter;
            } else if (attribute.equals("input")) {
                causality = Causality.input;
            } else if (attribute.equals("internal")) {
                causality = Causality.internal;
            } else if (attribute.equals("local")) {
                // FMI-2.0rc1
                _fmi2AttributeCheck(fmiModelDescription, attribute, message);
                causality = Causality.local;
            } else if (attribute.equals("output")) {
                causality = Causality.output;
            } else if (attribute.equals("none")) {
                causality = Causality.none;
            } else if (attribute.equals("parameter")) {
                // FMI-2.0rc1
                _fmi2AttributeCheck(fmiModelDescription, attribute, message);
                causality = Causality.parameter;
            } else {
                throw new IllegalArgumentException("causality \"" + attribute
                        + "\" " + message);
            }
        }

        if (element.hasAttribute("valueReference")) {
            String valueReferenceString = element
                    .getAttribute("valueReference");
            try {
                valueReference = Long.parseLong(valueReferenceString);
            } catch (NumberFormatException ex) {
                throw new NumberFormatException(
                        "Failed to parse valueReference "
                                + valueReferenceString + " of " + name);
            }
        }
        if (element.hasAttribute("variability")) {
            String attribute = element.getAttribute("variability");

            // FIXME: Check this for 2.0:
            String choices = "constant, continuous, fixed, discrete or tunable.";
            if (fmiModelDescription.fmiVersion.compareTo("2.0") < 0) {
                choices = "constant, continuous, discrete or parameter.";
            }
            String message = "must be one of " + choices + " in " + name + ", "
                    + description;

            if (attribute.equals("constant")) {
                variability = Variability.constant;
            } else if (attribute.equals("continuous")) {
                variability = Variability.continuous;
            } else if (attribute.equals("discrete")) {
                variability = Variability.discrete;
            } else if (attribute.equals("parameter")) {
                variability = Variability.parameter;
            } else if (attribute.equals("fixed")) {
                // FMI-2.0
                _fmi2AttributeCheck(fmiModelDescription, attribute, message);
                variability = Variability.fixed;
            } else if (attribute.equals("tunable")) {
                variability = Variability.tunable;
            } else {
                throw new IllegalArgumentException("variability \"" + attribute
                        + "\"" + message);
            }
        }

        NodeList children = element.getChildNodes(); // NodeList. Worst. Ever.
        for (int i = 0; i < children.getLength(); i++) {
            Node child = element.getChildNodes().item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                // Check to see if the childElement is DirectDependency.  If it is, then process
                // it.  If not, then we assume that it is a type and set _typeName.
                // There was a bug where a modelDescription.xml file from Dymola had
                // <ScalarVariable
                //  name="Troo_1"
                //  valueReference="335544320"
                //  causality="output">
                //  <Real
                //   declaredType="Modelica.Blocks.Interfaces.RealOutput"
                //   unit="degC"
                //   min="-273.15"/>
                // <DirectDependency/>
                // </ScalarVariable>
                // and we got:
                // Error looking up function 'stepCounter_fmiGetDirectDependency': dlsym(0x7fc0ea0091d0, stepCounter_fmiGetDirectDependency): symbol not found
                //
                String nodeName = childElement.getNodeName();
                if (nodeName.equals("DirectDependency")) {
                    if (fmiModelDescription.fmiVersion.compareTo("1.0") == 0) {   
                    	System.out.println("!!!!!#!#@#!@#%!@#^$#$$%*&^&$^#@$%#@$!@^%&*%()&(^*&*^%&$^#%@#@!#$&(*^)(*&(&*^&%^#%@#&^$*^&(*(&*^%%$^$#%$*&%(");
                        // Iterate over the children of this element to find the
                        // names of the dependents.
                        // FIXME: In FMI 2.0, DirectDependency will be replaced by
                        // "dependencies" in the ModelStructure element of the model description.
                        directDependency = new HashSet<String>();
                        NodeList names = childElement.getChildNodes();
                        for (int j = 0; j < names.getLength(); j++) {
                            Node name = element.getChildNodes().item(i);
                            if (name instanceof Element) {
                                String childType = ((Element) name)
                                        .getNodeName();
                                if (childType.equals("Name")) {
                                    // FIXME: Is getNodeValue() the way to get "foo"
                                    // from <Name>foo</Name>?
                                    directDependency.add(((Element) child)
                                            .getNodeValue());
                                }
                            }
                        }
                    }
                } 
                else if (nodeName.equals("InputDependency")) {
                	if (fmiModelDescription.fmiVersion.compareTo("2.0") == 0) {
                        // Iterate over the children of this element to find the
                        // names of the dependents.
                        // FIXME: In FMI 2.0, DirectDependency will be replaced by
                        // "dependencies" in the ModelStructure element of the model description.
                        directDependency = new HashSet<String>();
                        NodeList names = childElement.getChildNodes();
                        for (int j = 0; j < names.getLength(); j++) {
                            Node name = element.getChildNodes().item(i);
                            if (name instanceof Element) {
                                String childType = ((Element) name)
                                        .getNodeName();
                                if (childType.equals("Name")) {
                                    // FIXME: Is getNodeValue() the way to get "foo"
                                    // from <Name>foo</Name>?
                                    directDependency.add(((Element) child)
                                            .getNodeValue());
                                }
                            }
                        }
                    }
            	} else if (nodeName.equals("isLinear")
                        || nodeName.equals("VariableCategory")) {
                    if (!_errorElements.contains(_typeName)) {
                        _errorElements.add(_typeName);
                        System.out.println(element + ": Child element \""
                                + nodeName + "\" not implemented yet.");
                    }
                } else {
                    _typeName = nodeName;
                    if (_typeName.equals("Boolean")) {
                        type = new FMIBooleanType(name, description,
                                childElement);
                    } else if (_typeName.equals("Enumeration")) {
                        type = new FMIIntegerType(name, description,
                                childElement);
                        _typeName = "Integer";
                    } else if (_typeName.equals("Integer")) {
                        type = new FMIIntegerType(name, description,
                                childElement);
                    } else if (_typeName.equals("Real")) {
                        type = new FMIRealType(name, description, childElement);
                    } else if (_typeName.equals("String")) {
                        type = new FMIStringType(name, description,
                                childElement);
                    } else {
                        if (!_errorElements.contains(_typeName)) {
                            _errorElements.add(_typeName);
                            System.out.println(element + ": Child element \""
                                    + _typeName + "\" not implemented yet.");
                        }
                        _typeName = "skip";
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this variable as a boolean.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @return the value of this variable as boolean.
     *  @see #setBoolean(Pointer, boolean)
     */
    public boolean getBoolean(Pointer fmiComponent) {
        IntBuffer valueBuffer = IntBuffer.allocate(1);
        _getValue(fmiComponent, valueBuffer, FMIBooleanType.class);
        return valueBuffer.get(0) != 0;
    }

    /** Return the value of this variable as a double.
     *  If the variable is of type FMIIntegerType,
     *  the the integer value is cast to a double.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @return the value of this variable as double.
     *  @see #setDouble(Pointer, double)
     */
    public double getDouble(Pointer fmiComponent) {
        double result;
        //IntBuffer valueReferenceIntBuffer = IntBuffer.allocate(1).put(0,
        //        (int) valueReference);
        if (type instanceof FMIIntegerType) {
            IntBuffer valueBuffer = IntBuffer.allocate(1);
            _getValue(fmiComponent, valueBuffer, FMIIntegerType.class);
            result = valueBuffer.get(0);
        } else if (type instanceof FMIRealType) {
            DoubleBuffer valueBuffer = DoubleBuffer.allocate(1);
            _getValue(fmiComponent, valueBuffer, FMIRealType.class);
            result = valueBuffer.get(0);
        } else {
            // FIXME: Why a runtime exception?
            throw new RuntimeException("Type " + type + " not supported.");
        }
        return result;
    }

    /** Return the value of this variable as an int.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @return the value of this variable as an int.
     *  @see #setInt(Pointer, int)
     */
    public int getInt(Pointer fmiComponent) {
        IntBuffer valueBuffer = IntBuffer.allocate(1);
        _getValue(fmiComponent, valueBuffer, FMIIntegerType.class);
        return valueBuffer.get(0);
    }

    /** Return the value of this variable as a String.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @return the value of this variable as a String.
     *  @see #setString(Pointer, String)
     */
    public String getString(Pointer fmiComponent) {
        PointerByReference pointerByReference = new PointerByReference();
        _getValue(fmiComponent, pointerByReference, FMIStringType.class);
        Pointer reference = pointerByReference.getValue();
        String result = null;
        if (reference != null) {
            // If _fmiGetString is not supported, then we might
            // have reference == null.
            result = reference.getString(0);
        }
        return result;
    }

    /** Set the value of this variable as a boolean.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param value The value of this variable.
     *  @see #getBoolean(Pointer fmiComponent)
     */
    public void setBoolean(Pointer fmiComponent, boolean value) {
        IntBuffer valueBuffer = IntBuffer.allocate(1).put(0,
                value ? (byte) 1 : (byte) 0);
        _setValue(fmiComponent, valueBuffer, FMIBooleanType.class);
    }

    /** Set the value of this variable as a double.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param value The value of this variable.
     *  @see #getDouble(Pointer)
     */
    public void setDouble(Pointer fmiComponent, double value) {
        DoubleBuffer valueBuffer = DoubleBuffer.allocate(1).put(0, value);
        _setValue(fmiComponent, valueBuffer, FMIRealType.class);
    }

    /** Set the value of this variable as an integer.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param value The value of this variable.
     *  @see #getInt(Pointer)
     */
    public void setInt(Pointer fmiComponent, int value) {
        IntBuffer valueBuffer = IntBuffer.allocate(1).put(0, value);
        // FIXME: What about enums?
        _setValue(fmiComponent, valueBuffer, FMIIntegerType.class);
    }

    /** Set the value of this variable as a String.
     *  This method allocates memory, the caller should eventually
     *  call FMIModelDescription.dispose().
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param value The value of this variable.
     *  @see #getString(Pointer)
     */
    public void setString(Pointer fmiComponent, String value) {
        PointerByReference pointerByReference = new PointerByReference();
        // We use FMUAllocateMemory so that we can retain a reference
        // to the allocated memory and the memory does not get gc'd.

        // Include the trailing null character.
        Pointer reference = fmiModelDescription.getFMUAllocateMemory().apply(
                new NativeSizeT(value.length() + 1), new NativeSizeT(1));
        reference.setString(0, value);
        pointerByReference.setValue(reference);

        _setValue(fmiComponent, pointerByReference, FMIStringType.class);
    }
    
//    public String getTypeName() {
//    	return _typeName;
//    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Acceptable values for the alias xml attribute.
     *  Alias variables occur during assignment operations.
     */
    public enum Alias {
        /** This is an alias variable, use the valueReference handle
         * to set or get the actual value.
         */
        alias,
        /** This is an alias value, but the value returned must be
         * negated.
         */
        negatedAlias,
        /** This is not an alias (the default). */
        noAlias
    };

    /** Acceptable values for the causality xml attribute.
     *  Causality defines the visibility of the variable from outside of the model.
     */
    public enum Causality {
        /** New in FMI-2.0rc1. */
        calculatedParameter,
        /** The value is defined from the outside.  The value is
         * initially the value of the start attribute.
         */
        input,
        /** After initialization, a result may be stored.
         *  However, the value cannot be used in a connection.
         *  The default Causality is "internal".
         */
        internal,
        /** New in FMI-2.0rc1. */
        local,
        /** The value can be read from the outside with a connection.
         */
        output,
        /** New in FMI-2.0rc1. */
        parameter,
        /** The value does not affect computation.  Typically, "none"
         *  values are tool specific and used to enable logging.
         */
        none
    }

    /** Acceptable values for the variability xml attribute.
     *  The variablity attribute defines when a value changes,
     *  which determines when the value should be read.
     */
    public enum Variability {
        /** The value does not change.
         */
        constant,
        /** The value changes at any moment.  A continuous
         *  variable must be of type Real.
         *  The default Variability is "continuous".
         */
        continuous,
        /** The value only changes during initialization
         *  and at event instances.
         */
        discrete,
        /** The value does not change after initialization.
         */
        parameter,
        /** FMI-2.0. */
        fixed,
        /** The value is constant between external events.
         */
        tunable
    }

    ///////////////////////////////////////////////////////////////////
    ////             public fields                                 ////

    /** The value of the alias xml attribute. */
    public Alias alias;

    /** The value of the causality xml attribute. */
    public Causality causality;

    /** The value of the description xml attribute. */
    public String description;

    /** The input ports on which an output has a direct dependence. */
    public Set<String> directDependency;

    /** The Model Description for this variable. */
    public FMIModelDescription fmiModelDescription;

    /** The value of the name xml attribute. */
    public String name;

    /** The value of the type xml attribute. */
    public FMIType type;

    /** The value of the valueReference xml attribute.
     *  In FMI 1.0, a valueReference is typically 32-bits
     *  or an unsigned int.  Java does not have an unsigned
     *  int, so we use a long.
     */
    public long valueReference;

    /** The value of the variability xml attribute. */
    public Variability variability;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get or set the value of this variable.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param valueBuffer The buffer that contains the value to be gotten or set.
     *  For booleans, doubles and integers, this is a Buffer, for
     *  String it is a PointerByReference
     *  @param typeClass The expected class of the type.
     *  @param getOrSetFunction the fmiGet or fmiSet function.
     */
    private void _getOrSetValue(Pointer fmiComponent, Object valueBuffer,
            Class typeClass, Function getOrSetFunction) {
        // This is syntactic sugar that helps us avoid duplicated code.
        if (!typeClass.isInstance(type)) {
            throw new RuntimeException("Variable " + name + " is not a "
                    + typeClass.getName() + ", it is a "
                    + type.getClass().getName());
        }
        IntBuffer valueReferenceIntBuffer = IntBuffer.allocate(1).put(0,
                (int) valueReference);
        int fmiFlag = ((Integer) getOrSetFunction.invokeInt(new Object[] {
                fmiComponent, valueReferenceIntBuffer, new NativeSizeT(1),
                valueBuffer })).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException("Could not get or set \"" + name
                    + "\" as a " + typeClass.getName() + ", it was of type \""
                    + _typeName + "\": "
                    + FMULogUtilities.fmiStatusToString(fmiFlag));
        }
    }

    /** If the fmi version is not 2.0 or later, then throw
     *  an exception stating that the value is not acceptable.
     *  @param fmiModelDescription The FMIModelDescription that
     *  has the fmi version.
     *  @param attribute The name of the attribute that is not present in FMI-1.0.
     *  @param message The string to be displayed that describes possible
     *  values.  It should start with "must be".
     *  @exception IllegalArgumentException If the fmi version is
     *  is not 2.0 or later.
     */
    private void _fmi2AttributeCheck(FMIModelDescription fmiModelDescription,
            String attribute, String message) throws IllegalArgumentException {
        if (fmiModelDescription.fmiVersion.compareTo("2.0") < 0) {
            throw new IllegalArgumentException("The attribute \"" + attribute
                    + "\" is only accepted in FMI-2.0 and later.  "
                    + "The fmiVersion was \"" + fmiModelDescription + "\"."
                    + "It " + message);
        }
    }

    /** Get the value of this variable.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param valueBuffer The buffer that contains the value to be gotten.
     *  For booleans, doubles and integers, this is a Buffer, for
     *  String it is a PointerByReference
     *  @param typeClass The expected class of the type.
     */
    private void _getValue(Pointer fmiComponent, Object valueBuffer,
            Class typeClass) {
        if (_fmiGetFunction == null) {
            if (_typeName.equals("skip")) {
                System.out
                .println("Could not process type, it was marked as skip.");
                return;
            }
            try {
                _fmiGetFunction = fmiModelDescription.getFmiFunction("fmiGet"
                        + _typeName);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Failed to find the native library.", ex);
            }
        }
        _getOrSetValue(fmiComponent, valueBuffer, typeClass, _fmiGetFunction);
    }

    /** Set the value of this variable.
     *  @param fmiComponent The Functional Mock-up Interface (FMI)
     *  component that contains a reference to the variable.
     *  @param valueBuffer The buffer that contains the value to be set.
     *  For booleans, doubles and integers, this is a Buffer, for
     *  String it is a PointerByReference
     *  @param typeClass The expected class of the type.
     */
    private void _setValue(Pointer fmiComponent, Object valueBuffer,
            Class typeClass) {
        if (_fmiSetFunction == null) {
            if (_typeName.equals("skip")) {
                System.out
                .println("Could not process type, it was marked as skip.");
                return;
            }
            try {
                _fmiSetFunction = fmiModelDescription.getFmiFunction("fmiSet"
                        + _typeName);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Failed to find the native library.", ex);
            }
        }
        _getOrSetValue(fmiComponent, valueBuffer, typeClass, _fmiSetFunction);
    }

    ///////////////////////////////////////////////////////////////////
    ////             private fields                                ////

    /** The set of elements that we don't yet handle.
     *  This is used for error messages.
     */
    private static Set<String> _errorElements = new HashSet<String>();

    /** The FMI .c function that gets the value of this variable.
     *  The name of the function depends on the value of the
     *  fmiModelDescription.modelIdentifer field and the
     *  type name.  A typical value for the Bouncing Ball
     *  example might be "bouncingBall_fmiGetDouble".
     */
    private Function _fmiGetFunction;

    /** The FMI .c function that sets the value of this variable.
     *  The name of the function depends on the value of the
     *  fmiModelDescription.modelIdentifer field and the
     *  type name.  A typical value for the Bouncing Ball
     *  example might be "bouncingBall_fmiSetDouble".
     */
    private Function _fmiSetFunction;

    /** The name of the type of this variable. */
    private String _typeName;
}
