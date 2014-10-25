// A Simple JavaScript Reader.

// Below is the copyright agreement for the GDP
// Version: $Id: copyright.txt 68472 2014-02-24 22:53:44Z cxh $

// Copyright (c) 2014 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

// A Simple JavaScript Reader.

// This code reads from a hard-wired GDP Repo.  The contents of the
// gcl/ subdirectory should be copied to /var/tmp/gcl and gcl started.
// See http://www.terraswarm.org/swarmos/wiki/Main/GDPJavaScriptInterface

// @author: Alec Dara-Abrahms
// @version: $Id$

/* vim: set ai sw=4 sts=4 ts=4 : */

// Packages that we require to invoke the gdp.
// These are installed with
// npm install ffi
// npm install ref
// npm install ref-array
var ffi        = require('ffi')
var ref        = require('ref')
var ref_array  = require('ref-array')

// Create handles to access the debugging methods in the gdp ep/ep_dbg.h
var libep = ffi.Library('../src/gdp/libs/libep.2.0', {
    'ep_dbg_init': [ 'void', [ ] ],
    'ep_dbg_set':  [ 'void', [ 'string' ] ],
})

// From gdp/gdp.h => <inttypes.h> ==> <stdint.h> ==> <sys/_types/_int32_t.h>
var int32_t = ref.types.int32;

// From gdp/gdp.h => <stdbool.h> 
var bool_t = ref.types.int;

// From gdp/gdp.h
//CJS // the internal name of a GCL
//CJS typedef uint8_t gcl_name_t[32];
var uint8_t = ref.types.uint8;
var gcl_name_t = ref_array(uint8_t);

// From gdp/gdp.h
//CJS // a GCL record number
//CJS typedef int64_t                         gdp_recno_t;
var gdp_recno_t = ref.types.int64;  //?? check this

// From gdp/gdp.h
//CJS typedef enum
//CJS {
//CJS         GDP_MODE_ANY = 0,       // no mode specified
//CJS         GDP_MODE_RO = 1,        // read only
//CJS         GDP_MODE_AO = 2,        // append only
//CJS } gdp_iomode_t;
var GDP_MODE_ANY = 0;
var GDP_MODE_RO  = 1;
var GDP_MODE_AO  = 2;
var gdp_iomode_t = ref.types.int;  //?? check this - enum === int ?

var char_t = ref.types.char;
var buf_t = ref_array(char_t);

//CJS // an open handle on a GCL (opaque)
//CJS typedef struct gdp_gcl          gdp_gcl_t;
var gdp_gcl_t       = ref.types.void;  // opaque for us up here in JS
var gdp_gcl_tPtr    = ref.refType(gdp_gcl_t);
var gdp_gcl_tPtrPtr = ref.refType(gdp_gcl_tPtr);

//CJS typedef struct gdp_datum        gdp_datum_t;
var gdp_datum_t       = ref.types.void;  // opaque for us up here in JS
var gdp_datum_tPtr    = ref.refType(gdp_datum_t);
var gdp_datum_tPtrPtr = ref.refType(gdp_datum_tPtr);  //?? not used yet??

//CJS typedef struct gdp_event        gdp_event_t;
var gdp_event_t       = ref.types.void;  // opaque for us up here in JS
var gdp_event_tPtr    = ref.refType(gdp_event_t);
var gdp_event_tPtrPtr = ref.refType(gdp_event_tPtr);  //?? not used yet??

// From gdp/gdp_buf.h
//CJS typedef struct evbuffer gdp_buf_t;
var gdp_buf_t       = ref.types.void;  // opaque for us up here in JS
var gdp_buf_tPtr    = ref.refType(gdp_buf_t);
var gdp_buf_tPtrPtr = ref.refType(gdp_buf_tPtr);  //?? not used yet??


// Get a handle to the gdp library
var libgdp = ffi.Library('../src/gdp/libs/libgdp.1.0', {

    // From gdp/gdp.h
    //CJS // free an event (required after gdp_event_next)
    //CJS extern EP_STAT                  gdp_event_free(gdp_event_t *gev);
    'gdp_event_free': [ 'uint', [ gdp_event_tPtr ] ],

    //CJS // get next event (fills in gev structure)
    //CJS extern gdp_event_t              *gdp_event_next(bool wait);
    'gdp_event_next': [ gdp_event_tPtr, [ bool_t ] ],

    //CJS // get the type of an event
    //CJS extern int                       gdp_event_gettype(gdp_event_t *gev);
    'gdp_event_gettype': [ 'int', [ gdp_event_tPtr ] ],

    //CJS // get the GCL handle
    //CJS extern gdp_gcl_t                *gdp_event_getgcl(gdp_event_t *gev);
    'gdp_event_getgcl': [ gdp_gcl_tPtr, [ gdp_event_tPtr ] ],

    //CJS // get the datum
    //CJS extern gdp_datum_t              *gdp_event_getdatum(gdp_event_t *gev);
    'gdp_event_getdatum': [ gdp_datum_tPtr, [ gdp_event_tPtr ] ],

    //CJS // initialize the library
    //CJS EP_STAT gdp_init( const char *gdpd_addr );          // address of gdpd
    'gdp_init': [ 'uint', [ 'string' ] ],

    //CJS // create a new GCL
    //CJS EP_STAT gdp_gcl_create( gcl_name_t, gdp_gcl_t ** ); // pointer to result GCL handle
    'gdp_gcl_create': [ 'uint', [ gcl_name_t, gdp_gcl_tPtrPtr ] ],

    //CJS // open an existing GCL
    //CJS extern EP_STAT  gdp_gcl_open( gcl_name_t name, gdp_iomode_t rw, gdp_gcl_t **gclh);              // pointer to result GCL handle
    'gdp_gcl_open': [ 'uint', [ gcl_name_t, gdp_iomode_t, gdp_gcl_tPtrPtr ] ],

    //CJS // close an open GCL
    //CJS EP_STAT  gdp_gcl_close( gdp_gcl_t *gclh);           // GCL handle to close
    'gdp_gcl_close': [ 'uint', [ gdp_gcl_tPtr ] ],

    //CJS // parse a (possibly human-friendly) GCL name
    //CJS EP_STAT gdp_gcl_parse_name( const char *ext, gcl_name_t internal );
    'gdp_gcl_parse_name': [ 'uint', [ 'string', gcl_name_t ] ],

    //CJS // allocate a new message
    //CJS gdp_datum_t             *gdp_datum_new(void);
    'gdp_datum_new': [ gdp_datum_tPtr, [ ] ],

    //CJS // free a message
    //CJS void                    gdp_datum_free(gdp_datum_t *);
    'gdp_datum_free': [ 'void', [ gdp_datum_tPtr ] ],

    // From gdp/gdp.h
    //CJS // get the data buffer from a datum
    //CJS extern gdp_buf_t *gdp_datum_getbuf( const gdp_datum_t *datum );
    'gdp_datum_getbuf': [ gdp_buf_tPtr , [ gdp_datum_tPtr ] ],

    // From gdp/gdp_buf.h
    //CJS extern int gdp_buf_write( gdp_buf_t *buf, void *in, size_t sz );
    //'gdp_buf_write': [ 'int', [ gdp_buf_tPtr, 'pointer', 'size_t' ] ],
    //'gdp_buf_write': [ 'int', [ gdp_buf_tPtr, 'pointer', 'int' ] ],
    'gdp_buf_write': [ 'int', [ gdp_buf_tPtr, buf_t, 'size_t' ] ],

    // From gdp/gdp_buf.h
    //CJS extern size_t           gdp_buf_read( gdp_buf_t *buf, void *out, size_t sz);
    'gdp_buf_read': [ 'size_t', [ gdp_buf_tPtr, buf_t, 'size_t' ] ],

    // From gdp/gdp.h
    //CJS // append to a writable GCL
    //CJS extern EP_STAT  gdp_gcl_publish( gdp_gcl_t *gclh, gdp_datum_t *);
    'gdp_gcl_publish': [ 'uint', [ gdp_gcl_tPtr, gdp_datum_tPtr ] ],

    //CJS extern EP_STAT  gdp_gcl_subscribe(
    //CJS                                         gdp_gcl_t *gclh,                // readable GCL handle
    //CJS                                         gdp_recno_t start,              // first record to retrieve
    //CJS                                         int32_t numrecs,                // number of records to retrieve
    //CJS                                         EP_TIME_SPEC *timeout,  // timeout
    //CJS                                         gdp_gcl_sub_cbfunc_t cbfunc,
    //CJS 
    //CJS         // callback function for next datum
    //CJS                                         void *cbarg);                   // argument passed to callback
    // Note, in our call to this function in do_multiread() below we do not
    //       use the last 3 (pointer) arguments.
    'gdp_gcl_subscribe': [ 'uint', [ gdp_gcl_tPtr, gdp_recno_t, int32_t, 'pointer', 'pointer', 'pointer' ] ],

    //CJS // read from a readable GCL
    //CJS extern EP_STAT  gdp_gcl_read( gdp_gcl_t *gclh, gdp_recno_t recno, gdp_datum_t *datum);    // pointer to result message
    'gdp_gcl_read': [ 'uint', [ gdp_gcl_tPtr, gdp_recno_t, gdp_datum_tPtr ] ],

    // From gdp/gdp.h
    //CJS // get the data length from a datum
    //CJS extern size_t   gdp_datum_getdlen( const gdp_datum_t *datum);
    'gdp_datum_getdlen': [ 'size_t', [ gdp_datum_tPtr ] ],

})



var gclname_arg = "xuNqb5yb3g0RBzDKKYwhyyIoIglfAygxiOjH-N8JGFA" ;
// var gclname_arg = process.argv[ 3 ];
// var firstrec = 1  // first item entered in the gcl
var firstrec = -1  // most recently entered item in the gcl

//C  gcl_name_t gclname;
var gclname = ref.alloc(gcl_name_t);

//C  char *gdpd_addr = NULL;
var gdpd_addr = "127.0.0.1:2468";  // default port for a local gdpd


//C // initialize the GDP library
//C estat = gdp_init(gdpd_addr);
estat = libgdp.gdp_init(gdpd_addr);

estat = libgdp.gdp_gcl_parse_name(gclname_arg, gclname);


//C  // open the GCL; arguably this shouldn't be necessary
//C  estat = gdp_gcl_open(gclname, GDP_MODE_RO, &gclh);
var gclPtrPtr = ref.alloc( gdp_gcl_tPtrPtr );
estat = libgdp.gdp_gcl_open(gclname, GDP_MODE_RO, gclPtrPtr);
gcl_Ptr = gclPtrPtr.deref();


// From estat = do_simpleread(gcl_Ptr, firstrec, numrecs);

//C gdp_recno_t recno;
var recno;
recno = firstrec;

//C  while (numrecs < 0 || --numrecs >= 0)
//C  gdp_datum_t *datum = gdp_datum_new();
var datum;
datum = libgdp.gdp_datum_new();
// In this test program we do not free this datum

console.log( "Reading record #" + recno + " from GCL '%s'", gclname_arg );

//C // ask the GDP to give us a record
//C estat = gdp_gcl_read(gclh, recno, datum);
estat = libgdp.gdp_gcl_read(gcl_Ptr, recno, datum);

// Print out the contents of the datum's buffer we read
var datum_dlen = libgdp.gdp_datum_getdlen( datum );
var temp_gdp_buf = libgdp.gdp_datum_getbuf( datum );
var buf = new buf_t(1000); // hack size??
var temp_gdp_buf_size = libgdp.gdp_buf_read( temp_gdp_buf, buf, buf.length );
var aJSString = '';
for ( var i = 0; i < temp_gdp_buf_size; i++ )
{  
    aJSString = aJSString + String.fromCharCode( buf[i] );
}
console.log("read: '" + aJSString + "'" );

//C gdp_gcl_close(gclh);
estat = libgdp.gdp_gcl_close(gcl_Ptr);


