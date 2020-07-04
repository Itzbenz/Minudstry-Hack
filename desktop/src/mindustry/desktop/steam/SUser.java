package mindustry.desktop.steam;

import com.codedisaster.steamworks.SteamAuth.AuthSessionResponse;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;

public class SUser implements SteamUserCallback{
    public final SteamUser user = new SteamUser(this);

    @Override
    public void onValidateAuthTicket(SteamID steamID, AuthSessionResponse authSessionResponse, SteamID ownerSteamID){

    }

    @Override
    public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized){

    }

    @Override
    public void onEncryptedAppTicket(SteamResult result){

    }
}
