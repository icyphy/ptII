/* A library of accessors.

 Copyright (c) 2015 The Regents of the University of California.
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
package org.terraswarm.accessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// AccessorLibrary

/**
 A library of accessors.
 FIXME: More. Sketch:
 This is a configurable attribute.
 The configure method is used to specify the URL of the accessor library.
 In MoML, this is done using &lt;configure source="URL"/&gt;

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class AccessorLibrary extends EntityLibrary {

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AccessorLibrary(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Populate the actor by reading the file specified by the
     *  <i>source</i> parameter.  Note that the exception thrown here is
     *  a runtime exception, inappropriately.  This is because execution of
     *  this method is deferred to the last possible moment, and it is often
     *  evaluated in a context where a compile-time exception cannot be
     *  thrown.  Thus, extra care should be exercised to provide valid
     *  MoML specifications.
     *  @exception InvalidStateException If the source cannot be read, or if
     *   an exception is thrown parsing its MoML data.
     */
    @Override
    public void populate() throws InvalidStateException {
        if (_populating) {
            return;
        }
        try {
            // Avoid populating during cloning.
            if (_cloning) {
                return;
            }

            _populating = true;
            
            // Upon populating this library, change the icon loader to look for accessor icons.
            // To prevent overriding some other icon loader (e.g. Kepler),
            // set the custom icon loader only if there isn't one already.
            if (MoMLParser.getIconLoader() == null) {
                MoMLParser.setIconLoader(new AccessorIconLoader());
            }

            if (!_configureDone) {
                // NOTE: If you suspect this is being called prematurely,
                // then uncomment the following to see who is doing the
                // calling.
                // System.out.println("-----------------------");
                // (new Exception()).printStackTrace();
                // NOTE: Set this early to prevent repeated attempts to
                // evaluate if an exception occurs.  This way, it will
                // be possible to examine a partially populated entity.
                _configureDone = true;

                // NOTE: This does not seem like the right thing to do!
                // removeAllEntities();
                MoMLParser parser = new MoMLParser(workspace());

                parser.setContext(this);

                if (_configureSource != null && !_configureSource.equals("")) {
                    // FIXME: This will only work if the _configureSource is
                    // "http://terraswarm.org/accessors".
                    // Second argument specifies to update the repository.
                    URL source = JSAccessor._sourceToURL(_configureSource, true);
                    
                    // Get the index at the specified location.
                    URL indexFile = new URL(source, "index.json");
                    BufferedReader in = null;
                    StringBuffer contents = new StringBuffer();
                    try {
                        in = new BufferedReader(new InputStreamReader(
                                indexFile.openStream()));
                        String input;
                        while ((input = in.readLine()) != null) {
                            contents.append(input);
                        }
                        JSONArray index = new JSONArray(contents.toString());
                        for (int i = 0; i < index.length(); ++i) {
                            Object value = index.get(i);
                            if (value instanceof String) {
                                System.out.println("FIXME: " + value);
                            } else {
                                // FIXME: What to do here?
                                System.err.println("Cannot parse index entry: " + value);
                            }
                        }
                    } catch (IOException ex) {
                        // FIXME: What to do here?
                        System.err.println("Cannot open index file: " + indexFile
                                + "\n" + ex);
                    } catch (JSONException ex) {
                        // FIXME: What to do here?
                        System.err.println("Cannot parse index data: " + contents
                                + "\n" + ex);
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }

                    // FIXME: This assumes the configure source is a specific accessor file,
                    // not a library.
                    String moml = JSAccessor.accessorToMoML(_configureSource, false);
                    // FIXME: Need a URL base?
                    parser.parse(moml);
                }

                if (_configureText != null && !_configureText.equals("")) {
                    /* FIXME: What to do with included text?
                    
                    // NOTE: Regrettably, the XML parser we are using cannot
                    // deal with having a single processing instruction at the
                    // outer level.  Thus, we have to strip it.
                    String trimmed = _configureText.trim();

                    if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
                        trimmed = trimmed.substring(2, trimmed.length() - 2)
                                .trim();

                        if (trimmed.startsWith("moml")) {
                            trimmed = trimmed.substring(4).trim();
                            parser.parse(_base, trimmed);
                        }

                        // If it's not a moml processing instruction, ignore.
                    } else {
                        // Data is not enclosed in a processing instruction.
                        // Must have been given in a CDATA section.
                        parser.parse(_base, _configureText);
                    }
                    */
                }
            }
            // Set this if everything succeeds.
            _populated = true;
        } catch (Exception ex) {
            MessageHandler.error("Failed to populate library.", ex);

            // Oddly, under JDK1.3.1, we may see the line
            // "Exception occurred during event dispatching:"
            // in the console window, but there is no stack trace.
            // If we change this exception to a RuntimeException, then
            // the stack trace appears.  My guess is this indicates a
            // bug in the ptolemy.kernel.Exception* classes or in JDK1.3.1
            // Note that under JDK1.4, the stack trace is printed in
            // both cases.
            throw new InvalidStateException(this, ex,
                    "Failed to populate Library");
        } finally {
            _populating = false;
        }
    }
}
