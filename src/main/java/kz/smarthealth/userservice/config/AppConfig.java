package kz.smarthealth.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.smarthealth.commonlogic.aop.LoggingAspect;
import kz.smarthealth.commonlogic.filter.AuthenticationFilter;
import kz.smarthealth.commonlogic.exception.GlobalExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({GlobalExceptionHandler.class, LoggingAspect.class})
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }
}
