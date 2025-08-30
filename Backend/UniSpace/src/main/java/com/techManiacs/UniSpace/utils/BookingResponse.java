package com.techManiacs.UniSpace.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String room;
    private String date;
    private String time;
    private String status;
    private String reason;
}
