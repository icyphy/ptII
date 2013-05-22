/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_FMIPPCONFIG_H
#define _FMIPP_FMIPPCONFIG_H


extern "C"
{

#if defined(MINGW) // Windows/MINGW.
  #ifdef BUILD_FMI_DLL
    #define __FMI_DLL __declspec(dllexport)
  #else
    #define __FMI_DLL __declspec(dllimport)
  #endif
#elif defined(_MSC_VER) // Windows/VisualStudio.
  #ifdef BUILD_FMI_DLL
    #define __FMI_DLL __declspec(dllexport)
  #else
    #define __FMI_DLL __declspec(dllimport)
  #endif
#else
  #define __FMI_DLL
#endif


#if defined(MINGW)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <errno.h>
#elif defined(_MSC_VER)
#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#else
#define WIN64_LEAN_AND_MEAN
#endif
#include <windows.h>
#include <errno.h>
#else
#include <errno.h>
#define WINDOWS 0
#define TRUE 1
#define FALSE 0
#define HANDLE void *
/* See http://www.yolinux.com/TUTORIALS/LibraryArchives-StaticAndDynamic.html */
#include <dlfcn.h>
#endif

} // extern "C"

#endif // _FMIPP_FMIPPCONFIG_H
