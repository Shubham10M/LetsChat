package com.example.letschat

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var verifyTv: TextView
    lateinit var waitingTv: TextView
    lateinit var counterTv: TextView
    lateinit var sendcodeEt: EditText
    private lateinit var enterTv: TextView
    lateinit var verificationBtn: Button
    lateinit var resendBtn: Button
    private lateinit var mAuth: FirebaseAuth
    var auth = FirebaseAuth.getInstance()
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber: String? = null
    var mVerificationId: String? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCounterDown: CountDownTimer? = null
    private var timeLeft: Long = -1
    private lateinit var progressDialog: ProgressDialog

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
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
       progressDialog = createProgressDialog("Sending a verification code", false)
       progressDialog.show()
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
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
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
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException", e)
                    Log.e("=========:", "FirebaseAuthInvalidCredentialsException " + e.message)
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.e("Exception:", "FirebaseTooManyRequestsException", e)
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
                progressDialog.dismiss()
                counterTv.isVisible = false
                // Save verification ID and resending token so we can use them later
                Log.e("onCodeSent==", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = false
        mCounterDown = object : CountDownTimer(milliSecInFuture, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                counterTv.isVisible = true
                counterTv.text = "Seconds remaining: " + millisUntilFinished / 1000


                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }



    private fun notifyUserAndRetry(s: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("sending code")
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
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeft", timeLeft)
        outState.putString(PHONE_NUMBER, phoneNumber)
    }

    // This code will send code to a given phonenumber as SMS
    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }
                    //First Time Login
                    if (task.result?.additionalUserInfo?.isNewUser == true) {
                        showSignUpActivity()
                    } else {
                        showHomeActivity()
                    }
                } else {

                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }

                    notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
                }
            }
    }
    private fun showSignUpActivity() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text))
        val clickSpan : ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                //super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }

        }
        span.setSpan(clickSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

                var code = sendcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrEmpty()) {

                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()
                    val credential =
                        PhoneAuthProvider.getCredential(mVerificationId!!, code.toString())
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if (mResendToken != null) {
                    resendVerificationCode(phoneNumber.toString(), mResendToken)
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()
                } else {
                   Toast.makeText(this, "Sorry, You Can't request new code now, Please wait ...", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
     // this is for resending code verification
    private fun resendVerificationCode(phoneNumber: String, mResendToken: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            mResendToken
        ) // ForceResendingToken from callbacks
    }

    fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
        return ProgressDialog(this).apply {
            setCancelable(isCancelable)
            setCanceledOnTouchOutside(false)
            setMessage(message)
        }
    }
}