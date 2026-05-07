# Messing Backend

Backend của **Messing** — một ứng dụng chat realtime lấy cảm hứng từ Discord.
Dự án được xây dựng bằng **Spring Boot 4**, **Kotlin**, **Spring Security**, **Spring Data JPA**, **WebSocket/STOMP**, **MariaDB** và **LiveKit**.

## Tổng quan

Backend chịu trách nhiệm cho các chức năng chính sau:

- xác thực và phân quyền người dùng bằng JWT
- quản lý server, channel và thành viên
- lưu trữ và phát tán tin nhắn realtime
- xử lý invite và tham gia server bằng mã mời
- upload ảnh / file cho avatar, tin nhắn và server
- hỗ trợ voice room, screen share và signalling realtime
- quản lý bot của server
- xử lý lỗi thống nhất và validate dữ liệu đầu vào

## Công nghệ sử dụng

- Spring Boot 4.0.4
- Kotlin 2.2.21
- Spring Security
- Spring Data JPA
- Spring Web MVC
- Spring WebSocket
- Spring Session Data Redis
- Bean Validation
- MariaDB
- Cloudinary
- JWT với `jjwt`
- LiveKit server SDK
- GraphQL code generation

## Yêu cầu môi trường

- JDK tương thích với cấu hình build hiện tại
- Maven 3.x
- MariaDB
- Redis nếu môi trường của bạn bật session / caching
- LiveKit credentials nếu dùng voice features
- Cloudinary credentials nếu dùng storage cloud

> Cấu hình build hiện tại đang khai báo `java.version=22`, `maven.compiler.release=17` và `kotlin.compiler.jvmTarget=17`.
> Hãy dùng JDK phù hợp với môi trường build của bạn để tránh lỗi compile.

## Cấu trúc dự án

Các thư mục chính trong `src/main/kotlin/com/example/messing`:

- `config` — cấu hình Security, CORS, WebSocket, JWT filter
- `controller` — REST API và websocket entry points
- `service` — business logic
- `repository` — truy vấn database
- `entity` — model JPA
- `dto` — request / response objects
- `security` — JWT utilities và websocket auth interceptor
- `exception` — exception hierarchy và handler toàn cục

## Chạy project ở local

Từ thư mục `Messing`:

```sh
mvn clean install
```

Sau khi build xong, chạy ứng dụng:

```sh
mvn spring-boot:run
```

Hoặc chạy file jar đã build:

```sh
java -jar target/Messing-0.0.1-SNAPSHOT.jar
```

## Cấu hình runtime

Backend đọc cấu hình từ `src/main/resources/application.properties` và có thể override bằng `.env` nếu file tồn tại.

Các biến quan trọng gồm:

- `JWT_SECRET`
- `JWT_EXPIRATION`
- `APP_UPLOAD_DIR`
- `APP_PUBLIC_BASE_URL`
- `APP_AVATAR_SIZE`
- `MULTIPART_MAX_FILE_SIZE`
- `MULTIPART_MAX_REQUEST_SIZE`
- `LIVEKIT_URL`
- `LIVEKIT_API_KEY`
- `LIVEKIT_API_SECRET`
- `BOT_API_KEY`
- `BOT_BASE_URL`
- `BOT_MODEL`

Ví dụ file `.env` tối thiểu:

```properties
JWT_SECRET=your-base64-secret
JWT_EXPIRATION=86400000
APP_UPLOAD_DIR=uploads
APP_PUBLIC_BASE_URL=http://localhost:8080
LIVEKIT_URL=https://your-livekit-host
LIVEKIT_API_KEY=your-key
LIVEKIT_API_SECRET=your-secret
```

## Các luồng nghiệp vụ chính

### 1. Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

Luồng xác thực dùng JWT bearer token. Token được đưa vào header `Authorization: Bearer <token>` cho REST API, và dùng cho WebSocket/STOMP qua interceptor.

### 2. Server management

- tạo server
- cập nhật server
- xoá server
- upload avatar server
- mời thành viên
- tạo invite link
- join server bằng invite
- quản lý bot của server

### 3. Channel management

- tạo channel
- đổi tên channel
- xoá channel
- liệt kê channel theo server

### 4. Message handling

- lấy lịch sử tin nhắn theo channel
- gửi tin nhắn realtime qua WebSocket
- upload image message

### 5. Voice / realtime

- join / leave voice room
- toggle mic
- toggle screen share
- build và broadcast voice state

## REST API chính

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Servers

- `GET /api/servers`
- `POST /api/servers`
- `PUT /api/servers/{serverId}`
- `DELETE /api/servers/{serverId}`
- `POST /api/servers/{serverId}/avatar`
- `POST /api/servers/{serverId}/invite`
- `POST /api/servers/{serverId}/invites`
- `POST /api/servers/invites/{code}/accept`
- `PUT /api/servers/{serverId}/bot`

### Channels

- `GET /api/servers/{serverId}/channels`
- `POST /api/servers/{serverId}/channels`
- `PUT /api/servers/{serverId}/channels/{channelId}`
- `DELETE /api/servers/{serverId}/channels/{channelId}`

### Messages

- `GET /api/channels/{channelId}/messages`
- `POST /api/channels/{channelId}/images`

### Voice

- `POST /api/voice/join`
- `POST /api/voice/leave`
- `POST /api/voice/mic`
- `POST /api/voice/screen-share`
- `GET /api/voice/state/{channelId}`

## WebSocket / STOMP

### Endpoint

- `/ws`

### Application destination prefix

- `/app`

### Broker destinations

- `/topic`
- `/queue`

### Chat message send

Client gửi tới:

```text
/app/chat/{channelId}/sendMessage
```

Server broadcast message tới:

```text
/topic/channels/{channelId}
```

### Auth cho WebSocket

JWT được kiểm tra trong websocket inbound interceptor. Client có thể gửi token qua header native `Authorization` hoặc `X-Authorization` khi CONNECT.

## Storage

Backend hỗ trợ hai chế độ lưu file:

- **local** — lưu vào thư mục upload trên máy chạy backend
- **cloudinary** — upload ảnh lên Cloudinary

File storage được dùng cho:

- avatar user/server/bot
- ảnh tin nhắn

## GraphQL code generation

Project có cấu hình plugin generate source cho GraphQL client.

- schema path: `src/main/resources/graphql-client`
- generated source sẽ được thêm vào build trong giai đoạn generate sources

## Khuyến nghị phát triển

- Giữ controller mỏng, đẩy business logic vào service
- Luôn validate quyền truy cập server/channel trước khi thao tác dữ liệu
- Không để WebSocket hoặc voice state phụ thuộc vào dữ liệu không được xác thực
- Với upload file, luôn kiểm tra content type và giới hạn kích thước
- Dùng `GlobalExceptionHandler` để trả lỗi API đồng nhất

## Thứ tự đọc code đề xuất

Nếu muốn hiểu backend nhanh, nên đọc theo thứ tự:

1. `src/main/resources/application.properties`
2. `src/main/kotlin/com/example/messing/config`
3. `src/main/kotlin/com/example/messing/security`
4. `src/main/kotlin/com/example/messing/entity`
5. `src/main/kotlin/com/example/messing/repository`
6. `src/main/kotlin/com/example/messing/service`
7. `src/main/kotlin/com/example/messing/controller`
8. `src/main/kotlin/com/example/messing/dto`
9. `src/main/kotlin/com/example/messing/exception`

## Ghi chú

- Voice state hiện được lưu in-memory, nên sẽ reset khi backend restart.
- Một số tính năng realtime phụ thuộc vào đúng cấu hình JWT, LiveKit và WebSocket origin.
- Nếu bạn triển khai production, hãy rà lại CORS, allowed origins và cấu hình upload/public URL.
