package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

import Geoserver.Prj2Epsg;

public class ShapeImporter implements Importer {

	private File uploadFile;

	private List<Entry<String, String>> shapeAttrs;

	private String epsg;

	private String tableName;
	private Collection<FeatureCollection<SimpleFeatureType, SimpleFeature>> dbCollection;

	private String geomType;

	public void setFile(File file, String name) {
		this.uploadFile = file;
		this.tableName = name;
	}

	public void handleFile() {
		try {
			if (this.uploadFile == null) {
				throw new IllegalArgumentException(
						"No Shapefile: Please provide a Shapefile");
			}
			final File tmpDir = new File(File.createTempFile("ShapeImporter",
					"dir").getAbsolutePath()
					+ ".zip");
			tmpDir.mkdir();

			// '.zip' entpacken

			final File file = this.uploadFile;
			final ZipInputStream inputstream = new ZipInputStream(
					new FileInputStream(file));
			ZipEntry entry = inputstream.getNextEntry();

			// Liste erstellen in der die Entries gespeichert werden, und die
			// '.dbf' und '.shx' Dateien einzeln ablegen
			final List<String> map = new ArrayList<String>();
			File shpFile = null;

			// Durch das Verzeichnis laufen, und die Dateien für die Bearbeitung
			// speichern.
			while (entry != null) {
				final File tmpFile = new File(tmpDir, entry.getName());

				try (FileOutputStream output = new FileOutputStream(tmpFile)) {
					IOUtils.copy(inputstream, output);
				}
				map.add(entry.getName());

				if (entry.getName().toLowerCase().endsWith(".shp")) {
					shpFile = tmpFile;
				}
				entry = inputstream.getNextEntry();
			}
			if (shpFile == null) {
				tmpDir.delete();
			}

			// read
			readFile(tableName, shpFile);
			tmpDir.delete();
			inputstream.close();

		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			this.uploadFile.delete();

		}

	}

	public Collection<FeatureCollection<SimpleFeatureType, SimpleFeature>> getCollection() {
		return this.dbCollection;
	}

	public void readFile(
			String tableName, File file) throws IOException {
		final FileDataStore shpDataStore = FileDataStoreFinder
				.getDataStore(file);
		final SimpleFeatureSource shpSource = shpDataStore.getFeatureSource();
		final SimpleFeatureType shpSchema = shpSource.getSchema();

		final List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
		final FeatureCollection<SimpleFeatureType, SimpleFeature> shpCollection = shpSource
				.getFeatures();

		// Create Table, store Attr Name and Type
		this.shapeAttrs = new ArrayList<Entry<String, String>>();
		final SimpleFeatureTypeBuilder dbSftBuilder = new SimpleFeatureTypeBuilder();
		dbSftBuilder.setName(new NameImpl(tableName));
		final List<AttributeDescriptor> ads = shpSchema
				.getAttributeDescriptors();
		for (final AttributeDescriptor ad : ads) {
		
			final String n = ad.getName().toString();
			final String typ = ad.getType().getBinding().getSimpleName();
			final Entry<String, String> attr = new SimpleEntry<String, String>(
					n, typ);
			this.shapeAttrs.add(attr);

			// add to Database
			final Name name = new NameImpl(n.toUpperCase());
			final AttributeDescriptor t = new AttributeDescriptorImpl(
					ad.getType(), name, ad.getMinOccurs(), ad.getMaxOccurs(),
					ad.isNillable(), ad.getDefaultValue());
			dbSftBuilder.add(t);
		}
		// TODO: EPSG, wenn nicht automatisch erstellt wird übergeben lassen.
		try {
			String prj = shpSchema.getCoordinateReferenceSystem().toString();
			this.epsg = Prj2Epsg.getEpsg(prj);
		} catch (Exception e) {
			String prj = "";
			this.epsg = prj;
		}
		System.out.println("Epsg:  " + this.epsg);
		this.geomType = shpSchema.getGeometryDescriptor().getType().getName()
				.getLocalPart();

		final SimpleFeatureType dbSchema = dbSftBuilder.buildFeatureType();

		// Convert Shape Attributes to upper case 
		try (FeatureIterator<SimpleFeature> shpFeatures = shpCollection
				.features()) {
			while (shpFeatures.hasNext()) {
				final SimpleFeature sf = shpFeatures.next();
				final SimpleFeatureBuilder dbSfBuilder = new SimpleFeatureBuilder(
						dbSchema);
				for (final AttributeDescriptor ad : shpSchema
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
		final FeatureCollection<SimpleFeatureType, SimpleFeature> fCollection = new ListFeatureCollection(
				dbSchema, featureList);
		shpDataStore.dispose();
		dbCollection.add(fCollection);

	}

	public String getEpsg() {
		return this.epsg;
	}

	public String getGeomType() {
		return this.geomType;
	}

}
