package br.com.globo.conf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.reflect.Whitebox.setInternalState;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.Silent.class)
@PrepareForTest(RedisConfiguration.class)
public class RedisConfigurationTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private RedisConfiguration redisConfiguration;

    @Before
    public void init() {

        initMocks(this);
    }

    @Test
    public void deveConfigurarFabricaQuandoPropriedadeNaoContemUsuario() {

        String password = "honest-password";
        String host = "localhost";
        Integer port = 7006;

        setInternalState(redisConfiguration, "redisUrl", "redis://:" + password + "@" + host + ":" + port);

        RedisStandaloneConfiguration standaloneConfiguration = redisConfiguration.lettuceConnectionFactory().getStandaloneConfiguration();

        evaluateStandaloneConfiguration(standaloneConfiguration, RedisPassword.of(password), host, port);
    }

    @Test
    public void deveConfigurarFabricaQuandoPropriedadeNaoContemUsuarioNemSenha() {

        String host = "jafar-iago";
        Integer port = 65200;

        setInternalState(redisConfiguration, "redisUrl", "redis://:@" + host + ":" + port);

        RedisStandaloneConfiguration standaloneConfiguration = redisConfiguration.lettuceConnectionFactory().getStandaloneConfiguration();

        evaluateStandaloneConfiguration(standaloneConfiguration, RedisPassword.none(), host, port);
    }

    @Test
    public void deveConfigurarFabricaQuandoPropriedadeContemTudo() {

        String user = "jasmine";
        String password = "linda-password-larkin";
        String host = "agrabah";
        Integer port = 65530;

        setInternalState(redisConfiguration, "redisUrl", "redis://" + user + ":" + password + "@" + host + ":" + port);

        RedisStandaloneConfiguration standaloneConfiguration = redisConfiguration.lettuceConnectionFactory().getStandaloneConfiguration();

        evaluateStandaloneConfiguration(standaloneConfiguration, RedisPassword.of(password), host, port);
    }

    @Test
    public void deveConfigurarFabricaQuandoPropriedadeEstiverInapropriada() {

        String password = "honest-salt-password";
        String host = "lambesal";
        Integer port = 6379;

        setInternalState(redisConfiguration, "redisUrl", "redis://:" + password + "@" + host + ":" + port + "/0");

        RedisStandaloneConfiguration standaloneConfiguration = redisConfiguration.lettuceConnectionFactory().getStandaloneConfiguration();

        evaluateStandaloneConfiguration(standaloneConfiguration, RedisPassword.of(password), host, port);
    }

    @Test
    public void deveConfigurarClusterQuandoPropriedadesNaoContemUsuario() {

        setInternalState(redisConfiguration, "redisUrl", "cluster://:@localhost:6379,localhost:6380,localhost:6381,localhost:6382,localhost:6383,localhost:6384");

        RedisClusterConfiguration clusterConfiguration = redisConfiguration.redisClusterConfiguration();
    }

    @Test
    public void deveConfigurarClusterQuandoPropriedadesTemTudo() {

        setInternalState(redisConfiguration, "redisUrl", "cluster://iago:cave-of-wonders@agrabah:6379,agrabah:6380,agrabah:6381,agrabah:6382,agrabah:6383,agrabah:6384");

        RedisClusterConfiguration clusterConfiguration = redisConfiguration.redisClusterConfiguration();

        evaluateCluster(clusterConfiguration, RedisPassword.of("cave-of-wonders"), 6, "agrabah", 6379, 6384);
    }

    @Test
    public void deveConfigurarClusterQuandoPropriedadeEstiverInapropriada() {

        setInternalState(redisConfiguration, "redisUrl", "cluster://lambe:sal@saleiro:6000,saleiro:6001,saleiro:6002,saleiro:6003,saleiro:6004,saleiro:6005,saleiro:6006/budega0");

        RedisClusterConfiguration clusterConfiguration = redisConfiguration.redisClusterConfiguration();

        evaluateCluster(clusterConfiguration, RedisPassword.of("sal"), 7, "saleiro", 6000, 6006);
    }

    private void evaluateCluster(RedisClusterConfiguration configuration, RedisPassword password, Integer size, String host, Integer portStart, Integer portEnd) {

        assertThat(configuration.getPassword()).isEqualTo(password);
        assertThat(configuration.getClusterNodes().size()).isEqualTo(size);
        assertThat(configuration.getClusterNodes()).allSatisfy(r -> {
            assertThat(r.getHost()).isEqualTo(host);
            assertThat(r.getPort()).isBetween(portStart, portEnd);
        });
    }

    private void evaluateStandaloneConfiguration(RedisStandaloneConfiguration configuration, RedisPassword password, String host, Integer port) {

        assertThat(configuration.getPassword()).isEqualTo(password);
        assertThat(configuration.getHostName()).isEqualTo(host);
        assertThat(configuration.getPort()).isEqualTo(port);
    }
}