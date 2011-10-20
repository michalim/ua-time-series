package edu.arizona.cs.learn.timeseries.datageneration;

import java.io.File;

import edu.arizona.cs.learn.timeseries.experiment.SyntheticExperiments;
import edu.arizona.cs.learn.timeseries.prep.SymbolicData;

/**
 * This class handles generating synthetic data from
 * the simulator written by Naill Adams (Imperial College)
 * 
 * The simulator generates a time-series by randomly sampling
 * from a a p-dimensional gaussian.  We can control the means
 * of the gaussian as well as the covariance structure.
 * 
 * The newest addition is the ability to control the structure
 * of the time series generated from this simulator
 * 	
 * Originally, we only generated datasets of the following form ABA
 * where A contains a specific length episode wherein all of the dynamics
 * are the same.  B contains another episode where the dynamics had changed.
 * With this we were able to generate multiple classes, AAA and ABA and
 * then test classification and clustering accuracy.
 * 
 * Now we can add one additional episodes to begin to look at ordering
 * effects.  So we now have classes with the structures ABCA and ACBA.
 * If the classification method doesn't take into account temporal ordering
 * it will fail to find any differences between these two classes.
 * @author kerrw
 *
 */
public class SyntheticData {

	public static int N = 60;
	public static int STREAMS = 6;
	
	/**
	 * Call the Rscript that generates instances of a specific class
	 * @param pid -- the process-id of the running program.  Used to uniquely identify the results
	 * @param className -- the name we are planning to give to the generated class of instances
	 * @param mean -- the mean of the p-dimensional gaussian
	 * @param pct -- the covariance as a percentage interpolated between two covariance matrices
	 * @param eLength -- the length of each episodes
	 */
	public static void generateABA(String pid, String className, double mean, double pct, int eLength) { 		
		generateABA(pid, className, STREAMS, mean, pct, eLength);
	}
	
	/**
	 * Call the Rscript that generates instances of a specific class
	 * @param pid - the process-id of the running program.  Used to uniquely identify the results
	 * @param className - the name we are planning to give to the generated class of instances
	 * @param streams - the number of streams
	 * @param mean - the mean of the p-dimensional gaussian
	 * @param pct - the covariance as a percentage interpolated between two covariance matrices
	 * @param eLength - the length of each episode
	 */
	public static void generateABA(String pid, String className, int streams, double mean, double pct, int eLength) { 		
		String prefix = "/tmp/synthetic-" + pid + "/";
		File f = new File(prefix);
		if (!f.exists())
			f.mkdir();
		
		try {			
			String cmd = "scripts/sim.R " + prefix + className + " " + streams + " " + eLength + " " + mean + " " + pct;
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec("Rscript " + cmd);
			p.waitFor();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		SymbolicData.convert(prefix + className, prefix + "synthetic-" + className +".lisp", N);
	}
	
	/**
	 * Call the Rscript that generates instances of a specific class
	 * @param pid -- the process-id of the running program.  Used to uniquely identify the results
	 * @param className -- the name we are planning to give to the generated class of instances
	 * @param means -- the means of the p-dimensional gaussian -- need two of them
	 * @param pcts -- the covariance as a percentage interpolated between two covariance matrices
	 * @param eLength -- the length of each episodes
	 */
	public static void generateABCA(String pid, String className, double[] means, double[] pcts, int eLength) { 		
		generateABCA(pid, className, STREAMS, means, pcts, eLength);
	}
	
	
	/**
	 * Call the Rscript that generates instances of a specific class
	 * @param pid - the process-id of the running program.  Uniquely identifies the results
	 * @param className - the name we are planning to give to the generated class of instances
	 * @param streams - the number of streams.
	 * @param means - the means of the p-dimensional gaussians -- need two of them
	 * @param pcts - covariance as a percentage interpolated between two covariance matrices
	 * @param eLength - the length of each episode
	 */
	public static void generateABCA(String pid, String className, int streams, double[] means, double[] pcts, int eLength) {
		String prefix = "/tmp/synthetic-" + pid + "/";
		File f = new File(prefix);
		if (!f.exists())
			f.mkdir();
		
		try {			
			StringBuffer cmd = new StringBuffer("scripts/sim2.R ");
			cmd.append(prefix + className + " " + streams + " " + eLength + " ");
			cmd.append(means[0] + " " + pcts[0] + " ");
			cmd.append(means[1] + " " + pcts[1] + " ");
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec("Rscript " + cmd.toString());
			p.waitFor();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		SymbolicData.convert(prefix + className, prefix + "synthetic-" + className +".lisp", N);
	}
}
