package com.example.springedu2.meeting.service;


import com.example.springedu2.meeting.dto.MeetingSummary;
import com.example.springedu2.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository repository;

    public void save(MeetingSummary meeting){
        repository.save(meeting);
    }

    public List<MeetingSummary> getAll(){
        return repository.findAll();
    }

}