/* Ticket acts as a reference to a simulation request.
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.control;

import java.util.Date;
import java.util.Random;

///////////////////////////////////////////////////////////////////
//// Ticket

/** 
 * Defines the response to a simulation execution request.  Once the 
 * simulation has been launched at the request of the user, the ticket will 
 * be used to reference and administer control commands to the simulation 
 * (start, pause, resume, stop, etc).
 * 
 * @author jkillian
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
*/
public class Ticket implements java.io.Serializable {

    //////////////////////////////////////////////////////////////////////
    ////                public methods

    /**
     * Generate a new ticket for the provided model URL.
     * @return Ticket corresponding to simulation request
     */
    public static Ticket generateTicket(String url) {

        Ticket ticket = new Ticket();
        ticket.setTicketID(Long.toHexString(new Random().nextLong()));
        ticket.setUrl(url);
        ticket.setDateRequested(new Date());

        return ticket;
    }

    /** 
    * Get the unique ticket identifier.
    * @return Identifier used to reference the request
    */
    public String getTicketID() {
        return this._ticketID;
    }

    /**
     * Get the URL of the model file.
     * @return Path to the model file
     */
    public String getUrl() {
        return this._url;
    }

    /**
     * Get the date and time of the original request.
     * @return Date and time that the simulation request was submitted
     */
    public Date getDateRequested() {
        return this._dateRequested;
    }

    /**
     * Override the equals(x) to compare tickets by the ticketID property
     * @return Equality of the two objects
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        } else if (otherObject == null) {
            return false;
        } else if (!(otherObject instanceof Ticket)) {
            return false;
        }

        Ticket otherTicket = (Ticket) otherObject;
        if ((_ticketID == null) && (otherTicket.getTicketID() != null)) {
            return false;
        } else if (!_ticketID.equals(otherTicket._ticketID)) {
            return false;
        }

        return true;
    }

    /**
     * Override the hashCode() method.
     * @return The computed hashcode value of the object
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + ((_ticketID == null) ? 0 : _ticketID.hashCode());

        return result;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods

    /**
     * Set the unique ticket identifier
     * @param ticketID Universally unique identifier
     */
    private void setTicketID(String ticketID) {
        this._ticketID = ticketID;
    }

    /**
     * Set the URL of the requested model
     * @param url Path to the model file
     */
    private void setUrl(String url) {
        this._url = url;
    }

    /**
     * Set the date of the request
     * @param dateRequested Date and time of the request
     */
    private void setDateRequested(Date dateRequested) {
        this._dateRequested = dateRequested;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables

    private String _ticketID;
    private String _url;
    private Date _dateRequested;
}
