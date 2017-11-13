package net.schwehla.matrosdms.translate.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class MatrosPropertyFileWrapper implements Comparable<MatrosPropertyFileWrapper >{
	
	// Eclipse-File
	IFile file;
	
	// Property-Model
	PropertiesConfiguration config;
	
	Map <String, MatrosMappingModelSingleProperty> singleGuiProperty = new HashMap<>();
	
	public MatrosPropertyFileWrapper(IFile file ) {
		this.file = file;
	}

	
	public Map<String,MatrosMappingModelSingleProperty> getSingleGuiProperty() {
		return singleGuiProperty;
	}


	public void setSingleGuiProperty(Map<String,MatrosMappingModelSingleProperty> singleGuiProperty) {
		this.singleGuiProperty = singleGuiProperty;
	}


	public IFile getFile() {
		return file;
	}


	public PropertiesConfiguration getConfig() {
		return config;
	}


	@Override
	public int compareTo(MatrosPropertyFileWrapper o) {
		// TODO Auto-generated method stub
		return file.getName().compareTo(o.getFile().getName());
	}

	public String getKey() {
	
		String name = file.getName();
		if (name.indexOf("_") > 0) {
			
			String tmp[] = name.split("_");
			String key = tmp[tmp.length-1].replace(".properties", "");
			return key;
			
		} else {
			return "default";
		}
		
	}


	public void save() throws Exception {
		
		try {
			
			singleGuiProperty.values().stream().forEach( x -> {
				
				if (!config.containsKey(x.getText1()) && !x.isNotFilled()) {
					config.setProperty(x.getPropertyName(), x.getUserTranslatedText());
				}
				
				
			});
			
			StringWriter w = new StringWriter();
			config.write(w);
			
			InputStream is = new ByteArrayInputStream(w.toString().getBytes());
					

			file.setContents(is, IResource.FORCE, null);
			
		} finally {
			
			file.refreshLocal(IResource.DEPTH_ZERO, null);
			
			
		}
		

		
		// Refresh

	}


	public void initModel() throws Exception {
		
		
		config= new PropertiesConfiguration();

		config.setThrowExceptionOnMissing(false);
		config.setIncludesAllowed(false);

		Charset iso88591charset = Charset.forName("ISO-8859-1");

		InputStream content = file.getContents();
		config.read(new InputStreamReader(content, iso88591charset));
		
		
		config.getKeys().forEachRemaining( e -> {
			
			if (singleGuiProperty.containsKey(e)) {
				singleGuiProperty.get(e).setPropertyName(e);
				singleGuiProperty.get(e).setUserTranslatedText( config.getString(e));
			}
			
			
		});
		

		
		
	}
	

}
