package replace.replace;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;

import ca.gc.ec.dms.commons.controller.ConfigurationResult;
import ca.gc.ec.dms.commons.controller.DataPayload;
import ca.gc.ec.dms.commons.decoder.DecodeException;
import ca.gc.ec.dms.commons.util.Constants;
import ca.gc.ec.dms.commons.util.XMLPrint;


public class ParserTest {
	Parser p;
	Writer w;
	/*
	 **************************************** PUBLIC FIELDS ************************************************************
	 */

	/*
	 **************************************** PRIVATE FIELDS ***********************************************************
	 */

	/** logging */
	private static final Log LOG = LogFactory.getLog(ParserTest.class);
    /**
     * set the test properties
     */
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("component.configuration", "test.application.properties");
        System.setProperty("log4j.configuration", "test.log4j.properties");
    }
	/*
	 **************************************** CONSTRUCTORS *************************************************************
	 */

	/**
	 * Creates a new instance of ParserTest.
	 */
	public ParserTest() {
	}

	/*
	 **************************************** PUBLIC METHODS ***********************************************************
	 */
    /**
     * Setup for the test.
     * @throws IOException 
     */
    @Before
    public void setUp() throws IOException {
    	ArrayList<String> hostnames = new ArrayList(Arrays.asList("log1", "log2", "log3", "log4", "stab"));
		ArrayList<String> chkdatafiles = new ArrayList(Arrays.asList("host1", "host2", "host3", "host4", "host5"));
    	p = new Parser(hostnames, chkdatafiles);
		w = new Writer(p);

    }
    
	@Test
	public void testAdd() throws Exception{
		w.writeExcel("verification.xls");
		//System.out.println("Writing to mongo:");
		w.writeToMongo();
		System.out.println("Done");
		return;

	}
	/*
	 **************************************** PRIVATE METHODS **********************************************************
	 */
}
