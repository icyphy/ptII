/*
 Test cases for the file query and download functionality of the servlet.

 Copyright (c) 2011-2016 The Regents of the University of California.
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

package ptserver.test.junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.caucho.hessian.client.HessianProxyFactory;

import ptserver.control.IServerManager;
import ptserver.control.PtolemyServer;

///////////////////////////////////////////////////////////////////
//// FileDownloadTest

/** Test the ability to get a listing of available model files
 *  and download the XML data.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class FileDownloadTest {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Access the ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set up the initial singleton reference and Hessian proxy factory
     *  that will be used within the JUnit test cases.
     *  @exception Exception If there is an error creating the Hessian proxy.
     */
    @Before
    public void setup() throws Exception {
        _ptolemyServer = PtolemyServer.getInstance();

        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setUser("guest");
        factory.setPassword("guest");

        _servletProxy = (IServerManager) factory.create(
                IServerManager.class,
                String.format("http://%s:%s%s", "localhost",
                        CONFIG.getString("SERVLET_PORT"), "/"
                                + PtolemyServer.SERVLET_NAME));
    }

    /** Get the listing of models available on the server.
     *  @exception Exception If there was an error retrieving the
     *  model files on the server.
     */
    @Test
    public void getModelListing() throws Exception {
        String[] modelUrls = _servletProxy.getModelListing();
        assertNotNull(modelUrls);
        assertNotSame(0, modelUrls.length);
        //
        //        for (String modelUrl : modelUrls) {
        //            System.out.println(modelUrl);
        //        }
    }

    /** Download a model from the Ptolemy server.
     *  @exception Exception If there was an error downloading the
     *  model XML data from the server.
     */
    @Test
    public void getModelXMLData() throws Exception {
        String[] modelUrls = _servletProxy.getModelListing();
        assertNotNull(modelUrls);
        assertNotSame(0, modelUrls.length);

        String contents = new String(_servletProxy.downloadModel(modelUrls[0]));
        assertNotNull(contents);
        assertNotSame(0, contents.length());

        // Write contents of the downloaded file.
        //        System.out.println(contents);
    }

    /** Call the shutdown() method on the singleton and destroy all
     *  references to it.
     *  @exception Exception If there was an error shutting down the broker or
     *  servlet.
     */
    @After
    public void shutdown() throws Exception {
        _ptolemyServer.shutdown();
        _ptolemyServer = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Handle to the Ptolemy server singleton.
     */
    private PtolemyServer _ptolemyServer;

    /** Handle to the Hessian proxy.
     */
    private IServerManager _servletProxy;
}
