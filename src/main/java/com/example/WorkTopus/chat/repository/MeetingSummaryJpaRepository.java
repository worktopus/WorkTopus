package com.example.WorkTopus.chat.repository;

import com.example.WorkTopus.chat.entity.MeetingSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingSummaryJpaRepository
        extends JpaRepository<
        MeetingSummaryEntity,
        Long
        > {


    /*
     * 특정 프로젝트의 회의록을
     * 최신순으로 조회
     *
     * 나중에:
     *
     * 📋 회의록 기록
     *
     * 버튼에서 사용합니다.
     */
    List<MeetingSummaryEntity>
    findByProjectIdOrderByGeneratedAtDesc(
            Long projectId
    );


    /*
     * 특정 프로젝트의
     * 가장 최근 회의록 한 개 조회
     */
    Optional<MeetingSummaryEntity>
    findTopByProjectIdOrderByGeneratedAtDesc(
            Long projectId
    );


    /*
     * 특정 프로젝트에 저장된
     * 회의록 개수
     */
    long countByProjectId(
            Long projectId
    );


    /*
     * 프로젝트 삭제 시
     * 해당 프로젝트의 AI 회의록 삭제용
     *
     * 실제 프로젝트 기능 병합 후 사용할 수 있습니다.
     */
    void deleteByProjectId(
            Long projectId
    );
}