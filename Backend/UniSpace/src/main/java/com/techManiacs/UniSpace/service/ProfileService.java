package com.techManiacs.UniSpace.service;

import com.techManiacs.UniSpace.model.Profile;
import com.techManiacs.UniSpace.repository.ProfileRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileService {

    @Autowired
    private ProfileRepo profileRepo;

    public Profile getProfileByEmail(String email) {
        return profileRepo.findByEmail(email);
    }
}
