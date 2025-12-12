package com.rsmnarts.valoo.infrastructure.client.dto;

import lombok.Data;

@Data
public class VersionResponse {
	private int status;
	private Data data;

	@lombok.Data
	public static class Data {
		private String manifestId;
		private String branch;
		private String version;
		private String buildVersion;
		private String engineVersion;
		private String riotClientVersion;
		private String riotClientBuild;
		private String buildDate;
	}
}
