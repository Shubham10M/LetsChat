package com.example.letschat

import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var verifyTv: TextView
    lateinit var waitingTv: TextView
    lateinit var counterTv: TextView
    lateinit var sendcodeEt: EditText
    lateinit var enterTv: TextView
    lateinit var verificationBtn: Button
    lateinit var resendBtn: Button
    private lateinit var mAuth: FirebaseAuth
    var auth = FirebaseAuth.getInstance()
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber: String? = null
    var mVerificationId: String? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCounterDown: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        verifyTv = findViewById(R.id.verifyTv)
        waitingTv = findViewById(R.id.waitingTv)
        counterTv = findViewById(R.id.counterTv)
        sendcodeEt = findViewById(R.id.sendcodeEt)
        enterTv = findViewById(R.id.enterTv)
        verificationBtn = findViewById(R.id.verificationBtn)
        resendBtn = findViewById(R.id.resendBtn)
        mAuth = FirebaseAuth.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()

        startVerify()

    }

    private fun startVerify() {
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber!!)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        showTimer(60000)
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = true
        mCounterDown = object : CountDownTimer(milliSecInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counterTv.text = getString(R.string.seconds_remaining, millisUntilFinished / 1000)

            }

            override fun onFinish() {
                // this is called when we reach to zero
                resendBtn.isEnabled = true
                counterTv.isVisible = true
            }

        }
                .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()

        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                val smsCode = credential.smsCode
                if (smsCode.isNullOrBlank()) {
                    sendcodeEt.setText(smsCode)
                }
                //Log.d(TAG, "onVerificationCompleted:$credential")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                // Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                notifyUserAndRetry("Your Phone number might be wrong or connection error. Retry agaian!")
                // ...
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }
    }

    private fun notifyUserAndRetry(s: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(" ")
            setPositiveButton("Ok") { _, _ ->

            }
            setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                            startActivity(Intent(this, SignupActivity ::  class.java))
                        // Log.d(TAG, "signInWithCredential:success")

                        val user = task.result?.user
                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI
                        notifyUserAndRetry("Your Phone number Verificaton Failed.Try Again!")
                        // Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }

        }
        span.setSpan(clickableSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

    }


    override fun onBackPressed() {


    }

    override fun onClick(v: View?) {
        when (v) {
            verificationBtn -> {

                val code = sendcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    MaterialAlertDialogBuilder(this).apply {
                        setMessage("Please wait ...")
                        createProgressDialog("Please Wait ....", false).show()

                    }
                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if (mResendToken != null) {
                    MaterialAlertDialogBuilder(this).apply {
                        showTimer(60000)
                        setMessage("Please wait ...")
                        createProgressDialog("Sending verification code ", false).show()

                        val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phoneNumber!!)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this@OtpActivity)                // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build()
                        mResendToken
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }

                }
            }
        }

    }

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(false)
            setMessage(message)
            setCanceledOnTouchOutside(false)

        }
    }
}