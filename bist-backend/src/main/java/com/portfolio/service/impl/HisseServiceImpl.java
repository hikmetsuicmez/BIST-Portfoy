package com.portfolio.service.impl;

import com.portfolio.dto.request.HisseRequest;
import com.portfolio.dto.response.HisseDto;
import com.portfolio.dto.response.KapanisFiyatDto;
import com.portfolio.entity.Hisse;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.HisseRepository;
import com.portfolio.repository.KapanisFiyatRepository;
import com.portfolio.service.HisseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HisseServiceImpl implements HisseService {

    private final HisseRepository hisseRepository;
    private final KapanisFiyatRepository kapanisFiyatRepository;

    @Override
    public List<HisseDto> tumHisseleriGetir() {
        return hisseRepository.findByAktifTrue()
                .stream()
                .map(HisseDto::from)
                .toList();
    }

    @Override
    public HisseDto sembolIleGetir(String sembol) {
        return hisseRepository.findBySembol(sembol.toUpperCase())
                .map(HisseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));
    }

    @Override
    @Transactional
    public HisseDto yeniHisseEkle(HisseRequest request) {
        String sembol = request.sembol().toUpperCase();
        if (hisseRepository.existsBySembol(sembol)) {
            throw new BusinessException("Bu sembol zaten mevcut: " + sembol);
        }

        Hisse hisse = Hisse.builder()
                .sembol(sembol)
                .sirketAdi(request.sirketAdi())
                .sektor(request.sektor())
                .piyasa(request.piyasa() != null ? request.piyasa() : "BIST")
                .aktif(true)
                .build();

        Hisse kaydedilen = hisseRepository.save(hisse);
        log.debug("Yeni hisse eklendi: {}", sembol);
        return HisseDto.from(kaydedilen);
    }

    @Override
    public List<KapanisFiyatDto> fiyatGecmisiniGetir(String sembol, int gunSayisi) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));

        return kapanisFiyatRepository
                .findByHisseIdOrderByTarihDesc(hisse.getId(), PageRequest.of(0, gunSayisi))
                .stream()
                .map(KapanisFiyatDto::from)
                .toList();
    }
}
