package com.sandeep.repository;

import com.sandeep.document.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<ProfileDocument,String> {

    ProfileDocument findByClerkId(String clerkId);

    Boolean existsByClerkId(String clerkId);

}
