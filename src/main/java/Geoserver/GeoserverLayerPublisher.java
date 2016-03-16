package Geoserver;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType.Attribute;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
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
		fte.setName(layerName);

		System.out.println("INFORMATION: Setting Bounds on GeoServer for "
				+ fte.getName() + ": " + epsg + " with " + minX + ", " + minY
				+ ", " + maxX + ", " + maxY);

		fte.setSRS(epsg);
		fte.setNativeBoundingBox(minX, minY, maxX, maxY, epsg);
		fte.setTitle(geomType);

		System.out
				.println("INFORMATION: Creating GeoServer layer " + layerName);

		boolean published = this.publisher.publishDBLayer("Postgis",
				"Kartenapp", fte, gsl);

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

	public Map<String, String> getAttributes(String layer) {
		RESTLayer gsLayer = reader.getLayer(
				"Postgis", layer);
		RESTFeatureType fType = reader.getFeatureType(gsLayer);
		Iterator<Attribute> attrIter = fType.attributesIterator();
		Map<String, String> attributes = new HashMap<String, String>();
		while (attrIter.hasNext()) {
			Attribute attr = attrIter.next();
			attributes.put(attr.getName(), attr.getBinding());
		}
		return attributes;
	}

	/**
	 * Setzt den Stil für den angelegten Layer
	 *
	 * @param sldStyle
	 *            zu setzeneder Stil SLD Datei als String
	 * @param styleType
	 *            Name des Stils
	 */
	public void createStyle(String styleType, String tableName, String symbol,
			String styleAttr, List<Object> styleValues) {

		String styleName = tableName + "-" + styleAttr;
		System.out.println("Setting GeoServer style on " + styleType
				+ " with Attribute " + styleAttr);
		String sld;
		if (styleValues.isEmpty()) {
			sld = GeoserverStyleSet.doSimpleSLD(styleName, styleType,
					"Attribute Values not readable", symbol, 6, 1);
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
			System.out.println("Style already exists: " + published);
		}
		setStyle(styleName, tableName);
	}

	public void setStyle(String styleName, String tableName) {
		this.gsl.setDefaultStyle("Postgis:" + styleName);
		boolean configured = this.publisher.configureLayer("Postgis",
				tableName, this.gsl);
		System.out.println("Layer Configured: " + configured);
	}

	public boolean existsStyle(String styleName) {
		return reader.existsStyle("Postgis", styleName);
	}

	//TODO: Geometrie richtig übergeben!!
	public String getGeom(String layerName) {
		String sld = reader.getSLD("Postgis", reader.getLayer("Postgis", layerName).getDefaultStyle());
		if (sld.contains("PointSymbolizer")){
			return "Point";
		}
		else if (sld.contains("LineSymbolizer")){
			return "Line";
		}
		else {
			return "Polygon";
		}
	}
	public String getActiveAttr(String layerName){
		String style = reader.getLayer("Postgis", layerName).getDefaultStyle();
		String attr = style.replace(layerName+"-", "");
		return attr;
	}

}
