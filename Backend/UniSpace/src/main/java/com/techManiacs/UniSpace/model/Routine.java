package com.techManiacs.UniSpace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "routine" )
public class Routine {

    @Id
    @Field("id")
    @JsonProperty("id")
    private ObjectId _id;

    @Field("classroom_id")
    @JsonProperty("classroom_id")
    private ObjectId classroomId;

    @Field("faculty_name")
    @JsonProperty("faculty_name")
    private String facultyName;

    @Field("course_code")
    @JsonProperty("course_code")
    private String courseCode;

    @Field("day")
    @JsonProperty("day")
    private String day;

    @Field("start_time")
    @JsonProperty("start_time")
    private String startTime;

    @Field("end_time")
    @JsonProperty("end_time")
    private String endTime;

    private String program;

    private Integer semester;

    private Integer section;
}
