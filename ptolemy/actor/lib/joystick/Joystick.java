/* Ptolemy Actor that reads data from a Joystick
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

@ProposedRating red (winthrop@eecs.berkeley.edu)
@AcceptedRating red (winthrop@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.joystick;

import ptolemy.actor.lib.Source;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
////
/**
This actor reads data from a Joystick using the Joystick interface
from
<a href="http://sourceforge.net/projects/javajoystick/" target="_top"><code>http://sourceforge.net/projects/javajoystick/</code></a>

<p>Currently, this actor will only work under Windows, though
the Joystick interface also supports Linux.

<p>If you get the following error on the console:
<pre>
TBA
</pre>
then you need to be sure that the
<code>jjstick.dll</code> is in your path.
<p>The default location of the Joystick interface is
add
<code>$PTII/vendors/misc/joystick/lib</code>, so you could either
add that directory to your path, or copy <code>jjstick.dll</code>
to <code>$PTII/bin</code>


<b>FIXME: the information below is about the SerialComm actor, but
might be relevant</b>

<p>This actor is designed for use in DE and SDF.  By default, this actor
does not block in fire().  When fired, if no data is available on the
serial port, then no token is produced.  However, when using this actor in
SDF, you may want to experiment with the <i>blocking</i> parameter.
Setting this true has the actor block in fire() ultil data arrives.

<p>Bytes to be sent must enter the actor as an array of integers.
The lowest order byte from each integer is used.  (Negative numbers
are treated as though 256 has been just added enough times to make them
non-negative.)  Likewise, bytes received are broadcast out of the actor
as an integer array of which only the low bytes carry data.  This
actor makes no guarantee as to the contents of the other three bytes
of the integer.

<p>This actor is a class which implements SerialPortEventListener.
This means that when serial events (such as DATA_AVAILABLE) occur,
this actor's serialEvent() method gets called.  the serialEvent()
method calls the director's fireAtCurrentTime() method, triggering
a call to fire().

<p>By the time fire() executes, there may be several bytes of available
data.  This is primarily because the UART only signals the software every
8 bytes or so.  These are packaged by fire() into an array of integers
(one int per byte) and broadcast.

<p>The fire() method's reading of serial data is also governed by the
<i>blocking</i>, <i>threshold</i>, and <i>truncation</i> parameters.
Fire() first tests to see if there are at least <i>threshold</i> bytes
of data available.  If so, it reads them, or the <i>truncation</i>
most recent bytes, whichever is fewer.  (Or all the bytes, regardless
of how many, if <i>truncation</i> is set to zero.)  If fewer than
<i>threshold</i> bytes are available, then if <i>blocking</i> is false
it will not read any data, but if <i>blocking</i> is true it will wait
until at least <i>threshold</i> bytes are available.

<p>Because the DATA_AVAILABLE event typically occurs every 8 bytes,
continuous data arriving at 115200 baud can wake up the actor 1440
times per second!  This is to often to be calling fireAt*() on
the director of a DE model.  Thus, after the first fireAtCurrentTime()
call, the serialEvent() callback only notifys the fire() method
(in case it is awaiting additional data to meet its <i>threshold</i>)
and does not call fireAt*() again until the actor has completed a
firing since the last time fireAt*() was called.

@author Paul Yang
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.actor.lib.comm.SerialComm
 */
public class Joystick extends Source {

    /** Construct a Joystick actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Joystick(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter changed is <i>serialPortName</i>, then hope
     *  the model is not running and do nothing.  Likewise for baudRate.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Maybe thrown (?)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
	super.attributeChanged(attribute);
    }

    /** Transfers data between the Ptolemy model and the built in
     *  buffers associated with the serial port.  Actual serial
     *  input and output occur right before or right after fire().
     *  For example, serial output occurs in response to the .flush()
     *  call below.  This data written to the serial port out to the
     *  serial hardware.  The .flush() method does not wait for the
     *  hardware to complete the transmission, as this might take
     *  many milliseconds (roughly 1mS for every 10 bytes at 115200
     *  baud).
     *  <p>
     *  This fire() method checks for either or both of the following
     *  conditions.  Data may have been received and is available in
     *  the serial port.  A Token may have been received by this actor.
     *  If at least 1 byte is available, broadcast it.
     *  If an integer-array token is available, take a byte out
     *  of each integer and send the byte stream to the serial port.
     *  @exception IllegalActionException Thrown if the try fails.
     */
    public synchronized void fire() throws IllegalActionException {
	super.fire();
    }

    /** [Pre]initialize does the resource allocation for this actor.
     *  Specifically, it opens the serial port (setting the baud rate
     *  and other communication settings) and then activates the
     *  serial port's event listening resource, directing events to the
     *  serialEvent() method of this actor.  (serialEvent() is the
     *  required name for this method.  It is required since this actor
     *  implements SerialPortEventListener.  It is not explicitly named in
     *  the calls to .addEventListener() and .notifyOnDataAvailable()
     *  below.)
     *  @exception IllegalActionException if the try fails.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }


    /** Wrap up deallocates resources, specifically the serial port.
     *  @exception IllegalActionException Maybe thrown (?).
     */
    public void wrapup() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}


