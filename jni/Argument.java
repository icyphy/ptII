/** Argument.java
 * Represent an native function argument
 *
@ProposedRating Yellow (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
 */
package jni;

import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.KernelException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// Argument
/**
An Argument is a native function argument associated to a GenericJNIActor
@author V.Arnould
@version $Id$
@since Ptolemy II 2.1
@see jni.GenericJNIActor
@author  arnould
 */
public class Argument extends Attribute implements Settable {

        /** Creates a new instance of Argument with the given name
         * for the given GenericJNIActor
         */
        public Argument(GenericJNIActor container, String name)
                throws IllegalActionException, NameDuplicationException {
                super(container, name);
                getMoMLInfo().elementName = "property";
                setContainer(container);
        }

        /** Default constructor */
        public Argument() {
                super();
        }

        /** This is for derivates of Attribute
        @return the Visibility
        */
        public Visibility getVisibility() {
                return (Visibility) null;
        }

        /** This is for derivates of Attribute
        * set the Visibility
        @return void
        */
        public void setVisibility(ptolemy.kernel.util.
        Settable.Visibility v) {
        }

        /** Get the _isIn attribute
        @return true is it is an input, or false if not
        */
        public boolean isInput() {
                return _isIn;
        }

        /** Get the _isOut attribute
        @return true is it is an output, or false if not
        */
        public boolean isOutput() {
                return _isOut;
        }

        /** Get the _isReturn attribute
        @return true is it is an return, or false if not
        */
        public boolean isReturn() {
                return _isReturn;
        }

        /** Set the _isIn attribute with the given boolean
        @return void
        */
        public void setInput(boolean bo) {
                _isIn = bo;
        }

        /** Set the _isOut attribute with the given boolean
        @return void
        */
        public void setOutput(boolean bo) {
                _isOut = bo;
        }

        /** Set the _isReturn attribute with the given boolean
        @return void
        */
        public void setReturn(boolean bo) {
                _isReturn = bo;
        }

        /** Set the kind of the argument with the given string
        @return void
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
                if (selectedValues.equals("input, return"))
                        MessageHandler.error(
                                "An argument can't be input "
                                + "and return at the same time.");
                if (selectedValues.equals("output, return"))
                        MessageHandler.error(
                                "An argument can't be output "
                                + "and return or in at the same time.");
                if (selectedValues.equals("input, output, return"))
                        MessageHandler.error(
                                "An argument can't be in-out "
                                + "and return at the same time.");
        }

        /** Get the kind as a comma separated list
        @return "input", "output" "input, output" or "return"
        */
        public String getKind() {
                String ret = "";
                //set Kind
                if (isInput()) ret = "input";
                if (isOutput()&&!isInput()) ret = "output";
                if (isOutput()&&isInput()) ret = ",output";
                if (isReturn()) ret = "return";
                return ret;
        }

        /** Set the C type of the argument with the given string
        @return void
        */
        public void setCType(String cType) {
                _cType = cType.trim();
        }

        /** Get the C type of the argument
        @return the _cType attribute
        */
        public String getCType() {
                return _cType;
        }

        /** Get the C type of the argument
         * as a pointer if it is an array
        @return the _cType attribute in pointer
        */
        public String getC2Type() {
                String ret = _cType;
                if(_cType.endsWith("[]"))
                ret = _cType.substring(0, _cType.length()-2) + " *";
                return ret;
        }

        /** Get the Java type of the argument
        @return the corresponding Java type
        */
        public String getJType() {
                String retJType = "";
                //if it's an array
                if (_cType.endsWith("[]"))
                        {
                                retJType = "[]";
                        }
                // a C char is 8 bits unsigned. a java char is 16 bits,
                // so we use boolean which is 8 bits unsigned
                if (_cType.equals("char") || _cType.startsWith("char"))
                        retJType = "boolean" + retJType;
                // a C short is 16 bits unsigned, a java short is 16 bits
                // but not unsigned,
                // so we use char which is 16bits unsigned.
                else if (_cType.equals("short") ||
                _cType.startsWith("short"))
                        retJType = "char" + retJType;
                // a C long is 32 bits unsigned. a java long is 64 bits,
                // so we use int which is 32 , but not unsigned !! TBF
                else if (_cType.equals("long") ||
                _cType.startsWith("long"))
                        retJType = "int" + retJType;
                // double is 64 bits in C and in Java, so no problem
                else if (_cType.equals("double") ||
                _cType.startsWith("double"))
                        retJType = "double" + retJType;
                // for the functions with a void return
                else if (_cType.equals("void") ||
                _cType.startsWith("void"))
                        retJType = "void";
                else {
                        try {
                                MessageHandler.warning("Type = "
                                + _cType + " not convertible in JavaType");
                        } catch (Exception e) {}
                        retJType = "void";
                }
                return retJType;
        }

        /** Get the Java class corresponding to the Java Type
        @return the corresponding Java class
        */
        public String getType() {

                String retJType = "";
                if (_cType.endsWith("[]"))
                        {
                                retJType = "[]";
                        }
                if (_cType.equals("char") || _cType.startsWith("char"))
                        retJType = "Boolean" + retJType;
                else if (_cType.equals("short")
                || _cType.startsWith("short"))
                        retJType = "Byte" + retJType;
                else if (_cType.equals("long")
                || _cType.startsWith("long"))
                        retJType = "Integer"+ retJType;
                else if (_cType.equals("double")
                || _cType.startsWith("double"))
                        retJType = "Double" + retJType;
                else if (_cType.equals("void")
                || _cType.startsWith("void"))
                        retJType = "Object" + retJType;
                else {
                        MessageHandler.error("Type = "
                        + _cType + " not convertible in JavaClass");
                        retJType = "Object";
                }
                return retJType;
        }

        /** Get the JNI type of the argument
        @return the corresponding JNI type
        */
        public String getJNIType() {

                String retJNIType = "";
                if (_cType.endsWith("[]"))
                        {
                                retJNIType = "Array";
                        }
                if (_cType.equals("char") || _cType.startsWith("char"))
                        retJNIType = "jboolean" + retJNIType;
                // a C char is 8 bits. a java char is 16 bits,
                // so we use jbyte
                else if (_cType.equals("short")
                 || _cType.startsWith("short"))
                        retJNIType = "jchar" + retJNIType;
                // a C long is 32 bits. a java long is 64 bits,
                // so we use jint
                else if (_cType.equals("long")
                 || _cType.startsWith("long"))
                        retJNIType = "jint" + retJNIType;
                else if (_cType.equals("double")
                 || _cType.startsWith("double"))
                        retJNIType = "jdouble" + retJNIType;
                else if (_cType.equals("void")
                 || _cType.startsWith("void"))
                        retJNIType = "void" + retJNIType;
                else {
                        MessageHandler.error(
                                "JNIType unavailble for "
                                + _cType + "  : not convertible JNI type");
                        retJNIType = "void";
                }
                return retJNIType;
        }

        /** Get the expression of the argument.
         * The format is "_isIn, _isOut, _isReturn, _cType".
         @return the string containing the argument specifications
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

        /** Set the expression of the argument.
         * The format waited is "_isIn,_isOut,_isReturn,_cType".
          @return void
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
        }

        /** Set the expression of the argument from its attributes
        @return void
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

        /** Notify the container that an attribute has changed
        @return void
        @exception IllegalActionException If a error occurs
        */
        public void validate() throws IllegalActionException {
                NamedObj container = (NamedObj) getContainer();
                if (container != null)
                        container.attributeChanged(this);
        }

        /** Export the Argument in a property MoML
        @return void
        @exception IOException If an IO error occurs
        */
        public void exportMoML(Writer output, int depth, String name)
                throws IOException {
                String value = getExpression();
                output.write(
                        _getIndentPrefix(depth)
                                + "<"
                                + getMoMLInfo().elementName
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
                        + getMoMLInfo().elementName + ">\n");
        }


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

        /** Remove a listener to be notified when the value of
         *  this settable object changes.
         *  @param listener The listener to remove.
         */        public void removeValueListener(
                ptolemy.kernel.util.ValueListener listener) {
        }


        /** Get the container entity.
         *  @return The container, which is an instance
         *   of CompositeEntity.
         */
        public Nameable getContainer() {
                return _container;
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
                if (container.getClass().getName() != "jni.GenericJNIActor")
                        throw new IllegalActionException(
                                this,
                                container,
                                "Cannot place arguments on entities "
                                        + container.getClass().getName()
                                        + ", which are not GenericJNIActor.");
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
                        || _cType.startsWith("double"))
                        {
                         if(isOutput()&&!isInput()&&!_cType.endsWith("[]"))
                          {
                                MessageHandler.error(
                                "An argument can't be "
                                + "output with a simple type.");
                                setInput(true);
                           }
                         return;
                        }
                else {
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

        /** A boolean that specifie if the argument is an Input
         */
        private boolean _isIn;

        /** A boolean that specifie if the argument is an Output
         */
        private boolean _isOut;

        /** A boolean that specifie if the argument is an Return
         */
        private boolean _isReturn;

        /** A String that specifie the argument type, in C language.
         */
        private String _cType;

        /** A String that is the argument value, ie its expression
         */
        private String _value;

        /** @serial The entity that contains this entity.
         */
        private GenericJNIActor _container;

}
