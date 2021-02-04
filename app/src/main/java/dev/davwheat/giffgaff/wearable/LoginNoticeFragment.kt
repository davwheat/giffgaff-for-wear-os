package dev.davwheat.giffgaff.wearable

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class LoginNoticeFragment : Fragment() {
    private var _beginSignInButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var thisView = inflater.inflate(R.layout.fragment_login_notice, container, false)

        _beginSignInButton = thisView?.findViewById(R.id.signInButton)

        _beginSignInButton?.setOnClickListener {
            findNavController().navigate(R.id.action_loginNoticeFragment_to_loginEnterInfoFragment)
        }

        return thisView
    }

    companion object
}