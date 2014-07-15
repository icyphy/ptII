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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import ptolemy.data.RecordToken;

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
        _body = "";
        _method = Method.GET;
        _properties = new RecordToken();
        _timeout = 3000; // Milliseconds
        _url = null;
    }
    
    /** Construct a new HTTP request, parameterized with a URL, method,
     *  properties, body, and time out.
     * 
     *  @param url The URL to send the request to.
     *  @param method The request method (e.g., GET/POST/PUT ...). 
     *   According to the Java documentation, 
     *   "GET POST HEAD OPTIONS PUT DELETE TRACE are legal, subject to protocol 
     *   restrictions."
     *  @param properties
     *  @param body
     *  @param timeout
     */
    public HttpRequest(URL url, Method method, RecordToken properties, 
            String body, int timeout) {
        _body = body;
        _method = method;
        _properties = properties;
        _timeout = timeout; 
        _url = url;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Issue a HTTP request using the specified URL. Open a connection,
     *  send a header, a body if applicable, and wait for a response. 
     *  Return the response, or throw an error if no response is returned
     *  within the specified timeout.
     *  
     *  @throws IOException If an IO error occurs.
     */
    public HttpResponse execute() { 
        // FIXME: decode the response and return a HttpResponse object instead

        OutputStreamWriter writer = null;

        try {
            _connection = (HttpURLConnection) _url.openConnection();
        } catch(IOException e){
            HttpResponse response = new HttpResponse("Error connecting to URL "
                    + _url.toString());
            return response;
        }

        // Set all fields in the request header.
        for (String label : _properties.labelSet()) {
            _connection.setRequestProperty(label, _properties.get(label)
                    .toString());
        }

        // Specify request method (GET, POST, PUT...)
        try {
            _connection.setRequestMethod(_method.toString());
        } catch(IOException e){
            HttpResponse response = new HttpResponse("Error setting request " +
            		"method for URL " + _url.toString());
            return response;
        }
        
        if (_method.toString().equals(Method.GET.toString())) {
            _connection.setDoInput(true);
        } 
        // _connection.setDoOutput() set later if there is a message body

        // If a timeout has been specified, set it.
        if (_timeout >= 0) {
            _connection.setConnectTimeout(_timeout);
            _connection.setReadTimeout(_timeout);
        }

        // Send body if applicable.
        try {
            if (_body != null && !_body.equals("")) {
                _connection.setDoOutput(true);
                writer = new OutputStreamWriter(_connection.getOutputStream());
                writer.write(_body);
                writer.flush();
            }
            
        } catch (SocketTimeoutException e1) {
            HttpResponse response = new HttpResponse();
            response.setTimedOut();
            return response;
        } catch (IOException e2) {
            HttpResponse response = new HttpResponse("Error writing to URL "
                    + _url.toString());
            return response;
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e){
            HttpResponse response = new HttpResponse("Error closing writer for" +
            		" URL " + _url.toString());
            return response;
        }
        
        // Return response
        return new HttpResponse(_connection);
    }
    
    /** Disconnect the current HttpURLConnection
     */
    public void disconnect() {
        if (_connection != null) {
            // FIXME: Does nothing!!!!!!!!!!!!!!
            ((HttpURLConnection) _connection).disconnect();
        }
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

    /** Set the lineBreak character sequence.
     *  @return A String containing the line break sequence.
     */
    public String getLineBreak() {
        return _lineBreak;
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
    
    /** Get the currently set URL.
     *  @return The URL.
     */
    public URL getUrl() {
        return _url;
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

    /** The locally used line break character sequence. */
    private String _lineBreak = System.getProperty("line.separator");
    
    /** A string specifying the request method (GET, PUT, POST, ...). */
    private Method _method;
    
    /** A record token containing properties to be sent as part of the header */
    private RecordToken _properties;
    
    /** The amount of time to wait for a response before throwing an exception. 
     * In milliseconds. */
    private int _timeout;
    
    /** The URL to issue the request to. */
    private URL _url;
  
}
