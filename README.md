# Dynatrace-Amazon-AWS-S3-Plugin

Author: Hao-lin Liang (hao-lin.liang@dynatrace.com)

Available Metrics

Amazon provide guidance on which metrics and aggregation combinations are most useful (see here) and these are listed in bold below. These are the only metrics that have been tested during development of this plugin. However, others should work.

Daily Storage metrics(Optional metrics)

- _BucketSizeBytes - Average (Filter types: StandardStorage, StandardIAStorage, ReducedRedundancyStorage)
- _NumberOfObjects - Average (Filter types: AllStorageTypes)

Request Metrics 

- _AllRequests - Sum
- _GetRequests - Sum
- _PutRequests - Sum
- _DeleteRequests - Sum
- _PostRequests -Sum
- _ListRequests - Sum
- _BytesUploaded - Average, Sum, Min, Max
- _4xxErrors - Average, Sum, Min, Max
- _5xxErrors - Average, Sum, Min, Max
- _FirstByteLatency - Average, Sum, Min, Max
- _TotalRequestLatency - Average, Sum, Min, Max



## Prerequisites
To use this plugin, you'll need the following details:

- **Access Key**: User must be in a group which has the *CloudWatchReadOnlyAccess* permission.
- **Secret Access Key**: Corresponding Secret Access key for the above.
- **AWS Region**: eg. *eu-west-2* defaults to *eu-west-2*
- **Bucket Name**: e.g *myBucket* 


## Usage

1. Download the latest release from the Dynatrace Community.
2. Install the plugin via the client (or use the [REST interface](https://community.dynatrace.com/community/pages/viewpage.action?pageId=221381697) to automate).
3. Create a monitor and configure the monitor (the *host* setting is not used so setting this to localhost is acceptable).

### IMPORTANT: Supported Data Granularities

The **data granularity** and the plugin schedule time **MUST** match. In the following screenshots, both are set to 5 minutes.

#### Supported Data Granularities

- "1 Minute"
- "5 Minutes"
- "10 Minutes"
- "15 Minutes"
- "30 Minutes"
- "1 Hour"

    
![](http://i67.tinypic.com/2upq00k.png)

![](http://i68.tinypic.com/2yl93kj.png)
