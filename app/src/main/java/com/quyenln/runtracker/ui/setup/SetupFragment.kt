package com.quyenln.runtracker.ui.setup

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.quyenln.runtracker.R
import com.quyenln.runtracker.base.BaseFragment
import com.quyenln.runtracker.databinding.FragmentSetupBinding
import com.quyenln.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.quyenln.runtracker.utils.Constants.KEY_NAME
import com.quyenln.runtracker.utils.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSetupBinding =
        FragmentSetupBinding::inflate

    override fun initViews(savedInstanceState: Bundle?) {
        if (!isFirstAppOpen) {
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOption
            )
        }
    }

    override fun initListeners() {
        tvContinue.setOnClickListener {
            val success = writeToSharePreference()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(
                    requireView(),
                    "Please enter the field",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeToSharePreference(): Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) return false
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }
}