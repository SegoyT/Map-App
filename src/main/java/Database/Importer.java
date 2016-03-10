package Database;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


public interface Importer {
	
	public void setFile(File file,String name);
	
	public void handleFile();
	
	public Collection<FeatureCollection<SimpleFeatureType, SimpleFeature>> getCollection();
	
	public void readFile(String tablename, File file)throws IOException;
	
	public String getEpsg();
	public String getGeomType();
}
