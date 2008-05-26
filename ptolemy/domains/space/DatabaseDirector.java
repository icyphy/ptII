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
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;

import oracle.jdbc.OracleDriver;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DatabaseDirector

/**
 A DatabaseDirector. When executed, this director opens a connection
 to the specified database and invokes the fire() method of all actors
 in the order of their creation.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseDirector extends Director {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        database = new StringParameter(this, "database");
        // Default database is the EECS database at Berkeley.
        // NOTE: the server and database name ("acgeecs")
        // are going to change in summer 2008...
        database.setExpression("jdbc:oracle:thin:@buffy.eecs.berkeley.edu:1521:acgeecs");

        userName = new StringParameter(this, "userName");
        userName.setExpression("ptolemy");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    
    /** Database name. */
    public StringParameter database;
    
    /** User name. */
    public StringParameter userName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_connection != null) {
            if (attribute == database || attribute == userName) {
                try {
                    // If updating the database, need to commit or roll back here.
                    _connection.close();
                    _connection = null;
                } catch (SQLException e) {
                    throw new IllegalActionException(this, e,
                            "Failed to close open database connection.");
                }
            }
        }
    }

    /** Get a connection to the database.
     *  If one is already open, then simply return that one.
     *  Otherwise, use the parameter values and prompt for a password to
     *  open a new connection.
     *  @return A connection to the database, or null if none is
     *   successfully created.
     */
    public Connection getConnection() throws IllegalActionException {
        if (_connection != null) {
            return _connection;
        }
        // Open a dialog to get the password.
        // First find a frame to "own" the dialog.
        // Note that if we run in an applet, there may
        // not be an effigy.
        Effigy effigy = Configuration.findEffigy(toplevel());
        JFrame frame = null;
        if (effigy != null) {
            Tableau tableau = effigy.showTableaux();
            if (tableau != null) {
                frame = tableau.getFrame();
            }
        }
        
        // Next construct a query for user name and password.
        Query query = new Query();
        query.setTextWidth(60);
        query.addLine("database", "Database", database.getExpression());
        query.addLine("userName", "User name", userName.getExpression());
        query.addPassword("password", "Password", "");
        ComponentDialog dialog = new ComponentDialog(frame, "Open Connection", query);

        if (dialog.buttonPressed().equals("OK")) {
            // Update the parameter values.
            database.setExpression(query.getStringValue("database"));
            userName.setExpression(query.getStringValue("userName"));
            
            // The password is not stored as a parameter.
            char[] passwordValue = query.getCharArrayValue("password");

            // Get database connection.
            try {
                DriverManager.registerDriver(new OracleDriver());
                _connection = DriverManager.getConnection(
                        database.getExpression(),
                        userName.getExpression(),
                        new String(passwordValue));
                // If updating, use single transaction.
                _connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new IllegalActionException(this, e,
                        "Failed to open connection to the database.");
            }
        }
        return _connection;
    }

    /** Return false, indicating that no more iterations are needed.
     *  @return True to continue execution, and false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        return false;
    }

    /** Close the connection to the database, if it is open.
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it, or if we fail to
     *   close the database connection.
     */
    public void wrapup() throws IllegalActionException {
        try {
            super.wrapup();
        } finally {
            if (_connection != null) {
                try {
                    _connection.close();
                } catch (SQLException e) {
                    throw new IllegalActionException(this, e,
                            "Failed to close database connection");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The currently open connection. */
    private Connection _connection;
}
