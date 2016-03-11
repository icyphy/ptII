/***preinitBlock***/
#include "duktape.h"
#define  ALLOC_DEFAULT  0

// handle_eval is from duktape examples/cmdline/duk_cmdline.c

/* =============== */
/* Duktape license */
/* =============== */

/* (http://opensource.org/licenses/MIT) */

/* Copyright (c) 2013-2016 by Duktape authors (see AUTHORS.rst) */

/* Permission is hereby granted, free of charge, to any person obtaining a copy */
/* of this software and associated documentation files (the "Software"), to deal */
/* in the Software without restriction, including without limitation the rights */
/* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell */
/* copies of the Software, and to permit persons to whom the Software is */
/* furnished to do so, subject to the following conditions: */

/* The above copyright notice and this permission notice shall be included in */
/* all copies or substantial portions of the Software. */

/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR */
/* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, */
/* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE */
/* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER */
/* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, */
/* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN */
/* THE SOFTWARE. */
        
static int handle_eval(duk_context *ctx, char *code) {
	int rc;
	int retval = -1;

	duk_push_pointer(ctx, (void *) code);
	duk_push_uint(ctx, (duk_uint_t) strlen(code));
	duk_push_string(ctx, "eval");

	rc = duk_safe_call(ctx, wrapped_compile_execute, 3 /*nargs*/, 1 /*nret*/);

	if (rc != DUK_EXEC_SUCCESS) {
		print_pop_error(ctx, stderr);
	} else {
		duk_pop(ctx);
		retval = 0;
	}

	return retval;
}

// Load the script 


void loadScript() {
    duk_context *ctx = duk_create_heap_default();
    if (handle_eval(ctx, $get(script)) != 0) {
        fprintf(stderr, "handle_eval(%s) returned non-zero\n", $get(script));
    }
}
/**/
