package servlet;

import java.util.Arrays;
import java.util.List;

public class Layer {

	private final String name;
	private final List<Double> bounds;
	private boolean active = true;

	public Layer(String name, double minX, double minY, double maxX, double maxY) {
		this.name = name;
		this.bounds = Arrays.asList(minX, minY, maxX, maxY);
	}

	


	public String getName() {
		return name;
	}

	public List<Double> getbounds() {
		return bounds;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
