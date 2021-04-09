package test;

import java.util.ArrayList;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

public class UseCases {

	// Run main on series of files and log any times it fails
	public static void main(String args[]) {
		// Initialize 'Compiler' class
		main.Compiler compiler = new main.Compiler();
//		PrintStream printStream;
		
//		try {
//			printStream = new PrintStream("/home/alphagoat/eclipse-workspace/MiniJavaCompiler/logs/log.txt");
//			System.setErr(printStream);
//			System.setOut(printStream);
				
		/* Run unit tests */
		
		/* GOOD SAMPLES (should compile succesfully) */
		System.out.println("========================================");
		System.out.println("===========GOOD SAMPLES=================");
		System.out.println("=======(Should compile succesfully)=====");
		System.out.println("========================================");
		System.out.println("\n");
		int num_errors;
		int j = 0;
		String goodSamplePath = "/home/alphagoat/CodingPractice/Java/CSE_5251/MiniJavaSamples/GoodSamples";
		File goodDir = new File(goodSamplePath);
		for ( File file : goodDir.listFiles() ) {
			try {
				String f = file.toString();
				String[] filenames = { f };
				num_errors = compiler.compile(f, false);
				if (num_errors > 0) {
					System.out.printf("\nUnits Test %d failed test: %s\n", j, f);
				}
				j++;
				
			} catch (Exception e) {
				System.out.printf("\nUnits Test %d for %s exited with error: %s\n",
						j, file.toString(), e.getMessage());
			}
		} 
		
		/* BAD SAMPLES (should display list of errors) */
		j = 0;
		System.out.println("\n");
		System.out.println("========================================");
		System.out.println("============BAD SAMPLES=================");
		System.out.println("==========(Should Throw Errors)=========");
		System.out.println("========================================");
		System.out.println("\n");
		
		String badSamplePath = "/home/alphagoat/CodingPractice/Java/CSE_5251/MiniJavaSamples/BadSamples";
		File badDir = new File(badSamplePath);
		for ( File file : badDir.listFiles() ) {
			String f = file.toString();
			try {
				String[] filenames = { f };
				num_errors = compiler.compile(f, false);
				if (num_errors == -1) {
					System.out.printf("\nUnits Test %d failed (could not find file): %s\n", j, f);
				}
				
				if (num_errors == 0) {
					System.out.printf("\nUnits Test %d failed test: %s\n", j, f);
				}
				j++;
				
			} catch (Exception e) {
				System.out.printf("\nUnits Test %d with file %s exited with error: %s\n",
						j, f, e.getMessage());
			}
			
//				printStream.close();
		} 
	
//		} catch (FileNotFoundException e) {
//			System.out.println(e.getMessage());
//			System.exit(1);
//		}
//		
		
			
	}
	
	
}
