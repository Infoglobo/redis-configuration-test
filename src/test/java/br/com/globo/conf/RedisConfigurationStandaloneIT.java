package br.com.globo.conf;

import com.palantir.docker.compose.DockerComposeRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

import static br.com.globo.MainApplication.PROFILE_STANDALONE;
import static com.palantir.docker.compose.connection.waiting.HealthChecks.toHaveAllPortsOpen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.redis.core.RedisConnectionUtils.getConnection;
import static org.springframework.data.redis.core.RedisConnectionUtils.releaseConnection;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_STANDALONE)
public class RedisConfigurationStandaloneIT {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
        .file("src/test/resources/docker-compose.yml")
        .waitingForService("redis-standalone-and-cluster", toHaveAllPortsOpen())
        .build();

    @BeforeClass
    public static void beforeAll() {

        System.setProperty("REDIS_URL", "redis://:@localhost:7006/0");
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    public void deveConectarNoRedisStandaloneQuandoDadosConexaoProvidosPeloSistema() {

        RedisConnection connection = null;

        try {
            connection = getConnection(redisConnectionFactory);
            Properties properties = connection.info();

            assertThat(properties.getProperty("redis_version")).startsWith("4");
        } finally {
            releaseConnection(connection, redisConnectionFactory);
        }
    }
}