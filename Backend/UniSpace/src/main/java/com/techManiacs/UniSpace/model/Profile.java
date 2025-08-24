package com.techManiacs.UniSpace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Profile" )
public class Profile {
    @Id
    private ObjectId _id;
    @Field("user_id") // Mapping for MongoDB
    @JsonProperty("user_id") // Output mapping for frontend
    private ObjectId userId;
    @NonNull
    @Indexed(unique = true)
    private String full_name;
    @NonNull
    @Indexed(unique = true)
    private String email;
    private String program;
    private String semester;
    private String role;
}
