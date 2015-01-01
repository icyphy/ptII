/* Applet to display raw text from a URL in a text area.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.gui.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ptolemy.gui.BasicJApplet;

///////////////////////////////////////////////////////////////////
//// ShowRawDocumentApplet

/**
 Applet to display raw text from a URL in a TextArea.

 This applet is useful for displaying raw xml source with out processing
 it.  If you would like to display a document within a browser with regular
 processing, see the documentation for appletContext.showDocument(sourceURL).

 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class ShowRawDocumentApplet extends BasicJApplet {
    /** Constructor.
     */
    public ShowRawDocumentApplet() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read applet parameters, open the URL and display the contents
     *  in a TextArea.
     */
    @Override
    public void init() {
        super.init();

        // Process the rows and columns applet parameters
        int rows;

        // Process the rows and columns applet parameters
        int columns;
        String rowsString = getParameter("rows");

        if (rowsString != null) {
            rows = Integer.parseInt(rowsString);
        } else {
            rows = 10;
        }

        String columnsString = getParameter("columns");

        if (columnsString != null) {
            columns = Integer.parseInt(columnsString);
        } else {
            columns = 40;
        }

        // Get the source applet parameter before we create the
        // _jTextArea in case there are problems with the parameter.
        String sourceURLString = getParameter("source");
        URL sourceURL = null;

        if (sourceURLString != null) {
            try {
                showStatus("Reading data . . .");
                sourceURL = new URL(getDocumentBase(), sourceURLString);
            } catch (MalformedURLException e) {
                System.err.println(e.toString());
            }
        }

        /*JFrame _frame = */new JFrame();
        _jTextArea = new JTextArea(rows, columns);
        _jTextArea.setEditable(false);
        _scrollPane = new JScrollPane(_jTextArea);
        getContentPane().add(_scrollPane);

        String newline = System.getProperty("line.separator");

        if (sourceURL == null) {
            showStatus("Failed to open " + sourceURLString);
            System.err.println("Failed to open " + sourceURLString);
        } else {
            // Read in the data one line at a time.
            InputStream in = null;
            try {
                in = sourceURL.openStream();
                _bufferedReader = new BufferedReader(new InputStreamReader(in, java.nio.charset.Charset.defaultCharset()));

                String line = _bufferedReader.readLine();

                while (line != null) {
                    _jTextArea.append(line + newline);
                    line = _bufferedReader.readLine();
                }

                showStatus("Done");
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + e);
            } catch (IOException e) {
                System.err.println("Error reading input file: " + e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ex) {
                        System.err.println("Failed to close " + sourceURLString
                                + ":" + ex);
                    }
                }
            }
        }

        //getContentPane().add(_frame);
        //getContentPane().add(_jTextArea);
        validate();
        repaint();
    }

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    @Override
    public String[][] getParameterInfo() {
        String[][] newinfo = { { "columns", "integer", "40" },
                { "rows", "integer", "10" },
                { "source", "", "URL of the file to be displayed" }, };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private BufferedReader _bufferedReader;

    private JTextArea _jTextArea;

    private JScrollPane _scrollPane;
}
