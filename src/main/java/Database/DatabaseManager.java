package Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DatabaseManager {
	private Map<String,Object> params;
	private JDBCDataStore dataStore;
	private String epsg;
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	private String geometryType;
	private String tName;
	private boolean uploaded;

	
	public DatabaseManager() {
		
		this.params = new HashMap<String,Object>();
		this.params.put("dbtype", "postgis");
		this.params.put("host", "localhost");
		this.params.put("port", new Integer(5432));
		this.params.put("schema", "public");
		this.params.put("database", "kartenappdb");
		this.params.put("user", "postgres");
		this.params.put("passwd", "postgres");
	}

	public void addFile(InputStream fileStream, String name) {
		uploaded = false;
		try {
			
			// TODO: Pfad anpassen
			OutputStream os = new FileOutputStream("./"
					+ name);
			byte[] buffer = new byte[1024];
			int bytesRead;
			// read from fileStream to buffer
			while ((bytesRead = fileStream.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.flush();
			os.close();
			fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File("./" + name);

		Importer fileImport;
		if (name.endsWith(".zip")) {
			fileImport = new ShapeImporter();
			name = name.replace(".zip", "");
			this.tName = name;
		} else if (name.endsWith(".geojson")) {
			fileImport = new GeoJSONImporter();
		} else if (name.endsWith(".xml")) {
			fileImport = null;
		} else {
			fileImport = null;
			this.tName = name;
		}
		if (fileImport != null) {
			try{
			this.dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(this.params);
			fileImport.setFile(file, name);
			fileImport.handleFile();
			this.epsg = fileImport.getEpsg();
			this.geometryType = fileImport.getGeomType();

			try{
			writeFeaturesToDB(fileImport.getCollection(),name);	
			}catch(Exception e){
				System.err.println("ERROR: Database Schema "+name+ " already exists. Please try another File.");
				dataStore.dispose();
			}
			
			}catch (Exception e) {
				this.dataStore.dispose();
				file.delete();
				e.printStackTrace();
			}finally{
				file.delete();
				this.dataStore.dispose();
			}
			
			
		}
		file.delete();
	}
	public void deleteTable(String name) throws IOException, SQLException{
		dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(this.params);
		Connection con = this.dataStore.getDataSource().getConnection();
		String dropTable = "Drop TABLE public.\""+name+"\";";
		con.prepareStatement(dropTable).execute();
		System.out.println("INFORMATION: DataTable deleted!");
		con.close();
		dataStore.dispose();
	}

	public void writeFeaturesToDB(final FeatureCollection<SimpleFeatureType, SimpleFeature> dbCollection,
			final String tableName)
			throws Exception {
		try {
			final SimpleFeatureType dbSchema = dbCollection.getSchema();
			this.dataStore.createSchema(dbSchema);
			
			final SimpleFeatureSource dbSource = this.dataStore.getFeatureSource(dbSchema.getName().toString());

			// write features into db table
			if (dbSource instanceof SimpleFeatureStore) {
				final SimpleFeatureStore dbFeatureStore = (SimpleFeatureStore) dbSource;

				final Transaction t = new DefaultTransaction("create");
				dbFeatureStore.setTransaction(t);

				try {
					dbFeatureStore.addFeatures(dbCollection);
					t.commit();
					t.close();
				} catch (final IOException e) {
					System.out.println("Error while trying to commit features into database "+e);
					t.rollback();
					t.close();
					throw new Exception();
				}

				System.out.println(dbCollection.getSchema().getCoordinateReferenceSystem());
				System.out.println(dbCollection.getSchema().getGeometryDescriptor());
				

				final ReferencedEnvelope env = dbCollection.getBounds();
				this.minX = env.getMinX();
				this.minY = env.getMinY();
				this.maxX = env.getMaxX();
				this.maxY = env.getMaxY();
				

				this.dataStore.dispose();
				uploaded = true;

			} else {
				this.dataStore.dispose();
				System.out.println("DB not writable");
				throw new Exception();
			}
		} catch (final SQLException | IOException e) {
			System.out.println("Error processing file"+ e);
			throw new Exception();
		}
		
	}
	public double getminX(){
		return this.minX;
	}
	public double getminY(){
		return this.minY;
	}
	public double getmaxX(){
		return this.maxX;
	}
	public double getmaxY(){
		return this.maxY;
	}
	public String getEpsg(){
		return this.epsg;
	}
	public String getgeomType(){
		return this.geometryType;
	}
	public String getTableName(){
		return this.tName;
	}
	public boolean isUploaded() {
		return uploaded;
	}

}
