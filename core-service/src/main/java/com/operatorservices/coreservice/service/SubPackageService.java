package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.dto.GetAccountsByPackageDto;
import com.operatorservices.coreservice.dto.PackageRequestDto;
import com.operatorservices.coreservice.dto.PackageDto;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Purchase;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.repository.SubPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SubPackageService {

    private final SubPackageRepository packageRepository;
    private final ModelDtoConverter modelDtoConverter;

    public SubPackageService(SubPackageRepository packageRepository, ModelDtoConverter modelDtoConverter) {
        this.packageRepository = packageRepository;
        this.modelDtoConverter = modelDtoConverter;
    }

    public SubPackage returnSubPackageById(Long subPackageId) {
        return packageRepository.findById(subPackageId)
                .orElseThrow(
                        () -> new EntryNotFoundException("No package found with this id: " + subPackageId));
    }

    public PackageDto getPackageById(Long packageId) {
        return modelDtoConverter.packageToPackageDto(returnSubPackageById(packageId));
    }

    public List<PackageDto> getAllPackages() {
        return packageRepository.findAll()
                .stream()
                .map(modelDtoConverter::packageToPackageDto)
                .collect(Collectors.toList());
    }

    public List<GetAccountsByPackageDto> getAccountsByPackage(Long packageId) {
        return returnSubPackageById(packageId).getPurchases()
                .stream()
                .map(Purchase::getAccount).filter(Objects::nonNull)
                .map(modelDtoConverter::accountToGetAccountsByPackageDto)
                .collect(Collectors.toList());
    }

    public PackageDto createPackage(PackageRequestDto packageRequestDto) {

        SubPackage subPackage =new SubPackage(
                packageRequestDto.getName(),
                packageRequestDto.getPackageType(),
                packageRequestDto.getDuration(),
                packageRequestDto.getPurchasable());

        return modelDtoConverter.packageToPackageDto(packageRepository.save(subPackage));
    }

    public PackageDto updatePackage(PackageRequestDto packageRequestDto, Long packageId) {

        return packageRepository.findById(packageId)
                .map(subPackage -> {
                    subPackage.setName(packageRequestDto.getName());
                    subPackage.setPackageType(packageRequestDto.getPackageType());
                    subPackage.setDuration(packageRequestDto.getDuration());
                    subPackage.setPurchasable(packageRequestDto.getPurchasable());
                    return modelDtoConverter.packageToPackageDto(packageRepository.save(subPackage));
                })
                .orElseThrow(
                        () -> new EntryNotFoundException("No package found with this id: " + packageId)
                );
    }

    public void syncPackage(Purchase purchase){
        packageRepository.findById(Objects.requireNonNull(Objects.requireNonNull(purchase.getSubPackage()).getId()))
                .map(subPackage -> {
                    subPackage.getPurchases().remove(purchase);
                    return packageRepository.save(subPackage);
                });
    }

    /*
    public void deletePackage(Long packageId) {
        if (packageRepository.existsById(packageId)) {
            packageRepository.deleteById(packageId);
        } else {
            throw new EntryNotFoundException("No package found with this id: " + packageId);
        }
    }
     */
}
