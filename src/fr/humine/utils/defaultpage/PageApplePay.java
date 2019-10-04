package fr.humine.utils.defaultpage;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import fr.humine.utils.Challenger;
import fr.humine.utils.ItemShop;

public abstract class PageApplePay {

	public static final String NAME = "PREMIUM SHOP";
	public static final int SIZE = (9*4);
	public static int PRIZE = 0;
	public static Inventory inv;
	
	public static void openShop(Challenger challenger) {
		inv = Bukkit.createInventory(null, SIZE, NAME);
		inv.setItem(SIZE - (9*2) - 5, ItemShop.applePay(challenger, PRIZE));
		inv.setItem(SIZE-9, ItemShop.itemQuit());
		inv.setItem(SIZE-1, ItemShop.itemQuit());
		challenger.getPlayer().openInventory(inv);
	}
}