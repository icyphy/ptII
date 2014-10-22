#ifndef __sfmi_runtime_h_
#define __sfmi_runtime_h_
#include <cmath>
#include <set>
#include <map>
#include <list>
#include <string>
#include "fmi2TypesPlatform.h"

namespace sfmi
{

        class model_data
        {
                public:
                        // Allocate data arrays and change management structures
                        model_data(
                                        int num_reals,
                                        int num_ints,
                                        int num_strs,
                                        int num_bools,
                                        int num_events);

                        // Link an input variable to its function
                        void link(void* var, void(*func)(model_data*));
                        // Link a function its output variable
                        void link(void(*func)(model_data*),void* var);
                        // Indicate that a variable has been modified by a call to SetXXX
                        void modify(void* var);
                        // Perform updates for all modified variables
                        void update();
                        // Destroy data arrays and change management structures
                        ~model_data();

                        // Data arrays
                        fmi2Real Time;
                        fmi2Real* real_vars;
                        fmi2Integer* int_vars;
                        fmi2Boolean* bool_vars;
                    std::string* str_vars;
                        fmi2Real* pre_real_vars;
                        fmi2Integer* pre_int_vars;
                        fmi2Boolean* pre_bool_vars;
                    std::string* pre_str_vars;
                        fmi2Real* z;

                private:
                    // Map from variable addresses to addresses of functions that have the variable for input
                        std::map<void*,std::list<void (*)(model_data*)> > _input_info;
                    // Map from addresses of functions to variables they modify
                        std::map<void (*)(model_data*),std::list<void*> > _output_info;
                    // List of variables that have been modified
                        std::set<void*> _modified_vars;
        };


        typedef double modelica_real;
        typedef bool modelica_boolean;

        #define DIVISION(x,y) (x/y)
        #define $__start(x) x

        /* Linear system solvers */
        void GETRF(double*,long,long*);
        void GETRS(double*,long,long*,double*);

};

#endif
