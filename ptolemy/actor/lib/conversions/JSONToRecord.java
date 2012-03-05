package ptolemy.actor.lib.conversions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dde.kernel.NullToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/* Converts a string containing JSON-formatted name/value pairs to a record.

 Copyright (c) 2000-2010 The Regents of the University of California.
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

/**
 An actor that converts a string containing JSON-formatted name/value pairs to 
 a record.  If the input string contains nested JSON objects, then a record
 containing nested records will be produced.  Throws an exception if the input
 string is not valid JSON.
 Please see this page for a description of the JSON format:
 http://www.json.org/

 @author  Beth Latronico
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (ltrnc)
 @Pt.AcceptedRating Red (ltrnc)
 */

public class JSONToRecord extends Converter {

    public JSONToRecord(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.RECORD);
        
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
        StringToken inputToken = (StringToken) input.get(0);
        _inputString = inputToken.stringValue();

        RecordToken outputToken = _parseJSON() ;       
        output.send(0, outputToken);
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Extract a RecordToken, which could contain nested RecordTokens,
     * from the given JSON-formatted String of name/value pairs.  
     * Throws an exception if the input string is not valid JSON.
     * 
     * @param inputString  The JSON-formatted string to extract a RecordToken 
     * from
     * @return A RecordToken containing fields and values corresponding to the
     * JSON name and value pairs
     */
    RecordToken _parseJSON() throws IllegalActionException {
        
        // This class directly converts a JSON formatted String to a Ptolemy II
        // RecordToken.  Some conversion notes:
        //
        // There is an org.json.JSONObject class, which might be helpful
        // in the future if we want to print a RecordToken to a web page
        // and manipulate with Javascript.  This would require including the
        // org.json library:
        // http://www.json.org/javadoc/org/json/JSONObject.html
        //
        // The java.util.StringTokenizer class can extract a set of substrings
        // using a set of delimiters, but due to the ability to have nested
        // JSON objects, this did not seem like a good fit to parse the complete
        // string
        
        // Regular expressions are used to check for valid JSON format
        // Strings are trimmed to remove leading and trailing whitespace
        // (removing all whitespace is incorrect since there may be a name
        // or string value that contains a space, for example "My Tag"

        // Copy inputString to workingString so we can use the original 
        // inputString in error messages
        _workingString = new StringBuilder(_inputString.trim());
        ArrayList<String> names = new ArrayList();
        ArrayList<Token> values = new ArrayList();
         
        // String may be encapsulated in []; if so, remove
        // String should have the format { } 
        Pattern pattern = Pattern.compile("\\[.*\\]");
        Matcher matcher = pattern.matcher(_workingString);
        
        // Use delete instead of deleteCharAt, since the latter is inefficient
        // http://stackoverflow.com/questions/5212928/how-to-trim-a-java-stringbuilder
        if (matcher.matches()) {
            _workingString.delete(0,1);
            _workingString
              .delete(_workingString.length() - 1, _workingString.length());
        }
        
        pattern = Pattern.compile("\\{.*\\}");
        matcher = pattern.matcher(_workingString);
        if (!matcher.matches()) {
            throw new IllegalActionException(this, 
               _inputString + " is not a valid JSON string.  Unmatched curly " +
                    		"braces.");
        }
     
        // Parse list of name/value pairs into field/values for a RecordToken
        do {
            if (_workingString.charAt(0) == ',') {
                _workingString.delete(0,1);
            }
            
            // Find next name / value pair
            // Of the form " " : 
            int colonIndex = _workingString.indexOf(":");
            int quoteIndex = _workingString.indexOf("\"");
            
            if (colonIndex == -1 || quoteIndex == -1 
                    || quoteIndex == _workingString.length() - 1) {
                throw new IllegalActionException(this, 
                        _inputString + " is not a valid JSON string. " + 
                            "Improperly formatted name/value pair.");
            }
            
            int secondQuoteIndex = _workingString.indexOf("\"", quoteIndex + 1);
            
            // Name cannot be empty
            if (secondQuoteIndex == -1 || secondQuoteIndex == quoteIndex + 1) {
                throw new IllegalActionException(this, 
                        _inputString + " is not a valid JSON string.  " + 
                            "Empty or improperly formatted name.");
            }
            
            String name = _workingString.substring(quoteIndex + 1, 
                    secondQuoteIndex);
            
            // Create a new token, which is a NullToken by default
            Token value = new NullToken();
            
            // The value is next in the JSON string.  It can be:
            // - Another JSON string, indicated by a curly brace {
            // - An array, indicated by a square brace [
            // - A string, indicated by a double quote "
            // - The boolean literals true or false
            // - The literal null
            // - A number, indicated by a numeric value
            // Please see:  http://www.json.org/
    
            _workingString.delete(0, colonIndex + 1);
            // The String .trim() method to remove whitespace is not defined
            // for StringBuilder 
            // (StringBuilder's .trim() method does something else)
            _workingString = 
                new StringBuilder(_workingString.toString().trim());            
            
            if (_workingString.length() < 1) {
                throw new IllegalActionException(this, 
                        _inputString + " is not a valid JSON string.  " + "" +
                            "Empty or improperly formatted value.");
            }
            
            value = _parseNextValue();
            
            names.add(name);
            values.add(value);
            
            _workingString = 
                new StringBuilder(_workingString.toString().trim()); 
            
            // Check for more fields, indicated by a comma
        }
        while(_workingString.charAt(0) == ',');   

        return new RecordToken(names.toArray(new String[names.size()]), 
                values.toArray(new Token[values.size()]));
    }

    /** Parse the next value and return a Token containing this value.  Update
     * the workingString variable to remove this value.
     * 
     * @return A Token containing the next value.  Note that the next value may
     * a non-scalar type like an ArrayToken or a RecordToken.
     */
    private Token _parseNextValue() throws IllegalActionException{
        Token value = new NullToken();
        
        _workingString = 
            new StringBuilder(_workingString.toString().trim());
                    
        switch (_workingString.charAt(0)) {
        case '{':
            // TODO:  Handle records
            break;
        case '[' :
            // Delete [ 
            _workingString.delete(0, 1);
             
            // Create a new ArrayList to append tokens to
            // ArrayToken's append method requires an ArrayToken argument,
            // so we will instead use the ArrayList to append each element
            // and then create an ArrayToken at the end of the array
            // There is no method in ArrayToken to append a single element
            ArrayList<Token> tokenArray = new ArrayList<Token>();
            tokenArray.add(_parseNextValue());
            
            // If more values, keep adding them to the array
            while (_workingString.charAt(0) == ',') {
                _workingString.delete(0,1);
                tokenArray.add(_parseNextValue());
            }           
            
            // Create a new ArrayToken with all of the tokens
            value = new ArrayToken(tokenArray
                    .toArray(new Token[tokenArray.size()]));
          
            // When at the end of the array, remove ] and return
            _workingString.delete(0, 1);
            _workingString = 
                new StringBuilder(_workingString.toString().trim()); 
            
            break;
        case '\"' :
            
            // String can be any character except " (watch for escaped \")
            int index = 1;
            while (_workingString.length() > index) {
                index = _workingString.indexOf("\"", index);
                // If more characters, and found a non-escaped double quote,
                // extract the string
                if (index == 0 || index == -1) {
                    throw new IllegalActionException(this, 
                            _inputString + " is not a valid JSON string.  " +
                                "Empty or improperly formatted value.");

                }  else if (_workingString.charAt(index - 1) != '\\') {
                   value = 
                       new StringToken(_workingString.substring(1, index));
                   _workingString.delete(0, index + 1);
                   break;
                }
                // Otherwise, keep looking
            }               
            break;
        // Allow case-insensitive true, false and null
        case 't': case 'T':
            if (_workingString.substring(0, 4).equalsIgnoreCase("true")) {
                value = new BooleanToken(true);
                _workingString.delete(0, 4);
            } else {             
                throw new IllegalActionException(this, 
                    _inputString + " is not a valid JSON string.  " +
                        " Improperly formatted value.");
            }
            break;
        case 'f': case 'F':
            if (_workingString.substring(0, 5).equalsIgnoreCase("false")) {
                value = new BooleanToken(false);
                _workingString.delete(0, 5);
            } else {             
                throw new IllegalActionException(this, 
                    _inputString + " is not a valid JSON string.  " +
                        " Improperly formatted value.");
            }
            break;
        case 'n': case 'N':
            if (_workingString.substring(0, 4).equalsIgnoreCase("null")) {
                value = new NullToken();
                _workingString.delete(0, 4);
            } else {             
                throw new IllegalActionException(this, 
                    _inputString + " is not a valid JSON string.  " +
                        " Improperly formatted value.");
            }
            break;
        default:
            // Check for a numeric value; otherwise, throw exception   
            // Numeric values can have digits, a period, more digits, and
            // an exponent.
            Pattern pattern = Pattern.compile("\\d*\\.?\\d*[eE]?\\d*");
            Matcher matcher = pattern.matcher(_workingString);
            
            if (matcher.find()) {
                String numberString 
                    = _workingString.substring(0, matcher.end());
                
                _workingString.delete(0, matcher.end());
                
                // JSON appears to have looser type requirements than Ptolemy II
                // The elements in an array might be of different types
                // (see http://new.openbms.org/backend/api/data/uuid/b5c86c2a-7025-5af5-8c34-6bcc73740592?limit=2
                // which has an integer and a double in one array)
                // To handle this, this class currently treats all numbers
                // as doubles
                // Note that if it's also possible to mix strings and numbers
                // then this class will not handle this situation correctly
                // New features will be needed
                    try {
                        double number = Double.parseDouble(numberString);
                        value = new DoubleToken(number);
                    } catch(NumberFormatException e) {
                        throw new IllegalActionException(this, 
                           _inputString + " is not a valid JSON string.  " +
                                    " Improperly formatted value.");
                        }
            } else {             
                throw new IllegalActionException(this, 
                        _inputString + " is not a valid JSON string.  " +
                            " Improperly formatted value.");
            }
    }
        return value;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**  The original String passed into the input port.  Used to 
     * initialize working String, and used in error messages.
     */
    private String _inputString;
    
    /** A variable holding the remaining part of the input String to be 
     * parsed.
     */
    private StringBuilder _workingString;   
}
