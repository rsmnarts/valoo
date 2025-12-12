package com.rsmnarts.valoo.presentation.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.rsmnarts.valoo.common.utils.json;
import com.rsmnarts.valoo.presentation.dto.ErrorDto;
import com.rsmnarts.valoo.presentation.dto.ResponseDto;

@RestControllerAdvice(basePackages = "com.rsmnarts.valoo.presentation.controller")
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		if (body instanceof ErrorDto) {
			return body;
		}

		if (request.getURI().getPath().contains("/v3/api-docs") || request.getURI().getPath().contains("/swagger-ui")) {
			return body;
		}

		if (body instanceof String) {
			return json.Marshal(ResponseDto.builder().data(body).build());
		}

		if (body instanceof ResponseDto) {
			return body;
		}

		return ResponseDto.builder()
				.data(body)
				.build();
	}
}
