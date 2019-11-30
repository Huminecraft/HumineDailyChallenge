package fr.challenge.events.defaultpage.menuaccueil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.challenge.utils.ItemShop;
import fr.challenge.utils.events.ihm.ClickItemMenuAccueilEvent;

public class ClickSurvivalPassEvent implements Listener{

	@EventHandler
	public void onClick(ClickItemMenuAccueilEvent event) {
		if(event.getItem().isSimilar(ItemShop.survivalPassItem())) {
			event.getChallenger().getChallengePass().openShop();
		}
	}
}
