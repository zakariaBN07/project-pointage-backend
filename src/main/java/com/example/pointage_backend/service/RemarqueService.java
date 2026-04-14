package com.example.pointage_backend.service;

import com.example.pointage_backend.dto.RemarqueDTO;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.model.Remarque;
import com.example.pointage_backend.repository.GestionnaireRepository;
import com.example.pointage_backend.repository.RemarqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RemarqueService {

    private final RemarqueRepository remarqueRepository;
    private final GestionnaireRepository gestionnaireRepository;

    public RemarqueDTO sendRemarque(RemarqueDTO dto) {
        Gestionnaire sender = gestionnaireRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        String receiverName = null;
        if (dto.getReceiverId() != null) {
            Gestionnaire receiver = gestionnaireRepository.findById(dto.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
            receiverName = receiver.getName();
        }

        Remarque remarque = Remarque.builder()
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderRole(sender.getRole())
                .receiverRole(dto.getReceiverRole())
                .receiverId(dto.getReceiverId())
                .receiverName(receiverName)
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        Remarque saved = remarqueRepository.save(remarque);
        return mapToDTO(saved);
    }

    public List<RemarqueDTO> getInbox(Long userId) {
        Gestionnaire user = gestionnaireRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get remarks aimed at this user specifically OR their role generically (receiverId = null)
        List<Remarque> remarks = remarqueRepository.findAllByOrderByTimestampDesc();
        return remarks.stream()
                .filter(r -> (r.getReceiverId() != null && r.getReceiverId().equals(userId))
                        || (r.getReceiverId() == null && matchesRole(r.getReceiverRole(), user.getRole())))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesRole(String expected, String actual) {
        if (expected == null || actual == null) return false;
        String e = expected.toLowerCase();
        String a = actual.toLowerCase();
        if (e.contains("admin") && a.contains("admin")) return true;
        if (e.contains("charge") && a.contains("charge")) return true;
        if (e.contains("ingenieur") && (a.contains("ingenieur") || a.contains("ingénieur"))) return true;
        return e.equals(a);
    }

    public List<RemarqueDTO> getSent(Long userId) {
        return remarqueRepository.findBySenderIdOrderByTimestampDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteRemarque(Long id) {
        remarqueRepository.deleteById(id);
    }

    private RemarqueDTO mapToDTO(Remarque remarque) {
        return RemarqueDTO.builder()
                .id(remarque.getId())
                .senderId(remarque.getSenderId())
                .senderName(remarque.getSenderName())
                .senderRole(remarque.getSenderRole())
                .receiverRole(remarque.getReceiverRole())
                .receiverId(remarque.getReceiverId())
                .receiverName(remarque.getReceiverName())
                .content(remarque.getContent())
                .timestamp(remarque.getTimestamp())
                .build();
    }
}
