
Tests for the synthesis.graph.sched package.

@author Shahrooz Shahparnia
@version $Id$

If a test is giving errors write the content of the buffer to a file and
compare. Following code can be used for this purpose.

    set fileChannel [open temp.txt w+]
    puts $fileChannel $fileBuffer 
    close $fileChannel
