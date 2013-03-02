package com.massivecraft.factions.claims;

import org.bukkit.Location;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.iface.FClaimChecker;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.struct.FFlag;
import com.massivecraft.factions.struct.Rel;

public abstract class BaseClaimChecker implements FClaimChecker {
	/**
	 * Extending classes - do the following: ClaimResult baseResult =
	 * super.checkClaim(player, forFaction, fromFaction, location); Then, do
	 * your additional processing.
	 * <p />
	 * Note that <b>if the claim would succeed by default, null is returned.</b>
	 *
	 * @return ClaimResult - null if no problems, failing ClaimResult if there
	 *         are problems with the claim
	 */
	@Override
	public ClaimResult checkClaim(FPlayer player, Faction forFaction,
			Faction fromFaction, Location location) {
		if (Conf.worldGuardChecking
				&& Worldguard.checkForRegionsInChunk(location)) {
			return ClaimResult.fail_landProtect;
		}
		FLocation flocation = new FLocation(location);
		if (Conf.worldsNoClaiming.contains(flocation.getWorldName())) {
			return ClaimResult.fail_worldProtect;
		}
		if (forFaction == fromFaction) {
			return ClaimResult.fail_sameFaction(forFaction.describeTo(player,
					true));
		}
		if (forFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
			return ClaimResult
					.fail_notEnoughMembers(Conf.claimsRequireMinFactionMembers);
		}
		if (forFaction.getLandRounded() >= forFaction.getPowerRounded()) {
			return ClaimResult.fail_morePowerNeeded;
		}
		if (Conf.claimedLandsMax != 0
				&& forFaction.getLandRounded() >= Conf.claimedLandsMax
				&& !forFaction.getFlag(FFlag.INFPOWER)) {
			return ClaimResult.fail_morePowerNeeded;
		}
		if (!Conf.claimingFromOthersAllowed && fromFaction.isNormal()) {
			return ClaimResult.fail_noEnemyClaim;
		}
		if (!fromFaction.isNone()) {
			Rel relation = fromFaction.getRelationTo(forFaction);
			if (relation.isAtLeast(Rel.TRUCE)) {
				return ClaimResult.fail_relation(relation);
			}
		}
		if (Conf.claimsMustBeConnected
				&& forFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0
				&& !Board.isConnectedLocation(flocation, forFaction)
				&& !(Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction && fromFaction.isNormal())) {
			if (Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction) {
				return ClaimResult.fail_mustConnect;
			} else {
				return ClaimResult.fail_mustConnectWilderness;
			}
		}
		if (fromFaction.isNormal()) {
			if (!fromFaction.hasLandInflation()) {
				return ClaimResult.fail_powerfulEnemy(fromFaction.getTag(player));
			}
			if (!Board.isBorderLocation(flocation)) {
				return ClaimResult.fail_insideEnemyTerr;
			}
		}
		return null;
	}
}