package com.epam.rd.autocode.assessment.appliances.controller.api;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/appliances")
@RequiredArgsConstructor
public class ApplianceRestController {

    private final ApplianceService applianceService;

    @GetMapping
    public Page<ApplianceResponseDTO> findAll(@RequestParam(required = false) String q,
                                               @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return applianceService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public ApplianceRequestDTO findById(@PathVariable Long id) {
        return applianceService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody ApplianceRequestDTO dto, UriComponentsBuilder uriBuilder) {
        Long id = applianceService.saveAppliance(dto);
        var location = uriBuilder.path("/api/appliances/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody ApplianceRequestDTO dto) {
        applianceService.updateAppliance(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applianceService.deleteApplianceById(id);
        return ResponseEntity.noContent().build();
    }
}
