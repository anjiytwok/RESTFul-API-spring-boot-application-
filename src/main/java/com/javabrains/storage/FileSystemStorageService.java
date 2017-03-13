package com.javabrains.storage;

import com.javabrains.model.Uploadfile;
import com.javabrains.model.UploadfileDao;
import com.mysql.jdbc.log.Log;

import ch.qos.logback.classic.Logger;
import groovy.util.logging.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    
    @Autowired
    private UploadfileDao repo;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
			}
			long fileSize = (long) file.getSize();
			String contentType = file.getContentType();
			System.out.println("rootlocation" + rootLocation);
			Uploadfile uploadfile = new Uploadfile(contentType, file.getOriginalFilename(), fileSize);
			try {
				repo.save(uploadfile);
			} catch (Exception e) {

			}
			Files.copy(file.getInputStream(), rootLocation.resolve(file.getOriginalFilename()));
		} catch (IOException e) {
			throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
		}
	}

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

	@Override
	public List<Uploadfile> listallmetadata() {

	  Iterable<Uploadfile>	iterable = repo.findAll();
	  List<Uploadfile> filesList = new ArrayList<>();
	 // iterable.forEach(target::add);
	  for(Uploadfile listall: iterable){
		  System.out.println("check------------>"+listall);
		  filesList.add(listall);
	  }
	  return filesList; 
	
	}
}