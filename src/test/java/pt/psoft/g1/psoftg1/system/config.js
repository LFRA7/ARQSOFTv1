/**
 * k6 Test Configuration
 * Shared configuration for all k6 system tests
 */

export const config = {
    baseUrls: {
        dev: 'http://localhost:8084/api',
        staging: 'http://localhost:8085/api',
        prod: 'http://localhost:8086/api',
    },
    
    defaultEnv: 'dev',

    thresholds: {
        http_req_duration: ['p(95)<2000'],    
        http_req_failed: ['rate<0.1'],         
    },

    timeout: '30s',

    sleepDuration: 100,
};

export function getBaseUrl() {
    const env = __ENV.ENV || config.defaultEnv;
    return __ENV.BASE_URL || config.baseUrls[env] || config.baseUrls.dev;
}
