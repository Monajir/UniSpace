package com.techManiacs.UniSpace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_info" )
public class User {

    @Id
    @JsonProperty("_id")
    @Field("_id")
    private ObjectId id; // Make it ObjectId
    @NonNull
    @Indexed(unique = true)
    private String name;
    @NonNull
    private String password;
    @NonNull
    @Indexed(unique = true)
    private String email;

    private List<String> roles;

    private String program;

    private Integer semester;

    private Integer section;
}