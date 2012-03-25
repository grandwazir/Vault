package net.milkbowl.vault.economy.plugins;

import is.currency.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.iCo6.iConomy;
import com.iCo6.system.Accounts;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import name.richardson.james.bukkit.exchequer.AccountRecord;
import name.richardson.james.bukkit.exchequer.Exchequer;
import name.richardson.james.bukkit.exchequer.ExchequerHandler;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.milkbowl.vault.economy.plugins.Economy_CurrencyCore.EconomyServerListener;

public class Economy_Exchequer implements Economy {

  
  private static final Logger log = Logger.getLogger("Minecraft");
  
  private Plugin plugin;
  private Exchequer instance;
  private ExchequerHandler handler;
  private final String name = "Exchequer";
  
  public Economy_Exchequer(Plugin plugin) {
    this.plugin = plugin;
    Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);

    // Load Plugin in case it was loaded before
    if(instance == null) {
        Plugin exchequerPlugin = plugin.getServer().getPluginManager().getPlugin(name);
        if(exchequerPlugin != null && exchequerPlugin.getClass().getName().equals("name.richardson.james.bukkit.Exchequer")) {
            this.instance = (Exchequer) exchequerPlugin;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), name));  
        }
    }
  }
  
  public class EconomyServerListener implements Listener {
    Economy_Exchequer economy = null;

    public EconomyServerListener(Economy_Exchequer economy) {
        this.economy = economy;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (economy.plugin == null) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(name);

            if (plugin != null && plugin.isEnabled() && plugin.getClass().getName().equals("name.richardson.james.bukkit.Exchequer")) {
                economy.instance = (Exchequer) plugin;
                economy.handler = economy.instance.getHandler(Economy_Exchequer.class);
                log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), name));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (economy.plugin != null) {
            if (event.getPlugin().getDescription().getName().equals(name)) {
                economy.plugin = null;
                log.info(String.format("[%s][Economy] %s unhooked.", plugin.getDescription().getName(), name));
            }
        }
    }
}
  
  @Override
  public boolean isEnabled() {
    if (this.instance != null) return true;
    return false;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public String format(double amount) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String currencyNamePlural() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String currencyNameSingular() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasAccount(String playerName) {
    AccountRecord account = handler.getPlayerPersonalAccount(playerName);
    return account != null;
  }

  @Override
  public double getBalance(String playerName) {
    AccountRecord account = handler.getPlayerPersonalAccount(playerName);
    if (account != null) {
      return account.getBalance().doubleValue();
    } else {
      return 0.0;
    }
  }

  @Override
  public boolean has(String playerName, double amount) {
    AccountRecord account = handler.getPlayerPersonalAccount(playerName);
    if (account != null) {
      return (account.getBalance().doubleValue() >= amount);
    } else {
      return false;
    }
  }

  @Override
  public EconomyResponse withdrawPlayer(String playerName, double amount) {
    BigDecimal i = new BigDecimal(amount);
    AccountRecord account = handler.getPlayerPersonalAccount(playerName);
    if (account != null) {
      if (account.contains(i)) {
        account.subtract(i);
        handler.save(account);
        return new EconomyResponse(amount, account.getBalance().doubleValue(), ResponseType.SUCCESS, null);
      } else {
        return new EconomyResponse(0.0, account.getBalance().doubleValue(), ResponseType.FAILURE, "Insufficient funds");
      }
    } else {
      return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "That account does not exist");
    }
  }

  @Override
  // this is the same in case someone attempts to deposit a negative sum of money
  public EconomyResponse depositPlayer(String playerName, double amount) {
    BigDecimal i = new BigDecimal(amount);
    AccountRecord account = handler.getPlayerPersonalAccount(playerName);
    if (account != null) {
      if (account.contains(i)) {
        account.add(i);
        handler.save(account);
        return new EconomyResponse(amount, account.getBalance().doubleValue(), ResponseType.SUCCESS, null);
      } else {
        return new EconomyResponse(0.0, account.getBalance().doubleValue(), ResponseType.FAILURE, "Insufficient funds");
      }
    } else {
      return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "That account does not exist");
    }
  }

  @Override
  public EconomyResponse createBank(String name, String player) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse deleteBank(String name) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse bankBalance(String name) {
    try {
      int id = Integer.parseInt(name);
      AccountRecord account = handler.getAccount(id);
      return new EconomyResponse(0.0, account.getBalance().doubleValue(), ResponseType.SUCCESS, null);
    } catch (NumberFormatException exception) {
      return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, this.instance.getMessage("specify-valid-account"));
    }
  }

  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse isBankOwner(String name, String playerName) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public EconomyResponse isBankMember(String name, String playerName) {
    return new EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "Not implemented yet.");
  }

  @Override
  public List<String> getBanks() {
    return null;
  }

  @Override
  public boolean createPlayerAccount(String playerName) {
    if (!handler.isPlayerRegistered(playerName)) handler.registerPlayer(playerName);
    return true;
  }

}
