package replace.replace;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LogNode {
	String type;
	ArrayList<String> exampleLogs;
	ArrayList<String> origLogs = new ArrayList();
	String name;
	boolean ok;
	int count;
	/*
	 **************************************** PUBLIC FIELDS ************************************************************
	 */

	/*
	 **************************************** PRIVATE FIELDS ***********************************************************
	 */

	/** logging */
	private static final Log LOG = LogFactory.getLog(LogNode.class);

	/*
	 **************************************** CONSTRUCTORS *************************************************************
	 */

	/**
	 * Creates a new instance of logNode.
	 */
	public LogNode() {
	}
	
	public LogNode(boolean isOk) {
		this.ok= isOk;
	}
	
	public LogNode(String type) {
		this.type = type;
		this.exampleLogs = new ArrayList<String>();
	}
	
	public LogNode(String logName, String type, int num) {
		this.name = logName;
		this.type = type;
		this.count = num;
		this.exampleLogs = new ArrayList<String>();
	}

	/*
	 **************************************** PUBLIC METHODS ***********************************************************
	 */
	public void setCount(int i){
		this.count = i;
	}
	
	public void addExample(String example){
		String [] exArray = example.split("\\] \\[");
		if (!exampleLogs.contains(example)) this.exampleLogs.add(example);
	}
	
	// Checks if the log already exists
	public boolean hasLog(ArrayList<String> logs, String s){
		String [] sArray;
		for (String log : logs){
			sArray = log.split("\\] \\[");
			log = sArray[4]; // Index of specific log description 
			//if (s.equals(log)) return true;
			// Filter station 
			//if (s.contains("Unable to find station")) return true;
		}
		return false;
	}
	/* 
	 * Returns a string with example logs
	 * */
	public String toString(){
		String s = "\n";
		s += "====" + this.name + "==== [" + this.count + "]" + "\n";
		s += StringUtils.repeat("=", s.length() - 4);
		s += "\n";
		for (String example : this.exampleLogs){
			s += ">>   " + example + "\n\n";
		}
		return s;
	}
	
	/*
	 *  Adds a string origLog to the arraylist of original logs stored in the LogNode object.
	 * */
	public void addOriginalLog(String origLog){
		this.origLogs.add(origLog);
	}
	/*
	 **************************************** PRIVATE METHODS **********************************************************
	 */
}
