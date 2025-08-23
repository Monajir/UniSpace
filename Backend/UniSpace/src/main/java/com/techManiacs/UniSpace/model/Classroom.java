package com.techManiacs.UniSpace.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "classroom" )
public class Classroom {
    @Id
    @JsonProperty("id")
    @Field("id")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId _id;

    private String room_number;

    private String building;

    private String capacity;

    private List<String> equipment;

    @Field("is_available")
    @JsonProperty("is_available")
    private Boolean isAvailable;
}
