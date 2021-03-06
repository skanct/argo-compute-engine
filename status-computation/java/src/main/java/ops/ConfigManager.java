package ops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sync.AvailabilityProfiles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ConfigManager {

	private static final Logger LOG = Logger.getLogger(ConfigManager.class.getName());

	public String tenant;
	public String report;
	public String egroup; // endpoint group
	public String ggroup; // group of groups
	public String agroup; // alternative group
	public String weight; // weight factor type
	public TreeMap<String, String> egroupTags;
	public TreeMap<String, String> ggroupTags;
	public TreeMap<String, String> mdataTags;
	public HashMap<String, HashMap<String, String>> datastore_map;

	public ConfigManager() {
		this.tenant = null;
		this.report = null;
		this.egroup = null;
		this.ggroup = null;
		this.weight = null;
		this.egroupTags = new TreeMap<String, String>();
		this.ggroupTags = new TreeMap<String, String>();
		this.mdataTags = new TreeMap<String, String>();
		this.datastore_map = new HashMap<String, HashMap<String, String>>();
	}

	public void clear() {
		this.tenant = null;
		this.report = null;
		this.egroup = null;
		this.ggroup = null;
		this.weight = null;
		this.egroupTags.clear();
		this.ggroupTags.clear();
		this.mdataTags.clear();
		this.datastore_map.clear();
	}

	public String getMapped(String category, String value) {
		if (this.datastore_map.containsKey(category)) {
			return this.datastore_map.get(category).get(value);
		}

		return null;
	}

	public void loadJson(File jsonFile) throws IOException {
		// Clear data
		this.clear();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(jsonFile));

			JsonParser jsonParser = new JsonParser();
			JsonElement jElement = jsonParser.parse(br);
			JsonObject jObj = jElement.getAsJsonObject();
			// Get the simple fields
			this.tenant = jObj.getAsJsonPrimitive("tenant").getAsString();
			this.report = jObj.getAsJsonPrimitive("job").getAsString();
			this.egroup = jObj.getAsJsonPrimitive("egroup").getAsString();
			this.ggroup = jObj.getAsJsonPrimitive("ggroup").getAsString();
			this.weight = jObj.getAsJsonPrimitive("weight").getAsString();
			this.agroup = jObj.getAsJsonPrimitive("altg").getAsString();
			// Get compound fields
			JsonObject jEgroupTags = jObj.getAsJsonObject("egroup_tags");
			JsonObject jGgroupTags = jObj.getAsJsonObject("ggroup_tags");
			JsonObject jMdataTags = jObj.getAsJsonObject("mdata_tags");
			JsonObject jDataMap = jObj.getAsJsonObject("datastore_maps");
			// Iterate fields
			for (Entry<String, JsonElement> item : jEgroupTags.entrySet()) {

				this.egroupTags.put(item.getKey(), item.getValue().getAsString());
			}
			for (Entry<String, JsonElement> item : jGgroupTags.entrySet()) {

				this.ggroupTags.put(item.getKey(), item.getValue().getAsString());
			}
			for (Entry<String, JsonElement> item : jMdataTags.entrySet()) {

				this.mdataTags.put(item.getKey(), item.getValue().getAsString());
			}

			// Get super compound field
			for (Entry<String, JsonElement> item : jDataMap.entrySet()) {
				String itemKey = item.getKey();
				this.datastore_map.put(itemKey, new HashMap<String, String>());

				JsonObject jSubObj = item.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> subitem : jSubObj.entrySet()) {
					this.datastore_map.get(itemKey).put(subitem.getKey(), subitem.getValue().getAsString());
				}
			}

		} catch (FileNotFoundException ex) {
			LOG.error("Could not open file:" + jsonFile.getName());
			throw ex;

		} catch (JsonParseException ex) {
			LOG.error("File is not valid json:" + jsonFile.getName());
			throw ex;
		} finally {
			// Close quietly without exceptions the buffered reader
			IOUtils.closeQuietly(br);
		}

	}

}
