package sync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import ops.OpsManager;
import ops.OpsManagerTest;

import org.junit.BeforeClass;
import org.junit.Test;

public class AvailabilityProfilesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Assert that files are present
		assertNotNull("Test file missing", AvailabilityProfilesTest.class.getResource("/ops/ap1.json"));
	}
	
	@Test
	public void test() throws URISyntaxException, FileNotFoundException {
		//Prepare Resource File
		URL resJsonFile = OpsManagerTest.class.getResource("/ops/ap1.json");
		File jsonFile = new File(resJsonFile.toURI());
		// Instatiate class
		AvailabilityProfiles avp = new AvailabilityProfiles();
		avp.clearProfiles();
		avp.loadProfileJson(jsonFile);
	   
		// Check that only one availability profile was loaded
		assertEquals("Only 1 av profile present",avp.getAvProfiles().size(),1);
		
		ArrayList<String> expApList = new ArrayList<String>();
		expApList.add("ap1");
		
		// Check the profile list is correct
		assertEquals("Profile list check",avp.getAvProfiles(),expApList);
		
		// Check the profile namespace
		assertEquals("Profile namespace",avp.getProfileNamespace("ap1"),"test");
		
		// Check the profile groupType
		assertEquals("Profile group type",avp.getProfileGroupType("ap1"),"sites");
		
		// Set the expected profile groups
		ArrayList<String> expGroups = new ArrayList<String>();
		expGroups.add("information");
		expGroups.add("compute");
		expGroups.add("storage");
		// Check the available group list
		assertEquals("Profile Groups",avp.getProfileGroups("ap1"),expGroups);
		
		// Check compute group service list
		ArrayList<String> expServices = new ArrayList<String>();
		expServices.add("GRAM5");
		expServices.add("QCG.Computing");
		expServices.add("ARC-CE");
		expServices.add("unicore6.TargetSystemFactory");
		expServices.add("CREAM-CE");
        
		assertEquals("compute service list",avp.getProfileGroupServices("ap1", "compute"),expServices);
        
		// Check storage group service list
		expServices = new ArrayList<String>();
		expServices.add("SRM");
		expServices.add("SRMv2");
		assertEquals("storage service list",avp.getProfileGroupServices("ap1", "storage"),expServices);
		
		// Check storage group service list
		expServices = new ArrayList<String>();
		expServices.add("Site-BDII");
		assertEquals("accounting  list",avp.getProfileGroupServices("ap1", "information"),expServices);
		
		// Check Various Service Instances operation
		assertEquals("group compute: CREAM-CE op",avp.getProfileGroupServiceOp("ap1", "compute", "CREAM-CE"),"AND");
		assertEquals("group compute: ARC-CE op",avp.getProfileGroupServiceOp("ap1", "compute", "ARC-CE"),"AND");
		assertEquals("group storage: SRMv2 op",avp.getProfileGroupServiceOp("ap1", "storage", "SRM"),"AND");
		assertEquals("group storage: SRM op",avp.getProfileGroupServiceOp("ap1", "storage", "SRMv2"),"AND");
		assertEquals("group information: Site-BDII op",avp.getProfileGroupServiceOp("ap1", "information", "Site-BDII"),"AND");
		// we check for an unexpected operation
		assertNotEquals("group compute: CREAM-CE op",avp.getProfileGroupServiceOp("ap1", "compute", "CREAM-CE"),"OR");
	
       
	}

}
