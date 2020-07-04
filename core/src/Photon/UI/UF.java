package Photon.UI;

import arc.assets.AssetDescriptor;
import arc.func.Boolp;
import arc.func.Cons;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Array;
import arc.util.Log;
import mindustry.core.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UF extends UI {

    public UF(){

    }

    @Override
    public void loadAsync() {

    }

    @Override
    public void loadSync() {

    }

    @Override
    public Array<AssetDescriptor> getDependencies() {
        return super.getDependencies();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void init() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public TextureRegionDrawable getIcon(String name) {
        return super.getIcon(name);
    }

    @Override
    public void loadAnd(Runnable call) {

    }

    @Override
    public void loadAnd(String text, Runnable call) {

    }

    @Override
    public void showTextInput(String titleText, String dtext, int textLength, String def, boolean inumeric, Cons<String> confirmed) {
        super.showTextInput(titleText, dtext, textLength, def, inumeric, confirmed);
    }

    @Override
    public void showTextInput(String title, String text, String def, Cons<String> confirmed) {
        super.showTextInput(title, text, def, confirmed);
    }

    @Override
    public void showTextInput(String titleText, String text, int textLength, String def, Cons<String> confirmed) {
        super.showTextInput(titleText, text, textLength, def, confirmed);
    }

    @Override
    public void showInfoFade(String info) {
        reply(info);
    }

    @Override
    public void showInfoToast(String info, float duration) {
        reply(info);
    }

    @Override
    public void showInfoPopup(String info, float duration, int align, int top, int left, int bottom, int right) {
        reply(info);
    }

    @Override
    public void showLabel(String info, float duration, float worldx, float worldy) {
        reply(info);
    }

    @Override
    public void showInfo(String info) {
        reply(info);
    }

    @Override
    public void showErrorMessage(String text) {
        reply(text);
    }

    @Override
    public void showException(Throwable t) {
        reply(t.getMessage());
    }

    @Override
    public void showException(String text, Throwable exc) {
        reply(text, exc.getMessage());
    }

    @Override
    public void showExceptions(String text, String... messages) {
        reply(text, text);
    }

    @Override
    public void showText(String titleText, String text) {
        reply(titleText, text);
    }

    @Override
    public void showText(String titleText, String text, int align) {
        reply(titleText, text);
    }

    @Override
    public void showInfoText(String titleText, String text) {
        reply(titleText, text);
    }

    @Override
    public void showSmall(String titleText, String text) {
        reply(titleText, text);
    }

    @Override
    public void showConfirm(String title, String text, Runnable confirmed) {
        replyCustomConfirm(title, text, confirmed, ()->{});
    }

    @Override
    public void showConfirm(String title, String text, Boolp hide, Runnable confirmed) {
        replyCustomConfirm(title, text, confirmed, ()->{});
    }

    @Override
    public void showCustomConfirm(String title, String text, String yes, String no, Runnable confirmed, Runnable denied) {
       replyCustomConfirm(title,text, confirmed, denied);
    }

    @Override
    public void showOkText(String title, String text, Runnable confirmed) {
        replyConfirmOk(title, text, confirmed);
    }

    public void replyCustomConfirm(String title, String message, Runnable confirmed, Runnable denied){
        reply(title, message);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Type Yes/No to continue\n");
            String b = br.readLine();
            if(b.toLowerCase().equals("yes"))
                confirmed.run();
            else if(b.toLowerCase().equals("no"))
                denied.run();
        } catch (IOException ignored) { }
    }

    public void replyConfirmOk(String title, String message, Runnable confirmed){
        reply(title, message);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Type ok to continue\n");
       String b = br.readLine();
       if(b.toLowerCase().equals("ok"))
           confirmed.run();
        } catch (IOException ignored) { }

    }

    public void reply(String title, String message){
        Log.info(title + " say: " + message);
    }

    public void reply(String message){
        Log.info(message);
    }
}
