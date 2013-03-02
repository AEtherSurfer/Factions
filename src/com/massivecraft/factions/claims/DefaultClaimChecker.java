package com.massivecraft.factions.claims;

import org.bukkit.Location;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;

public class DefaultClaimChecker extends BaseClaimChecker {
	/**
	 * Default claim checking behavior.
	 */
	@Override
	public ClaimResult checkClaim(FPlayer player, Faction forFaction, Faction fromFaction, Location location) {
		ClaimResult baseResult = super.checkClaim(player, forFaction, fromFaction, location);
		if (baseResult == null) {
			if (fromFaction.isNormal())
			{
				return ClaimResult.success_fromEnemy; // this will notify enemy faction, if turned on in config
			}
			return ClaimResult.success_default;
		}
		// Admin overrides all except world protect - protected worlds should not change
		if (player.hasAdminMode() && (baseResult != ClaimResult.fail_worldProtect)) {
			return ClaimResult.success_admin;
		}
		return baseResult;
	}
}