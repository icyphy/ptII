/* A store for key-value pairs.

 Copyright (c) 2013-2014 The Regents of the University of California.
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

package ptolemy.actor.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.LoggerListener;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

/**
   A store for key-value pairs.
   This actor stores key-value pairs and provides an interface for retrieving
   them one at a time or in groups.

   @author Shuhei Emoto, Edward A. Lee, Kentaro Mizouchi
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class Dictionary extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Dictionary(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Alphabetized by variable name.

        keys = new TypedIOPort(this, "keys", false, true);
        new SingletonParameter(keys, "_showName").setExpression("true");

        readKey = new TypedIOPort(this, "readKey", true, false);
        readKey.setTypeEquals(BaseType.STRING);
        new SingletonParameter(readKey, "_showName").setExpression("true");

        readKeyArray = new TypedIOPort(this, "readKeyArray", true, false);
        new SingletonParameter(readKeyArray, "_showName").setExpression("true");

        result = new TypedIOPort(this, "result", false, true);
        new SingletonParameter(result, "_showName").setExpression("true");

        resultArray = new TypedIOPort(this, "resultArray", false, true);
        new SingletonParameter(resultArray, "_showName").setExpression("true");
        // FIXME: The length of the output array should match the length of the readKeyArray.
        // How to do that?

        triggerKeys = new TypedIOPort(this, "triggerKeys", true, false);
        new SingletonParameter(triggerKeys, "_showName").setExpression("true");
        new StringAttribute(triggerKeys, "_cardinal").setExpression("SOUTH");

        value = new TypedIOPort(this, "value", true, false);
        new SingletonParameter(value, "_showName").setExpression("true");

        writeKey = new TypedIOPort(this, "writeKey", true, false);
        writeKey.setTypeEquals(BaseType.STRING);
        new SingletonParameter(writeKey, "_showName").setExpression("true");

        notFound = new TypedIOPort(this, "notFound", false, true);
        new SingletonParameter(notFound, "_showName").setExpression("true");

        // Set the type constraints.
        keys.setTypeAtLeast(ArrayType.arrayOf(writeKey));
        readKeyArray.setTypeAtLeast(ArrayType.arrayOf(readKey));
        result.setTypeSameAs(value);
        resultArray.setTypeAtLeast(ArrayType.arrayOf(value));
        notFound.setTypeEquals(new ArrayType(BaseType.STRING));

        _store = new HashMap<String, Token>();

        file = new FileParameter(this, "file");
        updateFile = new Parameter(this, "updateFile");
        updateFile.setTypeEquals(BaseType.BOOLEAN);
        updateFile.setExpression("false");

        loggingDirectory = new FileParameter(this, "loggingDirectory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If a file is given here, it will be read upon initialization
     *  (if it exists and can be parsed as an array of arrays of tokens)
     *  to initialize the dictionary.
     */
    public FileParameter file;

    /** Upon receiving any token at the triggerKeys port, this actor
     *  will produce on this output an array containing all the keys
     *  of entries in the dictionary. The order is arbitrary.
     *  If there are no entries in the dictionary, then send an
     *  empty array.
     *  The type is array of string.
     */
    public TypedIOPort keys;

    /** If given, a log file will be written to the specified
     *  directory.
     *  <p>A file name can also contain the following strings that start
     *  with "$", which get substituted
     *  with the appropriate values.</p>
     *  <table>
     *  <tr>
     *  <th>String</th>
     *  <th>Description</th>
     *  <th>Property</th>
     *  </tr>
     *  <tr>
     *  <td><code>$CWD</code></td>
     *  <td>The current working directory</td>
     *  <td><code>user.dir</code></td>
     *  </tr>
     *  <tr>
     *  <td><code>$HOME</code></td>
     *  <td>The user's home directory</td>
     *  <td><code>user.home</code></td>
     *  </tr>
     *  <tr>
     *  <td><code>$PTII</code></td>
     *  <td>The home directory of the Ptolemy II installation</td>
     *  <td><code>ptolemy.ptII.dir</code></td>
     *  </tr>
     *  <tr>
     *  <td><code>$TMPDIR</code></td>
     *  <td>The temporary directory</td>
     *  <td><code>java.io.tmpdir</code></td>
     *  </tr>
     *  <tr>
     *  <td><code>$USERNAME</code></td>
     *  <td>The user's account name</td>
     *  <td><code>user.name</code></td>
     *  </tr>
     *  </table>
     */
    public FileParameter loggingDirectory;

    /** An output listing one or more keys that were
     *  requested but not found in the dictionary.
     *  The output is produced only if a key is not
     *  found. The output type is an array of strings.
     */
    public TypedIOPort notFound;

    /** An input that provides a key for a value to be read from the
     *  dictionary.  If the dictionary does not contain any value
     *  corresponding to this key, then the output will be a nil
     *  token. This has type string.
     */
    public TypedIOPort readKey;

    /** An input that provides an array of keys to be read
     *  simultaneously from the dictionary. The output will be an
     *  array with the same length as this input where each entry in
     *  the output array is the value corresponding to the
     *  corresponding key in the input array. For any key that has no
     *  entry in the dictionary, a nil token will be inserted in the
     *  output array.
     *  The type is array of string.
     */
    public TypedIOPort readKeyArray;

    /** An output providing the result of a single reading of the
     *  dictionary via the readKey input port. If the specified key
     *  is not found, this port will produce a nil token, and an
     *  array of length one with the key will be produced on the
     *  {@link #notFound} output port.
     */
    public TypedIOPort result;

    /** An output providing the result of a multiple reading of the
     *  dictionary via the readKeyArray input port. For any of the
     *  keys in the {@link #readKeyArray} input is not in the dictionary,
     *  there will be a nil token in the result array in the position
     *  of the missing key. The missing keys will be produced on the
     *  notFound output.
     */
    public TypedIOPort resultArray;

    /** Upon receiving any token at this port, this actor will produce
     *  on the keys output an array containing all the keys of entries
     *  in the dictionary. The order is arbitrary.
     */
    public TypedIOPort triggerKeys;

    /** If set to true, and if a <i>file</i> parameter is given, then
     *  upon each update to the dictionary, the contents of the dictionary
     *  will be stored in the file.  This defaults to false.
     */
    public Parameter updateFile;

    /** Input port for providing a value to store in the dictionary.
     *  The value will be stored only if a writeKey input arrives at
     *  the same time. Otherwise, it will be discarded.
     */
    public TypedIOPort value;

    /** An input that provides a key for a key-value pair to be stored
     *  in the dictionary. If a key arrives on this port, but there is
     *  no value on the value port or the value is nil, then the
     *  dictionary entry with the specified key will be
     *  removed. Otherwise, the value provided on the value port will
     *  be stored indexed by this key. This has type string.
     */
    public TypedIOPort writeKey;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Dictionary newObject = (Dictionary) super.clone(workspace);

        try {
            // Set the type constraints.
            newObject.readKeyArray.setTypeAtLeast(ArrayType
                    .arrayOf(newObject.readKey));
            newObject.keys
            .setTypeAtLeast(ArrayType.arrayOf(newObject.writeKey));
            newObject.result.setTypeSameAs(newObject.value);
            newObject.resultArray.setTypeAtLeast(ArrayType
                    .arrayOf(newObject.value));
        } catch (IllegalActionException ex) {
            CloneNotSupportedException exception = new CloneNotSupportedException(
                    "Failed to clone " + getFullName());
            exception.initCause(ex);
            throw exception;
        }
        // Initialize objects.
        newObject._store = new HashMap<String, Token>();

        return newObject;
    }

    /** If there is a writeKey input, then update the dictionary;
     *  specifically, if there is also a value input, then insert into
     *  the dictionary the key-value pair given by these two inputs.
     *  Otherwise, or if the value input is a nil token, then delete
     *  the dictionary entry corresponding to the key.  If there is a
     *  readKey input, then read the dictionary and produce on the
     *  result output the entry corresponding to the key, or a nil
     *  token if there is no such entry.  If there is a readKeyArray
     *  input, then read the dictionary and produce on the resultArray
     *  output the entries corresponding to the keys, with nil tokens
     *  inserted for any missing entry.  If there is a triggerKeys
     *  input, then produce on the keys output an array containing all
     *  the keys in the dictionary, in arbitrary order.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (writeKey.getWidth() > 0 && writeKey.hasToken(0)) {
            StringToken theKey = (StringToken) writeKey.get(0);

            // Get a value if there is one.
            Token theValue = null;
            if (value.getWidth() > 0 && value.hasToken(0)) {
                theValue = value.get(0);
            }
            if (theValue == null || theValue.isNil()) {
                // Remove the entry.
                Token removed = _store.remove(theKey.stringValue());
                if (_debugging) {
                    if (removed == null) {
                        _debug("Attempted to remove non-existent key: "
                                + theKey);
                    } else {
                        _debug("Removed key: " + theKey);
                    }
                }
            } else {
                _store.put(theKey.stringValue(), theValue);
                if (_debugging) {
                    _debug("Storing key, value: " + theKey + ", " + theValue);
                }
            }
        } else if (value.getWidth() > 0 && value.hasToken(0)) {
            // Read and discard the input token so that DE doesn't refire me.
            value.get(0);
        }
        if (readKey.getWidth() > 0 && readKey.hasToken(0)) {
            StringToken theKey = (StringToken) readKey.get(0);
            Token theResult = _store.get(theKey.stringValue());
            // NOTE: We choose to output a nil token if the result is not in the store.
            if (theResult != null) {
                result.send(0, theResult);
                if (_debugging) {
                    _debug("Retrieved key, value: " + theKey + ", " + theResult);
                }
            } else {
                // Sending nil on the output enables use of this actor in SDF, since
                // every input will trigger an output.
                result.send(0, Token.NIL);
                StringToken[] theKeys = new StringToken[1];
                theKeys[0] = theKey;
                notFound.send(0, new ArrayToken(theKeys));
                if (_debugging) {
                    _debug("Requested key with no value: " + theKey);
                }
            }
        }
        if (readKeyArray.getWidth() > 0 && readKeyArray.hasToken(0)) {
            ArrayToken theKeys = (ArrayToken) readKeyArray.get(0);
            Token[] theResult = new Token[theKeys.length()];
            ArrayList<StringToken> keysNotFound = new ArrayList<StringToken>();
            int i = 0;
            for (Token theKey : theKeys.arrayValue()) {
                String theKeyAsString = ((StringToken) theKey).stringValue();
                theResult[i] = _store.get(theKeyAsString);
                if (theResult[i] == null) {
                    theResult[i] = Token.NIL;
                    keysNotFound.add(new StringToken(theKeyAsString));
                }
                i++;
            }
            ArrayToken resultToken = new ArrayToken(value.getType(), theResult);
            if (_debugging) {
                _debug("Retrieved keys, values: " + theKeys + ", " + resultToken);
            }
            resultArray.send(0, resultToken);
            if (keysNotFound.size() > 0) {
                ArrayToken notFoundToken = new ArrayToken(
                        BaseType.STRING,
                        keysNotFound.toArray(new StringToken[keysNotFound.size()]));
                notFound.send(0, notFoundToken);
                if (_debugging) {
                    _debug("Keys with no value: " + notFoundToken);
                }
            }
        }
        if (triggerKeys.getWidth() > 0 && triggerKeys.hasToken(0)) {
            // Must consume the trigger, or DE will fire me again.
            triggerKeys.get(0);
            StringToken[] result = new StringToken[_store.size()];
            int i = 0;
            for (String label : _store.keySet()) {
                result[i] = new StringToken(label);
                i++;
            }
            if (result.length > 0) {
                keys.send(0, new ArrayToken(result));
            } else {
                // Send an empty array.
                keys.send(0, new ArrayToken(BaseType.STRING));
            }
        }
    }

    /** Clear the dictionary. If a <i>file</i> is specified,
     *  attempt to read it to initialize the dictionary.
     *  If <i>enableLogging</i> is true, then start logging.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        File directory = loggingDirectory.asFile();
        if (directory != null) {
            // Start logging.
            // Leave off the leading period on the file so it doen't get hidden.
            _logger = new LoggerListener(getFullName().substring(1), directory);
            addDebugListener(_logger);
            try {
                MessageHandler.message("Log file being written to "
                        + directory.getCanonicalPath());
            } catch (IOException e) {
                // Ignore.
            }
        } else {
            if (_logger != null) {
                removeDebugListener(_logger);
                _logger = null;
            }
        }

        _store.clear();

        File theFile = file.asFile();
        if (theFile != null && theFile.canRead()) {
            BufferedReader reader = file.openForReading();
            StringBuffer dictionary = new StringBuffer();
            String line;
            try {
                line = reader.readLine();
                while (line != null) {
                    dictionary.append(line);
                    line = reader.readLine();
                }
                // FIXME: May want to support JSON formatted input.
                if (_parser == null) {
                    _parser = new PtParser();
                }
                ASTPtRootNode parseTree = _parser.generateParseTree(dictionary
                        .toString());

                if (_parseTreeEvaluator == null) {
                    _parseTreeEvaluator = new ParseTreeEvaluator();
                }

                if (_scope == null) {
                    _scope = new EmptyScope();
                }

                Token parsed = _parseTreeEvaluator.evaluateParseTree(parseTree,
                        _scope);

                if (!(parsed instanceof RecordToken)) {
                    _errorMessage("Initialization file does not evaluate to a Ptolemy II record: "
                            + file.getExpression());
                }

                for (String key : ((RecordToken) parsed).labelSet()) {
                    Token value = ((RecordToken) parsed).get(key);
                    _store.put(key, value);
                }
                if (_debugging) {
                    _debug("Initialized store from file: " + theFile.getPath());
                }
            } catch (Exception e) {
                // Warning only. Continue without the file.
                _errorMessage("Failed to initialize store from file: "
                        + theFile.getPath() + " Exception: " + e.toString());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    _errorMessage("Failed to close initialization file: "
                            + theFile.getPath() + " Exception: " + e.toString());
                }
            }
        } else {
            if (_debugging) {
                _debug("Initialization file does not exist or cannot be read.");
            }
        }
    }

    /** If a <i>file</i> has been specified and <i>updateFile</i> is true, then
     *  save the current state of the dictionary in the file.
     *  If the file cannot be written, then dictionary contents will be sent
     *  to standard out and an exception will be thrown.
     *  @exception IllegalActionException If the file cannot be written.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        File theFile = file.asFile();
        if (theFile != null
                && ((BooleanToken) updateFile.getToken()).booleanValue()) {

            // Assemble a record from the current state of the store.
            RecordToken record = new RecordToken(_store);
            try {
                java.io.Writer writer = file.openForWriting();
                writer.write(record.toString());
                if (_debugging) {
                    _debug("Key-value store written to file: "
                            + theFile.getPath());
                }
            } catch (Exception e) {
                _errorMessage("Failed to update file: " + theFile.getPath()
                        + " Exception: " + e.toString());
                // Write contents to standard out so it can hopefully be retrieved.
                System.out.println(record.toString());
            } finally {
                file.close();
            }
        } else {
            if (_debugging) {
                _debug("Dictionary data discarded.");
            }
        }
        if (_logger != null) {
            _logger.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Log an error, or send messages to the default MessageHandler
     *  and debug listeners, if any.
     *  @param message The message.
     */
    private void _errorMessage(String message) {
        if (_logger != null) {
            _logger.log(Level.SEVERE, message);
        } else {
            MessageHandler.error(message);
            if (_debugging) {
                _debug(message);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The logger to use, if logging is enabled. */
    private LoggerListener _logger;

    /** The parser to use. */
    private PtParser _parser = null;

    /** The parse tree evaluator to use. */
    private ParseTreeEvaluator _parseTreeEvaluator = null;

    /** The scope for the parser. */
    private ParserScope _scope = null;

    /** The store. */
    private HashMap<String, Token> _store;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An empty scope to be used when parsing files. */
    private static class EmptyScope extends ModelScope {

        // FindBugs suggests making this static.

        /** Return null indicating that the attribute does not exist.
         *  @return Null.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            return null;
        }

        /** Return null indicating that the attribute does not exist.
         *  @return Null.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            return null;
        }

        /** Return null indicating that the attribute does not exist.
         *  @return Null.
         */
        @Override
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        @Override
        public Set identifierSet() {
            return _emptySet;
        }

        private Set _emptySet = new HashSet();
    }
}
