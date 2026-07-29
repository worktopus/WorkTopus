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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        Map<Long, ProjectMember> memberMap =
                projectMemberRepository
                        .findByProject_IdOrderByJoinedAtAsc(projectId)
                        .stream()
                        .filter(member -> member.getUser() != null)
                        .collect(Collectors.toMap(
                                member -> member.getUser().getUserNum(),
                                Function.identity()
                        ));

        return kanbanCardRepository
                .findByProjectIdAndDeletedYnOrderByCreatedAtAsc(projectId, "N")
                .stream()
                .map(card -> KanbanCardResponse.from(
                        card,
                        resolveAssigneeName(card.getAssignee(), memberMap)
                ))
                .toList();
    }

    private String resolveAssigneeName(
            String assignee,
            Map<Long, ProjectMember> memberMap
    ) {
        if (assignee == null || assignee.isBlank()) {
            return null;
        }

        try {
            Long userNum = Long.valueOf(assignee);

            ProjectMember member = memberMap.get(userNum);

            if (member == null || member.getUser() == null) {
                return "알 수 없는 사용자";
            }

            return member.getUser().getName();

        } catch (NumberFormatException e) {
            // 기존 카드에 이름이 저장되어 있는 경우 그대로 표시
            return assignee;
        }
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
    public KanbanCardResponse create(
            Long projectId,
            KanbanCardCreateRequest request
    ) {
        validateAssignee(projectId, request.assignee());

        KanbanCard card = new KanbanCard(
                projectId,
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        KanbanCard savedCard = kanbanCardRepository.save(card);

        return KanbanCardResponse.from(
                savedCard,
                findAssigneeName(projectId, savedCard.getAssignee())
        );
    }

    @Override
    public KanbanCardResponse update(
            Long projectId,
            Long cardId,
            KanbanCardUpdateRequest request
    ) {
        validateAssignee(projectId, request.assignee());

        KanbanCard card = getCard(projectId, cardId);

        card.update(
                request.title(),
                request.assignee(),
                request.dueDate(),
                request.priority(),
                request.description()
        );

        return KanbanCardResponse.from(
                card,
                findAssigneeName(projectId, card.getAssignee())
        );
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

        return KanbanCardResponse.from(card, findAssigneeName(projectId, card.getAssignee())
        );
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



    private void validateAssignee(
            Long projectId,
            String assignee
    ) {
        if (assignee == null || assignee.isBlank()) {
            return;
        }

        Long userNum;

        try {
            userNum = Long.valueOf(assignee);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("담당자 정보가 올바르지 않습니다.");
        }

        boolean projectMember =
                projectMemberRepository
                        .existsByProject_IdAndUser_UserNum(
                                projectId,
                                userNum
                        );

        if (!projectMember) {
            throw new IllegalArgumentException(
                    "프로젝트에 참여하지 않은 사용자는 담당자로 지정할 수 없습니다."
            );
        }
    }

    private String findAssigneeName(
            Long projectId,
            String assignee
    ) {
        if (assignee == null || assignee.isBlank()) {
            return null;
        }

        try {
            Long userNum = Long.valueOf(assignee);

            return projectMemberRepository
                    .findByProject_IdAndUser_UserNum(projectId, userNum)
                    .filter(member -> member.getUser() != null)
                    .map(member -> member.getUser().getName())
                    .orElse("알 수 없는 사용자");

        } catch (NumberFormatException e) {
            return assignee;
        }
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
