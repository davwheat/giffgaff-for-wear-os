package dev.davwheat.giffgaff.wearable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class InvalidSslErrorHelp : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var thisView = inflater.inflate(R.layout.fragment_invalid_ssl_error_help, container, false)

        return thisView
    }

    companion object {}
}