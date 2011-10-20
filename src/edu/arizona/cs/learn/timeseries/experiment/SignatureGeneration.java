package edu.arizona.cs.learn.timeseries.experiment;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.arizona.cs.learn.algorithm.alignment.Similarity;
import edu.arizona.cs.learn.timeseries.datageneration.SyntheticData;
import edu.arizona.cs.learn.timeseries.model.Instance;
import edu.arizona.cs.learn.timeseries.model.SequenceType;
import edu.arizona.cs.learn.timeseries.model.signature.Signature;
import edu.arizona.cs.learn.util.RandomFile;
import edu.arizona.cs.learn.util.Utils;

public class SignatureGeneration {
    private static Logger logger = Logger.getLogger(SignatureGeneration.class);

	public static void main(String[] args) { 
		Utils.LIMIT_RELATIONS = true;
		Utils.WINDOW = 5;
//		signature("chpt1-approach", SequenceType.tree, Similarity.alignment);
//		signature("ww3d-jump-over", SequenceType.allen, Similarity.strings);
//		sequence();
		
		syntheticSignature(0.05, 0.1);
	}
	
	public static void signature(String name, SequenceType type, Similarity sim) { 
		String file = "data/input/" + name + ".lisp";

    	List<Instance> instances = Instance.load(name, new File(file), type);
		logger.debug("Training signature on " + instances.size() + " sequences");

    	// now construct a copy and randomly shuffle it.
//    	Random r = new Random(System.currentTimeMillis());
//    	List<Instance> random = new ArrayList<Instance>(instances);
//    	Collections.shuffle(random, r);
    	
    	Signature s1 = new Signature(name, sim);
    	for (Instance instance : instances) { 
    		logger.debug("  Instance " + instance.id());
    		s1.update(instance.sequence());
    	}
    	
//    	s1 = s1.prune(s1.trainingSize() / 2);
    	s1.toXML(name + "-" + type + "-" + sim + ".xml");

//    	for (String method : new String[] { "single", "complete", "average" }) { 
//        	Signature s2 = Signature.agglomerativeTraining(method, instances);
//        	s2 = s2.prune(s2.trainingSize() / 2);
//        	s2.toXML("/Users/wkerr/Desktop/signature-" + method + ".xml");
//    	}

//    	generator = SequenceFactory.forwardAllenGenerator();
//    	instances = generator.sequences(name, "data/input/" + name + ".lisp");
//    	s = new Signature(name);
//    	s.train(instances);
//    	s.toFile("/tmp/signature2.signature", s.trainingSize()/2);
	}
	
	public static void syntheticSignature(double mean, double pct) { 
		String pid = RandomFile.getPID();
		String dir = "/tmp/synthetic-" + pid + "/";
		
		SyntheticData.generateABA(RandomFile.getPID(), "f", 0.025, 0.1, 100);
		Map<String,List<Instance>> map = Utils.load(dir, "synthetic-", SequenceType.allen);
		for (String key : map.keySet()) { 
			System.out.println("Building Signature for : " + key);
	    	Signature s = new Signature(key, Similarity.strings);
	    	for (Instance instance : map.get(key)) { 
	    		logger.debug("  Instance " + instance.id());
	    		s.update(instance.sequence());
	    	}
		}
	}
	
	/**
	 * I just want to generate a sequence to see how large it is.
	 */
	public static void sequence() { 
		for (int i = 1; i <= 5; ++i) { 
			String name = "ww2d-test-" + i;
			List<Instance> instances = Instance.load(name, new File("data/input/" + name + ".lisp"), SequenceType.allen);
			logger.debug("Sequence Size: " + instances.get(0).sequence().size());
		}
	}
}
