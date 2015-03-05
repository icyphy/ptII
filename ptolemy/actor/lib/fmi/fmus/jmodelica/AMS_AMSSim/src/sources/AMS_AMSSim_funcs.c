#include "AMS_AMSSim_base.h"




void func_AMS_LinearMap_def0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v, jmi_ad_var_t* h_o) {
    JMI_DYNAMIC_INIT()
    jmi_ad_var_t h_v;
    jmi_ad_var_t n_v;
    jmi_ad_var_t m_v;
    jmi_ad_var_t i_0i;
    jmi_ad_var_t i_0ie;
    jmi_ad_var_t j_1i;
    jmi_ad_var_t j_1ie;
    n_v = jmi_array_size(w_grid_a, 0);
    m_v = jmi_array_size(t_grid_a, 0);
    if (COND_EXP_EQ(n_v, jmi_array_size(h_grid_a, 0), JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
        jmi_assert_failed("mass flow size mismatch", JMI_ASSERT_ERROR);
    }
    if (COND_EXP_EQ(m_v, jmi_array_size(h_grid_a, 1), JMI_TRUE, JMI_FALSE) == JMI_FALSE) {
        jmi_assert_failed("temperature size mismatch", JMI_ASSERT_ERROR);
    }
    if (LOG_EXP_AND(COND_EXP_GE(w_v, jmi_array_val_1(w_grid_a, AD_WRAP_LITERAL(1)), JMI_TRUE, JMI_FALSE), COND_EXP_LE(w_v, jmi_array_val_1(w_grid_a, n_v), JMI_TRUE, JMI_FALSE)) == JMI_FALSE) {
        jmi_assert_failed("mass flow out of range", JMI_ASSERT_ERROR);
    }
    if (LOG_EXP_AND(COND_EXP_GE(t_v, jmi_array_val_1(t_grid_a, AD_WRAP_LITERAL(1)), JMI_TRUE, JMI_FALSE), COND_EXP_LE(t_v, jmi_array_val_1(t_grid_a, m_v), JMI_TRUE, JMI_FALSE)) == JMI_FALSE) {
        jmi_assert_failed("temperature out of range", JMI_ASSERT_ERROR);
    }
    i_0ie = n_v - 1 + 1 / 2.0;
    for (i_0i = 1; i_0i < i_0ie; i_0i += 1) {
        if (LOG_EXP_AND(COND_EXP_GE(w_v, jmi_array_val_1(w_grid_a, i_0i), JMI_TRUE, JMI_FALSE), COND_EXP_LE(w_v, jmi_array_val_1(w_grid_a, i_0i + 1), JMI_TRUE, JMI_FALSE))) {
            j_1ie = m_v - 1 + 1 / 2.0;
            for (j_1i = 1; j_1i < j_1ie; j_1i += 1) {
                if (LOG_EXP_AND(COND_EXP_GE(t_v, jmi_array_val_1(t_grid_a, j_1i), JMI_TRUE, JMI_FALSE), COND_EXP_LE(t_v, jmi_array_val_1(t_grid_a, j_1i + 1), JMI_TRUE, JMI_FALSE))) {
                    h_v = jmi_divide_function("AMS.LinearMap", jmi_divide_function("AMS.LinearMap", ((w_v - jmi_array_val_1(w_grid_a, i_0i)) * (t_v - jmi_array_val_1(t_grid_a, j_1i)) * jmi_array_val_2(h_grid_a, i_0i + 1, j_1i + 1) + (w_v - jmi_array_val_1(w_grid_a, i_0i)) * (jmi_array_val_1(t_grid_a, j_1i + 1) - t_v) * jmi_array_val_2(h_grid_a, i_0i + 1, j_1i) + (jmi_array_val_1(w_grid_a, i_0i + 1) - w_v) * (t_v - jmi_array_val_1(t_grid_a, j_1i)) * jmi_array_val_2(h_grid_a, i_0i, j_1i + 1) + (jmi_array_val_1(w_grid_a, i_0i + 1) - w_v) * (jmi_array_val_1(t_grid_a, j_1i + 1) - t_v) * jmi_array_val_2(h_grid_a, i_0i, j_1i)),(jmi_array_val_1(t_grid_a, j_1i + 1) - jmi_array_val_1(t_grid_a, j_1i)),"((w - w_grid[i]) * (t - t_grid[j]) * h_grid[i + 1,j + 1] + (w - w_grid[i]) * (t_grid[j + 1] - t) * h_grid[i + 1,j] + (w_grid[i + 1] - w) * (t - t_grid[j]) * h_grid[i,j + 1] + (w_grid[i + 1] - w) * (t_grid[j + 1] - t) * h_grid[i,j]) / (t_grid[j + 1] - t_grid[j])"),(jmi_array_val_1(w_grid_a, i_0i + 1) - jmi_array_val_1(w_grid_a, i_0i)),"((w - w_grid[i]) * (t - t_grid[j]) * h_grid[i + 1,j + 1] + (w - w_grid[i]) * (t_grid[j + 1] - t) * h_grid[i + 1,j] + (w_grid[i + 1] - w) * (t - t_grid[j]) * h_grid[i,j + 1] + (w_grid[i + 1] - w) * (t_grid[j + 1] - t) * h_grid[i,j]) / (t_grid[j + 1] - t_grid[j]) / (w_grid[i + 1] - w_grid[i])");
                }
            }
        }
    }
    JMI_RET(GEN, h_o, h_v)
    JMI_DYNAMIC_FREE()
    return;
}

jmi_ad_var_t func_AMS_LinearMap_exp0(jmi_array_t* w_grid_a, jmi_array_t* t_grid_a, jmi_array_t* h_grid_a, jmi_ad_var_t w_v, jmi_ad_var_t t_v) {
    jmi_ad_var_t h_v;
    func_AMS_LinearMap_def0(w_grid_a, t_grid_a, h_grid_a, w_v, t_v, &h_v);
    return h_v;
}








