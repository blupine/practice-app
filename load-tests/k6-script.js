import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration via environment variables
// Default to the user's provided ingress URL if not set
const BASE_URL = __ENV.TARGET_URL || 'https://api.blupine.freeddns.org/app-cache-1';
const VUS = __ENV.VUS || 10;
const DURATION = __ENV.DURATION || '30s';

export const options = {
    stages: [
        { duration: '10s', target: VUS }, // Ramp up fast
        { duration: DURATION, target: VUS }, // Stay at peak
        { duration: '10s', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    },
};

export default function () {
    // Randomize ID (1-10) to simulate cache hits/misses
    const id = Math.floor(Math.random() * 10) + 1;

    // Note: endpoints are /api/cache/mono/{id}
    // The BASE_URL should include the info like https://domain.com/app-name

    const params = {
        headers: {},
    };

    if (__ENV.HOST_HEADER) {
        params.headers['Host'] = __ENV.HOST_HEADER;
    }

    const res = http.get(`${BASE_URL}/api/cache/mono/${id}`, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'verify body': (r) => r.body && r.body.includes('Result for'),
    });

    sleep(0.1); // Small think time
}
