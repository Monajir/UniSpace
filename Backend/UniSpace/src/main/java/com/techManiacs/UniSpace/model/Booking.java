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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "booking" )
public class Booking {

    @Id
    @Field("id")
    @JsonProperty("id")
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId _id;

    @Field("classroom_id")
    @JsonProperty("classroom_id")
    private ObjectId classroomId;

    @Field("user_id")
    @JsonProperty("user_id")
    private ObjectId userId;

    @Field("faculty_email")
    @JsonProperty("faculty_email")
    private String facultyEmail;

    @Field("faculty_name")
    @JsonProperty("faculty_name")
    private String facultyName;

    private String reason;

    @Field("course_code")
    @JsonProperty("course_code")
    private String courseCode;

    private String day;

    @Field("booking_date")
    @JsonProperty("booking_date")
    private String bookingDate;

    @Field("start_time")
    @JsonProperty("start_time")
    private String startTime;

    @Field("end_time")
    @JsonProperty("end_time")
    private String endTime;

    private String status;

    @Field("created_at")
    @JsonProperty("created_at")
    private String createdAt;

    @Field("approved_at")
    @JsonProperty("approved_at")
    private String approvedAt;
}
