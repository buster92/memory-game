package com.andresgarrido.memorygame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController

class MemoryGameViewModel : ViewModel() {

    private var remainingPairs: Int = 0
    private val _score = MutableLiveData(0)
    val scoreText: LiveData<String> = Transformations.map(_score) {

        val score: Int = it
        if (score in 0..9) {
            "0".plus(score.toString())
        }
        else
            score.toString()

    }

    val exitGameFlag = MutableLiveData(false)

    lateinit var cardFlipPlayer: MediaPlayer
    lateinit var pairMatchPlayer: MediaPlayer
    lateinit var pairNotMatchPlayer: MediaPlayer
    lateinit var screenClearedPlayer: MediaPlayer

    fun init(context: Context, totalPairs: Int) {
        cardFlipPlayer = MediaPlayer.create(context, R.raw.card_flip)
        cardFlipPlayer.setVolume(1.0f, 1.0f)

        pairMatchPlayer = MediaPlayer.create(context, R.raw.casino_win)
        pairMatchPlayer.setVolume(1.0f, 1.0f)

        pairNotMatchPlayer = MediaPlayer.create(context, R.raw.wrong)
        pairNotMatchPlayer.setVolume(1.0f, 1.0f)

        screenClearedPlayer = MediaPlayer.create(context, R.raw.screen_cleared)
        screenClearedPlayer.setVolume(1.0f, 1.0f)

        remainingPairs = totalPairs
    }

    fun finalize() {
        cardFlipPlayer.release()
        pairMatchPlayer.release()
        pairNotMatchPlayer.release()
        screenClearedPlayer.release()
    }

    @SuppressLint("StaticFieldLeak")
    private var openedCardView:View? = null

    private var animating = false



    fun onBackClicked(v: View) {
        findNavController(v).navigateUp()
    }

    fun onCardClick(cardView: View) {
        val cardData = cardView.tag as CardData
        if (animating || cardData.state == CardState.SHOW)
            return
        animating = true
        cardFlipPlayer.start()

        val context = cardView.context
        val animatorSet = AnimatorSet()

        animatorSet.playSequentially(
                AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out)
                        .apply { setTarget(cardView) },

                )

        animatorSet.addListener({
            (cardView as ImageView).setImageResource(cardData.image)

            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(
                    AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in)
                            .apply { setTarget(cardView) },

                    )
            animatorSet.addListener({
                if (openedCardView != null) {
                    val openedCard = openedCardView?.tag as CardData
                    if (openedCard.image == cardData.image) {
                        _score.postValue(_score.value?.plus(50))
                        remainingPairs--
                        pairMatchPlayer.start()
                        animating = false
                        openedCardView = null

                        checkIfEndGame(cardView)
                    }
                    else {
                        pairNotMatchPlayer.start()
                        cardView.postDelayed({
                            animateBack(openedCardView)
                            animateBack(cardView)
                            _score.postValue(_score.value?.minus(10))
                            openedCardView = null
                            animating = false
                            cardFlipPlayer.start()
                        }, 1000)
                    }
                } else {
                    openedCardView = cardView
                    animating = false
                }
            })
            animatorSet.start()
        })
        animatorSet.start()
        cardData.state = CardState.SHOW
        cardView.tag = cardData
    }

    private fun animateBack(cardView: View?) {
        if (cardView == null) return
        val context = cardView.context
        val cardData = cardView.tag as CardData
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
                AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out)
                        .apply { setTarget(cardView) },

                )

        animatorSet.addListener({
            (cardView as ImageView).setImageResource(R.drawable.card_bg)

            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(
                    AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in)
                            .apply { setTarget(cardView) },

                    )
            animatorSet.addListener({
                cardData.state = CardState.HIDE
                cardView.tag = cardData
            })
            animatorSet.start()
        })
        animatorSet.start()
    }

    private fun checkIfEndGame(view: View) {
        if (remainingPairs == 0) {

            val alertDialog = AlertDialog.Builder(view.context)
                    .setView(R.layout.dialog_stage_clear)
                    .create()
            alertDialog.setOnShowListener {
                alertDialog.findViewById<Button>(R.id.button_try_again).setOnClickListener {
                    alertDialog.dismiss()
                    findNavController(view).navigateUp()
                }
                alertDialog.findViewById<Button>(R.id.button_exit_game).setOnClickListener {
                    alertDialog.dismiss()
                    exitGameFlag.postValue(true)
                }
            }
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
            screenClearedPlayer.start()
        }
    }
}