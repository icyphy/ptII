/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.gui;
import java.io.CharArrayReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import diva.util.ModelParser;

import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlParser;


/**
 * Parse all pages of a multi-page document from a file.  The document
 * parser must be provided with a model parser that is used to parse
 * the app-specific model on a single page.
 *
 * @author  Heloise Hse (hwawen@eecs.berkeley.edu)
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating  Red
 */
public class MultipageParser extends HandlerBase {
    /**
     * The public identity of the sketch dtd file.
     */
    public static final String PUBLIC_ID = "-//UC Berkeley//DTD multipage 1//EN";

    /**
     * The URL where the DTD is stored.
     */
    public static final String DTD_URL = "http://www.gigascale.org/diva/dtd/multipage.dtd";

    /**
     * The DTD for multipage models.
     */
    public static final String DTD_1 =
    "<!ELEMENT multipage (page*)> <!ATTLIST multipage title CDATA #REQUIRED> <!ELEMENT page (#PCDATA)> <!ATTLIST page title CDATA #REQUIRED num CDATA #REQUIRED>";

    /**
     * The string constant that specifies the title attribute of a
     * document.
     */
    public static final String TITLE_TAG = "title";

    /**
     * The string constant that specifies the start and end of a
     * document.
     */
    public static final String MULTIPAGE_TAG = "multipage";

    /**
     * The string constant that specifies the start and end of a sheet
     * in a document.
     */
    public static final String PAGE_TAG = "page";

    /**
     * The string constant that specifies the title attribute of a
     * page.  This is an optional attribute.
     */
    public static final String PAGE_TITLE_TAG = "title";

    /**
     * The string constant that specifies the page number of a page.
     */
    public static final String PAGE_NUM_TAG = "num";

    /**
     * Model parser is used to parse the content of a page.
     */
    private ModelParser _modelParser;

    /**
     * The parser driver.
     */
    private XmlParser _parser;

    /**
     * The document object that the parser is supposed to parse into.
     */
    private MultipageModel _multi;

    /**
     * A reference to the current page that's being parsed.
     */
    private Page _currentPage;

    /**
     * Keeps the attributes and their values.
     */
    private HashMap _currentAttributes = new HashMap();

    /**
     * Create a MultipageParser with the specified model parser which
     * is used to parse the content of a page.
     */
    public MultipageParser(ModelParser pageParser) {
        _modelParser = pageParser;
    }

    /**
     * Handle an attribute value assignment.  The attribute and its
     * value are temporary saved in a hash table.  They are later
     * retrieved in startElement or endElement.
     * @see com.microstar.xml.XmlHandler#attribute
     */
    public void attribute(String name, String value, boolean isSpecified)
            throws Exception {
        _currentAttributes.put(name, value);
    }

    /**
     * Handle character data.  The page content is buffered in a
     * character array by XML parser which invokes this method.  In
     * this method, the model parser gets called to parse the data in
     * the array.
     */
    public void charData(char[] chars, int offset, int length)
            throws Exception {
        Object model =
            _modelParser.parse(new CharArrayReader(chars, offset, length));
        _currentPage.setModel(model);
    }

    /**
     * Handle the end of an element.  If this is the end of a page, add
     * the current page to the document.
     */
    public void endElement(String name) throws Exception {
        if (name.equalsIgnoreCase(PAGE_TAG)) {
            _multi.addPage(_currentPage);
            _currentPage = null;
        }
        else if (name.equalsIgnoreCase(MULTIPAGE_TAG)) {
        }
        else {
            String err = "Error: unknown end element \"" + name + "\"";
            throw new RuntimeException(err);
        }
    }


    /**
     * Handle the start of an element.  If this is the start of a
     * page, create a Page object and set its label (retrieve from
     * _currentAttributes table)
     */
    public void startElement(String name)
            throws Exception {
        if (name.equalsIgnoreCase(MULTIPAGE_TAG)) {
        }
        else if (name.equalsIgnoreCase(PAGE_TAG)) {
            String label = (String)_currentAttributes.get(PAGE_TITLE_TAG);
            //page number is currently ignored.
            String numstring = (String)_currentAttributes.get(PAGE_NUM_TAG);
            int num = Integer.valueOf(numstring).intValue();
            _currentPage = new BasicPage(_multi, label);
        }
        else {
            String err = "Error: unknown start element \"" + name + "\"";
            throw new RuntimeException(err);
        }
    }

    /**
     * Resolve an external entity.  If the first argument is the name
     * of the MoML PUBLIC DTD ("-//UC Berkeley//DTD multipage 1//EN"),
     * then return a StringReader that will read the locally cached
     * version of this DTD (the public variable
     * DTD_1). Otherwise, return null, which has the effect of
     * deferring to Aelfred for resolution of the URI.  Derived
     * classes may return a modified URI (a string), an
     * InputStream, or a Reader.  In the latter two cases, the input
     * character stream is provided.
     *
     * @param publicID The public identifier, or null if none was supplied.
     * @param systemID The system identifier.
     * @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        if (publicID != null && publicID.equals(PUBLIC_ID)) {
            // This is the generic MoML DTD.
            return new StringReader(DTD_1);
        } else {
            return null;
        }
    }

    /**
     * Parse the file (from reader) into the given multipage data structure.
     *
     */
    public void parse (Reader reader, MultipageModel multi)
            throws java.lang.Exception  {
        _multi = multi;

        // create the parser
        _parser = new XmlParser();
        _parser.setHandler(this);
        _parser.parse(null, null, reader);
    }
}


