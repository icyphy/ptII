/*
 * @(#)FloatingDecimal.java	1.14 98/07/07
 *
 * Copyright 1996-2000 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package ptolemy.kvm.plot;
/** FIXME: PtFloatingDecimal is a copy of part of the Sun source code.
 */
class PtFloatingDecimal{
    boolean	isExceptional;
    boolean	isNegative;
    int		decExponent;
    char	digits[];
    int		nDigits;

    private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0', '0' };

    static final int	bigDecimalExponent = 324; // i.e. abs(minDecimalExponent)

    private	PtFloatingDecimal( boolean negSign, int decExponent, char []digits, int n,  boolean e )
    {
	isNegative = negSign;
	isExceptional = e;
	this.decExponent = decExponent;
	this.digits = digits;
	this.nDigits = n;
    }


    public static PtFloatingDecimal
    readJavaFormatString( String in ) throws NumberFormatException {
	boolean isNegative = false;
	boolean signSeen   = false;
	int     decExp;
	char	c;

    parseNumber:
	try{
	    //in = in.trim(); // don't fool around with white space.
			   // throws NullPointerException if null
	    int	l = in.length();
	    if ( l == 0 ) throw new NumberFormatException("empty String");
	    int	i = 0;
	    switch ( c = in.charAt( i ) ){
	    case '-':
		isNegative = true;
		//FALLTHROUGH
	    case '+':
		i++;
		signSeen = true;
	    }
	    // Would handle NaN and Infinity here, but it isn't
	    // part of the spec!
	    //
	    char[] digits = new char[ l ];
	    int    nDigits= 0;
	    boolean decSeen = false;
	    int	decPt = 0;
	    int	nLeadZero = 0;
	    int	nTrailZero= 0;
	digitLoop:
	    while ( i < l ){
		switch ( c = in.charAt( i ) ){
		case '0':
		    if ( nDigits > 0 ){
			nTrailZero += 1;
		    } else {
			nLeadZero += 1;
		    }
		    break; // out of switch.
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		    while ( nTrailZero > 0 ){
			digits[nDigits++] = '0';
			nTrailZero -= 1;
		    }
		    digits[nDigits++] = c;
		    break; // out of switch.
		case '.':
		    if ( decSeen ){
			// already saw one ., this is the 2nd.
			throw new NumberFormatException("multiple points");
		    }
		    decPt = i;
		    if ( signSeen ){
			decPt -= 1;
		    }
		    decSeen = true;
		    break; // out of switch.
		default:
		    break digitLoop;
		}
		i++;
	    }
	    /*
	     * At this point, we've scanned all the digits and decimal
	     * point we're going to see. Trim off leading and trailing
	     * zeros, which will just confuse us later, and adjust
	     * our initial decimal exponent accordingly.
	     * To review:
	     * we have seen i total characters.
	     * nLeadZero of them were zeros before any other digits.
	     * nTrailZero of them were zeros after any other digits.
	     * if ( decSeen ), then a . was seen after decPt characters
	     * ( including leading zeros which have been discarded )
	     * nDigits characters were neither lead nor trailing
	     * zeros, nor point
	     */
	    /*
	     * special hack: if we saw no non-zero digits, then the
	     * answer is zero!
	     * Unfortunately, we feel honor-bound to keep parsing!
	     */
	    if ( nDigits == 0 ){
		digits = zero;
		nDigits = 1;
		if ( nLeadZero == 0 ){
		    // we saw NO DIGITS AT ALL,
		    // not even a crummy 0!
		    // this is not allowed.
		    break parseNumber; // go throw exception
		}

	    }

	    /* Our initial exponent is decPt, adjusted by the number of
	     * discarded zeros. Or, if there was no decPt,
	     * then its just nDigits adjusted by discarded trailing zeros.
	     */
	    if ( decSeen ){
		decExp = decPt - nLeadZero;
	    } else {
		decExp = nDigits+nTrailZero;
	    }

	    /*
	     * Look for 'e' or 'E' and an optionally signed integer.
	     */
	    if ( (i < l) &&  ((c = in.charAt(i) )=='e') || (c == 'E') ){
		int expSign = 1;
		int expVal  = 0;
		int reallyBig = Integer.MAX_VALUE / 10;
		boolean expOverflow = false;
		switch( in.charAt(++i) ){
		case '-':
		    expSign = -1;
		    //FALLTHROUGH
		case '+':
		    i++;
		}
		int expAt = i;
	    expLoop:
		while ( i < l  ){
		    if ( expVal >= reallyBig ){
			// the next character will cause integer
			// overflow.
			expOverflow = true;
		    }
		    switch ( c = in.charAt(i++) ){
		    case '0':
		    case '1':
		    case '2':
		    case '3':
		    case '4':
		    case '5':
		    case '6':
		    case '7':
		    case '8':
		    case '9':
			expVal = expVal*10 + ( (int)c - (int)'0' );
			continue;
		    default:
			i--;	       // back up.
			break expLoop; // stop parsing exponent.
		    }
		}
		int expLimit = bigDecimalExponent+nDigits+nTrailZero;
		if ( expOverflow || ( expVal > expLimit ) ){
		    //
		    // The intent here is to end up with
		    // infinity or zero, as appropriate.
		    // The reason for yielding such a small decExponent,
		    // rather than something intuitive such as
		    // expSign*Integer.MAX_VALUE, is that this value
		    // is subject to further manipulation in
		    // doubleValue() and floatValue(), and I don't want
		    // it to be able to cause overflow there!
		    // (The only way we can get into trouble here is for
		    // really outrageous nDigits+nTrailZero, such as 2 billion. )
		    //
		    decExp = expSign*expLimit;
		} else {
		    // this should not overflow, since we tested
		    // for expVal > (MAX+N), where N >= abs(decExp)
		    decExp = decExp + expSign*expVal;
		}

		// if we saw something not a digit ( or end of string )
		// after the [Ee][+-], without seeing any digits at all
		// this is certainly an error. If we saw some digits,
		// but then some trailing garbage, that might be ok.
		// so we just fall through in that case.
		// HUMBUG
                if ( i == expAt ) 
		    break parseNumber; // certainly bad
	    }
	    /*
	     * We parsed everything we could.
	     * If there are leftovers, then this is not good input!
	     */
	    if ( i < l &&
                ((i != l - 1) ||
                (in.charAt(i) != 'f' &&
                 in.charAt(i) != 'F' &&
                 in.charAt(i) != 'd' &&
                 in.charAt(i) != 'D'))) {
                break parseNumber; // go throw exception
            }

	    return new PtFloatingDecimal( isNegative, decExp, digits, nDigits,  false );
	} catch ( StringIndexOutOfBoundsException e ){ }
	throw new NumberFormatException( in );
    }
}
