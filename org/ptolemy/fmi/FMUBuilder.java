/* Build a FMU file.

 Copyright (c) 2013 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jna.NativeLibrary;

///////////////////////////////////////////////////////////////////
//// FMUBuilder

/**
 * Build a FMU shared object.
 *
 * @author Christopher Brooks
 * @version $Id: FMIModelDescription.java 65508 2013-01-19 13:07:17Z eal $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUBuilder {
    
    /** Create a FMUBuilder.  As the commands are executed,
     *  output is appended to the StringBuffer - no output will appear
     *  on stderr and stdout.
     */
    public FMUBuilder() {
        buffer = new StringBuffer();
    }

    /** Create a FMUBuilder and optionally append to stderr
     *  and stdout as the commands are executed.
     *  @param appendToStderrAndStdout If true, then as the commands
     *  are executed, the output is append to stderr and stdout.
     */
    public FMUBuilder(boolean appendToStderrAndStdout) {
        _appendToStderrAndStdout = appendToStderrAndStdout;
        buffer = new StringBuffer();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The StringBuffer to which the output is appended.  This
     *  variable is public so that callers can clear the buffer as
     *  necessary.
     */
    public StringBuffer buffer;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Build the specified shared library inside the FMU.
     *  <p>A typicaly value is
     *  <code>/tmp/FMUFile12345/binaries/linux64/stepCounter.dylib</code>.	
     *
     *  <p>Typically the sharedLibrary is inside a temporary directory
     *  that was created when the fmu file was unzipped.  The 
     *  directory names the platform for which the binary is to be
     *  built.  This directory resides inside a directory named
     *  <code>binaries/</code>.  Adjacent to the <code>binaries/</code>
     *  directory is the <code>sources/</code> directory.
     *
     *  @param sharedLibraryFile  The shared library that should be built.
     *  @return true if the sharedLibraryFile was built.
     *  @throws IOException If the FMU contains a makefile but
     *  there was a problem building the shared library.
     */
    public boolean build(File sharedLibraryFile) throws IOException {
	stdout(_eol + "Attempting to build " + sharedLibraryFile + _eol);
	// The architecture, typically one of darwin64, linux32, linux64, win32, win64
	String architecture = sharedLibraryFile.getParentFile().getName();

	File sourcesDirectory = new File(sharedLibraryFile.getParentFile().getParentFile().getParentFile(), "sources");

	if (!sourcesDirectory.exists()) {
	    stderr("The source directory \"" + sourcesDirectory  + "\" does not exist." + _eol);
	    return false;
	}

	// FIXME: eventually, we should not use make.
	File makefile = new File(sourcesDirectory, "makefile");

	if (!makefile.exists()) {
	    stderr("The makefile \"" + makefile  + "\" does not exist." + _eol);
	    return false;
	}
	
	ProcessBuilder builder = new ProcessBuilder("make", architecture);
	builder.directory(sourcesDirectory);

	// Eventually, redirect to the buffer and return the results.
	Process process = builder.start();

	// Set up a Thread to read in any error messages
	_StreamReaderThread errorGobbler = new _StreamReaderThread(
                            process.getErrorStream(), this);
	
	// Set up a Thread to read in any output messages
	_StreamReaderThread outputGobbler = new _StreamReaderThread(
                            process.getInputStream(), this);

	// Start up the Threads
	errorGobbler.start();
	outputGobbler.start();

	try {
	    process.waitFor();
	} catch (InterruptedException ex) {
	    process.destroy();
	    throw new IOException("The process building " + sharedLibraryFile + " was interrupted.", ex);
	}

	int exitValue = process.exitValue();
	if (exitValue != 0) {
	    stderr("The exit value of the process building " + sharedLibraryFile + " was non-zero: " + exitValue);
	}
	if (!sharedLibraryFile.exists()) {
	    stderr("Failed to created " + sharedLibraryFile + "?");
	    return false;
	}
	return true;
    }

    /** Append the text message to the StringBuffer.  The output
     *  automatically gets a trailing end of line character(s)
     *  appended.
     *  Optionally, the text also appears on stderr
     *  @param text The text to append.
     */
    public void stderr(final String text) {
        if (_appendToStderrAndStdout) {
	    System.err.println(text);
	    System.err.flush();
        }
        _appendToBuffer(text);
    }

    /** Append the text message to the StringBuffer.  The output
     *  automatically gets end of line character(s) appended.
     *  Optionally, the text also appears on stdout.
     *  @param text The text to append.
     */
    public void stdout(final String text) {
        if (_appendToStderrAndStdout) {
	    System.out.println(text);
	    System.out.flush();
	}
        _appendToBuffer(text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** Private class that reads a stream in a thread and updates the
     *  the FMUBuilder.
     */
    private static class _StreamReaderThread extends Thread {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a StreamReaderThread.
         *  @param inputStream the stream from which to read.
         *  @param fmuBuilder The FMUBuilder to be written.
         */
        _StreamReaderThread(InputStream inputStream, FMUBuilder fmuBuilder) {
            _inputStream = inputStream;
            _fmuBuilder = fmuBuilder;
        }

        /** Read lines from the _inputStream and output them. */
        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        _inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    _fmuBuilder.stdout( /*_streamType + ">" +*/
                    line);
                }
            } catch (IOException ioe) {
                _fmuBuilder.stderr("IOException: " + ioe);
            }
        }

        /** Stream from which to read. */
        private InputStream _inputStream;

        /** FMUBuilder which is written. */
        private FMUBuilder _fmuBuilder;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Append to the internal StringBuffer.
     *  @param text The text to append.  If the text does not
     *  end with an end of line character(s), then _eol is appended.
     */
    private void _appendToBuffer(final String text) {
        buffer.append(text);
        if (!text.endsWith(_eol)) {
            buffer.append(_eol);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** If true, append to stderr and stdout as the commands are executed.
     */
    private boolean _appendToStderrAndStdout = false;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    private static String _eol;
    static {
        _eol = System.getProperty("line.separator");
    }
}
