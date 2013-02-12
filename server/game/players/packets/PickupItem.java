package server.game.players.packets;

import server.Server;
import server.content.skills.misc.SkillHandler;
import server.event.CycleEvent;
import server.event.CycleEventContainer;
import server.event.CycleEventHandler;
import server.game.items.GlobalDropsHandler;
import server.game.players.Client;
import server.game.players.PacketType;

/**
 * Pickup Item
 **/
public class PickupItem implements PacketType {

	@Override
	public void processPacket(final Client c, int packetType, int packetSize) {
		c.walkingToItem = false;
		c.pItemY = c.getInStream().readSignedWordBigEndian();
		c.pItemId = c.getInStream().readUnsignedWord();
		c.pItemX = c.getInStream().readSignedWordBigEndian();
		if (Math.abs(c.getX() - c.pItemX) > 25 || Math.abs(c.getY() - c.pItemY) > 25) {
			c.resetWalkingQueue();
			return;
		}
		GlobalDropsHandler.pickup(c, c.pItemId, c.pItemX, c.pItemY);
		c.getCombat().resetPlayerAttack();
		if(c.getX() == c.pItemX && c.getY() == c.pItemY) {
			c.getPA().sound(356);
			Server.itemHandler.removeGroundItem(c, c.pItemId, c.pItemX, c.pItemY, true);
		} else {
			c.walkingToItem = true;
			CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
				@Override
				public void execute(CycleEventContainer container) {
					if(!c.walkingToItem)
						container.stop();
					if(c.getX() == c.pItemX && c.getY() == c.pItemY) {
						 SkillHandler.resetallSkills(c);
						c.getPA().sound(356);
						Server.itemHandler.removeGroundItem(c, c.pItemId, c.pItemX, c.pItemY, true);
						container.stop();
					}
				}
				@Override
				public void stop() {
					c.walkingToItem = false;
				}
			}, 1);
		}

	}

}
