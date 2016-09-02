package replace.replace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	
    	// Store output from chk-log and chk-data as strings
    	
    	Parser p;
    	Writer w;
    	
    	ArrayList<String> hostnames = new ArrayList<String>(
    			
    			Arrays.asList(
    			"dms-dev-host1.chk-log.log", "dms-dev-host2.chk-log.log", 
    			"dms-dev-host3.chk-log.log","dms-dev-host4.chk-log.log",
    			"dw-dev1-host1.chk-log.log","dw-dev1-host2.chk-log.log"
    					));
		ArrayList<String> chkdatafiles = new ArrayList<String>
				(Arrays.asList(
    			"dms-dev-host1.chk-data.log", "dms-dev-host2.chk-data.log", 
    			"dms-dev-host3.chk-data.log", "dms-dev-host4.chk-data.log",
    			"dw-dev1-host1.chk-data.log","dw-dev1-host2.chk-data.log"
						));
    			

		
		
    	p = new Parser(hostnames, chkdatafiles);
		w = new Writer(p, hostnames, chkdatafiles);

		//w.writeToES();
		w.writeExcel("verification.xls");

		System.out.println("Done");
		return;

    }
}
