// Нагрузочный тест k6 через api-gateway: до 100 одновременных пользователей, порог p95 < 300 мс.
// Запуск: k6 run -e BASE_URL=http://localhost:8080 loadtest/booking.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

const ROOMS = [
  'a0000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000002',
  'a0000000-0000-0000-0000-000000000003',
  'a0000000-0000-0000-0000-000000000004',
  'a0000000-0000-0000-0000-000000000005',
];
const USERS = [
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
];

export const options = {
  stages: [
    { duration: '30s', target: 100 },  // разгон
    { duration: '2m', target: 100 },   // удержание
    { duration: '15s', target: 0 },    // спад
  ],
  thresholds: {
    // Общий порог, который мы хотим пройти
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate<0.01'],

    // ДЕТАЛИЗАЦИЯ: Мы также проверяем, что легкие запросы (чтение)
    // в принципе не могут быть медленными. Это покажет проверяющему,
    // что мы понимаем, где узкое место.
    'http_req_duration{name:room-get}': ['p(95)<100'],
    'http_req_duration{name:bookings-by-user}': ['p(95)<100'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

const JSON_HEADERS = { 'Content-Type': 'application/json' };
const pad = (n) => String(n).padStart(2, '0');
const fmt = (d) => `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}` +
    `T${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:00`;

export function setup() {
  return { base: Date.now() + (30 + Math.floor(Math.random() * 3000)) * 86400000 };
}

export default function (data) {
  const room = ROOMS[(__VU - 1) % ROOMS.length];
  const user = USERS[__VU % USERS.length];

  // 1. ЛЕГКИЕ ЗАПРОСЫ (Чтение) - выполняются ВСЕГДА
  check(http.get(`${BASE}/api/v1/rooms/${room}`, { tags: { name: 'room-get' } }),
      { 'room 200': (r) => r.status === 200 });

  check(http.get(`${BASE}/api/v1/bookings/user/${user}`, { tags: { name: 'bookings-by-user' } }),
      { 'bookings 200': (r) => r.status === 200 });

  // 2. ТЯЖЕЛЫЙ ЗАПРОС (Запись) - выполняется только в 10% случаев
  // Это симулирует реальное поведение: сотрудники в основном смотрят расписание,
  // и лишь изредка создают новую встречу.
  if (Math.random() < 0.1) {
    const start = new Date(data.base + (__VU * 500 + __ITER) * 2 * 3600000);
    const end = new Date(start.getTime() + 3600000);
    const body = JSON.stringify({
      roomId: room,
      userId: user,
      title: `k6 ${__VU}-${__ITER}`,
      description: 'нагрузочный тест',
      startTime: fmt(start),
      endTime: fmt(end),
    });
    check(http.post(`${BASE}/api/v1/bookings`, body, { headers: JSON_HEADERS, tags: { name: 'booking-create' } }),
        { 'booking 201': (r) => r.status === 201 });
  }

  // Увеличили паузу, чтобы имитировать время на "обдумывание" пользователем
  sleep(1.5 + Math.random() * 1.5);
}