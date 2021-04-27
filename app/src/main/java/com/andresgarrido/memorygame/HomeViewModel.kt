package com.andresgarrido.memorygame

import android.content.Context
import android.media.MediaPlayer
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val isStartGameVisible = MutableLiveData(true)

    val isGameExit = MutableLiveData(false)


    private val mutableOptionChosen = MutableLiveData<BoardType>()
    val optionChosen: LiveData<BoardType> get() = mutableOptionChosen

    lateinit var mediaPlayer: MediaPlayer

    fun initSelectionAudio(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.selection)
        mediaPlayer.setVolume(1.0f, 1.0f)

    }

    fun selectMenuItem(v: View?, boardType: BoardType) {
        if (v == null) {
            mutableOptionChosen.value = boardType
            return
        }
        playSound() {
            mutableOptionChosen.value = boardType
        }
    }

    fun onStartGameClick(v: View) {
        playSound {
            isStartGameVisible.postValue(false)
        }
    }

    fun onExitClick(v: View) {
        playSound {
            isGameExit.postValue(true)
        }
    }

    private fun playSound(onComplete: () -> Unit) {
        mediaPlayer.setOnCompletionListener { onComplete() }
        mediaPlayer.start()
    }
    public enum class BoardType {
        NONE,
        SIZE_3X4,
        SIZE_5X2,
        SIZE_4X4,
        SIZE_4X5
    }
}