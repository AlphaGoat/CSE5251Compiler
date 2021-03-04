JAVAC  := javac
JAVACC := javacc

default : compiler

compiler : scan.jar parse.jar

# options: c--create; f--name of jar file; e--entry point
scan.jar :
	$(JAVAC) */*.java
	jar cfe $@ main.Scan parser/*.class main/*.class 
	
parse.jar :
	jar cfe $@ main.Parser parser/*.class main/*.class
	
clean :
	/bin/rm -f scan.jar parse.jar
	/bin/rm -f */*.class