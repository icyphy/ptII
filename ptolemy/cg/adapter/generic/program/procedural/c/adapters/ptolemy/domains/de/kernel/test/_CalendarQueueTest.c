/*

 * @Version: $Id$
 * @Author: Christopher Brooks
 *
 * @Copyright (c) 2013-2014 The Regents of the University of California
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


#include <stdio.h>
#include <string.h>
#include "_CalendarQueue.h"
#include "_DEEvent.h"
#include "CUnit/Basic.h"

// The following objects are used throughout the rest of the tests.
struct DEEvent *p1, *p2, *p3, *p4, *p5, *p6, *p7, *p8, *p9, *p10, *p11, *p12, *p13, *p14, *p15, *p16, *p1again;

// The size of the result buffer
#define RESULT_SIZE 80

// Define for debugging
//#define CQTESTDEBUG 1

void _CQTestMessage(char * test, char * message) {
#ifdef CQTESTDEBUG
    fprintf(stderr, "%s Test: %s: <%s>\n", __FILE__, test, message);
#endif
}

char * _CQToString(struct CalendarQueue *cq) {
    char * result = (char *)malloc(RESULT_SIZE);
    result[0] = '\0';
    char * value = (char *)malloc(40);
    while (! (*(cq->isEmpty))(cq)) {
        struct DEEvent* take = (struct DEEvent*) (*(cq->take))(cq);
        sprintf(value, "%g ", take->_timestamp);
        strncat(result, value, RESULT_SIZE - strlen(result));
    }
    // Get rid of the trailing space.
    result[strlen(result)-1 ] = '\0';
    free(value);
    return result;
}

// Construct an empty queue and check the defaults.
char * CalendarQueue_2_1() {
    _CQTestMessage("2_1", "-----Start------");
    struct CalendarQueue* cq = CalendarQueue_New();
    char * returnValue = (char *)malloc(RESULT_SIZE);
    sprintf(returnValue, "size: %d, isEmpty: %d", (*(cq->size))(cq), (*(cq->isEmpty))(cq));
    _CQTestMessage("2_1", returnValue);
    free(cq);
    return returnValue;
}

// Construct an empty queue and attempt a take
char * CalendarQueue_2_2() {
    _CQTestMessage("2_2", "-----Start------");
    struct CalendarQueue* cq = CalendarQueue_New();
    // This will call exit();
    void * result = (*(cq->take))(cq);
    char * returnValue = (char *)malloc(RESULT_SIZE);
    sprintf(returnValue, "CQ_2_2: %p\n", (void *) result);
    _CQTestMessage("2_2", returnValue);
    free(cq);
    return returnValue;
}

// Put 4 entries in the queue and do a single take.
void CalendarQueue_3_0() {
    _CQTestMessage("3_0", "-----Start------");
    struct CalendarQueue* cq = CalendarQueue_New();
    (*(cq->put))(cq, (void *)p4);
    (*(cq->put))(cq, (void *)p2);
    (*(cq->put))(cq, (void *)p3);
    (*(cq->put))(cq, (void *)p1);
    struct DEEvent* get = (struct DEEvent*) (*(cq->get))(cq);
    struct DEEvent* take = (struct DEEvent*) (*(cq->take))(cq);
    char * result = (char *)malloc(RESULT_SIZE);
    sprintf(result, "%g %g %d %d", get->_timestamp, take->_timestamp, (*(cq->isEmpty))(cq), (*(cq->size))(cq));
    CU_ASSERT(strcmp("0 0 0 3", result) == 0);
    _CQTestMessage("3_0", result);
    free(cq);
    free(result);
}

// Skipping 3.1

// Add the same time stamp twice
void CalendarQueue_3_3_0() {
    _CQTestMessage("3_3_0", "-----Start------");
    struct CalendarQueue* cq = CalendarQueue_New();
    (*(cq->put))(cq, (void *)p1);
    (*(cq->put))(cq, (void *)p1again);
    char *result = _CQToString(cq);
    CU_ASSERT(strcmp("0 0", result) == 0);
    _CQTestMessage("3_3_0", result);
    free(cq);
    free(result);
}

// Test the resize method.
void CalendarQueue_3_3() {
    _CQTestMessage("3_3", "-----Start------");
    struct CalendarQueue* cq = CalendarQueue_New();
    (*(cq->put))(cq, (void *)p9);
    (*(cq->put))(cq, (void *)p5);
    (*(cq->put))(cq, (void *)p7);
    (*(cq->put))(cq, (void *)p2);
    (*(cq->put))(cq, (void *)p1);
    //queue size should get doubled here, becomes 4
    (*(cq->put))(cq, (void *)p10);
    (*(cq->put))(cq, (void *)p8);
    (*(cq->put))(cq, (void *)p6);
    (*(cq->put))(cq, (void *)p4);
    // queue size should get doubled here, becomes 8
    (*(cq->put))(cq, (void *)p1again);
    (*(cq->put))(cq, (void *)p3);
    char *result = _CQToString(cq);
    CU_ASSERT(strcmp("0 0 0.1 0.2 3 4 7.6 8.9 50 999.1 999.3", result) == 0);
    _CQTestMessage("3_3", result);
    free(cq);
    free(result);
}



/* Pointer to the file used by the tests. */
//static FILE* temp_file = NULL;

/** Initialize the test suite.
 */
int init_CalendarQueueSuite(void) {
    p1 = DEEvent_New();
    p1->_timestamp = 0.0;
    p2 = DEEvent_New();
    p2->_timestamp = 0.1;
    p3 = DEEvent_New();
    p3->_timestamp = 0.2;
    p4 = DEEvent_New();
    p4->_timestamp = 3.0;
    p5 = DEEvent_New();
    p5->_timestamp = 4.0;
    p6 = DEEvent_New();
    p6->_timestamp = 7.6;
    p7 = DEEvent_New();
    p7->_timestamp = 8.9;
    p8 = DEEvent_New();
    p8->_timestamp = 50.0;
    p9 = DEEvent_New();
    p9->_timestamp = 999.1;
    p10 = DEEvent_New();
    p10->_timestamp = 999.3;
    p11 = DEEvent_New();
    p11->_timestamp = 999.8;
    p12 = DEEvent_New();
    p12->_timestamp = 1001.0;
    p13 = DEEvent_New();
    p13->_timestamp = 1002.1;
    p14 = DEEvent_New();
    p14->_timestamp = 1002.2;
    p15 = DEEvent_New();
    p15->_timestamp = 1002.3;
    p16 = DEEvent_New();
    p16->_timestamp = 1002.4;
    p1again = DEEvent_New();
    p1again->_timestamp = 0.0;
    p1again->_microstep = 1;
    return 0;
}

/** Clean up the test suite.
 */
int clean_CalendarQueueSuite(void) {
    free(p1);
    free(p2);
    free(p3);
    free(p4);
    free(p5);
    free(p6);
    free(p7);
    free(p8);
    free(p9);
    free(p10);
    free(p11);
    free(p12);
    free(p13);
    free(p14);
    free(p15);
    free(p16);
    free(p1again);

    return 0;
}

/* Simple test of CalendarQueue_2_1
 * Pass various values and check them.
 */
void testCalendarQueue_2_1(void)
{
    char *message = CalendarQueue_2_1();
    CU_ASSERT(strcmp("size: 0, isEmpty: 1", message) == 0);
    free(message);

    // This test exits.
    //CU_ASSERT(strcmp("", CalendarQueue_2_2()) == 0);

    CalendarQueue_3_0();
    CalendarQueue_3_3_0();
    CalendarQueue_3_3();
}



/** Set up tests Int.c
 */
int main() {
    CU_pSuite pSuite = NULL;

    /* Initialize the CUnit test registry */
    if (CUE_SUCCESS != CU_initialize_registry())
        return CU_get_error();

    /* Add a suite to the registry */
    pSuite = CU_add_suite("CalendarQueueSuite", init_CalendarQueueSuite, clean_CalendarQueueSuite);
    if (NULL == pSuite) {
        CU_cleanup_registry();
        return CU_get_error();
    }

    /* Add the tests to the suite */
    if ((NULL == CU_add_test(pSuite, "test of CalendarQueue_New()", testCalendarQueue_2_1))) {
        CU_cleanup_registry();
        return CU_get_error();
    }

    /* Run the tests*/
    CU_basic_set_mode(CU_BRM_VERBOSE);
    CU_basic_run_tests();
    CU_cleanup_registry();
    return CU_get_error();
}

