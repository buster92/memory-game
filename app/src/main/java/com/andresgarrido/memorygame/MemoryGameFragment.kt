package com.andresgarrido.memorygame

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.andresgarrido.memorygame.HomeViewModel.BoardType
import com.andresgarrido.memorygame.databinding.FragmentMemoryGameBinding

class MemoryGameFragment : Fragment() {

    private lateinit var viewModel: MemoryGameViewModel

    val args: MemoryGameFragmentArgs by navArgs()
    lateinit var binding: FragmentMemoryGameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoryGameBinding.inflate(inflater)

        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MemoryGameViewModel::class.java)
        binding.viewModel = viewModel

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                loadViews()
            }
        })

        viewModel.exitGameFlag.observe(viewLifecycleOwner, { isExit ->
            if (isExit) {
                activity?.finish()
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.finalize()
    }

    fun loadViews() {

        var cardsCount = 0
        when (args.boardType) {
            BoardType.SIZE_3X4 -> {
                binding.gameGrid.columnCount = 3
                cardsCount = 12
            }
            BoardType.SIZE_4X4 -> {
                binding.gameGrid.columnCount = 4
                cardsCount = 16
            }
            BoardType.SIZE_4X5 -> {
                binding.gameGrid.columnCount = 4
                cardsCount = 20
            }
            BoardType.SIZE_5X2 -> {
                binding.gameGrid.columnCount = 5
                cardsCount = 10
            }
            else -> {}
        }

        val totalPairs = cardsCount / 2

        context?.let { viewModel.init(it, totalPairs) }

        val cardWidth = binding.gameGrid.width / binding.gameGrid.columnCount
        val cardHeight = binding.gameGrid.height / (cardsCount / binding.gameGrid.columnCount)

        Log.d("MEMGAME", "card width: $cardWidth, height: $cardHeight")



        val cardsAvailable = arrayListOf(
                R.drawable.card_bat, R.drawable.card_cat, R.drawable.card_cow,
                R.drawable.card_dragon, R.drawable.card_garbageman, R.drawable.card_ghostdog,
                R.drawable.card_hen, R.drawable.card_horse, R.drawable.card_pig,  R.drawable.card_spider
        )
                .subList(0, totalPairs)

        Log.d("MEMGAME", "prev list: $cardsAvailable total cards:$cardsCount")
        cardsAvailable.addAll(cardsAvailable)
        cardsAvailable.shuffle()

        Log.d("MEMGAME", "new list: $cardsAvailable")

        for ((index, image: Int) in cardsAvailable.withIndex()) {
            val cardView = ImageView(context)
            cardView.setImageResource(R.drawable.card_bg)
            cardView.adjustViewBounds = true
            cardView.scaleType = ImageView.ScaleType.FIT_CENTER
            cardView.setPadding(8.px, 8.px, 8.px, 8.px)
            cardView.layoutParams = FrameLayout.LayoutParams(cardWidth, cardHeight)
            cardView.tag = CardData(image)
            cardView.setOnClickListener {
                viewModel.onCardClick(it)
            }
            binding.root.postDelayed({
                binding.gameGrid.addView(cardView)

            },  index * 80L)
        }

    }

}