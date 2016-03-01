package Geoserver;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType.Attribute;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

public class GeoserverLayerPublisher {

	private final String restUrl = "http://localhost:8082/geoserver";
	private final String restUser = "admin";
	private final String restPw = "geoserver";
	private final GSFeatureTypeEncoder fte;
	private GeoServerRESTReader reader;

	private final GSLayerEncoder gsl;

	private final GeoServerRESTPublisher publisher;

	/**
	 * Creates a new instance of Geoserverlayerpublisher.
	 */
	public GeoserverLayerPublisher() {

		this.publisher = new GeoServerRESTPublisher(restUrl, restUser, restPw);
		this.fte = new GSFeatureTypeEncoder();
		this.gsl = new GSLayerEncoder();
		try {
			this.reader = new GeoServerRESTReader(restUrl, restUser, restPw);
		} catch (MalformedURLException e) {
			System.err
					.println("ERROR: Could not initialize GeoserverRESTReader!");
			e.printStackTrace();
		}
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

		System.out
				.println("INFORMATION: Creating GeoServer layer " + layerName);

		boolean published = this.publisher.publishDBLayer("Postgis",
				"Kartenapp", this.fte, this.gsl);

		// setStyle(geomType, layerName, "circle", "101010", "999999");

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
	 *            zu löschender Layer
	 */
	public void deleteLayer(final String layer) {
		System.out.println("INFORMATION: Removing GeoServer layer " + layer);
		boolean deleted = this.publisher.removeLayer("Postgis", layer);
		if (deleted) {
			System.out.println("INFORMATION: Layer " + layer + " deleted");
		} else if (!deleted) {
			System.out.println("ERROR: Deleting " + layer + " failed");
		}
	}

	public List<String> getLayers() {
		List<String> layerList = new ArrayList<String>();

		RESTLayerList layers = reader.getLayers();
		for (NameLinkElem layer : layers) {
			layerList.add(layer.getName());
		}
		return layerList;
	}

	public List<Attribute> getAttributes(String layer) {
		RESTFeatureType fType = reader.getFeatureType(reader.getLayer(
				"Postgis", layer));
		Iterator<Attribute> attrIter = fType.attributesIterator();
		List<Attribute> attributes = new ArrayList<Attribute>();
		while (attrIter.hasNext()) {
			attributes.add(attrIter.next());
		}
		return attributes;
	}

	/**
	 * Setzt den Stil für den angelegten Layer
	 *
	 * @param sldStyle
	 *            zu setzeneder Stil SLD Datei als String
	 * @param styleType
	 *            Name des Layers/Stils
	 */
	public void setStyle(String styleType, String styleName, String symbol,
			String styleAttr, List<Object> styleValues) {
		System.out.println("Setting GeoServer style on " + styleType
				+ " with Attribute " + styleAttr);
		String sld;
		if (styleValues.isEmpty()) {
			sld = GeoserverStyleSet.doSimpleSLD(styleName, styleType,
					"example", symbol, 6, 1);
		} else {
			sld = GeoserverStyleSet.doComplexSLD(styleName, styleType, symbol,
					styleAttr, styleValues);
		}
		boolean published = this.publisher.publishStyleInWorkspace("Postgis",
				sld, styleName);
		System.out.println("Layer published: " + published + "\n" + sld);
		if (published == false) {
			this.publisher.removeStyleInWorkspace("Postgis", styleName);
			published = this.publisher.publishStyleInWorkspace("Postgis", sld,
					styleName);
			System.out.println("Layer published: " + published);
		}
		this.gsl.setDefaultStyle("Postgis:" + styleName);
		boolean configured = this.publisher.configureLayer("Postgis",
				styleName, this.gsl);
		System.out.println("Layer Configured: " + configured);
	}

	public String getGeom(String layerName) {
		String style = reader.getSLD("Postgis", layerName);
		if (style.contains("PointSymbolizer")) {
			return "Point";
		} else if (style.contains("LineSymbolizer")) {
			return "Line";
		} else if (style.contains("PolygonSymbolizer")) {
			return "Polygon";
		}
		return "";
	}
}
