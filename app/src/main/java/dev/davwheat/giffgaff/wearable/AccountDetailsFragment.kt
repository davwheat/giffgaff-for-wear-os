package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.navigation.fragment.findNavController

class AccountDetailsFragment : Fragment() {

    private lateinit var _token: String;
    private lateinit var _preferences: SharedPreferences;

    private var _inAnimation: AlphaAnimation? = null
    private var _outAnimation: AlphaAnimation? = null
    private var _fetchingDataNoticeView: LinearLayout? = null
    private var _allDataView: LinearLayout? = null

    private lateinit var _welcomeText: TextView

    private lateinit var _goodybagImage: ImageView;

    private lateinit var _mobileNumberText: TextView
    private lateinit var _goodybagDataRemainingTextView: TextView;
    private lateinit var _goodybagUnlimitedCallsTexts: TextView;
    private lateinit var _noGoodybag: TextView;
    private lateinit var _creditText: TextView;

    private lateinit var _goodybagDataMeter: ProgressBar;

    private lateinit var _refreshDataButton: Button;
    private lateinit var _logOutButton: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var thisView = inflater.inflate(R.layout.fragment_account_details, container, false)

        _preferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        _welcomeText = thisView!!.findViewById(R.id.welcomeMemberTextView)

        _goodybagImage = thisView.findViewById(R.id.goodybagImage)

        _fetchingDataNoticeView = thisView.findViewById(R.id.fetchingDataMessageContainer)
        _allDataView = thisView.findViewById(R.id.allDataContainer)

        _mobileNumberText = thisView.findViewById(R.id.mobileNumber)
        _goodybagDataRemainingTextView = thisView.findViewById(R.id.goodybagDataRemainingTextView)
        _goodybagUnlimitedCallsTexts = thisView.findViewById(R.id.unlimitedCallsAndTexts)
        _noGoodybag = thisView.findViewById(R.id.noGoodybag)
        _creditText = thisView.findViewById(R.id.creditText)

        _goodybagDataMeter = thisView.findViewById(R.id.dataMeter)

        _refreshDataButton = thisView.findViewById(R.id.refreshDataButton)
        _logOutButton = thisView.findViewById(R.id.logOutButton)

        _welcomeText.text =
            getString(R.string.account_welcome, _preferences.getString("membername", "napaman"))

        _refreshDataButton.setOnClickListener {
            showFetchingDataView()
            loadData()
        }

        _logOutButton.setOnClickListener {
            showFetchingDataView()
            invalidateAccountDetails()
            hideFetchingDataView()
        }

        return thisView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData();
    }

    fun loadData() {
        showFetchingDataView()

        val token = _preferences.getString("token", "")

        if (token == "") {
            invalidateAccountDetails()
            return;
        }

        GetAccountInfo(token!!, requireContext()) { data ->
            if (data == null) {
                // Invalid data received, or error occurred.
                invalidateAccountDetails()
            } else {
                // Valid data!
                _mobileNumberText.text = data.phoneNumber
                _creditText.text = getString(
                    R.string.account_credit_balance,
                    data.creditBalance.toDouble() / 100
                )

                val goodybag = data.goodybag;

                if (goodybag != null) {
                    // goodybag on account
                    _goodybagDataRemainingTextView.visibility =
                        VISIBLE
                    _goodybagUnlimitedCallsTexts.visibility =
                        VISIBLE
                    _noGoodybag.visibility = GONE

                    // set the image for it
                    val goodybagDrawable =
                        resources.getIdentifier(goodybag.goodybagDrawableName, null, null)
                    _goodybagImage.setImageResource(goodybagDrawable)

                    // set remaining data text
                    _goodybagDataRemainingTextView.text =
                        getString(R.string.account_data_remaining, "${goodybag.dataRemainingGB} GB")

                    _goodybagDataMeter.visibility = VISIBLE
                    _goodybagDataMeter.max = if (goodybag.hasReserve) {
                        goodybag.dataAllowance
                    } else {
                        goodybag.dataAllowance + goodybag.reserveAllowance!!
                    }
                    _goodybagDataMeter.progress = if (goodybag.hasReserve) {
                        goodybag.dataRemaining
                    } else {
                        goodybag.dataRemaining + goodybag.reserveRemaining!!
                    }
                } else {
                    // No goodybag
                    view?.findViewById<ImageView>(R.id.goodybagImage)
                        ?.setImageResource(R.drawable.goodybag_blank)

                    _noGoodybag.visibility = VISIBLE
                    _goodybagDataRemainingTextView.visibility =
                        GONE
                    _goodybagUnlimitedCallsTexts.visibility =
                        GONE
                    _goodybagDataMeter.visibility = GONE
                }

                hideFetchingDataView()
            }
        }

    }

    fun showFetchingDataView() {
        _inAnimation = AlphaAnimation(0f, 1f)
        _inAnimation?.duration = 200
        _fetchingDataNoticeView?.animation = _inAnimation
        _allDataView?.visibility = GONE
        _fetchingDataNoticeView?.visibility = VISIBLE
    }

    fun hideFetchingDataView() {
        _outAnimation = AlphaAnimation(1f, 0f)
        _outAnimation?.duration = 200
        _fetchingDataNoticeView?.animation = _outAnimation
        _fetchingDataNoticeView?.visibility = GONE
        _allDataView?.visibility = VISIBLE
    }

    private fun invalidateAccountDetails() {
        // Let's attempt to recover by resetting our saved account info and asking to log back in.
        _preferences.edit().clear().apply()
        findNavController().navigate(R.id.action_accountDetailsFragment_to_loginNoticeFragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AccountDetailsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}