package kz.smarthealth.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.smarthealth.userservice.aop.Log;
import kz.smarthealth.userservice.model.dto.PatientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service used to send patient details to kafka broker
 *
 * Created by Samat Abibulla on 2023-06-07
 */
@Service
@RequiredArgsConstructor
public class PatientKafkaProducerService {

//    @Value("${kafka.topic.new-patients}")
//    private final String topicName;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Log
    public void sendMessage(PatientDTO patientDTO) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(patientDTO);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("user-service-new-patients", message);

        future.whenComplete((key, value) -> {
            System.out.println("Patient: " + key + " - " + value);
        });
    }
}
