const assert = require("node:assert/strict");
const { afterEach, test } = require("node:test");
const handler = require("./hotspots");

const originalFetch = global.fetch;

afterEach(() => {
  global.fetch = originalFetch;
});

function makeResponse() {
  const headers = {};
  return {
    headers,
    statusCode: undefined,
    body: undefined,
    setHeader(name, value) {
      headers[name] = value;
    },
    status(code) {
      this.statusCode = code;
      return this;
    },
    json(body) {
      this.body = body;
      return this;
    },
    end() {
      return this;
    },
  };
}

test("rejects coordinates outside Uganda without querying OpenStreetMap", async () => {
  global.fetch = () => {
    throw new Error("fetch should not be called");
  };
  const response = makeResponse();

  await handler(
    { method: "GET", query: { lat: "40", lon: "-70" } },
    response,
  );

  assert.equal(response.statusCode, 400);
});

test("rejects an invalid radius", async () => {
  const response = makeResponse();

  await handler(
    { method: "GET", query: { lat: "0.3", lon: "32.6", radius: "50000" } },
    response,
  );

  assert.equal(response.statusCode, 400);
});

test("normalizes mapped Wi-Fi listings and includes OpenStreetMap attribution", async () => {
  global.fetch = async () => ({
    ok: true,
    json: async () => ({
      elements: [
        {
          type: "node",
          id: 42,
          lat: 0.3,
          lon: 32.6,
          tags: {
            name: "Community library",
            "internet_access:fee": "no",
            "addr:city": "Kampala",
          },
        },
      ],
    }),
  });
  const response = makeResponse();

  await handler(
    { method: "GET", query: { lat: "0.3", lon: "32.6" } },
    response,
  );

  assert.equal(response.statusCode, 200);
  assert.equal(response.body.hotspots[0].id, "node/42");
  assert.equal(response.body.hotspots[0].name, "Community library");
  assert.match(response.body.attribution, /OpenStreetMap contributors/);
});

test("returns a useful upstream error when all OpenStreetMap services fail", async () => {
  global.fetch = async () => {
    throw new Error("offline");
  };
  const response = makeResponse();

  await handler(
    { method: "GET", query: { lat: "0.3", lon: "32.6" } },
    response,
  );

  assert.equal(response.statusCode, 502);
  assert.match(response.body.error, /temporarily unavailable/);
});
