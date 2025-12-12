# valoo

valoo is a Spring Boot application that provides widgets and dashboards for Valorant players. It allows users to view their **Daily Store** and **Night Market** (Bonus Store) directly in a browser, complete with weapon prices, discounts, and video previews of skins.

> **Note:** This project converts Riot Games access tokens to region-specific tokens to fetch data.

## Features

- **Storefront**: View your currently available daily weapon skins.
- **Night Market**: Check your Night Market specifically, including:
    - Original vs Discounted Prices.
    - Discount Percentage badges.
    - Premium UI with glassmorphism design.
- **Skin Previews**: Integrated video previews for weapon skins and their levels.
- **Authentication**: Supports Riot Games authentication (AccessToken & Entitlements Token required).
- **Responsive Design**: Built with TailwindCSS for a seamless experience on different screen sizes.

## Tech Stack

- **Java 25**
- **Spring Boot 3** (Web, Cache, Actuator)
- **Spring Cloud OpenFeign** (for Riot API communication)
- **Thymeleaf** (Server-side rendering)
- **TailwindCSS** (Standalone CLI via wrapper script)
- **Redis** (Caching support)
- **Docker** (Containerization)

## Prerequisites

- **Java JDK 25** (or higher)
- **Maven 3.9+**
- **Docker** (Optional, for containerized deployment)
- **Riot Games Account Tokens**: You need a valid `Authorization` (Bearer token) and `Entitlements-Token` to access the endpoints.

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/rsmnarts/valoo.git
cd valoo
```

### 2. Build the project

This project uses a custom script `build-css.sh` to download and run the TailwindCSS CLI purely from Maven/Shell, avoiding a Node.js dependency.

```bash
# Clean and compile (this will also trigger CSS build if configured in hooks, 
# otherwise run the script manually first)
./build-css.sh
./mvnw clean package
```

### 3. Run Locally

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

## Docker Support

You can build and run the application using Docker.

### Build Image
```bash
docker build -t valorant-widgets .
```

### Run Container
```bash
docker run -p 8080:8080 valorant-widgets
```

## Usage

### Accessing the Store

Since the application requires Riot Authentication tokens, the most common way to access the views is by passing your PUUID and Tokens.

**Endpoint pattern:**
`/stores?puuid={your_puuid}`

**Note:** The application expects the `Authorization` and `Entitlements-Token` to be available in your browser's Local Storage (`valorant_access_token` and `valorant_entitlements_token`) or handled via a proxy/login mechanism not detailed in this repository.

### Night Market

Navigate to the Night Market by clicking the button on the Daily Store page or directly via:
`/stores/night-market?puuid={your_puuid}`

## Contributing

Contributions are welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## License

Distributed under the MIT License. See `LICENSE` for more information.

## Legal

valoo isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are trademarks or registered trademarks of Riot Games, Inc.
