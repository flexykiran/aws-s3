package com.kiran.aws.s3.app.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kiran.aws.s3.app.service.AmazonClient;

@RestController
@RequestMapping("/aws/s3/")
public class S3RestController {

	private AmazonClient amazonClient; 
	
	@Autowired
	public S3RestController(AmazonClient amazonClient) {
		this.amazonClient = amazonClient;
	}
	
	@PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
        return this.amazonClient.uploadFile(file);
    }
	
	@GetMapping("/downloadFile")
    public String downloadFile(@RequestParam(value = "fileName") String fileName) throws IOException {
        return this.amazonClient.downloadFile(fileName, "C://Users/flexykiran//Downloads/");
    }
	
	@DeleteMapping("/deleteFile")
    public String deleteFile(@RequestParam(value = "fileName") String fileName) {
        return this.amazonClient.deleteFileFromS3Bucket(fileName);
    }
	
}
