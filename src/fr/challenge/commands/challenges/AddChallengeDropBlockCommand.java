package fr.challenge.commands.challenges;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import fr.challenge.main.ChallengeMain;
import fr.challenge.main.ChallengeUtils;
import fr.challenge.utils.Challenger;
import fr.challenge.utils.challenges.ChallengeDropBlock;

public class AddChallengeDropBlockCommand implements CommandExecutor{

	private static final String COMMAND = "/addchallengedropblock <title> <ItemToDrop> <amount> [premium] [description]";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		boolean hebdo = Arrays.asList(args).contains("HEBDOCHALLENGE");
		if(hebdo)
			args[args.length-1] = "";
		
		if(args.length < 3) {
			ChallengeMain.sendMessage(sender, "Argument insuffisant");
			ChallengeMain.sendMessage(sender, COMMAND);
			return false;
		}
		
		Material item = ChallengeUtils.getItem(args[1]);
		if(item == null) {
			ChallengeMain.sendMessage(sender, "ItemToDrop invalide");
			ChallengeMain.sendMessage(sender, COMMAND);
			return false;
		}
		
		if(!ChallengeUtils.isNumber(args[2])) {
			ChallengeMain.sendMessage(sender, "Amount invalide");
			ChallengeMain.sendMessage(sender, COMMAND);
			return false;
		}
		
		boolean premium = false;
		if(args.length >= 4) {
			if(args[3].equalsIgnoreCase("true"))
				premium = true;
		}
		
		String description = "";
		if(args.length >= 5) {
			for(int i = 4; i < args.length; i++)
				description += args[i] + " ";
		}
		
		ChallengeDropBlock challenge = new ChallengeDropBlock(args[0], description, new ItemStack(item), Integer.parseInt(args[2]), premium);
		
		if(!hebdo) {
			ChallengeMain.getDailyChallenge().add(challenge);
			for(Challenger c : ChallengeMain.getInstance().getBankChallenger().getChallengers())
				c.updateDailyChallenge(ChallengeMain.getDailyChallenge());
		}else {
			ChallengeMain.getHebdoChallenge().add(challenge);
			for(Challenger c : ChallengeMain.getInstance().getBankChallenger().getChallengers())
				c.updateHebdoChallenge(ChallengeMain.getHebdoChallenge());
		}
		
		
		ChallengeMain.sendMessage(sender, "ChallengeDropBlock " + args[0] + " ajoute !");
		return true;
	}

}
