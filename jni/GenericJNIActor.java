/** An actor able to call a C function

 Copyright (c) 2003-2007 The Regents of the University of California.
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
package jni;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jni.gui.JNIActorEditorFactory;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
///// JNIActor

/**
 Use the Java Native Interface to execute a native method.
 To use this actor, first configure the arguments of the native method
 by right clicking on the actor and selecting 'Configure Arguments', then
 configure the library name and location, then create the JNI
 files.

 <p>Note that under Windows, your path needs to include the directory
 named by the libraryDirectory Parameter.

 For further details, see {@link JNIUtilities}.


 @author Vincent Arnould (vincent.arnould@thalesgroup.com), Contributor: Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (vincent.arnould)
 @Pt.AcceptedRating Red (vincent.arnould)
 @see JNIUtilities
 */
public class GenericJNIActor extends TypedAtomicActor {
    /** Construct an entity in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public GenericJNIActor() {
        super();
        _argumentsList = new NamedList(this);
    }

    /** Construct an entity in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     */
    public GenericJNIActor(Workspace workspace) {
        super(workspace);
        _argumentsList = new NamedList(this);
    }

    /** Construct an entity in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this object.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.

     */
    public GenericJNIActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _argumentsList = new NamedList(this);

        // FIXME: Should libraryDirectory be a FileParameter?
        libraryDirectory = new Parameter(this, "libraryDirectory",
                new StringToken("jni/lib"));
        nativeFunction = new Parameter(this, "nativeFunction", new StringToken(
                "unknownFunction"));
        nativeLibrary = new Parameter(this, "nativeLibrary", new StringToken(
                "unknownLibrary"));

        // Create our own custom editor
        new JNIActorEditorFactory(this, "_editorFactory");

        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"0\" y=\"0\" "
                + "width=\"32\" height=\"38\" " + "style=\"fill:white\"/>\n"
                + "<image x=\"1\" y=\"1\" width=\"32\" height=\"38\""
                + "xlink:href=\"jni/dll.gif\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The directory that contains the native library, which under
     *  Windows should include a dll, an h file and a lib file.
     *  The default value of thie parameter is the string
     *  "jni" + File.separator + "lib"
     */
    public Parameter libraryDirectory;

    /** The name of the native library.  The default value of this
     *  parameter is the String "nativeFunction"
     */
    public Parameter nativeFunction;

    /** The name of the native library.  The default value of this
     *  parameter is the String "nativeLibrary"
     */
    public Parameter nativeLibrary;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a return argument to this entity.
     *  @exception IllegalActionException If the argument with the
     *  name "return" is not of an acceptable class for the container.
     *  @exception NameDuplicationException If there is already an
     *  argument with the name "return"
     */
    public void addArgumentReturn() throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getReadAccess();

            Argument ret = new Argument(this, "return");
            ret.setReturn(true);
            ret.setCType("void");
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the arguments belonging to this entity.
     *  The order is the order in which they became contained by this entity.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of Port objects.
     */
    public List argumentsList() {
        try {
            _workspace.getReadAccess();
            return _argumentsList.elementList();
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the getDirector() returns something other than null, then
     *  invalidate the resolved types.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        Director director = getDirector();

        if (director != null) {
            // FIXME: should this happen every time we call attribute changed?
            director.invalidateResolvedTypes();
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new entity with clones of the ports of the original
     *  entity.  The ports are set to the ports of the new entity.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return The new Entity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GenericJNIActor newEntity = (GenericJNIActor) super.clone(workspace);
        newEntity._argumentsList = new NamedList(newEntity);

        // Clone the ports.
        Iterator arguments = argumentsList().iterator();

        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            Argument newArgument = (Argument) argument.clone(workspace);

            // Assume that since we are dealing with clones,
            // exceptions won't occur normally (the original was successfully
            // incorporated, so this one should be too).  If they do, throw an
            // InvalidStateException.
            try {
                newArgument.setContainer(newEntity);
            } catch (KernelException ex) {
                workspace.remove(newEntity);
                throw new InvalidStateException(this,
                        "Failed to clone an Entity: " + ex.getMessage());
            }
        }

        Class myClass = getClass();
        Field[] fields = myClass.getFields();

        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].get(newEntity) instanceof Argument) {
                    fields[i].set(newEntity, newEntity.getArgument(fields[i]
                            .getName()));
                }
            } catch (IllegalAccessException e) {
                throw new CloneNotSupportedException(e.getMessage() + ": "
                        + fields[i].getName());
            }
        }

        return newEntity;
    }

    /** For each Argument, a port of the same name is created,
     *  belonging to this argument.
     *  @exception IllegalActionException If there is a problem
     *  creating the ports.
     */
    public void createPorts() throws IllegalActionException {
        Iterator arguments = this.argumentsList().iterator();
        TypedIOPort port;

        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            port = (TypedIOPort) this.getPort(argument.getName());

            if (port == null) {
                if (argument.isReturn()) {
                    try {
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"output\"/>\n"
                                        + "</port>");
                        request.setUndoable(true);
                        requestChange(request);

                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct port "
                                        + "port for argument \""
                                        + argument.getName() + "\"");
                    }
                } else if (argument.isInput() && argument.isOutput()) {
                    try {
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,
                                this,
                                "<group>\n"
                                        + " <port name=\""
                                        + argument.getName()
                                        + "in"
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"input\"/>\n"
                                        + " </port>\n"
                                        + " <port name=\""
                                        + argument.getName()
                                        + "out"
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"output\"/>\n"
                                        + " </port>\n" + "</group>");
                        request.setUndoable(true);
                        requestChange(request);
                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct " + "input or output "
                                        + "port for argument \""
                                        + argument.getName() + "\"");
                    }
                } else {
                    try {
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + (argument.isInput() ? "    <property name=\"input\"/>\n"
                                                : "")
                                        + (argument.isOutput() ? "    <property name=\"output\"/>\n"
                                                : "") + "</port>");
                        request.setUndoable(true);
                        requestChange(request);
                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct " + "port for argument \""
                                        + argument.getName() + "\"");
                    }
                }
            } else {

                // synchronized the arguments and the ports
                if (argument.isReturn()) {
                    MoMLChangeRequest request = new MoMLChangeRequest(
                            this,
                            this,
                            "<port name=\""
                                    + argument.getName()
                                    + "\" class=\"ptolemy.actor.TypedIOPort\"\n"
                                    + "    <property name=\"output\"/>\n"
                                    + "</port>");
                    request.setUndoable(true);
                    requestChange(request);
                } else {
                    MoMLChangeRequest request = new MoMLChangeRequest(
                            this,
                            this,
                            "<port name=\""
                                    + argument.getName()
                                    + "\" class=\"ptolemy.actor.TypedIOPort\"\n"
                                    + "    <property name=\"input\"/>\n"
                                    + "</port>");
                    request.setUndoable(true);
                    requestChange(request);

                }
            }
            port = (TypedIOPort) this.getPort(argument.getName());
            if (port != null) {
                port.setTypeEquals(BaseType.GENERAL);
            }
        }
    }

    /** Read the argument of the function from the ports,
     *  call the native method throw the generated interface,
     *  and put the results on the corresponding ports.
     *  @exception IllegalActionException If a exception occured
     */
    public void fire() throws IllegalActionException {
        super.fire();
        //getting the in/inout parameters
        Iterator ports = this.portList().iterator();
        Vector args = new Vector();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            if (port.isInput() && port.hasToken(0)
                    && !(port.isOutput() && !port.isInput())) {
                Token tok = port.get(0);

                String typ = _methods[_methodIndex].getParameterTypes()[args
                        .size()].toString();

                if (typ.equals("boolean")) {
                    args.add(Boolean.valueOf(((BooleanToken) tok)
                            .booleanValue()));
                } else if (typ.equals("int")) {
                    args.add(Integer.valueOf(((IntToken) tok).intValue()));
                } else if (typ.equals("double")) {
                    args.add(Double.valueOf(((DoubleToken) tok).doubleValue()));
                } else if (typ.equals("class [I")) {
                    int siz = ((ArrayToken) tok).arrayValue().length;
                    int[] tab = new int[siz];

                    for (int j = 0; j < siz; j++) {
                        tab[j] = ((IntToken) (((ArrayToken) tok).arrayValue()[j]))
                                .intValue();
                    }

                    //(int[])((ArrayToken)tok).arrayValue();
                    args.add(tab);
                } else {
                    System.out.println("The intype is not convertible "
                            + "with Ptolemy II types.");
                }
            }
        }

        //tBFixed : the out parameter is not in by definition
        //...so no port in can initialize the param
        //call the native function
        Object obj = null;
        Object ret = null;

        try {
            try {
                obj = _class.newInstance();
            } catch (Error error) {
                // Using JNI to link in a native library
                // can result in a java.lang.UnsatisfiedLinkError
                // which extends Error, not Exception.
                // FIXME: Rethrow the error as an exception
                String libraryPath = StringUtilities
                        .getProperty("java.library.path");

                throw new Exception("Class '" + _class
                        + "' cannot be instantiated.\n"
                        + "If you are running under Windows, "
                        + "be sure that the directory containing the library "
                        + "is in your PATH.\n"
                        + "If you are running under Solaris, "
                        + "be sure that the directory containing the library "
                        + "is in your LD_LIBRARY_PATH and that the library "
                        + "name begin with 'lib' and end with '.so'.\n"
                        + "You may need to exit, set your "
                        + "PATH or LD_LIBRARY_PATH to include the directory "
                        + "that contains the shared library and "
                        + "restart.\n" + "For example, under Windows "
                        + "in a Cygwin bash shell:\n"
                        + "PATH=/cygdrive/c/ptII/jni/dll:${PATH}\n"
                        + "export PATH\n" + "vergil -jni foo.xml\n"
                        + "A common error is that "
                        + "the class cannot be found in "
                        + "property 'java.library.path' " + "which is:\n"
                        + libraryPath + "\nError message was: "
                        + error.getMessage(), error);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Class cannot be instantiated");
        }

        try {
            ret = _methods[_methodIndex].invoke(obj, args.toArray());
        } catch (Throwable ex) {
            StringBuffer argumentsDescription = new StringBuffer("");

            try {
                if (args.size() >= 1) {
                    argumentsDescription.append(args.elementAt(0).toString());

                    for (int i = 1; i < args.size(); i++) {
                        argumentsDescription.append(", "
                                + args.elementAt(i).toString());
                    }
                }
            } catch (Throwable throwable) {
                // Ignore
            }

            throw new IllegalActionException(this, ex,
                    "Native operation call failed." + "Failed to invoke '"
                            + obj + "' with " + args.size() + " arg(s) "
                            + argumentsDescription.toString());
        }

        ports = portList().iterator();

        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();

            //if the argument is return
            if (getArgumentReturn() == null) {
                System.err.println("Warning: GenericJNIActor.java: "
                        + "getArgumentReturn() returns null?");
            }

            if ((port != null)
                    && (port.getName() != null)
                    && (getArgumentReturn() != null)
                    && port.getName()
                            .equals(this.getArgumentReturn().getName())) {
                String typ = "";
                Field field = null;

                try {
                    field = _class.getDeclaredField("_" + port.getName());
                    typ = field.getType().toString();
                } catch (NoSuchFieldException e) {
                    try {
                        throw new IllegalActionException(this, e,
                                "No return type field '_" + port.getName()
                                        + "'");
                    } catch (Throwable throwable) {
                        getDirector().stop();
                    }
                }

                if (typ.equals("boolean")) {
                    port.send(0, new BooleanToken(((Boolean) ret)
                            .booleanValue()));
                } else if (typ.equals("double")) {
                    port.send(0, new DoubleToken(((Double) ret).doubleValue()));
                } else if (typ.equals("int")) {
                    port.send(0, new IntToken(((Integer) ret).intValue()));
                } else if (typ.equals("char")) {
                    port.send(0,
                            new UnsignedByteToken(((Byte) ret).byteValue()));
                } else {
                    System.out.println("The return type is not convertible "
                            + "with Ptolemy II types.");
                }
            }
            //if the argument is output
            else if ((port != null)
                    && port.isOutput()
                    && (port.getName() != null)
                    && (getArgumentReturn() != null)
                    && !(port.getName().equals(this.getArgumentReturn()
                            .getName()))) {
                String typ = "";
                Field field = null;

                try {
                    field = _class.getDeclaredField("_" + port.getName());
                    typ = field.getType().toString();
                } catch (NoSuchFieldException ex) {
                    try {
                        field = _class.getDeclaredField("_"
                                + port.getName().substring(0,
                                        port.getName().length() - 3));
                        typ = field.getType().toString();
                    } catch (Throwable throwable) {
                        try {
                            throw new IllegalActionException(this, throwable,
                                    "No '+" + port.getName() + "' field !");
                        } catch (Throwable throwable2) {
                            getDirector().stop();
                        }
                    }
                }

                if (field == null) {
                    throw new InternalErrorException("Field '" + port.getName()
                            + "' in '" + _class + "' is null?");
                } else {
                    if (typ.equals("boolean")) {
                        try {
                            port.send(0,
                                    new BooleanToken(field.getBoolean(obj)));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else if (typ.equals("double")) {
                        try {
                            port.send(0, new DoubleToken(field.getDouble(obj)));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else if (typ.equals("int")) {
                        try {
                            port.send(0, new IntToken(field.getInt(obj)));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else if (typ.equals("char")) {
                        try {
                            port.send(0, new UnsignedByteToken(field
                                    .getChar(obj)));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else if (typ.equals("class [I")) {
                        try {
                            if (field == null) {
                                throw new InternalErrorException(
                                        "field == null?");
                            }
                            if (obj == null) {
                                throw new InternalErrorException("obj == null?");
                            }
                            if (field.get(obj) == null) {
                                throw new InternalErrorException(
                                        "field.get(obj)  == null? (field = "
                                                + field + " obj = " + obj);
                            }
                            Token[] toks = new Token[((int[]) field.get(obj)).length];

                            for (int j = 0; j < ((int[]) field.get(obj)).length; j++) {
                                toks[j] = new IntToken(
                                        ((int[]) field.get(obj))[j]);
                            }

                            port.send(0, new ArrayToken(BaseType.INT, toks));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else if (typ.equals("class [D")) {
                        try {
                            Token[] toks = new Token[((double[]) field.get(obj)).length];

                            for (int j = 0; j < ((double[]) field.get(obj)).length; j++) {
                                toks[j] = new DoubleToken(((double[]) field
                                        .get(obj))[j]);
                            }

                            port.send(0, new ArrayToken(toks));
                        } catch (IllegalAccessException ex) {
                            throw new IllegalActionException(this, ex, "Type '"
                                    + typ + "' is not castable");
                        }
                    } else {
                        // FIXME: for char[] and boolean[], there is
                        // no corresponding Token type.
                        System.out.println("The outtype '" + typ
                                + "' is not convertible "
                                + "with Ptolemy II types.");
                    }
                }
            }
        }
    }

    /** Return the argument contained by this entity that has the
     *  specified name.  If there is no such port, return null.  This
     *  method is read-synchronized on the workspace.
     *  @param name The name of the desired argument.
     *  @return A argument with the given name, or null if none exists.
     */
    public Argument getArgument(String name) {
        try {
            _workspace.getReadAccess();
            return (Argument) _argumentsList.get(name);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the argument contained by this entity that is return.
     *  If there is no such argument, return null.
     *  This method is read-synchronized on the workspace.
     *  @return the argument return, or null if none exists.
     */
    public Argument getArgumentReturn() {
        try {
            _workspace.getReadAccess();

            Iterator arguments = this.argumentsList().iterator();
            Argument returnValue = null;

            while (arguments.hasNext()) {
                Argument argument = (Argument) arguments.next();

                if ((argument != null) && argument.isReturn()) {
                    return argument;
                }
            }

            return returnValue;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Load the generated class and search for its fire method.
     */
    public void initialize() throws IllegalActionException {
        String nativeLibrary = JNIUtilities.getNativeLibrary(this);
        String className = "jni." + nativeLibrary + ".Jni" + this.getName();

        URL[] tab = new URL[1];

        try {
            File userDirAsFile = new File(StringUtilities
                    .getProperty("user.dir"));
            tab[0] = userDirAsFile.toURI().toURL();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Could not create URL "
                    + "from user.dir ("
                    + StringUtilities.getProperty("user.dir") + ")");
        }

        try {
            ClassLoader cl = new URLClassLoader(tab);
            _class = cl.loadClass(className);
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex,
                    "Could not load JNI C class '" + className
                            + "' relative to " + tab[0]);
        }

        if (_class == null) {
            throw new IllegalActionException(this, "Could load JNI C class, '"
                    + className + "' relative to " + tab[0]);
        }

        _methods = null;

        try {
            _methods = _class.getMethods();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Interface C _methods not found " + "class was: " + _class);
        }

        if (_methods == null) {
            throw new IllegalActionException(this,
                    "getMethods() returned null?, " + "class was: " + _class);
        }

        //getting the fire _method
        _methodIndex = -1;

        for (int i = 0; i < _methods.length; i++) {
            if (_methods[i].getName().equals("fire")) {
                _methodIndex = i;
                break;
            }
        }

        if (_methodIndex == -1) {
            StringBuffer methodNames = new StringBuffer();

            try {
                for (int i = 0; i < _methods.length; i++) {
                    if (i > 0) {
                        methodNames.append(", ");
                    }

                    methodNames.append(_methods[i].getName());
                }
            } catch (Exception ex) {
                methodNames.append("Failed to get method names: " + ex);
            }

            throw new IllegalActionException(this, "After looking at "
                    + _methods.length + " method(s),"
                    + "did not find fire method in '" + _class
                    + "', method names were: " + methodNames.toString());
        }
    }

    /** Remove all ports by setting their container to null.
     *  As a side effect, the ports will be unlinked from all relations.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     */
    public void removeAllPorts() {
        try {
            _workspace.getWriteAccess();

            // Have to copy _portList to avoid corrupting the iterator.
            List portListCopy = new LinkedList(portList());
            Iterator ports = portListCopy.iterator();

            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                try {
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            this, "<deletePort name=\"" + port.getName()
                                    + "\"/>");
                    request.setUndoable(true);
                    requestChange(request);
                } catch (Exception ex) {
                    // Ignore.
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Remove an argument from this entity.
     *  @param arg The argument to be removed.
     *  @exception IllegalActionException If there is a problem removing
     *  the argument.
     */
    public void removeArgument(Argument arg) throws IllegalActionException {
        _removeAttribute(arg);
        _argumentsList.remove(arg);
    }

    /** Update the ports to match the arguments.
     *  If an Argument has no corresponding port, a Port is added.
     *  If a Port does not have a corresponding Argument, then the Port
     *  is removed.
     *  If a input and/or output nature of a Port does not match the
     *  Argument with the same name, then the Port is adjusted.
     *  @exception IllegalActionException If there is a problem updating
     *  the ports.
     */
    public void updatePorts() throws IllegalActionException {
        Iterator arguments = this.argumentsList().iterator();
        TypedIOPort port;

        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            port = (TypedIOPort) this.getPort(argument.getName());

            if (port == null) {
                MoMLChangeRequest request = null;

                try {
                    if (argument.isReturn()) {

                        request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"output\"/>\n"
                                        + "</port>");
                    } else if (argument.isInput() && argument.isOutput()) {
                        request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "in"
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"input\"/>\n"
                                        + "</port>\n"
                                        + "<port name=\""
                                        + argument.getName()
                                        + "out"
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"output\"/>\n"
                                        + "</port>");
                    } else {
                        request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + (argument.isInput() ? "    <property name=\"input\"/>\n"
                                                : "")
                                        + (argument.isOutput() ? "    <property name=\"output\"/>\n"
                                                : "") + "</port>");

                    }
                    request.setUndoable(true);
                    requestChange(request);
                } catch (Throwable throwable) {
                    throw new IllegalActionException(this, throwable,
                            "MoMLChangeRequest for \"" + argument.getName()
                                    + "\" failed. Request was:\n" + request);
                }
            } else {
                // We have a preexisting port, synchronized the
                // arguments and the ports.

                if (argument.isReturn()) {
                    if (port.isInput()) {
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <deleteProperty name=\"input\"/>\n"
                                        + (port.isOutput() ? ""
                                                : "    <property name=\"output\"/>\n")
                                        + "</port>");
                        request.setUndoable(true);
                        requestChange(request);
                    }
                    if (!port.isOutput()) {
                        MoMLChangeRequest request = new MoMLChangeRequest(
                                this,
                                this,
                                "<port name=\""
                                        + argument.getName()
                                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                        + "    <property name=\"output\"/>\n"
                                        + "</port>");
                        request.setUndoable(true);
                        requestChange(request);
                    }
                } else /*if (port.isInput() != argument.isInput()
                                || port.isOutput() != argument.isOutput())*/{
                    MoMLChangeRequest request = new MoMLChangeRequest(
                            this,
                            this,
                            "<port name=\""
                                    + argument.getName()
                                    + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                    + (port.isInput() ? (argument.isInput() ? ""
                                            : "<deleteProperty name=\"input\"/>\n")
                                            : (argument.isInput() ? "<property name=\"input\"/>\n"
                                                    : ""))
                                    + (port.isOutput() ? (argument.isOutput() ? ""
                                            : "<deleteProperty name=\"output\"/>\n")
                                            : (argument.isOutput() ? "<property name=\"output\"/>\n"
                                                    : "")) + "</port>");
                    request.setUndoable(true);
                    requestChange(request);
                }
            }
        }

        // Remove any ports that do not have arguments.
        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            port = (TypedIOPort) ports.next();
            Argument argument = (Argument) _argumentsList.get(port.getName());
            if (argument == null) {
                MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                        "<deletePort name=\"" + port.getName() + "\"/>");
                request.setUndoable(true);
                requestChange(request);

            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an argument to this entity.
     *  @param arg The argument to be added.
     *  @exception IllegalActionException If the argument
     *  is not of an acceptable class for the container or the name contains
     *  a period.
     *  @exception NameDuplicationException If there is already an
     *  argument with the same name.
     */
    protected void _addArgument(Argument arg) throws IllegalActionException,
            NameDuplicationException {
        _addAttribute(arg);
        _argumentsList.append(arg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of Ports, which correspond to arguments of the native function,
    // owned by this Entity.
    private NamedList _argumentsList;

    // A private class dynamicly loaded for the JNI interface.
    private Class _class;

    // The index of the method we want to invoke in the _methods array.
    private int _methodIndex;

    // A private array that contains the methods of the generated
    // interface class.
    private Method[] _methods;
}
