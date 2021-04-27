package com.andresgarrido.memorygame

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.view.View
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

    val shouldExit = MutableLiveData(false)

//    fun getScoreText(): String {
//        val score: Int = _score.value!!
//        return if (score in 0..9) {
//            return "0".plus(score.toString())
//        }
//        else
//            score.toString()
//    }


    private val _score = MutableLiveData(0)
    val scoreText: LiveData<String> = Transformations.map(_score) {

            val score: Int = it
            if (score in 0..9) {
                "0".plus(score.toString())
            }
            else
                score.toString()

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
                        Toast.makeText(context, "WELL DONE!", Toast.LENGTH_SHORT).show()
                        _score.postValue(_score.value?.plus(50))
                        animating = false
                        openedCardView = null
                    }
                    else {
                        cardView.postDelayed({
                            animateBack(openedCardView)
                            animateBack(cardView)
                            _score.postValue(_score.value?.minus(10))
                            openedCardView = null
                            animating = false
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
}