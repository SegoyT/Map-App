package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.collection.ListFeatureCollection;
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

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;

import Geoserver.Prj2Epsg;

public class GeoJSONImporter implements Importer {

	private File uploadFile;
	private String tableName;
	private Collection<FeatureCollection<SimpleFeatureType, SimpleFeature>> dbCollection = new HashSet<FeatureCollection<SimpleFeatureType, SimpleFeature>>();
	private String epsg;
	private String geomType;
	private List<Entry<String, String>> shapeAttrs;

	@Override
	public void setFile(File file, String name) {
		this.uploadFile = file;
		this.tableName = name;

	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see Database.Importer#handleFile()
	 */
	public void handleFile() {
		try {
			if (uploadFile == null) {
				throw new IllegalArgumentException(
						"No File: Please provide a File");
			}
			readFile(tableName, uploadFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see Database.Importer#getCollection()
	 */
	public Collection<FeatureCollection<SimpleFeatureType, SimpleFeature>> getCollection() {
		return this.dbCollection;
	}

	/*
	 * Alte methode versuch wie shapefile zu importieren. könnte teilweise noch
	 * hilfreich sein <- war doch richtig..
	 */
	public void readFile(String tableName, File file) throws IOException {

		final InputStream inputstream = new FileInputStream(file);
		GeometryJSON gjson = new GeometryJSON(15);
		FeatureJSON fjson = new FeatureJSON(gjson);

		// System.out.println(fjson.readCRS(inputstream));

		FeatureCollection<SimpleFeatureType, SimpleFeature> jfc = fjson
				.readFeatureCollection(inputstream);

		final SimpleFeatureType jsonSchema = jfc.getSchema();

		final SimpleFeatureTypeBuilder dbSftBuilder = new SimpleFeatureTypeBuilder();
		final List<AttributeDescriptor> ads = jsonSchema
				.getAttributeDescriptors();
		for (final AttributeDescriptor ad : ads) {

			final String n = ad.getName().toString();

			final Name name = new NameImpl(n.toUpperCase());
			final AttributeDescriptor t = new AttributeDescriptorImpl(
					ad.getType(), name, ad.getMinOccurs(), ad.getMaxOccurs(),
					ad.isNillable(), ad.getDefaultValue());
			dbSftBuilder.add(t);
		}
		// TODO: EPSG!!
		this.epsg = "";

		this.geomType = jsonSchema.getGeometryDescriptor().getType()
				.getBinding().getName();

		// Getting Geomtries and Attributes right
		try (FeatureIterator<SimpleFeature> jsonFeatures = jfc.features()) {
			Map<Class, List<SimpleFeature>> classMap = new HashMap<Class, List<SimpleFeature>>();
			classMap.put(Polygon.class, new ArrayList<SimpleFeature>());
			classMap.put(MultiPolygon.class, new ArrayList<SimpleFeature>());
			classMap.put(LineString.class, new ArrayList<SimpleFeature>());
			classMap.put(Point.class, new ArrayList<SimpleFeature>());

			// Hässlichster Code den ich je gesehen habe.
			SimpleFeatureTypeBuilder pointS = dbSftBuilder;
			SimpleFeatureTypeBuilder polyS = dbSftBuilder;
			SimpleFeatureTypeBuilder mPolyS = dbSftBuilder;
			SimpleFeatureTypeBuilder lineS = dbSftBuilder;
			pointS.setName(new NameImpl(tableName + ":Polygon"));
			polyS.setName(new NameImpl(tableName + ":MultiPolygon"));
			mPolyS.setName(new NameImpl(tableName + ":LineString"));
			lineS.setName(new NameImpl(tableName + ":Point"));
			
			
			final SimpleFeatureType poi = pointS.buildFeatureType();
			final SimpleFeatureType pol = polyS.buildFeatureType();
			final SimpleFeatureType mPo = mPolyS.buildFeatureType();
			final SimpleFeatureType lin = lineS.buildFeatureType();

			while (jsonFeatures.hasNext()) {
				final SimpleFeature sf = jsonFeatures.next();
				String foo = sf.getDefaultGeometry().getClass().getName();

				SimpleFeatureBuilder dbSfBuilder;
				if (foo.contains("MultiPolygon")) { dbSfBuilder = new SimpleFeatureBuilder(
							mPo);
				} else if (foo.contains("Point")) {
				 dbSfBuilder = new SimpleFeatureBuilder(
							poi);
				} else if (foo.contains("LineString")) {
					 dbSfBuilder = new SimpleFeatureBuilder(
							lin);
				} else {
					dbSfBuilder = new SimpleFeatureBuilder(
							pol);

					for (final AttributeDescriptor ad : jsonSchema
							.getAttributeDescriptors()) {

						final String attr = ad.getLocalName();
						final String name = attr.toUpperCase();
						Object obj = sf.getAttribute(attr);
						dbSfBuilder.set(name, obj);

					}
					final SimpleFeature of = dbSfBuilder.buildFeature(null);
					classMap.get(sf.getDefaultGeometry().getClass()).add(of);
				}
			}
			for (List<SimpleFeature> list : classMap.values()) {
				if (list.size() > 0) {
					FeatureCollection<SimpleFeatureType, SimpleFeature> collection = new ListFeatureCollection(
						pol	, list);
					dbCollection.add(collection);
				}
			}
		}
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
