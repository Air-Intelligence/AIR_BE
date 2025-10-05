                          # Air Intelligence Backend

Real-time Air Quality Monitoring and Alert Notification System - Backend API Server

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation & Running](#installation--running)
    - [Environment Variables](#environment-variables)
- [Architecture](#architecture)
    - [System Architecture](#system-architecture)
    - [Package Structure](#package-structure)
    - [Data Flow](#data-flow)
- [API Documentation](#api-documentation)
- [Development Guide](#development-guide)
- [Deployment](#deployment)

## Overview

Air Intelligence Backend is a Spring Boot-based RESTful API server that provides real-time air pollution information based on NASA's NO2 air quality data, analyzes risk levels according to user location, and sends web push notifications.

## Key Features

- **Real-time Air Quality Data Collection**: Automatically fetches NO2 data from external FastAPI server every hour
- **AI-Powered PM2.5 Prediction**: Integrates with AI prediction service to forecast PM2.5 levels 2 hours ahead
- **Pre-calculated Geographic Data & Caching**:
    - Generate hazardous area polygons using Convex Hull algorithm and store in MongoDB
    - Grid-based point data aggregation and storage
    - Batch processing to minimize real-time computation load
- **Risk Level Analysis**: 5-tier warning level system (SAFE, READY, WARNING, DANGER, RUN)
- **Point-in-Polygon Algorithm**: User location-based hazard zone determination using Ray Casting algorithm
- **Web Push Notifications**: Automatic alerts when users enter hazardous zones or high PM2.5 is predicted
- **User Location Tracking**: Store and manage users' last known coordinates
- **GeoJSON Format Support**: Provide air quality data in standard GeoJSON format

## AI-Powered PM2.5 Prediction

The system integrates with an AI prediction service to provide proactive air quality alerts.

### How It Works

1. **Scheduled Checking**: Every hour, the `WarningScheduler` checks all user locations
2. **Polygon-Based Risk Assessment**: First, determines if users are inside any hazardous warning polygons
3. **Predictive Analysis**: For users outside warning zones, queries the AI prediction API for future PM2.5 levels
4. **Proactive Notifications**: Sends push notifications when predicted PM2.5 exceeds threshold (>0.01) 2 hours ahead

### Prediction API Integration

**Endpoint**: `POST https://fastapi.bestbreathe.us/api/predict/pm25`

**Request Format**:
```json
{
  "lat": 37.5665,
  "lon": 126.9780,
  "when": "2025-10-05T14:30:00"
}
```

**Response Format**:
```json
{
  "pred_pm25": 0.0123
}
```

**Implementation**: `NasaDataRepository.findPrediction(lat, lon)`
- Automatically calculates prediction time (current time + 2 hours)
- Formats request with ISO 8601 timestamp
- Returns predicted PM2.5 concentration value

### Use Cases

- **Early Warning**: Users receive notifications before air quality deteriorates
- **Location-Specific Predictions**: Tailored forecasts for each user's exact location
- **Complementary to Real-Time Data**: Combines current polygon-based warnings with future predictions

## Tech Stack

### Core
- **Java 21** - LTS version
- **Spring Boot 3.5.6** - Application framework
- **Spring Data MongoDB** - NoSQL database integration
- **Spring Security** - Security and CORS configuration
- **Spring Validation** - Input data validation
- **Spring Scheduling** - Batch job scheduling

### Database
- **MongoDB 7.0.24** - Storage for users, air quality data, and GeoFeature data

### Libraries
- **Lombok** - Reduce boilerplate code
- **Web Push (nl.martijndwars)** - VAPID-based web push notifications
- **BouncyCastle** - Cryptography library
- **SpringDoc OpenAPI** - Automatic API documentation generation

### Build & DevOps
- **Gradle 8.x** - Build tool
- **Docker** - Containerization
- **JUnit Platform** - Testing framework

## Getting Started

### Prerequisites

- Java 21 or higher
- Docker (for running MongoDB)
- Gradle (wrapper included in project)

### Installation & Running

#### 1. Run MongoDB

```bash
# Pull and run MongoDB container
docker pull mongodb/mongodb-community-server:7.0.24-rc0-ubi8
docker run --name mongodb -d -p 27017:27017 mongodb/mongodb-community-server:7.0.24-rc0-ubi8
```

#### 2. Clone & Build Project

```bash
# Clone project
git clone <repository-url>
cd AIR_BE

# Build
./gradlew build

# Run application
./gradlew bootRun
```

### Environment Variables

Configure the following values in `application.yml` or as environment variables:

```bash
# Web Push VAPID keys (required)
VAPID_PUBLIC_KEY=<your-vapid-public-key>
VAPID_PRIVATE_KEY=<your-vapid-private-key>

# Cookie domain (optional, default: localhost)
COOKIE_DOMAIN=localhost

# CORS allowed origins (optional, default: http://localhost:5173)
CORS_ALLOWED_ORIGINS=http://localhost:5173

# FastAPI server URL (required)
FAST_API_URL=https://fastapi.bestbreathe.us
```

## Architecture

### System Architecture

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Client    │◄────────┤  Spring Boot API │────────►│    MongoDB      │
│  (Browser)  │  REST   │                  │         │                 │
└─────────────┘         │  - Weather API   │         │ - Users         │
                        │  - User API      │         │ - NasaData      │
                        │  - Notification  │         │ - GeoFeatures   │
                        └──────────────────┘         └─────────────────┘
                               │      ▲
                        Fetch  │      │ Every 1 hour
                        NO2 &  │      │ Scheduled
                        Predict│      │
                               ▼      │
                        ┌──────────────────────────┐
                        │   FastAPI Server         │
                        │  - NASA NO2 Data API     │
                        │  - AI PM2.5 Prediction   │
                        └──────────────────────────┘
```

### Package Structure

```
air.intelligence/
├── config/              # Spring configuration classes
│   ├── SpringSecurityConfig.java      # CORS, Security settings
│   ├── BeanConfig.java                # Bean config (RestTemplate, etc.)
│   ├── ThirdPartyBeanConfig.java      # External library beans (PushService, etc.)
│   └── WarningConstant.java           # Warning threshold constants
│
├── controller/          # REST API endpoints
│   ├── WeatherController.java         # Air quality data query API
│   ├── UserController.java            # User management API
│   └── NotificationController.java    # Notification subscription API
│
├── service/             # Business logic
│   ├── WeatherService.java            # Weather service interface
│   ├── DefaultWeatherService.java     # Cached GeoFeature data retrieval
│   ├── MockDataWeatherService.java    # Mock service for testing
│   ├── UserService.java               # User management service
│   ├── NotificationService.java       # Push notification service
│   └── NasaDataService.java           # NASA data processing
│
├── repository/          # Data access layer
│   ├── UserRepository.java                # User MongoDB repository
│   ├── WeatherRepository.java             # Air quality data MongoDB repository
│   ├── GeoFeatureDataRepository.java      # GeoFeature cache repository
│   ├── NasaDataRepository.java            # FastAPI integration (NO2 & AI Prediction)
│   └── dto/                               # Repository DTOs
│       ├── No2DataDto.java                # NO2 data response
│       ├── No2ResponseDto.java            # NO2 API wrapper
│       └── Pm25PredictionDto.java         # AI prediction response (NEW)
│
├── domain/              # MongoDB entities
│   ├── User.java                      # User domain
│   ├── NasaData.java                  # Air quality data domain
│   └── GeoFeatureData.java            # GeoFeature cache domain (NEW)
│
├── dto/                 # API request/response DTOs
│   ├── GeoResponse.java               # GeoJSON response
│   ├── UserCreationDto.java           # User creation DTO
│   ├── LastCoordUpdateRequest.java    # Coordinate update request
│   └── SubscriptionRequest.java       # Push subscription request
│
├── value/               # Value objects
│   ├── Coord.java                     # Coordinates (latitude/longitude)
│   ├── WarningLevel.java              # Warning level enum
│   ├── WarningMessage.java            # Warning message
│   ├── GeoFeature.java                # GeoJSON Feature
│   ├── GeoFeatureType.java            # GeoJSON type
│   ├── GeoProperties.java             # GeoJSON Properties
│   └── Geometry.java                  # GeoJSON Geometry
│
├── scheduler/           # Scheduled tasks
│   └── WarningScheduler.java          # Data collection, GeoFeature generation, notifications
│
├── error/               # Error handling
│   ├── errorcode/                     # Error code definitions
│   ├── exception/                     # Custom exceptions
│   └── handler/                       # Global error handlers
│
└── util/                # Utilities
    ├── api/                           # API response wrappers
    └── http/                          # HTTP utilities
```

### Data Flow

#### 1. Periodic Data Collection & Notifications (WarningScheduler)

```
[Execute every 1 hour - fixedRate]
     │
     ▼
┌──────────────────────────────┐
│ 1. Fetch NO2 data from       │
│    FastAPI (NasaDataRepo)    │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ 2. Save NasaData to MongoDB  │
│    (WeatherRepository)       │
│    - Delete existing data    │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ 3. Calculate & save          │
│    GeoFeatures               │
│    (Batch optimization)      │
│                              │
│  a) Polygon Features:        │
│     - Group by warning level │
│     - Apply Convex Hull      │
│     - Save type="polygon"    │
│                              │
│  b) Point Features:          │
│     - Divide into 1° grid    │
│     - Calculate averages     │
│     - Save type="point"      │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ 4. Determine user risk level │
│    (Point-in-Polygon algo)   │
│                              │
│    - Query polygon data      │
│    - Ray Casting algorithm   │
│    - Check each user location│
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ 5. AI Prediction (NEW)       │
│    For users NOT in danger   │
│                              │
│    - Query PM2.5 prediction  │
│      API (2 hours ahead)     │
│    - If pred_pm25 > 0.01:    │
│      Send notification       │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ 6. Send web push             │
│    notifications for         │
│    DANGER+ levels            │
└──────────────────────────────┘
```

#### 2. Air Quality Data Query API (Improved Performance)

**Polygon Endpoint** (`/api/v1/weathers/polygon`)

```
[Client Request]
     │
     ▼
┌─────────────────────────────┐
│ 1. Query type="polygon"     │
│    from GeoFeatureDataRepo  │
│    (pre-calculated data)    │
└───────────┬─────────────────┘
            │
            ▼
┌─────────────────────────────┐
│ 2. Return cached GeoJSON    │
│    Polygon immediately      │
│    (no real-time calc)      │
└─────────────────────────────┘
```

**Point Endpoint** (`/api/v1/weathers/point`)

```
[Client Request]
     │
     ▼
┌─────────────────────────────┐
│ 1. Query type="point"       │
│    from GeoFeatureDataRepo  │
│    (pre-calculated data)    │
└───────────┬─────────────────┘
            │
            ▼
┌─────────────────────────────┐
│ 2. Return cached GeoJSON    │
│    Point immediately        │
│    (no real-time calc)      │
└─────────────────────────────┘
```

### Major Architecture Improvements

#### Batch Processing Optimization (Recent Refactoring)

**Previous Architecture:**
- Real-time Convex Hull calculation for every API request
- MongoDB queries and complex algorithms executed per request
- High response latency and server load

**Improved Architecture:**
- ✅ **Pre-calculation**: Scheduler calculates GeoFeatures every 5 minutes
- ✅ **Caching Strategy**: Store in `GeoFeatureData` collection (separated by type)
- ✅ **Fast Response**: APIs only perform simple queries
- ✅ **Consistency**: All clients retrieve identical data

**Performance Improvements:**
- API response time: ~500ms → ~10ms (approximately 50x improvement)
- Reduced server CPU usage
- Improved concurrent user handling capacity

### Point-in-Polygon Algorithm

Uses **Ray Casting Algorithm** for user location-based risk level determination:

```java
// WarningScheduler.java - isPointInPolygon()
- Cast a ray to the right from user coordinates (lon, lat)
- Count intersections with each polygon edge
- Odd number of intersections: inside (inside = true)
- Even number of intersections: outside (inside = false)
```

### Warning Level System

| Level | NO2 Range | Description | Notification |
|-------|-----------|-------------|--------------|
| SAFE | < 2.0 | Safe | ❌ |
| READY | 2.0 ~ 4.0 | Caution | ❌ |
| WARNING | 4.0 ~ 6.0 | Warning | ❌ |
| DANGER | 6.0 ~ 7.8 | Dangerous | ✅ Push notification |
| RUN | ≥ 7.8 | Very dangerous | ✅ Push notification |

Warning thresholds are managed in `WarningConstant.java`.

## API Documentation

After running the application, access the auto-generated API documentation via SpringDoc OpenAPI:

```
http://localhost:8080/swagger-ui.html
```

### Main Endpoints

#### Air Quality Query

- `GET /api/v1/weathers/polygon` - Query polygon data by warning level
    - ~~Query Parameters: `lower-lat`, `lower-lon`, `upper-lat`, `upper-lon`~~ (currently unused)
    - Response: GeoJSON FeatureCollection (Polygon)
    - Returns pre-calculated cached data

- `GET /api/v1/weathers/point` - Query grid-based point data
    - ~~Query Parameters: `lower-lat`, `lower-lon`, `upper-lat`, `upper-lon`~~ (currently unused)
    - Response: GeoJSON FeatureCollection (Point)
    - Returns pre-calculated cached data

#### User Management

- `POST /api/v1/users` - Create user
- `GET /api/v1/users/{userId}` - Get user
- `PUT /api/v1/users/{userId}/coord` - Update user location

#### Notification Subscription

- `POST /api/v1/notifications/subscribe` - Subscribe to push notifications

## Development Guide

### Code Style

- Use Lombok annotations (`@RequiredArgsConstructor`, `@Getter`, `@Builder`, etc.)
- Use constructor injection pattern
- Interface-based service design (swappable implementations)

### MongoDB Collections

| Collection | Purpose | Key Fields |
|-----------|---------|------------|
| `users` | User information | id, lastCoord, pushSubscription, warningLevel |
| `nasaData` | Raw NO2 data | timestamp, kind, lat, lon, value |
| `geo_feature` | GeoFeature cache | timestamp, type (polygon/point), features[] |

### Scheduler Operation

```java
@Scheduled(fixedRate = 1000 * 60 * 5)  // Every 5 minutes
public void task() {
    1. Fetch NO2 data
    2. Save NasaData
    3. Calculate and save GeoFeatureData (polygon, point)
    4. Determine user risk levels (Point-in-Polygon)
    5. Send push notifications
}
```

### Writing Tests

```bash
# Location for new test classes
src/test/java/air/intelligence/

# Run tests
./gradlew test
```

### Adding New API

1. Add controller to `controller/`
2. Add interface and implementation to `service/`
3. Add `repository/`, `dto/` if needed
4. Define error codes in `error/errorcode/`
5. Write test code

## Deployment

### Docker Build & Run

```bash
# 1. Build application
./gradlew build

# 2. Copy jar file from build/libs/ to build/docker/
mkdir -p build/docker
cp build/libs/*.jar build/docker/app.jar

# 3. Build Docker image
docker build -t air-intelligence .

# 4. Run Docker container
docker run -d \
  -p 8080:8080 \
  -e VAPID_PUBLIC_KEY=<your-key> \
  -e VAPID_PRIVATE_KEY=<your-key> \
  -e FAST_API_URL=https://fastapi.bestbreathe.us \
  -e CORS_ALLOWED_ORIGINS=https://your-domain.com \
  --name air-intelligence-app \
  air-intelligence
```

### Production Checklist

- [ ] Verify environment variables
- [ ] Verify MongoDB connection settings
- [ ] Configure CORS allowed origins
- [ ] Generate and configure VAPID keys
- [ ] Verify FastAPI server accessibility
- [ ] Verify scheduler operation (runs every 5 minutes)
- [ ] Create index on GeoFeatureData collection (`type` field)
- [ ] Adjust log levels (production environment)
- [ ] Verify health check endpoint (`/actuator/health`)

## Recent Changes (Changelog)

### v0.0.1-SNAPSHOT (Latest)
- ✅ **Batch Processing Optimization**: Moved GeoFeature calculation to scheduler
- ✅ **Caching Strategy**: Added `GeoFeatureData` collection
- ✅ **Point-in-Polygon Algorithm**: Ray Casting-based user location determination
- ✅ **API Performance Improvement**: Removed real-time calculations, changed to query-only APIs
- ✅ **Code Refactoring**: Integrated Convex Hull logic into Scheduler

## License

[License information to be added]

## Contact

[Contact information to be added]
