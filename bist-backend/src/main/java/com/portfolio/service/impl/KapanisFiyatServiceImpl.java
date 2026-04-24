package com.portfolio.service.impl;

import com.portfolio.dto.request.KapanisFiyatRequest;
import com.portfolio.dto.response.KapanisFiyatDto;
import com.portfolio.entity.Hisse;
import com.portfolio.entity.KapanisFiyat;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.HisseRepository;
import com.portfolio.repository.KapanisFiyatRepository;
import com.portfolio.service.KapanisFiyatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KapanisFiyatServiceImpl implements KapanisFiyatService {

    private final KapanisFiyatRepository kapanisFiyatRepository;
    private final HisseRepository hisseRepository;

    @Override
    @Transactional
    public KapanisFiyatDto tekFiyatKaydet(KapanisFiyatRequest request) {
        Hisse hisse = hisseRepository.findBySembol(request.sembol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.sembol()));

        if (kapanisFiyatRepository.findByHisseIdAndTarih(hisse.getId(), request.tarih()).isPresent()) {
            throw new BusinessException(request.sembol() + " için " + request.tarih() + " tarihli fiyat zaten mevcut");
        }

        KapanisFiyat fiyat = buildFiyat(hisse, request);
        return KapanisFiyatDto.from(kapanisFiyatRepository.save(fiyat));
    }

    @Override
    @Transactional
    public List<KapanisFiyatDto> topluFiyatKaydet(List<KapanisFiyatRequest> requests) {
        return requests.stream()
                .map(this::tekFiyatKaydet)
                .toList();
    }

    @Override
    public List<KapanisFiyatDto> tariheGoreFiyatlariGetir(LocalDate tarih) {
        return kapanisFiyatRepository.findByTarihOrderByHisseAsc(tarih)
                .stream()
                .map(KapanisFiyatDto::from)
                .toList();
    }

    @Override
    @Transactional
    public KapanisFiyatDto fiyatGuncelle(Long id, KapanisFiyatRequest request) {
        KapanisFiyat mevcutFiyat = kapanisFiyatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fiyat kaydı bulunamadı: " + id));

        mevcutFiyat.setKapanisFiyat(request.kapanisFiyat());
        mevcutFiyat.setAcilisFiyat(request.acilisFiyat());
        mevcutFiyat.setYuksekFiyat(request.yuksekFiyat());
        mevcutFiyat.setDusukFiyat(request.dusukFiyat());
        mevcutFiyat.setHacim(request.hacim());

        return KapanisFiyatDto.from(kapanisFiyatRepository.save(mevcutFiyat));
    }

    @Override
    public KapanisFiyatDto sembolVeTariheGoreFiyatGetir(String sembol, LocalDate tarih) {
        Hisse hisse = hisseRepository.findBySembol(sembol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + sembol));
        List<KapanisFiyat> oncekiFiyatlar = kapanisFiyatRepository
                .findPreviousByHisseId(hisse.getId(), tarih, PageRequest.of(0, 1));
        if (oncekiFiyatlar.isEmpty()) {
            throw new ResourceNotFoundException(
                    sembol.toUpperCase() + " için " + tarih + " tarihinden önce fiyat kaydı bulunamadı");
        }
        return KapanisFiyatDto.from(oncekiFiyatlar.get(0));
    }

    private KapanisFiyat buildFiyat(Hisse hisse, KapanisFiyatRequest request) {
        return KapanisFiyat.builder()
                .hisse(hisse)
                .tarih(request.tarih())
                .kapanisFiyat(request.kapanisFiyat())
                .acilisFiyat(request.acilisFiyat())
                .yuksekFiyat(request.yuksekFiyat())
                .dusukFiyat(request.dusukFiyat())
                .hacim(request.hacim())
                .build();
    }
}
