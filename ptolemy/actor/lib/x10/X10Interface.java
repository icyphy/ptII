/* This class creates an x10 inteface device which can send and receive 
10 commands to and from an x10 network.
 
 Copyright (c) 1998-2003 The Regents of the University of California.
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
                                        
ProposedRating Green (ptolemy@ptolemy.eecs.berkeley.edu)
AcceptedRating Yellow (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.x10;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.comm.CommPortIdentifier;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import x10.CM11ASerialController;
import x10.CM17ASerialController;
import x10.Controller;

//////////////////////////////////////////////////////////////////////////
//// x10Interface
/**
 * This class abstracts x10-interface devices for x10 connunication via the
 * serial port. Currently, this class supports the following x10 serial port
 * models for communication with a host computer:
 * <ul>
 * <li> "CM11A" serial module (wired)
 * <li> "CM17A" serial module (wireless)
 * <ul>
 * Note that the wireless serial module is unidirectional, only sending
 * commands to the X10 network, and not receiving from it.  Thus, the
 * Listener, CommandSensor, and LevelSensor actors will not work with it.
 * For instructions concerning the physical setup of these devices, refer to 
 * their respective manuals.
 * <p>
 * This actor requires that the Java comm API be installed.
 * The comm API comes from http://java.sun.com/products/javacomm/
 * To install the comm API on a Windows machine:
 * <ul>
 * <li> place the win32com.dll in $JDK\jre\bin directory. 
 * <li> make sure the win32com.dll is executable.
 * <li> Place the comm.jar in $JDK\jre\lib\ext. 
 * <li> Place the javax.comm.properties in $JDK\jre\lib . 
 * </ul>
 * where $JDK is the location of your Java development kit.
 * <p>
 * If the <i>x10Interface</i> or <i>serialPortName</i> parameters are changed
 * after preinitialize() is called, the changes will not take effect until
 * the next execution of the model.
 * <p>
 * Derived classes must implement send and receive functionality.
 * Furthermore, this class requires the x10 library (jar), which can
 * be obtained from
 * <a href="http://x10.homelinux.org/download.html">http://x10.homelinux.org/download.html</a>.
 * Unzip the file tjx10p-11.zip (Version 1.1) and install it in
 * $PTII/vendors/misc/x10. Then re-run configure in $PTII and rebuild to
 * compile the actors in the x10 library.
 * The configure script looks for the library in tjx10p-11/lib/x10.jar.
 * Note that these actors also require javax.comm be installed to work properly.
 * That can be obtained from <a href="http://java.sun.com/products/javacomm/">
 * http://java.sun.com/products/javacomm/</a>. To install it, unzip the commapi
 * directory into $PTII/vendors/sun and do the following (on a Windows machine):
 * <ul>
 * <li> Place the win32com.dll in jdk\jre\bin directory. 
 * <li> Place the comm.jar in jdk\jre\lib\ext. 
 * <li> Place the javax.comm.properties in jdk\jre\lib.
 * </ul>
 * where jdk is the location of your Java development kit.
 * Then re-run configure in $PTII.
 *
 * @author Colin Cochran and Edward A. Lee
 * @version $Id$
 */
public class X10Interface extends TypedAtomicActor {
    
    // NOTE: This class has a bit of duplication with actor.lib.io.SerialComm.
    // These should probably be consolidated.
    
    // FIXME: The x10 library that this relies on has a number of problems.
    // First, it takes down connections only in finalize(), which we can't
    // force to run.  Second, it has concurrency errors.  You might get
    // a ConcurrentModificationException at
    // x10.UnitEventDispatcher.run(UnitEventDispatcher:83).
    // The only recourse is to exit the application and restart.
    // We should remove the dependence on this package.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public X10Interface(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
                
        super(container, name);

        // Create input ports and port parameters.    
        x10Interface = new StringParameter(this, "x10Interface");
        serialPortName = new StringParameter(this, "serialPortName");
        
        // The x10 interface is selectable, e.g. CM11A or CM17A. The x10 
        // parameter allows the user to choose an interface.
        x10Interface.addChoice("CM11A");
        x10Interface.addChoice("CM17A");
        x10Interface.setExpression("CM11A");

        // Enumerate the available ports.
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        String defaultChoice = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier identifier =
                    (CommPortIdentifier) ports.nextElement();
            if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                String value = identifier.getName();
                serialPortName.addChoice(value);
                if (defaultChoice == null) {
                    defaultChoice = value;
                }
            }
        }
        if (defaultChoice == null) {
            defaultChoice = "no ports available";
            serialPortName.addChoice(defaultChoice);
        }
        serialPortName.setExpression(defaultChoice);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Attribute giving the type of interface to use. This is a string
     *  with default "CM11A". Currently, options are:
     *  <ul>
     *  <li> CM11A
     *  <li> CM17A
     *  <ul>
     */
    public StringParameter x10Interface;
    
    /** Attribute giving the serial port to use. This is a string with
     *  the default being the first serial port listed by the
     *  javax.comm.CommPortIdentifier class.  If there are no serial
     *  ports available (meaning probably that the javax.comm package
     *  is not installed properly), then the value of the string will
     *  be "no ports available".
     */
    public StringParameter serialPortName;
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Prepare the x10 interface, specified by the <i>x10Interface</i>
     *  parameter. This is done in preinitialize() because that method
     *  is assured of being invoked only once.
     *  @exception IllegalActionException If an exception is thrown opening
     *   the port.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        
        // NOTE: using private variables here ensures that if the parameter
        // values are changed while the model is running, the same port
        // and controller are taken down in wrapup() as are opened here.
        _controllerName = x10Interface.stringValue();
        _portName = serialPortName.stringValue();
        // The interface should only be opened ONCE during initialization.
        try {
            _interface = _openInterface(_portName, _controllerName);
        } catch (IOException ex) {
            throw new IllegalActionException(
                this,
                ex,
                "Failed to open X10 controller.");
        }
    }
    
    /** Close an x10 interface.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // The interface should only be closed ONCE during wrapup.
        _closeInterface(_portName);
    }

    /////////////////////////////////////////////////////////////////// 
    ////                         protected variables               ////

    /** This is the interface object used for sending and receiving x10 
      * commands.
      */
    protected Controller _interface;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Disassociate a user with an interface. When no more users are accessing
     *  an interface, the interface is closed.
     *  @param portName The name of the serial port this controller.
     */
    private static void _closeInterface(String portName) {
        synchronized (_serialNameToController) {
            // If the port is not in the HashMap, then opening it failed.
            if (_serialNameToController.containsKey(portName)) {
                _serialNameToUserCount.put(
                    portName,
                    new Integer(
                        ((Integer) _serialNameToUserCount.get(portName))
                            .intValue()
                            - 1));
                if (((Integer) _serialNameToUserCount.get(portName)).intValue()
                    == 0) {
                    // FIXME: Unfortunately, the x10 API takes down the controller
                    // in the finalize() method of the CM11ASerialController or
                    // CM17ASerialController.  However, there is no way to
                    // force that method to run.  Consequently, if we remove
                    // the reference here, then we will likely have to restart
                    // the Java virtual machine to establish a new connection.
                    // Thus, we do not remove this, and instead we leave the
                    // connection open.  We should probably change this in the
                    // source code for the X10 API.
                    /*
                     _serialToController.remove(portName);
                     _serialUsers.remove(portName);
                    */
                }
            }
        }
    }
    
    /** Return an x10 interface for sending and receiving x10 commands.
     *  @param portName The name of the serial port for the controller.
     *  @param controller The type of controller.
     *  @throws IOException If the serial port cannot be opened.
     */
    private static Controller _openInterface(
        String portName,
        String controller)
        throws IOException {
            
        synchronized (_serialNameToController) {
            if (!_serialNameToController.containsKey(portName)) {
                if (controller.equals("CM11A")) {
                    _serialNameToController.put(
                        portName,
                        new CM11ASerialController(portName));
                } else if (controller.equals("CM17A")) {
                    _serialNameToController.put(
                        portName,
                        new CM17ASerialController(portName));
                }
                _serialNameToUserCount.put(portName, new Integer(1));
            } else {
                _serialNameToUserCount.put(
                    portName,
                    new Integer(
                        1
                            + ((Integer) _serialNameToUserCount.get(portName))
                                .intValue()));
            }
            return ((Controller) _serialNameToController.get(portName));
        }
    }
    /////////////////////////////////////////////////////////////////// 
    ////                         private variables                 ////
    
    /** This is the type of controller being used. 
     */
    private String _controllerName;
    
    /** This is the name of the serial port being used.
     */
    private String _portName;
    
    /** This hash table associates a serial port name with a controller.
     */
    private static HashMap _serialNameToController = new HashMap();
    
    /** This hash table stores how many users are using each serial port.
     */
    private static HashMap _serialNameToUserCount = new HashMap();
}
