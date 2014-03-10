/* A HTTP request.

 @Copyright (c) 1998-2013 The Regents of the University of California.
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
package org.ptolemy.ptango.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import ptolemy.data.RecordToken;
import ptolemy.kernel.util.IllegalActionException;

/** An object representation of an HTTP request. This class combines all
 *  components of a message (method type, header properties, body) and 
 *  combines it with a target URL and a timeout to wait for a response.
 *  After instantiation, the <code>execute()</code> method can be used
 *  (repeatedly) to send requests and wait for a response.
 *  
 *  @see org.ptolemy.ptango.lib.HttpGet
 *  @see org.ptolemy.ptango.lib.HttpPost
 *  @see org.ptolemy.ptango.lib.HttpPut
 *  @author Marten Lohstroh
 *  @version $Id: HttpRequest.java 67693 2013-10-17 15:59:01Z hudson@moog.eecs.berkeley.edu $
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class HttpRequest  {
    
    /** Construct a new, empty HTTP request. 
     */
    public HttpRequest() {
        
    }
    
    /** Construct a new HTTP request, parameterized with a URL, method,
     *  properties, body, and time out.
     * 
     *  @param url The URL to send the request to.
     *  @param method The request method (e.g., GET/POST/PUT)
     *  @param properties
     *  @param body
     *  @param timeout
     */
    public HttpRequest(URL url, Method method, RecordToken properties, String body, int timeout) {
        _url = url;
        _method = method;
        _properties = properties;
        _body = body;
        _timeout = timeout; 
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Issue a HTTP request using the specified URL. Open a connection,
     *  send a header, a body if applicable, and wait for a response. 
     *  Return the response, or throw an error if no response it returned
     *  within the specified timeout.
     *  
     *  @throws IOException 
     *  @exception IllegalActionException If an IO error occurs.
     */
    public String execute() throws IOException { 
        // FIXME: decode the response and return a HttpResponse object instead

        StringBuffer response = new StringBuffer();
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        String line = "";

        _connection = (HttpURLConnection) _url.openConnection();

        // Set all fields in the request header.
        for (String label : _properties.labelSet()) {
            _connection.setRequestProperty(label, _properties.get(label)
                    .toString());
        }

        // Specify request method (GET, POST, PUT...)
        _connection.setRequestMethod(_method.toString());

        // If a timeout has been specified, set it.
        if (_timeout >= 0) {
            _connection.setConnectTimeout(_timeout);
            _connection.setReadTimeout(_timeout);
        }

        // Send body if applicable.
        if (_body != null && !_body.equals("")) {
            _connection.setDoOutput(true);
            writer = new OutputStreamWriter(_connection.getOutputStream());
            writer.write(_body);
            writer.flush();
        }

        // Wait for response.
        reader = new BufferedReader(new InputStreamReader(
                _connection.getInputStream()));

        // Read response.
        while ((line = reader.readLine()) != null) {
            response.append(line);
            if (!line.endsWith(_lineBreak)) {
                response.append(_lineBreak);
            }
        }

        writer.close();
        reader.close();

        // Return response.
        return response.toString();

    }

    /** Get the body of the message. 
     *  @return The body string.
     */
    public String getBody() {
        return _body;
    }

    /** Get the current connection.
     *  @return The current connection.
     */
    public HttpURLConnection getConnection() {
        return _connection;
    }

    /** Get the currently set URL.
     *  @return The URL.
     */
    public URL getUrl() {
        return _url;
    }

    /** Get the request method.
     *  @return The request method.
     */
    public Method getMethod() {
        return _method;
    }

    /** Get the properties that determine the HTTP header. 
     *  @return A record token that maps header fields to strings.
     */
    public RecordToken getProperties() {
        return _properties;
    }

    /** Get the timeout observed upon execution of the request.
     *  @return An integer denoting time in milliseconds.
     */
    public int getTimeout() {
        return _timeout;
    }

    /** Set the lineBreak character sequence.
     *  @return A String containing the line break sequence.
     */
    public String getLineBreak() {
        return _lineBreak;
    }

    /** Set the body of the message.
     *  @param body The body of the message.
     */
    public void setBody(String body) {
        _body = body;
    }
    
    /** Set the current connection.
     *  @param connection A HttpURLConnection object.
     */
    public void setConnection(HttpURLConnection connection) {
        _connection = connection;
    }

    /** Set the lineBreak character sequence.
     *  @param lineBreak A String containing the line break sequence.
     */
    public void setLineBreak(String lineBreak) {
        _lineBreak = lineBreak;
    }
    
    /** Set the request method. 
     *  @param method The request method.
     */
    public void setMethod(Method method) {
        _method = method;
    }

    /** Set the properties that determine the HTTP header. 
     *  @param properties A record token that maps header fields to strings.
     */
    public void setProperties(RecordToken properties) {
        _properties = properties;
    }
    
    /** Set the timeout observed upon execution of the request.
     * @param timeout An integer denoting time in milliseconds.
     */
    public void setTimeout(int timeout) {
        _timeout = timeout;
    }
    
    /** Set the url to send this HTTP request to.
     * @param url The target URL.
     */
    public void setUrl(URL url) {
        _url = url;
    }
    
    /** Enumeration of possible HTTP request methods.
     * @author marten
     *
     */
    public enum Method {
        OPTIONS, GET, HEAD, POST, PUT, 
        DELETE, TRACE, CONNECT 
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URL connection, if it exists. */
    private HttpURLConnection _connection;
    
    /** The body of the HTTP request, if it exists. */
    private String _body;

    /** The URL to issue the request to. */
    private URL _url;
    
    /** A string specifying the request method (GET, PUT, POST, ...). */
    private Method _method;
    
    /* A record token containing properties to be sent as part of the header */
    private RecordToken _properties;
    
    /* The amount of time to wait for a response before throwing an exception. */
    private int _timeout;
    
    /* The locally used line break character sequence. */
    private String _lineBreak = System.getProperty("line.separator");
}
