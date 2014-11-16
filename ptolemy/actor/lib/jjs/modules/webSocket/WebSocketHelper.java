/* AnExecute a script in JavaScript.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.webSocket;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;

import ptolemy.actor.TypedAtomicActor;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   FIXME
   
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper extends TypedAtomicActor {
    /** FIXME
     * @throws ScriptException 
     * @throws NoSuchMethodException 
     */
    public WebSocketHelper(ScriptEngine engine, String url, Object callbacks) throws NoSuchMethodException, ScriptException {
	HttpClient client = _vertx.createHttpClient();
	if (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
	    url = url.substring(0, url.length() - 1);
	}
	int sep = url.lastIndexOf(':');
	// client.setPort(Integer.parseInt(url.substring(sep + 1)));
	
	Object[] args = new Object[0];
	((Invocable) engine).invokeMethod(callbacks, "onOpen", args);
	
	/*
	client.connectWebsocket(url, new Handler<WebSocket>() {
	    @Override
	    public void handle(WebSocket websocket) {
		_wsIsOpen = true;
		if (_wsOnOpen != null) {
		    
		    // _wsOnOpen.call(_context, _scope, _global, null);
		}
		_webSocket = websocket;
		_webSocket.dataHandler(new Handler<Buffer>() {
		    @Override
		    public void handle(Buffer buff) {
			if (_wsOnMessage != null) {
			    byte[] bytes = buff.getBytes();
			    Integer[] objBytes = new Integer[bytes.length];
			    for (int i = 0; i < bytes.length; i++) {
				objBytes[i] = (int)bytes[i];
			    }
			    Object[] arg = new Object[1];
			    arg[0] = objBytes;
			    // _wsOnMessage.call(_context, _scope, _global, arg);
			}
		    }
		});
		_webSocket.endHandler(new VoidHandler() {
		    @Override
		    protected void handle() {
			_wsIsOpen = false;
			if (_wsOnClose != null) {
			    // _wsOnClose.call(_context, _scope, _global, null);
			}
		    }
		});
		_webSocket.exceptionHandler(new Handler<Throwable>() {
		    @Override
		    public void handle(Throwable arg0) {
			if (_wsOnError != null) {
			    // _wsOnError.call(_context, _scope, _global, null);
			}
		    }
		});
	    }
	});
	*/
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    public synchronized void close() {
	if (_webSocket != null) {
	    _webSocket.close();
	}
    }

    public static WebSocketHelper open(ScriptEngine engine, String url, Object callbacks)
	    throws NoSuchMethodException, ScriptException {
	return new WebSocketHelper(engine, url, callbacks);
    }
	
    public boolean wsIsOpen() {
	if (_webSocket == null) {
	    return false;
	}
	return _wsIsOpen;
    }
    /* FIXME: Stuff left from Rhino implementation.
    public void wsOnClose(Function function) {
	_wsOnClose = function;
    }
    public void wsOnError(Function function) {
	_wsOnError = function;
    }
    public void wsOnMessage(Function function) {
	_wsOnMessage = function;
    }
    public void wsOnOpen(Function function) {
	_wsOnOpen = function;
    }
    public void wsSendBytes(Object msg) {
	NativeArray msgArray = (NativeArray)msg;
	byte[] byteMsg = new byte[(int)msgArray.getLength()];
	for (Object o: msgArray.getIds()) {
	    int index = (Integer) o;
	    Object obj = msgArray.get(index, null);
	    if (obj.getClass() == Double.class) {
		Double oneByte = (double)msgArray.get(index, null);
		byteMsg[index] = (byte)oneByte.doubleValue();
	    }
	    else if (obj.getClass() == Integer.class) {
		Integer oneByte = (int)msgArray.get(index, null);
		byteMsg[index] = (byte)oneByte.doubleValue();
	    }
	    // byteMsg[index]
	}
	Buffer buffer = new Buffer(byteMsg);
	_webSocket.writeBinaryFrame(buffer);
	/// Following is commented out.
            Array a;
            byte[] bytes = new byte[oBytes.length];
            for(int i = 0; i < oBytes.length; i++){
                bytes[i] = oBytes[i];
            }
            Buffer buffer = new Buffer(bytes);
            _webSocket.writeBinaryFrame(buffer);
	 ///
    }
*/
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();

    private WebSocket _webSocket = null;

    private boolean _wsIsOpen = false;

    /*
    private Function _wsOnClose = null;

    private Function _wsOnError = null;

    private Function _wsOnMessage = null;

    private Function _wsOnOpen = null;
    */
}
