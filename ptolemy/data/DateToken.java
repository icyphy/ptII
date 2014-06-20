/* A token that contains a date.

@Copyright (c) 2008-2013 The Regents of the University of California.
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

package ptolemy.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

/** A token that contains a date.
 * @author Patricia Derler based on DateToken in Kepler
 * @version $Id: DateToken.java 24000 2010-04-28 00:12:36Z berkley $
 */

public class DateToken extends AbstractConvertibleToken {

	/** Construct a date token. The current time is used for the date. 
	 */
	public DateToken() {
		_value = new Date();
	}

	/** Construct a token with a specified java.util.Date.
	 *  @param value The specified java.util.Date type to construct the 
	 *    token with.
	 */
	public DateToken(Date value) {
		_value = value;
	}
	
	public DateToken(long value) {
        _value = new Date(value);
    }
	
	public DateToken(String value) throws IllegalActionException {
	    DateFormat df = DateFormat.getDateInstance();
	    try {
            _value = df.parse(value);
        } catch (ParseException e) {
            throw new IllegalActionException("The date value " + value + " could not be parsed");
        }
	}

	/**
	 * Return the type of this token.
	 * 
	 * @return {@link #DATE}, the least upper bound of all the date types.
	 */
	public Type getType() {
		return BaseType.DATE;
	}

	/**
	 * Return the java.util.Date
	 * 
	 * @return The java.util.Date that this Token was created with.
	 */
	public Date getValue() {
		return _value;
	}

	/**
	 * Return a String representation of the DateToken. The string is surrounded
	 * by double-quotes; without them, the Ptolemy expression parser fails to
	 * parse it.
	 * 
	 * @return A String representation of the DateToken.
	 */
	public String toString() {
		if (_value == null) {
			return null;
		} else {
			return "\"" + _value.toString() + "\"";
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/** The java.util.Date */
	private Date _value;

    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        
        return null;
    }

    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BooleanToken _isEqualTo(Token token)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }
}