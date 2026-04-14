package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.RemarqueDTO;
import com.example.pointage_backend.service.RemarqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/remarques")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RemarqueController {

    private final RemarqueService remarqueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RemarqueDTO sendRemarque(@RequestBody RemarqueDTO dto) {
        return remarqueService.sendRemarque(dto);
    }

    @GetMapping("/inbox/{userId}")
    public List<RemarqueDTO> getInbox(@PathVariable Long userId) {
        return remarqueService.getInbox(userId);
    }

    @GetMapping("/sent/{userId}")
    public List<RemarqueDTO> getSent(@PathVariable Long userId) {
        return remarqueService.getSent(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRemarque(@PathVariable Long id) {
        remarqueService.deleteRemarque(id);
    }
}
