/* PCCG: Function that implements native method
<java.lang.System: long currentTimeMillis()>
*/

/* FIXME: Does not give time since epoch defined in Java.
   Gives time from arbitrary epoch, usually beginning of program.
   However, thats good enough for benchmarking. 
*/
return (clock()*1000)/CLOCKS_PER_SEC;

    
