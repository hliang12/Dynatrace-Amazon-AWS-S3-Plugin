package com.dynatrace.AwsS3;

public class S3Exception extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public S3Exception(){
		
	}
	
	public S3Exception(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public S3Exception(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public S3Exception(String message, Throwable cause) {
		super(message, cause);
	}
}
