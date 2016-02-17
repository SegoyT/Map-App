package Geoserver;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

public class GeoserverLayerPublisher {

	private final String restUrl = "http://localhost:8082/geoserver";
	private final String restUser = "admin";
	private final String restPw = "geoserver";
	private final GSFeatureTypeEncoder fte;

	private final GSLayerEncoder gsl;

	private final GeoServerRESTPublisher publisher;

	/**
	 * Creates a new instance of Geoserverlayerpublisher.
	 */
	public GeoserverLayerPublisher() {

		this.publisher = new GeoServerRESTPublisher(restUrl, restUser, restPw);
		this.fte = new GSFeatureTypeEncoder();
		this.gsl = new GSLayerEncoder();
	}

	/**
	 * Publish layer in GS
	 *
	 * @param layerName
	 *            Name des layers
	 * @param epsg
	 *            Epsg setzen
	 * @param minX
	 *            min X Wert
	 * @param minY
	 *            min Y Wert
	 * @param maxX
	 *            max X Wert
	 * @param maxY
	 *            max Y Wert
	 */
	public boolean createLayer(final String layerName, final String epsg,
			final double minX, final double minY, final double maxX,
			final double maxY, final String geomType) {
		this.fte.setName(layerName);

		System.out.println("INFORMATION: Setting Bounds on GeoServer for "
				+ this.fte.getName() + ": " + epsg + " with " + minX + ", "
				+ minY + ", " + maxX + ", " + maxY);

		this.fte.setSRS(epsg);
		this.fte.setNativeBoundingBox(minX, minY, maxX, maxY, epsg);

		System.out.println("INFORMATION: Creating GeoServer layer " + layerName);

		boolean published = this.publisher.publishDBLayer("Postgis",
				"Kartenapp", this.fte, this.gsl);

		setStyle(geomType, layerName);

		if (published) {
			System.out.println("INFORMATION: Layer created!");
			return published;
		} else {
			System.out.println("ERROR: Creating Layer failed!");
			return published;
		}
	}

	/**
	 * Löscht einen Layer aus dem Arbeitsbereich "Postgis"
	 *
	 * @param layer
	 *            zu löschendes Layer
	 */
	public void deleteLayer(final String layer) {
		System.out.println("INFORMATION: Removing GeoServer layer " + layer);
		boolean deleted = this.publisher.removeLayer("Postgis", layer);
		if (deleted){
			System.out.println("INFORMATION: Layer "+layer+" deleted");
		}
		else if (!deleted){
			System.out.println("ERROR: Deleting "+ layer+ " failed");
		}
	}

	public List<String> getLayers() throws MalformedURLException {
		List<String> layerList = new ArrayList<String>();
		GeoServerRESTReader reader = new GeoServerRESTReader(restUrl, restUser,
				restPw);
		RESTLayerList layers = reader.getLayers();
		for (NameLinkElem layer : layers) {
			layerList.add(layer.getName());
		}
		return layerList;
	}

	/**
	 * Setzt den Stil für den angelegten Layer
	 *
	 * @param sldStyle
	 *            zu setzeneder Stil SLD Datei als String
	 * @param styleType
	 *            Name des Layers/Stils
	 */
	public void setStyle(String styleType, String styleName) {
		System.out.println("Setting GeoServer style on " + styleType);
		this.gsl.setEnabled(true);
		this.gsl.setQueryable(true);
		String sld = GeoserverStyleSet.doSimpleSLD(styleName, styleType,
				"example", "circle", 6, 1, "101010", "202020");

		boolean published = this.publisher.publishStyleInWorkspace("Postgis",
				sld, styleName);
		if (published == false) {
			this.publisher.removeStyleInWorkspace("Postgis", styleName);
			published = this.publisher.publishStyleInWorkspace("Postgis", sld,
					styleName);
		}
		boolean hope = this.publisher.reload();
		this.gsl.setDefaultStyle("Postgis:" + styleName);
		boolean configured = this.publisher.configureLayer("Postgis",
				styleName, this.gsl);
	}
}
