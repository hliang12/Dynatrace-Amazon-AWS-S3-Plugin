package com.dynatrace.AwsS3;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.dynatrace.AwsS3.S3Exception;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.Optional;


public class S3Monitor implements Monitor {

	
	protected static final String CONFIG_ACCCESS_KEY = "accessKey";
	protected static final String CONFIG_SECRET_ACCESS_KEY = "secretAccessKey";
	protected static final String CONFIG_INSTANCE_NAME = "instanceName";
	protected static final String CONFIG_REGIGON = "region";
	protected static final String CONFIG_GRANULARITY = "granularity";
	protected static final String CONFIG_IS_BUCKET_SELECTED = "bucketSizeBytes";
	protected static final String CONFIG_TYPE_BUCKET_BYTE_METRIC = "storageType";
	protected static final String CONFIG_IS_NUMBER_OF_OBJ_SELECTED = "numberOfObjects";
	
	private S3MetricExtractor metricExtractor;
	
	private List<String> s3Metrics = new ArrayList<String>();
	
	private static final Logger log = Logger.getLogger(S3Monitor.class.getName());

	@Override
	public Status setup(MonitorEnvironment env) throws Exception {
		
		metricExtractor = new S3MetricExtractor(env);
		
			// request metrics
			
		    s3Metrics.add("AllRequests");
		    s3Metrics.add("GetRequests");
		    s3Metrics.add("PutRequests");
		    s3Metrics.add("DeleteRequests");
		    s3Metrics.add("HeadRequests");
		    s3Metrics.add("PostRequests");
		    s3Metrics.add("ListRequests");
		    s3Metrics.add("BytesDownloaded");
		    s3Metrics.add("BytesUploaded");
		    s3Metrics.add("4xxErrors");
		    s3Metrics.add("5xxErrors");
		    s3Metrics.add("FirstByteLatency");
		    s3Metrics.add("TotalRequestLatency");
		    
		    
		  
		    if(env.getConfigBoolean(CONFIG_IS_BUCKET_SELECTED)){
		    	s3Metrics.add("BucketSizeBytes");
		    }
		    log.info("is number of obj there "+ env.getConfigBoolean(CONFIG_IS_NUMBER_OF_OBJ_SELECTED));
		    if(env.getConfigBoolean(CONFIG_IS_NUMBER_OF_OBJ_SELECTED)){
		    	s3Metrics.add("NumberOfObjects");
		    }
		    
		
		return new Status(Status.StatusCode.Success);
	}


	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
		 
		
		String accessKey = env.getConfigString(CONFIG_ACCCESS_KEY).trim();
		String secretAccessKey = env.getConfigPassword(CONFIG_SECRET_ACCESS_KEY).trim();
		String s3Name = env.getConfigString(CONFIG_INSTANCE_NAME).trim();
		String region = env.getConfigString(CONFIG_REGIGON).trim();
		String granularity = env.getConfigString(CONFIG_GRANULARITY).trim();
		Optional <String> storageType = Optional.of("");
		
		 if(env.getConfigBoolean(CONFIG_IS_BUCKET_SELECTED)){
			 storageType = Optional.of(env.getConfigString(CONFIG_TYPE_BUCKET_BYTE_METRIC));
		 }
		
		log.info(env.getConfigString(CONFIG_TYPE_BUCKET_BYTE_METRIC));
		
		log.info("accessKey = "+accessKey+ " secretKey = "+secretAccessKey+  " s3Name = " + s3Name+ " region = "+ region + " gran = " + granularity);
				//check if there is a value 
		
				if(accessKey == null | accessKey.isEmpty()){
					throw new S3Exception("No Access Key provided");
				}
				if(secretAccessKey == null | secretAccessKey.isEmpty()){
					throw new S3Exception("No Secret Access Key provided");
				}
				if(s3Name == null | s3Name.isEmpty()){
					throw new S3Exception("No Load Balancer Name provided");
				}
				if(region == null | region.isEmpty()){
					throw new S3Exception("No Region provided");
				}
				
				//Login to access the cloudwatch metrics
				AmazonCloudWatch watcher = metricExtractor.Login(accessKey, secretAccessKey, region);
				
				Collection<MonitorMeasure> monitorMeasures;
				
				// start extracting values for metrics which are subscribed to
				for(String metricName: s3Metrics){
				monitorMeasures = env.getMonitorMeasures("AWS S3 Metrics",metricName);
			
				GetMetricStatisticsResult result = metricExtractor.getResultsS3Metrics(watcher,s3Name,metricName,granularity,storageType);
				List<Datapoint> dp = result.getDatapoints();
				log.info(dp.size() + "  the dp size");

					for (MonitorMeasure subscribedMonitorMeasure : monitorMeasures) {
						
						
						double average = 0;
						double sum = 0;
						double min = 0;
						double max = 0;
						
						
				 		
				 		for(Datapoint p : dp){
				 			log.info(p.getSum() +  "  this should have a val?");
				 			
				 			average = p.getAverage();
				 			sum = p.getSum();
				 			min = p.getMinimum();
				 			max = p.getMaximum();
				 		}
				 		
				 		MonitorMeasure dynamicMeasure; 
				 		
				 		switch (metricName){
			        	case "AllRequests":
			        	case "GetRequests":
			        	case "PutRequests":
			        	case "DeleteRequests":
			        	case "HeadRequests":
			        	case "PostRequests":
			        	case "ListRequests":
			        		if(sum!=-1){
			        			dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Sum");
			        			dynamicMeasure.setValue(sum);
			        		}
			        		break;
			        	
			        	case "BucketSizeBytes":
			        	case "NumberOfObjects":
			        		if(sum!=-1){
			        			dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Average");
			        			dynamicMeasure.setValue(sum);
			        		}
			        		break;
			        		
			        	default:
			        
			        	if(average!=-1){
			        		dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Average");
			        		dynamicMeasure.setValue(average);
			        	}
						if(sum!=-1){
							dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Sum");
							dynamicMeasure.setValue(sum);
						}
						if(min!=-1){
							dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Minimum");
							dynamicMeasure.setValue(min);
						}
						if(max!=-1){
							dynamicMeasure = env.createDynamicMeasure(subscribedMonitorMeasure, "Aggregation", "Maximum");
							dynamicMeasure.setValue(max);
						}
						break;
				 		}
				}
			}
		
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public void teardown(MonitorEnvironment env) throws Exception {
		// TODO
	}
}
