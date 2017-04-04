package gov.nasa.jpl.edrn.labcas.actions;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

import gov.nasa.jpl.edrn.labcas.Constants;
import gov.nasa.jpl.edrn.labcas.utils.GeneralUtils;

/**
 * Class that publishes images of compatible type to the QUIP Image Viewer.
 * 
 * @author cinquini
 *
 */
public class QuipImageViewerPostIngestionAction extends CrawlerAction {
	
    // QUIP publishing endpoint
    private String quipSubmitImageUrl = "http://localhost:6002/submitData";
    
    // QUIP display endpoint
    private String quipViewImageUrl = "http://localhost:8000/camicroscope/osdCamicroscope.php?tissueId=";
    
	// compatible file extensions
    private String extensions = "";
    
    private Set<String> extensionsSet = new HashSet<String>();
    
    private Properties properties = new Properties();

	@Override
	public boolean performAction(File product, Metadata productMetadata) throws CrawlerActionException {
		
		// determine file extension
		String extension = GeneralUtils.getFileExtension(product).toLowerCase();
		
		// process compatible extensions
		if (this.extensionsSet.contains(extension)) {
			this.uploadFile(product, productMetadata);
		}
		
		// success
		return true;
		
	}
	
	/**
	 * Method to upload a file via a multi-part/form-data POST request to the QUIP server.
	 */
	private void uploadFile(File product, Metadata productMetadata) {
		
		LOG.info("QUIP: uploading file: "+product.getAbsolutePath());

		HttpClient httpclient = new DefaultHttpClient();
		HttpEntity resEntity = null;
		
		try {
			
			HttpPost httppost = new HttpPost(this.quipSubmitImageUrl);

			FileBody upload = new FileBody(product);
			StringBody case_id = new StringBody(product.getName());

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("upload", upload);
			reqEntity.addPart("case_id", case_id);
			httppost.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(httppost);
			resEntity = response.getEntity();
			LOG.info("QUIP upload result="+resEntity.toString());
			
			// add URL to metadata
			productMetadata.addMetadata("FileUrl", 
					Constants.URL_TYPE_CAMICROSCOPE + "|" + this.quipViewImageUrl + "?tissueId=" + product.getName());

		} catch(Exception e) {
			LOG.warning("QUIP upload resulted in error: "+e.getMessage());
			
		} finally {
			try {
				if (resEntity != null) {
					InputStream instream = resEntity.getContent();
					instream.close();
				}
			} catch(Exception e) {}
		}

    }
	
	/**
	 * Converts the 'extensions' String into a Set.
	 * Also retrieves the QUIP server end-points from the properties values.
	 */
	@Override
	public void validate() throws CrawlerActionException {
			
		
		String[] extensionsArray = extensions.split(",");
		for (String ext : extensionsArray) {
			extensionsSet.add(ext.toLowerCase());
		}
		LOG.info("QUIP will process these file extensions: "+extensionsSet);		
		
		// loop over properties
        for(Entry<Object, Object> e : properties.entrySet()) {
            LOG.info("QUIP will use this property: "+e);
        }
        
        this.quipSubmitImageUrl = properties.getProperty("quipSubmitImageUrl");
        this.quipViewImageUrl = properties.getProperty("quipViewImageUrl");
		
	}
	
    public void setQuipSubmitImageUrl(String quipSubmitImageUrl) {
		this.quipSubmitImageUrl = quipSubmitImageUrl;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}
	
	public void setQuipViewImageUrl(String quipViewImageUrl) {
		this.quipViewImageUrl = quipViewImageUrl;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
