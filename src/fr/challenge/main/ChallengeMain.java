package fr.challenge.main;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.challenge.commands.AddCustomHeadCosmetique;
import fr.challenge.commands.AddMaterialHatCosmetiqueCommand;
import fr.challenge.commands.AddPalierCommand;
import fr.challenge.commands.AddParticleCosmetiqueCommand;
import fr.challenge.commands.DailyChallengeLoadCommand;
import fr.challenge.commands.HebdoChallengeLoadCommand;
import fr.challenge.commands.PalierLoadCommand;
import fr.challenge.commands.ShowChallengePassCommand;
import fr.challenge.commands.ShowHebdoPageCommand;
import fr.challenge.commands.ShowMenuAccueil;
import fr.challenge.commands.ShowTokenCommand;
import fr.challenge.commands.TokenAddCommand;
import fr.challenge.commands.TokenRemoveCommand;
import fr.challenge.commands.challenges.AddAwardCommand;
import fr.challenge.commands.challenges.AddChallengeBiomeDiscoverCommand;
import fr.challenge.commands.challenges.AddChallengeBreakBlockCommand;
import fr.challenge.commands.challenges.AddChallengeDropBlockCommand;
import fr.challenge.commands.challenges.AddChallengeEnchantItemCommand;
import fr.challenge.commands.challenges.AddChallengeFishCommand;
import fr.challenge.commands.challenges.AddChallengeKillCommand;
import fr.challenge.commands.challenges.AddChallengeOpenChestCommand;
import fr.challenge.commands.challenges.AddChallengePlaceBlockCommand;
import fr.challenge.commands.challenges.ShowDailyChallengeCommand;
import fr.challenge.commands.challenges.ShowHedboChallengeCommand;
import fr.challenge.events.ChallengerQuitPassEvent;
import fr.challenge.events.CreateChallengerEvent;
import fr.challenge.events.QuitChallengerEvent;
import fr.challenge.events.challenges.ChallengeBiomeDiscoverEvent;
import fr.challenge.events.challenges.ChallengeBreakBlockEvent;
import fr.challenge.events.challenges.ChallengeDropBlockEvent;
import fr.challenge.events.challenges.ChallengeEnchantItemEvent;
import fr.challenge.events.challenges.ChallengeFishEvent;
import fr.challenge.events.challenges.ChallengeKillEvent;
import fr.challenge.events.challenges.ChallengeOpenChestEvent;
import fr.challenge.events.challenges.ChallengePlaceBlockEvent;
import fr.challenge.events.challenges.GiveAwardEvent;
import fr.challenge.events.challenges.GiveAwardPalierEvent;
import fr.challenge.events.defaultpage.hebdopage.ClickHebdoItemEvent;
import fr.challenge.events.defaultpage.menuaccueil.ClickChangeHebdoEvent;
import fr.challenge.events.defaultpage.menuaccueil.ClickDailyEvent;
import fr.challenge.events.defaultpage.menuaccueil.ClickHebdoEvent;
import fr.challenge.events.defaultpage.menuaccueil.ClickSurvivalPassEvent;
import fr.challenge.events.defaultpage.menudaily.ClickQuitEvent;
import fr.challenge.events.defaultpage.pageapplepay.ClickApplePayEvent;
import fr.challenge.events.defaultpage.pageapplepay.ClickQuitButtonEvent;
import fr.challenge.events.defaultpage.pageapplepay.OpenPageApplePayEvent;
import fr.challenge.events.defaultpage.pageunlockpalier.ClickUnlockPalierEvent;
import fr.challenge.events.defaultpage.pageunlockpalier.OpenPageUnlockPalierEvent;
import fr.challenge.utils.BankChallenger;
import fr.challenge.utils.BankCosmetique;
import fr.challenge.utils.Challenger;
import fr.challenge.utils.challenges.Challenge;
import fr.challenge.utils.files.LoadSystem;
import fr.challenge.utils.files.SaveSystem;
import fr.challenge.utils.menu.MenuApplePay;
import fr.challenge.utils.pass.ChallengePass;
import fr.challenge.utils.pass.Page;
import humine.utils.cosmetiques.Cosmetique;

public class ChallengeMain extends JavaPlugin{

	private static ChallengeMain instance;
	private static ChallengePass passMain;
	private static List<Challenge> dailyChallenge;
	private static List<Challenge> HebdoChallenge;
	
	private int currentWeek;
	private LocalDate currentDailyChallengeDate;
	private LocalDate currentHebdoChallengeDate;
	private BankChallenger bankChallenger;
	private BankCosmetique bankCosmetique;

	public final File FILEPALIER = new File(getDataFolder(), "paliers.hc");
	public final File FOLDERDAILYCHALLENGE = new File(getDataFolder(), "DailyChallenge");
	public final File FOLDERHEBDOCHALLENGE = new File(getDataFolder(), "HebdoChallenge");
	public final File FOLDERCHALLENGER = new File(getDataFolder(), "Challengers");
	public final File FOLDERDATA = new File(getDataFolder(), "Data");
	
	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		
		instance = this;
		passMain = new ChallengePass();
		dailyChallenge = new ArrayList<>();
		HebdoChallenge = new ArrayList<>();
		currentWeek = 1;
		
		this.bankChallenger = new BankChallenger();
		this.bankCosmetique = new BankCosmetique();

		files();
		commands();
		events();
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			File folder = new File(FOLDERCHALLENGER, player.getUniqueId().toString());
			if(!folder.exists()) {
				Challenger challenger = new Challenger(player);
				bankChallenger.addChallenger(challenger);
				continue;
			}

			try
			{
				Challenger challenger = LoadSystem.loadChallenger(folder);
				bankChallenger.addChallenger(challenger);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run()
			{
				if(currentDailyChallengeDate.isBefore(LocalDate.now())) {
					currentDailyChallengeDate = LocalDate.now();
					getServer().dispatchCommand(getServer().getConsoleSender(), "dailyload");
				}
				
				if(LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY && currentHebdoChallengeDate.isBefore(LocalDate.now())) {
					currentHebdoChallengeDate = LocalDate.now();
					File hebdoData = new File(FOLDERDATA, "HebdoChallenge");
					try {
						SaveSystem.saveWeekHebdoChallenge(HebdoChallenge, hebdoData, currentWeek);
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
					for(Challenger challenger : bankChallenger.getChallengers()) {
						File f = new File(challenger.getChallengerFolder(), "HebdoChallenge");
						try {
							if(challenger.hasPremium())
								SaveSystem.saveWeekHebdoChallenge(challenger.getHebdoChallenge(), f, challenger.getCurrentHebdoWeek());
							else
								SaveSystem.saveWeekHebdoChallenge(HebdoChallenge, f, currentWeek);
						}
						catch (IOException e) {
							e.printStackTrace();
						}
						challenger.setCurrentHebdoWeek(currentWeek + 1); 
					}
					
					getServer().dispatchCommand(getServer().getConsoleSender(), "hebdoload");
					currentWeek++;
				}
				
			}
		}, 0L, (60 * 20));
	}
	
	@Override
	public void onDisable()
	{
		getConfig().set("currentdailydate", this.currentDailyChallengeDate.toString());
		getConfig().set("currenthebdodate", this.currentHebdoChallengeDate.toString());
		getConfig().set("semaine", this.currentWeek);
		if(!getConfig().contains("huminepass"))
			getConfig().set("huminepass", 0);
		saveConfig();
		
		for(Challenger challenger : bankChallenger.getChallengers()) {
			challenger.getLevelBar().dissociate();
			File folder = challenger.getChallengerFolder();
			folder.mkdirs();
			try
			{
				SaveSystem.saveChallenger(challenger, folder);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		File pageFolder = new File(FOLDERDATA, "Pages");
		File dailyFolder = new File(FOLDERDATA, "DailyChallenge");
		File hebdoFolder = new File(FOLDERDATA, "HebdoChallenge");
		File cosmetiqueFolder = new File(FOLDERDATA, "Cosmetiques");
		
		try {
			SaveSystem.savePages(passMain.getPages(), pageFolder);
			SaveSystem.saveChallenges(dailyChallenge, dailyFolder);
			SaveSystem.saveWeekHebdoChallenge(HebdoChallenge, hebdoFolder, currentWeek);
			SaveSystem.saveCosmetiques(bankCosmetique.getCosmetiques(), cosmetiqueFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void files() {
		try
		{
			if(!FILEPALIER.exists())
				FILEPALIER.createNewFile();
			
			if(!FOLDERDAILYCHALLENGE.exists())
				FOLDERDAILYCHALLENGE.mkdirs();
			
			if(!FOLDERHEBDOCHALLENGE.exists())
				FOLDERHEBDOCHALLENGE.mkdirs();
			
			if(!FOLDERCHALLENGER.exists())
				FOLDERCHALLENGER.mkdirs();
			
			if(!FOLDERDATA.exists())
				FOLDERDATA.mkdirs();
			
			if(getConfig().contains("currentdailydate")) {
				String date[] = getConfig().getString("currentdailydate").split("-");
				this.currentDailyChallengeDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
			}
			else {
				this.currentDailyChallengeDate = LocalDate.now();
			}
			
			if(getConfig().contains("currenthebdodate")) {
				String date[] = getConfig().getString("currenthebdodate").split("-");
				this.currentHebdoChallengeDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
			}
			else {
				this.currentHebdoChallengeDate = LocalDate.now();
			}
			
			if(getConfig().contains("huminepass")) {
				MenuApplePay.PRIZE = getConfig().getInt("huminepass");
			}
			else {
				MenuApplePay.PRIZE = 0;
			}
			
			if(getConfig().contains("semaine")) {
				this.currentWeek = getConfig().getInt("semaine");
			}
			
			File pageFolder = new File(FOLDERDATA, "Pages");
			File dailyFolder = new File(FOLDERDATA, "DailyChallenge");
			File hebdoFolder = new File(FOLDERDATA, "HebdoChallenge");
			File cosmetiqueFolder = new File(FOLDERDATA, "Cosmetiques");
			try {
				if(pageFolder.exists()) {
					List<Page> pages = LoadSystem.loadPages(pageFolder);
					passMain.setPages(pages);
				}
				
				if(dailyFolder.exists()) {
					List<Challenge> dailyList = LoadSystem.loadChallenges(dailyFolder);
					dailyChallenge.addAll(dailyList);
				}
				
				if(hebdoFolder.exists()) {
					List<Challenge> hebdoList = LoadSystem.loadWeekHebdoChallenge(hebdoFolder, currentWeek);
					HebdoChallenge.addAll(hebdoList);
				}
				
				if(cosmetiqueFolder.exists()) {
					List<Cosmetique> cosmetiqueList = LoadSystem.loadCosmetique(cosmetiqueFolder);
					bankCosmetique.setCosmetiques(cosmetiqueList);
				}
				
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void events() {
		//initialize custom events
		this.getServer().getPluginManager().registerEvents(new InitializeEvents(), this);
		
		//fr.challenge.events
		this.getServer().getPluginManager().registerEvents(new ChallengerQuitPassEvent(), this);
		this.getServer().getPluginManager().registerEvents(new CreateChallengerEvent(), this);
		this.getServer().getPluginManager().registerEvents(new QuitChallengerEvent(), this);
		
		//fr.challenge.events.challenges
		this.getServer().getPluginManager().registerEvents(new ChallengeBiomeDiscoverEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeBreakBlockEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeDropBlockEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeEnchantItemEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeFishEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeKillEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengeOpenChestEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ChallengePlaceBlockEvent(), this);
		this.getServer().getPluginManager().registerEvents(new GiveAwardEvent(), this);
		this.getServer().getPluginManager().registerEvents(new GiveAwardPalierEvent(), this);
		
		//fr.challenge.events.defaultpage.hebdopage
		this.getServer().getPluginManager().registerEvents(new fr.challenge.events.defaultpage.hebdopage.ClickQuitButtonEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickHebdoItemEvent(), this);
		
		//fr.challenge.events.defaultpage.menuaccueil
		this.getServer().getPluginManager().registerEvents(new ClickChangeHebdoEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickDailyEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickHebdoEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickSurvivalPassEvent(), this);
		
		//fr.challenge.events.defaultpage.menudaily
		this.getServer().getPluginManager().registerEvents(new ClickQuitEvent(), this);
		
		//fr.challenge.events.defaultpage.menuhebdo
		this.getServer().getPluginManager().registerEvents(new fr.challenge.events.defaultpage.menuhebdo.ClickQuitEvent(), this);
		
		//fr.challenge.events.defaultpage.pageapplepay
		this.getServer().getPluginManager().registerEvents(new ClickApplePayEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickQuitButtonEvent(), this);
		this.getServer().getPluginManager().registerEvents(new OpenPageApplePayEvent(), this);
		
		//fr.challenge.events.defaultpage.pageunlockpalier
		this.getServer().getPluginManager().registerEvents(new fr.challenge.events.defaultpage.pageunlockpalier.ClickQuitButtonEvent(), this);
		this.getServer().getPluginManager().registerEvents(new ClickUnlockPalierEvent(), this);
		this.getServer().getPluginManager().registerEvents(new OpenPageUnlockPalierEvent(), this);
	}
	
	private void commands() {
		this.getCommand("token").setExecutor(new ShowTokenCommand());
		this.getCommand("pass").setExecutor(new ShowChallengePassCommand());
		this.getCommand("hebdomenu").setExecutor(new ShowHebdoPageCommand());
		this.getCommand("createpalier").setExecutor(new AddPalierCommand());
		this.getCommand("palierload").setExecutor(new PalierLoadCommand());
		this.getCommand("dailyload").setExecutor(new DailyChallengeLoadCommand());
		this.getCommand("hebdoload").setExecutor(new HebdoChallengeLoadCommand());
		this.getCommand("palierparticlecosmetique").setExecutor(new AddParticleCosmetiqueCommand());
		this.getCommand("paliermaterialhatcosmetique").setExecutor(new AddMaterialHatCosmetiqueCommand());
		this.getCommand("addchallengefish").setExecutor(new AddChallengeFishCommand());
		
		this.getCommand("addchallengekill").setExecutor(new AddChallengeKillCommand());
		this.getCommand("addchallengeplaceblock").setExecutor(new AddChallengePlaceBlockCommand());
		this.getCommand("addchallengebreakblock").setExecutor(new AddChallengeBreakBlockCommand());
		this.getCommand("addchallengebiomediscover").setExecutor(new AddChallengeBiomeDiscoverCommand());
		this.getCommand("addchallengeopenchest").setExecutor(new AddChallengeOpenChestCommand());
		this.getCommand("addchallengedropblock").setExecutor(new AddChallengeDropBlockCommand());
		this.getCommand("addchallengeenchantitem").setExecutor(new AddChallengeEnchantItemCommand());
		
		this.getCommand("paliercustomhatcosmetique").setExecutor(new AddCustomHeadCosmetique());
		this.getCommand("addAward").setExecutor(new AddAwardCommand());
		this.getCommand("dailychallenge").setExecutor(new ShowDailyChallengeCommand());
		this.getCommand("hebdochallenge").setExecutor(new ShowHedboChallengeCommand());
		this.getCommand("addtoken").setExecutor(new TokenAddCommand());
		this.getCommand("removetoken").setExecutor(new TokenRemoveCommand());
		
		this.getCommand("challenge").setExecutor(new ShowMenuAccueil());
	}
	
	public static void sendMessage(CommandSender sender, String message) {
		String prefix = ChatColor.AQUA + "[Challenge]" + ChatColor.RESET;
		sender.sendMessage(prefix + " " + message);
	}
	
	public static ChallengeMain getInstance() {
		return instance;
	}
	
	public static ChallengePass getPassMain()
	{
		return passMain;
	}
	
	public BankChallenger getBankChallenger() {
		return bankChallenger;
	}
	
	public BankCosmetique getBankCosmetique() {
		return bankCosmetique;
	}
	
	public static List<Challenge> getDailyChallenge() {
		return dailyChallenge;
	}
	
	public static List<Challenge> getHebdoChallenge()
	{
		return HebdoChallenge;
	}
	public LocalDate getCurrentDailyChallengeDate()
	{
		return currentDailyChallengeDate;
	}
	
	public LocalDate getCurrentHebdoChallengeDate()
	{
		return currentHebdoChallengeDate;
	}
	
	public int getCurrentWeek()
	{
		return currentWeek;
	}
}
