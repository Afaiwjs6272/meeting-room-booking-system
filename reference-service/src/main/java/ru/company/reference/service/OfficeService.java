package ru.company.reference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.common.exception.BusinessException;
import ru.company.reference.dto.CreateOfficeRequest;
import ru.company.reference.dto.OfficeResponse;
import ru.company.reference.dto.UpdateOfficeRequest;
import ru.company.reference.entity.Office;
import ru.company.reference.exception.ResourceNotFoundException;
import ru.company.reference.mapper.OfficeMapper;
import ru.company.reference.repository.OfficeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper officeMapper;

    public OfficeResponse getOffice(String id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", id));
        return officeMapper.toResponse(office);
    }

    public OfficeResponse getOfficeByCode(String code) {
        Office office = officeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "code", code));
        return officeMapper.toResponse(office);
    }

    public List<OfficeResponse> getAllOffices() {
        return officeRepository.findByActiveTrue().stream()
                .map(officeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OfficeResponse> getOfficesByCity(String city) {
        return officeRepository.findByCity(city).stream()
                .map(officeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "offices", allEntries = true)
    public OfficeResponse createOffice(CreateOfficeRequest request) {
        log.info("Creating new office with code: {}", request.getCode());

        if (officeRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("Office with code " + request.getCode() + " already exists");
        }

        Office office = officeMapper.toEntity(request);
        Office saved = officeRepository.save(office);
        log.info("Office created with id: {}", saved.getId());

        return officeMapper.toResponse(saved);
    }

    @Transactional
    public OfficeResponse updateOffice(String id, UpdateOfficeRequest request) {
        log.info("Updating office with id: {}", id);

        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", id));

        officeMapper.updateEntity(office, request);
        Office updated = officeRepository.save(office);

        return officeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteOffice(String id) {
        log.warn("Deleting office with id: {}", id);
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", id));
        officeRepository.delete(office);
    }
}