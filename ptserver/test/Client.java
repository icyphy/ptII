/*
 A  dummy client for the hessian service. 

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

package ptserver.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.PtolemyServer;
import ptserver.control.IServerManager;

import com.caucho.hessian.client.HessianProxyFactory;

public class Client {

	public static final ResourceBundle CONFIG = PtolemyServer.CONFIG;

	public static void main(String[] args) {

		try {

			//PtolemyServer _ptolemyServer = PtolemyServer.getInstance();
			HessianProxyFactory proxyFactory = new HessianProxyFactory();
			String servletUrl = String.format("http://%s:%s%s", "localhost",
					CONFIG.getString("SERVLET_PORT"),
					CONFIG.getString("SERVLET_PATH"));

			IServerManager _servletProxy = (IServerManager) proxyFactory
					.create(IServerManager.class, servletUrl);

			String[] models = _servletProxy.getModelListing();;
			
			
			if (models == null) { 
				System.out.println("models is empty");
				} else { 
				for (int i=0; i<models.length; i++) { 
				// Get filename of file or directory 
				String filename = models[i]; 
				System.out.println(filename);
				}
				}

			
			InputStream in = _servletProxy.downloadModel("model3.xml");
			File f2 = new File("c:/downloaded/model3.xml");

			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

			System.out.println("File copied.");

		} catch (IllegalActionException e) {

			e.printStackTrace();
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
