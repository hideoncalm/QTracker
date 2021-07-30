package com.quyenln.runtracker.ui.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.quyenln.runtracker.base.BaseFragment
import com.quyenln.runtracker.databinding.FragmentSettingsBinding
import com.quyenln.runtracker.utils.Constants.KEY_NAME
import com.quyenln.runtracker.utils.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingsBinding>() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    override val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingsBinding =
        FragmentSettingsBinding::inflate

    override fun initViews(savedInstanceState: Bundle?) {
        loadFromSharedPref()
    }

    override fun initListeners() {
        btnApplyChanges.setOnClickListener {
            val success = applyChangedToSharedPref()
            if (success) Snackbar.make(requireView(), "Saved Changes", Snackbar.LENGTH_LONG).show()
            else Snackbar.make(requireView(), "Enter Empty Fields", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadFromSharedPref() {
        val name = sharedPref.getString(KEY_NAME, "")
        val weight = sharedPref.getFloat(KEY_WEIGHT, 50f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangedToSharedPref(): Boolean {
        val nameText = etName.text.toString()
        val weight = etWeight.text.toString()
        if (nameText.isEmpty() || weight.isEmpty()) return false
        sharedPref.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .apply()
        return true
    }
}