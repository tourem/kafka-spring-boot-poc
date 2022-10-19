package com.larbotech.demo.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebConfig implements WebMvcConfigurer {


	@Value(value = "${spring.kafka.consumer.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Bean
    public Map<String, Object> adminConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return props;
   }

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods(HttpMethod.DELETE.name(),HttpMethod.GET.name(),HttpMethod.PUT.name(),HttpMethod.POST.name());
	}


	@Bean
	public KafkaAdmin kafkaAdmin() {
		return new KafkaAdmin(adminConfigs());
	}

	@Bean
    public AdminClient adminClient() {
       return AdminClient.create(kafkaAdmin().getConfigurationProperties());
    }

}