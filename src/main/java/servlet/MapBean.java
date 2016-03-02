package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import Database.DatabaseManager;
import Geoserver.GeoserverLayerPublisher;

@ManagedBean(name = "MapBean", eager = true)
@SessionScoped
public class MapBean {

	// private UploadedFile file;

	private List<Layer> layers = new ArrayList<Layer>();

	private static final long serialVersionUID = 1L;

	private DatabaseManager dbm = new DatabaseManager();
	private GeoserverLayerPublisher publisher;
	private String dialogMessage;
	private String epsg;
	private String styleAttr;

	public String getStyleAttr() {
		return styleAttr;
	}

	public void setStyleAttr(String styleAttr) {
		this.styleAttr = styleAttr;
	}

	public MapBean() {
		System.out.println("INFORMATION: Server started!");
		publisher = new GeoserverLayerPublisher();
		for (String layer : publisher.getLayers()) {
			System.out.println(layer);
			layers.add(new Layer(layer, publisher.getAttributes(layer),
					publisher.getGeom(layer)));
			System.out.println(publisher.getGeom(layer));
		}
		System.out.println("INFORMATION: Existing Layers added");
	}

	public void upload(FileUploadEvent event) {
		dialogMessage = "Something went wrong!";
		UploadedFile file = event.getFile(); // Retrieves <input
		String fileName = file.getFileName();
		try (InputStream fileContent = file.getInputstream()) {

			// parsing the File Content to the Database Manager
			dbm.addFile(fileContent, fileName);

		} catch (IOException e) {
			e.printStackTrace();
		}
		setEpsg(dbm.getEpsg());

		if (dbm.isUploaded()) {
			if (dbm.getEpsg().equals("")) {
				dialogMessage = "No EPSG was found, Epsg: ";
			} else {
				dialogMessage = "Epsg was found: ";
			}
		} else {
			dialogMessage = "Error, File already exists in database!";
			setEpsg("");
		}
	}

	/*
  * 
  * 
  * 
  */
	public void publish(String epsg, String symbol) {

		System.out.println("Symbol: " + symbol);
		if (epsg.length() > 3) {
			publisher = new GeoserverLayerPublisher();
			if (publisher.createLayer(dbm.getTableName(), "EPSG:" + epsg,
					dbm.getminX(), dbm.getminY(), dbm.getmaxX(), dbm.getmaxY(),
					dbm.getgeomType())) {
				setStyle(symbol, dbm.getTableName(), dbm.getgeomType());
				layers.add(new Layer(dbm.getTableName(), publisher
						.getAttributes(dbm.getTableName()), dbm.getgeomType()));
				dialogMessage = "Layer published!";
			} else {
				try {
					if (!getLayerNames().contains(dbm.getTableName())) {
						dbm.deleteTable(dbm.getTableName());
					}
				} catch (IOException | SQLException e1) {
					System.err.println("ERROR: failed to delete Table");
					e1.printStackTrace();
					dialogMessage = "Layer could not be created";
				}
			}
		} else {
			dialogMessage = "Could not publish Layer!";
		}

	}

	private void setStyle(String symbol, String name, String geom) {
		List<Object> styleValues;
		
		try {
			styleValues = dbm.getAttrValues(styleAttr, name);
		} catch (IOException | SQLException e) {
			System.err
					.println("ERROR: Something wrong with Attribute, will continue with basic Style!");
			styleValues = new ArrayList<>();
			e.printStackTrace();
		}
		publisher.createStyle(geom, name, symbol, styleAttr, styleValues);
	}

	public void changeStyle(String layer) {
		publisher = new GeoserverLayerPublisher();
		String styleName = layer+"-"+styleAttr;
		if (publisher.existsStyle(styleName)){
			publisher.setStyle(styleName, layer);
		}
		else{
		String geom="";
		for (Layer l : layers) {
			if (l.getName().equals(layer)) {
				geom = l.getGeometry();
				dbm.setAttributes(l.getAttributes());
				setStyle("circle", layer, geom);
				break;
			}
		}}
	}

	public Set<String> getAttributes() {
		return dbm.getAttributes().keySet();
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public List<String> getLayerNames() {

		List<String> listStr = new ArrayList<>();
		for (Layer layer : layers) {
			listStr.add(layer.getName());
		}
		return listStr;
	}

	public String getDialogMessage() {
		return dialogMessage;
	}

	public String getEpsg() {
		return epsg;
	}

	public void setEpsg(String epsg) {
		this.epsg = epsg;
	}
}