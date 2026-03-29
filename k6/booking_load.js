import http from "k6/http";
import { check, sleep } from "k6";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

/**
 * Login load test (100 VUs)
 * - Sends POST /api/auth/login repeatedly
 */

export const options = {
  vus: 100,
  duration: "30s", 
  thresholds: {
    http_req_duration: ["p(95)<800"],   // 95% of requests should be < 800ms
    http_req_failed: ["rate<0.05"],     // < 5% failures
  },
};

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USERNAME = __ENV.USERNAME || "student1";
const PASSWORD = __ENV.PASSWORD || "Student@123";

export default function () {
  // Fresh cookie jar per VU/iteration to mimic real logins
  const jar = http.cookieJar();

  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: USERNAME, password: PASSWORD }),
    {
      headers: { "Content-Type": "application/json" },
      jar,
    }
  );

  check(res, {
    "login returns 200": (r) => r.status === 200,
    "jwt cookie is set": () => {
      const cookies = jar.cookiesForURL(BASE_URL);
      return cookies && cookies.jwt && cookies.jwt.length > 0;
    },
  });
  sleep(0.2);
}

// HTML report after the test run.
export function handleSummary(data) {
  return {
    "summary.html": htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}