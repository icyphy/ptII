/*** sharedBlock ***/
// Match string against the extended regular expression in
// pattern, treating errors as no match.
boolean match(const char *string, char *pattern)
{
    int status;
    regex_t re;

    if (regcomp(&re, pattern, REG_EXTENDED|REG_NOSUB) != 0) {
        return false;      /* report error */
    }
    status = regexec(&re, string, (size_t) 0, NULL, 0);
    regfree(&re);
    if (status != 0) {
        return false;      /* report error */
    }
    return true;
}
/**/

/*** fireBlock ***/
	$ref(output) = match($ref(matchString), $ref(pattern));
/**/

