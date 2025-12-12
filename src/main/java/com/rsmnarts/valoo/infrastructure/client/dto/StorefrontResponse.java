package com.rsmnarts.valoo.infrastructure.client.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class StorefrontResponse {

	@JsonProperty("FeaturedBundle")
	private FeaturedBundle featuredBundle;

	@JsonProperty("SkinsPanelLayout")
	private SkinsPanelLayout skinsPanelLayout;

	@JsonProperty("UpgradeCurrencyStore")
	private UpgradeCurrencyStore upgradeCurrencyStore;

	@JsonProperty("AccessoryStore")
	private AccessoryStore accessoryStore;

	@JsonProperty("BonusStore")
	private BonusStore bonusStore;

	@Data
	public static class FeaturedBundle {
		@JsonProperty("Bundle")
		private Bundle bundle;
		@JsonProperty("Bundles")
		private List<Bundle> bundles;
		@JsonProperty("BundleRemainingDurationInSeconds")
		private long bundleRemainingDurationInSeconds;
	}

	@Data
	public static class Bundle {
		@JsonProperty("ID")
		private String id;
		@JsonProperty("DataAssetID")
		private String dataAssetID;
		@JsonProperty("CurrencyID")
		private String currencyID;
		@JsonProperty("Items")
		private List<Item> items;
		@JsonProperty("ItemOffers")
		private List<ItemOffer> itemOffers;
		@JsonProperty("TotalBaseCost")
		private Map<String, Integer> totalBaseCost;
		@JsonProperty("TotalDiscountedCost")
		private Map<String, Integer> totalDiscountedCost;
		@JsonProperty("TotalDiscountPercent")
		private double totalDiscountPercent;
		@JsonProperty("DurationRemainingInSeconds")
		private long durationRemainingInSeconds;
		@JsonProperty("WholesaleOnly")
		private boolean wholesaleOnly;
	}

	@Data
	public static class Item {
		@JsonProperty("Item")
		private ItemDetails item;
		@JsonProperty("BasePrice")
		private int basePrice;
		@JsonProperty("CurrencyID")
		private String currencyID;
		@JsonProperty("DiscountPercent")
		private double discountPercent;
		@JsonProperty("DiscountedPrice")
		private int discountedPrice;
		@JsonProperty("IsPromoItem")
		private boolean isPromoItem;
	}

	@Data
	public static class ItemDetails {
		@JsonProperty("ItemTypeID")
		private String itemTypeID;
		@JsonProperty("ItemID")
		private String itemID;
		@JsonProperty("Amount")
		private int amount;
	}

	@Data
	public static class ItemOffer {
		@JsonProperty("BundleItemOfferID")
		private String bundleItemOfferID;
		@JsonProperty("Offer")
		private Offer offer;
		@JsonProperty("DiscountPercent")
		private double discountPercent;
		@JsonProperty("DiscountedCost")
		private Map<String, Integer> discountedCost;
	}

	@Data
	public static class Offer {
		@JsonProperty("OfferID")
		private String offerID;
		@JsonProperty("IsDirectPurchase")
		private boolean isDirectPurchase;
		@JsonProperty("StartDate")
		private String startDate;
		@JsonProperty("Cost")
		private Map<String, Integer> cost;
		@JsonProperty("Rewards")
		private List<Reward> rewards;
	}

	@Data
	public static class Reward {
		@JsonProperty("ItemTypeID")
		private String itemTypeID;
		@JsonProperty("ItemID")
		private String itemID;
		@JsonProperty("Quantity")
		private int quantity;
	}

	@Data
	public static class SkinsPanelLayout {
		@JsonProperty("SingleItemOffers")
		private List<String> singleItemOffers;
		@JsonProperty("SingleItemStoreOffers")
		private List<SingleItemStoreOffer> singleItemStoreOffers;
		@JsonProperty("SingleItemOffersRemainingDurationInSeconds")
		private long singleItemOffersRemainingDurationInSeconds;
	}

	@Data
	public static class SingleItemStoreOffer {
		@JsonProperty("OfferID")
		private String offerID;
		@JsonProperty("IsDirectPurchase")
		private boolean isDirectPurchase;
		@JsonProperty("StartDate")
		private String startDate;
		@JsonProperty("Cost")
		private Map<String, Integer> cost;
		@JsonProperty("Rewards")
		private List<Reward> rewards;
	}

	@Data
	public static class UpgradeCurrencyStore {
		@JsonProperty("UpgradeCurrencyOffers")
		private List<UpgradeCurrencyOffer> upgradeCurrencyOffers;
	}

	@Data
	public static class UpgradeCurrencyOffer {
		@JsonProperty("OfferID")
		private String offerID;
		@JsonProperty("StorefrontItemID")
		private String storefrontItemID;
		@JsonProperty("Offer")
		private Offer offer;
		@JsonProperty("DiscountedPercent")
		private double discountedPercent;
	}

	@Data
	public static class AccessoryStore {
		@JsonProperty("AccessoryStoreOffers")
		private List<AccessoryStoreOffer> accessoryStoreOffers;
		@JsonProperty("AccessoryStoreRemainingDurationInSeconds")
		private long accessoryStoreRemainingDurationInSeconds;
		@JsonProperty("StorefrontID")
		private String storefrontID;
	}

	@Data
	public static class AccessoryStoreOffer {
		@JsonProperty("Offer")
		private Offer offer;
		@JsonProperty("ContractID")
		private String contractID;
	}

	@Data
	public static class BonusStore {
		@JsonProperty("BonusStoreOffers")
		private List<BonusStoreOffer> bonusStoreOffers;
		@JsonProperty("BonusStoreRemainingDurationInSeconds")
		private long bonusStoreRemainingDurationInSeconds;
	}

	@Data
	public static class BonusStoreOffer {
		@JsonProperty("BonusOfferID")
		private String bonusOfferID;
		@JsonProperty("Offer")
		private Offer offer;
		@JsonProperty("DiscountPercent")
		private double discountPercent;
		@JsonProperty("DiscountCosts")
		private Map<String, Integer> discountCosts;
		@JsonProperty("IsSeen")
		private boolean isSeen;
	}
}
