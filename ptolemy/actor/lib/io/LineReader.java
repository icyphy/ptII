/* An actor that outputs strings read from a text file or URL.

@Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import java.io.BufferedReader;
import java.io.IOException;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LineReader
/**
This actor reads a file or URL, one line at a time, and outputs each line
as a string.  The file or URL is specified using any form acceptable
to FileParameter. Before an end of file is reached, the <i>endOfFile</i>
output produces <i>false</i>.  In the iteration where the last line
of the file is read and produced on the <i>output</i> port, this actor
produces <i>true</i> on the <i>endOfFile</i> port. In that iteration,
postfire() returns false.  If the actor is iterated again, after the end
of file, then prefire() and postfire() will both return false, <i>output</i>
will produce the string "EOF", and <i>endOfFile</i> will produce <i>true</i>.
<p>
In some domains (such as SDF), returning false in postfire()
causes the model to cease executing.
In other domains (such as DE), this causes the director to avoid
further firings of this actor.  So usually, the actor will not be
invoked again after the end of file is reached.
<p>
This actor reads ahead in the file so that it can produce an output
<i>true</i> on <i>endOfFile</i> in the same iteration where it outputs
the last line.  It reads the first line in preinitialize(), and
subsequently reads a new line in each invocation of postfire().  The
line read is produced on the <i>output</i> in the next iteration
after it is read.
<p>
This actor can skip some lines at the beginning of the file or URL, with
the number specified by the <i>numberOfLinesToSkip</i> parameter. The
default value of this parameter is 0.
<p>
If you need to reset this line reader to start again at the beginning
of the file, the way to do this is to call initialize() during the run
of the model.  This can be done, for example, using a modal model
with a transition where reset is enabled.

@see FileParameter
@author  Edward A. Lee, Yuhong Xiong
@version $Id$
@since Ptolemy II 2.2
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

        endOfFile = new TypedIOPort(this, "endOfFile", false, true);
        endOfFile.setTypeEquals(BaseType.BOOLEAN);

        fileOrURL = new FileParameter(this, "fileOrURL");

        numberOfLinesToSkip = new Parameter(this, "numberOfLinesToSkip");
        numberOfLinesToSkip.setExpression("0");
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

    /** An output port that produces <i>false</i> until the end of file
     *  is reached, at which point it produces <i>true</i>. The type
     *  is boolean.
     */
    public TypedIOPort endOfFile;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

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
     *  negative, then throw an exception.  In the case of <i>fileOrURL</i>,
     *  do nothing if the file name is the same as the previous value of
     *  this attribute.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileOrURL</i> and the file cannot be opened, or the previously
     *   opened file cannot be closed; or if the attribute is
     *   <i>numberOfLinesToSkip</i> and its value is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            // NOTE: We do not want to close the file if the file
            // has not in fact changed.  We check this by just comparing
            // name, which is not perfect...
            String newFileOrURL = ((StringToken)fileOrURL.getToken()).stringValue();
            if (_previousFileOrURL != null
                    && !newFileOrURL.equals(_previousFileOrURL)) {
                _previousFileOrURL = newFileOrURL;
                fileOrURL.close();
                // Ignore if the fileOrUL is blank.
                if (fileOrURL.getExpression().trim().equals("")) {
                    _reader = null;
                } else {
                    _reader = fileOrURL.openForReading();
                }
                _reachedEOF = false;
            }
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

    /** If this is called after prefire() has been called but before
     *  wrapup() has been called, then close any
     *  open file re-open it, skip the number of lines given by the
     *  <i>numberOfLinesToSkip</i> parameter, and read the first line to
     *  be produced in the next invocation of prefire(). This occurs if
     *  this actor is re-initialized during a run of the model.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_firedSinceWrapup) {
            // It would be better if there were a way to reset the
            // input stream, but apparently there is not, short of
            // closing it and reopening it.
            fileOrURL.close();
            _reader = null;
            _openAndReadFirstLine();
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
                endOfFile.broadcast(BooleanToken.TRUE);
                return false;
            }
            endOfFile.broadcast(BooleanToken.FALSE);
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
        _firedSinceWrapup = true;
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
        _openAndReadFirstLine();
    }

    /** Close the reader if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
        fileOrURL.close();
        _reader = null;
        _firedSinceWrapup = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Cache of most recently read data. */
    protected String _currentLine;

    /** The current reader for the input file. */
    protected BufferedReader _reader;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open the file and read the first line.
     */
    private void _openAndReadFirstLine() throws IllegalActionException {
        _reader = fileOrURL.openForReading();
        _reachedEOF = false;
        try {
            // Read (numberOfLinesToSkip + 1) lines
            int numberOfLines =
                ((IntToken)numberOfLinesToSkip.getToken()).intValue();
            for (int i = 0; i <= numberOfLines; i++) {
                _currentLine = _reader.readLine();
                if (_currentLine == null) {
                    throw new IllegalActionException(this, "The file '"
                            + fileOrURL.stringValue() + "' does not "
                            + "have enough lines.");
                }
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Preinitialize failed.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Indicator that the fire() method has been called, but wrapup
     *  has not.  That is, we are in the middle of a run.
     */
    private boolean _firedSinceWrapup = false;

    /** Previous value of fileOrURL parameter. */
    private String _previousFileOrURL;

    /** Indicator that we have reached the end of file. */
    private boolean _reachedEOF = false;
}
