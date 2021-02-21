package com.example.letschat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker

class LoginActivity : AppCompatActivity() {

    lateinit var txtWlc : TextView
    lateinit var txtVerify : TextView
    lateinit var txtinfo : TextView
    lateinit var  ccp : CountryCodePicker
    lateinit var phone_number_edt : EditText
    lateinit var Nxtbtn :TextView
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private lateinit var alertDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        txtWlc = findViewById(R.id.txtWlc)
        txtVerify = findViewById(R.id.txtVerify)
        txtinfo = findViewById(R.id.txtinfo)
        ccp = findViewById(R.id.ccp)
        phone_number_edt = findViewById(R.id.phone_number_edt)
        Nxtbtn = findViewById(R.id.Nxtbtn)


        phone_number_edt.addTextChangedListener {
            Nxtbtn.isEnabled = !(it.isNullOrBlank() || it.length < 10)
        }

        Nxtbtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + phone_number_edt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number:$phoneNumber\n" +
            "Is this OK, or would you like to edit the number?")
            setPositiveButton("Ok"){_,_ ->
             showOtpActivity()
            }
            setNegativeButton("Edit"){dialog,which ->
             dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this, OtpActivity::class.java ).putExtra(PHONE_NUMBER,phoneNumber))
            finish()
    }

}