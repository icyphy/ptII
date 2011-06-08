/*
 Test cases for the file query and download functionality of the servlet. 

 Copyright (c) 2011 The Regents of the University of California.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ResourceBundle;

import org.junit.Test;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.PtolemyServer;
import ptserver.control.IServerManager;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// FileDownloadTest

/** Test the ability to get a listing of available model files
 *  and download the XML data.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class FileDownloadTest {

    public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

    @Test
    public void FileDownload() {

        try {
            // Set up the servlet proxy.
            PtolemyServer _ptolemyServer = PtolemyServer.getInstance();
            HessianProxyFactory proxyFactory = new HessianProxyFactory();
            String servletUrl = String.format("http://%s:%s%s", "localhost",
                    CONFIG.getString("SERVLET_PORT"),
                    CONFIG.getString("SERVLET_PATH"));

            IServerManager _servletProxy = (IServerManager) proxyFactory
                    .create(IServerManager.class, servletUrl);

            // Get listing of remote model files.
            String[] models = _servletProxy.getModelListing();
            assertNotNull(models);
            assertNotSame(0, models.length);

            for (String model : models)
                for (int i = 0; i < models.length; i++) {
                    System.out.println(model);
                }

            // Download the remote file.
            String contents = new String(
                    _servletProxy.downloadModel("addermodel.xml"));
            assertNotNull(contents);
            assertNotSame(0, contents.length());

            // Write contents of the downloaded file.
            System.out.println(contents);

            // Cleanup running processes.
            _ptolemyServer.shutdown();
            _ptolemyServer = null;
        } catch (IllegalActionException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
