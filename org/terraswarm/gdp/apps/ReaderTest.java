/* A GDP Reader Test application.

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

import java.nio.ByteBuffer;

import org.terraswarm.gdp.EP_STAT;
import org.terraswarm.gdp.Gdp10Library;
import org.terraswarm.gdp.GdpUtilities;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
    Read from a GCL

    From gdp/README (lightly edited):

    <p>"Create your first GCL using [WriterTest].  You can run this
    without arguments and it will create a (random) 256-bit name,
    or with an argument which will be sha-256'd to create the
    internal name.  It will accept text from the input and write
    that text to the GCL."</p>

    <p>" Check success by running apps/reader-test giving it the
    internal (256-bit) name printed by writer-test.  If you
    gave writer-test an argument you can use that instead.</p>

    @author Christopher Brooks, based on the gdp/apps/writer-test.c by Eric Allman.
    @version $Id$
    @since Ptolemy II 10.0
    @Pt.ProposedRating Red (eal)
    @Pt.AcceptedRating Red (cxh)
 */
public class ReaderTest {
    // This code follows the naming conventions of the original .c file.

    /** Read a gcl.
     *  @param gclh The handle to the GCL
     *  @param firstrec the record number of the first record.
     *  @return the status.
     */
    public static EP_STAT do_read(Pointer gclh, long /*gdp_recno_t*/firstrec) {
        // Was: EP_STAT do_read(gdp_gcl_t *gclh, gdp_recno_t firstrec)
        EP_STAT estat;
        long /*gdp_recno_t*/recno = firstrec;
        // Was: gdp_datum_t *datum = gdp_datum_new();
        PointerByReference datum = Gdp10Library.INSTANCE.gdp_datum_new();

        if (recno <= 0) {
            recno = 1;
        }
        for (;;) {
            // Was: estat = gdp_gcl_read(gclh, recno, datum);
            estat = Gdp10Library.INSTANCE.gdp_gcl_read(gclh, recno, datum);
            // Was: EP_STAT_CHECK(estat, break);
            if (!GdpUtilities.EP_STAT_ISOK(estat)) {
                break;
            }

            System.out.print(" >>> ");
            // Was: gdp_datum_print(datum, stdout);
            GdpUtilities.gdp_datum_print(datum/*, stdout*/);
            recno++;

            // flush any left over data
            // Was: if (gdp_buf_reset(gdp_datum_getbuf(datum)) < 0)
            if (Gdp10Library.INSTANCE.gdp_buf_reset(Gdp10Library.INSTANCE
                    .gdp_datum_getbuf(datum)) < 0) {
                //char nbuf[40];
                //strerror_r(errno, nbuf, sizeof nbuf);
                //printf("*** WARNING: buffer reset failed: %s\n",
                //  nbuf);
                System.out
                        .println("*** WARNING: buffer reset failed: FIXME: get errno?");
            }
        }
        // if we've reached the end of file, that's not an error, at least
        // as far as the user is concerned
        // Was: if (EP_STAT_IS_SAME(estat, GDP_STAT_NAK_NOTFOUND))
        //        estat = EP_STAT_END_OF_FILE;

        // epstat.h:
        // compare two status codes for equality
        // #define EP_STAT_IS_SAME(a, b)        ((a).code == (b).code)
        // gdp_stat.h:
        // #define GDP_STAT_NAK_NOTFOUND                GDP_STAT_NEW(ERROR, GDP_COAP_NOTFOUND)
        // ep_stat:
        //#define EP_STAT_END_OF_FILE        _EP_STAT_INTERNAL(WARN, EP_STAT_MOD_GENERIC, 3)
        // common status code definitions
        //#define _EP_STAT_INTERNAL(sev, mod, code)                     \
        //        EP_STAT_NEW(EP_STAT_SEV_ ## sev, EP_REGISTRY_EPLIB, mod, code)
        if (estat.code == GdpUtilities.GDP_STAT_NAK_NOTFOUND.code) {
            estat = GdpUtilities.EP_STAT_END_OF_FILE;
        }

        return estat;
    }

    /**
     * Subscribe or multiread from a GCL.
     * "This routine handles calls that return multiple values via the
     * event interface.  They might include subscriptions."
     *  @param gclh The handle to the GCL
     *  @param firstrec The record number of the first record.
     *  @param numrecs The number of records
     *  @param subscribe True if this is a subscription, false if it
     *  is a multiread.
     *  @return the status.
     */
    static EP_STAT do_subscribe(Pointer gclh, long /*gdp_recno_t*/firstrec,
            int /*int32_t*/numrecs, boolean subscribe) {
        EP_STAT estat;

        if (numrecs < 0) {
            numrecs = 0;
        }
        //System.out.println("ReaderTest.java: gclh: " + gclh);
        if (subscribe) {
            // Was: estat = gdp_gcl_subscribe(gclh, firstrec, numrecs, NULL, NULL, NULL);
            //PointerByReference gclhByReference = new PointerByReference(gclh);
            estat = Gdp10Library.INSTANCE.gdp_gcl_subscribe(gclh, firstrec,
                    numrecs, null, null, null);
            //gclh = gclhByReference.getValue();
        } else {
            // Was: estat = gdp_gcl_multiread(gclh, firstrec, numrecs, NULL, NULL);
            //PointerByReference gclhByReference = new PointerByReference();
            estat = Gdp10Library.INSTANCE.gdp_gcl_multiread(gclh, firstrec,
                    numrecs, null, null);
            //gclh = gclhByReference.getValue();
        }

        // Was: if (!EP_STAT_ISOK(estat)) {
        if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            //char ebuf[200];
            //ep_app_abort("Cannot %s: %s",
            //                        subscribe ? "subscribe" : "multiread",
            //                        ep_stat_tostr(estat, ebuf, sizeof ebuf));
            // I have no idea what to do with ep_stat_tostr(), so we just print
            System.err.println("Cannot "
                    + (subscribe ? "subscribe" : "multiread") + ": "
                    + estat.code);
        }

        for (;;) {
            // Was: gdp_event_t *gev = gdp_event_next(true);
            PointerByReference gev = Gdp10Library.INSTANCE
                    .gdp_event_next(true ? (byte) 1 : (byte) 0);
            switch (Gdp10Library.INSTANCE.gdp_event_gettype(gev)) {
            case GdpUtilities.GDP_EVENT_DATA:
                System.out.print(" >>> ");
                // Was: gdp_datum_print(gdp_event_getdatum(gev), stdout);
                GdpUtilities.gdp_datum_print(Gdp10Library.INSTANCE
                        .gdp_event_getdatum(gev)/*, stdout*/);
                break;
            case GdpUtilities.GDP_EVENT_EOS:
                System.err.println("End of "
                        + (subscribe ? "Subscription" : "Multiread"));
                return estat;
            default:
                System.err.print("Unknown event type"
                        + Gdp10Library.INSTANCE.gdp_event_gettype(gev));
                //sleep(1);
                break;
            }
            Gdp10Library.INSTANCE.gdp_event_free(gev);
        }

        // should never get here
        //return estat;
    }

    /** Read from a GCL
     *  <p>The arguments below are optional:</p>
     *
     *  <dl>
     *  <dt><code>-D <i>debuggingSpecification</i></code></dt>
     *  <dd>, where an example of <i>debuggingSpecifiction</i> is,
     *  <code>gdp.api=100</code>.  See the gdp .c files for debugging specifications.</dd>
     *
     *  <dt><code>-f <i>firstRecordNumber</i></code></dt>
     *  <dd>FIXME: what does this do?
     *  <code>-G <i>gdpdAddress</i></code>, which names the IP
     *  address and port name of the gdp daemon (<code>gdpd</code>)
     *  where an example of <i>gdpdAddress</i> is <code>127.0.0.1:2468</code>.  If <code>-G</code> is not
     *  present, then the default address of <code>127.0.0.1:2468</code> is used.</dd>
     *
     *  <dt><code>-m</code>
     *  <dd>Multiread FIXME: what does this do?</dd>
     *
     *  <dt><code>-n <i>numberOfRecords</i></code></dt>
     *  <dd>FIXME: what does this do?</dd>
     *
     *  <dt><code>-s</code></dt>
     *  <dd>Subscribe FIXME: what does this do?</dd>
     *
     *  <dt><code><i>gcl_name</i></code></dt>
     *  <dd>The name of the gcl.  This string is used with WriterTest</dd>
     *  </dl>
     *
     * @param argv An array of command line arguments, see above for the format.
     */
    public static void main(String[] argv) {
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
        System.out.println("Native.isProtected(): " + Native.isProtected());

        // Was: gdp_gcl_t *gclh;
        Pointer gclh = null;

        EP_STAT estat;
        // Was: gcl_name_t gclname;
        // gdp.h: the internal name of a GCL
        // gdp.h: typedef uint8_t                                gcl_name_t[32];
        ByteBuffer gclname = ByteBuffer.allocate(32);

        // Was: gcl_pname_t gclpname;
        // gdp.h: typedef char                                gcl_pname_t[GDP_GCL_PNAME_LEN + 1];
        // gdp.h:#define GDP_GCL_PNAME_LEN        43                        // length of an encoded pname
        ByteBuffer gclpname = ByteBuffer.allocate(43);

        // The address of the gdp daemon (gdpd), null means to use the
        // default of 127.0.0.1:2468.  This value can be set by the -G command line argument.
        String gdpd_addr = null;

        boolean subscribe = false;
        boolean multiread = false;
        int /*int32_t*/numrecs = -1;
        long /*gdp_recno_t*/firstrec = 1;

        int argc = argv.length;
        //while ((opt = getopt(argc, argv, "D:f:mn:s")) > 0)
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-D")) {
                // Was: ep_dbg_set(optarg);
                argc--;
                Gdp10Library.INSTANCE.ep_dbg_set(argv[i + 1]);
                argc--;
            } else if (argv[i].equals("-G")) {
                argc--;
                gdpd_addr = argv[i + 1];
                argc--;
            } else if (argv[i].equals("-f")) {
                // Was: firstrec = atol(optarg);
                argc--;
                firstrec = Long.valueOf(argv[i + 1]);
                argc--;
            } else if (argv[i].equals("-m")) {
                argc--;
                multiread = true;
            } else if (argv[i].equals("-n")) {
                // Was: numrecs = atol(optarg);
                argc--;
                numrecs = Integer.valueOf(argv[i + 1]);
                argc--;
            } else if (argv[i].equals("-s")) {
                argc--;
                subscribe = true;
            }
        }
        //argc -= optind;
        //argv += optind;

        if (argc <= 0) {
            System.err
                    .println("Usage: %s [-D dbgspec] [-f firstrec] [-m] [-n nrecs] [-s] <gcl_name>\n");
            System.exit(64 /* EX_USAGE from /usr/includes/sysexits.h */);

        }

        // estat = gdp_init();
        System.err.println("About to initialize the GDP.");
        estat = Gdp10Library.INSTANCE.gdp_init(gdpd_addr);
        if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            System.err.println("GDP Initialization failed");
            _fail0(estat);
        }

        // allow thread to settle to avoid interspersed debug output
        //sleep(1);

        System.out.println("firstrec: " + firstrec + ", multiread: "
                + multiread + ", numrecs: " + numrecs + ", subscribe: "
                + subscribe + " name: " + argv[argv.length - 1]);
        // Was estat = gdp_gcl_parse_name(argv[0], gclname);
        estat = Gdp10Library.INSTANCE.gdp_gcl_parse_name(argv[argv.length - 1],
                gclname);

        if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            // Was: ep_app_abort("illegal GCL name syntax:\n\t%s", argv[0]);
            System.err.println("illegal GCL name syntax:\n" + argv[0]);
            System.exit(1);
        }

        Gdp10Library.INSTANCE.gdp_gcl_printable_name(gclname, gclpname);

        System.out.println("Reading GCL " + gclpname);

        // Was: estat = gdp_gcl_open(gclname, GDP_MODE_RO, &gclh);
        PointerByReference gclhByReference = new PointerByReference();
        estat = Gdp10Library.INSTANCE.gdp_gcl_open(gclname,
                Gdp10Library.gdp_iomode_t.GDP_MODE_RO, gclhByReference);
        gclh = gclhByReference.getValue();

        if (!GdpUtilities.EP_STAT_ISOK(estat)) {
            //Was: char sbuf[100];
            //Was: ep_app_error("Cannot open GCL:\n    %s",
            //                ep_stat_tostr(estat, sbuf, sizeof sbuf));
            System.err.println("Cannot open GCL:  FIXME, estate: " + estat
                    + ", code: " + estat.code);
            //Was: goto fail0;
            _fail0(estat);
        }

        if (subscribe || multiread || numrecs >= 0) {
            estat = do_subscribe(gclh, firstrec, numrecs, subscribe);
        } else {
            estat = do_read(gclh, firstrec);
        }

    }

    private static void _fail0(EP_STAT estat) {
        // We use a separate method here so that we can mimic the
        // structure of the original writer-test.c.
        if (GdpUtilities.EP_STAT_ISOK(estat)) {
            estat = GdpUtilities.EP_STAT_OK;
        }

        // FIXME: writer-test.c has:
        // fprintf(stderr, "exiting with status %s\n",
        //                ep_stat_tostr(estat, buf, sizeof buf));
        // I have no idea what to do with ep_stat_tostr(), so we just print
        System.err.println("exiting with status " + estat + ", code: "
                + estat.code);

        System.exit(GdpUtilities.EP_STAT_ISOK(estat) ? 0 : 1);
    }
}
