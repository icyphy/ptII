/* A reference to a simulation request.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

/** Define the response to a simulation execution request.  Once the
 *  simulation has been launched at the request of the user, the ticket will
 *  be used to reference and administer control commands to the simulation
 *  (ex. start, pause, resume, stop, etc).
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
*/
public class Ticket implements java.io.Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare tickets by the ticketID property.
     *  @param object Object to compare this ticket with.
     *  @return True if the two objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null) {
            return false;
        } else if (getClass() != object.getClass()) {
            return false;
        }

        Ticket other = (Ticket) object;
        if (_ticketID == null) {
            if (other._ticketID != null) {
                return false;
            }
        } else if (!_ticketID.equals(other._ticketID)) {
            return false;
        }

        return true;
    }

    /** Generate a new ticket for the provided model and layout URL.
     *  @param modelUrl The path to the model file.
     *  @param layoutUrl The path to the layout file.
     *  @return Ticket corresponding to simulation request.
     */
    public static Ticket generateTicket(String modelUrl, String layoutUrl) {

        Ticket ticket = new Ticket();
        ticket._setTicketID(Long.toHexString(_randomGenerator.nextLong()));
        ticket._setModelUrl(modelUrl);
        ticket._setLayoutUrl(layoutUrl);
        ticket._setDateRequested(new Date());

        return ticket;
    }

    /** Get the date and time of the original request.
     *  @return Date and time that the simulation request was submitted.
     */
    public Date getDateRequested() {
        return (Date) _dateRequested.clone();
    }

    /** Get the URL of the layout file.
     *  @return Path to the layout file.
     */
    public String getLayoutUrl() {
        return _layoutUrl;
    }

    /** Get the URL of the model file.
     *  @return Path to the model file.
     */
    public String getModelUrl() {
        return _modelUrl;
    }

    /** Get the unique ticket identifier.
     *  @return Identifier used to reference the request.
     */
    public String getTicketID() {
        return _ticketID;
    }

    /** Return a hash code value for this ticket.  This method uses the hashcode()
     *  of the ticketID string to compute the hash code for this ticket.
     *  @return A hash code value for this ticket.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + (_ticketID == null ? 0 : _ticketID.hashCode());
        return result;
    }

    /**
     * Return the ticket id.
     * @return the ticket id.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _ticketID;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the date of the simulation request.
     *  @param dateRequested The date and time of the request.
     */
    private void _setDateRequested(Date dateRequested) {
        _dateRequested = dateRequested;
    }

    /** Set the URL of the requested layout.
     *  @param layoutUrl The path to the model file.
     */
    private void _setLayoutUrl(String layoutUrl) {
        _layoutUrl = layoutUrl;
    }

    /** Set the URL of the requested model.
     *  @param modelUrl The path to the model file.
     */
    private void _setModelUrl(String modelUrl) {
        _modelUrl = modelUrl;
    }

    /** Set the unique ticket identifier.
     *  @param ticketID The universally unique identifier.
     */
    private void _setTicketID(String ticketID) {
        _ticketID = ticketID;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A unique identifier of the ticket.
     */
    private String _ticketID;

    /** Path to the model file.
     */
    private String _modelUrl;

    /** Path to the layout file.
     */
    private String _layoutUrl;

    /** Date of the ticket creation.
     */
    private Date _dateRequested;

    /** Random number generator used for the ticket identifier.
     */
    private static transient Random _randomGenerator = new Random();
}
