%include "std_string.i"
%include carrays.i
%array_functions(double, double_array)
%array_functions(std::string, string_array)
 //%include <windows.i>
#define __FMI_DLL

%module helper

%{
  //  typedef double fmiReal;
#include "../include/IncrementalFMU.h"
%}
%ignore FMU;
%ignore getCurrentState;
%ignore getValue(const std::string&  name, fmiReal* val);
typedef double fmiTime;
typedef double fmiReal;
typedef unsigned int size_t;
%include "../include/IncrementalFMU.h"
