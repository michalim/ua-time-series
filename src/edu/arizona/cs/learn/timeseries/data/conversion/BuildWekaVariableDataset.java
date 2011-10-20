package edu.arizona.cs.learn.timeseries.data.conversion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.arizona.cs.learn.timeseries.data.generation.SyntheticData;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.symbols.ProportionSymbol;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;
import edu.arizona.cs.learn.util.DataMap;
import edu.arizona.cs.learn.util.Utils;

/**
 * We are building different types of datasets so that we can quickly
 * test specific machine learning / data mining techniques that are built
 * into Weka.  Random Forests, Decision Trees, Naive Bayes are all classification
 * techniques that are easily executed within Weka.
 * 
 * 
 * @author wkerr
 *
 */
public class BuildWekaVariableDataset {

    private static Logger logger = Logger.getLogger(BuildWekaVariableDataset.class);
    
    public static void main(String[] args) { 
//    	String prefix = "wes-pen";
//    	makeWekaFiles("wes-pen");
    	generateSynthetic(0.0, 0.5);
    }
    
    public static void generateSynthetic(double mean, double pct) { 
		String d1 = SyntheticData.generateABA("f", 0, 0, 150);
		String d2 = SyntheticData.generateABA("g", mean, pct, 150);
		
		if (!d1.equals(d2)) 
			throw new RuntimeException("Broken assumption.  The directories should be the same");

		makeWekaFiles(d1, SyntheticData.PREFIX);
    }
    
    public static void makeWekaFiles(String dir, String prefix) { 
    	writeBinaryWeka(dir, prefix);
    	writeIntegerWeka(dir, prefix);
    	writeRealWeka(dir, prefix);
    }
    
    /**
     * will write out a Weka ARFF file that contains instances
     * where each instance contains a sequence of propositions
     * and each proposition can either be either true or false.
     * 
     * If a proposition turns on during an episode it is considered
     * true for that instance.  False is only for propositions that
     * do not turn on at all during the instance.
     * @param prefix
     */
    public static void writeBinaryWeka(String dir, String prefix) { 
    	Map<String,List<Instance>> instanceMap = Utils.load(dir, prefix, SequenceType.starts);
    	
    	Set<String> variableSet = new HashSet<String>();
    	List<String> classes = new ArrayList<String>();
    	
    	Map<String,List<Set<String>>> map = new HashMap<String,List<Set<String>>>();
    	
		for (String className : instanceMap.keySet()) { 
			classes.add(className);
			logger.debug("class name: [" + className + "]");
			
			List<Set<String>> episodes = new ArrayList<Set<String>>();
			map.put(className, episodes);

			for (Instance instance : instanceMap.get(className)) { 
				Set<String> propSet = new HashSet<String>();
				episodes.add(propSet);
				
				for (Interval i : instance.intervals()) { 
					variableSet.add(DataMap.getKey(i.keyId));
					propSet.add(DataMap.getKey(i.keyId));
				}
			}
		}

		try { 
			BufferedWriter out = new BufferedWriter(new FileWriter("data/weka/" + prefix + "-b.arff"));
			out.write("% This a simple demonstration that variables have for classification \n");
			out.write("% dataset generated for " + prefix + "\n");
			out.write("% from files ---- \n");
			for (String s : map.keySet()) { 
				out.write("%   " + s + "\n");
			}

			out.write("@relation " + prefix + "\n\n");
			List<String> variables = new ArrayList<String>(variableSet);
			Collections.sort(variables);
			for (String v : variables) { 
				out.write("@attribute \"" + v + "\" {TRUE,FALSE}\n");
			}
			out.write("\n");
			StringBuffer buf = new StringBuffer();
			for (String c : classes) { 
				buf.append(c + ",");
			}
			buf.deleteCharAt(buf.length()-1);
			out.write("@attribute classLabel {" + buf.toString() + "}\n");
			out.write("\n");
			out.write("@data\n");
			
			for (String className : map.keySet()) { 
				List<Set<String>> episodes = map.get(className);
				for (Set<String> episode : episodes) { 
					for (String v : variables) { 
						if (episode.contains(v))
							out.write("TRUE,");
						else
							out.write("FALSE,");
					}
					out.write(className + "\n");
				}
			}
			out.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
    }
    
    
    /**
     * will write out a Weka ARFF file that contains instances
     * where each instance contains a list of propositions that
     * are represented by a number >= 0 meaning the number of steps
     * that the proposition was on for that instance.
     * 
     * @param classNames
     */
    public static void writeIntegerWeka(String dir, String prefix) { 
    	Map<String,List<Instance>> map = Utils.load(dir, prefix, SequenceType.proportionOn);
    	
    	Set<String> variableSet = new HashSet<String>();
    	Map<String,List<Map<String,Integer>>> onMap = new HashMap<String,List<Map<String,Integer>>>();
    	for (String className : map.keySet()) { 
    		
    		// Add in the list of mappings to the overall list.
    		List<Map<String,Integer>> list = new ArrayList<Map<String,Integer>>();
    		onMap.put(className, list);
    		
    		for (Instance instance : map.get(className)) { 
    			Map<String,Integer> tmpMap = new HashMap<String,Integer>();
    			List<Symbol> sequence = instance.sequence();
    			for (int i = 0; i < sequence.size(); ++i) { 
    				ProportionSymbol ps = (ProportionSymbol) sequence.get(i);
    				variableSet.add(DataMap.getKey(ps.propId()));
    				
    				tmpMap.put(DataMap.getKey(ps.propId()), ps.timeOn());
    			}
    			list.add(tmpMap);
    		}
    	}

		try { 
			BufferedWriter out = new BufferedWriter(new FileWriter("data/weka/" + prefix + "-i.arff"));
			out.write("% This a simple demonstration that variables have for classification \n");
			out.write("% dataset generated for " + prefix + "\n");
			out.write("% from files ---- \n");
			for (String s : onMap.keySet()) { 
				out.write("%   " + s + "\n");
			}

			out.write("@relation " + prefix + "\n\n");
			List<String> variables = new ArrayList<String>(variableSet);
			Collections.sort(variables);
			for (String v : variables) { 
				out.write("@attribute \"" + v + "\" integer\n");
			}
			out.write("\n");
			StringBuffer buf = new StringBuffer();
			for (String c : onMap.keySet()) { 
				buf.append(c + ",");
			}
			buf.deleteCharAt(buf.length()-1);
			out.write("@attribute classLabel {" + buf.toString() + "}\n");
			out.write("\n");
			out.write("@data\n");
			
			for (String className : onMap.keySet()) { 
				for (Map<String,Integer> tmpMap : onMap.get(className)) {

					// Iterate across *all* potential variables...0 if not ever on
					// and the actual value if found.
					for (String v : variables) { 
						if (tmpMap.containsKey(v))
							out.write(tmpMap.get(v) + ",");
						else
							out.write("0,");
					}
					out.write(className + "\n");
				}
			}
			out.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
    }
    
    /**
     * will write out a Weka ARFF file that contains instances
     * where each instance contains a list of propositions that
     * are represented by a number >= 0 and <= 1 meaning the proportion of 
     * time steps that the proposition was on for that instance.
     * 
     * @param classNames
     */
    public static void writeRealWeka(String dir, String prefix) { 
    	Map<String,List<Instance>> map = Utils.load(dir, prefix, SequenceType.proportionOn);
    	
    	Set<String> variableSet = new HashSet<String>();
    	Map<String,List<Map<String,Double>>> onMap = new HashMap<String,List<Map<String,Double>>>();
    	for (String className : map.keySet()) { 
    		
    		// Add in the list of mappings to the overall list.
    		List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
    		onMap.put(className, list);
    		
    		for (Instance instance : map.get(className)) { 
    			Map<String,Double> tmpMap = new HashMap<String,Double>();
    			List<Symbol> sequence = instance.sequence();
    			for (int i = 0; i < sequence.size(); ++i) { 
    				ProportionSymbol ps = (ProportionSymbol) sequence.get(i);
    				variableSet.add(DataMap.getKey(ps.propId()));
    				
    				tmpMap.put(DataMap.getKey(ps.propId()), (double) ps.timeOn() / (double) ps.duration());
    			}
    			list.add(tmpMap);
    		}
    	}

		try { 
			BufferedWriter out = new BufferedWriter(new FileWriter("data/weka/" + prefix + "-r.arff"));
			out.write("% This a simple demonstration that variables have for classification \n");
			out.write("% dataset generated for " + prefix + "\n");
			out.write("% from files ---- \n");
			for (String s : onMap.keySet()) { 
				out.write("%   " + s + "\n");
			}

			out.write("@relation " + prefix + "\n\n");
			List<String> variables = new ArrayList<String>(variableSet);
			Collections.sort(variables);
			for (String v : variables) { 
				out.write("@attribute \"" + v + "\" real\n");
			}
			out.write("\n");
			StringBuffer buf = new StringBuffer();
			for (String c : onMap.keySet()) { 
				buf.append(c + ",");
			}
			buf.deleteCharAt(buf.length()-1);
			out.write("@attribute classLabel {" + buf.toString() + "}\n");
			out.write("\n");
			out.write("@data\n");
			
			for (String className : onMap.keySet()) { 
				for (Map<String,Double> tmpMap : onMap.get(className)) {

					// Iterate across *all* potential variables...0 if not ever on
					// and the actual value if found.
					for (String v : variables) { 
						if (tmpMap.containsKey(v))
							out.write(tmpMap.get(v) + ",");
						else
							out.write("0,");
					}
					out.write(className + "\n");
				}
			}
			out.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
    }
}
