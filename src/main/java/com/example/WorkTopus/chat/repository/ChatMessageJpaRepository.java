package com.example.WorkTopus.chat.repository;

import com.example.WorkTopus.chat.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageJpaRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    /*
     * 특정 채팅방의 전체 메시지를
     * 오래된 메시지부터 조회합니다.
     *
     * 단체방:
     * project_2_group
     *
     * 개인방:
     * project_2_private_1_4
     */
    List<ChatMessageEntity>
    findByRoomIdOrderByMessageIdAsc(
            String roomId
    );


    /*
     * 특정 채팅방의 가장 마지막 메시지 조회
     *
     * 채팅방 목록에서 마지막 메시지와
     * 마지막 작성 시간을 표시할 때 사용합니다.
     */
    Optional<ChatMessageEntity>
    findTopByRoomIdOrderByMessageIdDesc(
            String roomId
    );


    /*
     * 특정 프로젝트의 모든 채팅 메시지 조회
     *
     * 단체방과 개인방 메시지가 모두 포함됩니다.
     * 이후 AI 회의록 범위 조회 등에 사용할 수 있습니다.
     */
    List<ChatMessageEntity>
    findByProjectIdOrderByMessageIdAsc(
            Long projectId
    );


    /*
     * 특정 채팅방의 메시지 개수 조회
     */
    long countByRoomId(
            String roomId
    );


    /*
     * 특정 채팅방의 모든 메시지 삭제
     *
     * 프로젝트 삭제 또는 테스트 데이터 정리에 사용합니다.
     */
    void deleteByRoomId(
            String roomId
    );


    /*
     * 특정 프로젝트에 속한 모든 채팅 메시지 삭제
     */
    void deleteByProjectId(
            Long projectId
    );
}