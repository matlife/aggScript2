package replace.replace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ComponentNode {
	public static String GREEN_STR = "GREEN";
	public static String RED_STR = "RED";
	String name;
	int numErrors;
	int numWarnings;
	int numFatals;
	HashMap logMap;
	String dirinfo;
	boolean isOK;
	ArrayList<LogNode> errors;
	ArrayList<LogNode> warnings;
	ArrayList<LogNode> fatals;
	ArrayList<String> mantises;
	/*
	 **************************************** PUBLIC FIELDS ************************************************************
	 */

	/*
	 **************************************** PRIVATE FIELDS ***********************************************************
	 */

	/** logging */
	private static final Log LOG = LogFactory.getLog(ComponentNode.class);

	/*
	 **************************************** CONSTRUCTORS *************************************************************
	 */

	/**
	 * Creates a new instance of ComponentNode.
	 */
	public ComponentNode() {
	}
	public ComponentNode(boolean isOK) {
		this.isOK = isOK;
	}
	public ComponentNode(String name, String dirinfo){
		this.name = name;
		this.dirinfo = dirinfo;
	}
	/**
	 * Creates a new instance of ComponentNode.
	 * 
	 * Accepts name of the component, number of warnings, errors, and fatals, an array list of the mantis filters assigned before processing,
	 * and a hashmap<name of >
	 */
	public ComponentNode(String name,  int numWarnings, int numErrors, int numFatals, ArrayList<String> mantises, HashMap logHM) {
		this.name = name;
		this.numWarnings = numWarnings;
		this.numErrors = numErrors;
		this.numFatals = numFatals;
		
		this.mantises = (ArrayList<String>) mantises.clone();
		this.isOK = true;
		if ((numWarnings + numErrors + numFatals) > 0){
			// Can replace isOK value with logic here
			this.isOK = false;
		}
		this.logMap = logHM;
	}
	/*
	 **************************************** PUBLIC METHODS ***********************************************************
	 */
	public void addWarning(LogNode e){
		(this.warnings).add(e);
	}
	
	public void addError(LogNode e){
		(this.errors).add(e);
	}
	
	public void addFatal(LogNode e){
		(this.fatals).add(e);
	}
	
	public String toString(){
		String string = "";
		string += String.format("%s\n", this.name);
		string += getSummary();
		return string;
	}
	
	/*
	 * Return a string representing the number of warnings, errors, and fatals. Also prints out mantises.
	 * */
	public String getSummary(){
		String string = "";
		
		if (this.dirinfo != null){
			string += dirinfo;
			return string;
		}
		
		if (this.isOK){
			return "N.P.";
		}
		string += String.format("Warnings: %d\nErrors: %d\nFatals: %d\n", this.numWarnings, this.numErrors, this.numFatals);
		for (String mantis : mantises){
			string += mantis + "\n";
		}
		Collection<LogNode> logs = logMap.values();
		for (LogNode log : logs){
			string += log.toString() + "\n";
		}
		
		return string;
	}

	public String getName(){
		String string = "";
		string += String.format("%s\n", this.name);
		return string;
	}
	public boolean isOK(){
		return this.isOK;
	}
	
	public String getColor(){
		
		if (this.isOK()){
			return GREEN_STR;
		}
		else {
			return RED_STR;
		}
	}
	
	public HashMap getLogMap(){
		return this.logMap;
	}
	/*
	 **************************************** PRIVATE METHODS **********************************************************
	 */
}
