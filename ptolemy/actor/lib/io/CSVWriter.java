/* An actor that writes the value of string tokens to a file, one per line.

 @Copyright (c) 2012-2014 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CSVWriter

/**
 <p>This actor reads record-valued input tokens and writes them,
 one line at a time, to a specified file, as comma-separated list
 (or separated by some other delimiter given by the <i>separator</i>
 parameter).
 The first line contains the names of the fields of the input
 record, separated by the same delimiter.
 <p>
 The file is specified by the <i>fileName</i> attribute
 using any form acceptable to {@link FileParameter}.</p>
 <p>
 If the <i>append</i> attribute has value <i>true</i>,
 then the file will be appended to. If it has value <i>false</i>,
 then if the file exists, the user will be queried for permission
 to overwrite, and if granted, the file will be overwritten.</p>
 <p>
 If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
 then this actor will overwrite the specified file if it exists
 without asking.  If <i>true</i> (the default), then if the file
 exists, then this actor will ask for confirmation before overwriting.</p>

 @see FileParameter
 @see ExpressionWriter
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class CSVWriter extends LineWriter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CSVWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        separator = new StringParameter(this, "separator");
        separator.setExpression("comma");
        separator.addChoice("comma");
        separator.addChoice("tab");
        separator.addChoice("semicolon");

        // Clear type constraint set by base class.
        input.setTypeEquals(BaseType.UNKNOWN);
        // Force the input type to be a record.
        input.setTypeAtMost(RecordType.EMPTY_RECORD);

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
    @Override
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
            } else {
                _delimiter = separator.stringValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints on the input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CSVWriter newObject = (CSVWriter) super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.UNKNOWN);
        newObject.input.setTypeAtMost(RecordType.EMPTY_RECORD);
        return newObject;
    }

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _firstFiring = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the specified token to the current writer.
     *  The token argument is required to be a record token.
     *  @param token The token to write.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected void _writeToken(Token token) throws IllegalActionException {
        RecordToken record = (RecordToken) token;
        String eol = "\n";
        Token eolToken = endOfLineCharacter.getToken();
        if (eolToken != null) {
            eol = ((StringToken) eolToken).stringValue();
        }
        if (_firstFiring) {
            // Write the first line, which is determined by the input.

            // Note that we get the labelSet from the record, which
            // may be ordered if this is an OrderedToken.
            // We used to read the RecordType labelSet, which is wrong:
            //RecordType inputType = (RecordType) input.getType();
            //_fieldNames = inputType.labelSet();

            _fieldNames = record.labelSet();

            boolean first = true;
            for (String field : _fieldNames) {
                if (!first) {
                    _writer.print(_delimiter);
                }
                first = false;
                _writer.print(field);
            }
            _writer.print(eol);
            _firstFiring = false;
        }
        boolean first = true;
        for (String field : _fieldNames) {
            if (!first) {
                _writer.print(_delimiter);
            }
            first = false;
            _writer.print(record.get(field));
        }
        _writer.print(eol);

        if (_flushValue) {
            _writer.flush();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The delimiter. */
    private String _delimiter = ",";

    /** Field names determined from input data type. */
    private Set<String> _fieldNames;

    /** Indicator for first firing. */
    private boolean _firstFiring;
}
