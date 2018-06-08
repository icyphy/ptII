/* An actor that writes the value of string tokens to a file, one per line.

 @Copyright (c) 2012-2016 The Regents of the University of California.
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
package ptolemy.domains.tcs.lib;

import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.io.ExpressionWriter;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CSVWriter

/**
 <p>This actor  writes records,
 one line at a time, to a specified file, as tab-separated list.
 The first line contains the names of the fields of the input
 record, separated by the same delimiter.
 <p>
 The file is specified by the <i>fileName</i> attribute
 using any form acceptable to {@link FileParameter}.</p>
 <p>


 @see FileParameter
 @see ExpressionWriter
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class StationWriter extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StationWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileName = new FileParameter(this, "fileName");
        fileName.setExpression("System.out");
    }

    /** The file name to which to write.  This is a string with
     *  any form accepted by FilePortParameter.  The default value is
     *  "System.out".
     *  @see FilePortParameter
     */
    public FileParameter fileName;

    /** If the specified attribute is <i>fileName</i> and there is an
     *  open file being written, then close that file.  The new file will
     *  be opened or created when it is next written to.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileName</i> and the previously
     *   opened file cannot be closed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileName) {
            // Do not close the file if it is the same file.
            String newFileName = ((StringToken) fileName.getToken())
                    .stringValue();

            if (_previousFileName != null
                    && !newFileName.equals(_previousFileName)) {
                _previousFileName = newFileName;
                fileName.close();
                _writer = null;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StationWriter newObject = (StationWriter) super.clone(workspace);
        newObject._writer = null;
        return newObject;
    }

    /** Read the value of alwaysFlush parameter.
     *  @exception IllegalActionException If there is an error reading the
     *  alwaysFlush parameter.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _flushValue = true;
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

    /** Close the writer if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        fileName.close();
        _writer = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the specified token to the current writer.
     *  The token argument is required to be a record token.
     *  @param token The token to write.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _writeToken(Token token) throws IllegalActionException {
        RecordToken record = (RecordToken) token;
        String eol = "\n";
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

    /** Write a token to a file.
     *  @param param The token
     *  @exception IllegalActionException If throw while getting the
     *  file name, while getting the file, while opening the file or
     *  while writing the token.
     */
    protected void _writingToFile(Token param) throws IllegalActionException {
        if (_writer == null) {
            // File has not been opened.
            String fileNameValue = fileName.stringValue();

            // If previousFileName is null, we have never opened a file.
            if (_previousFileName == null) {
                _previousFileName = fileNameValue;
            }
            if (!fileNameValue.equals("System.out")) {
                // Only check for append and overwrite if the
                // fileName is not "System.out"
                // Open the file.
                File file = fileName.asFile();
            }

            _writer = new PrintWriter(fileName.openForWriting(false), true);
            //  _writeToken(_delayToken);
        }
        _writeToken(param);
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The delimiter. */
    private String _delimiter = "\t";

    /** Field names determined from input data type. */
    private Set<String> _fieldNames;

    /** Indicator for first firing. */
    private boolean _firstFiring;

    /** Previous value of fileName parameter. */
    private String _previousFileName;

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** If true, flush the writer after every write. */
    protected boolean _flushValue;

    /** The current writer. */
    protected PrintWriter _writer;

}
