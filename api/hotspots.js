const OVERPASS_ENDPOINTS = [
  "https://overpass-api.de/api/interpreter",
  "https://overpass.kumi.systems/api/interpreter",
];

const UGANDA_BOUNDS = {
  south: -1.6,
  north: 4.3,
  west: 29.4,
  east: 35.1,
};

function isValidCoordinate(value, minimum, maximum) {
  return Number.isFinite(value) && value >= minimum && value <= maximum;
}

function normalizeElement(element) {
  const tags = element.tags || {};
  const latitude = element.lat ?? element.center?.lat;
  const longitude = element.lon ?? element.center?.lon;

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return null;

  const address = [
    tags["addr:street"],
    tags["addr:suburb"],
    tags["addr:city"],
  ].filter(Boolean).join(", ");

  return {
    id: `${element.type || "object"}/${element.id}`,
    name: tags.name || "Mapped Wi-Fi location",
    lat: latitude,
    lon: longitude,
    address,
    accessFee: tags["internet_access:fee"] || null,
    website: tags["contact:website"] || tags.website || null,
  };
}

async function fetchListings(latitude, longitude, radius) {
  const query = `
    [out:json][timeout:25];
    (
      node(around:${radius},${latitude},${longitude})["internet_access"~"^(wlan|wifi)$",i];
      way(around:${radius},${latitude},${longitude})["internet_access"~"^(wlan|wifi)$",i];
      relation(around:${radius},${latitude},${longitude})["internet_access"~"^(wlan|wifi)$",i];
    );
    out center tags;
  `;
  let lastError;

  for (const endpoint of OVERPASS_ENDPOINTS) {
    try {
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "User-Agent": "DataLinkUganda/1.0 (public hotspot directory)",
        },
        body: new URLSearchParams({ data: query }),
        signal: AbortSignal.timeout(12_000),
      });

      if (!response.ok) {
        lastError = new Error(`OpenStreetMap service returned HTTP ${response.status}`);
        continue;
      }

      const data = await response.json();
      return (data.elements || [])
        .map(normalizeElement)
        .filter(Boolean)
        .filter(
          (hotspot) =>
            hotspot.lat >= UGANDA_BOUNDS.south &&
            hotspot.lat <= UGANDA_BOUNDS.north &&
            hotspot.lon >= UGANDA_BOUNDS.west &&
            hotspot.lon <= UGANDA_BOUNDS.east,
        );
    } catch (error) {
      lastError = error;
    }
  }

  throw lastError || new Error("OpenStreetMap services are unavailable");
}

module.exports = async function handler(request, response) {
  response.setHeader("Access-Control-Allow-Origin", "*");
  response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  response.setHeader("Access-Control-Allow-Headers", "Content-Type");
  response.setHeader("Cache-Control", "public, s-maxage=300, stale-while-revalidate=1800");

  if (request.method === "OPTIONS") {
    return response.status(204).end();
  }
  if (request.method !== "GET") {
    response.setHeader("Allow", "GET, OPTIONS");
    return response.status(405).json({ error: "Use GET to request hotspot listings." });
  }

  const latitude = parseCoordinate(request.query.lat);
  const longitude = parseCoordinate(request.query.lon);
  const radius = request.query.radius == null ? 5_000 : parseCoordinate(request.query.radius);
  const inUganda =
    isValidCoordinate(latitude, UGANDA_BOUNDS.south, UGANDA_BOUNDS.north) &&
    isValidCoordinate(longitude, UGANDA_BOUNDS.west, UGANDA_BOUNDS.east);

  if (!inUganda || !Number.isInteger(radius) || radius < 100 || radius > 10_000) {
    return response.status(400).json({
      error: "Provide coordinates in Uganda and a search radius from 100 to 10000 metres.",
    });
  }

  try {
    const hotspots = await fetchListings(latitude, longitude, radius);
    return response.status(200).json({
      source: "OpenStreetMap",
      attribution: "© OpenStreetMap contributors",
      license: "Open Database License (ODbL)",
      savedAt: new Date().toISOString(),
      hotspots,
    });
  } catch (error) {
    console.error("OpenStreetMap hotspot lookup failed", error);
    return response.status(502).json({
      error: "Hotspot listings are temporarily unavailable. Try again later.",
    });
  }
};

module.exports.normalizeElement = normalizeElement;

function parseCoordinate(value) {
  if (typeof value !== "string" || value.trim() === "") return Number.NaN;
  return Number(value);
}
