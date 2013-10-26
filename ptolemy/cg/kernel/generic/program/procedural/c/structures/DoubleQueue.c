/*** declareBlock() ***/
typedef struct DoubleCell DoubleCell;
struct DoubleCell {
        double token;
        DoubleCell * next;
};

typedef struct DoubleQueue DoubleQueue;
struct DoubleQueue {
        DoubleCell * first;
        DoubleCell * last;
};
/**/

/*** funcDeclareBlock() ***/
void DoubleQueueClear(DoubleQueue * q);
double DoubleQueueGet(DoubleQueue * q);
double DoubleQueueTake(DoubleQueue * q);
boolean DoubleQueueHasToken(DoubleQueue * q);
void DoubleQueuePut(DoubleQueue * q, double d);
/**/

/*** funcImplementationBlock() ***/
// Simply deletes properly all the elements in the queue
void DoubleQueueClear(DoubleQueue * q) {
        if (q == NULL)
                return;
        if (q->first == NULL)
                return;
        if (q->first == q->last)
                free(q->first);
        else
        {
                DoubleCell * lastToken = NULL;
                DoubleCell * t;
                for(t = q->first ; t != q->last ; t = t->next) {
                        free(lastToken);
                        lastToken = t;
                }
                free(lastToken);
        }
        return;
}

// Returns the first element of the queue
double DoubleQueueGet(DoubleQueue * q) {
        if (!DoubleQueueHasToken(q))
                return -1.0;

        DoubleCell * result = q->first;

        return result->token;
}

// Returns the first element of the queue and deletes it
double DoubleQueueTake(DoubleQueue * q) {
        if (!DoubleQueueHasToken(q))
                return -1.0;

        DoubleCell * result = q->first;
        q->first = result->next;
        // In the case we removed the last element of the queue
        if (q->first == NULL)
                q->last = NULL;

        return result->token;
}

// Returns a boolean which says if there are token in the receiver
boolean DoubleQueueHasToken(DoubleQueue * q) {
        return (q != NULL && q->first != NULL);
}

// Puts a clone of the token t in the queue
void DoubleQueuePut(DoubleQueue * q, double d) {
        if (q == NULL) {
                q = malloc(sizeof(DoubleQueue));
                if (q == NULL) {
                        perror("Allocation error (DoubleQueuePut)");
                        exit(1);
                }
                DoubleCell * newToken = malloc(sizeof(DoubleCell));
                if (newToken == NULL) {
                        perror("Allocation error (DoubleQueuePut)");
                        exit(1);
                }
                newToken->token = d;
                q->first = q->last = newToken;
        }
        else if (!DoubleQueueHasToken(q)) {
                DoubleCell * newToken = malloc(sizeof(DoubleCell));
                if (newToken == NULL) {
                        perror("Allocation error (DoubleQueuePut)");
                        exit(1);
                }
                newToken->token = d;
                newToken->next = NULL;
                q->first = q->last = newToken;
        }
        else {
                DoubleCell * newToken = malloc(sizeof(DoubleCell));
                if (newToken == NULL) {
                        perror("Allocation error (DoubleQueuePut)");
                        exit(1);
                }
                newToken->token = d;
                newToken->next = NULL;
                q->last->next = newToken;
                q->last = newToken;
        }
        return;
}
/**/

