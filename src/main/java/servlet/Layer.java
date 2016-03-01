package servlet;

import it.geosolutions.geoserver.rest.decoder.RESTFeatureType.Attribute;

import java.util.ArrayList;
import java.util.List;

public class Layer {

	private final String name;
	private final List<Attribute> features;
	private boolean active = true;
	private int opacity = 30;
	private String geometry;

	

	public Layer(String name, List<Attribute> fType,String geomType) {
		this.name = name;
		this.features = fType;
		this.geometry = geomType;
	}
	public String getGeometry() {
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	public int getOpacity() {
		return opacity;
	}
	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}
	public String getName() {
		return name;
	}
	public List<String> getFeatures(){
		List<String> attr = new ArrayList<String>();
		
		for (Attribute attribute:features){
			String name = attribute.getName();
			attr.add(name);
		}
		return attr;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
