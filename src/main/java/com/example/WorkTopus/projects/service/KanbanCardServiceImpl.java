package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.service.NotificationService;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.projects.dto.request.KanbanCardCreateRequest;
import com.example.WorkTopus.projects.dto.request.KanbanCardStatusUpdateRequest;
import com.example.WorkTopus.projects.dto.request.KanbanCardUpdateRequest;
import com.example.WorkTopus.projects.dto.response.KanbanCardResponse;
import com.example.WorkTopus.projects.entity.KanbanCard;
import com.example.WorkTopus.projects.entity.KanbanStatus;
import com.example.WorkTopus.projects.repository.KanbanCardRepository;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KanbanCardServiceImpl implements KanbanCardService {

    private final KanbanCardRepository kanbanCardRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<KanbanCardResponse> findProjectCards(Long projectId) {
        return kanbanCardRepository
                .findByProjectIdAndDeletedYnOrderByCreatedAtAsc(projectId, "N")
                .stream()
                .map(KanbanCardResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectCards(Long projectId) {
        return kanbanCardRepository.countByProjectIdAndDeletedYn(projectId, "N");
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectCardsByStatus(Long projectId, KanbanStatus status) {
        return kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, status, "N");
    }

    @Override
    public KanbanCardResponse create(Long projectId, KanbanCardCreateRequest request) {
        KanbanCard card = new KanbanCard(
                projectId,
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        return KanbanCardResponse.from(kanbanCardRepository.save(card));
    }

    @Override
    public KanbanCardResponse update(Long projectId, Long cardId, KanbanCardUpdateRequest request) {
        KanbanCard card = getCard(projectId, cardId);
        card.update(
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        return KanbanCardResponse.from(card);
    }

    @Override
    public KanbanCardResponse updateStatus(Long projectId, Long cardId, KanbanCardStatusUpdateRequest request, Long senderUserNum) {
        KanbanCard card = getCard(projectId, cardId);

        KanbanStatus oldStatus = card.getStatus();   // 변경 전 상태
        KanbanStatus newStatus = request.status();  // 새로 바뀔 상태

        card.updateStatus(newStatus);

        // 새로 바뀐 상태가 REVIEW일 때 (또는 기존이 REVIEW가 아니었다가 REVIEW로 바뀌었을 때)
        if (oldStatus != KanbanStatus.REVIEW && newStatus == KanbanStatus.REVIEW) {
            // 아래 검은색으로 떠 있던 메서드를 여기서 호출해 줍니다!
            sendKanbanReviewNotification(projectId, card.getTitle(), senderUserNum);
        }

        return KanbanCardResponse.from(card);
    }

    @Override
    public void delete(Long projectId, Long cardId) {
        KanbanCard card = getCard(projectId, cardId);
        card.delete();
    }

    private KanbanCard getCard(Long projectId, Long cardId) {
        return kanbanCardRepository
                .findByIdAndProjectIdAndDeletedYn(cardId, projectId, "N")
                .orElseThrow(() -> new IllegalArgumentException("칸반 카드를 찾을 수 없습니다."));
    }

    // 프로젝트 멤버들에게 검토 요청 알림을 발송
    private void sendKanbanReviewNotification(Long projectId, String cardTitle, Long senderUserNum) {
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        String message = "[" + cardTitle + "] 카드가 검토(Review) 상태로 전환되었습니다.";
        String url = "/projects/" + projectId + "/boards/kanban";

        for (ProjectMember member : members) {
            // member.getUser()가 null이 아닌지 체크
            if (member.getUser() == null) continue;

            // senderUserNum(현재 로그인한 유저)과 비교 (Null Safe하게 Objects.equals 사용)
            if (java.util.Objects.equals(member.getUser().getUserNum(), senderUserNum)) {
                continue;
            }

            // 기존에 작성되어 잘 작동하던 notificationService 메서드 호출
            notificationService.createNotification(
                    member.getUser(),
                    message,
                    url,
                    NotificationType.KANBAN_REVIEW // 또는 기존에 쓰던 NotificationType
            );
        }
    }

}
