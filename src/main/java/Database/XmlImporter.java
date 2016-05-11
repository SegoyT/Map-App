package Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.wfs.v1_1.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XmlImporter implements Importer {

	private File uploadFile;
	private String tableName;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection;
	private String epsg;
	private String geomType;
	private Configuration config;

	@Override
	public void setFile(File file, String name) {
		this.uploadFile = file;
		this.tableName = name;
		config =  new GMLConfiguration();
	}

	@Override
	public void handleFile() {
		String filePath = uploadFile.getPath();
		try {
			readFile(tableName, uploadFile);
		} catch (IOException e) {
			System.err.println("ERROR: IO Exception");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("ERROR: Something wrong with xml parsing!");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection() {

		return dbCollection;
	}

	@Override
	public void readFile(String tablename, File file) throws IOException,
			SAXException, ParserConfigurationException {
		InputStream in;
		try {
			in = new FileInputStream(file);
		} catch (Exception e) {
			System.err.println("will see!");
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		//TODO Version anpassbar
		GML gml = new GML(Version.WFS1_0);
		FeatureCollection<SimpleFeatureType, SimpleFeature> xmlCollection = gml.decodeFeatureCollection(in);
		

//		URL schemaLocation = TestData.getResource(this, "states.xsd");
//
//		GML gml = new GML(Version.WFS1_1);
//		gml.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
//
//		SimpleFeatureType featureType = gml.decodeSimpleFeatureType(schemaLocation, new NameImpl(
//		        "http://www.openplans.org/topp", "states"));

		
		SimpleFeatureType xmlSchema = xmlCollection.getSchema();
		

//		TODO: Die Collection irgendwie richtig übergeben, sodass sie in die Datenbank geschrieben werden kann.
		final SimpleFeatureTypeBuilder dbSftBuilder = new SimpleFeatureTypeBuilder();
		dbSftBuilder.setName(new NameImpl(tableName));
		
//		Testcode für entfernen von features die nicht rein gehören:
//		xmlSchema.getAttributeDescriptors().remove(0)
		
		final List<AttributeDescriptor> ads = xmlSchema.getAttributeDescriptors();
		for (final AttributeDescriptor ad : ads) {
			final String n = ad.getName().toString();

			// add to Database
			final Name name = new NameImpl(n.toUpperCase());
			if (!(n.toUpperCase().equals("METADATAPROPERTY")||n.toUpperCase().equals("BOUNDEDBY")||n.toUpperCase().equals("DESCRIPTION")||n.toUpperCase().equals("NAME")||n.toUpperCase().equals("OBSERVED"))){
			final AttributeDescriptor t = new AttributeDescriptorImpl(
					ad.getType(), name, ad.getMinOccurs(), ad.getMaxOccurs(),
					ad.isNillable(), ad.getDefaultValue());
			dbSftBuilder.add(t);
			}
		}
		System.out.println(xmlSchema.getCoordinateReferenceSystem() + "    :"+xmlSchema.getGeometryDescriptor());
		geomType = xmlSchema.getGeometryDescriptor().getLocalName();
		final SimpleFeatureType dbSchema = dbSftBuilder.buildFeatureType();
		List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
		// Getting Geomtries and Attributes right
		try (FeatureIterator<SimpleFeature> xmlFeatures = xmlCollection
				.features()) {
			while (xmlFeatures.hasNext()) {
				final SimpleFeature sf = xmlFeatures.next();
				final SimpleFeatureBuilder dbSfBuilder = new SimpleFeatureBuilder(
						dbSchema);
				for (final AttributeDescriptor ad : dbSchema
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
		
		in.close();
		dbCollection = fCollection;
	}
//	 private File setSchemaLocation() throws Exception {
//        File xsd = File.createTempFile(tableName, "xsd");
//        IOUtils.copy(getClass().getResourceAsStream(tableName+".xsd"), 
//            new FileOutputStream(xsd));
//        
//        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        Document d = db.parse(getClass().getResourceAsStream( tableName+".xml") );
//        d.getDocumentElement().setAttribute( "xsi:schemaLocation", 
//            "http://www.openplans.org/topp " + xsd.getCanonicalPath() );
//        
//        File xml = File.createTempFile("states", "xml");
//        TransformerFactory.newInstance().newTransformer().transform( 
//            new DOMSource(d), new StreamResult(xml));
//        return xml;
//    }
	
	

	@Override
	public String getEpsg() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getGeomType() {
		return geomType;
	}

}
