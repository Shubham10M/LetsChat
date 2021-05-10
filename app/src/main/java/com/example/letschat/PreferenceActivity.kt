package com.example.letschat

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class PreferenceActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_preference,MainPreference())
                .commit()
        }
        else{
            title = savedInstanceState.getCharSequence(TAG_TITLE)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 0){
                setTitle(R.string.settings)
            }
        }
        setUpToolabar()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putCharSequence(TAG_TITLE,title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if(supportFragmentManager.popBackStackImmediate()){
            return true
        }
        return super.onSupportNavigateUp()
    }

    private fun setUpToolabar() {
        supportActionBar?.setTitle(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    class MainPreference : PreferenceFragmentCompat(){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.main_preference,rootKey)
        }

    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference?
    ): Boolean {
        //Initiate the new fragment
        val args  = pref?.extras
        val fragment = pref?.fragment.let {
                supportFragmentManager.fragmentFactory.instantiate(
                    classLoader,
                    it.toString()
                ).apply {
                    arguments =  args
                    setTargetFragment(caller,0)
                }

        }
       fragment?.let {
           supportFragmentManager.beginTransaction()
               .replace(R.id.content_preference, it)
               .addToBackStack(null)
               .commit()
       }
        title  = pref?.title
        return true
    }

   companion object {
       private val TAG_TITLE = "PREFERENCE_ACTIVITY"
   }
}