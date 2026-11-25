#!/usr/bin/env bash
# Сбор измерений для таблиц отчёта. Результаты пишутся в каталог reports/.
# Использование: scripts/measure.sh <images|tests|trivy|bench|k6|all>
# Требуются: docker, mvn (или ./mvnw), python3; для trivy и k6 — соответствующие утилиты.
set -euo pipefail
cd "$(dirname "$0")/.."
OUT=reports
mkdir -p "$OUT"
SERVICES=(reference-service:8081 booking-service:8082 notification-service:8083 integration-service:8084 api-gateway:8080)
MVN=${MVN:-mvn}

images() {
  echo "service,image_size_mb,build_no_cache_s,build_cached_s" > "$OUT/images.csv"
  for item in "${SERVICES[@]}"; do
    svc=${item%%:*}; port=${item##*:}
    args=(--build-arg SERVICE="$svc" --build-arg PORT="$port" -t "mrb-$svc:local" .)
    t0=$(date +%s); docker build --no-cache "${args[@]}" > /dev/null; t1=$(date +%s)
    docker build "${args[@]}" > /dev/null; t2=$(date +%s)
    size=$(docker image inspect "mrb-$svc:local" --format '{{.Size}}')
    echo "$svc,$(python3 -c "print(round($size/1048576,1))"),$((t1-t0)),$((t2-t1))" | tee -a "$OUT/images.csv"
  done
}

tests() {
  /usr/bin/env time -f "mvn verify: %e s" $MVN -B verify 2>&1 | tee "$OUT/mvn-verify.log" | tail -n 30
  python3 - <<'PY'
import glob, csv, xml.etree.ElementTree as ET
t = f = e = s = 0
for p in glob.glob('*/target/surefire-reports/TEST-*.xml'):
    r = ET.parse(p).getroot()
    t += int(r.get('tests')); f += int(r.get('failures')); e += int(r.get('errors')); s += int(r.get('skipped'))
print(f"tests={t} failures={f} errors={e} skipped={s}")
for p in sorted(glob.glob('*/target/site/jacoco/jacoco.csv')):
    miss = cov = 0
    for row in csv.DictReader(open(p, encoding='utf-8')):
        miss += int(row['LINE_MISSED']); cov += int(row['LINE_COVERED'])
    if miss + cov:
        print(f"{p.split('/')[0]}: line coverage {100*cov/(miss+cov):.1f}% ({cov}/{miss+cov})")
PY
}

trivy_scan() {
  # Запустите дважды: до и после исправлений; задайте метку: LABEL=before|after
  label=${LABEL:-run}
  for item in "${SERVICES[@]}"; do
    svc=${item%%:*}
    trivy image --quiet --severity HIGH,CRITICAL --ignore-unfixed --format json \
      --output "$OUT/trivy-$svc-$label.json" "mrb-$svc:local"
    python3 - "$OUT/trivy-$svc-$label.json" "$svc" <<'PY'
import json, sys
d = json.load(open(sys.argv[1])); c = {'CRITICAL': 0, 'HIGH': 0}
for r in d.get('Results', []):
    for v in r.get('Vulnerabilities') or []:
        c[v['Severity']] += 1
print(f"{sys.argv[2]}: CRITICAL={c['CRITICAL']} HIGH={c['HIGH']}")
PY
  done
  trivy fs --quiet --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --ignore-unfixed \
    --format table . | tee "$OUT/trivy-fs-${label}.txt" | tail -n 40
}

bench() {
  [ -d docker-bench-security ] || git clone --depth 1 https://github.com/docker/docker-bench-security.git
  (cd docker-bench-security && sudo sh docker-bench-security.sh -c container_images,container_runtime -l "../$OUT/docker-bench-${LABEL:-run}.log")
  for k in PASS WARN INFO NOTE; do echo "$k: $(grep -c "\[$k\]" "$OUT/docker-bench-${LABEL:-run}.log" || true)"; done
}

k6run() {
  k6 run -e BASE_URL="${BASE_URL:-http://localhost:8080}" --summary-export "$OUT/k6-summary.json" loadtest/booking.js | tee "$OUT/k6.txt"
  python3 - <<'PY'
import json
d = json.load(open('reports/k6-summary.json'))['metrics']['http_req_duration']
print({k: round(v, 1) for k, v in d.items() if k in ('med', 'p(90)', 'p(95)', 'avg', 'max')})
PY
}

case "${1:-all}" in
  images) images ;;
  tests) tests ;;
  trivy) trivy_scan ;;
  bench) bench ;;
  k6) k6run ;;
  all) images; tests ;;
  *) echo "Использование: $0 <images|tests|trivy|bench|k6|all>"; exit 1 ;;
esac
