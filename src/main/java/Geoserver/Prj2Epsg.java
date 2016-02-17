package Geoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Prj2Epsg {

	public static String getEpsg(String prj) {

		try {
			prj = URLEncoder.encode(prj, "UTF-8");
			URL url = new URL("http://prj2epsg.org/search.json?terms=" + prj);
			System.out.println("Prj2epsg API Request: "+ url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder s = new StringBuilder();
			int i;
			while ((i = reader.read()) != -1){
				s.append((char) i);
			}
//			TODO: JSON richtig auslesen!!!
			System.out.println("Prj2epsg API Response: " + s.toString());
			JSONObject json = (JSONObject) JSONValue.parse(s.toString());
			JSONArray jsonar = (JSONArray) json.get("codes");
			System.out.println(jsonar.toString().replace("[","").replace("]", ""));
			JSONObject json2 = (JSONObject) JSONValue.parse(jsonar.toJSONString().replace("]","").replace("[", ""));
		
			return json2.get("code").toString();
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return "failure";
		}
	}
}
