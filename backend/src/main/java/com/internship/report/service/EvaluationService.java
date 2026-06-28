package com.internship.report.service;

import com.internship.report.dto.EvaluationCreateRequest;
import com.internship.report.dto.EvaluationDTO;
import com.internship.report.entity.Evaluation;
import com.internship.report.entity.Internship;
import com.internship.report.entity.User;
import com.internship.report.repository.EvaluationRepository;
import com.internship.report.repository.InternshipRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public EvaluationDTO createEvaluation(Long evaluatorId, EvaluationCreateRequest request) {
        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new RuntimeException("评价者不存在"));
        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new RuntimeException("实习记录不存在"));

        Evaluation evaluation = new Evaluation();
        evaluation.setInternship(internship);
        evaluation.setStudent(internship.getStudent());
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluatorType(Evaluation.EvaluatorType.valueOf(request.getEvaluatorType()));
        evaluation.setWorkPerformance(request.getWorkPerformance());
        evaluation.setLearningAbility(request.getLearningAbility());
        evaluation.setCommunication(request.getCommunication());
        evaluation.setPunctuality(request.getPunctuality());
        evaluation.setOverall(request.getOverall());
        evaluation.setStrengths(request.getStrengths());
        evaluation.setWeaknesses(request.getWeaknesses());
        evaluation.setSuggestions(request.getSuggestions());

        Evaluation saved = evaluationRepository.save(evaluation);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationDTO> getEvaluationsByStudentId(Long studentId) {
        return evaluationRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EvaluationDTO> getEvaluationsByInternshipId(Long internshipId) {
        return evaluationRepository.findByInternshipIdOrderByCreatedAtDesc(internshipId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private EvaluationDTO convertToDTO(Evaluation e) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(e.getId());
        dto.setInternshipId(e.getInternship().getId());
        dto.setStudentName(e.getStudent().getRealName());
        dto.setStudentNo(e.getStudent().getStudentNo());
        dto.setCompany(e.getInternship().getCompanyName());
        dto.setPosition(e.getInternship().getPosition());
        dto.setEvaluatorName(e.getEvaluator().getRealName());
        dto.setEvaluatorType(e.getEvaluatorType().name());
        dto.setWorkPerformance(e.getWorkPerformance());
        dto.setLearningAbility(e.getLearningAbility());
        dto.setCommunication(e.getCommunication());
        dto.setPunctuality(e.getPunctuality());
        dto.setOverall(e.getOverall());
        dto.setAverageScore((e.getWorkPerformance() + e.getLearningAbility() + e.getCommunication() + e.getPunctuality() + e.getOverall()) / 5.0);
        dto.setStrengths(e.getStrengths());
        dto.setWeaknesses(e.getWeaknesses());
        dto.setSuggestions(e.getSuggestions());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
