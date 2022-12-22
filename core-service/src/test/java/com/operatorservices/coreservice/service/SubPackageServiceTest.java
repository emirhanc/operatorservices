package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.TestSupport;
import com.operatorservices.coreservice.dto.GetAccountsByPackageDto;
import com.operatorservices.coreservice.dto.PackageDto;
import com.operatorservices.coreservice.dto.PackageRequestDto;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Purchase;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.repository.SubPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubPackageServiceTest extends TestSupport {

    private SubPackageRepository packageRepository;
    private ModelDtoConverter modelDtoConverter;
    private SubPackageService packageService;

    @BeforeEach
    void setUp() {
        packageRepository = mock(SubPackageRepository.class);
        modelDtoConverter = mock(ModelDtoConverter.class);
        packageService = new SubPackageService(packageRepository, modelDtoConverter);
    }

    @DisplayName("getPackageById with a Valid Id Test")
    @Test
    void whenGetPackageByIdCalled_withAValidId_itShouldReturnPackageDto() {
        SubPackage subPackage = newSubPackage(1L);
        PackageDto packageDto = newPackageDto(subPackage);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(subPackage));
        when(modelDtoConverter.packageToPackageDto(subPackage)).thenReturn(packageDto);

        PackageDto test = packageService.getPackageById(1L);

        assertEquals(test, packageDto);

        verify(packageRepository).findById(1L);
        verify(modelDtoConverter).packageToPackageDto(subPackage);
    }

    @DisplayName("getPackageById with An Invalid Id Test")
    @Test
    void whenGetPackageByIdCalled_withAnInvalidId_itShouldThrowEntryNotFoundException() {

        when(packageRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class,
                () -> packageService.getPackageById(404L));

        verify(packageRepository).findById(404L);
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("getAllPackages Test")
    @Test
    void getAllPackages() {
        SubPackage subPackage1 = newSubPackage(1L);
        SubPackage subPackage2 = newSubPackage(2L);

        when(packageRepository.findAll()).thenReturn(List.of(subPackage1, subPackage2));
        when(modelDtoConverter.packageToPackageDto(subPackage1)).thenReturn(newPackageDto(subPackage1));
        when(modelDtoConverter.packageToPackageDto(subPackage2)).thenReturn(newPackageDto(subPackage2));

        List<PackageDto> test = packageService.getAllPackages();

        assertEquals(test, List.of(newPackageDto(subPackage1), newPackageDto(subPackage2)));

        verify(packageRepository).findAll();
        verify(modelDtoConverter).packageToPackageDto(subPackage1);
        verify(modelDtoConverter).packageToPackageDto(subPackage2);
    }

    @DisplayName("getAccountsByPackage with Valid Id")
    @Test
    void whenGetAccountsByPackageCalled_withValidId_itShouldReturnListOfAccountDto() {

        Account account1 = newAccount("account1", "customer1", 100L, Set.of());
        Account account2 = newAccount("account2", "customer2", 100L, Set.of());

        Purchase purchase1 = newPurchase(account1, "purchase1", 1L);
        Purchase purchase2 = newPurchase(account2, "purchase2", 1L);

        SubPackage subPackage = newSubPackage(1L, Set.of(purchase1, purchase2));


        when(packageRepository.findById(1L)).thenReturn(Optional.ofNullable(subPackage));

        when(modelDtoConverter.accountToGetAccountsByPackageDto(account1))
                .thenReturn(newGetAccountsByPackageDto(account1));

        when(modelDtoConverter.accountToGetAccountsByPackageDto(account2))
                .thenReturn(newGetAccountsByPackageDto(account2));

        List<GetAccountsByPackageDto> test = packageService.getAccountsByPackage(1L);
        test.sort(Comparator.comparing(GetAccountsByPackageDto::getId));

        assertEquals(test, List.of(
                newGetAccountsByPackageDto(account1),
                newGetAccountsByPackageDto(account2)));

        verify(packageRepository).findById(1L);
        verify(modelDtoConverter).accountToGetAccountsByPackageDto(account1);
        verify(modelDtoConverter).accountToGetAccountsByPackageDto(account2);
    }

    @DisplayName("createPackage Test")
    @Test
    void whenCreatePackageCalled_itShouldReturnPackageDto() {

        SubPackage subPackage = newSubPackage(1L);
        PackageDto packageDto = newPackageDto(subPackage);

        when(packageRepository.save(any(SubPackage.class))).thenReturn(subPackage);
        when(modelDtoConverter.packageToPackageDto(subPackage)).thenReturn(packageDto);

        PackageDto test = packageService.createPackage(newPackageRequestDto(subPackage));

        assertEquals(test, packageDto);

        verify(packageRepository).save(any(SubPackage.class));
        verify(modelDtoConverter).packageToPackageDto(subPackage);
    }

    @DisplayName("updatePackage with Valid Id Test ")
    @Test
    void whenUpdatePackageCalled_withValidId_itShouldReturnPackageDto() {

        SubPackage subPackage = newSubPackage(1L);
        PackageDto packageDto = newPackageDto(subPackage);
        PackageRequestDto packageRequestDto = newPackageRequestDto(subPackage);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(subPackage));
        when(packageRepository.save(any(SubPackage.class))).thenReturn(subPackage);
        when(modelDtoConverter.packageToPackageDto(subPackage)).thenReturn(packageDto);

        PackageDto test = packageService.updatePackage(packageRequestDto, subPackage.getId());

        assertEquals(test, packageDto);

        verify(packageRepository).findById(Objects.requireNonNull(subPackage.getId()));
        verify(packageRepository).save(any(SubPackage.class));
        verify(modelDtoConverter).packageToPackageDto(subPackage);
    }

    @DisplayName("syncPackage Test")
    @Test
    void whenSyncPackageCalled_itShouldUpdateSubPackage() {

        Account account = newAccount("id", "id", 100L, Set.of());
        Purchase purchase = newPurchase(account, "purchaseId", 1L);
        SubPackage subPackage = newSubPackage(1L, new HashSet<>(Collections.singleton(purchase)));

        when(packageRepository.findById(1L)).thenReturn(Optional.of(subPackage));
        when(packageRepository.save(any(SubPackage.class))).thenReturn(subPackage);

        packageService.syncPackage(purchase);

        verify(packageRepository).findById(1L);
        verify(packageRepository).save(any(SubPackage.class));
    }

    /*
    @DisplayName("deletePackage with Valid Id Test")
    @Test
    void whenDeletePackageCalled_withAValidId_itShouldDeletePackage() {
        when(packageRepository.existsById(200L)).thenReturn(true);

        packageService.deletePackage(200L);

        verify(packageRepository).existsById(200L);
        verify(packageRepository).deleteById(200L);
    }


    @DisplayName("deletePackage with Invalid Id Test")
    @Test
    void whenDeletePackageCalled_withAnInvalidId_itShouldThrowEntryNotFoundException() {
        when(packageRepository.existsById(404L)).thenReturn(false);

        assertThrows(EntryNotFoundException.class,
                () -> packageService.deletePackage(404L));

        verify(packageRepository).existsById(404L);
        verify(packageRepository, times(0)).deleteById(404L);
    }
     */
}