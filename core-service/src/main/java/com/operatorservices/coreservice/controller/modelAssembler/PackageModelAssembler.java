package com.operatorservices.coreservice.controller.modelAssembler;

import com.operatorservices.coreservice.controller.PackageController;
import com.operatorservices.coreservice.dto.PackageDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PackageModelAssembler implements RepresentationModelAssembler<PackageDto, EntityModel<PackageDto>> {

    @NotNull
    @Override
    public EntityModel<PackageDto> toModel(@NotNull PackageDto packageDto) {

        EntityModel<PackageDto> body = EntityModel.of(packageDto);

        body.add(linkTo(methodOn(PackageController.class).getPackageById(packageDto.getId())).withSelfRel());
        body.add(linkTo(methodOn(PackageController.class).getAllPackages()).withRel(IanaLinkRelations.COLLECTION));
        body.add(linkTo(methodOn(PackageController.class)
                .getAccountsByPackage(packageDto.getId())).withRel("accounts"));

        return body;
    }
}
