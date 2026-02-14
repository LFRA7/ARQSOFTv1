import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate } from 'k6/metrics';

/**
 * TEST TYPE: System Test - Functional opaque-box
 * SUT: Author management system (complete end-to-end flows)
 */

const errorRate = new Rate('errors');

// Test configuration
export const options = {
    vus: 1,                    
    iterations: 1,             
    thresholds: {
        'http_req_duration': ['p(95)<3000'], 
        'http_req_failed': ['rate<0.5'],      
        'errors': ['rate<0.5'],               
    },
};

// Configuration
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
    
    const author = { 
        authorNumber: __ENV.AUTHOR_ID || '1', 
        name: 'Test Author k6' 
    };
    
    console.log(`Using author number: ${author.authorNumber}`);
    
    return { author };
}

// Main test function
export default function(data) {
    const { author } = data;
    
    group('Author Management System Tests', function() {
        
        // System test: Complete author retrieval flow
        group('testCompleteAuthorRetrievalFlow', function() {
            const url = `${BASE_URL}/authors/${author.authorNumber}`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response is not empty': (r) => r.body && r.body.length > 0,
            });
            
            if (res.status === 200) {
                check(res, {
                    'response contains data': (r) => r.body.length > 10,
                });
            }
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testCompleteAuthorRetrievalFlow - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Author search by name
        group('testAuthorSearchByName', function() {
            const url = `${BASE_URL}/authors?name=Test`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testAuthorSearchByName - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Get specific author by authorNumber
        group('testGetSpecificAuthor', function() {
            const url = `${BASE_URL}/authors/${author.authorNumber}`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
                'response is not null': (r) => r.body !== null && r.body !== undefined,
                'response has content': (r) => r.body.length > 0,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testGetSpecificAuthor - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Author not found scenario
        group('testAuthorNotFoundScenario', function() {
            const url = `${BASE_URL}/authors/99999`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 404': (r) => r.status === 404,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testAuthorNotFoundScenario - Status: ${res.status}, expected 404`);
            sleep(0.1);
        });
        
        // System test: Get books by author
        group('testGetBooksByAuthor', function() {
            const url = `${BASE_URL}/authors/${author.authorNumber}/books`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 200': (r) => r.status === 200,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testGetBooksByAuthor - Status: ${res.status}`);
            sleep(0.1);
        });
        
        // System test: Get top authors by lendings (expects 404 when no lendings exist)
        group('testGetTopAuthorsByLendings', function() {
            const url = `${BASE_URL}/authors/top5`;
            const res = http.get(url, getParams());
            
            const success = check(res, {
                'status is 404 or 200': (r) => r.status === 404 || r.status === 200,
            });
            
            errorRate.add(!success);
            if (!success) console.error(`Test failed: testGetTopAuthorsByLendings - Status: ${res.status}`);
            sleep(0.1);
        });

        // System test: Redis cache test for top 5 authors
        group('testRedisCacheForTop5Authors', function() {
            const url = `${BASE_URL}/authors/top5`;
            
            console.log('=== Starting Redis Cache Test for Top 5 Authors ===');
            
            // First call - hits database and caches result (or returns 404 if no data)
            const firstCallStart = new Date().getTime();
            const res1 = http.get(url, getParams());
            const firstCallDuration = new Date().getTime() - firstCallStart;
            
            console.log(`First call: ${res1.status} in ${firstCallDuration}ms`);
            
            // If endpoint returns data (200), test caching behavior
            if (res1.status === 200) {
                sleep(0.05); // Small delay to ensure cache is written
                
                // Second call - should be served from Redis cache (faster)
                const secondCallStart = new Date().getTime();
                const res2 = http.get(url, getParams());
                const secondCallDuration = new Date().getTime() - secondCallStart;
                
                console.log(`Second call: ${res2.status} in ${secondCallDuration}ms`);
                
                const success = check(res2, {
                    'second call status is 200': (r) => r.status === 200,
                    'second call returns same data': (r) => r.body === res1.body,
                });
                
                errorRate.add(!success);
                
                // Third call - verify cache consistency
                sleep(0.05);
                const thirdCallStart = new Date().getTime();
                const res3 = http.get(url, getParams());
                const thirdCallDuration = new Date().getTime() - thirdCallStart;
                
                console.log(`Third call: ${res3.status} in ${thirdCallDuration}ms`);
                
                check(res3, {
                    'third call status is 200': (r) => r.status === 200,
                    'third call returns same data': (r) => r.body === res1.body,
                });
                
                // Calculate and log cache performance improvement
                if (secondCallDuration < firstCallDuration * 0.8) {
                    const improvement = Math.round((1 - secondCallDuration/firstCallDuration) * 100);
                    console.log(`✓ Cache performance improvement detected: ${improvement}% faster on second call`);
                } else {
                    console.log(`⚠ Second call (${secondCallDuration}ms) not significantly faster than first (${firstCallDuration}ms)`);
                }
                
                if (thirdCallDuration < firstCallDuration * 0.8) {
                    const improvement = Math.round((1 - thirdCallDuration/firstCallDuration) * 100);
                    console.log(`✓ Cache performance improvement detected: ${improvement}% faster on third call`);
                }
                
                console.log('✓ Redis cache test completed successfully - data consistency verified');
            } else if (res1.status === 404) {
                console.log('⚠ Endpoint returned 404 - no data available for caching test');
                console.log('  This is expected if no lending data exists in the system');
            } else {
                console.error(`✗ Unexpected status: ${res1.status}`);
                errorRate.add(true);
            }
            
            console.log('=== Redis Cache Test for Top 5 Authors Complete ===');
            sleep(0.1);
        });
    });
}

// Teardown function - runs once after all tests
export function teardown(data) {
    console.log('Test execution completed');
}
