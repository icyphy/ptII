/* Converts a string containing JSON-formatted name/value pairs to a record.

 Copyright (c) 2012 The Regents of the University of California.
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

package ptolemy.actor.lib.conversions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 An actor that converts a string containing JSON-formatted name/value pairs to 
 a record.  If the input string contains nested JSON objects, then a record
 containing nested records will be produced.  Throws an exception if the input
 string is not valid JSON.
 Please see this page for a description of the JSON format:
 http://www.json.org/

 @author  Beth Latronico
 @author  Marten Lohstroh
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (ltrnc)
 @Pt.AcceptedRating Red (ltrnc)
 */
public class JSONToRecord extends Converter {

    /** Construct an actor that converts JSON-formatted name/value
     *  pairs to a record.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JSONToRecord(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        dataSource = new Parameter(this, "dataSource");
        dataSource.setTypeEquals(BaseType.STRING);
        dataSource.setExpression("");
        
        typeSignature = new Parameter(this, "typeSignature");
        typeSignature.setExpression("");
        
        strictTyping = new Parameter(this, "strictTyping");
        strictTyping.setExpression("true");
        strictTyping.setTypeEquals(BaseType.BOOLEAN);
        
        trainingMode = new SharedParameter(this, "trainingMode", getClass(),
                "false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        
        useCachedData = new SharedParameter(this, "useCachedData", getClass(),
                "false");
        useCachedData.setTypeEquals(BaseType.BOOLEAN);
        
        // Set the input type.  The output type is set in preinitialize()
        input.setTypeEquals(BaseType.STRING);  
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Read a JSON-formatted String of name/value pairs from the input 
     *  and produce a record on the output with corresponding fields and 
     *  values.   
     * @exception IllegalActionException If the input String is not 
     *  properly formatted JSON.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Token outputToken;
        boolean strict = ((BooleanToken) strictTyping.getToken())
                .booleanValue();
        
        try {
			outputToken = _parseJSON();
			// Check that the type of this token matches the type
			// assigned to the output port in preinitialize()
			if (strict && !outputToken.getType().equals(output.getType())) {
			    throw new IllegalActionException(this, "JSON data" +
			       " type has changed since model initialization.");
			}
			output.send(0, outputToken);
		} catch (JSONException e) {
            new IllegalActionException("Unable to parse JSON input, no " +
            		"output sent.");
		}   
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
    	_readInput(); // TODO: should it return 0 or throw exception?
        return super.prefire();
    }
    
    /** Set the type for the output port based on the data parameter.  
     *  (In the future the actor could read the data directly from a URL.)
     *  This assumes that future data will have the same type signature. 
     *  This assumption is checked in the fire() method.
     *  @exception IllegalActionException Not thrown here
     */
    public void preinitialize() throws IllegalActionException {
        
        Token value = new RecordToken();
        
        if (((BooleanToken) strictTyping.getToken())
                .booleanValue()) {
            _readInput();
            try {
                value = _parseJSON();
            } catch(JSONException e) {
                // Catch exception and proceed to default case
            	// TODO
            }
        } 
        // Set type to record's type (the empty record's type in default case)
        output.setTypeEquals(value.getType());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    void _readInput() throws IllegalActionException {
    	if (_inputString != null && ((BooleanToken) useCachedData.getToken()).booleanValue())
    		return;
    	
    	String src = dataSource.getToken().toString().replaceAll("\"","").trim();
        BufferedReader in = null;
        String buff = "";
        StringBuilder json = new StringBuilder();
        
        if (src == null || src.equals(""))
        	throw new RuntimeException("Please specify a valid JSON resource for " + this._elementName + ".");
        
        if (src.matches("^[a-zA-Z]+://.*$")) {
           	try {
           		URL url = new URL(src);
           		try {
					in = new BufferedReader(new InputStreamReader(url.openStream()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           		
           	} catch (MalformedURLException e1) {
           		e1.printStackTrace();
           	}
        }
        else {
         	throw new RuntimeException("TODO: Handle local path?");
           	// TODO: not a fully qualified URI, probably a local path, absolute or relative
        }
       		
        if (in == null)
        	throw new RuntimeException("Could not open JSON resource: " + src);
        
        _inputString = "";
        try {
			while ((buff = in.readLine()) != null) {
			  json.append(buff);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        _inputString = json.toString().trim();
    }
    
    Token _parseJSON() throws IllegalActionException, JSONException {
    	if (_inputString.charAt(0) == '{')
    		return _scanJSONObject(new JSONObject(_inputString));
    	else
    		return _scanJSONArray(new JSONArray(_inputString));
    }
    
    /** Map an given value to the appropriate Token class and return the 
     *  new Token.  
     * @param value An Object representing some value
     * @return A Token representing the given value object
     * @throws JSONException 
     * @throws IllegalActionException 
     */
    Token _mapValueToToken(Object value) throws IllegalActionException, 
            JSONException {
    	
		// The value can be any of these types: 
    	// Boolean, Number, String, or the JSONObject.NULL
		if (value instanceof JSONArray)
			return _scanJSONArray((JSONArray)value);
		else if (value instanceof JSONObject)
			return _scanJSONObject((JSONObject)value);
		else {
			Token t;
		    if (value instanceof Boolean)
	            t = new BooleanToken((Boolean)value);
		    else if (value instanceof Integer)
	            t = new IntToken((Integer)value);
		    else if (value instanceof Long)
		    	t = new LongToken((Long)value);
		    else if (value instanceof Double)
		    	t = new DoubleToken((Double)value);
		    else if (value instanceof String)
		    	t = new StringToken((String)value);
		    else if (value.equals(JSONObject.NULL))
		    	t = new ObjectToken(null);
		    else
			    throw new IllegalActionException("Unable to map value of " +
			    		value.getClass().toString() + " to token.");
			return t;

		} 

    }
    
    /** Iterate over the elements inside a JSONArray and put them inside a 
     *  new record token. Apply recursion for JSONObjects and JSONArrays.
     * 
     * @param inputString  The JSON-formatted string to extract a RecordToken 
     * from
     * @return A RecordToken containing fields and values corresponding to the
     * JSON name and value pairs
     * @throws JSONException 
     * @throws IllegalActionException 
     */
    ArrayToken _scanJSONArray(JSONArray array) throws JSONException, 
            IllegalActionException {
    	ArrayList<Token> values = new ArrayList<Token>();
    	
        Object value;
        
    	for (int i = 0; i < array.length(); ++i) {
    		value = array.get(i);
    		values.add(_mapValueToToken(value));
    	}
    	
        return new ArrayToken(values.toArray(new Token[values.size()]));
    }
    
    

    /** Iterate over the elements inside a JSONObject and put them inside a 
     *  new record token. Apply recursion for JSONObjects and JSONArrays.
     * 
     * @param inputString  The JSON-formatted string to extract a RecordToken 
     * from
     * @return A RecordToken containing fields and values corresponding to the
     * JSON name and value pairs
     * @throws JSONException 
     * @throws IllegalActionException 
     */
    RecordToken _scanJSONObject(JSONObject object) throws JSONException, 
            IllegalActionException {
    	ArrayList<String> names = new ArrayList<String>();
        ArrayList<Token> values = new ArrayList<Token>();
        
        Object value;
        String name;
    	Iterator<?> i = object.keys();
    	
        while (i.hasNext()) {
        	name = (String)i.next();
        	value = object.get(name);
    		names.add(name);
    		values.add(_mapValueToToken(value));
        }
    	
        return new RecordToken(names.toArray(new String[names.size()]), 
                values.toArray(new Token[values.size()]));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////
    // TODO: rewrite comments 
    
    public Parameter dataSource;

    /** A parameter containing sample JSON data with the same type 
     *  signature as any data accepted by the input port. Used 
     *  in the preinitialize method to calculate the output type.
     */
    public Parameter typeSignature;
        
    /** If true, then only data that matches the type signature stored in
     *  <i>typeSignature</i> is accepted as input from the data source.
     */
    public Parameter strictTyping;
    
    /** If true, then do not type check inputs, but rather collect them
     *  into the <i>correctTypes</i> array. This parameter is a boolean,
     *  and it defaults to false. It is a shared parameter, meaning
     *  that changing it for any one instance in a model will change
     *  it for all instances in the model.
     */
    public SharedParameter trainingMode;
    
    /** If true, then no new data is read from the input source, but
     *  cached data is used instead. This parameter is a boolean,
     *  and it defaults to false. This parameter is a boolean,
     *  and it defaults to false. It is a shared parameter, meaning
     *  that changing it for any one instance in a model will change
     *  it for all instances in the model. Useful for debugging, to 
     *  speedup model execution, or in case of limited connectivity.
     */
    public SharedParameter useCachedData;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**  The original String passed into the input port.  Used to 
     * initialize working String, and used in error messages.
     */
    private String _inputString;
      
}
