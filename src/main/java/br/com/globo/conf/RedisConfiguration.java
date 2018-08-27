package br.com.globo.conf;

import br.com.globo.conf.excep.RedisConfigurationHostNotAvailableException;
import br.com.globo.conf.excep.RedisConfigurationPortNotAvailableException;
import br.com.globo.conf.excep.RedisConfigurationCoudlNotExtractConnectionDetailsException;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static br.com.globo.MainApplication.PROFILE_CLUSTER;
import static br.com.globo.MainApplication.PROFILE_STANDALONE;
import static br.com.globo.support.Matchers.matchGroupsAsList;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

@Configuration
public class RedisConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfiguration.class);

    private final String PATTERN_REGEX = "^([a-z]+:\\/\\/([a-zA-Z0-9-_.]+)?:([a-zA-Z0-9-_.]+)?)?@?([a-zA-Z0-9].+):([0-9]{4,5})";

    /**
     * The most commmon usage is <strong>{@code @Value("#{environment.REDIS_URL}")}</strong>. <i>System properties</i> was for test purpose only.
     */
    @Value("#{systemProperties['REDIS_URL']}")
    private String redisUrl;

    @Bean
    @Primary
    @Profile(PROFILE_CLUSTER)
    public RedisClusterConfiguration redisClusterConfiguration() {

        RedisClusterConfiguration configuration = new RedisClusterConfiguration();

        String[] nodes = redisUrl.split(",");

        Arrays.stream(nodes).collect(toList()).stream()
            .map(v -> matchGroupsAsList(v, PATTERN_REGEX))
            .map(optionalList -> optionalList.orElseThrow(RedisConfigurationCoudlNotExtractConnectionDetailsException::new))
            .map(params -> mapToConnectionDetails(params))
            .peek(connectionDetails -> connectionDetails.getPassword().ifPresent(p -> configuration.setPassword(RedisPassword.of(p))))
            .map(connectionDetails -> new RedisNode(connectionDetails.getHost(), connectionDetails.getPort()))
            .peek(redisNode -> LOGGER.info("Redis node: {}", redisNode))
            .forEach(configuration::addClusterNode);

        LOGGER.info("Number nodes: {}", configuration.getClusterNodes().size());

        return configuration;
    }

    @Bean
    @Primary
    @Profile(PROFILE_STANDALONE)
    public LettuceConnectionFactory lettuceConnectionFactory() {

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        Optional<List<Optional<String>>> optionalList = matchGroupsAsList(redisUrl, PATTERN_REGEX);
        List<Optional<String>> params = optionalList.orElseThrow(RedisConfigurationCoudlNotExtractConnectionDetailsException::new);

        ConnectionDetails connectionDetails = mapToConnectionDetails(params);
        RedisNode redisNode = new RedisNode(connectionDetails.getHost(), connectionDetails.getPort());

        LOGGER.info("Redis node: {}", redisNode);

        configuration.setHostName(redisNode.getHost());
        configuration.setPort(redisNode.getPort());
        connectionDetails.getPassword().ifPresent(p -> configuration.setPassword(RedisPassword.of(p)));

        return new LettuceConnectionFactory(configuration);
    }

    private ConnectionDetails mapToConnectionDetails(List<Optional<String>> params) {

        int INDEX_PORT = params.size() - 1;
        int INDEX_HOST = params.size() - 2;
        int INDEX_PASSWORD = params.size() - 3;
        int INDEX_USER = params.size() - 4;

        return ConnectionDetails.builder()
            .user(params.get(INDEX_USER))
            .password(params.get(INDEX_PASSWORD))
            .host(params.get(INDEX_HOST).orElseThrow(RedisConfigurationHostNotAvailableException::new))
            .port(parseInt(params.get(INDEX_PORT).orElseThrow(RedisConfigurationPortNotAvailableException::new)))
            .build();
    }

    @Data
    @Builder
    private static class ConnectionDetails {

        private Optional<String> user;
        private Optional<String> password;
        private String host;
        private Integer port;
    }
}