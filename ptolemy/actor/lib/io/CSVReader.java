/* An actor that outputs strings read from a text file or URL.

 @Copyright (c) 2002-2012 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ptolemy.data.BooleanToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// CSVReader

/**
 <p>
 This actor reads a file or URL, one line at a time, and outputs each line
 except the first as a record. The first line of the file gives
 the names of the fields of the output records.
 The remaining lines give the values of the fields,
 which all have type String. The output is an ordered
 record token, which means that the order defined in the
 first line is preserved.
 </p><p>
 If any line has more values than
 the first line, then the trailing values will be ignored.
 If any line has fewer values than the first line, then the
 field values will be an empty string.
 </p><p>
 By default, the separator between field names and values is a comma,
 so the file format is the standard CSV (comma-separated value) format.
 The <i>separator</i> parameter enables changing the separator to
 tabs or semicolons.
 </p><p>
 The file or URL is specified using any form acceptable
 to FileParameter.
 </p><p>
 Before an end of file is reached, the <i>endOfFile</i>
 output produces <i>false</i>.  In the iteration where the last line
 of the file is read and produced on the <i>output</i> port, this actor
 produces <i>true</i> on the <i>endOfFile</i> port. In that iteration,
 postfire() returns false.  If the actor is iterated again, after the end
 of file, then prefire() and postfire() will both return false, <i>output</i>
 will produce the string "EOF", and <i>endOfFile</i> will produce <i>true</i>.
 </p><p>
 In some domains (such as SDF), returning false in postfire()
 causes the model to cease executing.
 In other domains (such as DE), this causes the director to avoid
 further firings of this actor.  So usually, the actor will not be
 invoked again after the end of file is reached.
 </p><p>
 This actor reads ahead in the file so that it can produce an output
 <i>true</i> on <i>endOfFile</i> in the same iteration where it outputs
 the last line.  It reads the first two lines in preinitialize(), and
 subsequently reads a new line in each invocation of postfire(). The
 data type of the output is also set in preinitialize(), after reading
 the first line, which defines the structure of the record.
 line read is produced on the <i>output</i> in the next iteration
 after it is read.
 </p>

 @see FileParameter
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class CSVReader extends LineReader {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CSVReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        numberOfLinesToSkip.setVisibility(Settable.NONE);

        separator = new StringParameter(this, "separator");
        separator.setExpression("comma");
        separator.addChoice("comma");
        separator.addChoice("tab");
        separator.addChoice("semicolon");

        trimSpaces = new Parameter(this, "trimSpaces");
        trimSpaces.setTypeEquals(BaseType.BOOLEAN);
        trimSpaces.setExpression("true");

        new SingletonParameter(endOfFile, "_showName")
                .setToken(BooleanToken.TRUE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-25\" y=\"-20\" " + "width=\"50\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-15,-10 -12,-10 -8,-14 -1,-14 3,-10"
                + " 15,-10 15,10, -15,10\" " + "style=\"fill:red\"/>\n"
                + "<text x=\"-11\" y=\"4\""
                + "style=\"font-size:11; fill:white; font-family:SansSerif\">"
                + "CSV</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A specification of the separator between items in the table.
     *  The default is "comma", which results in assuming that fields
     *  are separated by commas. If the value is changed to "tab", then
     *  a tab separator will be used. If the value is "semicolon", then
     *  a semicolon separator will be used. If the value is anything
     *  else, then the value of the parameter, whatever it is, will
     *  be the separator.
     */
    public StringParameter separator;

    /** If true, then trim spaces around each field name and value.
     *  This is a boolean that defaults to true. If you change it
     *  to false, then all spaces in the field names and values are
     *  preserved. Note that if there are spaces in the field names,
     *  then the value of the record cannot be read by the
     *  expression evaluator, so spaces in field names are not
     *  recommended.
     */
    public Parameter trimSpaces;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>separator</i> then set a local
     *  variable with the value of the separator.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileOrURL</i> and the file cannot be opened, or the previously
     *   opened file cannot be closed; or if the attribute is
     *   <i>numberOfLinesToSkip</i> and its value is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == separator) {
            _delimiter = separator.stringValue();
            if (_delimiter.equals("comma")) {
                _delimiter = ",";
            } else if (_delimiter.equals("tab")) {
                _delimiter = "\t";
            } else if (_delimiter.equals("semicolon")) {
                _delimiter = ";";
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the data read in the preinitialize() or in the previous
     *  invocation of postfire(), if there is any.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        // Cannot invoke super.fire() because it produces the wrong
        // output.
        // super.fire();

        // Duplicated from the AtomicActor base class:
        if (_debugging) {
            _debug("Called fire()");
        }

        // Duplicated from the Source base class:
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
        if (_currentLine != null) {
            StringTokenizer tokenizer = new StringTokenizer(_currentLine,
                    _delimiter);
            int i = 0;
            StringToken[] fieldValues = new StringToken[_fieldNames.length];
            while (tokenizer.hasMoreTokens()) {
                if (i >= _fieldNames.length) {
                    // Ignore additional fields.
                    break;
                }
                String nextToken = tokenizer.nextToken();
                if (((BooleanToken) trimSpaces.getToken()).booleanValue()) {
                    nextToken = nextToken.trim();
                }
                fieldValues[i] = new StringToken(nextToken);
                i++;
            }
            while (i < _fieldNames.length) {
                fieldValues[i] = new StringToken("");
                i++;
            }
            RecordToken outputValue = new OrderedRecordToken(_fieldNames,
                    fieldValues);
            output.broadcast(outputValue);
        }
        if (_nextLine == null) {
            endOfFile.broadcast(BooleanToken.TRUE);
        } else {
            endOfFile.broadcast(BooleanToken.FALSE);
        }
    }

    /** Read the second line of the file.
     *  If this is called after prefire() has been called but before
     *  wrapup() has been called, then first close any
     *  open file re-open it, and read and discard the first line.
     *  This occurs if this actor is re-initialized during a run of the model.
     *  @exception IllegalActionException If the file or URL cannot be
     *   opened, or if the lines to be skipped and the first line to be
     *   sent out in the fire() method cannot be read.
     */
    public void initialize() throws IllegalActionException {
        // Close and re-open the file, and read the first two lines.
        // They will be in _currentLine and _nextLine, respectively.
        super.initialize();
        if (_reader == null) {
            throw new IllegalActionException(this, "No file to read.");
        }

        // Skip the first line, which only has header information.
        _currentLine = _nextLine;
        try {
            _nextLine = _reader.readLine();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "initialize() failed");
        }
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

        // The following call will read the first line and put
        // its value into the _currentLine variable.
        // It will also read the next line and put it into the
        // _nextLine variable. These two lines will be read again
        // in initialize(), which will close and re-open the file.
        _openAndReadFirstTwoLines();

        if (_currentLine == null) {
            throw new IllegalActionException("File has no data.");
        }
        StringTokenizer tokenizer = new StringTokenizer(_currentLine,
                _delimiter);
        ArrayList<String> fieldNames = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
            String nextName = tokenizer.nextToken();
            if (((BooleanToken) trimSpaces.getToken()).booleanValue()) {
                nextName = nextName.trim();
            }
            fieldNames.add(nextName);
        }
        _fieldNames = new String[1];
        _fieldNames = fieldNames.toArray(_fieldNames);

        Type[] fieldTypes = new Type[_fieldNames.length];
        for (int i = 0; i < _fieldNames.length; i++) {
            fieldTypes[i] = BaseType.STRING;
        }
        RecordType outputType = new RecordType(_fieldNames, fieldTypes);
        output.setTypeEquals(outputType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The string delimiter. */
    private String _delimiter = ",";

    /** Field names for the output record. */
    private String[] _fieldNames;
}
