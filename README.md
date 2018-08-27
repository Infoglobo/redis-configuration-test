# REDIS-CONFIGURATION-TEST

Saiba como configurar Redis na sua aplicação Spring Boot tanto para `standalone` como `cluster` customizando a leitura de uma variável de ambiente fornecida por um provider [PaaS](https://en.wikipedia.org/wiki/Platform_as_a_service), por exemplo [Heroku](https://www.heroku.com/), [Tsuru](https://tsuru.io/), [Azure](https://azure.microsoft.com/) e outros! O projeto inclui:

- Testes unitário com [PowerMock](https://github.com/powermock/powermock)
- Teste de integração conectando no Redis Standalone e Cluster (6 nós) com auxílio do [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule)

Veja mais detalhes na postagem abaixo:

- LINK

## Testando o projeto

Rode os testes unitários:

    mvn clean test

Incluindo teste de integração:

    mvn -Dskip.surefire.tests=true clean verify