/* Display an array of records that arrives on the input port.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.database;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.TableIcon;

///////////////////////////////////////////////////////////////////
//// ArrayOfRecordsRecorder

/**
 An actor that displays an array of records that arrives on its input port.
 The records (or a specified subset of the records) are displayed in the icon
 of the actor. In addition, double clicking on the actor displays
 the entire array of records in a table.  Alt-clicking configures
 the actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayOfRecordsRecorder extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayOfRecordsRecorder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(false);

        // Force the type to contain at least a record.
        input.setTypeAtMost(new ArrayType(RecordType.EMPTY_RECORD));

        records = new Parameter(this, "records");
        // Declare that it must be an array of records.
        records.setTypeAtMost(new ArrayType(RecordType.EMPTY_RECORD));
        records.setToken(new ArrayToken(RecordType.EMPTY_RECORD));

        Variable ALL = new Variable(this, "ALL");
        ALL.setVisibility(Settable.NONE);
        Token emptyStringArray = new ArrayToken(BaseType.STRING);
        ALL.setToken(emptyStringArray);

        columns = new Parameter(this, "columns");
        columns.setTypeEquals(new ArrayType(BaseType.STRING));
        columns.setExpression("ALL");

        iconColumns = new Parameter(this, "iconColumns");
        iconColumns.setTypeEquals(new ArrayType(BaseType.STRING));
        iconColumns.setExpression("ALL");

        colorKey = new StringParameter(this, "colorKey");

        // FIXME: This should be in the library, not in the
        // Java code, since it depends on vergil.
        TableIcon icon = new TableIcon(this, "_icon");
        icon.variableName.setExpression("records");
        icon.fields.setExpression("iconColumns");
        icon.colorKey.setExpression("$colorKey");

        // Customize the interaction by inserting this property.
        // This is done in the library,
        // not in the Java code, since it depends on vergil.
        /*
        ArrayOfRecordsConfigureFactory factory = new ArrayOfRecordsConfigureFactory(this, "factory");
        factory.parameterName.setExpression("records");
         */
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A column name to use as a color key. If this string is
     *  non-empty, then it specifies a column name that is used
     *  to determine a color for each row. The value in that
     *  row and column determines the color via a hash function,
     *  so that if two rows are identical in that column, then
     *  they are also identical in color.  This is a string that
     *  defaults to empty, indicating that all rows should
     *  be displayed in black.
     */
    public StringParameter colorKey;

    /** The columns to display when double clicking on the icon.
     *  This is an array of strings specifying the column
     *  names to display, and the order in which they are
     *  displayed. It defaults to ALL, which indicates
     *  that all fields should be displayed. In this case,
     *  the columns are organized alphabetically.
     */
    public Parameter columns;

    /** The columns to display in the icon.
     *  This is an array of strings specifying the column
     *  names to display, and the order in which they are
     *  displayed. It defaults to ALL, which indicates
     *  that all fields should be displayed. In this case,
     *  the columns are organized alphabetically.
     */
    public Parameter iconColumns;

    /** Parameter to store the array of records read at the input.
     *  This is an array of records that is by default empty.
     */
    public Parameter records;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayOfRecordsRecorder newObject = (ArrayOfRecordsRecorder) super
                .clone(workspace);
        newObject.input.setTypeAtMost(new ArrayType(RecordType.EMPTY_RECORD));
        return newObject;
    }

    /** Read the input and update the display.
     *  @exception IllegalActionException If we fail to update the
     *   contents parameter.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken array = (ArrayToken) input.get(0);
            records.setToken(array);
            records.setPersistent(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
}
