//*****************************************************************************
//
// lmi_fs.c - File System Processing for lwIP Web Server Apps.
//
// Copyright (c) 2007 Luminary Micro, Inc.  All rights reserved.
// 
// Software License Agreement
// 
// Luminary Micro, Inc. (LMI) is supplying this software for use solely and
// exclusively on LMI's microcontroller products.
// 
// The software is owned by LMI and/or its suppliers, and is protected under
// applicable copyright laws.  All rights are reserved.  You may not combine
// this software with "viral" open-source software in order to form a larger
// program.  Any use in violation of the foregoing restrictions may subject
// the user to criminal sanctions under applicable laws, as well as to civil
// liability for the breach of the terms and conditions of this license.
// 
// THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
// OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
// LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
// CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
// 
// This is part of revision 1900 of the Stellaris Peripheral Driver Library.
//
//*****************************************************************************

#include <string.h>
#include "../../../hw_types.h"
#include "lwip/opt.h"
#include "lwip/mem.h"
#include "fs.h"
#include "fsdata.h"
#include "../../../utils/ustdlib.h"

//*****************************************************************************
//
// Include the file system data for this application.  This file is generated
// by the makefsdata script from lwIP, using the following command (all on one
// line):
//
//     perl ../../../third_party/lwip-1.2.0/apps/httpd/makefsdata fs
//          lmi-fsdata.c
//
// If any changes are made to the static content of the web pages served by the
// application, this script must be used to regenerate fsdata-qs.c in order for
// those changes to be picked up by the web server.
//
//*****************************************************************************
#include "lmi-fsdata.c"

//*****************************************************************************
//
// Strings for the days of the week and the months of the year.
//
//*****************************************************************************
extern const char *g_ppcDay[7];
extern const char *g_ppcMonth[12];

//*****************************************************************************
//
// Initialize the file system.
//
//*****************************************************************************
void
fs_init(void)
{
    //
    // Nothing special to do for this application.  Flash File System only.
    //
}

//*****************************************************************************
// File System tick handler.
//*****************************************************************************
void
fs_tick(unsigned long ulTickMS)
{
    //
    // Nothing special to do for this application.  Flash File System only.
    //
}

//*****************************************************************************
//
// Open a file and return a handle to the file, if found.  Otherwise,
// return NULL.
//
//*****************************************************************************
struct fs_file *
fs_open(char *name)
{
    const struct fsdata_file *ptTree;
    struct fs_file *ptFile = NULL;
    char *ptr;

    //
    // Allocate memory for the file system structure.
    //
    ptFile = mem_malloc(sizeof(struct fs_file));
    if(ptFile == NULL)
    {
        return(NULL);
    }

    //
    // Initialize the file system tree pointer to the root of the linked list.
    //
    ptTree = FS_ROOT;

    //
    // Begin processing the linked list, looking for the requested file name.
    //
    while(ptTree != NULL)
    {
        //
        // Compare the requested file "name" to the file name in the
        // current node.
        //
        if(strncmp(name, (char *)ptTree->name, ptTree->len) == 0)
        {
            //
            // Fill in the data pointer and length values from the
            // linked list node.
            //
            ptFile->data = (char *)ptTree->data;
            ptFile->len = ptTree->len;

            //
            // For now, we setup the read index to the end of the file,
            // indicating that all data has been read.
            //
            ptFile->index = ptTree->len;

            //
            // See if the ptpclock shtml file has been selected.  If so,
            // enable additional script processing.
            //
            ptr = strchr(name, '.');
            if((ptr != NULL) && (strncmp(ptr, ".shtml", 6) == 0))
            {
                //
                // Force the http server code to call the read function, and
                // process the special script file there.
                //
                ptFile->data = NULL;
                ptFile->len = 0;
                ptFile->index = 0;
                ptFile->pextension = (void *)ptTree;
            }
            else
            {
                ptFile->pextension = NULL;
            }

            //
            // Exit the loop and return the file system pointer.
            //
            break;
        }

        //
        // If we get here, we did not find the file at this node of the linked
        // list.  Get the next element in the list.
        //
        ptTree = ptTree->next;
    }

    //
    // If we didn't find the file, ptTee will be NULL.  Make sure we
    // return a NULL pointer if this happens.
    //
    if(ptTree == NULL)
    {
        mem_free(ptFile);
        ptFile = NULL;
    }

    //
    // Return the file system pointer.
    //
    return(ptFile);
}

//*****************************************************************************
//
// Close an opened file designated by the handle.
//
//*****************************************************************************
void
fs_close(struct fs_file *file)
{
    //
    // If a Fat file was opened, free its object.
    //
    if(file->pextension)
    {
        mem_free(file->pextension);
    }

    //
    // Free the main file system object.
    //
    mem_free(file);
}

//*****************************************************************************
//
// Read the next chunck of data from the file.  Return the count of data
// that was read.  Return 0 if no data is currently available.  Return
// a -1 if at the end of file.
//
//*****************************************************************************
int
fs_read(struct fs_file *file, char *buffer, int count)
{
    int iAvailable;

    //
    // Check to see if an audio update was requested (pextension = 1).
    //
    if(file->pextension)
    {
        const struct fsdata_file *ptTree;
        //
        // Check to see if this is a file we know something about.
        //
        ptTree = file->pextension;
        if(strncmp((char *)ptTree->name, "/ptpclock", 9) == 0)
        {
            char *ptr, cTemp;
            extern volatile unsigned long g_ulSystemTimeSeconds;
            tTime sLocalTime;

            //
            // Check to see if the buffer size is large enough for the entire
            // file.  If not, return EOF.
            //
            if(ptTree->len > count)
            {
                return(-1);
            }

            //
            // Copy the file into the buffer.
            //
            memcpy(buffer, ptTree->data, ptTree->len);

            //
            // Find the "div" marker for the PTP time.
            //
            ptr = strstr((const char *)buffer, "Www Mmm Dd, Yyyy  Hh:Mm:Ss");
            if(ptr == NULL)
            {
                return(-1);
            }

            //
            // Save the character after the date string.
            //
            cTemp = *(ptr + 26);

            //
            // Get the local time from the current second counter.
            //
            ulocaltime(g_ulSystemTimeSeconds, &sLocalTime);

            //
            // Print the the local time into the page buffer.
            //
            usprintf(ptr, "%3s %3s %2d, %4d  %02d:%02d:%02d",
                     g_ppcDay[sLocalTime.ucWday], g_ppcMonth[sLocalTime.ucMon],
                     sLocalTime.ucMday, sLocalTime.usYear, sLocalTime.ucHour,
                     sLocalTime.ucMin, sLocalTime.ucSec);
            *(ptr + 26) = cTemp;

            //
            // Clear the extension data and return the count.
            //
            file->pextension = NULL;
            return(strlen(buffer));
        }

        //
        // Here, return EOF ... we don't now how to process this file.
        //
        else
        {
            return(-1);
        }
    }

    //
    // Check to see if more data is available.
    //
    if(file->len == file->index)
    {
        //
        // There is no remaining data.  Return a -1 for EOF indication.
        //
        return(-1);
    }

    //
    // Determine how much data we can copy.  The minimum of the 'count'
    // parameter or the available data in the file system buffer.
    //
    iAvailable = file->len - file->index;
    if(iAvailable > count)
    {
        iAvailable = count;
    }

    //
    // Copy the data.
    //
    memcpy(buffer, file->data + file->index, iAvailable);
    file->index += iAvailable;

    //
    // Return the count of data that we copied.
    //
    return(iAvailable);
}
