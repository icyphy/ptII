// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the COPYING.txt file.

#include "synchronizeWindowsEnvironment.h"

#ifdef __CYGWIN__

#include <stdlib.h>
#include <string.h>
#include <sys/cygwin.h>
#include <unistd.h>
#include <windows.h>
#include <iostream>

// This is part of Michael Schaap's GPL code from this revision of cygstart.c:
// http://sources.redhat.com/cgi-bin/cvsweb.cgi/cygutils/src/cygstart/cygstart.c?rev=1.5&content-type=text/x-cvsweb-markup&cvsroot=cygwin-apps
// FIXME: cygwin_internal(CW_SYNC_WINENV) would be an alternative implementation which would avoid this duplication.
// Unfortunately, that will only be available from (the as-yet unreleased) Cygwin 1.5.20.

/* Copy cygwin environment variables to the Windows environment if they're not
 * already there. */
static void setup_win_environ(void)
{
    char **envp = environ;
    char *var, *val;
    char curval[2];
    char *winpathlist;
    char winpath[MAX_PATH+1];

    while (envp && *envp) {
        var = strdup(*envp++);
        val = strchr(var, '=');
        *val++ = '\0';
        
        if (GetEnvironmentVariable(var, curval, 2) == 0
                    && GetLastError() == ERROR_ENVVAR_NOT_FOUND) {
            /* Convert POSIX to Win32 where necessary */
            if (!strcmp(var, "PATH") ||
                        !strcmp(var, "LD_LIBRARY_PATH")) {
                winpathlist = (char *)
                      malloc(cygwin_posix_to_win32_path_list_buf_size(val)+1);
                if (winpathlist) {
                    cygwin_posix_to_win32_path_list(val, winpathlist);
                    //std::cout << "A: setting " << var << " to " << winpathlist << "\n";
                    SetEnvironmentVariable(var, winpathlist);
                    free(winpathlist);
                }
            } else if (!strcmp(var, "HOME") ||
                        !strcmp(var, "TMPDIR") ||
                        !strcmp(var, "TMP") ||
                        !strcmp(var, "TEMP")) {
                //std::cout << "B: setting " << var << " to " << winpath << "\n";
                cygwin_conv_to_win32_path(val, winpath);
                SetEnvironmentVariable(var, winpath);
            } else {
                //std::cout << "C: setting " << var << " to " << val << "\n";
                //std::cout.flush();
                SetEnvironmentVariable(var, val);
            }
        }

        free(var);
    }
}

void synchronizeWindowsEnvironment() {
    // There is a limit (perhaps 32KiB) on the amount of native Windows environment that a process can have.
    // To side-step this limit for Cygwin programs, when a Cygwin program starts another Cygwin program,
    // it passes the environment through a non-native mechanism, leaving the native environment as it found it.
    // Windows Java, perhaps in java.dll, seems to get its copy of the environment using the Win32 call
    // GetEnvironmentStrings (rather than using any MSVCRT data, although that would be accessible to it).
    // So we need to synchronize the Windows environment with the Cygwin one, like cygcheck does,
    // currently using the code above.
    // More information, including mailing list links, is in the revision history.
    setup_win_environ();
}

#else

void synchronizeWindowsEnvironment() {
}

#endif
