package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

public class GeoJSONImporter implements Importer {

	private File uploadFile;
	private String tableName;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection;
	private String epsg;
	private String geomType;

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
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection() {
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

		dbSftBuilder.setName(new NameImpl(tableName));
		final SimpleFeatureType dbSchema = dbSftBuilder.buildFeatureType();
		List<SimpleFeature> sfList = new ArrayList<SimpleFeature>();
		// Getting Geomtries and Attributes right
		try (FeatureIterator<SimpleFeature> jsonFeatures = jfc.features()) {
			
			String bar = null;
			while (jsonFeatures.hasNext()) {
				final SimpleFeature sf = jsonFeatures.next();
				String foo = sf.getDefaultGeometry().getClass().getName();

				// falls verschiedene Geoemtrien im JSON sind nur die erste
				// Anzeigen, alle anderen ignorieren.
				if (bar != null && !foo.equals(bar)) {
					System.out.println(foo);
					continue;
				}

				SimpleFeatureBuilder dbSfBuilder = new SimpleFeatureBuilder(
						dbSchema);

				for (final AttributeDescriptor ad : jsonSchema
						.getAttributeDescriptors()) {

					final String attr = ad.getLocalName();
					final String name = attr.toUpperCase();
					Object obj = sf.getAttribute(attr);
					dbSfBuilder.set(name, obj);

				}
				final SimpleFeature of = dbSfBuilder.buildFeature(null);
				sfList.add(of);
				bar = foo;
			}
		}

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = new ListFeatureCollection(
				dbSchema, sfList);
		dbCollection = collection;

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
