/* 
*   Matrix Market I/O library for ANSI C
*
*   See http://math.nist.gov/MatrixMarket for details.
*
*
*   (Modifications by dbindel, 10/4 -- see end of file)
* 
*/
#include <upc.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#include <ctype.h>
#include <assert.h>

#include "mmio.h"
#include "csr_problem.h"


int mm_read_unsymmetric_sparse(const char *fname, int *M_, int *N_,
                               int *nz_, double **val_, int **I_, int **J_)
{
    FILE *f;
    MM_typecode matcode;
    int M, N, nz;
    int i;
    double *val;
    int *I, *J;

    if ((f = fopen(fname, "r")) == NULL)
        return -1;


    if (mm_read_banner(f, &matcode) != 0) {
        printf
            ("mm_read_unsymetric: Could not process Matrix Market banner ");
        printf(" in file [%s]\n", fname);
        return -1;
    }



    if (!(mm_is_real(matcode) && mm_is_matrix(matcode) &&
          mm_is_sparse(matcode))) {
        fprintf(stderr, "Sorry, this application does not support ");
        fprintf(stderr, "Market Market type: [%s]\n",
                mm_typecode_to_str(matcode));
        return -1;
    }

    /* find out size of sparse matrix: M, N, nz .... */

    if (mm_read_mtx_crd_size(f, &M, &N, &nz) != 0) {
        fprintf(stderr,
                "read_unsymmetric_sparse(): could not parse matrix size.\n");
        return -1;
    }

    *M_ = M;
    *N_ = N;
    *nz_ = nz;

    /* reseve memory for matrices */

    I = (int *) malloc(nz * sizeof(int));
    J = (int *) malloc(nz * sizeof(int));
    val = (double *) malloc(nz * sizeof(double));

    *val_ = val;
    *I_ = I;
    *J_ = J;

    /* NOTE: when reading in doubles, ANSI C requires the use of the "l"  */
    /*   specifier as in "%lg", "%lf", "%le", otherwise errors will occur */
    /*  (ANSI C X3.159-1989, Sec. 4.9.6.2, p. 136 lines 13-15)            */

    for (i = 0; i < nz; i++) {
        fscanf(f, "%d %d %lg\n", &I[i], &J[i], &val[i]);
        I[i]--;                 /* adjust from 1-based to 0-based */
        J[i]--;
    }
    fclose(f);

    return 0;
}

int mm_is_valid(MM_typecode matcode)
{
    if (!mm_is_matrix(matcode))
        return 0;
    if (mm_is_dense(matcode) && mm_is_pattern(matcode))
        return 0;
    if (mm_is_real(matcode) && mm_is_hermitian(matcode))
        return 0;
    if (mm_is_pattern(matcode) && (mm_is_hermitian(matcode) ||
                                   mm_is_skew(matcode)))
        return 0;
    return 1;
}

int mm_read_banner(FILE * f, MM_typecode * matcode)
{
    char line[MM_MAX_LINE_LENGTH];
    char banner[MM_MAX_TOKEN_LENGTH];
    char mtx[MM_MAX_TOKEN_LENGTH];
    char crd[MM_MAX_TOKEN_LENGTH];
    char data_type[MM_MAX_TOKEN_LENGTH];
    char storage_scheme[MM_MAX_TOKEN_LENGTH];
    char *p;


    mm_clear_typecode(matcode);

    if (fgets(line, MM_MAX_LINE_LENGTH, f) == NULL)
        return MM_PREMATURE_EOF;

    if (sscanf(line, "%s %s %s %s %s", banner, mtx, crd, data_type,
               storage_scheme) != 5)
        return MM_PREMATURE_EOF;

    for (p = mtx; *p != '\0'; *p = tolower(*p), p++);   /* convert to lower case */
    for (p = crd; *p != '\0'; *p = tolower(*p), p++);
    for (p = data_type; *p != '\0'; *p = tolower(*p), p++);
    for (p = storage_scheme; *p != '\0'; *p = tolower(*p), p++);

    /* check for banner */
    if (strncmp(banner, MatrixMarketBanner, strlen(MatrixMarketBanner)) !=
        0)
        return MM_NO_HEADER;

    /* first field should be "mtx" */
    if (strcmp(mtx, MM_MTX_STR) != 0)
        return MM_UNSUPPORTED_TYPE;
    mm_set_matrix(matcode);


    /* second field describes whether this is a sparse matrix (in coordinate
       storgae) or a dense array */


    if (strcmp(crd, MM_SPARSE_STR) == 0)
        mm_set_sparse(matcode);
    else if (strcmp(crd, MM_DENSE_STR) == 0)
        mm_set_dense(matcode);
    else
        return MM_UNSUPPORTED_TYPE;


    /* third field */

    if (strcmp(data_type, MM_REAL_STR) == 0)
        mm_set_real(matcode);
    else if (strcmp(data_type, MM_COMPLEX_STR) == 0)
        mm_set_complex(matcode);
    else if (strcmp(data_type, MM_PATTERN_STR) == 0)
        mm_set_pattern(matcode);
    else if (strcmp(data_type, MM_INT_STR) == 0)
        mm_set_integer(matcode);
    else
        return MM_UNSUPPORTED_TYPE;


    /* fourth field */

    if (strcmp(storage_scheme, MM_GENERAL_STR) == 0)
        mm_set_general(matcode);
    else if (strcmp(storage_scheme, MM_SYMM_STR) == 0)
        mm_set_symmetric(matcode);
    else if (strcmp(storage_scheme, MM_HERM_STR) == 0)
        mm_set_hermitian(matcode);
    else if (strcmp(storage_scheme, MM_SKEW_STR) == 0)
        mm_set_skew(matcode);
    else
        return MM_UNSUPPORTED_TYPE;


    return 0;
}

int mm_write_mtx_crd_size(FILE * f, int M, int N, int nz)
{
    if (fprintf(f, "%d %d %d\n", M, N, nz) != 3)
        return MM_COULD_NOT_WRITE_FILE;
    else
        return 0;
}

int mm_read_mtx_crd_size(FILE * f, int *M, int *N, int *nz)
{
    char line[MM_MAX_LINE_LENGTH];
    int num_items_read;

    /* set return null parameter values, in case we exit with errors */
    *M = *N = *nz = 0;

    /* now continue scanning until you reach the end-of-comments */
    do {
        if (fgets(line, MM_MAX_LINE_LENGTH, f) == NULL)
            return MM_PREMATURE_EOF;
    } while (line[0] == '%');

    /* line[] is either blank or has M,N, nz */
    if (sscanf(line, "%d %d %d", M, N, nz) == 3)
        return 0;

    else
        do {
            num_items_read = fscanf(f, "%d %d %d", M, N, nz);
            if (num_items_read == EOF)
                return MM_PREMATURE_EOF;
        }
        while (num_items_read != 3);

    return 0;
}


int mm_read_mtx_array_size(FILE * f, int *M, int *N)
{
    char line[MM_MAX_LINE_LENGTH];
    int num_items_read;
    /* set return null parameter values, in case we exit with errors */
    *M = *N = 0;

    /* now continue scanning until you reach the end-of-comments */
    do {
        if (fgets(line, MM_MAX_LINE_LENGTH, f) == NULL)
            return MM_PREMATURE_EOF;
    } while (line[0] == '%');

    /* line[] is either blank or has M,N, nz */
    if (sscanf(line, "%d %d", M, N) == 2)
        return 0;

    else                        /* we have a blank line */
        do {
            num_items_read = fscanf(f, "%d %d", M, N);
            if (num_items_read == EOF)
                return MM_PREMATURE_EOF;
        }
        while (num_items_read != 2);

    return 0;
}

int mm_write_mtx_array_size(FILE * f, int M, int N)
{
    if (fprintf(f, "%d %d\n", M, N) != 2)
        return MM_COULD_NOT_WRITE_FILE;
    else
        return 0;
}



/*-------------------------------------------------------------------------*/

/******************************************************************/
/* use when I[], J[], and val[]J, and val[] are already allocated */
/******************************************************************/

int mm_read_mtx_crd_data(FILE * f, int M, int N, int nz, int I[], int J[],
                         double val[], MM_typecode matcode)
{
    int i;
    if (mm_is_complex(matcode)) {
        for (i = 0; i < nz; i++)
            if (fscanf
                (f, "%d %d %lg %lg", &I[i], &J[i], &val[2 * i],
                 &val[2 * i + 1])
                != 4)
                return MM_PREMATURE_EOF;
    } else if (mm_is_real(matcode)) {
        for (i = 0; i < nz; i++) {
            if (fscanf(f, "%d %d %lg\n", &I[i], &J[i], &val[i])
                != 3)
                return MM_PREMATURE_EOF;

        }
    }

    else if (mm_is_pattern(matcode)) {
        for (i = 0; i < nz; i++)
            if (fscanf(f, "%d %d", &I[i], &J[i])
                != 2)
                return MM_PREMATURE_EOF;
    } else
        return MM_UNSUPPORTED_TYPE;

    return 0;

}

int mm_read_mtx_crd_entry(FILE * f, int *I, int *J,
                          double *real, double *imag, MM_typecode matcode)
{
    if (mm_is_complex(matcode)) {
        if (fscanf(f, "%d %d %lg %lg", I, J, real, imag)
            != 4)
            return MM_PREMATURE_EOF;
    } else if (mm_is_real(matcode)) {
        if (fscanf(f, "%d %d %lg\n", I, J, real)
            != 3)
            return MM_PREMATURE_EOF;

    }

    else if (mm_is_pattern(matcode)) {
        if (fscanf(f, "%d %d", I, J) != 2)
            return MM_PREMATURE_EOF;
    } else
        return MM_UNSUPPORTED_TYPE;

    return 0;

}


/************************************************************************
    mm_read_mtx_crd()  fills M, N, nz, array of values, and return
                        type code, e.g. 'MCRS'

                        if matrix is complex, values[] is of size 2*nz,
                            (nz pairs of real/imaginary values)
************************************************************************/

int mm_read_mtx_crd(char *fname, int *M, int *N, int *nz, int **I, int **J,
                    double **val, MM_typecode * matcode)
{
    int ret_code;
    FILE *f;

    if (strcmp(fname, "stdin") == 0)
        f = stdin;
    else if ((f = fopen(fname, "r")) == NULL)
        return MM_COULD_NOT_READ_FILE;


    if ((ret_code = mm_read_banner(f, matcode)) != 0)
        return ret_code;

    if (!(mm_is_valid(*matcode) && mm_is_sparse(*matcode) &&
          mm_is_matrix(*matcode)))
        return MM_UNSUPPORTED_TYPE;

    if ((ret_code = mm_read_mtx_crd_size(f, M, N, nz)) != 0)
        return ret_code;


    *I = (int *) malloc(*nz * sizeof(int));
    *J = (int *) malloc(*nz * sizeof(int));
    *val = NULL;

    if (mm_is_complex(*matcode)) {
        *val = (double *) malloc(*nz * 2 * sizeof(double));
        ret_code = mm_read_mtx_crd_data(f, *M, *N, *nz, *I, *J, *val,
                                        *matcode);
        if (ret_code != 0)
            return ret_code;
    } else if (mm_is_real(*matcode)) {
        *val = (double *) malloc(*nz * sizeof(double));
        ret_code = mm_read_mtx_crd_data(f, *M, *N, *nz, *I, *J, *val,
                                        *matcode);
        if (ret_code != 0)
            return ret_code;
    }

    else if (mm_is_pattern(*matcode)) {
        ret_code = mm_read_mtx_crd_data(f, *M, *N, *nz, *I, *J, *val,
                                        *matcode);
        if (ret_code != 0)
            return ret_code;
    }

    if (f != stdin)
        fclose(f);
    return 0;
}

int mm_write_banner(FILE * f, MM_typecode matcode)
{
    char *str = mm_typecode_to_str(matcode);
    int ret_code;

    ret_code = fprintf(f, "%s %s\n", MatrixMarketBanner, str);
    free(str);
    if (ret_code != 2)
        return MM_COULD_NOT_WRITE_FILE;
    else
        return 0;
}

int mm_write_mtx_crd(char fname[], int M, int N, int nz, int I[], int J[],
                     double val[], MM_typecode matcode)
{
    FILE *f;
    int i;

    if (strcmp(fname, "stdout") == 0)
        f = stdout;
    else if ((f = fopen(fname, "w")) == NULL)
        return MM_COULD_NOT_WRITE_FILE;

    /* print banner followed by typecode */
    fprintf(f, "%s ", MatrixMarketBanner);
    fprintf(f, "%s\n", mm_typecode_to_str(matcode));

    /* print matrix sizes and nonzeros */
    fprintf(f, "%d %d %d\n", M, N, nz);

    /* print values */
    if (mm_is_pattern(matcode))
        for (i = 0; i < nz; i++)
            fprintf(f, "%d %d\n", I[i], J[i]);
    else if (mm_is_real(matcode))
        for (i = 0; i < nz; i++)
            fprintf(f, "%d %d %20.16g\n", I[i], J[i], val[i]);
    else if (mm_is_complex(matcode))
        for (i = 0; i < nz; i++)
            fprintf(f, "%d %d %20.16g %20.16g\n", I[i], J[i], val[2 * i],
                    val[2 * i + 1]);
    else {
        if (f != stdout)
            fclose(f);
        return MM_UNSUPPORTED_TYPE;
    }

    if (f != stdout)
        fclose(f);

    return 0;
}


char *mm_typecode_to_str(MM_typecode matcode)
{
    char buffer[MM_MAX_LINE_LENGTH];
    char *types[4];
    int error = 0;

    /* check for MTX type */
    if (mm_is_matrix(matcode))
        types[0] = MM_MTX_STR;
    else
        error = 1;

    /* check for CRD or ARR matrix */
    if (mm_is_sparse(matcode))
        types[1] = MM_SPARSE_STR;
    else if (mm_is_dense(matcode))
        types[1] = MM_DENSE_STR;
    else
        return NULL;

    /* check for element data type */
    if (mm_is_real(matcode))
        types[2] = MM_REAL_STR;
    else if (mm_is_complex(matcode))
        types[2] = MM_COMPLEX_STR;
    else if (mm_is_pattern(matcode))
        types[2] = MM_PATTERN_STR;
    else if (mm_is_integer(matcode))
        types[2] = MM_INT_STR;
    else
        return NULL;


    /* check for symmetry type */
    if (mm_is_general(matcode))
        types[3] = MM_GENERAL_STR;
    else if (mm_is_symmetric(matcode))
        types[3] = MM_SYMM_STR;
    else if (mm_is_hermitian(matcode))
        types[3] = MM_HERM_STR;
    else if (mm_is_skew(matcode))
        types[3] = MM_SKEW_STR;
    else
        return NULL;

    sprintf(buffer, "%s %s %s %s", types[0], types[1], types[2], types[3]);
    return strdup(buffer);

}


/* ------ * Additions by dbindel * --- 
 * 
 * This is basically a hacked up version of the example code
 * on the Matrix Market web page.
 */

typedef struct {
    int i, j;
    double val;
} matrix_entry_t;


int entry_comparison(const void *e1, const void *e2)
{
    const matrix_entry_t *entry1 = (const matrix_entry_t *) e1;
    const matrix_entry_t *entry2 = (const matrix_entry_t *) e2;

    if (entry1->i < entry2->i)
        return -1;
    else if (entry1->i > entry2->i)
        return 1;

    if (entry1->j < entry2->j)
        return -1;
    else if (entry1->j > entry2->j)
        return 1;

    return 0;
}


csr_matrix_t *csr_mm_load(char *filename)
{
    int ret_code;
    MM_typecode matcode;
    FILE *f;

    int entry_count;
    int i, M, N, nz;
    matrix_entry_t *entries;
    matrix_entry_t *entry;

    int *row_start;
    int *col_index;
    double *val;

    csr_matrix_t *A;

    if ((f = fopen(filename, "r")) == NULL)
        exit(1);

    if (mm_read_banner(f, &matcode) != 0) {
        printf("Could not process Matrix Market banner.\n");
        exit(1);
    }

    if (!mm_is_sparse(matcode) || !mm_is_symmetric(matcode) ||
        !mm_is_real(matcode)) {

        printf("Sorry, this application does not support ");
        printf("Market Market type: [%s]\n", mm_typecode_to_str(matcode));
        exit(1);
    }

    /* find out size of sparse matrix .... */

    if ((ret_code = mm_read_mtx_crd_size(f, &M, &N, &nz)) != 0)
        exit(1);


    /* reserve memory for matrices */

    row_start = (int *) malloc((M + 1) * sizeof(int));
    col_index = (int *) malloc((2 * nz - M) * sizeof(int));
    val = (double *) malloc((2 * nz - M) * sizeof(double));

    entries =
        (matrix_entry_t *) malloc((2 * nz - M) * sizeof(matrix_entry_t));


    /* NOTE: when reading in doubles, ANSI C requires the use of the "l"  */
    /*   specifier as in "%lg", "%lf", "%le", otherwise errors will occur */
    /*  (ANSI C X3.159-1989, Sec. 4.9.6.2, p. 136 lines 13-15)            */

    entry = entries;
    entry_count = 0;

    for (i = 0; i < nz; i++) {

        int row, col;
        double val;

        fscanf(f, "%d %d %lg\n", &row, &col, &val);
        --row;                  /* adjust to 0-based */
        --col;

        assert(row >= 0 && col >= 0);
        assert(entry_count++ < 2 * nz - M);
        entry->i = row;
        entry->j = col;
        entry->val = val;
        ++entry;

        if (row != col) {       /* Fill out the other half... */
            assert(entry_count++ < 2 * nz - M);
            entry->i = col;
            entry->j = row;
            entry->val = val;
            ++entry;
        }
    }

    if (f != stdin)
        fclose(f);

    /**********************************/
    /* now make CSR version of matrix */
    /**********************************/

    nz = 2 * nz - M;
    qsort(entries, nz, sizeof(matrix_entry_t), entry_comparison);

    entry = entries;

    row_start[0] = 0;
    for (i = 0; i < nz; ++i) {
        row_start[entry->i + 1] = i + 1;
        col_index[i] = entry->j;
        val[i] = entry->val;
        ++entry;
    }

    free(entries);

    A = (csr_matrix_t *) malloc(sizeof(csr_matrix_t));
    A->m = M;
    A->n = N;
    A->nz = nz;
    A->row_start = row_start;
    A->col_idx = col_index;
    A->val = val;

    return A;
}


/* Load a sparse matrix in Matrix Market format and distribute
 * the values among processors
 */
csr_matrix_t *csr_hb_load(char *filename)
{
    csr_matrix_t *initial_matrix = NULL;
    csr_matrix_t *local_matrix = NULL;
    int *start;
    int i;
    int n_per_proc, nlocal, nzlocal;
    int first_nz;

	static int shared n;

    /*
    static shared int* row_start;
    row_start = (shared int*) upc_alloc(MAX_N * sizeof(int));
    static shared int* col_idx;
    col_idx = (shared int*) upc_alloc(MAX_NNZ * sizeof(int));
    static shared double* val;
    val = (shared double*) upc_alloc(MAX_NNZ * sizeof(double));
    */
    static shared [] int row_start[MAX_N];
    static shared [] int col_idx[MAX_NNZ];
    static shared [] double val[MAX_NNZ];
    //static int shared nz;

    /* Read the initial matrix at processor 0 and copy it
     * into shared space
     */
	

    if (MYTHREAD == 0) {
        int nz;

        initial_matrix = csr_mm_load(filename);
        //n is the number of rows of x and b
        //                   columns of A
        n = initial_matrix->n;

		printf("n = %d\n", n);

        nz = initial_matrix->nz;

        assert(nz < MAX_NNZ);
        assert(n < MAX_N);

        //row_start is the array that stores the starting index of each row
        for (i = 0; i <= n; ++i) 
            row_start[i] = initial_matrix->row_start[i];
	
        for (i = 0; i < nz; ++i) {
            col_idx[i] = initial_matrix->col_idx[i];
            val[i] = initial_matrix->val[i];
        }

        free(initial_matrix->val);
        free(initial_matrix->col_idx);
        free(initial_matrix->row_start);
        free(initial_matrix);
    }

    upc_barrier;


    /* Share out sections of the matrix to each processor */

    local_matrix = (csr_matrix_t *) malloc(sizeof(csr_matrix_t));
    local_matrix->n = n;
/*
    // n_per_proc number of rows are distributed to each thread.
    // all threads other than the last one are responsible for equal number of
    // rows, the last one is responsible for the left over rows
    n_per_proc = n / THREADS;
    // start is the array that stores which row we are dealing with at each
    // thread.
    start = (int*) malloc((THREADS + 1) * sizeof(int));

    assert(THREADS <= MAX_THREADS);
    for (i = 0; i < THREADS; ++i) {
        start[i] = i * n_per_proc;
        //local_matrix->localStart[i] = start[i];
    }
*/
	//more on balancing issue	
    start = (int*) malloc((THREADS + 1) * sizeof(int));

	start[0] = 0;
	int last_start = 0;

	int load_per_proc = (row_start[n] - row_start[0])/THREADS;

	//find the load for each proc nearest to the load per proc
	for(i = 1; i <= n; i++)
	{
		int cur_load = row_start[i] - row_start[start[last_start]]; 

		if(cur_load >= load_per_proc)
		{
			//compare with the previous to find the nearest 
			int pre_load = row_start[i - 1] - row_start[start[last_start]];

			last_start++;	

			if((load_per_proc - pre_load) > (cur_load - load_per_proc))
			{
				start[last_start] = i;	
			}
			else
			{
				start[last_start] = i - 1;
			}
			
			//make sure that the number of threads does not exceed THREADS
			if(last_start == THREADS) 
				break;
		}
	}	


    start[THREADS] = n;
    //local_matrix->localStart[THREADS] = start[THREADS];
    // nlocal is the number of rows for each thread
    nlocal = start[MYTHREAD + 1] - start[MYTHREAD];

    // store nlocal in m of local_matrix
    local_matrix->m = nlocal;
    // store the starting index to localStart
    local_matrix->myStart = start[MYTHREAD];

    local_matrix->row_start = (int *) malloc((nlocal + 1) * sizeof(int));

    // index to the first none-zero for each processor
    first_nz = row_start[start[MYTHREAD]];
    for (i = 0; i <= nlocal; ++i) {
        // find the starting pointer for each row, normalize it to for row 0, 1,
        // etc, then set it to row_start's for local_matrix
        local_matrix->row_start[i] = row_start[start[MYTHREAD] + i] - first_nz;
    }

    // number of none-zeros in the local processor
    nzlocal = row_start[start[MYTHREAD + 1]] - row_start[start[MYTHREAD]];

	//printf("thread: %d,n = %d, load per proc %d, start %d, last start %d, nzlocal = %d\n", MYTHREAD, n, load_per_proc, start[MYTHREAD], last_start, nzlocal);

    local_matrix->nz = nzlocal;

    local_matrix->col_idx = (int *) malloc(nzlocal * sizeof(int));
    local_matrix->val = (double *) malloc(nzlocal * sizeof(double));
    assert(local_matrix->col_idx != NULL);
    assert(local_matrix->val != NULL);

#define max(a, b) (a > b ? a : b)
#define min(a, b) (a < b ? a : b)
	local_matrix->last_col = 0;
	local_matrix->first_col = n;

    for (i = 0; i < nzlocal; ++i) {
        local_matrix->col_idx[i] = col_idx[first_nz + i];
        local_matrix->val[i] = val[first_nz + i];

		local_matrix->last_col = max(local_matrix->last_col, col_idx[first_nz + i]);
		local_matrix->first_col = min(local_matrix->first_col, col_idx[first_nz + i]);
    }

    free(start);

    upc_barrier;

    return local_matrix;
}
