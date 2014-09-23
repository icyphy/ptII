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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.ptolemy.fmi.NativeSizeT;
import org.terraswarm.gdp.EP_STAT;
import org.terraswarm.gdp.Gdp10Library;
import org.terraswarm.gdp.GdpUtilities;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
    /** Write to the GCL.
     *  <p>Usage:<code>
     *  DYLD_LIBRARY_PATH=${PTII}/org/terraswarm/gdp/src/gdp/libs:/usr/local/lib LD_LIBRARY_PATH=${PTII}/org/terraswarm/gdp/src/gdp/libs:/usr/local/lib $(JAVA) -Djna.debug_load=true -Djna.dump_memory=true -classpath $(CLASSPATH) org.terraswarm.gdp.apps.ReaderTest elvis</code></p>
     *  
     *  <p>The arguments below are optional:</p>
     *
     *  <p><code>-D <i>debuggingSpecification</i></code>, where an example of <i>debuggingSpecifiction</i> is,
     *  <code>gdp.api=100</code>.  See the gdp .c files for debugging specifications</p>.
     *
     *  <p><code>-a</code> FIXME: what does this do?</p>
     *
     *  <p><code><i>gcl_name</i></code> The name of the gcl.  This string is used with ReaderTest</p>
     */
    public static void main(String [] argv) throws Throwable {
        // See
        // https://twall.github.io/jna/4.1.0/overview-summary.html#crash-protection
        // "It is not uncommon when defining a new library and writing
        // tests to encounter memory access errors which crash the
        // VM. These are often caused by improper mappings or invalid
        // arguments passed to the native library. To generate Java
        // errors instead of crashing the VM, call
        // Native.setProtected(true). Not all platforms support this
        // protection; if not, the value of Native.isProtected() will
        // remain false."
        Native.setProtected(true);

        // On the Mac, isProtected() will probably return false.
        _debug("Native.isProtected(): " + Native.isProtected());

        // Was:	gdp_gcl_t *gclh;
	//PointerByReference gclh = new PointerByReference();
        //_debug("new gclh: " + gclh);
        //gdp_gcl_t gclhReally = new gdp_gcl_t(gclh.getValue());
        Pointer gclh = null;
        

	// Was: gcl_name_t gcliname;
        // BTW - gcl_name_t is defined in gdp/gdp/gdp.h:
        // typedef uint8_t                            gcl_name_t[32];
        ByteBuffer gcliname = ByteBuffer.allocate(32);

	int opt;
	EP_STAT estat;
	boolean append = false;
	String xname = null;
	String buf = "";

        int argc = argv.length;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-a")) {
                append = true;
                argc--;
            } else if (argv[i].equals("-D")) {
                argc--;
                Gdp10Library.INSTANCE.ep_dbg_set(argv[i+1]);
                argc--;
            }
        }

         _debug("WriterTest: argv.length: " + argv.length + ", argc: " + argc);

        if (argc > 0) {
            xname = argv[argv.length - 1];
            argc--;
        } 
	if (argc != 0 || (append && xname == null)) {
            System.err.println("Usage: WriterTest [-D dbgspec] [-a] [<gcl_name>]\n"
                    + "  (name is required for -a)");
            _debug("argc: " + argc + ", append: " + append + ", xname: " + (xname == null ? "null" : xname));
            System.exit(64 /* EX_USAGE from /usr/includes/sysexits.h */);
	}

        _debug("About to initialize the GDP.");
	estat = Gdp10Library.INSTANCE.gdp_init();
	if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            System.err.println("GDP Initialization failed");
            _fail0(estat);
	}
        _debug("GDP Initialized.");

	if (xname == null) {
            // create a new GCL handle
            _debug("About to create a new handle.");

            // Was: estat = gdp_gcl_create(NULL, &gclh);
            // gdp.h declared: extern EP_STAT gdp_gcl_create(gcl_name_t, gdp_gcl_t **);
            //estat = Gdp10Library.INSTANCE.gdp_gcl_create((ByteBuffer)null, gclh);
            PointerByReference gclhByReference = new PointerByReference();
            estat = Gdp10Library.INSTANCE.gdp_gcl_create((ByteBuffer)null, gclhByReference);
            gclh = gclhByReference.getValue();
            _debug("Handle created: " + estat);
            _debug("2 gclh: " + gclh);
	} else {
            _debug("About to parse " + xname);
            Gdp10Library.INSTANCE.gdp_gcl_parse_name(xname, gcliname);
            if (append) {
                _debug("About to call gdp_gcl_open()");
                //estat = Gdp10Library.INSTANCE.gdp_gcl_open(gcliname, Gdp10Library.gdp_iomode_t.GDP_MODE_AO, gclh);
                PointerByReference gclhByReference = new PointerByReference();
                estat = Gdp10Library.INSTANCE.gdp_gcl_open(gcliname, Gdp10Library.gdp_iomode_t.GDP_MODE_AO, gclhByReference);
                gclh = gclhByReference.getValue();
            } else {
                _debug("About to call gdp_gcl_create()");
                PointerByReference gclhByReference = new PointerByReference();
                estat = Gdp10Library.INSTANCE.gdp_gcl_create(gcliname, gclhByReference);
                gclh = gclhByReference.getValue();
            }
	}
        _debug("About to check error code after either creating a new handle, gdp_gcl_open() or gdp_gcl_create()");
	// EP_STAT_CHECK(estat, goto fail0);
        if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            _fail0(estat);
        }

	//Gdp10Library.INSTANCE.gdp_gcl_print(gclh, stdout, 0, 0);
        _debug("GCL" + System.identityHashCode(gclh) + ":");

        // FIXME: Need to allocate something 44 chars long (GDP_GCL_PNAME_LEN in gdp.h)
        // Gdp10Library.INSTANCE.gdp_gcl_printable_name(gclh, nbuf);
        // Print nbuf


	System.out.println("Starting to read input.");

        _debug("About to create a gdp_datum.");
	PointerByReference datum = Gdp10Library.INSTANCE.gdp_datum_new();
        _debug("Done creating a gdp_datum");
        // Invoke with -Djna.dump_memory=true
        _debug("datum: " + datum);

        BufferedReader bufferedReader = null;
        try {
           bufferedReader = new BufferedReader(new InputStreamReader(System.in));
           String line;
           // 200 is a magic number from writer-test and seems wrong.
           final int bufferLength = 200;
           while((line = bufferedReader.readLine())!=null){
                System.out.println("Got input \"" +  line + "\"");

                // FIXME: gdp/gdp_buf.h has
                // #define gdp_buf_write(b, i, z)	evbuffer_add(b, i, z)
                // evbuffer_add is declared in /usr/local/include/event2/buffer.h
                //gdp_buf_write(datum->dbuf, buf, buf.length());

                // See FMULibrary for similar code.
                // FIXME: we should probably have alloc and free methods and avoid lieaks.
                if (line.length() > bufferLength) {
                    throw new Exception("The length of the line \"" + line
                            + "\" is greater than " + bufferLength );
                }
                Memory memory = new Memory(bufferLength);
                // FIXME: not sure about alignment.
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);
                pointer.setString(0, line);

                _debug("About to call gdp_datum_getbuf()");
                PointerByReference dbuf = Gdp10Library.INSTANCE.gdp_datum_getbuf(datum);
                _debug("About to call gdp_buf_write(): pointer: " + pointer + "pointer.getString(): " + pointer.getString(0));
                Gdp10Library.INSTANCE.gdp_buf_write(dbuf, pointer, new NativeSizeT(line.length()));
                
                _debug("About to call gdp_gcl_publish()");
                _debug("gclh: " + gclh);
                _debug("datum: " + datum);
                GdpUtilities.gdp_datum_print(datum/*, stdout*/);
		estat = Gdp10Library.INSTANCE.gdp_gcl_publish(gclh, datum);
                if (!GdpUtilities.EP_STAT_ISOK(estat)) {
                    PointerByReference gclhByReference = new PointerByReference();
                    _fail1(estat, gclhByReference);
                    gclh = gclhByReference.getValue();
                }
                // Instead of calling the gdp_datum_print() method in C, we implement our own.
                //gdp_datum_print(datum, stdout);
                _debug("About to call gdp_datum_print()");
                GdpUtilities.gdp_datum_print(datum/*, stdout*/);

            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
	Gdp10Library.INSTANCE.gdp_datum_free(datum);
        _fail0(estat);
    }


    /** Close the GCL handle and call exit.
     *  @parameter estat The libep Enhanced Portability status code.
     *  @parameter gclh The GCL handle.
     */
    private static void _fail1(EP_STAT estat, PointerByReference gclh) {
        // We use a separate method here so that we can mimic the 
        // structure of the original writer-test.c.
	Gdp10Library.INSTANCE.gdp_gcl_close(gclh);
        _fail0(estat);
    }

    /** Print a status message and exit.
     *  @parameter estat The libep Enhanced Portability status code.
     */
    private static void _fail0(EP_STAT estat) {
        // We use a separate method here so that we can mimic the 
        // structure of the original writer-test.c.
        if (GdpUtilities.EP_STAT_ISOK(estat)) {
            estat = GdpUtilities.EP_STAT_OK;
        }

        // FIXME: writer-test.c has:
	// fprintf(stderr, "exiting with status %s\n",
	//		ep_stat_tostr(estat, buf, sizeof buf));
        // I have no idea what to do with ep_stat_tostr(), so we just print
	System.err.println("exiting with status " + estat + ", code: " + estat.code);

	System.exit(GdpUtilities.EP_STAT_ISOK(estat) ? 0 : 1);
    }

    /** Optionally print a message.
     *  @param the message
     */   
    private static void _debug(String message) {
        //System.out.println(message);
    }
}
