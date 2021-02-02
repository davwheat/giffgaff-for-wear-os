package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginEnterInfoFragment : Fragment() {

    private var _signInButton: Button? = null

    private var _inAnimation: AlphaAnimation? = null
    private var _outAnimation: AlphaAnimation? = null
    private var _progressBarHolder: FrameLayout? = null

    private var _membernameEl: EditText? = null
    private var _passwordEl: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var thisView = inflater.inflate(R.layout.fragment_enter_login_info, container, false)

        _signInButton = thisView?.findViewById(R.id.signInButton)
        _progressBarHolder = thisView?.findViewById(R.id.progressBarHolder)
        _membernameEl = thisView?.findViewById(R.id.memberNameTextField)
        _passwordEl = thisView?.findViewById(R.id.passwordTextField)

        _signInButton?.setOnClickListener {
            val membername = _membernameEl?.text.toString()
            val password = _passwordEl?.text.toString()

            // Check for blank membername or password
            if (membername == "" || password == "") {
                Toast.makeText(
                    context,
                    getString(R.string.sign_in_blank_details_message),
                    Toast.LENGTH_LONG
                ).show()

            } else {
                showLoadingSpinner()

                if (membername == getString(R.string.testing_username) && password == getString(R.string.testing_password)) {
                    Toast.makeText(
                        context,
                        getString(R.string.welcome_member, membername),
                        Toast.LENGTH_LONG
                    )
                        .show()

                    requireActivity().getPreferences(Context.MODE_PRIVATE)
                        .edit()
                        .putString("membername", membername)
                        .putString("password", password)
                        .putString("token", getString(R.string.testing_token))
                        .apply()


                    Log.d(
                        "giffgaff Wear",
                        "Member name: $membername\nPassword: <hidden>\nToken: N/A..."
                    )

                    findNavController().navigate(R.id.action_loginEnterInfoFragment_to_accountDetailsFragment)
                    hideLoadingSpinner()
                } else {
                    makeTokenRequest(membername, password, requireContext()) { token ->
                        if (token == null) {
                            // Invalid token
                            Toast.makeText(
                                context,
                                getString(R.string.unknown_error),
                                Toast.LENGTH_LONG
                            ).show()
                            hideLoadingSpinner()
                        } else if (token.startsWith("ERR:")) {
                            // Other error
                            Toast.makeText(
                                context,
                                token.substring(4),
                                Toast.LENGTH_LONG
                            ).show()
                            hideLoadingSpinner()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.welcome_member, membername),
                                Toast.LENGTH_LONG
                            )
                                .show()

                            requireActivity().getPreferences(Context.MODE_PRIVATE)
                                .edit()
                                .putString("membername", membername)
                                .putString("password", password)
                                .putString("token", token)
                                .apply()


                            Log.d(
                                "giffgaff Wear",
                                "Member name: $membername\nPassword: <hidden>\nToken: ${
                                    token.substring(
                                        0,
                                        16
                                    )
                                }..."
                            )

                            findNavController().navigate(R.id.action_loginEnterInfoFragment_to_accountDetailsFragment)
                            hideLoadingSpinner()
                        }
                    }
                }
            }
        }

        return thisView
    }

    fun showLoadingSpinner() {
        _inAnimation = AlphaAnimation(0f, 1f)
        _inAnimation?.duration = 200
        _progressBarHolder?.animation = _inAnimation
        _progressBarHolder?.visibility = View.VISIBLE
    }

    fun hideLoadingSpinner() {
        _outAnimation = AlphaAnimation(1f, 0f)
        _outAnimation?.duration = 200
        _progressBarHolder?.animation = _outAnimation
        _progressBarHolder?.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginEnterInfoFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}