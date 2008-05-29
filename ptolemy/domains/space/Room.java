/* Base class for simple source actors.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.domains.space;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Room

/**
 A Room.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Room extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Room(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        building = new StringParameter(this, "building");
        building.setExpression("Cory");
        
        room = new StringParameter(this, "room");
        room.setExpression("545Q");
        
        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");
        
        occupants = new TypedIOPort(this, "occupants", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Name of the building. */
    public StringParameter building;
    
    /** Name of the DatabaseManager to use. 
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;
    
    /** Port through which to access the occupants. */
    public TypedIOPort occupants;

    /** Name of the room. */
    public StringParameter room;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Read the occupants from the database and produce them on the output
     *  port.
     *  @throws IllegalActionException If the database query fails.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ArrayToken result = getOccupants();
        occupants.send(0, result);
    }
    
    /** Return an array of RecordToken, one for each occupant of the room.
     *  @return An array of RecordToken, or null if there is no database update.
     *  @throws IllegalActionException If the database query fails.
     */
    public ArrayToken getOccupants() throws IllegalActionException {
        String databaseName = databaseManager.getValueAsString();
        CompositeActor container = (CompositeActor)getContainer();
        NamedObj database = container.getEntity(databaseName);
        if (!(database instanceof DatabaseManager)) {
            throw new IllegalActionException(this,
                    "Cannot find database manager named " + databaseName);
        }
        // Prepare query.
        // FIXME: This should have more fields and should be parameterized.
        /* Relevant column names:
BLDG            CHAR(10)   
ROOM            VARCHAR2(10)
SPACEID         NUMBER(6)  
DESKNO          NUMBER(3)  
PERSONID        NUMBER(6)  
LNAME           VARCHAR2(25)
FNAMES          VARCHAR2(25)
SPONSORID       NUMBER(6)  
SPONSORLNAME    VARCHAR2(25)
SPONSORFNAMES   VARCHAR2(25)
         */
        String sqlQuery = "select personid, lname, fnames from v_spaces where trim(bldg) = ? and room = ?";
        PreparedStatement statement = null;
        ArrayList<RecordToken> occupants = new ArrayList<RecordToken>();
        /* execute query */
        try {
            Connection connection = ((DatabaseManager)database).getConnection();
            if (connection == null) { 
                return null;
            }
            statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, building.getExpression());  //1 = 1st parameter in sql string
            statement.setString(2, room.getExpression());  //2 = 2nd parameter in sql string
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                HashMap<String,StringToken> map = new HashMap<String,StringToken>();
                String lastName = rset.getString("lname");
                if (lastName == null) {
                    lastName = ""; // FIXME: What does this mean?
                }
                map.put("LastName", new StringToken(lastName));
                String firstName = rset.getString("fnames");
                if (firstName == null) {
                    firstName = ""; // FIXME: What does this mean?
                }
                map.put("FirstName", new StringToken(firstName));
                // FIXME: What is the column for the desk number?
                // String desk = rset.getString("desk");
                String desk = null;
                if (desk == null) {
                    desk = "?"; // FIXME: What does this mean?
                }
                map.put("Desk", new StringToken(desk));
                RecordToken token = new RecordToken(map);
                occupants.add(token);
            }
            /* if updating, you would want to commit here... */
            //conn.commit();
        } catch (SQLException e) {
            /* if updating, you would want to rollback here... */
            //conn.rollback();
            throw new IllegalActionException(this, e,
                    "Failed to update room from database.");
        }
        RecordToken[] array = new RecordToken[occupants.size()];
        int i = 0;
        for (RecordToken recordToken : occupants) {
            array[i++] = recordToken;
        }
        ArrayToken result = new ArrayToken(array);
        return result;
    }
}
