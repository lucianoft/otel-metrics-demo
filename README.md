# OpenTelemetry + Prometheus + Grafana (somente métricas)

Stack leve para **métricas** — sem logs, sem Loki, sem ELK.

```
demo-app (Spring Boot 21)
    │  OTLP HTTP :4318
    ▼
OpenTelemetry Collector
    │  Prometheus exporter :8889
    ▼
Prometheus :9090
    ▼
Grafana :3000
```

## Pré-requisitos

- Docker + Docker Compose
- Java 21 + Maven (para a app demo)

## 1. Subir o stack

```bash
cd C:\development\java\otel-metrics-demo
docker compose up -d
```

Aguarde ~30s e verifique:

```bash
docker compose ps
```

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | `admin` / `admin` |
| Prometheus | http://localhost:9090 | — |
| OTel Collector OTLP HTTP | http://localhost:4318 | — |

## 2. Rodar a app demo (Spring Boot 21)

```bash
cd demo-app
mvn spring-boot:run
```

Endpoints de teste:

```bash
curl http://localhost:8080/api/ping
curl -X POST http://localhost:8080/api/orders/simulate
curl http://localhost:8080/api/load
```

Repita algumas vezes para gerar tráfego.

## 3. Ver métricas no Grafana

1. Abra http://localhost:3000
2. Login: `admin` / `admin`
3. Menu **Dashboards** → **Demo Metrics - OpenTelemetry**

Ou explore no **Explore** → datasource **Prometheus**:

```promql
rate(otel_demo_orders_created_total[1m])
```

> Os nomes das métricas no Prometheus recebem o prefixo `otel_` pelo exporter do Collector.

## 4. Validar pipeline

**App enviando OTLP:**

```bash
curl -s http://localhost:4318/v1/metrics -o /dev/null -w "%{http_code}\n"
```

**Prometheus coletando do Collector:**

- http://localhost:9090/targets → job `otel-collector` deve estar **UP**

**Métricas no Collector:**

- http://localhost:8889/metrics

## Estrutura

```
otel-metrics-demo/
├── docker-compose.yaml
├── otel-collector/config.yaml
├── prometheus/prometheus.yml
├── grafana/provisioning/
└── demo-app/                 # Spring Boot 21
    ├── pom.xml
    └── src/...
```

## Parar tudo

```bash
docker compose down
# com volumes:
docker compose down -v
```

## Próximos passos (opcional)

- Adicionar alertas no Grafana
- Conectar app real (ex: jretail-loja-back) com `management.otlp.metrics.export.url`
- Manter ELK separado só para logs
