package replace.replace;

import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import ca.gc.ec.dms.commons.util.DateUtil;


public class LogNode {
	String type;
	ArrayList<String> exampleLogs;
	ArrayList<String> origLogs;
	String name;
	ArrayList<String> context = new ArrayList<String>();
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
	
	public void addContext(String c){
		this.context.add(c);
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
	public String stripBrackets(String s){
		return s.replace("]", "").replace("[", "");
	}

	/**
	 *   {
"datetime" time>,
"log_level" level>,
"component_name" name>,
"logging class" class>,
"log_msg" log_msg>}
	 * */
	public JSONObject BuildJsonFromLog(String log){
		String [] logArray = log.split("\\] \\[");
		
		for (int i = 0; i < logArray.length; i++){
			logArray[i] = stripBrackets(logArray[i]);
		}
		long datetime_long = 0;
		String datetime = logArray[0];
		try {
			 datetime_long = DateUtils.parseDate(datetime, new String[]{"yyyy-MM-dd HH:mm:ss SSS"}).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		String log_level = logArray[1];
		// Have to remove  thread from component name
		String component_name = logArray[2].split(":")[0]; 
		String logging_class = logArray[3];
		String log_msg = "";
		for (int i = 4; i < logArray.length; i++){
			log_msg = log_msg + logArray[i]; 
		}
		
		if (log_msg.length() > 500){
			log_msg = log_msg.substring(0, 500);
			log_msg +=  "...";
		}
		
		JSONObject json = new JSONObject()
		.put("datetime", datetime_long)
		.put("log_level", log_level)
		.put("component_name", component_name)
		.put("logging_class", logging_class)
		.put("log_msg", log_msg);
		
		
		return json;
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
	
	public ArrayList<JSONObject> getJSONlogs(){
		ArrayList<JSONObject>jsonobjects = new ArrayList();
		for (String example : this.exampleLogs){
			JSONObject json = BuildJsonFromLog(example);
			System.out.println(json);
			jsonobjects.add(json);
		}
		for (String cntxt : this.context){
			JSONObject json = BuildJsonFromLog(cntxt);
			System.out.println(json);
			jsonobjects.add(json);
		}
		return jsonobjects;
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
