/* Base class for simple source actors.

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

import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DatabaseQuery

/**
 Issue a database query via the specified
 database manager. The output is an array of records, one for each row
 in the returned result.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseQuery extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseQuery(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        query = new PortParameter(this, "query");
        query.setStringMode(true);
        query.setTypeEquals(BaseType.STRING);
        query.setExpression("select * from desks");

        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        // Constrain the output type to be a record type with
        // unspecified fields.
        // NOTE: The output is actually a subtype of this.
        // This is OK because lossless conversion occurs at the
        // output, which (as of 6/19/08) leaves the record unchanged.
        output.setTypeEquals(new ArrayType(BaseType.RECORD));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Name of the DatabaseManager to use.
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;

    /** An SQL query. This is a string that defaults to "select * from
     *  desks", indicating that the all the rows from the desks table
     *  should be returned.
     */
    public PortParameter query;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the query on the database and produce the result
     *  on the output port.
     *  @exception IllegalActionException If the database query fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        query.update();

        String databaseName = databaseManager.stringValue();
        DatabaseManager database = DatabaseManager.findDatabaseManager(
                databaseName, this);
        ArrayToken result = database.executeQuery(((StringToken) query
                .getToken()).stringValue());
        if (result != null) {
            if (_debugging) {
                _debug("Result of query:\n" + result);
            }
            output.send(0, result);
        }
    }
}
