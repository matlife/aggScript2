package replace.replace;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.gc.ec.dms.commons.util.Constants;

import com.google.common.io.Files;


public class Parser {
	
	/*
	 **************************************** PUBLIC FIELDS ************************************************************
	 */

	/*
	 **************************************** PRIVATE FIELDS ***********************************************************
	 */

	private String input;
	/* Maps host name to HashMap of components */
	private HashMap hosts;
	/* Maps component name to componentnode object*/
	private HashMap<String, ComponentNode> components;
	
	private HashMap hosts_chkdata;
	private HashMap components_chkdata;
	private static final String CONFIGNAME = "components";
	private String configfile;
	/** logging */
	private static final Log LOG = LogFactory.getLog(Parser.class);

	/*
	 **************************************** CONSTRUCTORS *************************************************************
	 */

	/**
	 * Creates a new instance of Parser.
	 * @throws IOException 
	 */
	public Parser(ArrayList<String> filenames, ArrayList<String> chkdatafiles) throws IOException {
		this.hosts = new HashMap();
		String[] inputArray;
		String nonPassing;
		String passing;
		String[] passingLogs;
		for (String filename : filenames){
			components = new HashMap();
	        this.input = new Scanner(new File("src/test/resources/"+filename)).useDelimiter("\\Z").next();
	        this.configfile = new Scanner(new File("src/main/java/config/"+CONFIGNAME)).useDelimiter("\\Z").next();
	        this.configfile = this.configfile.toLowerCase();
	        
	        inputArray = this.input.split("~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x~x");;
	        nonPassing = inputArray[0];
	        passing = inputArray[1];
	        
	        //Remove labels, replace spaces
	        passingLogs = passing.replace("Passing Logs:", "").replaceAll("(?m)^\\s+", "").split("\n");
	        this.parse(filename, nonPassing);
	        
	        // Create and add components that are ok to hashmpa
	        for (String passingComponent : passingLogs){
	        	passingComponent = passingComponent.split(" ")[0];
	        	// map component to a logNode designated as 'ok'
	        	components.put(passingComponent, new ComponentNode(true));
	        }
		}
		
		// Parse chk-data files
		this.hosts_chkdata = new HashMap();
		for (String cdf: chkdatafiles){
	        this.input = new Scanner(new File("src/test/resources/"+cdf)).useDelimiter("\\Z").next();
	        this.configfile = new Scanner(new File("src/main/java/config/"+CONFIGNAME)).useDelimiter("\\Z").next();
	        this.configfile = this.configfile.toLowerCase();
	        
	        inputArray = this.input.split("         No old files remaining:");
	        String cdfdata = inputArray[0];
			this.parseData(cdf, cdfdata);
			
			passingLogs = inputArray[1].replaceAll("\\s+", " ").split(" ");
			// Add passing components to hashmap
	        for (String passingComponent : passingLogs){
	        	passingComponent = passingComponent;
	            this.components_chkdata.put(passingComponent, new ComponentNode(true));
	        }
		}


	}
	/*
	 **************************************** PUBLIC METHODS ***********************************************************
	 */
	/* Parses warnLogs. Stores information in ComponentNode objects. 
	 * 
	 * */
	public void parse(String filename, String warnLogs){
		this.input = warnLogs;
        String[] blocks = this.input.split("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+");
        int passingIndex = -1;
        
        if (passingIndex == -1){
        	passingIndex = blocks.length;
        }
       // System.out.println((blocks.get(passingIndex)).equals("Passing Logs:\r"));
        String header = blocks[0];
        String [] nonPassing = new String[blocks.length - 1];
        System.arraycopy(blocks, 1, nonPassing, 0, blocks.length - 1);
        
        /*Will store the name of the component
        String componentName;
        /* Will store the information found between the component name and details
         e.g.
            WARNINGS: X
            ERRORS: Y
            FATALS: Z */
        String componentHeader;
        /* Stores name of the component. Also used as a key for components HashMap*/
        String componentName;
        /* Stores the details of the warnings/errors/fatals*/
        String details;
        /* Stores the log examples*/
        String logs;
        /* Stores the array representation of the block.*/
        String[] blockArray, lineArray;
        
        /* Stores mantis strings */
        ArrayList<String> mantis;
        /* Temporarily stores log objects in an ArrayList labeled as their respective types */
        ArrayList<LogNode> warnings, errors, fatals;
        /* Stores info logs before problme*/
        ArrayList<String> origmsg;
        
    	ComponentNode node = new ComponentNode();
    	/* Object containing log information*/
    	LogNode log;
    	/* Hashmap of component name : logNode objects. logNodes store the name of the source of problematic log and examples */
    	HashMap logHM;
    	/* Name of component that the log is attributed to */
    	String logName;
    	/* Type of error log denotes (ERROR/FATAL/WARNING)*/
    	String logType;
  
        int numWarnings = 0, numErrors= 0, numFatals = 0;
        
        for (String block : nonPassing){
        	
        	// New map for every component
        	logHM = new HashMap();
        	
        	block = block.trim().replaceAll(" +", " ");
        	block = block.replaceAll(" +", " ");
        	
        	blockArray = block.split("\n");
        	/*System.out.println(lineArray[0]);
        	System.out.println(lineArray[1]);   
        	 */
        	componentName = blockArray[0].split(" ")[0];
        	
        	// Do not parse if the component is not in the manifest 
        	if (!this.configfile.contains(componentName)){
        		continue;
        	}
            mantis = new ArrayList<String>();
            warnings = new ArrayList<LogNode>();
            errors = new ArrayList<LogNode>();
            fatals = new ArrayList<LogNode>();
            origmsg = new ArrayList<String>();
            
        	for (String line:blockArray){
        		
        		line = line.trim();
        		lineArray = line.split(" ");
        		
        		if (line.startsWith("WARNINGS: ")){
        			numWarnings = Integer.parseInt(removeNewBreaks(lineArray[1]));
        		}
        		else if (line.startsWith("ERRORS: ")){     			
        			numErrors = Integer.parseInt(removeNewBreaks(lineArray[1]));
                	
        		}
        		else if (line.startsWith("FATALS: ")){  			
        			numFatals = Integer.parseInt(removeNewBreaks(lineArray[1]));
        		}
        		else if (line.startsWith("Mantis")){
        			mantis.add(line);
        		}
        		
        		// Create a LogNode for source of errors 
        		else if (line.matches("[a-zA-Z\\:0-9]+[a-zA-Z\\:0-9\\-\\_]+ - (ERROR|FATAL|WARN): +\\d*")){
        			logName = lineArray[0];
        			logType = lineArray[2].replace(":", "");
        			
    				log = new LogNode(logName, logType, Integer.parseInt(lineArray[3]));
    				// Remember name of source, map to its object
    				logHM.put(logName, log);
    				//System.out.println("added: " + line);
        		}
        		// Capture Log examples
        		else if (line.matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\d{3}\\] \\[(ERROR|FATAL|WARN) *\\] \\[[\\w-:\\\\. ]+\\] .*")){
        			int logNameIndex = 3;
        			int logTypeIndex = 1;
        			
        			lineArray = line.split("\\] \\[");
        			logName = lineArray[logNameIndex].replace("[", "").replace("]", "");
        			logType = lineArray[logTypeIndex].replace(" ", "");
        			
        			if (logName.contains("PID:")){
            			logName = lineArray[logNameIndex].replace("[", "").replace("]", "");
        			}
        			
    				log = (LogNode) logHM.get(logName);
    				log.addExample(line);
        		}
        		
        		if (line.matches("\\[.*\\]")){
        			origmsg.add(line);
        		}
        	}
        	/* Add values to new component object */
    		node = new ComponentNode(componentName, numWarnings, numErrors, numFatals, mantis, logHM);
    		numErrors = numWarnings = numFatals = 0;

        	/*Add component to HashMap*/
        	components.put(componentName, node);
        	mantis.clear();
        }
      //  System.out.println(nonPassing);
        hosts.put(filename, components);
	}
	
	/*
	 * Parses chk-data logs.
	 * */
	public void parseData(String host, String chkdatalog){
		String[] dataLogArray = chkdatalog.split("Number of files in");
		dataLogArray = Arrays.copyOfRange(dataLogArray, 1, dataLogArray.length - 1);
		String compname;
		String info;
		String [] blockarray;
		ComponentNode cn;
		components_chkdata = new HashMap();
		for (String block : dataLogArray){
			blockarray = block.split(":", 2);
			compname = blockarray[0].replaceAll(" ", "");
			info = blockarray[1];
			cn = new ComponentNode(compname, info);
			components_chkdata.put(compname, cn);
		}
		hosts_chkdata.put(host, components_chkdata);
	}
	
	public ComponentNode getComponent(String name, String host){
		name = removeNewBreaks(name).toLowerCase();
		HashMap<String, ComponentNode> components = (HashMap<String, ComponentNode>) this.hosts.get(host);
		ComponentNode c = (ComponentNode) components.get(name);
		return c;
	}
	
	public ComponentNode getChkDataComponent(String componentname, String hostname){
		componentname = removeNewBreaks(componentname).toLowerCase();
		HashMap<String, ComponentNode> host = (HashMap<String, ComponentNode>) this.hosts_chkdata.get(hostname);
		ComponentNode c = (ComponentNode) host.get(componentname);
		return c;
	}
	
	public String getConfigFile(){
		return "src/main/java/config/"+CONFIGNAME;
	}
	

	/*
	 **************************************** PRIVATE METHODS **********************************************************
	 */
	private boolean indented(String s){
		if( s.startsWith("   ") ){
			return true;
		}
		return false;
	}
	

	private String removeNewBreaks(String s){
		return s.replaceAll("(\\r|\\n|\\r\\n|\r)+", "");
	}
	
}
