#include "FileSearch.h"

bool searchFile( char *current_directory, const char *filename, char *return_path ) {
	char dir[MAX_PATH];
	WIN32_FIND_DATA FileData;
	HANDLE hSearch;

	strcpy(dir, current_directory);
	strcat(dir, "/*");

	hSearch = FindFirstFile(dir, &FileData); // Directory: .
	FindNextFile(hSearch, &FileData); // Directory: ..
	while ( FindNextFile(hSearch, &FileData) ) {
		if ( FileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) {
			strcpy(dir, current_directory); strcat(dir, "/"); strcat(dir, FileData.cFileName);
			if ( searchFile(dir, filename, return_path) ) return true;
		} else {
			if ( !strcmp(filename, FileData.cFileName) ) {
				strcpy(return_path, current_directory); strcat(return_path, "/"); strcat(return_path, FileData.cFileName);
				return true;
			}
		}
	}
	return false;
}
