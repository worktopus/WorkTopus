package com.example.WorkTopus.meeting.repository;

import com.example.WorkTopus.meeting.dto.MeetingSummary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MeetingRepository {

    private final List<MeetingSummary> meetings = new ArrayList<>();

    public void save(MeetingSummary meeting) {
        meetings.add(meeting);

    }

    public List<MeetingSummary> findAll() {
        return meetings;
    }
}
