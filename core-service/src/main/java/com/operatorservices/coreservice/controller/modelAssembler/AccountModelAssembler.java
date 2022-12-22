package com.operatorservices.coreservice.controller.modelAssembler;

import com.operatorservices.coreservice.controller.AccountController;
import com.operatorservices.coreservice.dto.AccountDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountModelAssembler
        implements RepresentationModelAssembler<AccountDto, EntityModel<AccountDto>> {

    @NotNull
    @Override
    public EntityModel<AccountDto> toModel(@NotNull AccountDto accountDto) {

        EntityModel<AccountDto> body = EntityModel.of(accountDto);
        body.add(linkTo(methodOn(AccountController.class).getAccountById(accountDto.getId())).withSelfRel());
        body.add(linkTo(methodOn(AccountController.class).getAllAccounts()).withRel(IanaLinkRelations.COLLECTION));
        body.add(linkTo(methodOn(AccountController.class)
                .getPurchasesByAccountId(accountDto.getId())).withRel("purchases"));

        return body;
    }
}
