package com.example.WorkTopus.chat.repository;

import com.example.WorkTopus.chat.entity.ChatReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatReadJpaRepository
        extends JpaRepository<ChatReadEntity, Long> {

    /*
     * 특정 사용자의 특정 채팅방 읽음 정보 조회
     *
     * 예:
     * roomId = project_2_group
     * userNum = 1
     */
    Optional<ChatReadEntity>
    findByRoomIdAndUserNum(
            String roomId,
            Long userNum
    );


    /*
     * 사용자가 해당 채팅방의 읽음 정보를
     * 이미 가지고 있는지 확인
     */
    boolean existsByRoomIdAndUserNum(
            String roomId,
            Long userNum
    );


    /*
     * 특정 사용자의 모든 채팅방 읽음 정보 조회
     */
    List<ChatReadEntity>
    findByUserNum(
            Long userNum
    );


    /*
     * 특정 프로젝트의 읽음 정보 조회
     */
    List<ChatReadEntity>
    findByProjectId(
            Long projectId
    );


    /*
     * 특정 채팅방의 모든 참여자 읽음 정보 조회
     */
    List<ChatReadEntity>
    findByRoomId(
            String roomId
    );


    /*
     * 프로젝트 삭제 시 해당 프로젝트의
     * 읽음 정보 전체 삭제
     */
    void deleteByProjectId(
            Long projectId
    );


    /*
     * 채팅방 삭제 시 해당 방의
     * 읽음 정보 전체 삭제
     */
    void deleteByRoomId(
            String roomId
    );
}