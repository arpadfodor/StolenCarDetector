package com.arpadfodor.stolenvehicledetector.android.app.view.utils

import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.viewmodel.utils.RecognitionViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_recognition_detail.*

class RecognitionDetailFragment : Fragment(){

    companion object{

        const val TAG = "Recognition detail fragment"

        lateinit var viewModel: RecognitionViewModel
        var title = ""

        var sendSucceedSnackBarText = ""
        var sendFailedSnackBarText = ""
        var deletedSnackBarText = ""
        var alreadySentSnackBarText = ""
        var updateSucceedSnackBarText = ""
        var updateFailedSnackBarText = ""

        fun setParams(viewModel: RecognitionViewModel, title: String,
                      sendSucceedSnackBarText: String, sendFailedSnackBarText: String,
                      deletedSnackBarText: String, alreadySentSnackBarText: String,
                      updateSucceedSnackBarText: String, updateFailedSnackBarText: String){

            this.viewModel = viewModel
            this.title = title

            this.sendSucceedSnackBarText = sendSucceedSnackBarText
            this.sendFailedSnackBarText = sendFailedSnackBarText
            this.deletedSnackBarText = deletedSnackBarText
            this.alreadySentSnackBarText = alreadySentSnackBarText
            this.updateSucceedSnackBarText = updateSucceedSnackBarText
            this.updateFailedSnackBarText = updateFailedSnackBarText

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recognition_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recognition_detail_title.text = title
    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {

        // Create the observer
        val selectedRecognitionObserver = Observer<Int> { id ->

            val recognition = viewModel.getRecognitionById(id)
            recognition?.let {

                val recognitionImage = recognition.image
                recognitionImage?.let{
                    recognitionDetailImage?.setImageBitmap(recognitionImage)
                }

                //force done button on keyboard instead of the new line button
                recognitionDetailMessage?.setRawInputType(InputType.TYPE_CLASS_TEXT)
                recognitionDetailMessage?.text = SpannableStringBuilder(it.message)

                recognitionDetailLicenseId?.text = recognition.licenseId
                recognitionDetailDate?.text = recognition.date
                recognitionDetailLocation?.text =
                    requireContext().getString(R.string.recognition_item_location, recognition.longitude, recognition.latitude)

                recognition_back_button?.setOnClickListener {
                    viewModel.deselectRecognition()
                }

                recognitionDeleteButton?.setOnClickListener {

                    viewModel.deleteRecognition(recognition.artificialId){

                        val currentContext = context
                        val currentView = view
                        currentContext ?: return@deleteRecognition
                        currentView ?: return@deleteRecognition

                        viewModel.deselectRecognition()

                        AppSnackBarBuilder.buildInfoSnackBar(
                            currentContext,
                            currentView,
                            deletedSnackBarText,
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }

                }

                // if the recognition has been sent -> hide send button, disable editing message
                if(recognition.isSent){

                    recognitionSendButton?.setOnClickListener {

                        val currentContext = context
                        val currentView = view
                        currentContext ?: return@setOnClickListener
                        currentView ?: return@setOnClickListener

                        AppSnackBarBuilder.buildInfoSnackBar(
                            currentContext,
                            currentView,
                            alreadySentSnackBarText,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    recognitionDetailMessage?.isFocusable = false
                    recognitionDetailMessage?.isClickable = false
                    recognitionDetailMessage?.setOnEditorActionListener { textView, actionId, event ->
                        true
                    }

                }
                else{

                    recognitionSendButton?.setOnClickListener {

                        viewModel.sendRecognition(recognition.artificialId){ isSuccess ->

                            val currentContext = context
                            val currentView = view
                            currentContext ?: return@sendRecognition
                            currentView ?: return@sendRecognition

                            when(isSuccess){
                                true -> {
                                    AppSnackBarBuilder.buildSuccessSnackBar(
                                        currentContext,
                                        currentView,
                                        sendSucceedSnackBarText,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    AppSnackBarBuilder.buildAlertSnackBar(
                                        currentContext,
                                        currentView,
                                        sendFailedSnackBarText,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }

                        viewModel.deselectRecognition()

                    }

                    recognitionDetailMessage?.isFocusable = true
                    recognitionDetailMessage?.isClickable = true
                    recognitionDetailMessage?.setOnEditorActionListener { textView, actionId, event ->

                        val message = textView.text.toString()

                        return@setOnEditorActionListener when (actionId){
                            EditorInfo.IME_ACTION_DONE ->{
                                viewModel.updateRecognitionMessage(id, message){ isSuccess ->

                                    val currentContext = context
                                    val currentView = view
                                    currentContext ?: return@updateRecognitionMessage
                                    currentView ?: return@updateRecognitionMessage

                                    when(isSuccess){
                                        true -> {
                                            AppSnackBarBuilder.buildInfoSnackBar(
                                                currentContext,
                                                currentView,
                                                updateSucceedSnackBarText,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                        else -> {
                                            AppSnackBarBuilder.buildAlertSnackBar(
                                                currentContext,
                                                currentView,
                                                updateFailedSnackBarText,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                }
                                recognitionDetailMessage.clearFocus()
                                false
                            }
                            else -> {
                                false
                            }
                        }

                    }

                }

            }

        }

        // Observe the LiveData, passing in this viewLifeCycleOwner as the LifecycleOwner and the observer
        viewModel.selectedRecognitionId.observe(requireActivity(), selectedRecognitionObserver)

    }

}