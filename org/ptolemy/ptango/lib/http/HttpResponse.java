/* An HTTP response.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/** An HTTP response, comprised of selected fields from an HttpURLConnection.
 *  Useful for classes that want response data but should not have authority
 *  to open and close connections.
 *
 *  @see org.ptolemy.ptango.lib.HttpGet
 *  @see org.ptolemy.ptango.lib.HttpPost
 *  @see org.ptolemy.ptango.lib.HttpPut
 *  @author Marten Lohstroh, Elizabeth Latronico
 *  @version $Id: HttpResponse.java 69983 2014-09-04 04:14:47Z beth@berkeley.edu $
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class HttpResponse {

    /** Construct a new, blank HttpResponse object.
     */
    public HttpResponse() {
        _body = "";
        _contentLength = 0;
        _contentType = "text/plain";
        _responseCode = -1;
        _responseMessage = "No response";
    }

    /** Construct a new HttpResponse with the given message. Used for creating
     *  responses when errors occur on the Ptolemy side, such as an IOException.
     *  
     *  @param message The response message
     */
    public HttpResponse(String message) {
        _body = "";
        _contentLength = 0;
        _contentType = "text/plain";
        _responseCode = -1;
        _responseMessage = message;
    }

    /** Construct a new HttpResponse object from an HttpURLConnection object.
     * 
     * @param connection The connection object
     */
    public HttpResponse(HttpURLConnection connection) {
        // FIXME: HttpURLConnection getContentLengthLong() is not
        //present in Java 1.6, which we need to compile under Mac OS X.
        //_contentLength = connection.getContentLengthLong();
        _contentLength = connection.getContentLength();

       _contentType = connection.getContentType();

        try {
            _responseCode = connection.getResponseCode();
            _responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            _responseCode = -1;
            _responseMessage = "Error connecting to server.";
        }

        // Store the response body.
        // Successful requests have data on the connection's input stream.
        // Erroneous requests have data on the connection's error stream.
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        String line = "";
        _body = "";

        try {
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
        } catch (SocketTimeoutException e1) {
            _body = "";
            _responseCode = 408;
            _responseMessage = "Request timed out";
        } catch (IOException e2) {
            // IOException is thrown by connection.getInputStream() in case of
            // erroneous requests.  If so, connection.getErrorStream() has body
            // getErrorStream() does not throw exceptions
            if (connection.getErrorStream() != null) {
                reader = new BufferedReader(new InputStreamReader(
                        connection.getErrorStream()));
            }
        }

        // Read response.
        if (reader != null) {
            try {
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                _body = response.toString();
                reader.close();
            } catch (IOException e) {
                _body = "";
                _responseCode = -1;
                _responseMessage = "Error reading response body.";
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the response body.
     *
     * @return The response body.
     */
    public String getBody() {
        return _body;
    }

    /** Return the length of the response body, in bytes, or -1 if length is
     * unknown.
     *
     * @return  The length of the response body, in bytes, or -1 if length is
     * unknown.
     */
    public long getContentLength() {
        return _contentLength;
    }

    /** Return the Internet media (MIME) type of the response.
     *
     * @return The Internet media (MIME) type of the response.
     */
    public String getContentType() {
        return _contentType;
    }

    /** Return the source of the error, ErrorSource.CLIENT or
     *  ErrorSource.SERVER, or ErrorSource.NONE if no error.
     *
     * @return ErrorSource.CLIENT, ErrorSource.SERVER or Error.NONE.
     */
    public ErrorSource getErrorSource() {
        if (isSuccessful()) {
            return ErrorSource.NONE;
        } else if (_responseCode >= 400 && _responseCode < 500) {
            return ErrorSource.CLIENT;
        } else {
            return ErrorSource.SERVER;
        }
    }

    /** Return a code indicating the status of the response.
     *
     * @return A code indicating the status of the response.
     */
    public int getResponseCode() {
        return _responseCode;
    }

    /** A message describing the status of the response.
     *
     * @return A message describing the status of the response.
     */
    public String getResponseMessage() {
        return _responseMessage;
    }

    /** Return all status items as a RecordToken.
     *
     * @return All status items as a RecordToken
     */
    public RecordToken getStatus() {
        ArrayList<Token> values = new ArrayList(Arrays.asList(new IntToken(
                _responseCode), new StringToken(_responseMessage),
                new BooleanToken(isSuccessful()), new BooleanToken(
                        isFurtherActionExpected())));

        try {
            return new RecordToken(_labels, values.toArray(new Token[values
                                                                     .size()]));
        } catch (IllegalActionException e) {
            return new RecordToken();
        }
    }

    /** Return the Type of the token of getStatus().  Used for setting type
     *  constraints before the response content is available.
     *
     * @return The Type of the token of getStatus()
     */
    public static Type getStatusType() {
        // Define default values here so that a record token can be instantiated
        // in order to return the Type of that token
        ArrayList<Token> values = new ArrayList(Arrays.asList(
                new IntToken(200), new StringToken("OK"),
                new BooleanToken(true), new BooleanToken(false)));

        try {
            return new RecordToken(_labels, values.toArray(new Token[values
                                                                     .size()])).getType();
        } catch (IllegalActionException e) {
            return new RecordToken().getType();
        }
    }

    /** Return true if further action is expected of the client, e.g., issuing
     *  a second request; false otherwise.  Status codes 1xx and 3xx.
     *
     * @return True if further action is expected of the client, e.g., issuing
     *  a second request; false otherwise.  Status codes 1xx and 3xx.
     */
    public boolean isFurtherActionExpected() {
        if ((_responseCode >= 100 && _responseCode < 200)
                || (_responseCode >= 300 && _responseCode < 400)) {
            return true;
        } else {
            return false;
        }
    }

    /** Return true if no errors were encountered; false otherwise.
     *  Status codes 1xx, 2xx, 3xx.
     *
     * @return True if no errors were encountered.  Status codes 1xx, 2xx, 3xx
     */
    public boolean isSuccessful() {
        if (_responseCode >= 100 && _responseCode < 400) {
            return true;
        } else {
            return false;
        }
    }

    /** Create a timed-out response.  Used for creating a response in the event
     * of a send time out.
     */
    public void setTimedOut() {
        _responseCode = 408;
        _responseMessage = "Request timed out";
    }

    /** Return true if the request timed out; false otherwise.  Status code 408.
     *
     * @return True if the request timed out; false otherwise.  Status code 408.
     */
    public boolean timedOut() {
        if (_responseCode == 408) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    /** Enumeration of possible sources of response errors, or none if none.
     *  CLIENT:  Status codes 4xx
     *  SERVER:  Status codes 5xx and -1
     *  NONE:  Any other status code
     */
    public enum ErrorSource {
        // Each of these requires a comment to avoid Javadoc warnings
        /** NONE:  Any other status code. */
        NONE,
        /** CLIENT:  Status codes 4xx. */
        CLIENT, 
        /** SERVER:  Status codes 5xx and -1. */
        SERVER
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The body of the response.  */
    private String _body;

    /** Length of the response body, in bytes.  -1 if length it unknown. */
    private long _contentLength;

    /** The Internet media (MIME) type of the response.
     *  For categories and a list, see:
     *  http://en.wikipedia.org/wiki/MIME_type
     *  For mappings to common file extensions, see:
     *  http://www.sitepoint.com/web-foundations/mime-types-complete-list/
     */
    private String _contentType;

    /** Labels for returning the status as a RecordToken */
    static final String[] _labels = { "responseCode", "responseMessage",
        "successful", "furtherActionExpected" };

    /** A code indicating the status of the response.
     *  See http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
     *  -1  If the response is not valid Http
     *  1xx Informational:  Request received; intermediate response.  Client
     *          typically takes further action.  For example, client obtains
     *          permission to upload a large file.
     *  2xx Success
     *  3xx Redirection.  Client typically takes additional action to complete
     *          the request.
     *  4xx Client Error
     *  5xx Server Error
     *
     */
    private int _responseCode;

    /** A description of the response status.  For example, "OK" for code 200.
     */
    private String _responseMessage;
}
