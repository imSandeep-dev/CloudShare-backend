package com.sandeep.repository;

import com.sandeep.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepository extends MongoRepository<FileMetadataDocument,String> {

    List<FileMetadataDocument> findByClerkId(String clerkId);

}
