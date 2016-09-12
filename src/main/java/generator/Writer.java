package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.bson.types.ObjectId;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
/*
 * Writer writes parsed data in to an excel file. An instance of Writer will be created for each host. 
 * */
public class Writer {
	Parser parser;
	Workbook wb;
	HashMap rows;
	
	// Names of the files that hold the chk-log stuff
	ArrayList<String> hosts;
	// Names of the files that hold the chk-data stuff
	ArrayList<String> hosts_chkdata;	/*
	 **************************************** PUBLIC FIELDS ************************************************************
	 */
	

	/*
	 **************************************** PRIVATE FIELDS ***********************************************************
	 */

	/** logging */
	private static final Log LOG = LogFactory.getLog(Writer.class);

	/*
	 **************************************** CONSTRUCTORS *************************************************************
	 */

	public Writer(Parser parser) {
		this.parser = parser;
		this.rows = new HashMap();
	}
	/**
	 * Creates a new instance of Writer.
	 */
	public Writer(Parser parser, ArrayList<String> chkloghosts, ArrayList<String> chkdatahosts) {
		this.parser = parser;
		this.rows = new HashMap();
		hosts = chkloghosts;
		hosts_chkdata = chkdatahosts;
	}
	

	/*
	 **************************************** PUBLIC METHODS FOR WRITING TO MONGO ***********************************************************
	 */
	public void writeToMongo(){

		// Since 2.10.0, uses MongoClient
		MongoClient mongo = null;
		try {
			System.out.println("Connecting to host");
			mongo = new MongoClient( "localhost" , 27017 );
			System.out.println("Connected");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to find host");
			e.printStackTrace();
		}
		
		

		DB db = mongo.getDB("my_database");
		System.out.println("Getting collection");
		
		// Populate component names and server names
		// Collection containing the components
		DBCollection componentList_collection = db.getCollection("master_componentList");
		// Collection containing the servers 
		DBCollection serverList_collection = db.getCollection("master_server");
		
		BasicDBObject hostObject = null;
		
		for (String host: hosts){
			hostObject = new BasicDBObject();
			hostObject.put("_id", host);
			hostObject.put("name", host);
			hostObject.put("location", host);
			serverList_collection.save(hostObject);
		}
		
		// Populate database
		BasicDBObject componentDocument = new BasicDBObject();
		DBCollection component_collection = db.getCollection("master_component");
		DBCollection problem_collection = db.getCollection("master_problem");

		ArrayList<String> manifestComponents = null;
		try {
			manifestComponents = getManifestComponents();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}

		BasicDBObject componentDoc = null;
		ObjectId componentID;
		BasicDBObject problemDoc = null;
		ObjectId problemID;
		for (String host: hosts){
			System.out.println(host);
			//iterate over 
			for (String component : manifestComponents){
				
				// Create a new componentDocument for every component
				// each component will hold type, list of problems, 
				componentID = new ObjectId();
				componentDoc = new BasicDBObject();
				componentDoc.put("_id", componentID);
				componentDoc.put("name", component);
				componentDoc.put("location", host);
				
				ComponentNode componentnode =  this.parser.getComponent(component, host);
				if (componentnode != null) {
					if (!componentnode.isOK){
						HashMap hm = componentnode.getLogMap();
						Collection<String> values = (Collection<String>) hm.values();
						Collection<String> keys = (Collection<String>) hm.keySet();
						// Does script grab every problem? Or just one problem for every source
						// Look in to this
						// TAkes everything. Will need to change parser to seperate these things? Or nah
						// Split logs up to individual things?
						// Loop over problems in hashmap of logNodes
						// Create a new document to store problem information
						problemDoc = new BasicDBObject();
						problemID = new ObjectId();
						List<BasicDBObject> problemList = new ArrayList<BasicDBObject>();
						for (String key: keys){
							LogNode problem = (LogNode) hm.get(key);			
							// item for every problem??
							// yes.. I guess so since the number of items will be bound (50)

							
							ArrayList<String> loglines = problem.exampleLogs;

							problemDoc = new BasicDBObject();
							problemID = new ObjectId();
							problemDoc.put("_id", problemID);
							problemDoc.put("name", problem.name);
							problemDoc.put("location", host);
							
							problemDoc.put("full_logs", problem.origLogs);
							problemDoc.put("problem_logs", problem.exampleLogs);
							
							problemList.add(problemDoc);
							
							// When finished we should have an array of problem objects
						    System.out.println(problem);

						}
					    
						// Finally, add object to list of problems to Component
					    componentDoc.put("problems", problemList);
					    
						System.out.println(values);
						component_collection.insert(componentDoc);
					}
					else {
						System.out.println("Component \"" + component + "\" is ok.");
					}
				}
				else{
					System.out.println("Couldn't find \"" + component + "\"");
					}
			}
				
				// for every warning log append warnings
				// log componentDocument = {name, {example}*, {helper logs}}
				// then append the helpers?
				// helper logs = logs in their 'natural habitat', with the 5 surrounding logs? Let's start with this.
		}

			
	}
	
	public ArrayList<String> getManifestComponents() throws FileNotFoundException{ 	
		// Add manifest components to an arraylist
		System.out.println(this.parser.getConfigFile()); 
		Scanner scanner = new Scanner (new File(this.parser.getConfigFile())).useDelimiter("\n");
		ArrayList<String> manifestComponents = new ArrayList<String>();
		while (scanner.hasNext()){
			manifestComponents.add(scanner.next());
		}
		scanner.close();
		return manifestComponents;
	}
	
	public void writeToES() throws FileNotFoundException{
		System.out.println("Writing to ES");
		// Add manifest components to an arraylist
		System.out.println(this.parser.getConfigFile());
		Scanner scanner = new Scanner (new File(this.parser.getConfigFile())).useDelimiter("\n");
		ArrayList<String> manifestComponents = new ArrayList<String>();
		while (scanner.hasNext()){
			manifestComponents.add(scanner.next());
		}
		scanner.close();
		
		// Iterate over manifest components to display host information
		ComponentNode component;
		HashMap components;
		for (String componentName : manifestComponents){
			componentName=componentName.replace("\r", "");
			for (String host : hosts){
				component = this.parser.getComponent(componentName, host);
				if (component != null){
					component.pushLogs();
				}

			}
		}
	}
	
	/*
	 **************************************** PUBLIC METHODS FOR WRITING TO EXCEL ***********************************************************
	 */
	public void writeExcel(String workbook) throws IOException{
	    wb = new HSSFWorkbook();  // or new XSSFWorkbook();1

	    // You can use org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)}
	    // for a safe way to create valid names, this utility replaces invalid characters with a space (' ')
	    
	    //Create name for data sheet
	    String safeName = WorkbookUtil.createSafeSheetName("DMS Verification"); // returns " O'Brien's sales   "
	    Sheet sheet = wb.createSheet(safeName);
	    
	    //Create name for chkdata sheet
	    safeName = WorkbookUtil.createSafeSheetName("DMS Directory"); // returns " O'Brien's sales   "
	    Sheet sheet_chkdata = wb.createSheet(safeName);
	    
	    // Create a row and put some cells in it. Rows are 0 based.
	    Row row;
	    Cell cell;
	    String s = "";
		int index = 0;
		int rowIndex = 0;
		
		// Add manifest components to an arraylist
		System.out.println(this.parser.getConfigFile());
		Scanner scanner = new Scanner (new File(this.parser.getConfigFile())).useDelimiter("\n");
		ArrayList<String> manifestComponents = new ArrayList<String>();
		while (scanner.hasNext()){
			manifestComponents.add(scanner.next());
		}
		scanner.close();

		// Iterate over manifest components to display host information
		ComponentNode component;
		HashMap components;
		int i = 0;
		for (String componentName : manifestComponents){
			for (String host : hosts){
				component = this.parser.getComponent(componentName, host);
				this.createRow(sheet, component, host, this.hosts, componentName.toLowerCase(), i);
			}
			for (String hostname : hosts_chkdata){
				component = this.parser.getChkDataComponent(componentName, hostname);
				this.createRow(sheet_chkdata, component, hostname, this.hosts_chkdata,  componentName.toLowerCase(), i);
			}
			i++;
		}
		
		
	    FileOutputStream fileOut = new FileOutputStream("verification.xls");
	    wb.write(fileOut);
	    fileOut.close();
	}
	

	/* Creates row in sheet, at index roWIndex, with the cell style of cs. Information pulled from the ComponentNode c*/
	public void createRow(Sheet sheet, ComponentNode c, String host, ArrayList<String> filenames, String cname, int rowIndex){
		int index = filenames.indexOf(host);
		Row row = getsetRow(sheet, rowIndex);
		Cell cell;
		
		
		// If the component name column has yet to be filled, fill it before adding host column.
		// First host may not have component name (was not found in its HashMap)
		if (index == 0){
			// Name of component
			cell = row.createCell(0);
			cell.setCellValue(cname);
		}
		
		if (c != null){
			cell = row.createCell(index + 1);
			String summary = c.getSummary();
			int MAX = 36000;
			if (summary.length() >= MAX){
				cell.setCellValue(summary.substring(0,36000));}
				
			else {
				cell.setCellValue(summary);
			}
		}
		// If cell doesn't exist then it wasn't found in the hobbit logs 
		else {
			cell = row.createCell(hosts.indexOf(host) + 1);
			cell.setCellValue("Not found");
		}

		cell.setCellStyle(getCellStyle(c));
	}
	
	public CellStyle getCellStyle (ComponentNode component){
		CellStyle cs = wb.createCellStyle();
		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs.setWrapText(true);
		
		if (component != null) {
			if (component.getColor().equals(component.GREEN_STR)){
				cs.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			}
			else{
				cs.setFillForegroundColor(IndexedColors.ROSE.getIndex());
			}
		}
		else {
			cs.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		}
		return cs;
	}
	
	
	/* return the row at rowIndex, creates one at index if it does not exist */
	public Row getsetRow(Sheet sheet, int rowIndex){
		Row row = sheet.getRow(rowIndex);
		if (row == null){
			row = sheet.createRow(rowIndex);
		}
		return row;
	}



}
