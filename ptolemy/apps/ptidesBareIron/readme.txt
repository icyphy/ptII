This directory was created to allows to keep up with code changes.
Table of Contents.
1. Theme of project
2. Infrastructure Setup
3. Settings to erase or download to flash (from Keil uVision)

Theme:
* Learn FreeRTOS
* Port P1000 Ptides or do it from Scratch using the FreeRTOS.
* Remove the dependency on the RTOS (save a copy of the RTOS version for testing)
  and implement the scheduler
   - Is there any improvement over the RTOS version? (We can try different 
   scheduling mechanism EDF, etc)
* Run a few tests
  * We can make a few examples that explore what Ptides offers specifically
  * The we can port Benchmarks like PapaBence to Ptides so that we have a set of general
    benchmarks
  * I'm (Shanna) going to email Stefan Resmerita from Toyota to ask if we could get a few 
  traces from them (this probably isn't the most promising since companies are very protective
  of their IP). (fingers crossed just in case)
  
  
2. Infrastructure Setup:
	1.   will finish later


3. Settings to erase or download to flash((from Keil uVision)):
* Make sure you build the target.

Go to Flash on the top menu, and select Configure flash tools.
    Under the debug tab ULink Cortex Debugger should be selected selected
    under the device tab make sure lm3s8962 is selected.

Go to Project on the top menu, and select options for target "freeRTOS demo": select the debug tab. select use: UCLink Cortex Debugger, Select settings next to this and click on the flash download tab.
If  LM3sxxx 256KB in not one the option in the program algorithm box, click on the add button and select this as the program algorithm option. Select ok, then ok and you should be good to go.

Now you should be able to erase and download to the  board's flash.

Jia and I noted that at times it can be a bit temperamental, so we simply unplugged and replugged the USB connected to the board and that tended to do the trick.

We were not able to get it to run with the Luminary Eval Board option (without ULink).

~Shanna.




