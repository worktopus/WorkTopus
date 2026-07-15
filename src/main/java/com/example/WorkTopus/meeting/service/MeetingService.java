package com.example.WorkTopus.meeting.service;


import com.example.WorkTopus.meeting.dto.MeetingSummary;
import com.example.WorkTopus.meeting.repository.MeetingRepository;
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