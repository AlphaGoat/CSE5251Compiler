Author: Peter Thomas, pthomas2019@my.fit.edu
Course: CSE 5251, Section 01, Spring 2021
Assignment: Asgn 8

Steps to building compiler:

make SUPPORT=/path/to/support.jar

The make script will create an executable jar "compile.jar" as well as the runtime object file "runtime.o" needed by the assemble shell script.
It will also make the assemble shell script executable by the user.

Steps to compiling and linking for executable:

./assemble /path/to/Program/to/compile/and/link

To create an assembly file without executable, you can just call the jar directly:

java -jar compile.jar  /path/to/Program/to/compile

The compiler has been tested in directories outside of the source directory successfully. Additionall,
the assemble shell script has also been tested in other directories PROVIDED THAT it is still in the 
same directory as "compile.jar", "Macros.s", and "runtime.o".

The compiler successfully compiles all 8 of Appel's Mini Java Programs.
The following compiled programs execute correctly:

1. Factorial.java
2. LinearSearch.java
3. BinarySearch.java
4. BinaryTree.java
5. QuickSort.java
6. BubbleSort.java

TreeVisitor.java compiles, executes without errors, and terminates successfully. However, it does not display the correct output.

 LinkedList.java also compiles and executes without any errors, but is placed  in an infinite loop.
 
 Compilation of the above examples demonstrates the compiler's ability to handle recursion, array access, field access, and register spilling.
 Execution of BinaryTree also shows that the compiler is able to handle object oriented programming functionality such as object creation and assignment.

 The included test file in the examples directory ("MergeSort.java") tests the compiler's ability to perform array access and recursion.