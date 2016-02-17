package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.zip.ZipInputStream;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

public class GeoJSONImporter implements Importer {

	private File uploadFile;
	private String tablename;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection;

	
	@Override
	public void setFile(File file, String name) {
		this.uploadFile = file;
		this.tablename = name;

	}

	@Override
	public void handleFile() {
		try {
			if (this.uploadFile == null) {
				throw new IllegalArgumentException("No Shapefile: Please provide a Shapefile");
			}

			final File jsonfile = this.uploadFile;
			this.dbCollection = readfile(this.tablename, jsonfile);
			
			
			
		}catch (final IOException e) {
			e.printStackTrace();}

	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection() {
		return this.dbCollection;
	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> readfile(
			String tableName, File file) throws IOException {
		
		final InputStream inputstream = new FileInputStream(file);
		FeatureJSON fjson = new FeatureJSON();
		FeatureCollection<SimpleFeatureType, SimpleFeature> jfc = fjson.readFeatureCollection(inputstream);
		final SimpleFeatureType jsonSchema = jfc.getSchema();
		final List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
		
		final SimpleFeatureTypeBuilder dbSftBuilder = new SimpleFeatureTypeBuilder();
		dbSftBuilder.setName(new NameImpl(tableName));
		final List<AttributeDescriptor> ads = jsonSchema.getAttributeDescriptors();
		for (final AttributeDescriptor ad : ads) {
			// add to list for Gestaltung
			final String n = ad.getName().toString();
//			final String typ = ad.getType().getBinding().getSimpleName();
//			final Entry<String, String> attr = new SimpleEntry<String, String>(n, typ);
			
			final Name name = new NameImpl(n.toUpperCase());
			final AttributeDescriptor t = new AttributeDescriptorImpl(ad.getType(), name, ad.getMinOccurs(),
					ad.getMaxOccurs(), ad.isNillable(), ad.getDefaultValue());
			dbSftBuilder.add(t);
		}
		
		
		final SimpleFeatureType dbSchema = dbSftBuilder.buildFeatureType();
		System.out.println(dbSchema.getName());
		
		try (FeatureIterator<SimpleFeature> shpFeatures = jfc.features()) {
			while (shpFeatures.hasNext()) {
				final SimpleFeature sf = shpFeatures.next();
				final SimpleFeatureBuilder dbSfBuilder = new SimpleFeatureBuilder(dbSchema);
				for (final AttributeDescriptor ad : jsonSchema.getAttributeDescriptors()) {
					final String attr = ad.getLocalName();
					final String name = attr.toUpperCase();
					final Object obj = sf.getAttribute(attr);
					dbSfBuilder.set(name, obj);
				}
				final SimpleFeature of = dbSfBuilder.buildFeature(null);
				featureList.add(of);
			}
		}
		final FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection = new ListFeatureCollection(dbSchema,
				featureList);
		return dbCollection;
	}

	@Override
	public String getEpsg() {
		return null;
	}

	@Override
	public String getGeomType() {
		return null;
	}
	
	

}
