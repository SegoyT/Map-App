package servlet;

import it.geosolutions.geoserver.rest.decoder.RESTFeatureType.Attribute;

import java.util.Arrays;
import java.util.List;

public class Layer {

	private final String name;
	private final List<Attribute> features;
	private boolean active = true;

	public Layer(String name, List<Attribute> fType) {
		this.name = name;
		this.features = fType;
	}

	


	public String getName() {
		return name;
	}

	public List<Attribute> getFeatures(){
		return features;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
