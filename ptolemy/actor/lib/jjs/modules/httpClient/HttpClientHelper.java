/* A JavaScript helper for HttpClient.

@Copyright (c) 2015-2017 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY



 */
package ptolemy.actor.lib.jjs.modules.httpClient;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// HttpClientHelper

/**
   A helper class for the HttpClient module in JavaScript.  You should
   use {@link #getOrCreateHelper(Object, ScriptObjectMirror)} to
   create exactly one instance of this helper per actor. This class
   will then ensure that response and error callbacks will occur in
   the same order as the queries that trigger them.

   To initiate a query, create an instance of the JavaScript
   ClientRequest object and call {@link #request(ScriptObjectMirror, Map)},
   passing it the ClientRequest object and a Map of options
   (see the associated httpRequest.js JavaScript module, which defines
   this class and utility functions for creating it).

   @author Marten Lohstroh, Edward A. Lee, contributors: Ravi Akella, Christopher Brooks.
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
 */
public class HttpClientHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make an HTTP request on behalf of the specified JavaScript object (an
     *  instance of ClientRequest) with the specified options.
     *  The options argument can be a string URL
     *  or a map with the following fields (this helper class assumes
     *  all fields are present, so please be sure they are):
     *  <ul>
     *  <li> body: The request body, if any.  This supports at least strings and image data.
     *  <li> headers: An object containing request headers. By default this
     *       is an empty object. Items may have a value that is an array of values,
     *       for headers with more than one value.
     *  <li> keepAlive: A boolean that specified whether to keep sockets around
     *       in a pool to be used by other requests in the future. This defaults to false.
     *  <li> method: A string specifying the HTTP request method.
     *       This defaults to 'GET', but can also be 'PUT', 'POST', 'DELETE', etc.
     *  <li> outputCompleteResponseOnly: If false, then the multiple invocations of the
     *       callback may be invoked for each request. This defaults to true, in which case
     *       there will be only one invocation of the callback.
     *  <li> proxyHost: The name of the http proxy host, if any.
     *  <li> proxyPort: The port number of the proxy host, if any.  A proxy host
     *       will be used only if proxyHost and proxyPort are not empty.
     *  <li> timeout: The amount of time (in milliseconds) to wait for a response
     *       before triggering a null response and an error. This defaults to 5000.
     *  <li> url: A string that can be parsed as a URL, or an object containing
     *       the following fields:
     *       <ul>
     *       <li> host: A string giving the domain name or IP address of
     *            the server to issue the request to. This defaults to 'localhost'.
     *       <li> path: Request path as a string. This defaults to '/'. This can
     *            include a query string, e.g. '/index.html?page=12', or the query
     *            string can be specified as a separate field (see below).
     *            An exception is thrown if the request path contains illegal characters.
     *       <li> protocol: The protocol. This is a string that defaults to 'http'.
     *       <li> port: Port of remote server. This defaults to 80.
     *       <li> query: A query string to be appended to the path, such as '?page=12'.
     *       </ul>
     *  </ul>
     *  Note that if there are too many pending requests, then this method may stall
     *  for some time before returning. The amount of time will depend on the number of
     *  pending requests.
     *  @param currentObj The JavaScript instance using this helper (a ClientRequest).
     *  @param options The options.
     */
    public void request(ScriptObjectMirror currentObj, Map<String, Object> options) {
        // System.err.println("****** Initiating request " + _sequenceNumber);
        StartHttpRequest request = new StartHttpRequest(currentObj, options, _sequenceNumber++);
        submit(request);
    }

    /** Get or create a helper for the specified actor.
     *  If one has been created before and has not been garbage collected, return
     *  that one. Otherwise, create a new one.
     *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
     *  @param helping JavaScript object this is helping.
     *  @return A new HttpClientHelper instance.
     */
    public static HttpClientHelper getOrCreateHelper(Object actor, ScriptObjectMirror helping) {
        VertxHelperBase helper = VertxHelperBase.getHelper(actor);
        if (helper instanceof HttpClientHelper) {
            return (HttpClientHelper) helper;
        }
        return new HttpClientHelper(actor, helping);
    }

    /** Reset this handler. This method discards any pending submitted jobs,
     *  marks the handler not busy, and resets the sequence number to zero.
     */
    public void reset() {
        super.reset();
        // Execute this in the vert.x event thread.
        submit(new Runnable() {
            public void run() {
                // Ensure that the next execution starts with sequence number 0.
                _sequenceNumber = 0L;
                _pendingRequests = 0;
            }
        });
    }
    
    /** Reset the helper associated with the specified actor.
     *  This method discards any pending submitted jobs,
     *  marks the helper not busy, and resets the sequence number to zero.
     *  If there is no HttpClientHelper associated with this actor, then do nothing.
     *  @param actor the actor with the associated helper to be reset.
     */
    public static void reset(Object actor) {
        VertxHelperBase helper = VertxHelperBase.getHelper(actor);
        if (helper instanceof HttpClientHelper) {
            ((HttpClientHelper)helper).reset();
        }
    }

    /** Stop a request. This ensures that future callbacks are discarded.
     */
    public void stop() {
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public fields                         ////

    /** The threshold of number of pending requests before further
     *  requests introduce a delay.
     */
    public static int PENDING_REQUESTS_THRESHOLD = 20;

    /** The scale factor (in milliseconds) by which requests are
     *  delayed when PENDING_REQUESTS_THRESHOLD is exceeded.
     */
    public static int PENDING_REQUESTS_DELAY_FACTOR = 500;

    ///////////////////////////////////////////////////////////////////
    ////                     protected constructors                ////

    /** Construct a helper for the specified actor.
     *  @param actor The JavaScript actor that this helps.
     *  @param helping A JavaScript object that this helps.
     */
    protected HttpClientHelper(Object actor, ScriptObjectMirror helping) {
        super(actor, helping);
    }

    /** Construct a helper for the specified actor.
     *  @param actor The JavaScript actor that this helps.
     *  @param helping A JavaScript object that this helps.
     *  @param base The helper base to share a verticle with.
     */
    protected HttpClientHelper(Object actor, ScriptObjectMirror helping, VertxHelperBase base) {
        super(actor, helping, base);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The number of pending requests. */
    private int _pendingRequests = 0;

    /** The sequence number of this request. */
    private long _sequenceNumber = 0L;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The event handler that is triggered when an error occurs in the HTTP connection.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        /** The HTTP client for this request. */
        private HttpClient _client;
        /** The number of the request that triggered this response. */
        private long _requestNumber;
        /** The JavaScript object that this is a helper for. */
        protected ScriptObjectMirror _requestObj;

        public HttpClientExceptionHandler(
                ScriptObjectMirror requestObj, HttpClient client, long requestNumber) {
            _client = client;
            _requestObj = requestObj;
            _requestNumber = requestNumber;
        }
        @Override
        public void handle(final Throwable throwable) {
            _pendingRequests--;
            String message = "";
            // FIXME: When we report an error, we should include the exception.
            // For this to work, we need to have host-specific error processing.

            // If we get a SSLHandshakeException, suggest upgrading the JVM.
            // $PTII/ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTGetCompleteResponseOnly.xml 
            // was failing with JDK 1.8.0_91 because https://httpbin.org 
            // uses a lets encrypt certificate.
            if (throwable instanceof javax.net.ssl.SSLHandshakeException) {
                message = "HttpClientHelper.java: "
                + "The exceptions is a SSLHandshakeException, which is "
                + "probably caused because the certs for the website in question "
                + "are not in the JVM keystore.  "
                + "Try updating your JVM or see "
                + "See https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates/35454903#35454903: ";
            }
            final String finalMessage = message;
            // System.err.println("****** Received an error for request " + _requestNumber + ": " + throwable);
            // throwable.printStackTrace();
            // True argument indicates that this request is done.
            _issueOrDeferResponse(_requestNumber, true, new Runnable() {
                public void run() {
                    _requestObj.callMember("_errorResponse", null, finalMessage + throwable.getMessage());
                }
            });
            if (_client != null) {
                _client.close();
                _client = null;
            }
        }
    }

    /** The event handler that is triggered when a response arrives from the server.
     *  Notice that this response does not include the body of the retrieved data.
     *  We need to register a handler with the response to handle the body.
     */
    private class HttpClientResponseHandler implements
    Handler<HttpClientResponse> {
        private List<byte[]> _imageParts;
        private String _boundary;
        private boolean _inSegment;
        /** The HTTP client for this request. */
        private HttpClient _client;
        /** Boolean indicating whether outputting partial responses is permitted. */
        private boolean _outputCompleteResponseOnly = true;
        /** The number of the request that triggered this response. */
        private long _requestNumber;
        /** The JavaScript object that this is a helper for. */
        protected ScriptObjectMirror _requestObj;

        public HttpClientResponseHandler(
                ScriptObjectMirror requestObj,
                HttpClient client,
                boolean outputCompleteResponseOnly,
                long requestNumber) {
            _client = client;
            _outputCompleteResponseOnly = outputCompleteResponseOnly;
            _requestObj = requestObj;
            _requestNumber = requestNumber;
        }
        @Override
        public void handle(final HttpClientResponse response) {
            // The response is not yet complete, but we have some information.
            int status = response.statusCode();
            if (status >= 400) {
                _pendingRequests--;
                // An error occurred.
                response.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer body) {
                      // The entire response body has been received
                      System.out.println("The total body: " + body.toString());
                    }
                  });
                
                // True argument indicates that this request is done.
                // System.err.println("****** Received an error code for request " + _requestNumber + ", " + status);
                _issueOrDeferResponse(_requestNumber, true, new Runnable() {
                    public void run() {
                        // Null argument indicates error.
                        _requestObj.callMember("_errorResponse", response,
                                "Request failed with code " + status + ": "
                                        + response.statusMessage());
                    }
                });
                _client.close();
                _client = null;
                return;
            }
            MultiMap headers = response.headers();

            // If the response is a redirect, handle that here.

            // We used to handle redirects here, instead redirects
            // should be handled by the caller, which is how Vert.x
            // does it.  Vert.x HttpClient does not handle redirects,
            // instead WebClient does.  See
            // https://groups.google.com/forum/#!msg/vertx-dev/LzFm1YXUYMY/7qB6OSIXCAAJ
            
            // For how Vert.x handles redirects, see
            // https://github.com/eclipse/vert.x/blob/master/src/main/java/io/vertx/core/http/impl/HttpClientRequestImpl.java

            // For how we handle redirects in Ptolemy, see
            // FileUtilities.followRedirects() and
            // FileUtilities.openStreamFollowingRedirectsReturningBoth()
            
            if (status >= 300 && status <= 308 && status != 306) {
                _pendingRequests--;
                String newLocation = headers.get("Location");
                if (newLocation != null) {
                    // True argument indicates that this request is done.
                    _issueOrDeferResponse(_requestNumber, true, new Runnable() {
                        public void run() {
                            _requestObj.callMember("_response", response, "<p>HttpClientHelper.java redirect body.</p>");

                            // Here is the old code that would just emit an error if we had a redirect:
                            // _requestObj.callMember("_errorResponse", response,
                            //         "Redirect to "
                            //                 + newLocation
                            //                 + " not yet handled by HttpClientHelper. "
                            //                 + status + ": " + response.statusMessage());
                        }
                    });
                    _client.close();
                    _client = null;
                    return;
                }
            }

            String contentType = headers.get("Content-Type");
            final boolean isText = (contentType == null)
                    || (contentType.startsWith("text"))
                    || (contentType.startsWith("application/javascript"))
                    || (contentType.startsWith("application/json"));
            final boolean isMultipart = (contentType != null)
                    && (contentType.startsWith("multipart"));
            if (isMultipart) {
                int index = contentType.indexOf("=");
                if (index > 0) {
                    _boundary = "--"
                            + contentType.substring(index + 1).trim();
                }
            }

            // FIXME: The Content-Type might be something like
            // multipart/x-mixed-replace;boundary=ipcamera, in which case,
            // we really need to be chunking the data rather than using a
            // bodyHandler.

            if (_outputCompleteResponseOnly) {
                // A bodyHandler is invoked after the response is complete.
                // Note that large responses could create a memory problem here.
                // Could look at the Content-Length header.
                response.bodyHandler(new Handler<Buffer>() {
                    public void handle(final Buffer body) {
                        _pendingRequests--;
                        // System.err.println("****** Received complete response for request " + _requestNumber);
                        // True argument indicates that this request is done.
                        _issueOrDeferResponse(_requestNumber, true, new Runnable() {
                            public void run() {
                                if (isText) {
                                    _requestObj.callMember("_response",
                                            response, body.toString());
                                } else if (contentType.startsWith("image")) {
                                    InputStream stream = new ByteArrayInputStream(
                                            body.getBytes());
                                    try {
                                        Image image = ImageIO.read(stream);
                                        Token token = new AWTImageToken(image);
                                        _requestObj.callMember("_response",
                                                response, token);
                                    } catch (IOException e) {
                                        // FIXME: What to do here?
                                        _requestObj.callMember("_response",
                                                response, body.getBytes());
                                    }
                                } else {
                                    // FIXME: Need to handle other MIME types.
                                    _requestObj.callMember("_response",
                                            response, body.getBytes());
                                }
                            }
                        });
                        _client.close();
                        _client = null;
                    }
                });
            } else {
                response.endHandler(new Handler<Void>() {
                    public void handle(Void v) {
                        _pendingRequests--;
                        // System.err.println("****** Received end of response for request " + _requestNumber);
                        // True argument indicates that this request is done.
                        _issueOrDeferResponse(_requestNumber, true, new Runnable() {
                            public void run() {
                            }
                        });
                        _client.close();
                        _client = null;
                    }
                });
                _imageParts = new LinkedList<byte[]>();
                _inSegment = false;
                response.handler(new Handler<Buffer>() {
                    public void handle(Buffer body) {
                        // System.err.println("****** Received body response for request " + _requestNumber);
                        // False argument indicates that this request is NOT done.
                        _issueOrDeferResponse(_requestNumber, false, new Runnable() {
                            public void run() {
                                if (isText) {
                                    _requestObj.callMember("_response",
                                            response, body.toString());
                                } else if (isMultipart) {
                                    _handleMultipartResponse(response, body);
                                } else {
                                    // FIXME: Need to handle other MIME types.
                                    _requestObj.callMember("_response",
                                            response, body.getBytes());
                                }
                            }
                        });
                        // FIXME: Probably shouldn't close here?
                        // _client.close();
                        // _client = null;
                    }
                });
            }
        }
        private void _handleMultipartResponse(
                final HttpClientResponse response, Buffer body) {
            byte[] data = body.getBytes();
            String dataAsString = body.toString();
            int boundaryIndex = dataAsString
                    .indexOf(_boundary);
            if (boundaryIndex >= 0) {
                // The data contains a boundary.  If we are in
                // a segment, finish it.
                if (_inSegment) {
                    if (boundaryIndex > 1) {
                        // There is additional data in this segment.
                        byte[] prefix = new byte[boundaryIndex];
                        System.arraycopy(data, 0,
                                prefix, 0,
                                boundaryIndex - 1);
                        _imageParts.add(prefix);

                        // Construct one big byte array.
                        int length = 0;
                        for (byte[] piece : _imageParts) {
                            length += piece.length;
                        }
                        byte[] imageBytes = new byte[length];
                        int position = 0;
                        for (byte[] piece : _imageParts) {
                            System.arraycopy(piece, 0,
                                    imageBytes,
                                    position,
                                    piece.length);
                            position += piece.length;
                        }

                        // Have a complete image.
                        InputStream stream = new ByteArrayInputStream(
                                imageBytes);
                        try {
                            Image image = ImageIO
                                    .read(stream);
                            if (image != null) {
                                Token token = new AWTImageToken(
                                        image);
                                _requestObj
                                .callMember(
                                        "_response",
                                        response,
                                        token);
                                System.out
                                .println("Sent an image.");
                            } else {
                                System.err
                                .println("Input data is apparently not an image.");
                            }
                        } catch (IOException e) {
                            _requestObj.callMember(
                                    "_errorResponse",
                                    response,
                                    e.toString());
                        }
                    }
                }
                // Since there is a boundary string, we are definitely in the segment.
                _inSegment = true;
                _imageParts.clear();
                // Skip to character 255, the start of a jpeg image.
                // (in signed notation, -1).
                int start = boundaryIndex
                        + _boundary.length() + 2;
                while (start < data.length
                        && data[start] != -1) {
                    start++;
                }
                if (start < data.length) {
                    byte[] segment = new byte[data.length
                                              - start];
                    System.arraycopy(data, start,
                            segment, 0, segment.length);
                    _imageParts.add(segment);
                }
            } else {
                // data does not contain a boundary.
                if (_inSegment) {
                    _imageParts.add(data);
                }
            }
        }
    }

    /** Job to start an HTTP request.
     */
    private class StartHttpRequest implements Runnable {
        /** The options specified when creating this object. */
        private Map<String, Object> _options;
        /** The number of this request. */
        private long _requestNumber;
        /** The JavaScript object that this is a helper for. */
        protected ScriptObjectMirror _requestObj;

        public StartHttpRequest(
                ScriptObjectMirror requestObj,
                Map<String, Object> options,
                long sequenceNumber) {
            _options = options;
            _requestObj = requestObj;
            _requestNumber = sequenceNumber;
            _pendingRequests++;
            
            // If there are too many pending requests, stall the
            // calling thread. Do that here to not block vertx.
            // Note unsynchronized access to _pendingRequests, so time
            // may end up being negative.
            if (_pendingRequests > PENDING_REQUESTS_THRESHOLD) {
                long time = (_pendingRequests - PENDING_REQUESTS_THRESHOLD)
                        * PENDING_REQUESTS_DELAY_FACTOR;
                if (time > 0) {
                    try {
                        _actor.log("WARNING: More than " + PENDING_REQUESTS_THRESHOLD
                                + " pending requests. Sleeping " + time + " ms.");
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        // Ignore and proceed.
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            Map<String, Object> urlSpec = (Map<String, Object>) _options.get("url");

            HttpClient client = null;
            boolean useProxy = false;

            Object proxyHostObject = _options.get("proxyHost");
            Object proxyPortObject = _options.get("proxyPort");
            if (proxyHostObject != null && proxyPortObject != null) {
                useProxy = true;
                String proxyHostSpec = proxyHostObject.toString().trim();
                int proxyPortSpec = (int)proxyPortObject;
                client = _vertx.createHttpClient(
                    new HttpClientOptions()
                    .setKeepAlive((boolean) _options.get("keepAlive"))
                    // NOTE: We use the timeout parameter both for connect and response.
                    // Should these be different numbers?
                    .setConnectTimeout((Integer)_options.get("timeout"))
                    .setSsl(urlSpec.get("protocol").toString().equalsIgnoreCase("https"))
                    .setProxyOptions(new ProxyOptions()
                                     .setType(ProxyType.HTTP)
                                     .setHost(proxyHostSpec)
                                     .setPort(proxyPortSpec)));
            } else { 
                client = _vertx.createHttpClient(
                    new HttpClientOptions()
                    .setDefaultHost((String) urlSpec.get("host"))
                    .setDefaultPort((int) urlSpec.get("port"))
                    .setKeepAlive((boolean) _options.get("keepAlive"))
                    // NOTE: We use the timeout parameter both for connect and response.
                    // Should these be different numbers?
                    .setConnectTimeout((Integer)_options.get("timeout"))
                    .setSsl(urlSpec.get("protocol").toString().equalsIgnoreCase("https"))
                    );
            }

            String query = "";
            Object queryObject = urlSpec.get("query");
            if (queryObject != null) {
                String querySpec = queryObject.toString().trim();
                if (!querySpec.equals("") && !querySpec.startsWith("?")) {
                    query = "?" + querySpec;
                }
            }

            // The argument is a path with a query, not a URL.
            String uri = urlSpec.get("path") + query;

            Object complete = _options.get("outputCompleteResponseOnly");
            boolean outputCompleteResponseOnly = true;
            if (complete instanceof Boolean && !(Boolean) complete) {
                outputCompleteResponseOnly = false;
            }

            // Set the method (GET, PUT, POST, etc.)
            HttpMethod httpMethod = HttpMethod.valueOf(((String) _options.get("method")).trim().toUpperCase());

            HttpClientRequest request = null;
            if (useProxy) {
                request = client.requestAbs(httpMethod,
                                            (String) urlSpec.get("protocol") +
                                            "://" + (String) urlSpec.get("host") + uri,
                                            new HttpClientResponseHandler(_requestObj,
                                                                          client,
                                                                          outputCompleteResponseOnly,
                                                                          _requestNumber));
            } else {
                request = client.request(httpMethod, uri,
                                         new HttpClientResponseHandler(_requestObj,
                                                                       client,
                                                                       outputCompleteResponseOnly,
                                                                       _requestNumber));
            }

            // NOTE: We use the timeout parameter both for connect and response.
            // Should these be different numbers?
            request.setTimeout((Integer)_options.get("timeout"));
            request.exceptionHandler(new HttpClientExceptionHandler(
                    _requestObj, client, _requestNumber));

            // Handle the headers.
            Map<String,Object> headers = (Map<String,Object>) _options.get("headers");
            boolean isImage = false;
            String imageType = "";
            if (!headers.isEmpty()) {
                for (Entry<String, Object> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key.equalsIgnoreCase("Content-Type") &&
                            ( (String)value).startsWith("image")) {
                        isImage = true;
                        imageType = ((String) value).substring(6);
                        
                        if ( !imageType.equalsIgnoreCase("gif") && 
                        	 !imageType.equalsIgnoreCase("jpg") && 
                        	 !imageType.equalsIgnoreCase("jpeg") && 
                        	 !imageType.equalsIgnoreCase("png")) {
                        	_error(_requestObj, "Unsupported image type " + imageType + ". Attempting to send as jpg."
                        			+ " Supported types are gif, jpg and png.");
                        	imageType = "jpg";
                        }
                    }
                    if (value instanceof String) {
                        request.putHeader((String) key, (String) value);
                    } else if (value instanceof Integer) {
                        request.putHeader((String) key,
                                ((Integer) value).toString());
                    } else if (value instanceof Iterable) {
                        request.putHeader((String) key, (Iterable<String>) value);
                    }
                }
            }

            // Handle the body, if present.  Images are sent chunked to avoid a TooLargeFrameException on the server.
            // Format any images
            if (isImage) {
                AWTImageToken token = (AWTImageToken) _options.get("body");
                
                Image image = token.getValue();
                BufferedImage bufferedImage;

                // If image is not a BufferedImage, create a BufferedImage.  See:
                // http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
                if (image instanceof BufferedImage)
                {
                    bufferedImage = (BufferedImage) image;
                } else {
                    // Create a buffered image with transparency
                    bufferedImage = new BufferedImage(image.getWidth(null),
                            image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                    // Draw the image on to the buffered image
                    Graphics2D bGr = bufferedImage.createGraphics();
                    bGr.drawImage(image, 0, 0, null);
                    bGr.dispose();
                }
                
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                
                try {
                    // This is tested by
                    // $PTII/ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTSendImage.xml 
                    ImageIO.write(bufferedImage, imageType , os);
                    os.close();  // Important, flushes the output buffer.
                    
                    // Chunked requests don't use a Content-Length header.
                    byte[] bytes = os.toByteArray();
                    request.sendHead();
                    request.setChunked(true);
                    
                    // Encode the entire image first and then call request on 4000
                    // character junks.  If we encode each 4000 character chunk,
                    // then we get padding character(s) (=) in the output.
                    String encoded = Base64.getEncoder().encodeToString(bytes);
                    for (int i = 0; i < encoded.length(); i = i + 4000){
                        int end = i + 4000 < encoded.length()
                            ? i + 4000
                            : encoded.length();
                     	request.write(Buffer.buffer(encoded.substring(i, end)));
                    }

                    // for (int i = 0; i < bytes.length; i = i + 4000){
                    // 	request.write(Buffer.buffer(Base64.getEncoder().encodeToString(Arrays.copyOfRange(bytes, i, i + 4000))));
                    // }
                    
                } catch (final IOException ioe) {
                	String message = "Can't write image to HTTP request.  Unable to convert image to base-64 string.";
                	 _error(_requestObj, message);
                }
            }  else {
                // Otherwise, send body as string
                Object bodyObject = _options.get("body");
                if (bodyObject != null) {
                    String body =  bodyObject.toString();
                    if (body != null) {
                        request.putHeader("Content-Length", Integer.toString(body.length()));
                        request.write(body);
                    }
                }
            }
            
            // Allow overlapped requests. Sequence numbers take care of ensuring outputs
            // come out in order.
            // _setBusy(true);

            // FIXME: The following doesn't allow further writes to the request.
            request.end();
        }
    }
}
