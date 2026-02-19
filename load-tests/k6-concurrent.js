import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration via environment variables
const HOST_HEADER = __ENV.HOST_HEADER || 'api.blupine.freeddns.org';
const LB_IP = __ENV.LB_IP;
const KEY_RANGE = __ENV.KEY_RANGE || 10; // Default 10 means 100% hit rate (Cache Size 100)

if (!LB_IP) {
    throw new Error('Please provide LB_IP environment variable (e.g. -e LB_IP=192.168.0.50)');
}

const BASE_URL = `http://${LB_IP}`;

export const options = {
    scenarios: {
        caffeine_test: {
            executor: 'constant-vus',
            vus: 50,
            duration: '2m',
            exec: 'caffeineTest',
            tags: { app: 'webflux-cache' }, // Tag metrics for filtering
        },
        ehcache_test: {
            executor: 'constant-vus',
            vus: 50,
            duration: '2m',
            exec: 'ehcacheTest',
            tags: { app: 'webflux-ehcache' }, // Tag metrics for filtering
        },
    },
    thresholds: {
        'http_req_duration{app:webflux-cache}': ['p(95)<100'],
        'http_req_duration{app:webflux-ehcache}': ['p(95)<2000'], // Expecting it to be bad
    },
};

const params = {
    headers: { 'Host': HOST_HEADER },
    tags: { name: 'get_mono' },
};

export function caffeineTest() {
    const id = Math.floor(Math.random() * KEY_RANGE) + 1;
    const res = http.get(`${BASE_URL}/app-cache-1/api/cache/mono/${id}`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(0.1);
}

export function ehcacheTest() {
    const id = Math.floor(Math.random() * KEY_RANGE) + 1;
    const res = http.get(`${BASE_URL}/app-ehcache/api/cache/mono/${id}`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(0.1);
}
