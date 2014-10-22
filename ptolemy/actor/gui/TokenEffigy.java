/* A representative of a file that contains one or more tokens.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TokenEffigy

/**
 An effigy for a file that contains one or more tokens, one per line,
 represented as text in the expression language.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class TokenEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public TokenEffigy(Workspace workspace) {
        super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TokenEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified token to the token array associated with this
     *  effigy.
     *  @param token The token to append.
     *  @exception IllegalActionException If the token is not acceptable.
     */
    public void append(Token token) throws IllegalActionException {
        _tokens.add(token);

        // Notify the contained tableaux.
        Iterator tableaux = entityList(TokenTableau.class).iterator();

        while (tableaux.hasNext()) {
            TokenTableau tableau = (TokenTableau) tableaux.next();
            tableau.append(token);
        }
    }

    /** If the argument is the <i>uri</i> parameter, then read the
     *  specified URL and parse the data contained in it.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be read or
     *   if the data is malformed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // The superclass does some handling of the url attribute.
        super.attributeChanged(attribute);

        if (attribute == uri) {
            try {
                URL urlToRead = uri.getURL();

                if (urlToRead != null) {
                    read(urlToRead);
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, null, ex,
                        "Failed to read data: " + ex.getMessage());
            }
        }
    }

    /** Clear the token array associated with this effigy.
     */
    public void clear() {
        _tokens.clear();

        // Notify the contained tableaux.
        Iterator tableaux = entityList(TokenTableau.class).iterator();

        while (tableaux.hasNext()) {
            TokenTableau tableau = (TokenTableau) tableaux.next();
            tableau.clear();
        }
    }

    /** Return an array of the tokens in the file.
     *  @return An array of tokens in the file.
     *  @see #setTokens(List)
     */
    public ArrayList getTokens() {
        return _tokens;
    }

    /** Read the specified URL and parse the data.
     *  @param input The URL to read.
     *  @exception IOException If an error occurs while reading the URL
     *   or parsing the data.
     */
    public void read(URL input) throws IOException {
        if (input == null) {
            throw new IOException("Attempt to read from null input.");
        }

        LineNumberReader reader = null;

        try {
            reader = new LineNumberReader(new InputStreamReader(
                    input.openStream()));

            while (true) {
                // NOTE: The following tolerates all major line terminators.
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                try {
                    // Parse the line.
                    if (_variable == null) {
                        _variable = new Variable(workspace());
                        _variable.setName("Expression evaluator");
                    }

                    _variable.setExpression(line);

                    Token token = _variable.getToken();

                    if (token != null) {
                        _tokens.add(token);

                        // Notify the contained tableaux.
                        Iterator tableaux = entityList(TokenTableau.class)
                                .iterator();

                        while (tableaux.hasNext()) {
                            ((TokenTableau) tableaux.next()).append(token);
                        }
                    }
                } catch (KernelException ex) {
                    throw new IOException("Error evaluating data expression: "
                            + ex.getMessage());
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /** Set the token array associated with this effigy.
     *  @param tokens An array of tokens.
     *  @exception IllegalActionException If the tokens are not acceptable.
     *  @see #getTokens()
     */
    public void setTokens(List tokens) throws IllegalActionException {
        _tokens.clear();
        _tokens.addAll(tokens);

        // Notify the contained tableaux.
        Iterator tableaux = entityList(TokenTableau.class).iterator();

        while (tableaux.hasNext()) {
            TokenTableau tableau = (TokenTableau) tableaux.next();
            tableau.clear();
            tableau.append(tokens);
        }
    }

    /** Write the current data of this effigy to the specified file.
     *  This is done by producing one line per token using its toString()
     *  method.
     *  @param file The file to write to, or null to write to standard out.
     *  @exception IOException If the write fails.
     */
    @Override
    public void writeFile(File file) throws IOException {
        Writer writer = null;

        if (file == null) {
            writer = new OutputStreamWriter(System.out);
        } else {
            writer = new FileWriter(file);
        }

        // Use a PrintWriter so that the local platform's notion
        // of line termination is used, making a more user-friendly output.
        // Note that the corresponding reader is platform-tolerant.
        PrintWriter print = new PrintWriter(writer);
        Iterator tokens = _tokens.iterator();

        while (tokens.hasNext()) {
            Token token = (Token) tokens.next();
            print.println(token.toString());
        }

        print.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The list of tokens read from the file.
    private ArrayList _tokens = new ArrayList();

    // The variable used to evaluate the expression.
    private Variable _variable;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return false, indicating that this effigy factory is not
         *  capable of creating an effigy without a URL being specified.
         *  @return False.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the
         *  specified URL. If the specified URL is null, or
         *  if the URL does not end with extension ".ptd"
         *  (for Ptolemy data), then return null.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, which are
         *   ignored here, and therefore can be null.
         *  @param input The input URL.
         *  @return A new instance of TokenEffigy, or null if the URL
         *   does not have a recognized extension.
         *  @exception Exception If the URL cannot be read.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            if (input != null) {
                String extension = getExtension(input);

                if (extension.equals("ptd")) {
                    TokenEffigy effigy = new TokenEffigy(container,
                            container.uniqueName("effigy"));
                    effigy.uri.setURL(input);
                    return effigy;
                }
            }

            return null;
        }
    }
}
