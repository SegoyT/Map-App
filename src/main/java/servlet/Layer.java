package servlet;


import java.util.Map;
import java.util.Set;

public class Layer {

	private final String name;
	private final Map<String,String> attributes;
	private boolean active = true;
	private int opacity = 30;
	private String geometry;

	

	public Layer(String name, Map<String, String> attrs,String geomType) {
		this.name = name;
		this.attributes = attrs;
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
	public Map<String, String> getAttributes(){
		return attributes;
	}
	public Set<String> getAttrKeys(){
		return attributes.keySet();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
