package com.daugames.main;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound {

    private Clip clip;

    public static final Sound MUSIC_BG = new Sound("/music_bg.wav");

    public Sound(String path) {
        try {
            URL soundURL = getClass().getResource(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);

            clip = AudioSystem.getClip();
            clip.open(audioStream);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null) {
			return;
		}

        stop(); // garante que reinicia
        clip.setFramePosition(0);
        clip.start();
    }

    public void loop() {
        if (clip == null) {
			return;
		}

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if (clip == null) {
			return;
		}

        clip.stop();
    }
    
    public void setVolume(float volume) {
        if (clip == null) {
			return;
		}

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(volume); // ex: -10.0f
    }

}
