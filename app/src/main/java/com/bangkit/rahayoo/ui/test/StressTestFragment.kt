package com.bangkit.rahayoo.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bangkit.rahayoo.R
import com.bangkit.rahayoo.data.model.StressTestAnswer
import com.bangkit.rahayoo.data.model.StressTestQuestions
import com.bangkit.rahayoo.data.model.UiState
import com.bangkit.rahayoo.databinding.FragmentStressTestBinding
import com.bangkit.rahayoo.di.Injection
import com.bangkit.rahayoo.ui.ViewModelFactory
import com.bangkit.rahayoo.util.toEmoteValue
import com.bangkit.rahayoo.util.toProgressValue
import com.bangkit.rahayoo.util.toScaleValue
import com.google.android.material.snackbar.Snackbar
import com.robinhood.ticker.TickerUtils

class StressTestFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "StressTestFragment"
    }

    private var _binding: FragmentStressTestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StressTestViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private var questionCounter = 0

    private val questions = StressTestQuestions.getAllQuestion()
    private var currentQuestion = questions[questionCounter]
    private val answeredQuestion = StressTestAnswer(mutableListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStressTestBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.slider.addOnChangeListener { _, value, _ ->
            binding.cpiSlider.progress = value.toProgressValue()
            binding.tvLabelAnswer.text = value.toScaleValue().toString()
            binding.tvAnswerEmote.text = value.toEmoteValue(currentQuestion.questionType)
        }

        binding.btnStressTest.setOnClickListener(this)
        binding.btnQuestionNavigate.setOnClickListener(this)
        binding.btnClose.setOnClickListener(this)

        binding.tvLabelAnswer.setCharacterLists(TickerUtils.provideNumberList())
        binding.tvStressLevelHeadline.setCharacterLists(TickerUtils.provideAlphabeticalList())

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is UiState.Loading -> {
                    Snackbar.make(
                        binding.root,
                        "Submitting Answer",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                is UiState.Success -> {
                    Snackbar.make(
                        binding.root,
                        "Answer Submitted",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.getUserData {
                        showResult()
                    }
                }


                is UiState.Error -> {
                    // Show Error
                    Snackbar.make(
                        binding.root,
                        "Error submitting answer: ${uiState.error}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupQuestion() {
        binding.tvStressLevelIntroTitle.visibility = View.GONE
        binding.tvStressLevelIntroSubtitle.visibility = View.GONE
        binding.btnStressTest.visibility = View.GONE
        binding.cvQuestionContainer.visibility = View.VISIBLE
        binding.btnQuestionNavigate.visibility = View.VISIBLE
        binding.tvQuestionHeader.visibility = View.VISIBLE

        binding.lpiQuestion.progress = 10
        binding.tvStressLevelHeadline.text =
            getString(R.string.question_counter_headline, questionCounter + 1)
        binding.tvQuestion.text = currentQuestion.question
    }

    private fun showResult() {
        binding.cvQuestionContainer.visibility = View.GONE
        binding.tvQuestionHeader.visibility = View.GONE
        binding.tvStressLevelHeadline.text = getString(R.string.stress_level_test)
        binding.ivStressResult.visibility = View.VISIBLE
        binding.tvStressResult.visibility = View.VISIBLE
        binding.cvStressResultInfoContainer.visibility = View.VISIBLE
        binding.btnStressTest.text = getString(R.string.done)
        binding.btnQuestionNavigate.visibility = View.GONE
        binding.btnStressTest.visibility = View.VISIBLE

        val userData = viewModel.userData.value

        when (val stressLevel = userData?.weeklyStressAvg ?: 0F) {
            in 0F..15F -> {
                binding.ivStressResult.setImageResource(R.drawable.item_stress_result_low)
                binding.tvStressResult.text = getString(R.string.stress_level_result, stressLevel, "Low")
                binding.tvStressResultInfo.setText(R.string.stress_level_result_low)
                binding.tvStressResultInfoTips.setText(R.string.stress_level_low_info_tips)
            }
            in 15F..30F -> {
                binding.ivStressResult.setImageResource(R.drawable.item_stress_result_moderate)
                binding.tvStressResult.text = getString(R.string.stress_level_result, stressLevel, "Moderate")
                binding.tvStressResultInfo.setText(R.string.stress_level_result_moderate)
                binding.tvStressResultInfoTips.setText(R.string.stress_level_moderate_info_tips)
            }
            else -> {
                binding.ivStressResult.setImageResource(R.drawable.item_stress_result_high)
                binding.tvStressResult.text = getString(R.string.stress_level_result, stressLevel, "High")
                binding.tvStressResultInfo.setText(R.string.stress_level_result_high)
                binding.tvStressResultInfoTips.setText(R.string.stress_level_high_info_tips)
            }
        }

    }

    private fun nextQuestion() {
        if (questionCounter == questions.size - 1) {
            viewModel.submitAnswer(answeredQuestion)
            return
        }

        answeredQuestion.stressLevel.add(binding.slider.value.toScaleValue())
        currentQuestion = questions[++questionCounter]

        resetSlider()

        binding.lpiQuestion.incrementProgressBy(10)
        binding.tvStressLevelHeadline.text =
            getString(R.string.question_counter_headline, questionCounter + 1)
        binding.tvQuestion.text = currentQuestion.question

        if (questionCounter == questions.size - 1) {
            binding.btnQuestionNavigate.text = getString(R.string.submit)
        }
    }

    private fun resetSlider() {
        binding.slider.value = 0F
        binding.cpiSlider.progress = 0
        binding.tvAnswerEmote.text = 0F.toEmoteValue(currentQuestion.questionType)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_stress_test -> {
                if (questionCounter == questions.size - 1) {
                    findNavController().navigateUp()
                } else setupQuestion()
            }

            R.id.btn_question_navigate -> {
                nextQuestion()
            }

            R.id.btn_close -> {
                findNavController().navigateUp()
            }
        }
    }
}