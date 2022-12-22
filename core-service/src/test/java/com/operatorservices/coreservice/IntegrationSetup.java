package com.operatorservices.coreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public class IntegrationSetup extends TestSupport {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ModelDtoConverter modelDtoConverter;

    @Autowired
    public  ObjectMapper objectMapper;

}
