/** Represent an native function argument

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Argument
/**
   An Argument is a native function argument associated to a GenericJNIActor
   @author V.Arnould, Thales
   @version $Id$
   @since Ptolemy II 2.2
   @see jni.GenericJNIActor
*/
public class Argument extends Attribute implements Settable {

    /** Creates a new instance of Argument with the given name
     * for the given GenericJNIActor
     */
    public Argument(GenericJNIActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Default constructor */
    public Argument() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add.
     */
    public void addValueListener(
            ptolemy.kernel.util.ValueListener listener) {
    }

    /** Export the Argument in a property MoML. If this object is not
     *  persistent, then write nothing.
     *  @exception IOException If an IO error occurs
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_suppressMoML(depth)) {
            return;
        }
        String value = getExpression();
        output.write(
                _getIndentPrefix(depth)
                + "<"
                + _elementName
                + " name=\""
                + name.trim()
                + "\" class=\""
                + getMoMLInfo().className.trim()
                + "\" value=\""
                + value.trim()
                + "\" >\n");
        _exportMoMLContents(output, depth + 1);
        output.write(
                _getIndentPrefix(depth) + "</"
                + _elementName + ">\n");
    }


    /** Get the C type of the argument as a pointer if it is an array.
     *  @return the _cType attribute in pointer
     */
    public String getC2Type() {
        String ret = _cType;
        if (_cType.endsWith("[]")) {
            if (StringUtilities.getProperty("os.name")
                    .startsWith("SunOS")) {
            }
            ret = _cType.substring(0, _cType.length()-2) + " *";
        }
        return ret;
    }

    /** Get the C2 Type Array, but if we are under SunOS, and
     *  getC2Type returns long, then return int *.  On other platforms,
     *  just return the value of getC2Type().  FIXME: This platform
     *  dependent change is necessary under Solaris 8 for some reason.
     *  @return the _cType attribute in pointer
     */
    public String getC2TypeHack() {
        String returnValue = getC2Type();
        if (StringUtilities.getProperty("os.name")
                .startsWith("SunOS")) {
            if (returnValue.startsWith("long")) {
                return "int *";
            }
        }
        return returnValue;
    }

    /** Get the C type of the argument.
     *   @return the _cType attribute
     */
    public String getCType() {
        return _cType;
    }

    /** Get the container entity.
     *  @return The container, which is an instance
     *   of CompositeEntity.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Get the expression of the argument.
     * The format is "_isInput, _isOutput, _isReturn, _cType".
     * @return the string containing the argument specifications
     */
    public String getExpression() {
        String ret =
            new Boolean(isInput()).toString()
                + ","
            + new Boolean(isOutput()).toString()
                + ","
            + new Boolean(isReturn()).toString()
                + ","
            + getCType();
        return ret;
    }

    /** Get the JNI type of the argument.
     *  @return the corresponding JNI type.
     */
    public String getJNIType() {
        String returnJNIType = "";
        if (_cType.endsWith("[]")) {
            returnJNIType = "Array";
        }
        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnJNIType = "jboolean" + returnJNIType;
        } else if (_cType.equals("short")
                || _cType.startsWith("short")) {
            // a C char is 8 bits. a java char is 16 bits,
            // so we use jbyte
            returnJNIType = "jchar" + returnJNIType;
        } else if (_cType.equals("long")
                || _cType.startsWith("long")) {
            // a C long is 32 bits. a java long is 64 bits,
            // so we use jint
            returnJNIType = "jint" + returnJNIType;
        } else if (_cType.equals("double")
                || _cType.startsWith("double")) {
            returnJNIType = "jdouble" + returnJNIType;
        } else if (_cType.equals("void")
                || _cType.startsWith("void")) {
            returnJNIType = "void" + returnJNIType;
        } else {
            MessageHandler.error(
                    "JNIType unavailable for '"
                    + _cType + "': not convertible JNI type");
            returnJNIType = "void";
        }
        return returnJNIType;
    }

    /** Get the Java type of the argument
     *  @return the corresponding Java type
     */
    public String getJType() {
        String returnJType = "";
        //if it's an array
        if (_cType.endsWith("[]")) {
            returnJType = "[]";
        }
        // a C char is 8 bits unsigned. a java char is 16 bits,
        // so we use boolean which is 8 bits unsigned
        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnJType = "boolean" + returnJType;
        } else if (_cType.equals("short") ||
                _cType.startsWith("short")) {
            // a C short is 16 bits unsigned, a java short is 16 bits
            // but not unsigned,
            // so we use char which is 16bits unsigned.
            returnJType = "char" + returnJType;
        } else if (_cType.equals("long") ||
                _cType.startsWith("long")) {
            // a C long is 32 bits unsigned. a java long is 64 bits,
            // so we use int which is 32 , but not unsigned !! TBF
            returnJType = "int" + returnJType;
        } else if (_cType.equals("double") ||
                _cType.startsWith("double")) {
            // double is 64 bits in C and in Java, so no problem
            returnJType = "double" + returnJType;
        } else if (_cType.equals("void") ||
                _cType.startsWith("void")) {
            // for the functions with a void return
            returnJType = "void";
        } else {
            // FIXME: I guess we are printing a warning using
            // the MessageHandler and then returning void here?
            try {
                MessageHandler.warning("Type = "
                        + _cType + " not convertible in JavaType");
            } catch (Exception e) {}
            returnJType = "void";
        }
        return returnJType;
    }

    /** Get the kind as a comma separated list
     *  @return "input", "output" "input, output" or "return"
     */
    public String getKind() {
        String returnValue = "";
        //set Kind
        if (isInput()) {
            returnValue = "input";
        }
        if (isOutput() && !isInput()) {
            returnValue = "output";
        }
        if (isOutput() && isInput()) {
            returnValue = "input,output";
        }
        if (isReturn()) {
            returnValue = "return";
        }
        return returnValue;
    }

    /** Get the Java class corresponding to the Java Type
     *  @return the corresponding Java class
     */
    public String getType() {

        String returnCType = "";
        if (_cType.endsWith("[]")) {
            returnCType = "[]";
        }
        if (_cType.equals("char") || _cType.startsWith("char")) {
            returnCType = "Boolean" + returnCType;
        } else if (_cType.equals("short")
                || _cType.startsWith("short")) {
            returnCType = "Byte" + returnCType;
        } else if (_cType.equals("long")
                || _cType.startsWith("long")) {
            returnCType = "Integer"+ returnCType;
        } else if (_cType.equals("double")
                || _cType.startsWith("double")) {
            returnCType = "Double" + returnCType;
        } else if (_cType.equals("void")
                || _cType.startsWith("void")) {
            returnCType = "Object" + returnCType;
        } else {
            // FIXME: why is this code not like the code above
            MessageHandler.error("Type = "
                    + _cType + " not convertible in JavaClass");
            returnCType = "Object";
        }
        return returnCType;
    }

    /** This is for derivates of Attribute
     *  @return the Visibility
     */
    public Visibility getVisibility() {
        return (Visibility) null;
    }

    /** Return true if it is an input.
     *  @return true is it is an input, or false if not.
     */
    public boolean isInput() {
        return _isInput;
    }

    /** Return true if it an output.
     *  @return true is it is an output, or false if not.
     */
    public boolean isOutput() {
        return _isOutput;
    }

    /** Return true if it is a return
     *   @return true is it is an return, or false if not.
     */
    public boolean isReturn() {
        return _isReturn;
    }

    /** Remove a listener to be notified when the value of
     *  this settable object changes.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ptolemy.kernel.util.ValueListener
            listener) {
    }

    /** Set the C type of the argument with the given string
     */
    public void setCType(String cType) {
        _cType = cType.trim();
    }


    /** Specify the container, adding the entity to the list
     *  of entities in the container.
     *  If the container already contains
     *  an entity with the same name,
     *  then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.
     *  If the entity is already contained by the container,
     *  do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory,
     *  so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action
     *   would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(GenericJNIActor container)
            throws IllegalActionException, NameDuplicationException {

        if (container != null && _workspace != container.workspace()) {
            throw new IllegalActionException(
                    this,
                    container,
                    "Cannot set container "
                    + "because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            _checkContainer(container);
            // NOTE: The following code is quite tricky.
            // It is very careful
            // to leave a consistent state even
            // in the face of unexpected
            // exceptions.  Be very careful if modifying it.
            GenericJNIActor previousContainer =
                (GenericJNIActor) getContainer();
            if (previousContainer == container)
                return;

            // Do this first, because it may throw an exception,
            // and we have
            // not yet changed any state.
            if (container != null) {
                container._addArgument(this);
                if (previousContainer == null) {
                    _workspace.remove(this);
                }
            }
            _container = (GenericJNIActor) container;
            if (previousContainer != null) {
                // This is safe now because it does not
                // throw an exception.
                previousContainer._removeArgument(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the expression of the argument.
     * The format wanted is "_isInput,_isOutput,_isReturn,_cType".
     */
    public void setExpression(String expression) {
        _value = expression;
        StringTokenizer tokenizer = new StringTokenizer(
                _value, ",");
        try {
            setInput(new Boolean(tokenizer.nextToken()
                    .toString()).booleanValue());
            setOutput(new Boolean(tokenizer.nextToken()
                    .toString()).booleanValue());
            setReturn(new Boolean(tokenizer.nextToken()
                    .toString()).booleanValue());
            setCType(tokenizer.nextToken().toString());
        } catch(java.util.NoSuchElementException e) {}
        try {
            validate();
        } catch (IllegalActionException e) {
            MessageHandler.error(
                    "TRT error! Bad expression for Argument "
                    + getName(), e);
        }
        // Make sure the new value is exported in MoML.  EAL 12/03.
        setPersistent(true);
    }

    /** Set the expression of the argument from its attributes
     */
    public void setExpression() {
        String ret =
            new Boolean(isInput()).toString()
                + ","
            + new Boolean(isOutput()).toString()
                + ","
            + new Boolean(isReturn()).toString()
                + ","
            + getCType();
        setExpression(ret);
    }

    /** Set to true if the attribute is an input.
     *  @param input True if this is an input, false if it is not.
     */
    public void setInput(boolean input) {
        _isInput = input;
    }

    /** Set the kind of the argument with the given string
     *  @param selectedValues A string describing the type of Argument.
     *  valid values are "input", "output", "return", "input, output".
     */
    public void setKind(String selectedValues) {
        if (selectedValues.equals("input")) {
            this.setInput(true);
        } else {
            this.setInput(false);
        }
        if (selectedValues.equals("output")) {
            this.setOutput(true);
        } else {
            this.setOutput(false);
        }
        if (selectedValues.equals("return")) {
            this.setReturn(true);
        } else {
            this.setReturn(false);
        }
        if (selectedValues.equals("input, output")) {
            this.setInput(true);
            this.setOutput(true);
        }
        // FIXME: this should throw an exception not call MessageHandler
        // directly.
        if (selectedValues.equals("input, return")) {
            MessageHandler.error(
                    "An argument can't be input "
                    + "and return at the same time.");
        }
        if (selectedValues.equals("output, return")) {
            MessageHandler.error(
                    "An argument can't be output "
                    + "and return or in at the same time.");
        }
        if (selectedValues.equals("input, output, return")) {
            MessageHandler.error(
                    "An argument can't be in-out "
                    + "and return at the same time.");
        }
    }

    /** Set to true if the attribute is an output.
     *        @param output True if this is an output, false if it is not.
     */
    public void setOutput(boolean output) {
        _isOutput = output;
    }

    /** Set to true if the attribute is a return
     *        @param returnFlag True if this is an input, false if it is not.
     */
    public void setReturn(boolean returnFlag) {
        _isReturn = returnFlag;
    }

    /** This is for derivates of Attribute set the Visibility
     */
    public void setVisibility(ptolemy.kernel.util.
            Settable.Visibility v) {
    }

    /** Notify the container that an attribute has changed
        @return void
        @exception IllegalActionException If a error occurs
    */
    public void validate() throws IllegalActionException {
        NamedObj container = (NamedObj) getContainer();
        if (container != null)
            container.attributeChanged(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this entity.  In this base class, this method returns immediately
     *  without doing anything.
     *  Derived classes may override it to constrain
     *  the container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(GenericJNIActor container)
            throws IllegalActionException {
        if (container.getClass().getName() != "jni.GenericJNIActor") {
            throw new IllegalActionException(
                    this,
                    container,
                    "Cannot place arguments on entities "
                    + container.getClass().getName()
                    + ", which are not GenericJNIActor.");
        }
    }

    /** Check that the specified type is a suitable type for
     *  this entity.
     *  @exception IllegalActionException If the Argument has not
     *   an acceptable C type.  Not thrown in this base class.
     */
    protected void _checkType() {
        if (_cType.startsWith("char")
                || _cType.startsWith("long")
                || _cType.startsWith("short")
                || _cType.startsWith("double")) {
            if (isOutput()&&!isInput()&&!_cType.endsWith("[]")) {
                MessageHandler.error(
                        "An argument can't be "
                        + "output with a simple type.");
                setInput(true);
            }
            return;
        } else {
            MessageHandler.error(
                    "The type : "
                    + _cType
                    + " is not supported. Types supported:"
                    + "\nchar, long (unsigned)"
                    + " , short, double"
                    + "\nThe JNI code generation"
                    + " will not work");
            setCType("");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** A boolean that specified if the argument is an Input
     */
    private boolean _isInput;

    /** A boolean that specified if the argument is an Output
     */
    private boolean _isOutput;

    /** A boolean that specified if the argument is a return
     */
    private boolean _isReturn;

    /** A String that specified the argument type, in C language.
     */
    private String _cType;

    /** A String that is the argument value, ie its expression
     */
    private String _value;

    /** @serial The entity that contains this entity.
     */
    private GenericJNIActor _container;

}
