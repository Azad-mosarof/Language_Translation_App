package com.example.languagetranslation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.languagetranslation.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var RQ_CODE = 102
    private lateinit var tts: TextToSpeech
    private lateinit var bengaliEnglishTranslator: Translator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),102)
            SPEAK("Please give the Microphone Access")
        }
        else{
            SPEAK("Please tap on the microphone to say something")
        }

        // Create an Bengali-English translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.BENGALI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        bengaliEnglishTranslator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        bengaliEnglishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                Toast.makeText(this,"Model downloaded successfully. Okay to start translating",Toast.LENGTH_SHORT).show()
                binding.imageView3.setOnClickListener{
                    askSpeechInput()
                }
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                Toast.makeText(this,"Model couldn’t be downloaded or other internal error",Toast.LENGTH_SHORT).show()
            }
    }

    private fun translate(text: String){
        bengaliEnglishTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                binding.textView.text = translatedText
                SPEAK(translatedText)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,"Sorry some error occurred",Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RQ_CODE && resultCode == RESULT_OK) {
            val result: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = result?.get(0).toString()
            translate(text)
        }

    }

    private fun askSpeechInput(){
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn")
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something please!")
        try {
            this.startActivityForResult(i,RQ_CODE)
        } catch (e: Exception) {
            // on below line we are displaying error message in toast
            Toast
                .makeText(
                    this@MainActivity, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    private fun SPEAK(text: String){
        tts = TextToSpeech(applicationContext) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault()
                tts.setSpeechRate(1.0f)
                tts.speak(text, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }
}