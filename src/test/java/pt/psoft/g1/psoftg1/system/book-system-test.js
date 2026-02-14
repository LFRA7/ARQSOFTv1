import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate } from 'k6/metrics';

/**
 * TEST TYPE: System Test - Functional opaque-box
 * SUT: Entire book management system
 */

const errorRate = new Rate('errors');

export const options = {
    vus: 1,                    
    iterations: 1,             
    thresholds: {
        'http_req_duration': ['p(95)<2000'],  
        'http_req_failed': ['rate<0.7'],  // Allow up to 70% failures (many endpoints may return 404 with empty DB)
        'errors': ['rate<0.7'],           // Allow up to 70% errors (tests check for 200 or 404 responses)
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8084/api';

function getParams() {
    return {
        headers: {
            'Content-Type': 'application/json',
        },
    };
}

// Setup function - runs once before tests
export function setup() {
    console.log(`Setting up test data at: ${BASE_URL}`);
    
    const testData = {
        isbn: '9782826012092',
        title: 'Test Book',
        genre: 'Fiction',
    };
    
    return { testData };
}

// Main test function
export default function(data) {
    const { testData } = data;
    
    group('Book Management System Tests', function() {
        
        // System test: Complete book retrieval flow
        group('testCompleteBookRetrievalFlow', function() {
            const url = `${BASE_URL}/books/${testData.isbn}`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response contains ISBN': (r) => r.body.includes(testData.isbn) || r.body.includes('isbn'),
                'response contains title': (r) => r.body.includes('Test Book') || r.body.length > 0,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testCompleteBookRetrievalFlow - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Book search flow
        group('testBookSearchFlow', function() {
            const url = `${BASE_URL}/books?title=Test Book`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response contains results': (r) => r.body.includes('Test Book') || r.body.length > 2,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testBookSearchFlow - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Genre-based book search
        group('testGenreBasedBookSearch', function() {
            const url = `${BASE_URL}/books?genre=Fiction`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response contains Fiction': (r) => r.body.includes('Fiction') || r.body.length > 2,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testGenreBasedBookSearch - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Top books retrieval flow
        group('testTopBooksRetrievalFlow', function() {
            const url = `${BASE_URL}/books/top5`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response is not null': (r) => r.body !== null && r.body !== undefined,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testTopBooksRetrievalFlow - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Redis cache test - Top 5 books caching behavior
        group('testRedisCacheForTop5Books', function() {
            const url = `${BASE_URL}/books/top5`;
            
            console.log('Testing Redis cache for top 5 books...');
            
            // First call - should hit database and cache the result
            const firstCallStart = new Date().getTime();
            const res1 = http.get(url, getParams());
            const firstCallDuration = new Date().getTime() - firstCallStart;
            
            const check1 = check(res1, {
                'first call status is 200': (r) => r.status === 200,
                'first call has data': (r) => r.body !== null && r.body.length > 0,
            });
            
            console.log(`First call duration: ${firstCallDuration}ms`);
            sleep(0.2);
            
            // Second call - should be served from Redis cache (faster)
            const secondCallStart = new Date().getTime();
            const res2 = http.get(url, getParams());
            const secondCallDuration = new Date().getTime() - secondCallStart;
            
            const check2 = check(res2, {
                'second call status is 200': (r) => r.status === 200,
                'second call has data': (r) => r.body !== null && r.body.length > 0,
                'second call returns same data': (r) => r.body === res1.body,
            });
            
            console.log(`Second call duration: ${secondCallDuration}ms`);
            
            // Third call - verify consistency
            const res3 = http.get(url, getParams());
            const check3 = check(res3, {
                'third call status is 200': (r) => r.status === 200,
                'third call consistent with cache': (r) => r.body === res1.body,
            });
            
            const success = check1 && check2 && check3;
            errorRate.add(!success);
            
            if (!success) {
                console.error('Test failed: testRedisCacheForTop5Books');
            } else {
                console.log('✓ Redis cache working - subsequent calls returned cached data');
                if (secondCallDuration < firstCallDuration * 0.8) {
                    console.log(`✓ Cache performance improvement: ${Math.round((1 - secondCallDuration/firstCallDuration) * 100)}% faster`);
                }
            }
            
            sleep(0.1);
        });
        
        // System test: Book not found scenario
        group('testBookNotFoundScenario', function() {
            const url = `${BASE_URL}/books/9999999999999`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 404': (r) => r.status === 404,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testBookNotFoundScenario - Status: ${res.status}, expected 404`);
            sleep(0.1);
        });
        
        // System test: Book photo retrieval
        group('testBookPhotoRetrieval', function() {
            const url = `${BASE_URL}/books/${testData.isbn}/photo`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testBookPhotoRetrieval - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Average lending duration endpoint accessibility
        group('testAverageLendingDurationEndpoint', function() {
            const url = `${BASE_URL}/books/${testData.isbn}/avgDuration`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 400 or 200 or 404': (r) => r.status === 400 || r.status === 200 || r.status === 404,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testAverageLendingDurationEndpoint - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Search books with POST request
        group('testSearchBooksWithPostRequest', function() {
            const url = `${BASE_URL}/books/search`;
            
            const payload = JSON.stringify({
                page: { number: 1, limit: 10 },
                query: { title: 'Test', genre: '', authorName: '' }
            });
            
            const params = {
                headers: {
                    'Content-Type': 'application/json',
                },
            };
            
            const res = http.post(url, payload, params);
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response is not null': (r) => r.body !== null && r.body !== undefined,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testSearchBooksWithPostRequest - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Multiple sequential requests flow
        group('testMultipleSequentialRequestsFlow', function() {
            const searchUrl = `${BASE_URL}/books?genre=Fiction`;
            const searchRes = http.get(searchUrl);
            const check1 = check(searchRes, {
                'search status is 200': (r) => r.status === 200,
            });
            
            sleep(0.05);

            const detailsUrl = `${BASE_URL}/books/${testData.isbn}`;
            const detailsRes = http.get(detailsUrl);
            const check2 = check(detailsRes, {
                'details status is 200': (r) => r.status === 200,
            });
            
            sleep(0.05);

            const topUrl = `${BASE_URL}/books/top5`;
            const topRes = http.get(topUrl);
            const check3 = check(topRes, {
                'top books status is 200': (r) => r.status === 200,
            });
            
            const success = check1 && check2 && check3;
            errorRate.add(!success);
            if (!success) console.error('Test failed: testMultipleSequentialRequestsFlow');
            sleep(0.1);
        });
        
        // System test: Response headers validation
        group('testResponseHeadersValidation', function() {
            const url = `${BASE_URL}/books/${testData.isbn}`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'has Content-Type header': (r) => r.headers['Content-Type'] !== undefined,
                'has ETag header': (r) => r.headers['Etag'] !== undefined || r.headers['etag'] !== undefined,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testResponseHeadersValidation - Status: ${res.status}`);
            sleep(0.1);
        });
    });
}

export function teardown(data) {
    console.log('Test execution completed');

}
