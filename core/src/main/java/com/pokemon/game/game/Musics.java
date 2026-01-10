package com.pokemon.game.game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Musics {
    private Music backgroundmusic1,backgroundmusic2, backgroundmusic3, backgroundmusic4;

    public Musics() {
        try{
            backgroundmusic1=Gdx.audio.newMusic(Gdx.files.internal("music/02 - Title Screen.mp3"));
            backgroundmusic2=Gdx.audio.newMusic(Gdx.files.internal("music/15 - Pewter City.mp3"));
            backgroundmusic3=Gdx.audio.newMusic(Gdx.files.internal("music/1-15. Battle (VS Trainer).mp3"));
            backgroundmusic4=Gdx.audio.newMusic(Gdx.files.internal("music/1-38. Surf.mp3"));
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    //Iniciar musicas
    public void startmenumusic(){
        backgroundmusic1.setLooping(true);
        backgroundmusic1.setVolume(0.5f);    // Volumen entre 0.0 y 1.0
        backgroundmusic1.play();
    }


    public void startopenworldmusic(){
        backgroundmusic2.setLooping(true);
        backgroundmusic2.setVolume(0.5f);    // Volumen entre 0.0 y 1.0
        backgroundmusic2.play();
    }

    public void startBattleMusic(){
        backgroundmusic3.setLooping(true);
        backgroundmusic3.setVolume(0.5f);    // Volumen entre 0.0 y 1.0
        backgroundmusic3.play();
    }

    public void startpausemusic(){
        backgroundmusic4.setLooping(true);
        backgroundmusic4.setVolume(0.05f);    // Volumen entre 0.0 y 1.0
        backgroundmusic4.play();
    }

    //Detener las musicas
    public void stopmenumusic(){
        backgroundmusic1.stop();
    }

    public void stoppausemusic(){
        backgroundmusic4.stop();
    }

    public void stopopenworldmusic(){
        backgroundmusic2.stop();
    }

    // Deshacerse de las musicas
    public void disposemenumusic() {
        backgroundmusic1.dispose();
    }

    public void disposeopenworldmusic() {
        backgroundmusic2.dispose();
    }

    public void stopBattleMusic() { backgroundmusic3.stop();}
}
