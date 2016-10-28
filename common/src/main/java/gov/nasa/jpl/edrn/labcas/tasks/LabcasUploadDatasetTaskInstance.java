package gov.nasa.jpl.edrn.labcas.tasks;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

import gov.nasa.jpl.edrn.labcas.Constants;
import gov.nasa.jpl.edrn.labcas.utils.FileManagerUtils;

/**
 * Task that initializes the upload of a new Dataset within a given Collection (aka ProductType):
 * o the ProductType name must be supplied as part of the task configuration metadata.
 * o the DatasetId is passed as part of the XML/RPC HTTP request.
 * 
 * @author luca
 *
 */
public class LabcasUploadDatasetTaskInstance implements WorkflowTaskInstance {
	
	private static final Logger LOG = Logger.getLogger(LabcasUploadDatasetTaskInstance.class.getName());
	
	@Override
	public void run(Metadata metadata, WorkflowTaskConfiguration config) throws WorkflowTaskInstanceException {
		
		// debug: print all workflow instance metadata
        for (String key : metadata.getAllKeys()) {
        	for (String val : metadata.getAllMetadata(key)) {
        		LOG.fine("==> Input metadata key="+key+" value="+val);
        	}
        }
		        
		try {
			
			// populate dataset metadata from workflow configuration
			Metadata datasetMetadata = FileManagerUtils.readConfigMetadata(metadata, config);
			
			// retrieve product type from configuration metadata
			// also needed at file-level metadata for ingestion
			String productTypeName = datasetMetadata.getMetadata(Constants.METADATA_KEY_PRODUCT_TYPE);
			metadata.replaceMetadata(Constants.METADATA_KEY_PRODUCT_TYPE, productTypeName); // transfer to product level metadata
			LOG.info("Using productType="+productTypeName );
			
			// retrieve dataset identifier from XML/RPC parameters
			String datasetId = metadata.getMetadata(Constants.METADATA_KEY_DATASET_ID);
			// enforce no spaces
			if (datasetId.contains(" ")) {
				throw new WorkflowTaskInstanceException("DatasetId cannot contain spaces");
			}
						
	        // add dataset version to product type metadata (used for generating product unique identifiers)
			//String parentDatasetId = datasetMetadata.getMetadata(Constants.METADATA_KEY_PARENT_DATASET_ID);
	        //int version = FileManagerUtils.findLatestDatasetVersion( datasetId, parentDatasetId );
	        // FIXME
	        int version = 0;
	        if (version==0) {  // dataset does not yet exist -> assign first version
	        	version = 1; 
	        } else {              // keep the same version unless the flag is set
	        	if (Boolean.parseBoolean(metadata.getMetadata(Constants.METADATA_KEY_NEW_VERSION))) {
	        		version += 1; // increment version
	        		metadata.removeMetadata(Constants.METADATA_KEY_NEW_VERSION); // remove the flag
	        	}
	        }
	        datasetMetadata.replaceMetadata(Constants.METADATA_KEY_VERSION, ""+version); // product type metadata
	        metadata.replaceMetadata(Constants.METADATA_KEY_VERSION, ""+version);        // workflow (-> product) metadata
	        LOG.fine("Using dataset version=: "+version);

						
			// copy all product type metadata to product metadata
	        for (String key : datasetMetadata.getAllKeys()) {
	        	if (!metadata.containsKey(key)) {
	        		LOG.fine("==> Copy metadata for key="+key+" from dataset-level to file-level.");
	        		metadata.addMetadata(key, datasetMetadata.getAllMetadata(key));
	        	}
	        }
			
			// reload the catalog configuration so that the new product type is available for publishing
			//FileManagerUtils.reload();
	                        
	        // remove all .met files from staging directory - probably a leftover of a previous workflow submission
	        String stagingDir = System.getenv(Constants.ENV_LABCAS_STAGING) + "/" + datasetId;
	        String[] metFiles = new File(stagingDir).list(new FilenameFilter() {
	                  @Override
	                  public boolean accept(File current, String name) {
	                    return new File(current, name).getAbsolutePath().endsWith(Constants.OODT_METADATA_EXTENSION);
	                  }
	                });
	        for (String metFile : metFiles) {
	        	File _metFile = new File(stagingDir, metFile);
	        	LOG.fine("Deleting older metadata file: "+_metFile.getAbsolutePath());
	        	_metFile.delete();
	        }
		
		} catch(Exception e) {
			e.printStackTrace();
			LOG.warning(e.getMessage());
			throw new WorkflowTaskInstanceException(e.getMessage());
		}
		
	}

}
