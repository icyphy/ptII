/* An actor that outputs strings read from a text file or URL.

@Copyright (c) 1998-2002 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// LineReader
/**
This actor reads a file or URL, one line at a time, and outputs each line
as a string.  The file or URL is specified using any form acceptable
to FileAttribute.  If an end of file is reached, then prefire() and
postfire() will both return false.
<p>
This actor can skip some lines at the beginning of the file or URL, with
the number specified by the <i>numberOfLinesToSkip</i> parameter. The
default value of this parameter is 0.

@see FileAttribute
@author  Edward A. Lee, Yuhong Xiong
@version $Id$
*/
public class LineReader extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LineReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.STRING);

        fileOrURL = new FileAttribute(this, "fileOrURL");

        numberOfLinesToSkip = new Parameter(this, "numberOfLinesToSkip",
                                            new IntToken(0));
        numberOfLinesToSkip.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-25\" y=\"-20\" "
                + "width=\"50\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-15,-10 -12,-10 -8,-14 -1,-14 3,-10"
                + " 15,-10 15,10, -15,10\" "
                + "style=\"fill:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileAttribute.
     *  @see FileAttribute
     */
    public FileAttribute fileOrURL;

    /** The number of lines to skip at the beginning of the file or URL.
     *  This parameter contains an IntToken, initially with value 0.
     *  The value of this parameter must be non-negative.
     */
    public Parameter numberOfLinesToSkip; 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fileOrURL</i> and there is an
     *  open file being read, then close that file and open the new one;
     *  if the attribute is <i>numberOfLinesToSkip</i> and its value is
     *  negative, throw an exception.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileOrURL</i> and the file cannot be opened, or the previously
     *   opened file cannot be closed; or if the attribute is
     *   <i>numberOfLinesToSkip</i> and its value is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            fileOrURL.close();
            _reader = fileOrURL.openForReading();
            _reachedEOF = false;
        } else if (attribute == numberOfLinesToSkip) {
            int linesToSkip =
                    ((IntToken)numberOfLinesToSkip.getToken()).intValue();
            if (linesToSkip < 0) {
                throw new IllegalActionException(this, "The number of lines "
                        + "to skip cannot be negative.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        LineReader newObject = (LineReader)super.clone(workspace);
        newObject._currentLine = null;
        newObject._reachedEOF = false;
        newObject._reader = null;
        return newObject;
    }

    /** Output the data read in the preinitialize() or in the previous
     *  invocation of postfire(), if there is any.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_currentLine != null) {
            output.broadcast(new StringToken(_currentLine));
        }
    }

    /** Read the next line from the file. If there is no next line,
     *  return false.  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If there is a problem reading
     *   the file.
     */
    public boolean postfire() throws IllegalActionException {
        if (_reader == null) {
            return false;
        }
        try {
            _currentLine = _reader.readLine();
            if (_currentLine == null) {
                // In case the return value gets ignored by the domain:
                _currentLine = "EOF";
                _reachedEOF = true;
                return false;
            }
            return super.postfire();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "Postfire failed");
        }
    }

    /** Return false if there is no more data available in the file.
     *  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_reachedEOF) return false;
        else return super.prefire();
    }

    /** Open the file or URL, skip the number of lines specified by the
     *  <i>numberOfLinesToSkip</i> parameter, and read the first line to
     *  be sent out in the fire() method.
     *  This is done in preinitialize() so
     *  that derived classes can extract information from the file
     *  that affects information used in type resolution or scheduling.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _reachedEOF = false;
        _reader = fileOrURL.openForReading();
        _reachedEOF = false;
        try {
            // Read (numberOfLinesToSkip + 1) lines
            int numberOfLines =
                    ((IntToken)numberOfLinesToSkip.getToken()).intValue();
            for (int i = 0; i <= numberOfLines; i++) {
                _currentLine = _reader.readLine();
                if (_currentLine == null) {
                    throw new IllegalActionException(this, "The file does not "
                            + "have enough lines.");
                }
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Preinitialize failed.");
        }
    }


    /** Close the reader if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
        fileOrURL.close();
        _reader = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Cache of most recently read data. */
    protected String _currentLine;

    /** The current reader for the input file. */
    protected BufferedReader _reader;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Indicator that we have reached the end of file. */
    private boolean _reachedEOF = false;
}
