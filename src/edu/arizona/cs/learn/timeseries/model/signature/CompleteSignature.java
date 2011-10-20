package edu.arizona.cs.learn.timeseries.model.signature;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import edu.arizona.cs.learn.algorithm.alignment.Params;
import edu.arizona.cs.learn.algorithm.alignment.Report;
import edu.arizona.cs.learn.algorithm.alignment.SequenceAlignment;
import edu.arizona.cs.learn.algorithm.alignment.Similarity;
import edu.arizona.cs.learn.timeseries.model.symbols.Symbol;

/**
 * The CompleteSignature is a signature that also maintains
 * the table for multiple sequence alignment.  This table is
 * only really necessary for building FSM recognizers and 
 * requires a lot of memory, so in order to reduce the memory
 * footprint of a signature we have subclassed Signature and
 * moved the table code here.
 * @author kerrw
 *
 */
public class CompleteSignature extends Signature {
	private List<Symbol[]> _rows;

	public CompleteSignature(String key) { 
		this(key, Similarity.strings);
	}
	
	public CompleteSignature(String key, Similarity similarity) { 
		super(key, similarity);
		
		_rows = new ArrayList<Symbol[]>();
	}
	
	public List<Symbol[]> table() {
		return _rows;
	}
	
	@Override
	public void update(List<Symbol> seq) {
		_count += 1;

		_params.seq1 = _signature;
		_params.seq2 = seq;
		
		Report report = SequenceAlignment.align(_params);
		updateTable(report);

		_signature = SequenceAlignment.combineAlignments(report.results1, report.results2);
	}

	/**
	 * Update the table given the sequence alignment report.
	 * @param report
	 */
	private void updateTable(Report report) {
		int newLength = report.results1.size();
		List<Symbol[]> tmp = new ArrayList<Symbol[]>(_rows.size() + 1);
		for (int i = 0; i < this._rows.size() + 1; i++) {
			tmp.add(new Symbol[newLength]);
		}

		int position = 0;
		for (int i = 0; i < report.results1.size(); i++) {
			Symbol left = report.results1.get(i);
			Symbol right = report.results2.get(i);
			if ((left != null) && (right != null)) {
				for (int j = 0; j < this._rows.size(); j++) {
					Symbol[] oldRow = _rows.get(j);
					Symbol[] newRow = tmp.get(j);
					newRow[i] = oldRow[position];
				}

				tmp.get(_rows.size())[i] = right;

				position++;
			} else if (right == null) {
				for (int j = 0; j < this._rows.size(); j++) {
					Symbol[] oldRow = _rows.get(j);
					Symbol[] newRow = tmp.get(j);
					newRow[i] = oldRow[position];
				}
				position++;
			} else {
				if (left != null) {
					continue;
				}

				tmp.get(_rows.size())[i] = right;
			}
		}
		_rows = tmp;
	}
	
	@Override
	public void merge(Signature s) {
		if (!(s instanceof CompleteSignature))
			throw new RuntimeException("Can only merge CompleteSignatures with other CompleteSignatures");
		
		CompleteSignature s1 = this;
		CompleteSignature s2 = (CompleteSignature) s;

		Params params = new Params();
		params.setMin(0, 0);
		params.setBonus(1.0D, 1.0D);
		params.setPenalty(-1.0D, -1.0D);
		params.seq1 = s1.signature();
		params.seq2 = s2.signature();

		Report report = SequenceAlignment.align(params);
		List<Symbol[]> table = new ArrayList<Symbol[]>();

		int newLength = report.results1.size();
		for (int i = 0; i < s1._rows.size() + s2._rows.size(); i++) {
			table.add(new Symbol[newLength]);
		}

		int s1pos = 0;
		int s2pos = 0;
		for (int i = 0; i < report.results1.size(); i++) {
			Symbol left = report.results1.get(i);
			Symbol right = report.results2.get(i);
			if ((left != null) && (right != null)) {
				for (int j = 0; j < s1._rows.size(); j++) {
					Symbol[] oldRow = s1._rows.get(j);
					Symbol[] newRow = table.get(j);
					newRow[i] = oldRow[s1pos];
				}

				for (int j = 0; j < s2._rows.size(); j++) {
					Symbol[] oldRow = s2._rows.get(j);
					Symbol[] newRow = table.get(s1._rows.size() + j);
					newRow[i] = oldRow[s2pos];
				}

				s1pos++;
				s2pos++;
			} else if (right == null) {
				for (int j = 0; j < s1._rows.size(); j++) {
					Symbol[] oldRow = s1._rows.get(j);
					Symbol[] newRow = table.get(j);
					newRow[i] = oldRow[s1pos];
				}
				s1pos++;
			} else if (left == null) {
				for (int j = 0; j < s2._rows.size(); j++) {
					Symbol[] oldRow = s2._rows.get(j);
					Symbol[] newRow = table.get(s1._rows.size() + j);
					newRow[i] = oldRow[s2pos];
				}
				s2pos++;
			}
		}

		_rows = table;
		_count = this._rows.size();
		_signature = SequenceAlignment.combineAlignments(report.results1, report.results2);
	}
	
	/**
	 * Return the counts for some things...
	 * @return
	 */
	public Map<Symbol, Integer> getCounts() {
		if (true)
			throw new RuntimeException("Not yet coverted properly for Symbols!");
		Map<Symbol,Integer> map = new TreeMap<Symbol,Integer>();
		Set<Integer> columns = new HashSet<Integer>();
		for (Symbol[] row : _rows) {
			for (int i = 0; i < row.length; i++) {
				if (columns.contains(Integer.valueOf(i))) {
					continue;
				}
				if (row[i] == null) {
					continue;
				}
				columns.add(Integer.valueOf(i));

				Integer count = (Integer) map.get(row[i]);
				if (count == null) {
					count = Integer.valueOf(0);
				}
				count = Integer.valueOf(count.intValue() + 1);
				map.put(row[i], count);
			}
		}

		return map;
	}
	

	@Override
	public void toXML(String file) {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("CompleteSignature ")
				.addAttribute("key", _key)
				.addAttribute("similarity", _params.similarity.toString())
				.addAttribute("count", _count+"");

		for (Symbol obj : _signature) {
			obj.toXML(root);
		}

		Element table = root.addElement("Table")
				.addAttribute("rows", _rows.size()+"")
				.addAttribute("cols", _rows.get(0).length+"");

		for (int i = 0; i < this._rows.size(); i++) {
			Element rowElement = table.addElement("Row").addAttribute("id", i+"");

			Symbol[] row = _rows.get(i);
			for (int j = 0; j < row.length; j++) {
				if (row[j] != null) {
					Element cellElement = rowElement.addElement("Cell")
							.addAttribute("id", j+"");
					row[j].toXML(cellElement);
				}
			}
		}

		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(file), format);
			writer.write(document);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Signature prune(int min) {
		CompleteSignature s = new CompleteSignature(_key, _params.similarity);
		s._count = _count;

		Set<Integer> ignore = new HashSet<Integer>();
		List<Symbol> sequence = new ArrayList<Symbol>();
		for (int i = 0; i < _signature.size(); i++) {
			Symbol obj = _signature.get(i);
			if (obj.weight() > min) {
				Symbol copy = obj.copy();
				copy.prune(min);
				sequence.add(copy);
			} else {
				ignore.add(Integer.valueOf(i));
			}
		}

		s._signature = sequence;
		s._rows = new ArrayList<Symbol[]>();

		int newSize = _rows.get(0).length - ignore.size();
		for (Symbol[] row : _rows) {
			int pos = 0;
			Symbol[] rowCopy = new Symbol[newSize];
			for (int i = 0; i < row.length; i++) {
				if (ignore.contains(Integer.valueOf(i)))
					continue;
				rowCopy[pos] = row[i];
				pos++;
			}
			s._rows.add(rowCopy);
		}
		return s;
	}
	
	/**
	 * Reconstruct a signature from an XML file
	 * @param file
	 * @return
	 */
	public static CompleteSignature fromXML(String file) {
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			document = reader.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Element root = document.getRootElement();

		String key = root.attributeValue("key");
		Similarity similarity = Similarity.valueOf(root.attributeValue("similarity"));
		CompleteSignature s = new CompleteSignature(key, similarity);
		s._count = Integer.parseInt(root.attributeValue("count"));

		List list = root.elements("symbol");
		for (Object o : list) { 
			Element woe = (Element) o;
			s._signature.add(Symbol.fromXML(woe));
		}

		Element table = root.element("Table");
		int nrows = Integer.parseInt(table.attributeValue("rows"));
		int ncols = Integer.parseInt(table.attributeValue("cols"));

		List rowList = table.elements("Row");
		if (rowList.size() != nrows) {
			throw new RuntimeException("Error in the number of rows: "
					+ rowList.size() + " " + nrows);
		}
		for (int i = 0; i < nrows; i++) {
			Element rowElement = (Element) rowList.get(i);
			Symbol[] row = new Symbol[ncols];

			List cellList = rowElement.elements("Cell");
			for (Object o : cellList) { 
				Element cell = (Element) o;
				int id = Integer.parseInt(cell.attributeValue("id"));
				row[id] = Symbol.fromXML(cell.element("symbol"));
			}
			s._rows.add(row);
		}
		return s;
	}
}
