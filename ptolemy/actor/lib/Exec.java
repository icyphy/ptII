/* Execute a command in a subprocess.

 Copyright (c) 2004 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu) 2/5/04
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

//////////////////////////////////////////////////////////////////////////
//// Execute
/**
Execute a command as separately running subprocess.

<p>This actor uses java.lang.Runtime.exec() to invoke the subprocess
For information about Runtime.exec(), see:
<a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html" target="_top">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>
and
<a href="http://mindprod.com/jgloss/exec.html" target="_top">http://mindprod.com/jgloss/exec.html</a>

@author Christopher Hylands Brooks, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public class Exec extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Exec(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setToken(BooleanToken.TRUE);

        command = new PortParameter(this, "command",
                new StringToken("echo 'Hello, world.'"));
        // Make command be a StringParameter (no surrounding double quotes).
        command.setStringMode(true);

        directory = new FileParameter(this, "directory");
        directory.setExpression("$CWD");
        // Hide the directory parameter.
        directory.setVisibility(Settable.EXPERT);

        environment = new Parameter(this, "environment");
        environment.setTypeEquals(new ArrayType(BaseType.STRING));
        // Array with a size of one empty element means use the
        // default environment of the calling process.
        environment.setExpression("{\"\"}");
        // Hide the environment parameter.
        environment.setVisibility(Settable.EXPERT);

        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

        waitFor = new Parameter(this, "waitFor");
        waitFor.setTypeEquals(BaseType.BOOLEAN);
        waitFor.setToken(BooleanToken.FALSE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** Indicator of whether fire method blocks on the process and waits
     *  for output.  
     *  If true, fire() waits until there is output from the subprocess.
     *  The type is boolean with default true.
     */
    public Parameter blocking;

    /** The command to be executed.  The command is parsed by
     * {@link ptolemy.util.StringUtilities#tokenizeForExec(String)}
     * into tokens and then executed as a  separate process.
     * The initial default is the string "echo 'Hello, world.'".
     * If the value of the <i>command</i> PortParameter changes,
     * then each time fire() is called, a new subprocess always is invoked.
     * If the value() of the <i>command</i> PortParameter does not change,
     * then each time fire() is called, a new subprocess is only invoked
     * if the previous subprocess exited.
     */
    public PortParameter command;

    /** The directory to execute the command in.  The directory parameter is
     *  read each time the subprocess is executed. The initial default
     *  value of this parameter $CWD, which corresponds with the value
     *  of the JDK user.dir property.  
     */
    public FileParameter directory;

    // FIXME: need a test here
    /** The environment to execute the command in.  This parameter is read
     *  each time the subprocess is executed. This parameter
     *  is an array of strings of the format { {"name=value"} }.  If the
     *  length of the array is zero (the default), then the environment
     *  from the current process is used in the new command.
     */
    public Parameter environment;

    /** Data that is generated by the subprocess on standard error.
     */
    public TypedIOPort error;

    /** Strings to pass to the standard in of the subprocess.
     */
    public TypedIOPort input;

    /** Data that is generated by the subprocess on standard out.
     */
    public TypedIOPort output;

    /** Indicator of whether fire method blocks until the process exits.
     *  If true, fire() waits until the underlying java.lang.Process.waitFor()
     *  method returns, indicating that the process has exited.
     *  The type is boolean with an initial default value of false.
     */
    public Parameter waitFor;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blocking) {
            _blocking =
                ((BooleanToken)blocking.getToken()).booleanValue();
        } else if (attribute == waitFor) {
            _waitFor =
                ((BooleanToken)waitFor.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }


    /** Send input to the subprocess and read output and errors.
     */
    public synchronized void fire() throws IllegalActionException {
        super.fire();
        String line = null;

        try {

            command.update();

            if (_process == null
                    || ((StringToken)command.getToken()).stringValue()
                    != _oldCommandValue) {
                // If the command changed, we stop the process
                // and start another.
                _terminateProcess();
                _exec();
                _oldCommandValue =
                    ((StringToken)command.getToken()).stringValue();
            }

            // FIXME: What if there is no input?
            if (input.numberOfSources() > 0
                    && input.hasToken(0)) {
                // FIXME: Do we need to append a new line?
                if ((line = ((StringToken)input.get(0)).stringValue())
                        != null) {
                    if (_debugging) {
                        _debug("Exec: Input: '" + line + "'");
                    }
                    if (_inputBufferedWriter != null) {
                        _inputBufferedWriter.write(line);
                        try {
                            _inputBufferedWriter.flush();
                        } catch (IOException ex) {
                            // If we are running a 'echo "foo"' process,
                            // then flushing might throw an IOException
                            // if the pipe to the subprocess is closing
                            if (_debugging) {
                                _debug("Exec.fire(), "
                                        + "_inputBufferWriter.flush() failed"
                                        + " but we are ignoring it.");
                            }
                        }
                    }
                }
            }

            String errorString;
            String outputString;
            if (_debugging) {
                _debug("Exec: about to call _outputGobbler.*GetAndReset");
            }

            if (_waitFor) {
                // See ptolemy.gui.JTextAreaExec for similar waitFor() code;
                try {
                    int processReturnCode = _process.waitFor();
                    synchronized(this) {
                        _process = null;
                    }
                    // FIXME: throw an exception if we get a non zero code?
                    // if (processReturnCode != 0) break;
                } catch (InterruptedException interrupted) {
                    throw new InternalErrorException(this, interrupted,
                            "_process.waitFor() was interrupted");
                }

            }

            if (_blocking) {
                outputString = _outputGobbler.blockingGetAndReset();
            } else {
                outputString = _outputGobbler.nonblockingGetAndReset();
            }

            if (_debugging) {
                _debug("Exec: about to call _errorGobbler.*GetAndReset");
            }

            errorString = _errorGobbler.nonblockingGetAndReset();
            if (_debugging) {
                _debug("Exec: Error: '" + errorString + "'");
                _debug("Exec: Output: '" + outputString + "'");
            }

            error.send(0, new StringToken(errorString));
            output.send(0, new StringToken(outputString));

        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem reading or writing '" + line + "'");
        }
    }

    /** Create the subprocess.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _process = null;
    }

    /** Override the base class to stop waiting for input data.
     */
    public /*synchronized*/ void stop() {
        // This method is not synchronized because when the user
        // hits the stop button, the fire() method is at
        // blockingGetAndReset() and waiting for input.
        // What we do is call notifyAll on the outputGobbler process.

        // See ptolemy.actor.io.comm.SerialComm for a similar situation.
        super.stop();
        if (_outputGobbler != null) {
            _outputGobbler.notifyStringBuffer();

        }
    }

    /** Override the base class to stop waiting for input data.
     */
    public /*synchronized*/ void stopFire() {
        // See the comment in stop() for why this is _not_ synchronized
        super.stopFire();
        _stopFireRequested = true;
        if (_outputGobbler != null) {
            _outputGobbler.notifyStringBuffer();

        }
    }

    /** Terminate the subprocess.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        _terminateProcess();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    // Execute a command, set _process to point to the subprocess
    // and set up _errorGobbler and _outputGobbler.
    private void _exec() throws IllegalActionException {
        try {
            _stopFireRequested = false;

            if (_process != null) {
                // Note that we assume that _process is null upon entry
                // to this method, but we check again here just to be sure.
                _terminateProcess();
            }

            Runtime runtime = Runtime.getRuntime();

            String [] commandArray =
                StringUtilities.tokenizeForExec(
                        ((StringToken)command.getToken()).stringValue());

            File directoryAsFile = directory.asFile();

            if (_debugging) {
                _debug("About to exec \""
                        + ((StringToken)command.getToken()).stringValue()
                        + "\""
                        + "\n in \"" + directoryAsFile
                        + "\"\n with environment:");
            }

            Token [] environmentToken =
                ((ArrayToken)environment.getToken()).arrayValue();

            String [] environmentArray = null;
            if (environmentToken.length >= 1) {
                environmentArray = new String[environmentToken.length];
                for (int i = 0; i < environmentToken.length; i++) {
                    // FIXME: is there a better way to convert a Token []
                    // to a String []?
                    StringToken stringToken =
                        (StringToken)environmentToken[i];
                    environmentArray[i] = stringToken.stringValue();
                    if (_debugging) {
                        _debug("  " + i + ". \"" + environmentArray[i]
                                + "\"");
                    }

                    if ( i == 0 && environmentToken.length == 1
                            && environmentArray[0].length() == 0) {
                        if (_debugging) {
                            _debug("There is only one element, "
                                    + "it is a string of length 0,\n so we "
                                    + "pass Runtime.exec() an null "
                                    + "environment so that we use\n "
                                    + "the default environment");
                        }
                        environmentArray = null;
                    }
                }
            }


            _process = runtime.exec(commandArray, environmentArray,
                    directoryAsFile);

            _outputGobbler =
                new _StreamReaderThread(_process.getInputStream(),
                        "Exec Stdout Gobbler-" + _streamReaderThreadCount++,
                        this);
            _errorGobbler =
                new _StreamReaderThread(_process.getErrorStream(),
                        "Exec Stderr Gobbler-" +  _streamReaderThreadCount++,
                        this, _outputGobbler);
            _errorGobbler.start();
            _outputGobbler.start();

            if (_streamReaderThreadCount > 1000) {
                // Avoid overflow. 
                _streamReaderThreadCount = 0;
            }

            OutputStreamWriter inputStreamWriter =
                new OutputStreamWriter(_process.getOutputStream());
            _inputBufferedWriter =
                new BufferedWriter(inputStreamWriter);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem setting up command '" + command + "'");
        }
    }


    // Terminate the process and close any associated streams.
    private void _terminateProcess() throws IllegalActionException {
        // FIXME: Note that if we terminate the streams associated
        // with the process, then we get various errors.

//        try {
//             if (_inputBufferedWriter != null) {
//                 _inputBufferedWriter.close();
//             }
//             if (_process != null) {
//                 if (_process.getInputStream() != null) {
//                     if (_outputGobbler != null) {
//                         _outputGobbler.notifyStringBuffer();
//                     }
//                     _process.getInputStream().close();
//                 }
//                 if (_process.getOutputStream() != null) {
//                     _process.getOutputStream().close();
//                 }
//                 if (_process.getErrorStream() != null) {
//                     _process.getErrorStream().close();
//                 }
//                 //_process = null;
//             }
//
//            // Kill of the gobblers threads. See
//            // http://java.sun.com/j2se/1.4.2/docs/guide/misc/threadPrimitiveDeprecation.html
//             if (_errorGobbler != null) {
//                 Thread moribundErrorGobbler = _errorGobbler;
//                 _errorGobbler = null;
//                 moribundErrorGobbler.interrupt();
//             }
//             if (_outputGobbler != null) {
//                 Thread moribundOutputGobbler = _outputGobbler;
//                 _outputGobbler = null;
//                 moribundOutputGobbler.interrupt();
//             }
//
//         } catch (IOException ex) {
//             System.out.println("Exec._terminate: IOException" + Thread.currentThread());
//             ex.printStackTrace();
//             // ignore
//         }

        // FIXME: Should we do a process.waitFor() and throw an exception
        // if the return value is not 0?

        //synchronized(this) {

        if (_process != null) {
            _process.destroy();
            _process = null;
        }

        //    _process = null;
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Private class that reads a stream in a thread and updates the
    // stringBuffer.
    private class _StreamReaderThread extends Thread {

        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this StreamReaderThread,
         *  which is useful for debugging.
         *  @param actor The parent actor of this thread, which
         *  is used in error messages.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                Nameable actor) {
            this(inputStream, name, actor, null);
        }

        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this StreamReaderThread,
         *  which is useful for debugging.
         *  @param actor The parent actor of this thread, which
         *  is used in error messages.
         *  @param otherStream The other stream associated with
         *  this stream.  If non-null, then if this stream reads
         *  any data, we notify the otherStream.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                Nameable actor, _StreamReaderThread otherStream) {
            super(name);
            _inputStream = inputStream;
            _actor = actor;
            _otherStream = otherStream;

            _lockingStringBuffer = new LockingStringBuffer(actor);
        }

        public String blockingGetAndReset() throws IllegalActionException {
            return _lockingStringBuffer.blockingGetAndReset();
        }

        /** Notify the associatedLockingStringBuffer.
         *  This method is called if we call stop() or stopFire() or if
         *  another StreamReaderThread gets output and we want to
         *  notify this thread so it stop blocking.
         */   
        public void notifyStringBuffer() {
            // Object.notifyAll() is final, so we call this method
            // notifyStringBuffer instead.
            _lockingStringBuffer.notifyStringBuffer();
        }

        /** Get the current value of the stringBuffer and empty
         *  the contents of the StringBuffer.
         */
        public String nonblockingGetAndReset() {
            return _lockingStringBuffer.nonblockingGetAndReset();
        }

        /** Read lines from the inputStream and append them to the
         *  stringBuffer.  If _otherStream is not null, then
         *  if we read any data, notify the other thread. 
         */
        public void run() {
            try {
                InputStreamReader inputStreamReader =
                    new InputStreamReader(_inputStream);
                // We read the data as a char[] instead of using readline()
                // so that we can get strings that do not end in end of
                // line chars such as prompts to stderr when running bash.

                //BufferedReader bufferedReader =
                //    new BufferedReader(inputStreamReader);
                //String line = null;
                //while ( (line = bufferedReader.readLine()) != null) {
                //    if (_debugging) {
                //        _debug("Gobbler: " + line);
                //    }
                //    _lockingStringBuffer.append(line + "\n");
                char [] chars = new char[80];
                int length; // Number of characters read.

                while ((length = inputStreamReader.read(chars, 0, 80))
                        != -1 ) {
                    if (_debugging) {
                        _debug("Gobbler: " + String.valueOf(chars, 0, length));
                    }
                    _lockingStringBuffer.append(chars, 0, length );
                    if (_otherStream != null) {
                        // Wake up the other thread
                        _otherStream.notifyStringBuffer();
                    }
                }
            } catch (Throwable throwable) {
                throw new InternalErrorException(_actor, throwable,
                        "Failed while reading from " + _inputStream); 
            }
        }

        // The actor associated with this stream reader.
        private Nameable _actor;

        // StringBuffer that we read and write to in a locking fashion.
        private LockingStringBuffer _lockingStringBuffer;

        // If non-null, then the other stream to notify if this stream
        // gets any data.
        private _StreamReaderThread _otherStream;

        // StringBuffer to update.
        private StringBuffer _stringBuffer;

        // Stream to read from.
        private InputStream _inputStream;
    }


    // A specially synchronized interface to StringBuffer.
    //
    // The documentation for StringBuffer says that the underlying
    // mechanism is synchronized, but we need slightly finer control
    // so that we can optionally block on reading from a StringBuffer
    // until either there is data or stop as been requested or the
    // error stream has been written to.
    //
    // See
    // http://java.sun.com/docs/books/tutorial/essential/threads/synchronization.html
    private class LockingStringBuffer {

        /** Create a LockingStringBuffer.
         *  @param actor The parent actor of this LockingStringBuffer, which
         *  is used in error messages.
         */   
        public LockingStringBuffer(Nameable actor) {
            _actor = actor;
            _stringBuffer = new StringBuffer();
        }

        /* Call notifyAll() */ 
        public synchronized void notifyStringBuffer() {
            // Object.notifyAll() is final, so we call this method
            // notifyStringBuffer instead.
            notifyAll();
        }

        /** Always append the value to the internal StringBuffer.
         *  @param chars the characters to be appended
         *  @param offset the index of the first character to append.
         *  @param length the number of characters to append.
         */
        public synchronized void append(char[] chars, int offset, int length) {
            // If we have the lock, we always append.
            _stringBuffer.append(chars, offset, length);
            _appendCalled = true;
            if (_debugging) {
                _debug("Exec: append(char[]): set _appendCalled to true");
            }

            notifyAll();
        }

        /** Always append the value to the internal StringBuffer.
         *  @param value The string to append to the internal StringBuffer
         */
        public synchronized void append(String value) {
            // If we have the lock, we always append.
            _stringBuffer.append(value);
            _appendCalled = true;
            if (_debugging) {
                _debug("Exec: append(String): set _appendCalled to true");
            }

            notifyAll();
        }


        /** Block until there is data in the StringBuffer or
         *  until stop() or stopFire() is called and then
         *  return the value of the internal StringBuffer.
         *  The StringBuffer is reset before returning.
         */
        public synchronized String blockingGetAndReset()
                throws IllegalActionException {
            while (_stringBuffer.length() == 0
                    && !_stopRequested
                    && !_stopFireRequested
                    && !_appendCalled ) {
                try {
                    if (_debugging) {
                        _debug("Exec: _blockingGetAndReset: wait() loop "
                                + _appendCalled );
                    }
                    wait();
                } catch (InterruptedException ex) {
                    throw new IllegalActionException(_actor, ex,
                            "Thread interrupted waiting for exec() data.");
                }
            }
            if (_appendCalled) {
                _appendCalled = false;
                // FIXME? If we write to stderr, then appendCalled gets
                // set to true. Wait 100ms to see if anything appears
                // on stdout.
                if (_debugging) {
                    _debug("Exec: _blockingGetAndReset: about 2 sleep");
                }
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException ex) {
                    throw new IllegalActionException(_actor, ex,
                            "Thread interrupted waiting 100 ms. "
                            + "for exec() data.");
                }
            }
            String returnValue = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();

            notifyAll();
            return returnValue;
        }

        /** Get what ever value is in the StringBuffer.
         *  The StringBuffer is reset before returning.
         */
        public synchronized String nonblockingGetAndReset() {
            String returnValue = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();
            _appendCalled = false;
            if (_debugging) {
                _debug("Exec: nonblockingGetAndReset(): "
                        + "set _appendCalled to false");
            }
            notifyAll();
            return returnValue;
        }

        // The actor associated with this locking string buffer;
        private Nameable _actor;

        // The internal StringBuffer that we use to store
        // the data.
        private StringBuffer _stringBuffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicator that append() was called.
    // We use this to handle the case where we are blocking on
    // stdout, but stderr gets output and we want to proceed.
    private boolean _appendCalled = false;

    // Whether this actor is blocking.
    private boolean _blocking;

    // Buffer that we write input to the subprocess with.
    private BufferedWriter _inputBufferedWriter;

    // StreamReader that we read stderr from the subprocess with.
    private _StreamReaderThread _errorGobbler;

    // The previous value of the command parameter.  If the command parameter
    // changes, we kill the old process and start a new process.
    private String _oldCommandValue;

    // StreamReader that we read stdout from the subprocess with.
    private _StreamReaderThread _outputGobbler;

    // The Process that we are running.
    private Process _process;

    // Indicator that stopFire() has been called.
    private boolean _stopFireRequested = false;

    // Instance count of output and error threads, used for debugging.
    // When the value is greater than 1000, we reset to 0.
    private static int _streamReaderThreadCount = 0;

    // Whether we wait for the subprocess to finish.  See Process.waitFor().
    private boolean _waitFor;

}
