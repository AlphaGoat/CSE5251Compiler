/*
 * Author: Peter Thomas, pthomas2019@my.fit.edu
 * Course: CSE 5251, Section 01, Spring 2021
 */
#include <stdlib.h>
#include <stdio.h>

/*
 * compile with 'gcc':
 * sparc-linux-gcc -c runtime.c -o runtime.o
 */
int print_int(int n) {
    printf("%d\n", n);
    fflush(stdout);
    return 1;
}

int * alloc_object(int n) {
    return malloc(n);
}

int * alloc_array(int n) {
    return malloc(n);
}
