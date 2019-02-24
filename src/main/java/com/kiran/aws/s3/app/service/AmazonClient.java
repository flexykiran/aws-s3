package com.kiran.aws.s3.app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;


/**
 * 
 * @author flexykiran
 *
 */
@Service
public class AmazonClient {

	private static final Log logger = LogFactory.getLog(AmazonClient.class);
	
	private AmazonS3 s3client;

    @Value("${aws.s3.endpointUrl}")
    private String endpointUrl;
    @Value("${aws.s3.bucketName}")
    private String bucketName;
    @Value("${aws.accessKey}")
    private String accessKey;
    @Value("${aws.secretKey}")
    private String secretKey;
    
    
    /**
     * call this method after class constructor is invoked to set Credential detail.
     */
	@PostConstruct
    private void initializeAmazon() {
    	BasicAWSCredentials creds = new BasicAWSCredentials(this.accessKey, this.secretKey);
    	this.s3client = AmazonS3ClientBuilder.standard().
    			withCredentials(new AWSStaticCredentialsProvider(creds)).
    			withRegion(Regions.US_WEST_2).build();
    	/*Region region = Region.getRegion(Regions.US_WEST_2);
    	s3client.setRegion(region);*/
       /* AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
       this.s3client = new AmazonS3Client(credentials);*/
    }

    /**
     * 
     * @param file
     * @return
     * @throws IOException
     */
	public String uploadFile(MultipartFile file) throws IOException {
		
		//convert the MultiPartFile object to File
		File tempFile = convertMultiPartToFile(file);
		
		/*Get the original file name and add dynamic name based on
			timestamp inorder to avoid overiding of file in S3.*/
		String fileName = generateFileName(file);
		
		//URL for accessing the S3
		String url = endpointUrl + "/" + bucketName + "/" + fileName;
		
		logger.info("URL: "+url);
		logger.info("FileName: "+fileName);
		logger.info("BucketName: "+bucketName);
		
		//call upload method of s3client.
		PutObjectResult result = s3client.putObject(new PutObjectRequest(bucketName, fileName, tempFile).
		    		withCannedAcl(CannedAccessControlList.PublicRead));
		logger.info("ETag: "+result.getETag());
		
		
		
		//delete the temporary file.
		tempFile.delete();
		
		//return the URL.
		return url;
	}
	
	/**
	 * 
	 * @param fileUrl
	 * @return
	 * @throws IOException 
	 */
	public String downloadFile(String fileName, String destination) throws IOException {
		
		String destFileName = destination + new Date().getTime() + fileName;
		//Using TransferManagerBuilder
		//TransferManagerBuilder.defaultTransferManager().download(bucketName, fileName, new File(destFileName));
		
		//Using InputStream
		S3Object s3Obj = s3client.getObject(new GetObjectRequest(bucketName, fileName));
		InputStream in = s3Obj.getObjectContent();
		Files.copy(in, Paths.get(destFileName));
		
		return destFileName;
		
		
	}

	/**
	 * 
	 * @param fileUrl
	 * @return
	 */
	public String deleteFileFromS3Bucket(String fileName) {
		s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
		return "Deleted Successfully "+fileName;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
	    File convFile = new File(file.getOriginalFilename());
	    FileOutputStream fos = new FileOutputStream(convFile);
	    fos.write(file.getBytes());
	    fos.close();
	    return convFile;
	}
	
	/**
	 * 
	 * @param multiPart
	 * @return
	 */
	private String generateFileName(MultipartFile multiPart) {
	    return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}
}
