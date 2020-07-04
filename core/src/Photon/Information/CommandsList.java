package Photon.Information;

import Photon.gae;
import arc.struct.Array;



public class CommandsList {

    public String commandsName, commandsDescription, commandsRunConfirmationMessage;
    public int commandsIconASCII;
    public Array<ChildCommandsList> childCommandsList = new Array<ChildCommandsList>();

    public void addCommandList(String commandName, String commandsDescription, int iconASCII, String commandsRunConfirmationMessage, Array<ChildCommandsList> childCommandsList) {
        commandName = gae.hydrogen.capitalizeFirstLetter(commandName);
        commandsRunConfirmationMessage = commandsRunConfirmationMessage;
        this.commandsRunConfirmationMessage =  commandsRunConfirmationMessage;
        this.childCommandsList = childCommandsList;
        this.commandsName = commandName;
        this.commandsDescription = commandsDescription;
        this.commandsIconASCII = iconASCII;
    }

    public static class ChildCommandsList{
        public final String parentCommandsName;
        public String childCommandsName, childCommandsRunPrompt;

        public ChildCommandsList(String parentCommandsName){
            this.parentCommandsName = parentCommandsName;
        }
        public String getChildCommandsName(){ return this.childCommandsName; }

        public void setChildCommandsName(String childCommandsName){
            childCommandsName = this.parentCommandsName.replace(" ", "-") + " " + childCommandsName;
            this.childCommandsName = childCommandsName;
        }

        public String getParentCommandsName() {
            return parentCommandsName;
        }

        public String getChildCommandsRunPrompt(){ return this.childCommandsRunPrompt; }

        public void setChildCommandsRunPrompt(String childCommandsRunPrompt){ this.childCommandsRunPrompt = "Are you sure want to " + childCommandsRunPrompt.toLowerCase(); }
    }

}
