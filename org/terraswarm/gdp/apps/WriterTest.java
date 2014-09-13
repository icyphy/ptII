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


import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.ptolemy.fmi.NativeSizeT;

import org.terraswarm.gdp.EP_STAT;
//import org.terraswarm.gdp.Event2Library;
//import org.terraswarm.gdp.Event2Library.evbuffer;
import org.terraswarm.gdp.Gdp10Library;
import org.terraswarm.gdp.Gdp10Library.gdp_gcl_t;
import org.terraswarm.gdp.ep_stat_to_string;
//import org.terraswarm.gdp.gdp_datum;

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
        // Will be false on the Mac.
        System.out.println("Native.isProtected(): " + Native.isProtected());

        // Was:	gdp_gcl_t *gclh;
	//PointerByReference gclh = new PointerByReference();
        //System.out.println("new gclh: " + gclh);
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

        // System.out.println("WriterTest: argv.length: " + argv.length + ", argc: " + argc);

        if (argc > 0) {
            xname = argv[argv.length-argc];
            argc--;
        } 
	if (argc != 0 || (append && xname == null)) {
            System.err.println("Usage: WriterTest [-D dbgspec] [-a] [<gcl_name>]\n"
                    + "  (name is required for -a)");
            System.exit(64 /* EX_USAGE from /usr/includes/sysexits.h */);
	}

        System.err.println("About to initialize the GDP.");
	estat = Gdp10Library.INSTANCE.gdp_init();
	if (!EP_STAT_ISOK(estat)) {
            System.err.println("GDP Initialization failed");
            _fail0(estat);
	}
        System.err.println("GDP Initialized.");

	if (xname == null) {
            // create a new GCL handle
            System.err.println("About to create a new handle.");

            // Was: estat = gdp_gcl_create(NULL, &gclh);
            // gdp.h declared: extern EP_STAT gdp_gcl_create(gcl_name_t, gdp_gcl_t **);
            //estat = Gdp10Library.INSTANCE.gdp_gcl_create((ByteBuffer)null, gclh);
            PointerByReference gclhByReference = new PointerByReference();
            estat = Gdp10Library.INSTANCE.gdp_gcl_create((ByteBuffer)null, gclhByReference);
            gclh = gclhByReference.getValue();
            System.err.println("Handle created: " + estat);
            System.out.println("2 gclh: " + gclh);
	} else {
            System.err.println("About to parse " + xname);
            Gdp10Library.INSTANCE.gdp_gcl_parse_name(xname, gcliname);
            if (append) {
                System.err.println("About to call gdp_gcl_open()");
                //estat = Gdp10Library.INSTANCE.gdp_gcl_open(gcliname, Gdp10Library.gdp_iomode_t.GDP_MODE_AO, gclh);
                PointerByReference gclhByReference = new PointerByReference();
                estat = Gdp10Library.INSTANCE.gdp_gcl_open(gcliname, Gdp10Library.gdp_iomode_t.GDP_MODE_AO, gclhByReference);
                gclh = gclhByReference.getValue();
            } else {
                System.err.println("About to call gdp_gcl_create()");
                PointerByReference gclhByReference = new PointerByReference();
                estat = Gdp10Library.INSTANCE.gdp_gcl_create(gcliname, gclhByReference);
                gclh = gclhByReference.getValue();
            }
	}
        System.err.println("About to check error code after either creating a new handle, gdp_gcl_open() or gdp_gcl_create()");
	// EP_STAT_CHECK(estat, goto fail0);
        if (!EP_STAT_ISOK(estat)) {
            _fail0(estat);
        }

	//Gdp10Library.INSTANCE.gdp_gcl_print(gclh, stdout, 0, 0);
        System.out.print("GCL" + System.identityHashCode(gclh) + ":");

        // FIXME: Need to allocate something 44 chars long (GDP_GCL_PNAME_LEN in gdp.h)
        // Gdp10Library.INSTANCE.gdp_gcl_printable_name(gclh, nbuf);
        // Print nbuf


	System.out.println("Starting to read input.");

        System.out.println("About to create a gdp_datum.");
	PointerByReference datum = Gdp10Library.INSTANCE.gdp_datum_new();
        System.out.println("Done creating a gdp_datum");
        // Invoke with -Djna.dump_memory=true
        System.out.println("datum: " + datum);

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

                System.out.println("About to call gdp_datum_getbuf()");
                PointerByReference dbuf = Gdp10Library.INSTANCE.gdp_datum_getbuf(datum);
                System.out.println("About to call gdp_buf_write(): pointer: " + pointer + "pointer.getString(): " + pointer.getString(0));
                Gdp10Library.INSTANCE.gdp_buf_write(dbuf, pointer, new NativeSizeT(line.length()));
                
                System.out.println("About to call gdp_gcl_publish()");
                System.out.println("gclh: " + gclh);
                System.out.print("datum: " + datum);
                _gdp_datum_print(datum/*, stdout*/);
		estat = Gdp10Library.INSTANCE.gdp_gcl_publish(gclh, datum);
                if (!EP_STAT_ISOK(estat)) {
                    PointerByReference gclhByReference = new PointerByReference();
                    _fail1(estat, gclhByReference);
                    gclh = gclhByReference.getValue();
                }
                // Instead of calling the gdp_datum_print() method in C, we implement our own.
                //gdp_datum_print(datum, stdout);
                System.out.println("About to call gdp_datum_print()");
                _gdp_datum_print(datum/*, stdout*/);

            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
	Gdp10Library.INSTANCE.gdp_datum_free(datum);
        _fail0(estat);
    }

    private static void _fail1(EP_STAT estat, PointerByReference gclh) {
	Gdp10Library.INSTANCE.gdp_gcl_close(gclh);
        _fail0(estat);
    }

    private static void _fail0(EP_STAT estat) {
        // FIXME: EP_STAT_ISOK is a macro in the original c code.  See ../_jnaerator.macros.cpp.
        if (EP_STAT_ISOK(estat)) {
            estat = EP_STAT_OK;
        }

        // FIXME: writer-test.c has:
	// fprintf(stderr, "exiting with status %s\n",
	//		ep_stat_tostr(estat, buf, sizeof buf));
        // I have no idea what to do with ep_stat_tostr(), so we just print
	System.err.println("exiting with status " + estat + ", code: " + estat.code);

	System.exit(EP_STAT_ISOK(estat) ? 0 : 1);
    }

    /** Print the datum to standard out.  This is a port of
     *  gdp_datum_print from gdp/gdp_api.c by Eric Allman.
     *  @param datum The datum to be printed.
     */   
    private static void _gdp_datum_print(/*gdp_datum*/PointerByReference datum) {
        
        Pointer d = null;
        NativeSizeT length = new NativeSizeT();
        length.setValue(-1);
        if (datum == null) {
            System.out.println("null datum");
        }
        System.out.print("GDP record " + 
                Gdp10Library.INSTANCE.gdp_datum_getrecno(datum) + ", ");
        PointerByReference dbuf = Gdp10Library.INSTANCE.gdp_datum_getbuf(datum);
        if (dbuf == null) {
            System.out.print("no data");
        } else {
            // In gdp_api.c, gdp_datum_print() calls:
            // l = gdp_buf_getlength(datum->dbuf);
            length = Gdp10Library.INSTANCE.gdp_buf_getlength(dbuf);
            System.out.print("len " + length);

            // In gdp_api.c, this method calls:
            // d = gdp_buf_getptr(datum->dbuf, l);
            // gdp_buf.h has:
            // #define gdp_buf_getptr(b, z)	evbuffer_pullup(b, z)
            // So, we would need to call evbuffer_pullup() here.

            // A different idea would be to have a gdp_buf.c method
            // that calls evbuffer_pullup so that we don't need to run
            // JNA on that class.

            //d = Event2Library.INSTANCE.evbuffer_pullup(new evbuffer(datum.dbuf.getValue()), new NativeLong(length.longValue()));
            d = Gdp10Library.INSTANCE.gdp_buf_getptr(dbuf, new NativeSizeT(length.longValue()));
        }
        //Gdp10Library.INSTANCE.gdp_buf_getts(dbuf, new NativeSizeT(length.longValue()));
        //if (datum.ts.tv_sec != Long.MIN_VALUE) {
            System.out.print(", timestamp ");
            System.out.print("FIXME");
            //ep_time_print(&datum->ts, fp, true);
            //} else {
            //System.out.print(", no timestamp ");
            //}
        if (length.longValue() > 0) {
            // gdp_api.c has
            //fprintf(fp, "\n	 %s%.*s%s", EpChar->lquote, l, d, EpChar->rquote);
            System.out.println("\n  \"" + length + d + "\"");
        }
    }

    /** Return true if the status code is ok.
     *  Based on ep_stat.h, Copyright Eric Allman, See ep_license.htm
     *  @param estat The status code.
     *  @return true if the code is less than EP_STAT_SEV_WARN
     */
    public static boolean EP_STAT_ISOK(EP_STAT estat) {
        long code = estat.code.longValue();
        //(((c).code >> _EP_STAT_SEVSHIFT) & ((1UL << _EP_STAT_SEVBITS) - 1))
        long EP_STAT_SEVERITY = (code >> Gdp10Library._EP_STAT_SEVSHIFT) & ((1l << Gdp10Library._EP_STAT_SEVBITS) - 1);

        //System.out.println("EP_STAT_ISOK(): code: " + code + ", EP_STAT_SEVERITY: " + EP_STAT_SEVERITY 
        //        + ", EP_STAT_SEV_WARN: " + EP_STAT_SEV_WARN 
        //        + "EP_STAT_SEVERITY < EP_STAT_SEV_WARN: " + (EP_STAT_SEVERITY < EP_STAT_SEV_WARN));
        return EP_STAT_SEVERITY < Gdp10Library.EP_STAT_SEV_WARN;
    }
    
    public static EP_STAT EP_STAT_NEW(int s, int r, int m, int d) {
        long code = ((((s) & ((1l << Gdp10Library._EP_STAT_SEVBITS) - 1)) << Gdp10Library._EP_STAT_SEVSHIFT) | 
                (((r) & ((1l << Gdp10Library._EP_STAT_REGBITS) - 1)) << Gdp10Library._EP_STAT_REGSHIFT) | 
                (((m) & ((1l << Gdp10Library._EP_STAT_MODBITS) - 1)) << Gdp10Library._EP_STAT_MODSHIFT) |
                (((d) & ((1l << Gdp10Library._EP_STAT_DETBITS) - 1))));
        return new EP_STAT( new NativeLong(code));
    }                    

    // #define EP_STAT_OK		EP_STAT_NEW(EP_STAT_SEV_OK, 0, 0, 0)
    public static EP_STAT EP_STAT_OK = EP_STAT_NEW(Gdp10Library.EP_STAT_SEV_OK, 0, 0, 0);
}
