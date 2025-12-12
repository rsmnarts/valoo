package com.rsmnarts.valoo.infrastructure.client.dto.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RiotApiError {
	@JsonProperty("httpStatus")
	private int httpStatus;

	@JsonProperty("errorCode")
	private String errorCode;

	@JsonProperty("message")
	private String message;

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
