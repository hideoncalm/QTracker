package com.quyenln.runtracker.ui.run

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.quyenln.runtracker.R
import com.quyenln.runtracker.base.BaseFragment
import com.quyenln.runtracker.databinding.FragmentRunBinding
import com.quyenln.runtracker.ui.main.MainViewModel
import com.quyenln.runtracker.ui.run.adapter.RunAdapter
import com.quyenln.runtracker.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.quyenln.runtracker.utils.SortType
import com.quyenln.runtracker.utils.hasLocationPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : BaseFragment<FragmentRunBinding>(), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter

    override val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRunBinding =
        FragmentRunBinding::inflate

    override fun initViews(savedInstanceState: Bundle?) {
        requestPermissions()
        setUpRecyclerView()

        when(viewModel.sortType){
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.CALORIES -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.apply {
                    when (position) {
                        0 -> sortRuns(SortType.DATE)
                        1 -> sortRuns(SortType.RUNNING_TIME)
                        2 -> sortRuns(SortType.DISTANCE)
                        3 -> sortRuns(SortType.AVG_SPEED)
                        4 -> sortRuns(SortType.CALORIES)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.summitList(it)
        })
    }

    override fun initListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setUpRecyclerView(){
        runAdapter = RunAdapter()
        rvRuns.apply {
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun requestPermissions() {
        when {
            hasLocationPermissions(requireContext()) -> {
                return
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                EasyPermissions.requestPermissions(
                    this,
                    "This App Need Permissions",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
            else -> EasyPermissions.requestPermissions(
                this,
                "This App Need Permissions",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}