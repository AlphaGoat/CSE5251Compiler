#!/bin/bash 
dir=$(dirname $1)
base=$(basename $1 .java)
file=$dir/$base
run=.

# Assume files $1, $run/macro.s, and $run/runtime.o exist
# creates file $file
echo "$dir"
#echo "$base"
#echo "$file"
/bin/rm -f $file.s 
java -jar compile.jar $1
sparc-linux-as -g -Asparc $file.s -o $file.o
## do not delete $file.s; leave for debugging and evaluation
sparc-linux-ld -dynamic-linker /lib/ld-uClibc.so.0 -e start $file.o $run/runtime.o -lc -o $file
/bin/rm -f $file.o # $1.s delete temporary files
