package com.javabrains;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javabrains.model.Uploadfile;
import com.javabrains.storage.StorageFileNotFoundException;
import com.javabrains.storage.StorageService;
import com.javabrains.MailClient;

/**
 * @author Anjaiah
 * REST API for the multi part file upload functionality , show all the loaded files, search based on the specific file and delete.
 */
@RestController
@RequestMapping(path="/mutipart")
public class FileUploadRestController {

	// Muti-File StorageService
	private final StorageService storageService;

	private MailClient mailclient;

	@Autowired
	public FileUploadRestController(StorageService storageService, MailClient mailclient) {
		this.storageService = storageService;
		this.mailclient = mailclient;
	}

	/** Get the files based on the filename
	 * @param filename
	 * @return
	 */
	@RequestMapping(path = "/files/{filename:.+}", method = RequestMethod.GET)
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	/**
	 * To File Upload 
	 * @param file
	 * @return
	 */
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public String handleFileUpload(@RequestParam("file") MultipartFile file) {
		storageService.store(file);
		mailclient.prepareAndSend("anjiytwok@gmail.com", "hi");
		return "Sucessfully uploaded the file";
	}

	/**
	 * Get all the files
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(path = "/getallfiles", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<Uploadfile>> getUploadedFiles(Model model) throws IOException {
		List<Uploadfile> uploadfile = (List<Uploadfile>) storageService.listallmetadata();
		return new ResponseEntity<>(uploadfile, HttpStatus.OK);
	}

	/**
	 * File not found response entity
	 * @param exc
	 * @return
	 */
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}