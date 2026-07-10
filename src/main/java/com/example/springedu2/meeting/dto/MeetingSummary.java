package com.example.springedu2.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummary {

    private String title;
    private String content;
    private String writer;
    private String projectId;
}