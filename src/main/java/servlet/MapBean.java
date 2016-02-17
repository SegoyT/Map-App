package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.Part;

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

	public MapBean() {
		System.out.println("INFORMATION: Server started!");
		publisher = new GeoserverLayerPublisher();
		try {
			for (String layer : publisher.getLayers()) {
				layers.add(new Layer(layer, 0.0, 0.0, 0.0, 0.0));
			}
		} catch (MalformedURLException e) {
			System.out.println("ERROR: Inkorrekte Geoserver URL!");
			e.printStackTrace();
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
		}
		else {
			dialogMessage = "File could not be Uploaded, File already exists!";
		}
	}

	public void publish(String epsg) {

		publisher = new GeoserverLayerPublisher();
		if (publisher.createLayer(dbm.getTableName(), "EPSG:" + epsg,
				dbm.getminX(), dbm.getminY(), dbm.getmaxX(), dbm.getmaxY(),
				dbm.getgeomType())) {
			layers.add(new Layer(dbm.getTableName(), dbm.getminX(), dbm
					.getminY(), dbm.getmaxX(), dbm.getmaxY()));

			dialogMessage = "Layer published!";
		} else {
			try {
				dbm.deleteTable(dbm.getTableName());
				dialogMessage = "Layer could not be created";
			} catch (IOException | SQLException e1) {
				System.err.println("ERROR: failed to delete Table");
				e1.printStackTrace();
				dialogMessage = "Layer could not be created";
			}

		}

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