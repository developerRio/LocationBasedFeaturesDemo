package com.originalstocks.locationbasedfeaturedemo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showSnackBar(
    rootView: View,
    message: String,
    action: String,
    onClickListener: View.OnClickListener
) {
    val snackBar: Snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
    snackBar.setAction(action, onClickListener)
    snackBar.setActionTextColor(ColorStateList.valueOf(Color.parseColor("#FFC107")))
    snackBar.show()
}