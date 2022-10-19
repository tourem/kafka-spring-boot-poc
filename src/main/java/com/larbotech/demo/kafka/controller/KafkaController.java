package com.larbotech.demo.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.larbotech.demo.kafka.producer.KafkaProducer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = "/kafka")
@Api(description = "Set of endpoints for sending Message to Kafka Broker.")
public class KafkaController {

	private final KafkaProducer producer;

	@Autowired
	KafkaController(KafkaProducer producer) {
		this.producer = producer;
	}

	@GetMapping(value = "/publish/{message}")
	@ApiOperation("send {message} to Kakfa Broker} .. "
			+ "***Post successful Response Please check Server logs to validate if its received***")
	public String sendMessageToKafkaTopic(@ApiParam("Message to be Published") @PathVariable("message") String message) {
		this.producer.sendMessage(message);
		return "Message sent! check logs!";
	}
}