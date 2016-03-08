package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

import Geoserver.Prj2Epsg;

public class GeoJSONImporter implements Importer {

	private File uploadFile;
	private String tableName;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection;
	private String epsg;
	private String geomType;
	private List<Entry<String, String>> shapeAttrs;

	@Override
	public void setFile(File file, String name) {
		this.uploadFile = file;
		this.tableName = name;

	}

	@Override
	public void handleFile() {
		try {
			if (uploadFile == null) {
				throw new IllegalArgumentException(
						"No File: Please provide a File");
			}

			dbCollection = readFile(tableName, uploadFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection() {
		return this.dbCollection;
	}

	/*
	 * Alte methode versuch wie shapefile zu importieren. könnte teilweise noch
	 * hilfreich sein <- war doch richtig..
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> readFile(
			String tableName, File file) throws IOException {

		final InputStream inputstream = new FileInputStream(file);
		GeometryJSON gjson = new GeometryJSON();
		FeatureJSON fjson = new FeatureJSON(gjson);
		
//		System.out.println(fjson.readCRS(inputstream));
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> jfc = fjson
				.readFeatureCollection(inputstream);

		final SimpleFeatureType jsonSchema = jfc.getSchema();
		final List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();

		final SimpleFeatureTypeBuilder dbSftBuilder = new SimpleFeatureTypeBuilder();
		dbSftBuilder.setName(new NameImpl(tableName));
		final List<AttributeDescriptor> ads = jsonSchema
				.getAttributeDescriptors();
		for (final AttributeDescriptor ad : ads) {

			final String n = ad.getName().toString();
			// final String typ = ad.getType().getBinding().getSimpleName();
			// final Entry<String, String> attr = new SimpleEntry<String,
			// String>(n, typ);

			final Name name = new NameImpl(n.toUpperCase());
			final AttributeDescriptor t = new AttributeDescriptorImpl(
					ad.getType(), name, ad.getMinOccurs(), ad.getMaxOccurs(),
					ad.isNillable(), ad.getDefaultValue());
			dbSftBuilder.add(t);
		}
		// TODO: EPSG, wenn nicht automatisch erstellt wird übergeben lassen.
		String prj = "";
		this.epsg = prj;

		this.geomType = jsonSchema.getGeometryDescriptor().getType()
				.getBinding().getName();

		final SimpleFeatureType dbSchema = dbSftBuilder.buildFeatureType();
		System.out.println(dbSchema.getName());

		try (FeatureIterator<SimpleFeature> shpFeatures = jfc.features()) {
			while (shpFeatures.hasNext()) {
				final SimpleFeature sf = shpFeatures.next();
				final SimpleFeatureBuilder dbSfBuilder = new SimpleFeatureBuilder(
						dbSchema);
				for (final AttributeDescriptor ad : jsonSchema
						.getAttributeDescriptors()) {
					final String attr = ad.getLocalName();
					final String name = attr.toUpperCase();
					final Object obj = sf.getAttribute(attr);
					dbSfBuilder.set(name, obj);
				}
				final SimpleFeature of = dbSfBuilder.buildFeature(null);
				featureList.add(of);
			}
		}
		final FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection = new ListFeatureCollection(
				dbSchema, featureList);
		return dbCollection;
	}

	@Override
	public String getEpsg() {
		return epsg;
	}

	@Override
	public String getGeomType() {
		return geomType;
	}

}
