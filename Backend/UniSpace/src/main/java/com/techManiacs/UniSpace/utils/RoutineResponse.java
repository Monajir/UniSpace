package com.techManiacs.UniSpace.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutineResponse {

    private String day;

    // Static nested class
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Class {
        private String time;
        private String subject;
        private String room;
        private String type;
    }

    private List<Class> classes;
}
