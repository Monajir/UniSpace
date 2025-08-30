package com.techManiacs.UniSpace.utils;

import com.techManiacs.UniSpace.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PendingResponse {
    private Booking pending;
    private String user_name;
    private String room_number;
}
