package fr.humine.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.humine.main.ChallengeMain;
import fr.humine.main.ChallengeUtils;
import fr.humine.utils.Challenger;
import fr.humine.utils.pass.Palier;
import humine.utils.cosmetiques.Cosmetique;

public class AddPalierCommand implements CommandExecutor
{
	private static final String COMMAND = "/createpalier <numPalier> <MaterialPresentation> <priceHumis> <awardHumis> <awardExp> <token> [premium] [numeroCosmetique]";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player)) {
			ChallengeMain.sendMessage(sender, "Vous devez etre un joueur");
			return false;
		}
		
		if(args.length < 6) {
			ChallengeMain.sendMessage(sender, "Argument insuffisant");
			ChallengeMain.sendMessage(sender, COMMAND);
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[0])) {
			ChallengeMain.sendMessage(sender, "Numero de palier invalide");
			return false;
		}
		
		Material itemPresentation = ChallengeUtils.getItem(args[1]);
		if(itemPresentation == null) {
			ChallengeMain.sendMessage(sender, "MaterialPresentation invalide");
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[2])) {
			ChallengeMain.sendMessage(sender, "priceHumis invalide");
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[3])) {
			ChallengeMain.sendMessage(sender, "awardHumis invalide");
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[4])) {
			ChallengeMain.sendMessage(sender, "awardExp invalide");
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[5])) {
			ChallengeMain.sendMessage(sender, "Token invalide");
			return false;
		}
		
		boolean premium = false;
		if(args.length >= 7 && args[6].equalsIgnoreCase("true")) {
			premium = true;
		}
		
		Cosmetique cosmetique = null;
		if(args.length >= 8) {
			cosmetique = ChallengeMain.getInstance().getBankCosmetique().getCosmetique(args[7]);
		}
		
		int numPalier = Integer.parseInt(args[0]);
		int priceHumis = Integer.parseInt(args[2]);
		int awardHumis = Integer.parseInt(args[3]);
		int awardExp = Integer.parseInt(args[4]);
		int tokenPass = Integer.parseInt(args[5]);
		Palier palier = new Palier(numPalier, new ItemStack(itemPresentation), priceHumis, awardHumis, awardExp, tokenPass, cosmetique, false, premium);
				
		ChallengeMain.getPassMain().addPalier(palier);
		for(Challenger c : ChallengeMain.getInstance().getBankChallenger().getChallengers())
			c.getChallengePass().update(ChallengeMain.getPassMain());
		ChallengeMain.sendMessage(sender, "Palier n" + args[0] + " " + palier.getType().toString() + " ajoute !");
		return true;
	}

}
