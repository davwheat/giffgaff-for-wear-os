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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var thisView = inflater.inflate(R.layout.fragment_login_notice, container, false)

        _beginSignInButton = thisView?.findViewById<Button>(R.id.signInButton)

        _beginSignInButton?.setOnClickListener {
            findNavController().navigate(R.id.action_loginNoticeFragment_to_loginEnterInfoFragment)
        }

        return thisView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginNoticeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}