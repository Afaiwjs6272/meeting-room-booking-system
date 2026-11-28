// Нагрузочный тест k6 через api-gateway: до 100 одновременных пользователей, порог p95 < 300 мс.
// Запуск: k6 run -e BASE_URL=http://localhost:8080 loadtest/booking.js
// Используются демонстрационные данные из миграции reference-service (V2__demo_data.sql).
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
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate<0.01'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

const JSON_HEADERS = { 'Content-Type': 'application/json' };
const pad = (n) => String(n).padStart(2, '0');
const fmt = (d) => `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}` +
    `T${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:00`;

// Общая для всех виртуальных пользователей «соль» прогона, чтобы слоты не пересекались между запусками.
export function setup() {
  return { base: Date.now() + (30 + Math.floor(Math.random() * 3000)) * 86400000 };
}

export default function (data) {
  const room = ROOMS[(__VU - 1) % ROOMS.length];
  const user = USERS[__VU % USERS.length];

  // чтение через шлюз: комната (reference-service) и бронирования пользователя (booking-service)
  check(http.get(`${BASE}/api/v1/rooms/${room}`, { tags: { name: 'room-get' } }),
      { 'room 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/v1/bookings/user/${user}`, { tags: { name: 'bookings-by-user' } }),
      { 'bookings 200': (r) => r.status === 200 });

  // запись: уникальный двухчасовой слот на каждую пару (VU, итерация) — конфликтов нет
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

  sleep(0.5 + Math.random());
}