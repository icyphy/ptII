#include "CoupledClutches_base.h"
static char* StateSelect_0_e[] = { "", "never", "avoid", "default", "prefer", "always" };




void func_Modelica_Math_Vectors_interpolate_def0(jmi_array_t* x_a, jmi_array_t* y_a, jmi_ad_var_t xi_v, jmi_ad_var_t iLast_v, jmi_ad_var_t* yi_o, jmi_ad_var_t* iNew_o) {
    JMI_DYNAMIC_INIT()
    jmi_ad_var_t yi_v;
    jmi_ad_var_t iNew_v;
    jmi_ad_var_t i_v;
    jmi_ad_var_t nx_v;
    jmi_ad_var_t x1_v;
    jmi_ad_var_t x2_v;
    jmi_ad_var_t y1_v;
    jmi_ad_var_t y2_v;
    iNew_v = 1;
    nx_v = jmi_array_size(x_a, 0);
    if (COND_EXP_GT(nx_v, AD_WRAP_LITERAL(0), JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
        jmi_assert_failed("The table vectors must have at least 1 entry.", JMI_ASSERT_ERROR);
    }
    if (COND_EXP_EQ(nx_v, 1, JMI_TRUE, JMI_FALSE)) {
        yi_v = jmi_array_val_1(y_a, 1);
    } else {
        i_v = jmi_min(jmi_max(iLast_v, AD_WRAP_LITERAL(1)), nx_v - AD_WRAP_LITERAL(1));
        if (COND_EXP_GE(xi_v, jmi_array_val_1(x_a, i_v), JMI_TRUE, JMI_FALSE)) {
            while (LOG_EXP_AND(COND_EXP_LT(i_v, nx_v, JMI_TRUE, JMI_FALSE), COND_EXP_GE(xi_v, jmi_array_val_1(x_a, i_v), JMI_TRUE, JMI_FALSE))) {
                i_v = i_v + 1;
            }
            i_v = i_v - 1;
        } else {
            while (LOG_EXP_AND(COND_EXP_GT(i_v, 1, JMI_TRUE, JMI_FALSE), COND_EXP_LT(xi_v, jmi_array_val_1(x_a, i_v), JMI_TRUE, JMI_FALSE))) {
                i_v = i_v - 1;
            }
        }
        x1_v = jmi_array_val_1(x_a, i_v);
        x2_v = jmi_array_val_1(x_a, i_v + 1);
        y1_v = jmi_array_val_1(y_a, i_v);
        y2_v = jmi_array_val_1(y_a, i_v + 1);
        if (COND_EXP_GT(x2_v, x1_v, JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
            jmi_assert_failed("Abscissa table vector values must be increasing", JMI_ASSERT_ERROR);
        }
        yi_v = y1_v + jmi_divide_function("Modelica.Math.Vectors.interpolate", (y2_v - y1_v) * (xi_v - x1_v),(x2_v - x1_v),"(y2 - y1) * (xi - x1) / (x2 - x1)");
        iNew_v = i_v;
    }
    JMI_RET(GEN, yi_o, yi_v)
    JMI_RET(GEN, iNew_o, iNew_v)
    JMI_DYNAMIC_FREE()
    return;
}

jmi_ad_var_t func_Modelica_Math_Vectors_interpolate_exp0(jmi_array_t* x_a, jmi_array_t* y_a, jmi_ad_var_t xi_v, jmi_ad_var_t iLast_v) {
    jmi_ad_var_t yi_v;
    func_Modelica_Math_Vectors_interpolate_def0(x_a, y_a, xi_v, iLast_v, &yi_v, NULL);
    return yi_v;
}








