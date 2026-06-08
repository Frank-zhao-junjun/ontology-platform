package com.ontology.platform.application.service;

import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.ReviewComment;
import com.ontology.platform.domain.entity.WorkflowStateLog;
import com.ontology.platform.domain.repository.ReviewCommentRepository;
import com.ontology.platform.domain.repository.WorkflowStateLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 建模工作流服务 (US-G05)。
 * 管理状态流转审计日志和审核批注。
 * Demo/测试阶段不强制双人审核。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {
    private final WorkflowStateLogRepository stateLogRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    // ── 状态流转日志 ──

    /** 记录一次状态变更 */
    public WorkflowStateLog recordStateChange(String contextId, String fromState, String toState,
                                              String operatedBy, String comment) {
        WorkflowStateLog log = WorkflowStateLog.record(contextId, fromState, toState, operatedBy, comment);
        stateLogRepository.save(log);
        return log;
    }

    /** 查询限界上下文的状态流转历史（按时间倒序） */
    public List<WorkflowStateLog> getWorkflowLog(String contextId) {
        return stateLogRepository.findByContextId(contextId);
    }

    // ── 审核批注 ──

    /** 创建审核批注 */
    public ReviewComment createReviewComment(String contextId, String targetType, String targetId,
                                             String reviewer, String resolution, String content) {
        ReviewComment comment = ReviewComment.create(contextId, targetType, targetId, reviewer, resolution, content);
        reviewCommentRepository.save(comment);
        return comment;
    }

    /** 查询限界上下文的所有批注 */
    public List<ReviewComment> getReviewComments(String contextId) {
        return reviewCommentRepository.findByContextId(contextId);
    }

    /** 查询特定建模元素的批注 */
    public List<ReviewComment> getReviewCommentsByTarget(String targetType, String targetId) {
        return reviewCommentRepository.findByTarget(targetType, targetId);
    }

    /** 更新批注决议状态 */
    public ReviewComment resolveComment(String commentId, String resolution) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Review comment not found: " + commentId));
        comment.resolve(resolution);
        reviewCommentRepository.save(comment);
        return comment;
    }
}
