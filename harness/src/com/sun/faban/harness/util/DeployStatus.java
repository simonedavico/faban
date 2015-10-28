package com.sun.faban.harness.util;

/***
 * A class representing the status of a deploy request
 * through a descriptive code:
 * 
 * 0: if the benchmark is correctly deployed on the harness
 *    (201 CREATED)
 * 1: if the file is not found on the user's system
 *    (FileNotFoundException)
 * 2: a more generic I/O exception
 *    (IOException)
 * 3: a conflict (the benchmark is running or scheduled to be run)
 * 	  (409 CONFLICT)
 * 4: benchmark not acceptable (not a jar, name with dots)
 *    (406 NOT ACCEPTABLE)
 * 5: other, undefined error
 * 6: no benchmark specified as argument to the fabancli
 * 
 * The choice for this implementation is that we need to return a
 * descriptive value in range 0-255 to the ./fabancli shell script      
 *
 * @author simonedavico
 *
 */
public class DeployStatus {

	public static enum CODE { DEPLOYED, FILE_NOT_FOUND, 
					   IO_EXCEPTION, CONFLICT, 
					   NOT_ACCEPTABLE, UNDEFINED_ERROR,
					   NO_BENCHMARK_SPECIFIED };

}
