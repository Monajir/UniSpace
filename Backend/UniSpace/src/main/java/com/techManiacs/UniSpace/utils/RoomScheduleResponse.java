package com.techManiacs.UniSpace.utils;

import com.techManiacs.UniSpace.model.Booking;
import com.techManiacs.UniSpace.model.Routine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomScheduleResponse {
    private List<Routine> regular;
    private List<Booking> extras;
}
