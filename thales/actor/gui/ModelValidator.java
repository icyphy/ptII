/*
 * Created on 01 sept. 2003
 *
 * If the present is considered as mature and necessary, it has to be merged
 * with the MoML parser.
 *
 * @ProposedRating Red (jerome.blanc@thalesgroup.com)
 * @AcceptedRating
 */
package thales.actor.gui;

import ptolemy.actor.gui.MoMLApplication;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.DOMWriterImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Titre : ModelValidator</p>
 * <p>Description : A simple parser that delete empty parameters before opening
 * a MoML file. Used to avoid MoMLparser bugs on older Ptolemy MoML files.</p>
Copyright (c) 2003 THALES.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THALES BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THALES HAS BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THALES SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
BASIS, AND THALES HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * <p>Sociâtâ : Thales Research and technology</p>
 * @author Jârüme Blanc & Benoit Masson
 * 01 sept. 2003
 */
public class ModelValidator {

        private DOMParser _parser;
        private URL _fileURL;

        public void filter(URL fileURL) {
                if (fileURL != null) {
                        String externalForm = fileURL.toExternalForm();
                        if (externalForm.endsWith(".moml")
                                || externalForm.endsWith(".xml")) {
                                _fileURL = fileURL;

                                if (_parser == null) {
                                        _parser = new DOMParser();
                                } else {
                                        _parser.reset();
                                }

                                try {
                                        _parser.setEntityResolver(new MyEntityResolver());
                                        _parser.parse(new InputSource(_fileURL.openStream()));
                                } catch (SAXException e) {
                                        e.printStackTrace();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }

                                if (_parser.getDocument() != null) {
                                        filter();
                                }
                        }
                }

        }
        /**
         *
         */
        private void filter() {
                boolean modified = false;
                Document doc = _parser.getDocument();
                NodeList properties = doc.getElementsByTagName("property");
                for (int i = 0; i < properties.getLength(); ++i) {
                        Node aNode = properties.item(i);
                        NamedNodeMap attributes = aNode.getAttributes();
                        if (attributes.getNamedItem("value") == null) {
                                //le noeud est eligible
                                String name = attributes.getNamedItem("name").getNodeValue();
                                if (isEligible(name, aNode)) {
                                        //il faut supprimer le noeud
                                        Node parent = aNode.getParentNode();
                                        parent.removeChild(aNode);
                                        modified = true;
                                }
                        }
                }

                if (modified) {
                        DOMWriter writer = new DOMWriterImpl();
                        try {
                                writer.writeNode(new FileOutputStream(_fileURL.getFile()), doc);
                                System.out.println(_fileURL.getFile() + " file filtered ...");
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        }
                }
        }

        /**
         * @param name
         * @return
         */
        private boolean isEligible(String name, Node node) {
                boolean answer = false;

                if (name.equals("_vergilSize")
                        || name.equals("_vergilLocation")
                        || name.equals("_location")) {
                        answer = true;
                }

                return answer;
        }

        private class MyEntityResolver implements EntityResolver {
                /* (non-Javadoc)
                 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
                 */
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {

                        URL url = MoMLApplication.specToURL("ptolemy/moml/moml.dtd");

                        return new InputSource(url.openStream());
                }
        }

}
