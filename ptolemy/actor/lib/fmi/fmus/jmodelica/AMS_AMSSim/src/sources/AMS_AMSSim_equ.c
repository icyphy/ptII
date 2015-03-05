#include "AMS_AMSSim_base.h"


static int dae_block_0(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Block: 1 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
        x[0] = 300;
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
        x[0] = 0.0;
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 364;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_DIRECTLY_IMPACTING_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_DIRECTLY_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _T6_310;
        init_with_lbound(x[0], 0.0, "Resetting initial value for variable T6");
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = 716.75 * (- _W33_306);
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            check_lbound(x[0], 0.0, "Out of bounds for variable T6");
            _T6_310 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = 0 - (716.75 * ((- _W33_306) * _T6_310 + _W33_306 * _fork_t_8) - _hx_qh_287);
        }
    }
    return ef;
}

static int dae_block_1(jmi_t* jmi, jmi_real_t* x, jmi_real_t* residual, int evaluation_mode) {
    /***** Block: 2 *****/
    jmi_real_t** res = &residual;
    int ef = 0;
    if (evaluation_mode == JMI_BLOCK_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_START) {
    } else if (evaluation_mode == JMI_BLOCK_MIN) {
    } else if (evaluation_mode == JMI_BLOCK_MAX) {
    } else if (evaluation_mode == JMI_BLOCK_VALUE_REFERENCE) {
        x[0] = 359;
    } else if (evaluation_mode == JMI_BLOCK_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_DIRECTLY_IMPACTING_NON_REAL_VALUE_REFERENCE) {
    } else if (evaluation_mode == JMI_BLOCK_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_DIRECTLY_ACTIVE_SWITCH_INDEX) {
    } else if (evaluation_mode == JMI_BLOCK_EQUATION_NOMINAL) {
    } else if (evaluation_mode == JMI_BLOCK_INITIALIZE) {
        x[0] = _cabin_wa_298;
    } else if (evaluation_mode==JMI_BLOCK_EVALUATE_JACOBIAN) {
        memset(residual, 0, 1 * sizeof(jmi_real_t));
        residual[0] = - _cabin_t_295 * 716.75;
    } else if (evaluation_mode & JMI_BLOCK_EVALUATE || evaluation_mode & JMI_BLOCK_WRITE_BACK) {
        if ((evaluation_mode & JMI_BLOCK_EVALUATE_NON_REALS) == 0) {
            _cabin_wa_298 = x[0];
        }
        if (evaluation_mode & JMI_BLOCK_EVALUATE) {
            (*res)[0] = (_W8_313 * _T8_312 + _fan_outport_w_309 * _cabin_t_295 + _cabin_wa_298 * _cabin_t_295) * 716.75 + _cabin_Qpass_291 * _cabin_passenger_292 + _cabin_dQ_293 - (0);
        }
    }
    return ef;
}





void model_add_blocks(jmi_t** jmi) {
    jmi_dae_add_equation_block(*jmi, dae_block_0, NULL, 1, 0, 0, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 0, "1", -1);
    jmi_dae_add_equation_block(*jmi, dae_block_1, NULL, 1, 0, 0, 0, 0, JMI_CONTINUOUS_VARIABILITY, JMI_CONSTANT_VARIABILITY, JMI_LINEAR_SOLVER, 1, "2", -1);



}

int model_ode_derivatives(jmi_t* jmi) {
    int ef = 0;
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_4, 11, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_5, 10, 1)
    JMI_ARR(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_6, 110, 2)
    model_ode_guards(jmi);
    /************* ODE section *********/
    _fork_t_8 = jmi_divide_equation(jmi, (- _fork_Q_10),(- _fork_m_9 * 716.75),"(- fork.Q) / (- fork.m * 716.75)");
    _fork_p_7 = jmi_divide_equation(jmi, _fork_m_9 * _fork_t_8 * 8.314472,(_fork_v_5 * 0.02897),"fork.m * fork.t * 8.314472 / (fork.v * 0.02897)");
    _W1_303 = COND_EXP_EQ((COND_EXP_GT(_fork_p_7, AD_WRAP_LITERAL(0.5) * _valve1_pi_282, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(7.316E-5) * (_valve1_pi_282 + AD_WRAP_LITERAL(2) * _fork_p_7) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _fork_p_7,_valve1_pi_282,"fork.p / valve1.pi")),_valve1_t_294,"(1 - fork.p / valve1.pi) / valve1.t")), AD_WRAP_LITERAL(1.0338499999999999E-4) * _valve1_pi_282 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_valve1_t_294,"1 / valve1.t")));
    _hx_rhoh_286 = jmi_divide_equation(jmi, 0.02897 * _fork_p_7,(8.314472 * _fork_t_8),"0.02897 * fork.p / (8.314472 * fork.t)");
    _W33_306 = sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(2) * (_fork_p_7 - _valve2_po_302) * _hx_rhoh_286 * (1.0 * (_hx_Ahx_280) * (_hx_Ahx_280)),0.009895,"2 * (fork.p - valve2.po) * hx.rhoh * hx.Ahx ^ 2 / 0.009895"));
    _W32_305 = COND_EXP_EQ((COND_EXP_GT(_valve2_po_302, AD_WRAP_LITERAL(0.5) * _fork_p_7, JMI_TRUE, JMI_FALSE)), JMI_TRUE, AD_WRAP_LITERAL(4.72E-5) * (_fork_p_7 + AD_WRAP_LITERAL(2) * _valve2_po_302) * sqrt(jmi_divide_equation(jmi, (AD_WRAP_LITERAL(1) - jmi_divide_equation(jmi, _valve2_po_302,_fork_p_7,"valve2.po / fork.p")),_fork_t_8,"(1 - valve2.po / fork.p) / fork.t")), AD_WRAP_LITERAL(6.67E-5) * _fork_p_7 * sqrt(jmi_divide_equation(jmi, AD_WRAP_LITERAL(1),_fork_t_8,"1 / fork.t")));
    _fork_outport_w_6 = - _W33_306 + (- _W32_305);
    _der_fork_m_337 = _W1_303 + _fork_outport_w_6;
    _der_fork_Q_338 = _W1_303 * _valve1_t_294 * 716.75 + _fork_outport_w_6 * _fork_t_8 * 716.75;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_4, 11, 1, 11)
    jmi_array_ref_1(tmp_4, 1) = _hx_wh_grid_1_29;
    jmi_array_ref_1(tmp_4, 2) = _hx_wh_grid_2_30;
    jmi_array_ref_1(tmp_4, 3) = _hx_wh_grid_3_31;
    jmi_array_ref_1(tmp_4, 4) = _hx_wh_grid_4_32;
    jmi_array_ref_1(tmp_4, 5) = _hx_wh_grid_5_33;
    jmi_array_ref_1(tmp_4, 6) = _hx_wh_grid_6_34;
    jmi_array_ref_1(tmp_4, 7) = _hx_wh_grid_7_35;
    jmi_array_ref_1(tmp_4, 8) = _hx_wh_grid_8_36;
    jmi_array_ref_1(tmp_4, 9) = _hx_wh_grid_9_37;
    jmi_array_ref_1(tmp_4, 10) = _hx_wh_grid_10_38;
    jmi_array_ref_1(tmp_4, 11) = _hx_wh_grid_11_39;
    JMI_ARRAY_INIT_1(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_5, 10, 1, 10)
    jmi_array_ref_1(tmp_5, 1) = _hx_th_grid_1_40;
    jmi_array_ref_1(tmp_5, 2) = _hx_th_grid_2_41;
    jmi_array_ref_1(tmp_5, 3) = _hx_th_grid_3_42;
    jmi_array_ref_1(tmp_5, 4) = _hx_th_grid_4_43;
    jmi_array_ref_1(tmp_5, 5) = _hx_th_grid_5_44;
    jmi_array_ref_1(tmp_5, 6) = _hx_th_grid_6_45;
    jmi_array_ref_1(tmp_5, 7) = _hx_th_grid_7_46;
    jmi_array_ref_1(tmp_5, 8) = _hx_th_grid_8_47;
    jmi_array_ref_1(tmp_5, 9) = _hx_th_grid_9_48;
    jmi_array_ref_1(tmp_5, 10) = _hx_th_grid_10_49;
    JMI_ARRAY_INIT_2(STATREAL, jmi_ad_var_t, jmi_array_t, tmp_6, 110, 2, 11, 10)
    jmi_array_ref_2(tmp_6, 1,1) = _hx_hh_grid_1_1_170;
    jmi_array_ref_2(tmp_6, 1,2) = _hx_hh_grid_1_2_171;
    jmi_array_ref_2(tmp_6, 1,3) = _hx_hh_grid_1_3_172;
    jmi_array_ref_2(tmp_6, 1,4) = _hx_hh_grid_1_4_173;
    jmi_array_ref_2(tmp_6, 1,5) = _hx_hh_grid_1_5_174;
    jmi_array_ref_2(tmp_6, 1,6) = _hx_hh_grid_1_6_175;
    jmi_array_ref_2(tmp_6, 1,7) = _hx_hh_grid_1_7_176;
    jmi_array_ref_2(tmp_6, 1,8) = _hx_hh_grid_1_8_177;
    jmi_array_ref_2(tmp_6, 1,9) = _hx_hh_grid_1_9_178;
    jmi_array_ref_2(tmp_6, 1,10) = _hx_hh_grid_1_10_179;
    jmi_array_ref_2(tmp_6, 2,1) = _hx_hh_grid_2_1_180;
    jmi_array_ref_2(tmp_6, 2,2) = _hx_hh_grid_2_2_181;
    jmi_array_ref_2(tmp_6, 2,3) = _hx_hh_grid_2_3_182;
    jmi_array_ref_2(tmp_6, 2,4) = _hx_hh_grid_2_4_183;
    jmi_array_ref_2(tmp_6, 2,5) = _hx_hh_grid_2_5_184;
    jmi_array_ref_2(tmp_6, 2,6) = _hx_hh_grid_2_6_185;
    jmi_array_ref_2(tmp_6, 2,7) = _hx_hh_grid_2_7_186;
    jmi_array_ref_2(tmp_6, 2,8) = _hx_hh_grid_2_8_187;
    jmi_array_ref_2(tmp_6, 2,9) = _hx_hh_grid_2_9_188;
    jmi_array_ref_2(tmp_6, 2,10) = _hx_hh_grid_2_10_189;
    jmi_array_ref_2(tmp_6, 3,1) = _hx_hh_grid_3_1_190;
    jmi_array_ref_2(tmp_6, 3,2) = _hx_hh_grid_3_2_191;
    jmi_array_ref_2(tmp_6, 3,3) = _hx_hh_grid_3_3_192;
    jmi_array_ref_2(tmp_6, 3,4) = _hx_hh_grid_3_4_193;
    jmi_array_ref_2(tmp_6, 3,5) = _hx_hh_grid_3_5_194;
    jmi_array_ref_2(tmp_6, 3,6) = _hx_hh_grid_3_6_195;
    jmi_array_ref_2(tmp_6, 3,7) = _hx_hh_grid_3_7_196;
    jmi_array_ref_2(tmp_6, 3,8) = _hx_hh_grid_3_8_197;
    jmi_array_ref_2(tmp_6, 3,9) = _hx_hh_grid_3_9_198;
    jmi_array_ref_2(tmp_6, 3,10) = _hx_hh_grid_3_10_199;
    jmi_array_ref_2(tmp_6, 4,1) = _hx_hh_grid_4_1_200;
    jmi_array_ref_2(tmp_6, 4,2) = _hx_hh_grid_4_2_201;
    jmi_array_ref_2(tmp_6, 4,3) = _hx_hh_grid_4_3_202;
    jmi_array_ref_2(tmp_6, 4,4) = _hx_hh_grid_4_4_203;
    jmi_array_ref_2(tmp_6, 4,5) = _hx_hh_grid_4_5_204;
    jmi_array_ref_2(tmp_6, 4,6) = _hx_hh_grid_4_6_205;
    jmi_array_ref_2(tmp_6, 4,7) = _hx_hh_grid_4_7_206;
    jmi_array_ref_2(tmp_6, 4,8) = _hx_hh_grid_4_8_207;
    jmi_array_ref_2(tmp_6, 4,9) = _hx_hh_grid_4_9_208;
    jmi_array_ref_2(tmp_6, 4,10) = _hx_hh_grid_4_10_209;
    jmi_array_ref_2(tmp_6, 5,1) = _hx_hh_grid_5_1_210;
    jmi_array_ref_2(tmp_6, 5,2) = _hx_hh_grid_5_2_211;
    jmi_array_ref_2(tmp_6, 5,3) = _hx_hh_grid_5_3_212;
    jmi_array_ref_2(tmp_6, 5,4) = _hx_hh_grid_5_4_213;
    jmi_array_ref_2(tmp_6, 5,5) = _hx_hh_grid_5_5_214;
    jmi_array_ref_2(tmp_6, 5,6) = _hx_hh_grid_5_6_215;
    jmi_array_ref_2(tmp_6, 5,7) = _hx_hh_grid_5_7_216;
    jmi_array_ref_2(tmp_6, 5,8) = _hx_hh_grid_5_8_217;
    jmi_array_ref_2(tmp_6, 5,9) = _hx_hh_grid_5_9_218;
    jmi_array_ref_2(tmp_6, 5,10) = _hx_hh_grid_5_10_219;
    jmi_array_ref_2(tmp_6, 6,1) = _hx_hh_grid_6_1_220;
    jmi_array_ref_2(tmp_6, 6,2) = _hx_hh_grid_6_2_221;
    jmi_array_ref_2(tmp_6, 6,3) = _hx_hh_grid_6_3_222;
    jmi_array_ref_2(tmp_6, 6,4) = _hx_hh_grid_6_4_223;
    jmi_array_ref_2(tmp_6, 6,5) = _hx_hh_grid_6_5_224;
    jmi_array_ref_2(tmp_6, 6,6) = _hx_hh_grid_6_6_225;
    jmi_array_ref_2(tmp_6, 6,7) = _hx_hh_grid_6_7_226;
    jmi_array_ref_2(tmp_6, 6,8) = _hx_hh_grid_6_8_227;
    jmi_array_ref_2(tmp_6, 6,9) = _hx_hh_grid_6_9_228;
    jmi_array_ref_2(tmp_6, 6,10) = _hx_hh_grid_6_10_229;
    jmi_array_ref_2(tmp_6, 7,1) = _hx_hh_grid_7_1_230;
    jmi_array_ref_2(tmp_6, 7,2) = _hx_hh_grid_7_2_231;
    jmi_array_ref_2(tmp_6, 7,3) = _hx_hh_grid_7_3_232;
    jmi_array_ref_2(tmp_6, 7,4) = _hx_hh_grid_7_4_233;
    jmi_array_ref_2(tmp_6, 7,5) = _hx_hh_grid_7_5_234;
    jmi_array_ref_2(tmp_6, 7,6) = _hx_hh_grid_7_6_235;
    jmi_array_ref_2(tmp_6, 7,7) = _hx_hh_grid_7_7_236;
    jmi_array_ref_2(tmp_6, 7,8) = _hx_hh_grid_7_8_237;
    jmi_array_ref_2(tmp_6, 7,9) = _hx_hh_grid_7_9_238;
    jmi_array_ref_2(tmp_6, 7,10) = _hx_hh_grid_7_10_239;
    jmi_array_ref_2(tmp_6, 8,1) = _hx_hh_grid_8_1_240;
    jmi_array_ref_2(tmp_6, 8,2) = _hx_hh_grid_8_2_241;
    jmi_array_ref_2(tmp_6, 8,3) = _hx_hh_grid_8_3_242;
    jmi_array_ref_2(tmp_6, 8,4) = _hx_hh_grid_8_4_243;
    jmi_array_ref_2(tmp_6, 8,5) = _hx_hh_grid_8_5_244;
    jmi_array_ref_2(tmp_6, 8,6) = _hx_hh_grid_8_6_245;
    jmi_array_ref_2(tmp_6, 8,7) = _hx_hh_grid_8_7_246;
    jmi_array_ref_2(tmp_6, 8,8) = _hx_hh_grid_8_8_247;
    jmi_array_ref_2(tmp_6, 8,9) = _hx_hh_grid_8_9_248;
    jmi_array_ref_2(tmp_6, 8,10) = _hx_hh_grid_8_10_249;
    jmi_array_ref_2(tmp_6, 9,1) = _hx_hh_grid_9_1_250;
    jmi_array_ref_2(tmp_6, 9,2) = _hx_hh_grid_9_2_251;
    jmi_array_ref_2(tmp_6, 9,3) = _hx_hh_grid_9_3_252;
    jmi_array_ref_2(tmp_6, 9,4) = _hx_hh_grid_9_4_253;
    jmi_array_ref_2(tmp_6, 9,5) = _hx_hh_grid_9_5_254;
    jmi_array_ref_2(tmp_6, 9,6) = _hx_hh_grid_9_6_255;
    jmi_array_ref_2(tmp_6, 9,7) = _hx_hh_grid_9_7_256;
    jmi_array_ref_2(tmp_6, 9,8) = _hx_hh_grid_9_8_257;
    jmi_array_ref_2(tmp_6, 9,9) = _hx_hh_grid_9_9_258;
    jmi_array_ref_2(tmp_6, 9,10) = _hx_hh_grid_9_10_259;
    jmi_array_ref_2(tmp_6, 10,1) = _hx_hh_grid_10_1_260;
    jmi_array_ref_2(tmp_6, 10,2) = _hx_hh_grid_10_2_261;
    jmi_array_ref_2(tmp_6, 10,3) = _hx_hh_grid_10_3_262;
    jmi_array_ref_2(tmp_6, 10,4) = _hx_hh_grid_10_4_263;
    jmi_array_ref_2(tmp_6, 10,5) = _hx_hh_grid_10_5_264;
    jmi_array_ref_2(tmp_6, 10,6) = _hx_hh_grid_10_6_265;
    jmi_array_ref_2(tmp_6, 10,7) = _hx_hh_grid_10_7_266;
    jmi_array_ref_2(tmp_6, 10,8) = _hx_hh_grid_10_8_267;
    jmi_array_ref_2(tmp_6, 10,9) = _hx_hh_grid_10_9_268;
    jmi_array_ref_2(tmp_6, 10,10) = _hx_hh_grid_10_10_269;
    jmi_array_ref_2(tmp_6, 11,1) = _hx_hh_grid_11_1_270;
    jmi_array_ref_2(tmp_6, 11,2) = _hx_hh_grid_11_2_271;
    jmi_array_ref_2(tmp_6, 11,3) = _hx_hh_grid_11_3_272;
    jmi_array_ref_2(tmp_6, 11,4) = _hx_hh_grid_11_4_273;
    jmi_array_ref_2(tmp_6, 11,5) = _hx_hh_grid_11_5_274;
    jmi_array_ref_2(tmp_6, 11,6) = _hx_hh_grid_11_6_275;
    jmi_array_ref_2(tmp_6, 11,7) = _hx_hh_grid_11_7_276;
    jmi_array_ref_2(tmp_6, 11,8) = _hx_hh_grid_11_8_277;
    jmi_array_ref_2(tmp_6, 11,9) = _hx_hh_grid_11_9_278;
    jmi_array_ref_2(tmp_6, 11,10) = _hx_hh_grid_11_10_279;
    _hx_hh_283 = func_AMS_LinearMap_exp0(tmp_4, tmp_5, tmp_6, _W33_306, _fork_t_8);
    _hx_t_284 = jmi_divide_equation(jmi, (- _hx_Q_285),(- _hx_m_17 * _hx_Cmetal_281),"(- hx.Q) / (- hx.m * hx.Cmetal)");
    _hx_qh_287 = _hx_hh_283 * (_fork_t_8 - _hx_t_284);
    _hx_qc_288 = _hx_hc_323 * (_ambientair_port_t_301 - _hx_t_284);
    _der_hx_Q_339 = _hx_qh_287 + _hx_qc_288;
    _W8_313 = _W33_306 - _fan_outport_w_309 + _W32_305;
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[0]);
    _cabin_t_295 = jmi_divide_equation(jmi, (- _cabin_p_290),(- _cabin_m_296 * 8.314472 * jmi_divide_equation(jmi, 1.0,_cabin_v_289,"(1.0 / cabin.v)") * jmi_divide_equation(jmi, 1.0,0.02897,"(1.0 / 0.02897)")),"(- cabin.p) / (- cabin.m * 8.314472 * (1.0 / cabin.v) * (1.0 / 0.02897))");
    _T8_312 = jmi_divide_equation(jmi, (- _W33_306 * _T6_310 + (- (- _fan_outport_w_309) * _cabin_t_295) + (- _W32_305 * _fork_t_8)),(- _W8_313),"(- W33 * T6 + (- (- fan.outport.w) * cabin.t) + (- W32 * fork.t)) / (- W8)");
    ef |= jmi_solve_block_residual(jmi->dae_block_residuals[1]);
    _der_cabin_m_340 = _W8_313 + _fan_outport_w_309 + _cabin_wa_298;
    /************ Real outputs *********/
    /****Integer and boolean outputs ***/
    /**** Other variables ***/
    _cabin_Q_297 = _cabin_m_296 * _cabin_t_295 * 716.75;
    _W31_304 = _W32_305 + _W33_306;
    /********* Write back reinits *******/

    return ef;
}

int model_dae_R(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}
