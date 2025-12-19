package com.rsmnarts.valoo.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class json {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private json() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Converts an object to its JSON string representation.
	 *
	 * @param object The object to marshal.
	 * @return The JSON string.
	 */
	public static byte[] Marshal(Object object) {
		try {
			return objectMapper.writeValueAsBytes(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to marshal object to JSON", e);
		}
	}

	/**
	 * Converts an object to its JSON string representation.
	 *
	 * @param object The object to marshal.
	 * @return The JSON string.
	 */
	public static String MarshalToString(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to marshal object to JSON", e);
		}
	}

	/**
	 * Converts a JSON string to an object of the specified type.
	 *
	 * @param json  The JSON string.
	 * @param clazz The class of the object.
	 * @param <T>   The type of the object.
	 * @return The unmarshalled object.
	 */
	public static <T> T Unmarshal(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to unmarshal JSON to object", e);
		}
	}

	/**
	 * Converts a JSON string to an object of the specified type reference.
	 * Useful for generic types like List<MyObject>.
	 *
	 * @param json          The JSON string.
	 * @param typeReference The type reference of the object.
	 * @param <T>           The type of the object.
	 * @return The unmarshalled object.
	 */
	public static <T> T Unmarshal(String json, TypeReference<T> typeReference) {
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to unmarshal JSON to object", e);
		}
	}
}
