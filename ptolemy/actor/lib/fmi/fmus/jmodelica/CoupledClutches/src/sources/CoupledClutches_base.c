#include "CoupledClutches_base.h"
static const int N_real_ci = 9;
static const int N_real_cd = 0;
static const int N_real_pi = 54;
static const int N_real_pi_s = 0;
static const int N_real_pi_f = 0;
static const int N_real_pi_e = 0;
static const int N_real_pd = 9;

static const int N_integer_ci = 15 + 0;
static const int N_integer_cd = 0 + 0;
static const int N_integer_pi = 6 + 7;
static const int N_integer_pi_s = 0 + 0;
static const int N_integer_pi_f = 0 + 0;
static const int N_integer_pi_e = 0 + 0;
static const int N_integer_pd = 0 + 0;

static const int N_boolean_ci = 0;
static const int N_boolean_cd = 0;
static const int N_boolean_pi = 12;
static const int N_boolean_pi_s = 4;
static const int N_boolean_pi_f = 0;
static const int N_boolean_pi_e = 0;
static const int N_boolean_pd = 0;

static const int N_string_ci = 0;
static const int N_string_cd = 0;
static const int N_string_pi = 0;
static const int N_string_pi_s = 0;
static const int N_string_pi_f = 0;
static const int N_string_pi_e = 0;
static const int N_string_pd = 0;

static const int N_real_dx = 8;
static const int N_real_x = 8;
static const int N_real_u = 0;
static const int N_real_w = 48;

static const int N_real_d = 0;

static const int N_integer_d = 3 + 0;
static const int N_integer_u = 0 + 0;

static const int N_boolean_d = 12;
static const int N_boolean_u = 0;

static const int N_string_d = 0;
static const int N_string_u = 0;

static const int N_ext_objs = 0;

static const int N_time_sw = 4;
static const int N_state_sw = 27;
static const int N_sw = 4 + 27;
static const int N_delay_sw = 0;
static const int N_eq_F = 71;
static const int N_eq_R = 27;

static const int N_dae_blocks = 1;
static const int N_dae_init_blocks = 1;
static const int N_guards = 0;

static const int N_dynamic_state_sets = 0;

static const int N_eq_F0 = 71 + 23;
static const int N_eq_F1 = 56;
static const int N_eq_Fp = 0;
static const int N_eq_R0 = 27 + 0;
static const int N_sw_init = 0;
static const int N_guards_init = 0;

static const int N_delays = 0;
static const int N_spatialdists = 0;

static const int N_outputs = 0;

static const int Scaling_method = JMI_SCALING_NONE;

const char *C_GUID = "77afec03b06d6441475d7ce29d5d3523";

static const int Output_vrefs[1] = {-1};

static int CAD_dae_n_nz = 1;
static const int CAD_dae_nz_rows[1] = {-1};
static const int CAD_dae_nz_cols[1] = {-1};


static const int CAD_ODE_A_n_nz = 0;
static const int CAD_ODE_B_n_nz = 0;
static const int CAD_ODE_C_n_nz = 0;
static const int CAD_ODE_D_n_nz = 0;
static const int CAD_ODE_A_nz_rows[1] = {-1};
static const int CAD_ODE_A_nz_cols[1] = {-1};
static const int CAD_ODE_B_nz_rows[1] = {-1};
static const int CAD_ODE_B_nz_cols[1] = {-1};
static const int CAD_ODE_C_nz_rows[1] = {-1};
static const int CAD_ODE_C_nz_cols[1] = {-1};
static const int CAD_ODE_D_nz_rows[1] = {-1};
static const int CAD_ODE_D_nz_cols[1] = {-1};


static const int N_initial_relations = 0;
static const int DAE_initial_relations[] = { -1 };

static const int N_relations = 27;
static const int DAE_relations[] = { JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LEQ, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_GT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT, JMI_REL_LT };

static const int N_nominals = 8;
static const jmi_real_t DAE_nominals[] = { 1.0E-4, 1.0, 1.0E-4, 1.0, 1.0E-4, 1.0, 1.0, 1.0 };

const char *fmi_runtime_options_map_names[] = {
    "_block_jacobian_check",
    "_block_jacobian_check_tol",
    "_block_solver_experimental_mode",
    "_cs_rel_tol",
    "_cs_solver",
    "_cs_step_size",
    "_enforce_bounds",
    "_events_default_tol",
    "_events_tol_factor",
    "_iteration_variable_scaling",
    "_log_level",
    "_nle_solver_check_jac_cond",
    "_nle_solver_default_tol",
    "_nle_solver_max_iter",
    "_nle_solver_max_residual_scaling_factor",
    "_nle_solver_min_residual_scaling_factor",
    "_nle_solver_min_tol",
    "_nle_solver_regularization_tolerance",
    "_nle_solver_step_limit_factor",
    "_nle_solver_tol_factor",
    "_rescale_after_singular_jac",
    "_rescale_each_step",
    "_residual_equation_scaling",
    "_runtime_log_to_file",
    "_use_Brent_in_1d",
    "_use_jacobian_equilibration",
    NULL
};

const int fmi_runtime_options_map_vrefs[] = {
    536871012, 51, 268435543, 52, 268435544, 53, 536871013, 54, 55, 268435545,
    268435546, 536871014, 56, 268435547, 57, 58, 59, 60, 61, 62,
    536871015, 536871016, 268435548, 536871017, 536871018, 536871019, 0
};

const int fmi_runtime_options_map_length = 26;




static char* StateSelect_0_e[] = { "", "never", "avoid", "default", "prefer", "always" };


int model_ode_guards(jmi_t* jmi) {

    return 0;
}

static int model_ode_next_time_event(jmi_t* jmi, jmi_time_event_t* event) {
    jmi_time_event_t nextEvent = {0};
    jmi_real_t nSamp;
    if (SURELY_LT_ZERO(_time - (_sin1_startTime_47))) {
        jmi_min_time_event(&nextEvent, 1, 0, _sin1_startTime_47);
    }
    if (SURELY_LT_ZERO(_time - (_step1_startTime_10))) {
        jmi_min_time_event(&nextEvent, 1, 0, _step1_startTime_10);
    }
    if (SURELY_LT_ZERO(_time - (_sin2_startTime_136))) {
        jmi_min_time_event(&nextEvent, 1, 0, _sin2_startTime_136);
    }
    if (SURELY_LT_ZERO(_time - (_step2_startTime_51))) {
        jmi_min_time_event(&nextEvent, 1, 0, _step2_startTime_51);
    }
    *event = nextEvent;


    return 0;
}

static int model_ode_derivatives_dir_der(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

static int model_ode_outputs(jmi_t* jmi) {
    int ef = 0;

    return ef;
}

int model_ode_guards_init(jmi_t* jmi) {

    return 0;
}

static int model_ode_initialize_dir_der(jmi_t* jmi) {
    int ef = 0;
    /* This function is not needed - no derivatives of the initialization system is exposed.*/
    return ef;
}

static int model_dae_F(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_dae_dir_dF(jmi_t* jmi, jmi_real_t** res, jmi_real_t** dF, jmi_real_t** dz) {

    return 0;
}

static int model_init_F0(jmi_t* jmi, jmi_real_t** res) {

    return 0;
}

static int model_init_F1(jmi_t* jmi, jmi_real_t** res) {
    (*res)[0] = 0.0 - _J1_a_7;
    (*res)[1] = 0.0 - _torque_tau_8;
    (*res)[2] = 0.0 - _clutch1_fn_17;
    (*res)[3] = 0.0 - _clutch1_f_normalized_18;
    (*res)[4] = 0 - _clutch1_phi_rel_19;
    (*res)[5] = 0 - _clutch1_w_rel_20;
    (*res)[6] = 0 - _clutch1_a_rel_21;
    (*res)[7] = 0.0 - _clutch1_tau_22;
    (*res)[8] = 0.0 - _clutch1_tau0_26;
    (*res)[9] = 0.0 - _clutch1_tau0_max_27;
    (*res)[10] = 0.0 - _clutch1_sa_29;
    (*res)[11] = 0.0 - _clutch1_lossPower_42;
    (*res)[12] = 0.0 - _J2_a_56;
    (*res)[13] = 0.0 - _clutch2_fn_63;
    (*res)[14] = 0.0 - _clutch2_f_normalized_64;
    (*res)[15] = 0 - _clutch2_phi_rel_65;
    (*res)[16] = 0 - _clutch2_w_rel_66;
    (*res)[17] = 0 - _clutch2_a_rel_67;
    (*res)[18] = 0.0 - _clutch2_tau_68;
    (*res)[19] = 0.0 - _clutch2_tau0_72;
    (*res)[20] = 0.0 - _clutch2_tau0_max_73;
    (*res)[21] = 0.0 - _clutch2_sa_75;
    (*res)[22] = 0.0 - _clutch2_lossPower_88;
    (*res)[23] = 0.0 - _J3_a_93;
    (*res)[24] = 0.0 - _clutch3_fn_100;
    (*res)[25] = 0.0 - _clutch3_f_normalized_101;
    (*res)[26] = 0 - _clutch3_phi_rel_102;
    (*res)[27] = 0 - _clutch3_w_rel_103;
    (*res)[28] = 0 - _clutch3_a_rel_104;
    (*res)[29] = 0.0 - _clutch3_tau_105;
    (*res)[30] = 0.0 - _clutch3_tau0_109;
    (*res)[31] = 0.0 - _clutch3_tau0_max_110;
    (*res)[32] = 0.0 - _clutch3_sa_112;
    (*res)[33] = 0.0 - _clutch3_lossPower_125;
    (*res)[34] = 0.0 - _J4_a_131;
    (*res)[35] = 0.0 - _der_J1_phi_159;
    (*res)[36] = 0.0 - _der_J1_w_160;
    (*res)[37] = 0.0 - _der_J2_phi_161;
    (*res)[38] = 0.0 - _der_J2_w_162;
    (*res)[39] = 0.0 - _der_J3_phi_163;
    (*res)[40] = 0.0 - _der_J3_w_164;
    (*res)[41] = 0.0 - _der_2_J1_phi_165;
    (*res)[42] = 0.0 - _der_2_clutch1_phi_rel_166;
    (*res)[43] = 0.0 - _der_2_J2_phi_167;
    (*res)[44] = 0.0 - _der_2_clutch2_phi_rel_168;
    (*res)[45] = 0.0 - _der_2_J3_phi_169;
    (*res)[46] = 0.0 - _der_2_clutch3_phi_rel_170;
    (*res)[47] = 0.0 - _der_2_J4_phi_171;

    return 0;
}

static int model_init_Fp(jmi_t* jmi, jmi_real_t** res) {
    /* C_DAE_initial_dependent_parameter_residuals */
    return -1;
}

static int model_init_delay(jmi_t* jmi) {

    return 0;
}

static int model_sample_delay(jmi_t* jmi) {

    return 0;
}

int jmi_new(jmi_t** jmi, jmi_callbacks_t* jmi_callbacks) {

    jmi_init(jmi, N_real_ci, N_real_cd,  N_real_pi,    N_real_pi_s,    N_real_pi_f,    N_real_pi_e,    N_real_pd,
             N_integer_ci, N_integer_cd, N_integer_pi, N_integer_pi_s, N_integer_pi_f, N_integer_pi_e, N_integer_pd,
             N_boolean_ci, N_boolean_cd, N_boolean_pi, N_boolean_pi_s, N_boolean_pi_f, N_boolean_pi_e, N_boolean_pd,
             N_string_ci,  N_string_cd,  N_string_pi,  N_string_pi_s,  N_string_pi_f,  N_string_pi_e,  N_string_pd,
             N_real_dx, N_real_x, N_real_u, N_real_w,
             N_real_d, N_integer_d, N_integer_u, N_boolean_d, N_boolean_u,
             N_string_d, N_string_u, N_outputs, (int (*))Output_vrefs,
             N_sw, N_sw_init, N_time_sw,N_state_sw, N_guards, N_guards_init,
             N_dae_blocks, N_dae_init_blocks,
             N_initial_relations, (int (*))DAE_initial_relations,
             N_relations, (int (*))DAE_relations, N_dynamic_state_sets,
             (jmi_real_t *) DAE_nominals,
             Scaling_method, N_ext_objs, jmi_callbacks);



    model_add_blocks(jmi);
    
    model_init_add_blocks(jmi);

    /* Initialize the DAE interface */
    jmi_dae_init(*jmi, *model_dae_F, N_eq_F, NULL, 0, NULL, NULL,
                 *model_dae_dir_dF,
                 CAD_dae_n_nz,(int (*))CAD_dae_nz_rows,(int (*))CAD_dae_nz_cols,
                 CAD_ODE_A_n_nz, (int (*))CAD_ODE_A_nz_rows, (int(*))CAD_ODE_A_nz_cols,
                 CAD_ODE_B_n_nz, (int (*))CAD_ODE_B_nz_rows, (int(*))CAD_ODE_B_nz_cols,
                 CAD_ODE_C_n_nz, (int (*))CAD_ODE_C_nz_rows, (int(*))CAD_ODE_C_nz_cols,
                 CAD_ODE_D_n_nz, (int (*))CAD_ODE_D_nz_rows, (int(*))CAD_ODE_D_nz_cols,
                 *model_dae_R, N_eq_R, NULL, 0, NULL, NULL,*model_ode_derivatives,
                 *model_ode_derivatives_dir_der,
                 *model_ode_outputs,*model_ode_initialize,*model_ode_guards,
                 *model_ode_guards_init,*model_ode_next_time_event);

    /* Initialize the Init interface */
    jmi_init_init(*jmi, *model_init_F0, N_eq_F0, NULL,
                  0, NULL, NULL,
                  *model_init_F1, N_eq_F1, NULL,
                  0, NULL, NULL,
                  *model_init_Fp, N_eq_Fp, NULL,
                  0, NULL, NULL,
                  *model_init_eval_parameters,
                  *model_init_R0, N_eq_R0, NULL,
                  0, NULL, NULL);
    
    /* Initialize the delay interface */
    jmi_init_delay_if(*jmi, N_delays, N_spatialdists, *model_init_delay, *model_sample_delay, N_delay_sw);

    return 0;
}

int jmi_destruct_external_objs(jmi_t* jmi) {

    return 0;
}

const char *jmi_get_model_identifier() {
    return "CoupledClutches";
}
