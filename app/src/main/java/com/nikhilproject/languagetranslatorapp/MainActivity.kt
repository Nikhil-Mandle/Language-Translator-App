package com.nikhilproject.languagetranslatorapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.nikhilproject.languagetranslatorapp.service.TranslatorService
import com.nikhilproject.languagetranslatorapp.ui.theme.LanguageTranslatorAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val speechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }

    private var service: TranslatorService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as TranslatorService.TranslatorBinder).getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }

    }


    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageTranslatorAppTheme {
                Surface(
                    modifier = Modifier
                        .safeContentPadding()
                        .fillMaxSize()
                ) {
                    val permission = rememberPermissionState(
                        permission = android.Manifest.permission.RECORD_AUDIO
                    )

                    LaunchedEffect(key1 = Unit) {
                        permission.launchPermissionRequest()
                    }
                    val text = remember { mutableStateOf("") }

                    val isListen = remember { mutableStateOf(false) }

                    LaunchedEffect(key1 = isListen.value) {
                        if (isListen.value) {
                            listen(onStartListen = {
                                text.value = "Start listening"
                            }, onResult = {

                                text.value = it
                                service?.translate(text.value, onResult = {
                                    text.value = it
                                    isListen.value = false
                                }, onFailure = {
                                    isListen.value = false
                                    speechRecognizer.stopListening()
                                    speechRecognizer.destroy()
                                })
                            })

                        }

                    }

                    Scaffold(
                        topBar = {
                            TopAppBar(title = {
                                Text(text = "Language Translator")
                            })
                        },
                        floatingActionButton = {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isListen.value) Color.Green else Color.Red
                                )
                            ) {
                                IconButton(onClick = {
                                    isListen.value = !isListen.value
                                }) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "")
                                }
                            }
                        }
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(it)
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(value = text.value, onValueChange = {
                                text.value = it
                            }, modifier = Modifier.fillMaxWidth(), minLines = 5)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(onClick = {
                                service?.translate(text.value, onResult = {
                                    text.value = it
                                }, onFailure = {})
                            }) {
                                Text(text = "Translate")
                            }
                        }
                    }
                }
            }
        }
    }


    fun listen(onStartListen: () -> Unit, onResult: (String) -> Unit) {
        val intent = Intent().apply {
            action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {
                onStartListen.invoke()
            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {
                speechRecognizer.stopListening()
            }

            override fun onError(error: Int) {

            }

            override fun onResults(bundle: Bundle?) {
                bundle?.let {
                    val result =
                        bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.first()
                    result?.let(onResult)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        })

        speechRecognizer.startListening(intent)
    }


    override fun onStart() {
        super.onStart()

        val intent = Intent(this, TranslatorService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }
}
