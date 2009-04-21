/*

 * @Version: $Id$
 * @Author: Christopher Brooks
 *
 * @Copyright (c) 2009 The Regents of the University of California
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in all
 * copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
 * LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 * OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
 * ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
 * RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA  AND RESEARCH IN MOTION LIMITED
 * HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 *                                                 PT_COPYRIGHT_VERSION_2
 *                                                 COPYRIGHTENDKEY
 */

// Example of Code under Test, replace me!!
char * Int_toString(int token) {
  char * returnValue;
  returnValue = (char *)malloc(10);
  sprintf(returnValue, "%d", token);
  return returnValue;
}

#include <stdio.h>
#include <string.h>
#include "CUnit/Basic.h"

/* Pointer to the file used by the tests. */
static FILE* temp_file = NULL;

/** Initialize the test suite.
 */
int init_IntSuite(void) {
  return 0;
}

/** Clean up the test suite.
 */
int clean_IntSuite(void) {
  return 0;
}

/* Simple test of Int_toString().
 * Pass various values and check them.
 */
void testInt_toString(void)
{
  CU_ASSERT(strcmp("0", Int_toString(0)) == 0);
  // This one fails
  //CU_ASSERT(strcmp("0", Int_toString(1)) == 0);
}


/** Set up tests Int.c
 */
int main() {
   CU_pSuite pSuite = NULL;

   /* Initialize the CUnit test registry */
   if (CUE_SUCCESS != CU_initialize_registry())
      return CU_get_error();

   /* Add a suite to the registry */
   pSuite = CU_add_suite("IntSuite", init_IntSuite, clean_IntSuite);
   if (NULL == pSuite) {
      CU_cleanup_registry();
      return CU_get_error();
   }

   /* Add the tests to the suite */
   if ((NULL == CU_add_test(pSuite, "test of Int_toString()", testInt_toString))) {
      CU_cleanup_registry();
      return CU_get_error();
   }

   /* Run the tests*/
   CU_basic_set_mode(CU_BRM_VERBOSE);
   CU_basic_run_tests();
   CU_cleanup_registry();
   return CU_get_error();
}

