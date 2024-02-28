package com.example.motivateme

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motivateme.ui.theme.MotivateMeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotivateMeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Home() {
    // Logic to get word of the day with associated quote

    var apiResponse by remember { mutableStateOf(ApiResponse("", "")) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    var checked by remember { mutableStateOf(true) }

    // Fetch word of the day with associated quote
    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            try {
                apiResponse = getQuoteFromApi()

            } finally {

                isLoading = false
            }
        }

    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Word Of The Day",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                },
                actions = {
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        thumbContent = if (checked) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                },
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
            )

            //Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Text("Loading...")
            } else {
                WordCard(apiResponse.text, apiResponse.author)
            }
        }

    }

}


@Composable
fun WordCard(text: String, author: String) {
    // Card UI
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(200.dp),
//        elevation = CardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = author,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(16.dp)
                )

            }
        }
    }
}

data class ApiResponse(
    val text: String,
    val author: String,
)

object ApiConfig {
    const val BASE_URL = "https://quotes-inspirational-quotes-motivational-quotes.p.rapidapi.com/quote?token=ipworld.info"
    const val API_KEY = ""
    const val HOST = "quotes-inspirational-quotes-motivational-quotes.p.rapidapi.com"
}

fun getQuoteFromApi(): ApiResponse {

    val client = OkHttpClient()

    val request = Request.Builder()
        .url(ApiConfig.BASE_URL)
        .get()
        .addHeader("X-RapidAPI-Key", ApiConfig.API_KEY)
        .addHeader("X-RapidAPI-Host", ApiConfig.HOST)
        .build()

    return try {
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: ""
            val jsonObject = JSONObject(responseBody)
            val text = jsonObject.optString("text", "")
            val author = jsonObject.optString("author", "")
            return ApiResponse(text, author)
        } else {
            ApiResponse("Request failed with code: ${response.code}", "")
        }
    } catch (e: IOException) {
        ApiResponse("Network error occurred: ${e.message}", "")
    } catch (e: JSONException) {
        return ApiResponse("Error parsing JSON: ${e.message}", "")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MotivateMeTheme {
        Home()
    }
}