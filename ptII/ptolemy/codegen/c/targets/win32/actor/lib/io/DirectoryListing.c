/***preinitBlock***/
   // This template is written for the Windows api.
   WIN32_FIND_DATA descriptor;
   LARGE_INTEGER filesize;

   // FIXME: We limit the filepath length to MAX_PATH,
   // which is implementation dependent.
   TCHAR szDir[MAX_PATH];
   //size_t length_of_arg;
   HANDLE hFind;
/**/

/***initBlock***/
   hFind = INVALID_HANDLE_VALUE;
   $ref(output) = $new(Array(0,0));
/**/


/***fireBlock($filepath)***/
   //printf(TEXT("\nTarget directory is %s\n\n"), "$filepath");

   // FIXME: Assume the input path plus 2 is not longer than MAX_PATH.
   // Prepare string for use with FindFile functions.  First, copy the
   // string to a buffer, then append '\*' to the directory name.
   strcpy(szDir, "$filepath");
   strcat(szDir, TEXT("\\*"));

   // Find the first file in the directory.

   hFind = FindFirstFile(szDir, &descriptor);

   // List all the files in the directory with some info about them.
   do {
      if (~$val(listOnlyFiles) & descriptor.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
//         printf(TEXT("  %s   <DIR>\n"), descriptor.cFileName);
    	 Array_insert($ref(output), $new(String(descriptor.cFileName)));

      } else if (~$val(listOnlyDirectories)) {
//         filesize.LowPart = descriptor.nFileSizeLow;
//         filesize.HighPart = descriptor.nFileSizeHigh;
//         printf(TEXT("  %s   %ld bytes\n"), descriptor.cFileName, filesize.QuadPart);
    	 Array_insert($ref(output), $new(String(descriptor.cFileName)));
      }
   } while (FindNextFile(hFind, &descriptor) != 0);

   //FIXME: we assume the directory parameter is dynamic.
   // Otherwise, we can open and close the directory only once.
   FindClose(hFind);
/**/



/***wrapupBlock***/
/**/

// Original actor fire code.
////directoryOrURL.update();
//
//URL sourceURL = directoryOrURL.asURL();
//
//$ref(listOnlyDirectories)
////boolean directoriesOnly = ((BooleanToken) listOnlyDirectories.getToken()).booleanValue();
//$ref(listOnlyFiles)
////boolean filesOnly = ((BooleanToken) listOnlyFiles.getToken()).booleanValue();
//
//// assume we are working only files.
////if (sourceURL.getProtocol().equals("file")) {
//
//	//File sourceFile = directoryOrURL.asFile();
//
//    if (sourceFile.isDirectory()) {
//        if (_debugging) {
//            _debug("Reading directory.");
//        }
//
//        File[] files = sourceFile.listFiles(this);
//        ArrayList result = new ArrayList();
//
//        for (int i = 0; i < files.length; i++) {
//            if (filesOnly && !files[i].isFile()) {
//                continue;
//            }
//
//            if (directoriesOnly && !files[i].isDirectory()) {
//                continue;
//            }
//
//            if (accept(null, files[i].getName())) {
//                String path = files[i].getAbsolutePath();
//
//                if (_debugging) {
//                    _debug("Path: " + path);
//                }
//
//                result.add(new StringToken(path));
//            }
//        }
//
//        if (result.size() == 0) {
//            throw new IllegalActionException(this,
//                    "No files or directories that match the pattern.");
//        }
//
//        StringToken[] resultArray = new StringToken[result.size()];
//
//        for (int i = 0; i < resultArray.length; i++) {
//            resultArray[i] = (StringToken) result.get(i);
//        }
//
//        output.broadcast(new ArrayToken(BaseType.STRING, resultArray));
//    } else if (sourceFile.isFile()) {
//        StringToken[] result = new StringToken[1];
//        result[0] = new StringToken(sourceFile.toString());
//
//        if (_debugging) {
//            _debug("Listing just the specified file: "
//                    + result[0].stringValue());
//        }
//
//        output.broadcast(new ArrayToken(BaseType.STRING, result));
//    } else {
//        throw new IllegalActionException("'" + directoryOrURL
//                + "' is neither a file " + "nor a directory.");
//    }
//} else {
//    try {
//        _readURL(sourceURL);
//    } catch (IOException ex) {
//        throw new IllegalActionException(this, ex,
//                "Error reading the URL \'" + directoryOrURL + "\'.");
//    }
//}
