/* PCCG: Function that implements native method
<java.lang.System: long currentTimeMillis()>
*/

struct timeval *time = malloc(sizeof(struct timeval));
/* Returns 1 on error. */
if (gettimeofday(time, NULL)) {
    printf("Error in System.getCurrentTimeMillis()\n");
    return 0;
}
else {
    return ((time->tv_sec)*1000 + (time->tv_usec)/1000);
}

    
