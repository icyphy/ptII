/* A GDP Writer Test application.

   Copyright (c) 2014 The Regents of the University of California.
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

package org.terraswarm.gdp.apps;

import com.sun.jna.Pointer;
import com.sun.jna.NativeLibrary;

import org.ptolemy.fmi.NativeSizeT;

import org.terraswarm.gdp.EP_STAT;
import org.terraswarm.gdp.GdpLibrary;
//import org.terraswarm.gdp.GdpLibraryImplementation;
import org.terraswarm.gdp.GdpLibrary.gdp_gcl_t;
import org.terraswarm.gdp.ep_stat_to_string;
import org.terraswarm.gdp.gdp_datum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ptolemy.util.StringUtilities;

/** Create a GCL.
    
    Invoke the main method of this class without arguments and it will
    create a random 256-bit name.  Run it with an argument and the
    value of the argument will be used to sha-256'd and used to create
    the internal name.  Du this without arguments and it will create a
    (random) 256-bit name, or with an argument which will be sha-256'd
    to create the internal name.  Text that is written to stdin will
    be written to the GCL.

    @author Christopher Brooks, based on the gdp/apps/writer-test.c by Eric Allman.
    @version $Id: AccessorOne.java 69931 2014-08-29 23:36:38Z eal $
    @since Ptolemy II 10.0
    @Pt.ProposedRating Red (eal)
    @Pt.AcceptedRating Red (cxh)
*/
public class WriterTest {
//     public static void main(String [] argv) throws Throwable {

//         _gdpLibrary = new GdpLibraryImplementation();
// 	gdp_gcl_t gclh;
// 	Pointer /*gcl_name_t*/ gcliname;
// 	int opt;
// 	EP_STAT estat;
// 	boolean append = false;
// 	String xname = null;
// 	String buf = "";

//         String ptII = StringUtilities.getProperty("ptolemy.ptII.dir");

//         // FIXME: use .so for linux:
//         NativeLibrary nativeLibrary = NativeLibrary.getInstance(ptII + "/lib/libgdp.dylib");

//         int argc = argv.length;
//         for (int i = 0; i < argv.length; i++) {
//             if (argv[i].equals('a')) {
//                 append = true;
//                 argc--;
//             } else if (argv[i].equals('D')) {
//                 argc--;
//                 _gdpLibrary.ep_dbg_set(argv[i+1]);
//             }
//         }

//         if (argc > 0) {
//             xname = argv[argc];
//             argc--;
//         } 
// 	if (argc != 0 || (append && xname == null)) {
//             System.err.println("Usage: WriterTest [-D dbgspec] [-a] [<gcl_name>]\n"
//                     + "  (name is required for -a)");
//             System.exit(64 /* EX_USAGE from /usr/includes/sysexits.h */);
// 	}

// 	estat = _gdpLibrary.gdp_init();
// 	if (/*!EP_STAT_ISOK(estat)*/ estat.code.intValue() != 0) {
//             System.err.println("GDP Initialization failed");
//             _fail0(estat);
// 	}

// 	if (xname == null) {
//             // create a new GCL handle
//             estat = _gdpLibrary.gdp_gcl_create(null, gclh);
// 	} else {
//             _gdpLibrary.gdp_gcl_parse_name(xname, gcliname);
//             if (append) {
//                 estat = _gdpLibrary.gdp_gcl_open(gcliname, GdpLibrary.gdp_iomode_t.GDP_MODE_AO, gclh);
//             } else {
//                 estat = _gdpLibrary.gdp_gcl_create(gcliname, gclh);
//             }
// 	}
// 	// EP_STAT_CHECK(estat, goto fail0);
//         if (estat.code.intValue() != 0) {
//             _fail0(estat);
//         }

// 	_gdpLibrary.gdp_gcl_print(gclh, stdout, 0, 0);
// 	System.out.println("Starting to read input.");

// 	gdp_datum_t datum = _gdpLibrary.gdp_datum_new();


//         BufferedReader bufferedReader = null;
//         try {
//            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//             while((buf=bufferedReader.readLine())!=null){
//                 System.out.println("Got input \"" +  buf + "\"");
//                 // FIXME: gdp/gdp_buf.h has
//                 // #define gdp_buf_write(b, i, z)	evbuffer_add(b, i, z)
//                 // evbuffer_add is declared in /usr/local/include/event2/buffer.h
//                 //gdp_buf_write(datum->dbuf, buf, buf.length());
//                 evbuffer_add(datum.dbuf, buf, buf.length());
//                 estat = gdp_gcl_publish(gclh, datum);
//                 if (estat.code.intValue() != 0) {
//                     _fail1(estat, gclh);
//                 }
//                 gdp_datum_print(datum, stdout);
//             }
//         } finally {
//             if (bufferedReader != null) {
//                 bufferedReader.close();
//             }
//         }
// 	gdp_datum_free(datum);
//         _fail0(estat);
//     }

//     private static int _fail1(EP_STAT estat, gdp_gcl_t gclh) {
// 	_gdpLibrary.gdp_gcl_close(gclh);
//         return _fail0(estat);
//     }

//     private static int _fail0(EP_STAT estat) {
//         // FIXME: EP_STAT_ISOK is a macro in the original c code.  See ../_jnaerator.macros.cpp.
//         //if (EP_STAT_ISOK(estat)) {
//         //    estat = EP_STAT_OK;
//         //}
//         // FIXME: Need to allocate memory here
//         Pointer buf = new Pointer();
// 	System.err.println("exiting with status " 
//                 + _gdpLibrary.ep_stat_tostr(estat, buf, new NativeSizeT(buf.length())));
// 	return /*!EP_STAT_ISOK(estat)*/ (estat.code.intValue() == 0 ? 1 : 0);
//     }

//     private static GdpLibrary _gdpLibrary;
}
