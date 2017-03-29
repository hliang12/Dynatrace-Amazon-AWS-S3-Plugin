package com.dynatrace.AwsS3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.Optional;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;


public class S3MetricExtractor {
	
	private List<String> aggregationList = new ArrayList<String>();
    
	private static final Logger log = Logger.getLogger(S3MetricExtractor.class.getName());
	
	public S3MetricExtractor(MonitorEnvironment env){
		
		
	    aggregationList.add("Average");
	    aggregationList.add("Sum");
	    aggregationList.add("Minimum");
	    aggregationList.add("Maximum");

	}


	public AmazonCloudWatch Login(String accessKey, String secretAccessKey,String region){

	        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretAccessKey);
	        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);
	        String strEndpoint = "https://monitoring." + region + ".amazonaws.com"; // monitoring.eu-west-2.amazonaws.com
	       // String strEndpoint = "http://s3-eu-west-2.amazonaws.com"+"/dynatracetest123";
	        EndpointConfiguration endpointConfig = new EndpointConfiguration(strEndpoint, region);
	        AmazonCloudWatch client = AmazonCloudWatchClientBuilder.standard().withCredentials(provider).withEndpointConfiguration(endpointConfig).build();

	        return client;
	 
	}
	
	
	private int calculateDataGranularity(String strDataGranularity) throws S3Exception{
		
		switch(strDataGranularity){
		
		case "1 Minute":
			
			return 60 *1000;
	
		case "5 Minute" :
			
			return 60 * 5 * 1000;
		
		case "10 Minute" : 
			
			return 60 * 10 * 1000;
		
		case "15 Minute": 
			
			return 60 * 15 * 1000;
		
		case "30 Minute":
			
			return 60 * 30 * 1000;
			
		case "60 Minute":
			
			return 60 * 60 * 1000;
			
		default: 
			
			throw new S3Exception("Strange data granularity selected");
		}
	}
	
	
	public  GetMetricStatisticsResult getResultsS3Metrics(AmazonCloudWatch client, String s3Name, String metricName, String granularity, Optional<String> storageType) throws S3Exception{
		 
		log.info(metricName);
		log.info(granularity);
		log.info(s3Name+  " the bucket name");
		
		log.info("-"+metricName+"-");
        Date endTime = new Date();
        
        int time = calculateDataGranularity(granularity);
        
        
        Date startTime = new Date(endTime.getTime() - time);
       

        String nameSpace = "AWS/S3";
        String name = "BucketName";  //// 
        
        GetMetricStatisticsRequest request1 = new GetMetricStatisticsRequest();
        
        switch(metricName){
        
        case "BucketSizeBytes":
        	request1.withStartTime(startTime)
            .withNamespace(nameSpace)
            .withPeriod(time)
            .withDimensions(new Dimension().withName(name).withValue(s3Name))
            .withDimensions(new Dimension().withName("FilterId").withValue("EntireBucket"))
            .withDimensions(new Dimension().withName("StorageType").withValue(storageType.get()))
            .withMetricName(metricName)
            .withStatistics(aggregationList)
            .withEndTime(endTime);
        	break;

        case "NumberOfObjects":
        	request1.withStartTime(startTime)
            .withNamespace(nameSpace)
            .withPeriod(time)
            .withDimensions(new Dimension().withName(name).withValue(s3Name))
            .withDimensions(new Dimension().withName("FilterId").withValue("EntireBucket"))
            .withDimensions(new Dimension().withName("StorageType").withValue("AllStorageTypes"))
            .withMetricName(metricName)
            .withStatistics(aggregationList)
            .withEndTime(endTime);
        	break;

        default: 
        	
        	request1.withStartTime(startTime)
            .withNamespace(nameSpace)
            .withPeriod(time)
            .withDimensions(new Dimension().withName("FilterId").withValue("EntireBucket"))
            //.withDimensions(new Dimension().withName("StorageType").withValue("StandardStorage"))
            .withDimensions(new Dimension().withName(name).withValue(s3Name))
            .withMetricName(metricName)
            
            .withStatistics(aggregationList)
            
            .withEndTime(endTime);
        	
        	break;
	
        }
     
        	
         try{
        	
        	GetMetricStatisticsResult	result = client.getMetricStatistics(request1);

        	return result; 
        	
        }catch(Exception e){
        	
        	log.severe(e.getMessage());
        }
         
       	return null;
       
	}
}
