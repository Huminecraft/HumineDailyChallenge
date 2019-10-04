package fr.humine.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import fr.humine.main.ChallengeMain;
import fr.humine.utils.Challenger;
import fr.humine.utils.pass.ChallengePass;

public class ChallengerQuitPassEvent implements Listener
{

	public void onQuit(InventoryCloseEvent event) {
		if(event.getView().getTitle().startsWith(ChallengePass.SHOPNAME)) {
			Challenger challenger = ChallengeMain.getInstance().getBankChallenger().getChallenger((Player) event.getPlayer());
			challenger.getChallengePass().closeShop();
		}
	}
}
