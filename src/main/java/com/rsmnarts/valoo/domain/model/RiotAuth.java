package com.rsmnarts.valoo.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiotAuth {
	String clientPlatform;
	String clientVersion;
	String entitlementsToken;
	String authorization;
	String shard;
}
