package com.massivecraft.factions.iface;

import org.bukkit.Location;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Rel;

/**
 * Interface to determine whether a player's faction claim should be allowed.
 *
 * @author Kane York
 *
 */
public interface FClaimChecker {
	/**
	 * Can this player claim land for this faction at this location?
	 *
	 * @param player
	 *            - player attempting the claim
	 * @param forFaction
	 *            - faction player is claiming for
	 * @param fromFaction
	 *            - faction player is claiming from (often Wilderness)
	 * @param location
	 *            - {@link org.bukkit.Location} the claim was done at
	 * @return ClaimResult describing the result of the claim attempt
	 */
	public ClaimResult checkClaim(FPlayer player, Faction forFaction,
			Faction fromFaction, Location location);

	/**
	 * Class to describe the result of a claim attempt
	 */
	public static class ClaimResult {
		private boolean result;
		private boolean notifyOthers;
		private String message;

		// Start public methods

		/**
		 * Should this claim attempt be allowed?
		 * @return true if allowed, false if blocked
		 */
		public boolean isAllowed() {
			return this.result;
		}

		/**
		 * Should this claim attempt notify the members of the faction whose land
		 * the player attempted to claim from, given Conf.notifyEnemyFactionOnClaim?
		 * @return true if should notify, false if should not
		 */
		public boolean shouldNotifyOthers() {
			return this.notifyOthers;
		}

		/**
		 * Message to give to the player attempting the claim
		 * @return Minecraft-formatted message
		 */
		public String getMessage() {
			return this.message;
		}

		/**
		 * Create a custom ClaimResult with the specified result and
		 * message, which is run through {@link com.massivecraft.factions.zcore.util.TextUtil#parse(String)}.
		 *
		 * @param success - the value that will be returned from isAllowed()
		 * @param notifyOthers - whether this claim attempt should notify the land owner
		 * @param message - Message to be delivered to the player, after being run through TextUtils.parse()
		 *
		 * @return ClaimResult with specified values
		 */
		public static ClaimResult create(boolean success, boolean notifyOthers, String message) {
			return new ClaimResult(success, notifyOthers, message);
		}

		/**
		 * Create a custom ClaimResult with the specified result and
		 * message, which has already been run through {@link com.massivecraft.factions.zcore.util.TextUtil#parse(String, Object...)}.
		 * @param success - the value that will be returned from isAllowed()
		 * @param notifyOthers - whether this claim attempt should notify the land owner
		 * @param formattedMessage - Message to deliver to player, having been already run through TextUtils.parse(). If no message is desired, use the empty string.
		 * @return ClaimResult with specified values
		 */
		public static ClaimResult createPreFormatted(boolean success, boolean notifyOthers, String formattedMessage) {
			if (formattedMessage == null) {
				throw new IllegalArgumentException("Message cannot be null.");
			}
			ClaimResult cr = new ClaimResult(success, notifyOthers);
			cr.message = formattedMessage;
			return cr;
		}

		// Start predefined ClaimResults

		/**
		 * Something about this location is protected - usually a WorldGuard region.
		 */
		public static final ClaimResult fail_landProtect = new ClaimResult(false, false, "<b>This land is protected");
		/**
		 * This world is protected.
		 */
		public static final ClaimResult fail_worldProtect = new ClaimResult(false, false, "<b>Cannot claim land in this world.");
		/**
		 * This faction's power is less than or equal to its owned land.
		 */
		public static final ClaimResult fail_morePowerNeeded = new ClaimResult(false, false, "<b>You can't claim more land! You need more power!");
		/**
		 * This faction has reached the land cap
		 */
		public static final ClaimResult fail_maxLandReached = new ClaimResult(false, false, "<b>Limit reached. You can't claim more land!");
		/**
		 * Normal factions are unable to claim from others due to {@link com.massivecraft.factions.Conf.claimingFromOthersAllowed}.
		 */
		public static final ClaimResult fail_noEnemyClaim = new ClaimResult(false, false, "<b>You may not claim land from others.");
		/**
		 * Can't claim from inside enemy territory.
		 * <p />
		 * Notifies the enemy faction that you attempted to claim.
		 */
		public static final ClaimResult fail_insideEnemyTerr = new ClaimResult(false, true, "<b>You can't claim land from inside of enemy territory - start on the outside.");
		/**
		 * All land has to be connected to other lands due to {@link com.massivecraft.factions.Conf.claimsMustBeConnected}.
		 */
		public static final ClaimResult fail_mustConnect = new ClaimResult(false, false, "<b>You can only claim additional land which is connected to your first claim!");
		/**
		 * All land has to connect, except for enemy land, due to {@link com.massivecraft.factions.Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction}.
		 */
		public static final ClaimResult fail_mustConnectWilderness = new ClaimResult(false, false, "<b>You can only claim additional land which is connected to your first claim, or controlled by another faction!");
		/**
		 * Admins can do anything.
		 */
		public static final ClaimResult success_admin = new ClaimResult(true, false, "<g>Claim succeeded due to admin privileges.");
		/**
		 * Land was claimed from enemy territory.
		 * <p />
		 * Notifies the enemy faction that you attempted to claim.
		 */
		public static final ClaimResult success_fromEnemy = new ClaimResult(true, true, "");
		/**
		 * No failure conditions met
		 */
		public static final ClaimResult success_default = new ClaimResult(true,	false, "");

		// Start non-static ClaimResult factory methods

		/**
		 * Construct a ClaimResult for the case where this land was already claimed for your faction.
		 *
		 * @param ourFaction - colored name of land owner via Faction.describeTo(FPlayer, true)
		 */
		public static ClaimResult fail_sameFaction(String ourFaction) {
			return createPreFormatted(false, false, P.p.txt.parse("%s<i> already own this land.", ourFaction));
		}

		/**
		 * Construct a ClaimResult for the case where the enemy faction can keep this land because of their power.
		 * <p />
		 * Notifies the enemy faction that you attempted to claim.
		 * @param otherFaction - colored name of land owner via Faction.getTag(FPlayer)
		 */
		public static ClaimResult fail_powerfulEnemy(String otherFaction) {
			return createPreFormatted(false, true, P.p.txt.parse("%s<i> owns this land and is strong enough to keep it.", otherFaction));
		}

		/**
		 * Construct a ClaimResult for the case where your faction needs more people to meet the minimum member requirement.
		 * 
		 * @param minMembers - minimum members to claim land, as per Conf.claimsRequireMinFactionMembers
		 */
		public static final ClaimResult fail_notEnoughMembers(int minMembers) {
			return createPreFormatted(false, false, P.p.txt.parse("Factions must have at least <h>%s<b> members to claim land.", Integer.toString(minMembers)));
		}

		/**
		 * Construct a ClaimResult for the case where your relationship with the owning faction is too high.
		 *
		 * @param relation - {@link com.massivecraft.factions.struct.Rel}ation between the factions
		 */
		public static final ClaimResult fail_relation(Rel relation) {
			return createPreFormatted(false, false, P.p.txt.parse("<b>You may not claim land from <i>%s<b>.", relation.getDescFactionMany()));
		}

		// Start private constructors

		private ClaimResult(boolean success, boolean notifyOthers) {
			this.result = success;
			this.notifyOthers = notifyOthers;
		}

		private ClaimResult(boolean success, boolean notifyOthers, String unformatted) {
			this(success, notifyOthers);
			this.message = P.p.txt.parse(unformatted);
		}
	}
}