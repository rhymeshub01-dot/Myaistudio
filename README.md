# My AI Studio — Android starter
A real Android app frontend for text-to-video generation. This is **not yet a standalone AI video generator**.
It requires a working video-generation backend; without one, the Generate button cannot produce videos.

## Build on GitHub (from a phone)
1. Create a GitHub repository and upload the project files preserving folders.
2. Open Actions > Build Android APK > Run workflow.
3. When complete, download the `MyAIStudio-debug-apk` artifact and extract the APK.
4. Install the APK on your Android phone (review Android's install warnings).

## Backend API contract
POST to an HTTPS endpoint with JSON: `{"prompt":"A friendly 3D cartoon elephant dancing"}`
On successful completion, return JSON: `{"video_url":"https://your-server/video.mp4"}`
On failure return JSON: `{"error":"Explanation"}` and a non-2xx status.
The backend is NOT included: running models such as Wan/LTX requires GPU compute.
Many video services are asynchronous; a production app should support job IDs and polling.
Do not embed private API keys in Android source code. Use a secured backend.
