package com.ripenapps.adoreandroid.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.DialogBoostAvailableBinding
import com.ripenapps.adoreandroid.databinding.DialogInfoBinding
import com.ripenapps.adoreandroid.databinding.DialogOneButtonBinding
import com.ripenapps.adoreandroid.databinding.DialogYesNoBinding
import com.ripenapps.adoreandroid.databinding.DialogYesNoImageBinding
import com.ripenapps.adoreandroid.models.static_models.AppDialog


fun createYesNoDialog(
    listener: AppDialogListener,
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String = "Yes",
    negativeButtonText: String = "No",
    positiveButtonColor: Int = 1,   // 1 for black, any other for red
    isCancellable: Boolean = false,
    icon:Int=0,

) {
    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogYesNoBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_yes_no,
        null,
        false
    )
    alertDialog.setContentView(binding.root)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    binding.action = listener
    binding.d = alertDialog
    binding.dialog = AppDialog(title, message, positiveButtonText, negativeButtonText, positiveButtonColor, icon)
    alertDialog.setCancelable(isCancellable)
    alertDialog.show()
}

fun createSingleButtonPopup(
    listener: AppDialogListener,
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String = "Yes",
    negativeButtonText: String = "No",
    positiveButtonColor: Int,   // 1 for black, any other for red
    isCancellable: Boolean = false,
    icon:Int=0,

    ) {
    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogOneButtonBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_one_button,
        null,
        false
    )
    alertDialog.setContentView(binding.root)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    binding.action = listener
    binding.d = alertDialog
    binding.dialog = AppDialog(title, message, positiveButtonText, negativeButtonText, positiveButtonColor, icon)
    alertDialog.setCancelable(isCancellable)
    alertDialog.show()
}

fun createBoostAvailableDialog(
    listener: AppDialogListener,
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String = "Yes",
    negativeButtonText: String = "No",
    positiveButtonColor: Int = 1,   // 1 for black, any other for red
    isCancellable: Boolean = false,
    icon:Int=0,
    showButtons:Int=1
    ) {
    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogBoostAvailableBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_boost_available,
        null,
        false
    )
    alertDialog.setContentView(binding.root)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    binding.action = listener
    binding.d = alertDialog
    binding.dialog = AppDialog(title, message, positiveButtonText, negativeButtonText, positiveButtonColor, icon, showButtons)
    alertDialog.setCancelable(isCancellable)
    alertDialog.show()
}

fun createYesNoImageDialog(
    listener: AppDialogListener,
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String = "Yes",
    negativeButtonText: String = "No",
    positiveButtonColor: Int = 1,   // 1 for black, any other for red
    isCancellable: Boolean = false,
    icon:Int=0,
    showButtons:Int=1
) {
    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogYesNoImageBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_yes_no_image,
        null,
        false
    )
    alertDialog.setContentView(binding.root)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    binding.action = listener
    binding.d = alertDialog
    binding.dialog = AppDialog(title, message, positiveButtonText, negativeButtonText, positiveButtonColor, icon, showButtons)
    alertDialog.setCancelable(isCancellable)
    alertDialog.show()
}


fun createInfoDialog(
    context: Context,
    title: String,
    message: String,
    cancellable: Boolean = false
) {

    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogInfoBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_info,
        null,
        false
    )
    alertDialog.setContentView(binding.root)
    binding.dialog = AppDialog(title, message)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    alertDialog.setCancelable(cancellable)
    alertDialog.show()

}

fun createInfoDialogWithOption(
    context: Context,
    title: String,
    message: String,
    cancellable: Boolean = false,
    listener: AppDialogListener
) {

    val alertDialog = Dialog(context)
    val binding = DataBindingUtil.inflate<DialogInfoBinding>(
        LayoutInflater.from(context),
        R.layout.dialog_info,
        null,
        false
    )
    binding.tvContinue.visibility = View.VISIBLE
    binding.tvContinue.setOnClickListener { listener.onPositiveButtonClickListener(alertDialog) }
    alertDialog.setContentView(binding.root)
    binding.dialog = AppDialog(title, message)
    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    alertDialog.setCancelable(cancellable)
    alertDialog.show()

}


interface AppDialogListener {
    fun onPositiveButtonClickListener(dialog: Dialog)
    fun onNegativeButtonClickListener(dialog: Dialog)
}