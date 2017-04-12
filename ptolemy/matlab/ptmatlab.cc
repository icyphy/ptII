/* ptmatlab.cc - Java Native Interface to the matlab engine API

 Copyright (c) 1998-2009 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

Authors: Zoltan Kemenczy and Sean Simmons, Research in Motion Limited
Version $Id$
*/

// jni assumes MSVC and __int64
#ifdef __GNUC__
typedef long long __int64;
#endif

// Define MX_COMPAT_32 so that mwSize, mwIndex are defined.
// See extern/include/tmwtypes.h
#undef MX_COMPAT_32
#define MX_COMPAT_32


// V5_COMPAT allows matlab version 5 compatible (obsolete in 6.5 and
// beyond) functions (like mxGetArray()...) in the source yielding one
// shared ptmatlab library. The alternative would be to have two source
// files depending on the matlab version or define our own mxGet wrapper
// macros that select the right one... Either way, Ptolemy would have to
// build and jar two versions of the DLL for webstart and
// ptolemy.matlab.impl.impl.mathworks.MathworksEngine would have to know (via a java property?) which
// version of the ptmatlab shared library to load...
#define V5_COMPAT

#ifdef HAVE_MALLOC_SIZE
#include <malloc/malloc.h>
#endif

#include <jni.h>
#include "ptmatlab.h"
#include "engine.h"

// These defines were needed for Matlab r2009a under Mac OS X.
// They also seem to work for r2008a under Linux and Windows
// $PTII/configure sets PTMATLAB_CC_FLAGS to these defines:
//#define PT_NO_ENGGETARRAY 
//#define PT_NO_ENGPUTARRAY
//#define PT_NO_MXGETNAME

#ifdef PT_NO_ENGGETARRAY
#ifndef PT_INCLUDE_MEX_H
#define PT_INCLUDE_MEX_H
#endif
#endif

#ifdef PT_NO_ENGPUTARRAY

#ifndef PT_INCLUDE_MEX_H
#define PT_INCLUDE_MEX_H
#endif

#endif //PT_NO_ENGPUTARRAY


#ifdef PT_NO_MXGETNAME
#define mxGetName(a) "a"
#endif


#ifdef PT_INCLUDE_MEX_H
#include "mex.h"
#endif

// The following test is a kludge for a missing #define in the matlab
// include files that would allow engine interface C code to test which
// matlab version it is dealing with... This is a test for an earlier
// matlab version (than 6p5 or later). NOTE that this test may be broken
// by subsequent matlab versions so we will need a ./configure - time
// matlab version determination eventually.
#if !defined(mxArray_DEFINED)
// Starting with matlab 6p5, logical variables (mxArrays) are using an
// mxLogical type and their class string returns "logical". However
// mxLogical is not defined in earlier matlab versions, so we define it
// here...
#define mxLogical char
#define mxGetLogicals(ma) mxGetPr(ma)
#endif

#include <stdio.h>
#include <stdlib.h>

extern "C"
{
  JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_2;
  }

  static char ptmatlabGetDebug
  (JNIEnv *jni,
   jobject obj)
  {
    jfieldID debug = jni->GetFieldID(jni->GetObjectClass(obj), "debug", "B");
    return jni->GetByteField(obj, debug);
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngOpen
  (JNIEnv *jni,
   jobject obj,
   jstring cmdString)
  {
    const char *cmd = NULL;
    jlong retval = 0;
    if (cmdString != NULL) cmd = jni->GetStringUTFChars(cmdString, 0);
    Engine *ep = engOpen(cmd);
    if (ep != NULL) {
      retval = (jlong)ep;
    }
    else {
      printf("ptmatlabEngOpen: %s failed!\n", cmd==NULL?"":cmd); fflush(stdout);
    }
    if (cmdString != NULL) jni->ReleaseStringUTFChars(cmdString, cmd);
    return retval;
  }

  JNIEXPORT jint JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngClose
  (JNIEnv *jni,
   jobject obj,
   jlong e,
   jlong p)
  {
    Engine *ep = (Engine*)e;
    char* output_buf = (char*)p;
    const char debug = ptmatlabGetDebug(jni,obj);
    if (debug > 1) {
        printf("ptmatlabEngClose: calling engClose(%d)\n", ep);  fflush(stdout);
    }
    jint retval = (jint)engClose(ep);

    if (p != 0) {
        if (debug > 1) {
            printf("ptmatlabEngClose: freeing 0x%x\n", output_buf); fflush(stdout);
        }
#ifdef HAVE_MALLOC_SIZE
        // See http://mercury.nceas.ucsb.edu/kepler/pipermail/kepler-dev/2010-April/018091.html
        if (malloc_size(output_buf) != 0) {
            free(output_buf);
        }
#else
        free(output_buf);
#endif
    }
    return retval;
  }

  JNIEXPORT jint JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngEvalString
  (JNIEnv *jni,
   jobject obj,
   jlong e,
   jstring evalStr)
  {
    Engine *ep = (Engine*)e;
    const char *str = jni->GetStringUTFChars(evalStr, 0);
    int retval = engEvalString(ep, str);
    jni->ReleaseStringUTFChars(evalStr, str);
    return (jint) retval;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngGetArray
  (JNIEnv *jni,
   jobject obj,
   jlong e,
   jstring name)
  {
      Engine *ep = (Engine*) e;
    char debug = ptmatlabGetDebug(jni,obj);
    const char *str = jni->GetStringUTFChars(name, 0);
#ifdef PT_NO_ENGGETARRAY 
    mxArray *ma = engGetVariable(ep, str);
#else
    mxArray *ma = engGetArray(ep, str);
#endif
    if (debug > 1 && ma != NULL) {
        const int *dimArray = mxGetDimensions(ma);
        printf("ptmatlabEngGetArray(%s) %d x %d\n", str, dimArray[0], dimArray[1]);
    }
    jni->ReleaseStringUTFChars(name, str);
    return (jlong) ma;
  }

  JNIEXPORT jint JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngPutArray
  (JNIEnv *jni,
   jobject obj,
   jlong e,
   jstring name,
   jlong pma)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    Engine *ep = (Engine*) e;
    const char *str = jni->GetStringUTFChars(name, 0);
    mxArray *ma = (mxArray*)pma;
#ifdef PT_NO_ENGPUTARRAY
    // Daniel Crawl says to use engPutVariable(), see
    // http://www.mathworks.com/access/helpdesk/help/techdoc/index.html?/access/helpdesk/help/techdoc/rn/f34-998197.html
    int retval = engPutVariable(ep, str, ma);
#else
    mxSetName(ma, str);
    const int *dimArray = mxGetDimensions(ma);
    if (debug > 1) printf("ptmatlabEngPutArray(%s) %d x %d\n", str, dimArray[0], dimArray[1]);
    int retval = engPutArray(ep, ma);
#endif
    jni->ReleaseStringUTFChars(name, str);
    return retval;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabEngOutputBuffer
  (JNIEnv *jni,
   jobject obj,
   jlong e,
   jint n)
  {
    Engine *ep = (Engine*) e;
    char debug = ptmatlabGetDebug(jni,obj);
    char *p = (char*)calloc(n+1,sizeof(char));
    if (p == NULL) {
      printf("ptmatlabEngOutputBuffer(...) could not obtain output buffer pointer - null\n");  fflush(stdout);
      return -2;
    }
    if (debug > 1){
        printf("ptmatlabEngOutputBuffer: set, engine=%d, p=0x%x, n=%d\n", ep, p, n);  fflush(stdout);
    }
    engOutputBuffer(ep, p, n);
    return (jlong)p;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateCellMatrix
  (JNIEnv *jni,
   jobject obj,
   jstring name,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = mxCreateCellMatrix(n, m);
    if (debug > 1) {
      const char *nstr = jni->GetStringUTFChars(name, 0);
      printf("ptmatlabCreateCellMatrix(%s) %d x %d\n", nstr, n, m);
      jni->ReleaseStringUTFChars(name, nstr);
    }
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateString
  (JNIEnv *jni,
   jobject obj,
   jstring name,
   jstring s,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    char *str = (char*) jni->GetStringUTFChars(s, 0);
    if (debug > 1) {
      const char *nstr = jni->GetStringUTFChars(name, 0);
      printf("ptmatlabCreateString(%s,0) %d x %d = %s\n", nstr, n, m, str);
      jni->ReleaseStringUTFChars(name, nstr);
    }
    mxArray *ma;
    if (n == 1) {
      ma = mxCreateString(str);
    } else {
      int dims[] = {n, m};
      ma = mxCreateCharArray(2, dims);
      mxChar *d = (mxChar*)mxGetData(ma);
      for (int i = 0; i < m; i++) {
        d[n*i] = (mxChar)str[i];
      }
    }
    jni->ReleaseStringUTFChars(s, str);
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateDoubleMatrixOneDim
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jdoubleArray a,
   jint length)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = mxCreateDoubleMatrix(1, length, mxREAL);

    double *pr = mxGetPr(ma);
    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabCreateDoubleMatrix(%s) %d x %d\n", nstr, 1, (int)length);
        jni->ReleaseStringUTFChars(name, nstr);
    }

    jni->GetDoubleArrayRegion(a, 0, length, pr);
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateDoubleMatrix
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jobjectArray a,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = mxCreateDoubleMatrix(n, m, mxREAL);
    double *pr = mxGetPr(ma);

    if (debug > 1) {
      const char *nstr = jni->GetStringUTFChars(name, 0);
      printf("ptmatlabCreateDoubleMatrix(%s) %d x %d\n", nstr, (int)n, (int)m);
      jni->ReleaseStringUTFChars(name, nstr);
    }

    jboolean isCopy;
    for (int i = 0; i < n; i++) {
      jdoubleArray row = (jdoubleArray)jni->GetObjectArrayElement(a, i);
      jdouble* rowelements = (jdouble*)jni->GetPrimitiveArrayCritical(row, &isCopy);
      for (int j = 0; j < m; j++) {
        *(pr+i+n*j) = rowelements[j];   // Java indexes row-major, matlab column-major mode...
      }
      jni->ReleasePrimitiveArrayCritical(row, rowelements, 0);
      jni->DeleteLocalRef(row);         // free references
    }
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateComplexMatrixOneDim
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jobjectArray a,
   jint length)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = mxCreateDoubleMatrix(1, length, mxCOMPLEX);
    double *pr = mxGetPr(ma);
    double *pi = mxGetPi(ma);

    jclass complexClass= jni->FindClass("Lptolemy/math/Complex;");
    if (complexClass == NULL) { printf("Cant find complex class\n"); return 0; }
    jfieldID complexRealFieldID = jni->GetFieldID(complexClass, "real", "D");
    jfieldID complexImagFieldID = jni->GetFieldID(complexClass, "imag", "D");

    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabCreateComplexMatrix(%s) %d x %d\n", nstr, 1, (int)length);
        jni->ReleaseStringUTFChars(name, nstr);
    }

    for (int j = 0; j < length; j++) {
      jobject element = (jobject)jni->GetObjectArrayElement(a, j);
      *(pr+j) = jni->GetDoubleField(element, complexRealFieldID);
      *(pi+j) = jni->GetDoubleField(element, complexImagFieldID);
      jni->DeleteLocalRef(element);
    }
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateComplexMatrix
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jobjectArray a,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = mxCreateDoubleMatrix(n, m, mxCOMPLEX);
    double *pr = mxGetPr(ma);
    double *pi = mxGetPi(ma);

    jclass complexClass= jni->FindClass("Lptolemy/math/Complex;");
    if (complexClass == NULL) { printf("Cant find complex class\n"); return 0; }
    jfieldID complexRealFieldID = jni->GetFieldID(complexClass, "real", "D");
    jfieldID complexImagFieldID = jni->GetFieldID(complexClass, "imag", "D");

    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabCreateComplexMatrix(%s) %d x %d\n", nstr, (int)n, (int)m);
        jni->ReleaseStringUTFChars(name, nstr);
    }

    for (int i = 0; i < n; i++) {
      jobjectArray jcolumn = (jobjectArray)jni->GetObjectArrayElement(a, i);
      for (int j = 0; j < m; j++) {
        jobject element = (jobject)jni->GetObjectArrayElement(jcolumn, j);
        *(pr+i+n*j) = jni->GetDoubleField(element, complexRealFieldID);
        *(pi+i+n*j) = jni->GetDoubleField(element, complexImagFieldID);
        jni->DeleteLocalRef(element);
      }
      jni->DeleteLocalRef(jcolumn);
    }
    return (jlong) ma;
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabCreateStructMatrix
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jobjectArray fieldNames,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    int i;
    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabCreateStructMatrix(%s) with fields:", nstr);
        jni->ReleaseStringUTFChars(name, nstr);
    }
    jsize nfields = jni->GetArrayLength(fieldNames);
    // MSVC can't deal with variable length arrays
    //char *names[nfields];
    char **names = (char **)malloc(nfields * sizeof(char *));
    for (i = 0; i < nfields; i++) {
      names[i] = (char*) jni->GetStringUTFChars((jstring)jni->GetObjectArrayElement(fieldNames,i),0);
      if (debug > 1) printf(" %s", names[i]);
    }
    if (debug > 1) printf("\n");

    mxArray *ma = mxCreateStructMatrix(n, m, nfields, (const char**)names);
    for (i = 0; i < nfields; i++) {
      jni->ReleaseStringUTFChars((jstring)jni->GetObjectArrayElement(fieldNames,i),names[i]);
    }
    free(names);
    return (jlong) ma;
  }

  JNIEXPORT void JNICALL Java_ptolemy_matlab_Engine_ptmatlabDestroy
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jstring name)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    if (debug > 1) {
        const char *str = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabDestroy(%s)\n", str); fflush(stdout);
        jni->ReleaseStringUTFChars(name, str);
    }
    mxDestroyArray(ma);
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetCell
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    int subs[] = {n, m};
    int index = mxCalcSingleSubscript(ma, 2, subs);
    mxArray *fma = mxGetCell(ma, index);
    if (debug > 1) {
        const int *dimArray = mxGetDimensions(fma);
        printf("ptmatlabGetCell(%s,%d,%d) %d x %d\n", mxGetName(ma), n, m, dimArray[0], dimArray[1]);
    }
    return (jlong) fma;
  }

  JNIEXPORT jstring JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetClassName
  (JNIEnv *jni,
   jobject obj,
   jlong pma)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = (mxArray*) pma;
    const char *classNameStr = mxGetClassName(ma);
    if (debug > 1) printf("ptmatlabGetClassName(%s) = %s\n", mxGetName(ma), classNameStr);
    return jni->NewStringUTF(classNameStr);
  }

  JNIEXPORT jintArray JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetDimensions
  (JNIEnv *jni,
   jobject obj,
   jlong pma)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = (mxArray*) pma;
    jint ndims = mxGetNumberOfDimensions(ma);
    const int *dims = mxGetDimensions(ma);
    // MSVC can't deal with variable length arrays
    //jint jdims[ndims];
    jint *jdims= (jint *)malloc(ndims * sizeof(jint));
    if (debug > 1) printf("ptmatlabGetDimensions(%s) = %d x %d\n", mxGetName(ma), dims[0], dims[1]);
    for (int i = 0; i < ndims; i++)
      jdims[i] = dims[i];
    jintArray retval = jni->NewIntArray(ndims);
    jni->SetIntArrayRegion(retval, 0, ndims, jdims);
    free(jdims);
    return retval;
  }

  JNIEXPORT jstring JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetFieldNameByNumber
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint k)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    const char* fieldNameStr = mxGetFieldNameByNumber(ma, k);
    if (debug > 1) printf("ptmatlabGetFieldNameByNumber(%s,%d) = %s\n",mxGetName(ma), k, fieldNameStr);
    return jni->NewStringUTF(fieldNameStr);
  }

  JNIEXPORT jlong JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetFieldByNumber
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint k,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    int subs[] = {n, m};
    int index = mxCalcSingleSubscript(ma, 2, subs);
    mxArray *fma = mxGetFieldByNumber(ma, index, k);
    if (debug > 1) {
        const int *dimArray = mxGetDimensions(fma);
        printf("ptmatlabGetFieldByNumber(%s,%d,%d,%d) %d x %d\n", mxGetName(ma), k, n, m, dimArray[0], dimArray[1]);
    }
    return (jlong) fma;
  }

  JNIEXPORT jint JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetNumberOfFields
  (JNIEnv *jni,
   jobject obj,
   jlong pma)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    if (debug > 1) printf("ptmatlabGetNumberOfFields(%s) = %d\n",mxGetName(ma), mxGetNumberOfFields(ma));
    return (jint) mxGetNumberOfFields(ma);
  }

  JNIEXPORT jstring JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetString
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint n)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    mxArray *ma = (mxArray*) pma;
    const int *dims = mxGetDimensions(ma);
    int strlen = dims[1];
    int nrows = dims[0];
    char *str = (char*)mxCalloc(strlen+1,sizeof(mxChar));
    // int err = mxGetString(ma, str, strlen+1);
    // The following, albeit slower (and not multi-byte) supports picking
    // out a 1xm string from a nxm string "matrix" of matlab.
    mxChar *d = (mxChar*)mxGetData(ma);
    for (int i = 0; i < strlen; i++) {
        str[i] = (char)d[n+nrows*i];
    }
    if (debug > 1) printf("ptmatlabGetString(%s,%d) = %s\n",mxGetName(ma), n, str);
    jstring retval = jni->NewStringUTF(str);
    mxFree(str);
    return retval;
  }

  JNIEXPORT jstring JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetOutput
  (JNIEnv *jni,
   jobject obj,
   jlong jp,
   jint n)
  {
    char debug = ptmatlabGetDebug(jni,obj);
    char *p = (char*)jp;
    if (debug > 1) printf("ptmatlabGetOutput(%d) = %s\n", n, p);
    jstring retval = jni->NewStringUTF(p);
    return retval;
  }


  JNIEXPORT jboolean JNICALL Java_ptolemy_matlab_Engine_ptmatlabIsComplex
  (JNIEnv *jni,
   jobject obj,
   jlong pma)
  {
      mxArray *ma = (mxArray*) pma;

    char debug = ptmatlabGetDebug(jni,obj);
    if (debug > 1) printf("ptmatlabIsComplex(%s) = %d\n",mxGetName(ma), mxIsComplex(ma));

    return (jboolean) mxIsComplex(ma);
  }

  JNIEXPORT jobjectArray JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetDoubleMatrix
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = (mxArray*) pma;
    jdouble *pr = (jdouble*) mxGetPr(ma); // Cast assumes jdouble is double

    if (debug > 1) printf("ptmatlabGetDoubleMatrix(%s) %d x %d\n",mxGetName(ma), n, m);

    jclass doubleArrayClass = jni->FindClass("[D");
    if (doubleArrayClass == NULL) { printf("Cant find double array class\n"); return 0; }
    jobjectArray retval = jni->NewObjectArray(n, doubleArrayClass, NULL);
    jboolean isCopy;
    for (int i = 0; i < n; i++) {
      jdoubleArray row = jni->NewDoubleArray(m);
      jdouble* rowelements = (jdouble*)jni->GetPrimitiveArrayCritical(row, &isCopy);
      for (int j = 0; j < m; j++) {
         rowelements[j] = *(pr+i+n*j);   // Java indexes row-major, matlab column-major mode...
      }
      jni->ReleasePrimitiveArrayCritical(row, rowelements, 0);
      jni->SetObjectArrayElement(retval, i, row);
    }
    return retval;
  }

  JNIEXPORT jobjectArray JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetComplexMatrix
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = (mxArray*) pma;
    jdouble *pr = (jdouble*) mxGetPr(ma); // Cast assumes jdouble is double
    jdouble *pi = (jdouble*) mxGetPi(ma);

    jclass complexClass= jni->FindClass("Lptolemy/math/Complex;");
    if (complexClass == NULL) { printf("Cant find complex class\n"); return 0; }
    jfieldID complexRealFieldID = jni->GetFieldID(complexClass, "real", "D");
    jfieldID complexImagFieldID = jni->GetFieldID(complexClass, "imag", "D");
    jclass complexArrayClass = jni->FindClass("[Lptolemy/math/Complex;");
    if (complexArrayClass == NULL) { printf("Cant find complex array class"); return 0; }
    jmethodID complexConstructor = jni->GetMethodID(complexClass, "<init>", "(DD)V");
    if (complexConstructor == NULL) { printf("Cant find complex constructor\n"); return 0; }

    if (debug > 1) printf("ptmatlabGetComplexMatrix(%s) %d x %d\n",mxGetName(ma), n, m);

    jobjectArray retval = jni->NewObjectArray(n, complexArrayClass, NULL);
    jvalue v[2];
    for (int i = 0; i < n; i++) {
      jobjectArray tmp = jni->NewObjectArray(m, complexClass, NULL);
      for (int j = 0; j < m; j++) {
        v[0].d = *(pr+i+n*j);
        v[1].d = *(pi+i+n*j);
        jobject element = jni->NewObjectA(complexClass, complexConstructor, v);
        jni->SetObjectArrayElement(tmp, j, element);
      }
      jni->SetObjectArrayElement(retval, i, tmp);
    }
    return retval;
  }

  JNIEXPORT jobjectArray JNICALL Java_ptolemy_matlab_Engine_ptmatlabGetLogicalMatrix
  (JNIEnv *jni,
   jobject obj,
   jlong pma,
   jint n,
   jint m)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *ma = (mxArray*) pma;
    mxLogical *pr = (mxLogical*) mxGetLogicals(ma);

    if (debug > 1) printf("ptmatlabGetLogicalMatrix(%s) %d x %d\n",mxGetName(ma), n, m);

    jclass intArrayClass = jni->FindClass("[I");
    if (intArrayClass == NULL) { printf("Cant find int array class\n"); return 0; }
    jobjectArray retval = jni->NewObjectArray(n, intArrayClass, NULL);
    jboolean isCopy;
    for (int i = 0; i < n; i++) {
      jintArray row = jni->NewIntArray(m);
      jint* rowelements = (jint*)jni->GetPrimitiveArrayCritical(row, &isCopy);
      for (int j = 0; j < m; j++) {
          rowelements[j] = *(pr+i+n*j) == true ? 1 : 0;
          // Java indexes row-major, matlab column-major mode...
      }
      jni->ReleasePrimitiveArrayCritical(row, rowelements, 0);
      jni->SetObjectArrayElement(retval, i, row);
    }
    return retval;
  }

  JNIEXPORT void JNICALL Java_ptolemy_matlab_Engine_ptmatlabSetString
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jlong pma,
   jint n,
   jstring s,
   jint slen
  )
  {
    char debug = ptmatlabGetDebug(jni,obj);
    char *str = (char*) jni->GetStringUTFChars(s, 0);
    mxArray *ma = (mxArray *)pma;
    mxChar *d = (mxChar*)mxGetData(ma);
    const int *dims = mxGetDimensions(ma);
    int nrows = dims[0];
    if (debug > 1) {
      const char *nstr = jni->GetStringUTFChars(name, 0);
      printf("ptmatlabSetString(%s,%d) %d x %d = %s\n", nstr, n, dims[0], dims[1], str);
      jni->ReleaseStringUTFChars(name, nstr);
    }
    for (int i = 0; i < slen; i++) {
      d[n+nrows*i] = (mxChar)str[i];
    }
    jni->ReleaseStringUTFChars(s, str);
  }

  JNIEXPORT void JNICALL Java_ptolemy_matlab_Engine_ptmatlabSetStructField
  (JNIEnv *jni,
   jobject obj,
   jstring name,                        // for debug only
   jlong sma,
   jstring fieldName,
   jint n,
   jint m,
   jlong fma)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *structMa = (mxArray *) sma;
    mxArray *fieldMa = (mxArray *) fma;
    int subs[] = {n, m};
    int index = mxCalcSingleSubscript(structMa, 2, subs);
    const char *str = jni->GetStringUTFChars(fieldName, 0);
    mxSetField(structMa, index, str, fieldMa);
    // NOTE: The above assumes that the field was empty! (otherwise the
    // previous content (mxArray) should be destroyed)
    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabSetStructField(%s.%s)\n", nstr, str);
        jni->ReleaseStringUTFChars(name, nstr);
    }
    jni->ReleaseStringUTFChars(fieldName, str);
  }

  JNIEXPORT void JNICALL Java_ptolemy_matlab_Engine_ptmatlabSetCell
  (JNIEnv *jni,
   jobject obj,
   jstring name,
   jlong sma,
   jint n,
   jint m,
   jlong fma)
  {
    char debug = ptmatlabGetDebug(jni,obj);

    mxArray *cellArray = (mxArray *)sma;
    mxArray *cell = (mxArray *)fma;
    int subs[] = {n, m};
    int index = mxCalcSingleSubscript(cellArray, 2, subs);
    mxSetCell(cellArray, index, cell);
    if (debug > 1) {
        const char *nstr = jni->GetStringUTFChars(name, 0);
        printf("ptmatlabSetCell(%s,%d,%d)\n", nstr, n, m);
        jni->ReleaseStringUTFChars(name, nstr);
    }
  }
}
