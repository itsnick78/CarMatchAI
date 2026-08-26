package ai.carmatch.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI auto-configures a {@link ChatClient.Builder} once exactly one
 * chat-model starter (spring-ai-starter-model-ollama here) is on the
 * classpath. Building the singleton {@link ChatClient} explicitly, rather
 * than autowiring the builder into every service, keeps the model wiring in
 * one place and mirrors how RestClient/WebClient builders are normally
 * turned into a single shared client.
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
