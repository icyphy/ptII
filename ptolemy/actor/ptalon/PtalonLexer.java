// $ANTLR 2.7.7 (2006-11-01): "parser.g" -> "PtalonLexer.java"$
/* Lexer/Parser for Ptalon.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

import antlr.ANTLRHashString;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.NoViableAltForCharException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.impl.BitSet;

/**
  PtalonLexer.java generated from populator.g by ANTLR.

  @author Adam Cataldo, Elaine Cheong, Thomas Huining Feng
  @version $Id$
  @since Ptolemy II 7.0
  @Pt.ProposedRating Red (celaine)
  @Pt.AcceptedRating Red (celaine)
 */

public class PtalonLexer extends antlr.CharScanner implements PtalonTokenTypes,
        TokenStream {
    public PtalonLexer(InputStream in) {
        this(new ByteBuffer(in));
    }

    public PtalonLexer(Reader in) {
        this(new CharBuffer(in));
    }

    public PtalonLexer(InputBuffer ib) {
        this(new LexerSharedInputState(ib));
    }

    public PtalonLexer(LexerSharedInputState state) {
        super(state);
        caseSensitiveLiterals = true;
        setCaseSensitive(true);
        literals = new Hashtable();
        literals.put(new ANTLRHashString("for", this), new Integer(25));
        literals.put(new ANTLRHashString("outport", this), new Integer(8));
        literals.put(new ANTLRHashString("transparent", this), new Integer(15));
        literals.put(new ANTLRHashString("optional", this), new Integer(37));
        literals.put(new ANTLRHashString("parameter", this), new Integer(10));
        literals.put(new ANTLRHashString("false", this), new Integer(21));
        literals.put(new ANTLRHashString("true", this), new Integer(20));
        literals.put(new ANTLRHashString("preserve", this), new Integer(39));
        literals.put(new ANTLRHashString("actor", this), new Integer(12));
        literals.put(new ANTLRHashString("remove", this), new Integer(38));
        literals.put(new ANTLRHashString("import", this), new Integer(19));
        literals.put(new ANTLRHashString("next", this), new Integer(27));
        literals.put(new ANTLRHashString("inport", this), new Integer(7));
        literals.put(new ANTLRHashString("reference", this), new Integer(16));
        literals.put(new ANTLRHashString("port", this), new Integer(4));
        literals.put(new ANTLRHashString("initially", this), new Integer(26));
        literals.put(new ANTLRHashString("actorparameter", this), new Integer(
                13));
        literals.put(new ANTLRHashString("is", this), new Integer(24));
        literals.put(new ANTLRHashString("danglingPortsOkay", this),
                new Integer(28));
        literals.put(new ANTLRHashString("attachDanglingPorts", this),
                new Integer(29));
        literals.put(new ANTLRHashString("relation", this), new Integer(14));
        literals.put(new ANTLRHashString("if", this), new Integer(22));
        literals.put(new ANTLRHashString("negate", this), new Integer(36));
        literals.put(new ANTLRHashString("else", this), new Integer(23));
    }

    @Override
    public Token nextToken() throws TokenStreamException {
        tryAgain: for (;;) {
            int _ttype = Token.INVALID_TYPE;
            resetText();
            try { // for char stream error handling
                try { // for lexical error handling
                    switch (LA(1)) {
                    case ',': {
                        mCOMMA(true);
                        break;
                    }
                    case '.': {
                        mDOT(true);
                        break;
                    }
                    case '{': {
                        mLCURLY(true);
                        break;
                    }
                    case '(': {
                        mLPAREN(true);
                        break;
                    }
                    case ']': {
                        mRBRACKET(true);
                        break;
                    }
                    case '}': {
                        mRCURLY(true);
                        break;
                    }
                    case ')': {
                        mRPAREN(true);
                        break;
                    }
                    case ';': {
                        mSEMI(true);
                        break;
                    }
                    case '+': {
                        mPLUS(true);
                        break;
                    }
                    case '\\': {
                        mESC(true);
                        break;
                    }
                    case '$':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case '_':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z': {
                        mID(true);
                        break;
                    }
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9': {
                        mNUMBER_LITERAL(true);
                        break;
                    }
                    case '"': {
                        mSTRING_LITERAL(true);
                        break;
                    }
                    case '\t':
                    case '\n':
                    case '\u000c':
                    case '\r':
                    case ' ': {
                        mWHITE_SPACE(true);
                        break;
                    }
                    default:
                        if ((LA(1) == ':') && (LA(2) == '=')) {
                            mASSIGN(true);
                        } else if ((LA(1) == '=') && (LA(2) == '>')) {
                            mTRANSFORM(true);
                        } else if ((LA(1) == '/') && (LA(2) == '/')) {
                            mLINE_COMMENT(true);
                        } else if ((LA(1) == '[') && (LA(2) == '[')) {
                            mEXPRESSION(true);
                        } else if ((LA(1) == '/') && (LA(2) == '*')) {
                            mCOMMENT(true);
                        } else if ((LA(1) == '[') && (true)) {
                            mLBRACKET(true);
                        } else if ((LA(1) == '=') && (true)) {
                            mEQUALS(true);
                        } else if ((LA(1) == ':') && (true)) {
                            mCOLON(true);
                        } else {
                            if (LA(1) == EOF_CHAR) {
                                uponEOF();
                                _returnToken = makeToken(Token.EOF_TYPE);
                            } else {
                                throw new NoViableAltForCharException(LA(1),
                                        getFilename(), getLine(), getColumn());
                            }
                        }
                    }
                    if (_returnToken == null) {
                        continue tryAgain; // found SKIP token
                    }
                    _ttype = _returnToken.getType();
                    _returnToken.setType(_ttype);
                    return _returnToken;
                } catch (RecognitionException e) {
                    throw new TokenStreamRecognitionException(e);
                }
            } catch (CharStreamException cse) {
                if (cse instanceof CharStreamIOException) {
                    throw new TokenStreamIOException(
                            ((CharStreamIOException) cse).io);
                } else {
                    throw new TokenStreamException(cse.getMessage());
                }
            }
        }
    }

    public final void mASSIGN(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = ASSIGN;
        match(":=");
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mCOMMA(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = COMMA;
        match(',');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mDOT(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = DOT;
        match('.');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mLBRACKET(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LBRACKET;
        match('[');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mLCURLY(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LCURLY;
        match('{');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mLPAREN(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LPAREN;
        match('(');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mRBRACKET(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = RBRACKET;
        match(']');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mRCURLY(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = RCURLY;
        match('}');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mRPAREN(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = RPAREN;
        match(')');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSEMI(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SEMI;
        match(';');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mEQUALS(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = EQUALS;
        match('=');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mCOLON(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = COLON;
        match(':');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mTRANSFORM(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = TRANSFORM;
        match("=>");
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mPLUS(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = PLUS;
        match('+');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mESC(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = ESC;
        match('\\');
        {
            switch (LA(1)) {
            case 'n': {
                match('n');
                break;
            }
            case 'r': {
                match('r');
                break;
            }
            case 't': {
                match('t');
                break;
            }
            case 'b': {
                match('b');
                break;
            }
            case 'f': {
                match('f');
                break;
            }
            case '"': {
                match('"');
                break;
            }
            case '\'': {
                match('\'');
                break;
            }
            default: {
                throw new NoViableAltForCharException(LA(1), getFilename(),
                        getLine(), getColumn());
            }
            }
        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mID(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = ID;
        {
            switch (LA(1)) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z': {
                matchRange('a', 'z');
                break;
            }
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z': {
                matchRange('A', 'Z');
                break;
            }
            case '_': {
                match('_');
                break;
            }
            case '$': {
                match('$');
                break;
            }
            default: {
                throw new NoViableAltForCharException(LA(1), getFilename(),
                        getLine(), getColumn());
            }
            }
        }
        {
            _loop89: do {
                switch (LA(1)) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z': {
                    matchRange('a', 'z');
                    break;
                }
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z': {
                    matchRange('A', 'Z');
                    break;
                }
                case '_': {
                    match('_');
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    matchRange('0', '9');
                    break;
                }
                default: {
                    break _loop89;
                }
                }
            } while (true);
        }
        _ttype = testLiteralsTable(_ttype);
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mNUMBER_LITERAL(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = NUMBER_LITERAL;
        {
            int _cnt92 = 0;
            _loop92: do {
                if (((LA(1) >= '0' && LA(1) <= '9'))) {
                    matchRange('0', '9');
                } else {
                    if (_cnt92 >= 1) {
                        break _loop92;
                    } else {
                        throw new NoViableAltForCharException(LA(1),
                                getFilename(), getLine(), getColumn());
                    }
                }

                _cnt92++;
            } while (true);
        }
        {
            if ((LA(1) == '.')) {
                match('.');
                {
                    int _cnt95 = 0;
                    _loop95: do {
                        if (((LA(1) >= '0' && LA(1) <= '9'))) {
                            matchRange('0', '9');
                        } else {
                            if (_cnt95 >= 1) {
                                break _loop95;
                            } else {
                                throw new NoViableAltForCharException(LA(1),
                                        getFilename(), getLine(), getColumn());
                            }
                        }

                        _cnt95++;
                    } while (true);
                }
            } else {
            }

        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSTRING_LITERAL(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = STRING_LITERAL;
        match('"');
        {
            _loop99: do {
                switch (LA(1)) {
                case '\\': {
                    mESC(false);
                    break;
                }
                case '\u0000':
                case '\u0001':
                case '\u0002':
                case '\u0003':
                case '\u0004':
                case '\u0005':
                case '\u0006':
                case '\u0007':
                case '\u0008':
                case '\t':
                case '\u000b':
                case '\u000c':
                case '\u000e':
                case '\u000f':
                case '\u0010':
                case '\u0011':
                case '\u0012':
                case '\u0013':
                case '\u0014':
                case '\u0015':
                case '\u0016':
                case '\u0017':
                case '\u0018':
                case '\u0019':
                case '\u001a':
                case '\u001b':
                case '\u001c':
                case '\u001d':
                case '\u001e':
                case '\u001f':
                case ' ':
                case '!':
                case '#':
                case '$':
                case '%':
                case '&':
                case '\'':
                case '(':
                case ')':
                case '*':
                case '+':
                case ',':
                case '-':
                case '.':
                case '/':
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
                case ':':
                case ';':
                case '<':
                case '=':
                case '>':
                case '?':
                case '@':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '[':
                case ']':
                case '^':
                case '_':
                case '`':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '{':
                case '|':
                case '}':
                case '~':
                case '\u007f': {
                    {
                        match(_tokenSet_0);
                    }
                    break;
                }
                default: {
                    break _loop99;
                }
                }
            } while (true);
        }
        match('"');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mWHITE_SPACE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = WHITE_SPACE;
        {
            switch (LA(1)) {
            case ' ': {
                match(' ');
                break;
            }
            case '\t': {
                match('\t');
                break;
            }
            case '\u000c': {
                match('\f');
                break;
            }
            case '\n':
            case '\r': {
                {
                    if ((LA(1) == '\r') && (LA(2) == '\n')) {
                        match('\r');
                        match('\n');
                    } else if ((LA(1) == '\r') && (true)) {
                        match('\r');
                    } else if ((LA(1) == '\n')) {
                        match('\n');
                    } else {
                        throw new NoViableAltForCharException(LA(1),
                                getFilename(), getLine(), getColumn());
                    }

                }
                newline();
                break;
            }
            default: {
                throw new NoViableAltForCharException(LA(1), getFilename(),
                        getLine(), getColumn());
            }
            }
        }
        _ttype = Token.SKIP;
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mLINE_COMMENT(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LINE_COMMENT;
        match("//");
        {
            _loop106: do {
                if ((_tokenSet_1.member(LA(1)))) {
                    {
                        match(_tokenSet_1);
                    }
                } else {
                    break _loop106;
                }

            } while (true);
        }
        {
            if ((LA(1) == '\n' || LA(1) == '\r')) {
                {
                    if ((LA(1) == '\r') && (LA(2) == '\n')) {
                        {
                            match('\r');
                            match('\n');
                        }
                    } else if ((LA(1) == '\r') && (true)) {
                        match('\r');
                    } else if ((LA(1) == '\n')) {
                        match('\n');
                    } else {
                        throw new NoViableAltForCharException(LA(1),
                                getFilename(), getLine(), getColumn());
                    }

                }
                newline();
            } else {
            }

        }

        _ttype = Token.SKIP;

        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mEXPRESSION(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = EXPRESSION;
        match('[');
        match('[');
        {
            _loop112: do {
                // nongreedy exit test
                if ((LA(1) == ']') && (LA(2) == ']') && (true)) {
                    break _loop112;
                }
                if (((LA(1) >= '\u0000' && LA(1) <= '\u007f'))
                        && ((LA(2) >= '\u0000' && LA(2) <= '\u007f'))
                        && ((LA(3) >= '\u0000' && LA(3) <= '\u007f'))) {
                    matchNot(EOF_CHAR);
                } else {
                    break _loop112;
                }

            } while (true);
        }
        match(']');
        match(']');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mCOMMENT(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = COMMENT;
        match('/');
        match('*');
        {
            _loop115: do {
                // nongreedy exit test
                if ((LA(1) == '*') && (LA(2) == '/') && (true)) {
                    break _loop115;
                }
                if (((LA(1) >= '\u0000' && LA(1) <= '\u007f'))
                        && ((LA(2) >= '\u0000' && LA(2) <= '\u007f'))
                        && ((LA(3) >= '\u0000' && LA(3) <= '\u007f'))) {
                    matchNot(EOF_CHAR);
                } else {
                    break _loop115;
                }

            } while (true);
        }
        match('*');
        match('/');
        _ttype = Token.SKIP;
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    private static final long[] mk_tokenSet_0() {
        long[] data = { -17179878401L, -268435457L, 0L, 0L };
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

    private static final long[] mk_tokenSet_1() {
        long[] data = { -9217L, -1L, 0L, 0L };
        return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

}
