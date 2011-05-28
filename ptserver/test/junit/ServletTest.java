/* Test cases for the Hessian servlets

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


import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ptserver.PtolemyServer2;
import ptserver.control.IServerManager;
import ptserver.control.Ticket;

import com.caucho.hessian.client.HessianProxyFactory;

///////////////////////////////////////////////////////////////////
//// ServletTest

/** This class is responsible testing the Hessian servlet
 * 
 * @author pdf
 * @version $Id$
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class ServletTest {

    @Before
    public void setUp() throws Exception {
        _ptolemyServer = PtolemyServer2.getInstance();
        
        HessianProxyFactory factory = new HessianProxyFactory();
     
        _servletProxy = (IServerManager) factory.create(IServerManager.class, _servletURL);
    }
  
    @Test
    public void StartThread() {
        URL url = null;
        Ticket ticket = null;

        try {
            url = new URL(_testModelFileURL);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            ticket = _servletProxy.open(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        assertNotNull(ticket);
        assertNotNull(ticket.getTicketID());
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables
    private PtolemyServer2 _ptolemyServer;
    private IServerManager _servletProxy;
    
    private String _testModelFileURL = "file:///C:/Users/Peter/Workspace/ptII/ptserver/test/rampmodel.xml";
    private String _servletURL = "http://localhost:8080/ServerManager";
}
