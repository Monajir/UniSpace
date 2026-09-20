package com.techManiacs.UniSpace.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techManiacs.UniSpace.domain.BookingStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private String id;
    private String room;
    private String date;
    private String time;
    private BookingStatus status;
    private String reason;
}
