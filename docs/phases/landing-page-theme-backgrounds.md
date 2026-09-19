# Landing page theme backgrounds

## Status

Complete.

## What changed

- Replaced the animated blueprint background on the landing page with the supplied static artwork.
- Light mode uses `public/bg_light.jpg`.
- Dark mode uses `public/bg_dark.jpg`.
- Added theme-specific translucent overlays so headings, controls, and glass panels remain readable while the artwork stays visible.
- Kept the existing hero and section entrance animations; only the animated background was removed.
- Removed the unused blueprint SVG, scan layer, animation styles, and keyframes.

## Verification

- Frontend production build succeeds.
- Both image files are included in the production output.
- Existing lint baseline remains unchanged.

## Running with Docker

Rebuild the frontend image so Docker copies the new assets and CSS:

```text
docker compose up --build
```
