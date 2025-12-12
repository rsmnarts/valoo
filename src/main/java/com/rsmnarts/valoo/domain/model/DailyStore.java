package com.rsmnarts.valoo.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStore {
	private Long expireAt;
	private List<DailyStoreItem> items;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DailyStoreItem {
		private String uuid;
		private String displayName;
		private String levelItem;
		private String displayIcon;
		private String streamedVideo;
		private String assetPath;
		private String cost;
		private List<Level> levels;

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		public static class Level {
			private String uuid;
			private String displayName;
			private String displayIcon;
			private String streamedVideo;
		}
	}
}
