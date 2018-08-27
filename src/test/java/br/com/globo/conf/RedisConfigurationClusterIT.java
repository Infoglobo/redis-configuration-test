package br.com.globo.conf;

import com.palantir.docker.compose.DockerComposeRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static br.com.globo.MainApplication.PROFILE_CLUSTER;
import static com.palantir.docker.compose.connection.waiting.HealthChecks.toHaveAllPortsOpen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.redis.core.RedisConnectionUtils.getConnection;
import static org.springframework.data.redis.core.RedisConnectionUtils.releaseConnection;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_CLUSTER)
public class RedisConfigurationClusterIT {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
        .file("src/test/resources/docker-compose.yml")
        .waitingForService("redis-standalone-and-cluster", toHaveAllPortsOpen())
        .build();

    @BeforeClass
    public static void beforeAll() {

        System.setProperty("REDIS_URL", "cluster://:@localhost:7000,localhost:7001,localhost:7002,localhost:7003,localhost:7004,localhost:7005/0");
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    public void deveConectarNoRedisClusterQuandoDadosConexaoProvidosPeloSistema() throws InterruptedException {

        RedisConnection connection = null;

        try {
            connection = getConnection(redisConnectionFactory);

            // Connection may be refused
            TimeUnit.SECONDS.sleep(5);

            assertThat(connection).isInstanceOf(RedisClusterConnection.class);
            assertThat(((RedisClusterConnection) connection).clusterGetClusterInfo().getClusterSize()).isGreaterThan(1);
        } finally {
            releaseConnection(connection, redisConnectionFactory);
        }
    }
}