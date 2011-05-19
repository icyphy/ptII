/***preinitBlock***/
    // This template is written for the POSIX API.
    DIR *dp;
    struct dirent *ep;
    struct stat statbuf;
/**/

/***initBlock***/
   $ref(output) = $new(Array(0,0));
/**/

/***fireBlock($filepath)***/
    dp = opendir ("$filepath");
    if (dp != NULL) {
        while (ep = readdir (dp)) {

                // FIXME: need absolute path otherwise it won't work.
//                if (stat(ep->d_name, &statbuf) == -1) {
//                  printf("%d\n.", errno);
//                }
//
//            if ($val(listOnlyFiles) && !S_ISREG(statbuf.st_mode)) {
//                    // Exclude non-files.
//            } else if ($val(listOnlyDirectories) && !S_ISDIR(statbuf.st_mode)) {
//                    // Exclude non-directories.
//            } else {
                Array_insert($ref(output), $new(String(ep->d_name)));
//            }
        }
        (void) closedir (dp);
    }
/**/

/***wrapupBlock***/
   Array_delete($ref(output));
/**/
