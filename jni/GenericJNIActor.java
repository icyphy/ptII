/** An actor able to call a C function

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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
     *  @exception IllegalActionException If the name has a period.
     */
    public GenericJNIActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _argumentsList = new NamedList(this);

        // FIXME: Should libraryDirectory be a FileAttribute?
        libraryDirectory = new Parameter(this, "libraryDirectory",
                (Token) new StringToken("jni/lib"));
        nativeFunction = new Parameter(this, "nativeFunction",
                (Token) new StringToken("unknownFunction"));
        nativeLibrary = new Parameter(this, "nativeLibrary",
                (Token) new StringToken("unknownLibrary"));

        _attachText(
                "_iconDescription",
                "<svg>\n"
                + "<rect x=\"0\" y=\"0\" "
                + "width=\"32\" height=\"38\" "
                + "style=\"fill:white\"/>\n"
                + "<image x=\"1\" y=\"1\" width=\"32\" height=\"38\""
                + "xlink:href=\"jni/dll.gif\"/>\n"
                + "</svg>\n");

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

    /** Add a return argument to this entity
     */
    public void addArgumentReturn()
            throws IllegalActionException, NameDuplicationException {
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
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
                throw new InvalidStateException(
                        this,
                        "Failed to clone an Entity: " + ex.getMessage());
            }
        }
        Class myClass = getClass();
        Field fields[] = myClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].get(newEntity) instanceof Argument) {
                    fields[i].set(
                            newEntity,
                            newEntity.getArgument(fields[i].getName()));
                }
            } catch (IllegalAccessException e) {
                throw new CloneNotSupportedException(
                        e.getMessage() + ": " + fields[i].getName());
            }
        }
        return newEntity;
    }

    /** For each Argument, a port of the same name is created,
     *  belonging to this argument.
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
                        port = (TypedIOPort) this.newPort(argument.getName());
                        port.setInput(false);
                        port.setOutput(true);
                        port.setTypeEquals(BaseType.GENERAL);
                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct "
                                + "return port '"
                                + port +"'");
                    }
                } else if (argument.isInput() && argument.isOutput()) {
                    try {
                        port = (TypedIOPort) this.newPort(argument.getName()
                                + "in");
                        port.setInput(argument.isInput());
                        port.setTypeEquals(BaseType.GENERAL);
                        port = (TypedIOPort) this.newPort(argument.getName()
                                + "out");
                        port.setOutput(argument.isOutput());
                        port.setTypeEquals(BaseType.GENERAL);
                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct "
                                + "input or output "
                                + "port '"
                                + port +"'");
                    }
                } else {
                    try {
                        port = (TypedIOPort) this.newPort(argument.getName());
                        port.setInput(argument.isInput());
                        port.setOutput(argument.isOutput());
                        port.setTypeEquals(BaseType.GENERAL);
                    } catch (Exception ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable to construct "
                                + "port '"
                                + port +"'");
                    }
                }
            } else {
                //end if port == nul
                // synchronized the arguments and the ports
                if (argument.isReturn()) {
                    port.setInput(false);
                    port.setOutput(true);
                    port.setTypeEquals(BaseType.GENERAL);
                } else {
                    port.setInput(argument.isInput());
                    port.setOutput(argument.isOutput());
                    port.setTypeEquals(BaseType.GENERAL);
                }
            }
        }
    }

    /** Read the argument of the function from the ports,
     *  call the native method throw the generated interface,
     *  and put the results on the corresponding ports
     *  @exception IllegalActionException If a exception occured
     */
    public void fire() throws IllegalActionException {

        //getting the in/inout parameters
        Iterator ports = this.portList().iterator();
        Vector args = new Vector();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (port.isInput() && port.hasToken(0) &&
                    !(port.isOutput()&&!port.isInput())) {
                Token tok = (Token) port.get(0);

                String typ = (String) _methods[_methodIndex]
                    .getParameterTypes()[args.size()].toString();
                if (typ.equals("boolean")) {
                    args.add(new Boolean((boolean)
                            ((BooleanToken) tok).booleanValue()));
                } else if (typ.equals("int")) {
                    args.add(new Integer((int)((IntToken) tok).intValue()));
                } else if (typ.equals("double")) {
                    args.add(new Double((double)((DoubleToken) tok)
                            .doubleValue()));
                } else if (typ.equals("class [I")) {
                    int siz = ((ArrayToken) tok).arrayValue().length;
                    int[] tab = new int[siz];
                    for (int j = 0; j < siz; j++) {
                        tab[j] = (int) ((IntToken) (((ArrayToken) tok)
                                .arrayValue()[j]))
                            .intValue();
                    }
                    //(int[])((ArrayToken)tok).arrayValue();
                    args.add((Object)tab);
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
                String libraryPath =
                    StringUtilities.getProperty("java.library.path");

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
                        + "restart.\n"
                        + "For example, under Windows "
                        + "in a Cygwin bash shell:\n"
                        + "PATH=/cygdrive/c/ptII/jni/dll:${PATH}\n"
                        + "export PATH\n"
                        + "vergil -jni foo.xml\n"
                        + "A common error is that "
                        + "the class cannot be found in "
                        + "property 'java.library.path' "
                        + "which is:\n"
                        + libraryPath
                        + "\nError message was: "
                        + error.getMessage(),
                        error);
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
                if (args.size() >= 1 ) {
                    argumentsDescription.append(args.elementAt(0)
                            .toString());
                    for (int i = 1; i < args.size(); i++) {
                        argumentsDescription.append(", "
                                + args.elementAt(i)
                                .toString());
                    }
                }
            } catch (Exception ex2) {
                // Ignore
            }
            throw new IllegalActionException(this, ex,
                    "Native operation call failed."
                    + "Failed to invoke '" + obj
                    + "' with " + args.size()
                    + " arg(s) "
                    + argumentsDescription.toString()
                                             );
        }

        ports = portList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            //if the argument is return
            if (port.getName().equals(this.getArgumentReturn().getName())) {
                String typ = "";
                Field field = null;

                try {
                    field = _class.getDeclaredField("_" + port.getName());
                    typ = (String) field.getType().toString();
                } catch (NoSuchFieldException e) {
                    try {
                        throw new IllegalActionException(this, e,
                                "No return type field '_"
                                + port.getName() + "'");
                    } catch (Exception ex2) {
                        getDirector().stop();
                    }
                }
                if (typ.equals("boolean")) {
                    port.send(0, (Token) new BooleanToken(((Boolean) ret)
                            .booleanValue()));
                } else if (typ.equals("double")) {
                    port.send(0, (Token) new DoubleToken(((Double) ret)
                            .doubleValue()));
                } else if (typ.equals("int")) {
                    port.send(0, (Token) new IntToken(((Integer) ret)
                            .intValue()));
                } else if (typ.equals("char")) {
                    port.send(0, (Token) new UnsignedByteToken(((Byte) ret)
                            .byteValue()));
                } else {
                    System.out.println("The return type is not convertible "
                            + "with Ptolemy II types.");
                }
            }
            //if the argument is output
            else if (
                    port.isOutput()
                    && !(port.getName()
                            .equals(this.getArgumentReturn().getName()))) {

                String typ = "";
                Field field = null;

                try {
                    field = _class.getDeclaredField("_" + port.getName());
                    typ = (String) field.getType().toString();
                } catch (NoSuchFieldException ex) {
                    try {
                        field = _class.getDeclaredField("_"
                                + port.getName().substring(0,
                                        port.getName().length() - 3));
                        typ = (String) field.getType().toString();
                    } catch (Exception e) {
                        try {
                            throw new IllegalActionException(this, e,
                                    "No '+" + port.getName() + "' field !");
                        } catch (Exception ex2) {
                            getDirector().stop();
                        }
                    }
                }
                if (typ.equals("boolean")) {
                    try {
                        port.send(0,
                                (Token) new BooleanToken(field
                                        .getBoolean(obj)));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
                    }
                } else if (typ.equals("double")) {
                    try {
                        port.send(0,
                                (Token) new DoubleToken(field.getDouble(obj)));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
                    }
                } else if (typ.equals("int")) {
                    try {
                        port.send(0,
                                (Token) new IntToken( field.getInt(obj)));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
                    }
                } else if (typ.equals("char")) {
                    try {
                        port.send(0, (Token) new UnsignedByteToken((char)field
                                .getChar(obj)));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
                    }
                } else if (typ.equals("class [I")) {
                    try {
                        Token[] toks =  new Token[((int[])field.get(obj))
                                .length];
                        for (int j = 0; j<((int[])field.get(obj)).length ; j++)
                            toks[j] = new IntToken(((int[])field.get(obj))[j]);
                        port.send(0, new ArrayToken(toks));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
                    }
                }  else if (typ.equals("class [D")) {
                    try {
                        Token[] toks =  new Token[((double[])field.get(obj))
                                .length];
                        for (int j = 0; j<((double[])field.get(obj)).length;
                             j++) {
                            toks[j] = new DoubleToken(((double[])field
                                    .get(obj))[j]);
                        }
                        port.send(0, new ArrayToken(toks));
                    } catch (IllegalAccessException ex) {
                        throw new IllegalActionException(this, ex,
                                "Type '" + typ + "' is not castable");
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
                if (argument != null && argument.isReturn()) {
                    return (Argument) argument;
                }
            }
            return (Argument) returnValue;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Load the generated class and search for its fire method.
     */
    public void initialize() throws IllegalActionException {

        //         String interNativeLibraryValue =
        //             "jni" + nativeLibraryValue
        //             .substring(1, nativeLibraryValue.length() - 1);

        //        String interNativeLibrary = JNIUtilities._getInterNativeLibrary(this);
        //searching the class generated

        //        String className = "jni." + interNativeLibrary + ".Jni"
        //            + this.getName();

        String nativeLibrary = JNIUtilities.getNativeLibrary(this);
        String className = "jni." + nativeLibrary + ".Jni"
            + this.getName();



        URL[] tab = new URL[1];
        try {
            File userDirAsFile =
                new File(StringUtilities.getProperty("user.dir"));
            tab[0] = userDirAsFile.toURL();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Could not create URL "
                    + "from user.dir ("
                    + StringUtilities
                    .getProperty("user.dir") + ")");
        }
        try {
            ClassLoader cl = new URLClassLoader(tab);
            _class = cl.loadClass(className);
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex,
                    "Could not load JNI C class '"
                    + className + "' relative to "
                    + tab[0]);

        }

        if (_class == null) {
            throw new IllegalActionException(this,
                    "Could load JNI C class, '"
                    + className + "' relative to "
                    + tab[0]);
        }

        // FIXME: This adds to the path every time the actor is initialized
        // FIXME: This does not work for me anyway.  I think the
        // java.library.path needs to be set by the environment before
        // the java process starts or else it needs to be set with
        // -Djava.library.path when java is invoked

        // Add the value of libraryDirectory to the java.library.path
        // First, look relative to the current directory (user.dir)
        // Second, look relative to $PTII

        //         System.setProperty("java.library.path",
        //                 StringUtilities.getProperty("user.dir")
        //                 + File.separator
        //                 + libraryDirectoryValue
        //                 + File.pathSeparator

        //                 + StringUtilities
        //                 .getProperty("ptolemy.ptII.dir")
        //                 + File.separator
        //                 + libraryDirectoryValue
        //                 + File.pathSeparator

        //                 + StringUtilities.getProperty("user.dir")
        //                 + File.separator
        //                 + "jni"
        //                 + File.separator
        //                 + "jnitestDeux"
        //                 + File.pathSeparator

        //                 + System.getProperty("java.library.path"));

        _methods = null;

        try {
            _methods = _class.getMethods();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Interface C _methods not found "
                    + "class was: " + _class);
        }

        if (_methods == null) {
            throw new IllegalActionException(this,
                    "getMethods() returned null?, "
                    + "class was: " + _class);

        }
        //getting the fire _method
        _methodIndex = -1;
        for (int i = 0; i < _methods.length; i++) {
            if (_methods[i].getName().equals("fire"))
                _methodIndex = i;
            break;
        }

        if (_methodIndex == -1) {
            throw new IllegalActionException(this,
                    "After looking at "
                    + _methods.length + " method(s),"
                    + "did not find fire method in '"
                    +  _class + "'");

        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an argument to this entity
     */
    protected void _addArgument(Argument arg)
            throws IllegalActionException, NameDuplicationException {
        _argumentsList.append((Nameable) arg);
    }

    /** Remove an argument from this entity.
     * @exception IllegalActionException If an error occurs.
     */
    protected void _removeArgument(Argument arg)
            throws IllegalActionException {
        _argumentsList.remove(((Nameable) arg));
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
