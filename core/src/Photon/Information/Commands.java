package Photon.Information;

import Photon.gae;

public class Commands {
    public String commandName, logMessage;
    public int timesExecuted = 0;
    public boolean showed = false, isExecutable = false;

    public void CantBeExecuted(){
        resetExecutedTimes();
        this.isExecutable = false;
    }

    public void resetExecutedTimes() { this.timesExecuted = 0;}

    public void setShowed(boolean showed){this.showed = showed;}

    public void setCommandName(String commandName){
        if(commandName.startsWith("/"))
            commandName = gae.hydrogen.RemoveFirstChar(commandName, 1);
        this.commandName = commandName;
    }

    public void reset(){
        this.commandName = "NULL";
        this.logMessage = "NULL";
        this.timesExecuted = 0;
        this.showed = false;
        this.isExecutable = false;
    }

    public void Executed(String commandName){ this.timesExecuted++; this.isExecutable = true; this.commandName = commandName;}

    public void setLogMessage(String logMessage){ this.logMessage = logMessage; this.isExecutable = true; }


}
