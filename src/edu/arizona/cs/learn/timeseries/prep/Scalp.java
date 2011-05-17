package edu.arizona.cs.learn.timeseries.prep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.arizona.cs.learn.timeseries.model.Interval;
import edu.arizona.cs.learn.timeseries.prep.Scalp.Variable;

public class Scalp {
	
	public enum Variable { 
		drowsy, dist, engagement, workload
	}

	private String _workingDir;
	private List<User> _list;
	
	public Scalp(String workingDir) { 
		_workingDir = workingDir;
	}
	
	public void load(String fileName) throws Exception { 
		BufferedReader in = new BufferedReader(new FileReader(_workingDir + fileName));
		// remove the header information
		String header = in.readLine();

		_list = new ArrayList<User>();
		User current = null;
		while (in.ready()) { 
			String line = in.readLine();
			String[] tokens = line.split("[,]");

			int userId = Integer.parseInt(tokens[0]);
			if (current == null || current.id != userId) {
				current = new User(userId);
				_list.add(current);
			}
			
			current.addRow(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]),
					Double.parseDouble(tokens[3]), Double.parseDouble(tokens[4]));
		}
		in.close();
	}
	
//	public void standardize(String prefix) throws Exception { 
//		BufferedWriter out = new BufferedWriter(new FileWriter(_workingDir + "standardized/" + prefix + ".csv"));
//		out.write("user,time,drowsy,dist,eng,wkld,drowsySTD,distSTD,engSTD,wkldSTD\n");
//		for (User u : _list) { 
//			BufferedWriter local = new BufferedWriter(new FileWriter(_workingDir + "standardized/" + prefix + "-" + u.id + ".csv"));
//			local.write("user,time,drowsy,dist,eng,wkld,drowsySTD,distSTD,engSTD,wkldSTD\n");
//
//			List<Double> drowsy = TimeSeries.standardize(u.drowsy);
//			List<Double> dist = TimeSeries.standardize(u.dist);
//			List<Double> eng = TimeSeries.standardize(u.eng);
//			List<Double> wkld = TimeSeries.standardize(u.wkld);
//
//			for (int i = 0; i < u.size; ++i) { 
//				String normal = u.drowsy.get(i) + "," + u.dist.get(i) + "," + u.eng.get(i) + "," + u.wkld.get(i);
//				String std = drowsy.get(i) + "," + dist.get(i) + "," + eng.get(i) + "," + wkld.get(i);
//				
//				String line = u.id + "," + (i+1) + "," +  normal + "," + std;
//				out.write(line + "\n");
//				local.write(line + "\n");
//			}
//			local.close();
//		}
//		out.close();
//	}
//	
	public void toLisp(String prefix) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(_workingDir + "lisp/" + prefix + ".lisp"));
		for (int i = 0; i < _list.size(); ++i) { 
			User u = _list.get(i);
			u.standardize();
			
			List<Interval> intervals = new ArrayList<Interval>();
			for (Variable v : Variable.values()) { 
				List<Double> values = u.std.get(v);
				List<String> sax = TimeSeries.sax(values, 5);
				List<String> sdl = TimeSeries.sdl(TimeSeries.diff(values));
				
				intervals.addAll(TimeSeries.toIntervals(v.toString(), sax));
				intervals.addAll(TimeSeries.toIntervals(v.toString(), sdl));
			}
			
			out.write("(" + u.id + "\n");
			out.write(" (\n");
			
			for (Interval interval : intervals)
				out.write("  (\"" + interval.name + "\" " + interval.start + " " + interval.end + ")\n");
			
			out.write(" )\n");
			out.write(")\n");
		}
		out.close();
	}
	
	public static void main(String[] args) throws Exception { 
		Scalp s = new Scalp("/Users/wkerr/data/federico/");
		s.load("raw/SATTriangle07-H_right.csv");
		s.toLisp("SATTriangle07-H_right");
//		s.standardize("SATTriangle07-H_wrong");		
	}
}


class User { 
	public int id;
	public int size = 0;
	
	public Map<Variable,List<Double>> map;
	public Map<Variable,List<Double>> std;
	
	public User(int id) { 
		this.id = id;

		map = new HashMap<Variable,List<Double>>();
		for (Variable v : Variable.values())
			map.put(v, new ArrayList<Double>());
	}
	
	public void addRow(double drowsy, double dist, double eng, double wkld) { 
		map.get(Variable.drowsy).add(drowsy);
		map.get(Variable.dist).add(dist);
		map.get(Variable.engagement).add(eng);
		map.get(Variable.workload).add(wkld);
		
		++size;
	}
	
	public void standardize() { 
		std = new HashMap<Variable,List<Double>>();
		for (Variable v : Variable.values()) { 
			std.put(v, TimeSeries.standardize(map.get(v)));
		}
	}
}