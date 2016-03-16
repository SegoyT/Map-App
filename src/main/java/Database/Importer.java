package Database;

import java.io.File;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


public interface Importer {
	
	public void setFile(File file,String name);
	
	public void handleFile();
	
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection();
	
	public void readFile(String tablename, File file)throws Exception;
	
	public String getEpsg();
	public String getGeomType();
}
